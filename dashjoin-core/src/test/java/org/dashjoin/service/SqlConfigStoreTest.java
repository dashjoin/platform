package org.dashjoin.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * config served from SQL: import of a git checkout, layers, deletion markers, app sync, locking.
 *
 * The PostgreSQL variant runs when DJ_TEST_PG_URL (and DJ_TEST_PG_USER, DJ_TEST_PG_PASSWORD) are
 * set. It works in a schema of its own, which is dropped afterwards.
 */
public class SqlConfigStoreTest {

  @TempDir
  Path tmp;

  static final Table PAGE = Table.ofName("page");
  static final Table FUNCTION = Table.ofName("dj-function");
  static final Table TENANTUSERS = Table.ofName("tenantusers");

  Path home;
  Git git;

  void write(String path, String content) throws Exception {
    Path f = home.resolve(path);
    Files.createDirectories(f.getParent());
    Files.writeString(f, content);
  }

  void commit(String message) throws Exception {
    git.add().addFilepattern(".").call();
    git.add().addFilepattern(".").setUpdate(true).call();
    git.commit().setMessage(message).setAuthor("test", "test@example.org")
        .setCommitter("test", "test@example.org").call();
  }

  /** an app checkout: one commit plus local changes in the working tree */
  void createApp() throws Exception {
    home = Files.createDirectories(tmp.resolve("app"));
    git = Git.init().setDirectory(home.toFile()).call();
    write("model/page/a.json", "{\"ID\": \"a\", \"title\": \"A\", \"color\": \"red\"}");
    write("model/dj-function/f.json",
        "{\"ID\": \"f\", \"djClassName\": \"x\", \"expression-pointer\": \"expression.jsonata\"}");
    write("model/dj-function/f.expression.jsonata", "1 + 1");
    write("model/tenantusers/@EVERYONE.json",
        "{\"ID\": \"@EVERYONE\", \"active\": true, \"roles\": [\"user\"]}");
    write("model/tenantusers/@gone.json", "{\"ID\": \"@gone\", \"active\": true}");
    write("upload/file.txt", "not config");
    write(".gitignore", "model/dj-function/secret.json\n");
    commit("app v1");

    // local changes, not committed
    write("model/page/a.json", "{\"ID\": \"a\", \"title\": \"A local\", \"color\": \"red\"}");
    write("model/dj-function/secret.json", "{\"ID\": \"secret\", \"djClassName\": \"y\"}");
    Files.delete(home.resolve("model/tenantusers/@gone.json"));
  }

  /** the union database as Services sets it up: attaching brings the effective records up to date */
  UnionDatabase union(SqlConfigStore store) throws Exception {
    store.attach(List.of(store.app()));
    UnionDatabase u = new UnionDatabase();
    u._user = store.local();
    u._dbs = new ArrayList<>(List.of(store.app()));
    return u;
  }

  static Map<String, Object> map(Object... kv) {
    Map<String, Object> res = new HashMap<>();
    for (int i = 0; i < kv.length; i += 2)
      res.put((String) kv[i], kv[i + 1]);
    return res;
  }

  static List<Object> ids(List<Map<String, Object>> records) {
    return records.stream().map(r -> r.get("ID")).sorted().map(Object.class::cast).toList();
  }

  /**
   * the effective records maintained incrementally are the same as the ones of a full rebuild, and
   * the same as merging the layers in memory
   */
  void assertEffectiveConsistent(SqlConfigStore store) throws Exception {
    Map<String, Map<String, Map<String, Object>>> incremental =
        store.readLayer(SqlConfigStore.EFFECTIVE);
    store.transaction(c -> {
      store.rebuildEffective(c);
      return null;
    });
    Assertions.assertEquals(store.readLayer(SqlConfigStore.EFFECTIVE), incremental);

    UnionDatabase inMemory = new UnionDatabase() {
      @Override
      JSONSqlDatabase effective(String table) {
        return null;
      }
    };
    inMemory._user = store.local();
    inMemory._dbs = List.of(store.app());
    for (String table : incremental.keySet())
      for (Map<String, Object> r : incremental.get(table).values())
        Assertions.assertEquals(inMemory.read(Table.ofName(table), Map.of("ID", r.get("ID"))), r);
  }

  List<Map<String, Object>> all(UnionDatabase u, String table) throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = table;
    return u.query(q, null);
  }

  @SuppressWarnings("unchecked")
  void scenario(SqlConfigStore store) throws Exception {
    createApp();
    store.bootstrap(home.toString(), false);
    UnionDatabase u = union(store);

    // import: HEAD is the app layer, the working tree changes are the local layer
    Assertions.assertEquals(map("ID", "a", "title", "A local", "color", "red"),
        u.read(PAGE, Map.of("ID", "a")));
    Assertions.assertEquals(map("ID", "a", "title", "A local"),
        store.local().read(PAGE, Map.of("ID", "a")), "only the changed field is local");
    Assertions.assertEquals(map("ID", "f", "djClassName", "x", "expression", "1 + 1"),
        u.read(FUNCTION, Map.of("ID", "f")), "externalized string inlined, pointer dropped");
    Assertions.assertEquals(map("ID", "secret", "djClassName", "y"),
        store.local().read(FUNCTION, Map.of("ID", "secret")), "local only record");
    Assertions.assertNull(u.read(TENANTUSERS, Map.of("ID", "@gone")), "deleted locally");
    Assertions.assertEquals(List.of("@EVERYONE"), ids(all(u, "tenantusers")));

    // runtime writes go to the local layer
    u.create(TENANTUSERS, map("ID", "joe@x.org", "active", false));
    u.update(TENANTUSERS, Map.of("ID", "@EVERYONE"), map("roles", List.of("user", "admin")));
    Assertions.assertEquals(map("ID", "@EVERYONE", "roles", List.of("user", "admin")),
        store.local().read(TENANTUSERS, Map.of("ID", "@EVERYONE")));
    Assertions.assertEquals(List.of("@EVERYONE", "joe@x.org"), ids(all(u, "tenantusers")));

    // deleting an app record leaves a marker; creating it again removes the marker
    Assertions.assertTrue(u.delete(PAGE, Map.of("ID", "a")));
    Assertions.assertNull(u.read(PAGE, Map.of("ID", "a")));
    Assertions.assertTrue(all(u, "page").isEmpty());
    Assertions.assertFalse(u.update(PAGE, Map.of("ID", "a"), map("title", "x")),
        "a deleted record cannot be updated");
    u.create(PAGE, map("ID", "a", "title", "A again"));
    Assertions.assertEquals("A again", u.read(PAGE, Map.of("ID", "a")).get("title"));
    u.delete(PAGE, Map.of("ID", "a"));
    u.update(TENANTUSERS, Map.of("ID", "@EVERYONE"), map("roles", List.of("user", "admin")));
    assertEffectiveConsistent(store);

    // queries: filter, sort, paging (in SQL on PostgreSQL)
    u.create(TENANTUSERS, map("ID", "ann@x.org", "active", true, "n", 2, "tags", List.of("a")));
    u.create(TENANTUSERS, map("ID", "bob@x.org", "active", true, "n", 1));
    Assertions.assertEquals(List.of("@EVERYONE", "ann@x.org", "bob@x.org"),
        u.all(TENANTUSERS, null, null, null, false, map("active", true)).stream()
            .map(r -> r.get("ID")).toList());
    Assertions.assertEquals(List.of("bob@x.org", "ann@x.org", "@EVERYONE", "joe@x.org"),
        u.all(TENANTUSERS, null, null, "n", false, null).stream().map(r -> r.get("ID")).toList(),
        "records without the field come last");
    Assertions.assertEquals(List.of("ann@x.org"),
        u.all(TENANTUSERS, 2, 1, "n", true, null).stream().map(r -> r.get("ID")).toList());
    Assertions.assertEquals(List.of("ann@x.org"), u
        .all(TENANTUSERS, null, null, null, false, map("tags", List.of("a"))).stream()
        .map(r -> r.get("ID")).toList());
    Assertions.assertEquals(List.of("@EVERYONE", "joe@x.org"),
        u.all(TENANTUSERS, null, null, null, false, map("n", null)).stream()
            .map(r -> r.get("ID")).toList());
    u.delete(TENANTUSERS, Map.of("ID", "ann@x.org"));
    u.delete(TENANTUSERS, Map.of("ID", "bob@x.org"));

    // new app version: the app layer is replaced, the local layer stays
    git.checkout().setAllPaths(true).call(); // discard working tree changes of the checkout
    write("model/page/a.json", "{\"ID\": \"a\", \"title\": \"A v2\", \"color\": \"blue\"}");
    write("model/page/b.json", "{\"ID\": \"b\", \"title\": \"B\"}");
    write("model/tenantusers/@EVERYONE.json",
        "{\"ID\": \"@EVERYONE\", \"active\": true, \"roles\": [\"guest\"], \"comment\": \"v2\"}");
    Files.delete(home.resolve("model/dj-function/f.json"));
    Files.delete(home.resolve("model/dj-function/f.expression.jsonata"));
    commit("app v2");

    Map<String, Object> dry = store.sync(home.toString(), false, true);
    Assertions.assertEquals(List.of("page/b"), dry.get("added"));
    Assertions.assertNotNull(u.read(FUNCTION, Map.of("ID", "f")), "dry run changes nothing");

    Map<String, Object> report = store.sync(home.toString(), false, false);
    Assertions.assertEquals(List.of("page/b"), report.get("added"));
    Assertions.assertEquals(List.of("page/a", "tenantusers/@EVERYONE"),
        ((List<String>) report.get("changed")).stream().sorted().toList());
    Assertions.assertEquals(List.of("dj-function/f"), report.get("removed"));
    Assertions.assertEquals(List.of("tenantusers/@EVERYONE: roles"),
        report.get("localOverridesOfChangedFields"));

    Assertions.assertNull(u.read(PAGE, Map.of("ID", "a")), "still deleted locally");
    Assertions.assertEquals(map("ID", "b", "title", "B"), u.read(PAGE, Map.of("ID", "b")));
    Assertions.assertNull(u.read(FUNCTION, Map.of("ID", "f")));
    Assertions.assertNotNull(u.read(FUNCTION, Map.of("ID", "secret")));
    Assertions.assertEquals(
        map("ID", "@EVERYONE", "active", true, "roles", List.of("user", "admin"), "comment", "v2"),
        u.read(TENANTUSERS, Map.of("ID", "@EVERYONE")), "local override wins, app change shows");
    Assertions.assertNotNull(u.read(TENANTUSERS, Map.of("ID", "joe@x.org")));
    assertEffectiveConsistent(store);

    // a second start does not import again
    store.bootstrap(home.toString(), false);
    Assertions.assertNull(u.read(PAGE, Map.of("ID", "a")));

    // concurrent updates of one record are not lost
    int threads = 8, perThread = 10;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    List<Future<?>> fs = new ArrayList<>();
    for (int t = 0; t < threads; t++) {
      int tt = t;
      fs.add(pool.submit(() -> {
        for (int i = 0; i < perThread; i++)
          u.update(TENANTUSERS, Map.of("ID", "joe@x.org"), map("f" + tt + "_" + i, i));
        return null;
      }));
    }
    for (Future<?> f : fs)
      f.get();
    pool.shutdown();
    Assertions.assertEquals(threads * perThread + 2,
        u.read(TENANTUSERS, Map.of("ID", "joe@x.org")).size());
    assertEffectiveConsistent(store);

    // a changed platform (here: an unknown hash) rebuilds the effective records on startup
    store.setMeta("platform", "other");
    store.transaction(c -> {
      try (java.sql.Statement stmt = c.createStatement()) {
        stmt.execute("delete from dj_config where layer = 'effective'");
      }
      return null;
    });
    store.maintainEffective();
    Assertions.assertNotNull(u.read(TENANTUSERS, Map.of("ID", "joe@x.org")));
  }

  /**
   * the injected config database (e.g. for the session and tenant managers) reads merged records,
   * including records from the app, and writes the local layer
   */
  @Test
  public void view() throws Exception {
    SqlConfigStore store =
        new SqlConfigStore("jdbc:sqlite:" + tmp.resolve("config.db"), null, null);
    try {
      createApp();
      store.bootstrap(home.toString(), false);
      UnionDatabase u = union(store);
      SqlConfigView view = store.view();

      Assertions.assertEquals(map("ID", "@EVERYONE", "active", true, "roles", List.of("user")),
          view.read(TENANTUSERS, Map.of("ID", "@EVERYONE")), "app record");
      Assertions.assertTrue(
          view.update(TENANTUSERS, Map.of("ID", "@EVERYONE"), map("roles", List.of("admin"))));
      Assertions.assertEquals(map("ID", "@EVERYONE", "roles", List.of("admin")),
          store.local().read(TENANTUSERS, Map.of("ID", "@EVERYONE")), "only the change is local");
      Assertions.assertEquals(List.of("admin"),
          u.read(TENANTUSERS, Map.of("ID", "@EVERYONE")).get("roles"));
      Assertions.assertFalse(view.update(TENANTUSERS, Map.of("ID", "x"), map("active", true)));

      view.create(TENANTUSERS, map("ID", "new@x.org", "active", true));
      Assertions.assertEquals(map("ID", "new@x.org", "active", true),
          u.read(TENANTUSERS, Map.of("ID", "new@x.org")));
      Assertions.assertTrue(view.delete(TENANTUSERS, Map.of("ID", "@EVERYONE")));
      Assertions.assertNull(view.read(TENANTUSERS, Map.of("ID", "@EVERYONE")), "marker written");
      assertEffectiveConsistent(store);
    } finally {
      store.close();
      git.close();
    }
  }

  @Test
  public void sqlite() throws Exception {
    SqlConfigStore store =
        new SqlConfigStore("jdbc:sqlite:" + tmp.resolve("config.db"), null, null);
    try {
      scenario(store);
    } finally {
      store.close();
      git.close();
    }
  }

  @Test
  public void postgres() throws Exception {
    String url = System.getenv("DJ_TEST_PG_URL");
    Assumptions.assumeTrue(url != null, "DJ_TEST_PG_URL not set");
    String user = System.getenv("DJ_TEST_PG_USER");
    String password = System.getenv("DJ_TEST_PG_PASSWORD");
    String schema = "dj_config_test_" + Long.toHexString(System.nanoTime());
    try (Connection con = DriverManager.getConnection(url, user, password);
        Statement stmt = con.createStatement()) {
      stmt.execute("create schema " + schema);
    }
    SqlConfigStore store = new SqlConfigStore(
        url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema, user, password);
    try {
      scenario(store);
    } finally {
      store.close();
      git.close();
      try (Connection con = DriverManager.getConnection(url, user, password);
          Statement stmt = con.createStatement()) {
        stmt.execute("drop schema " + schema + " cascade");
      }
    }
  }
}
