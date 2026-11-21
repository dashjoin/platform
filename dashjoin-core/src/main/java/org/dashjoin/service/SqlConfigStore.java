package org.dashjoin.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.Home;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Serves the config database from SQL instead of the model folder (JSON files).
 *
 * Enabled by the property dashjoin.configdb.url (with dashjoin.configdb.username and
 * dashjoin.configdb.password). The config has these layers (higher wins, see UnionDatabase):
 *
 * - local (SQL, writable): everything written at runtime (UI, API, logins), records that only exist
 * locally and deletion markers
 *
 * - app (SQL, read only): the model folder of one git commit of the app. A sync replaces it as a
 * whole, the local layer stays
 *
 * - platform defaults (classpath and the metadata of the installed function and database classes)
 *
 * The merged result of all layers is stored as well (layer "effective"), so that reads and queries
 * do not merge the layers in memory. Every write to the local layer recomputes the effective record
 * in the same transaction; an app sync recomputes the records it changed; a change of the platform
 * defaults (detected by a hash at startup) rebuilds all effective records.
 *
 * On first start, the model folder of the git checkout is imported: the HEAD commit becomes the app
 * layer, local changes in the working tree become the local layer. Later, $appSync() pulls the app
 * and replaces the app layer. The checkout is still used for all other files of the app.
 */
public class SqlConfigStore {

  private final static Logger logger = Logger.getLogger(SqlConfigStore.class.getName());

  public static final String APP = "app";
  public static final String LOCAL = "local";
  public static final String EFFECTIVE = "effective";

  /** increase when the way effective records are computed changes: rebuilds them on startup */
  static final String EFFECTIVE_VERSION = "1";

  /** sync replaces the app records of these tables; comma separated */
  static final String EXCLUDE_PROPERTY = "dashjoin.configdb.sync.exclude";

  static volatile SqlConfigStore instance;

  /**
   * the store if dashjoin.configdb.url is set, null otherwise. The first call creates the tables.
   * The import of the model folder (if the store is empty) runs when the config database attaches
   * its other layers (see attach)
   */
  public static SqlConfigStore get() {
    if (instance == null)
      synchronized (SqlConfigStore.class) {
        if (instance == null) {
          String url = property("dashjoin.configdb.url");
          if (url == null || url.isBlank())
            return null;
          SqlConfigStore store = new SqlConfigStore(url, property("dashjoin.configdb.username"),
              property("dashjoin.configdb.password"));
          store.home = Home.get().getHome();
          store.hasAppUrl = property("dashjoin.appurl") != null;
          try {
            store.createTables();
          } catch (Exception e) {
            throw new RuntimeException("Cannot initialize the SQL config database", e);
          }
          instance = store;
        }
      }
    return instance;
  }

  public static boolean enabled() {
    String url = property("dashjoin.configdb.url");
    return url != null && !url.isBlank();
  }

  static String property(String name) {
    try {
      return ConfigProvider.getConfig().getOptionalValue(name, String.class).orElse(null);
    } catch (Exception e) {
      return null;
    }
  }

  final BasicDataSource ds;
  final boolean postgres;
  final JSONSqlDatabase app;
  final JSONSqlDatabase local;
  final JSONSqlDatabase effective;
  final SqlConfigView view;

  /** app checkout to import / sync on attach (null: none) */
  String home;
  boolean hasAppUrl;

  /**
   * the layers below the local layer, lowest first (platform defaults, app, polymorphism). null
   * until attach: effective records cannot be computed before
   */
  volatile List<JSONDatabase> base;

  boolean attached;

  /** tables never written to the app layer */
  Set<String> exclude = new HashSet<>();

  SqlConfigStore(String url, String username, String password) {
    ds = new BasicDataSource();
    ds.setUrl(url);
    ds.setUsername(username);
    ds.setPassword(password);
    postgres = url.startsWith("jdbc:postgresql:");
    app = new JSONSqlDatabase(this, APP, true);
    local = new JSONSqlDatabase(this, LOCAL, false);
    effective = new JSONSqlDatabase(this, EFFECTIVE, true);
    view = new SqlConfigView(this);
    String ex = property(EXCLUDE_PROPERTY);
    if (ex != null)
      for (String t : ex.split(","))
        if (!t.isBlank())
          exclude.add(t.trim());
  }

  public JSONSqlDatabase app() {
    return app;
  }

  public JSONSqlDatabase local() {
    return local;
  }

  public JSONSqlDatabase effective() {
    return effective;
  }

  /**
   * the config database to inject as JSONDatabase: reads merged records, writes the local layer
   */
  public SqlConfigView view() {
    return view;
  }

  /**
   * called when the config database is set up: base are the layers below the local layer (lowest
   * first). The first call imports or syncs the app and brings the effective records up to date
   */
  public void attach(List<JSONDatabase> base) throws Exception {
    synchronized (this) {
      this.base = List.copyOf(base);
      if (attached)
        return;
      attached = true;
    }
    syncLocked(() -> {
      if (home != null)
        bootstrap(home, hasAppUrl);
      maintainEffective();
      return null;
    });
  }

  public void close() throws Exception {
    ds.close();
  }

  // ---------------------------------------------------------------- connections and locks

  /** connection of the transaction of locked() on this thread */
  final ThreadLocal<Connection> tx = new ThreadLocal<>();

  final Map<String, ReentrantLock> jvmLocks = new ConcurrentHashMap<>();

  /** a connection that is closed unless it belongs to the running transaction */
  static class Con implements AutoCloseable {
    final Connection con;
    final boolean own;

    Con(Connection con, boolean own) {
      this.con = con;
      this.own = own;
    }

    @Override
    public void close() throws Exception {
      if (own)
        con.close();
    }
  }

  Con con() throws Exception {
    Connection c = tx.get();
    if (c != null)
      return new Con(c, false);
    return new Con(ds.getConnection(), true);
  }

  /**
   * runs body in a transaction that holds the lock of key: a JVM lock and, on PostgreSQL, an
   * advisory lock, so read-modify-write sequences of other instances cannot interleave. Nested
   * calls join the transaction
   */
  <T> T locked(String key, Callable<T> body) throws Exception {
    ReentrantLock jvm = jvmLocks.computeIfAbsent(key, k -> new ReentrantLock());
    jvm.lock();
    try {
      return transaction(c -> {
        advisoryLock(c, key);
        return body.call();
      });
    } finally {
      jvm.unlock();
    }
  }

  interface TxBody<T> {
    T run(Connection c) throws Exception;
  }

  /** local records written in the transaction of this thread: their effective records are stale */
  final ThreadLocal<Set<List<String>>> dirty = ThreadLocal.withInitial(LinkedHashSet::new);

  /**
   * runs body in a transaction whose connection con() returns on this thread. Before the commit,
   * the effective records of the local records written in the transaction are recomputed. Nested
   * calls join the transaction
   */
  <T> T transaction(TxBody<T> body) throws Exception {
    Connection cur = tx.get();
    if (cur != null)
      return body.run(cur);
    try (Connection c = ds.getConnection()) {
      c.setAutoCommit(false);
      tx.set(c);
      try {
        T res = body.run(c);
        flush(c);
        c.commit();
        return res;
      } catch (Throwable t) {
        c.rollback();
        throw t;
      } finally {
        tx.remove();
        dirty.remove();
        c.setAutoCommit(true);
      }
    }
  }

  /** IDs are ordered like Java strings (and SQLite), not by the locale of the database */
  String binaryCollation() {
    return postgres ? " collate \"C\"" : "";
  }

  static String key(String tbl, String id) {
    return tbl + "/" + id;
  }

  void advisoryLock(Connection c, String key) throws Exception {
    if (!postgres)
      return;
    try (PreparedStatement stmt = c.prepareStatement("select pg_advisory_xact_lock(hashtext(?))")) {
      stmt.setString(1, key);
      stmt.execute();
    }
  }

  /** key of the cluster wide lock for import and sync */
  static final long SYNC_LOCK = 0x646a636f6e666967L;

  final ReentrantLock syncLock = new ReentrantLock();

  /** runs body while holding the cluster wide import / sync lock */
  <T> T syncLocked(Callable<T> body) throws Exception {
    syncLock.lock();
    try {
      // nested (e.g. sync during bootstrap): this thread already holds the lock
      if (!postgres || syncLock.getHoldCount() > 1)
        return body.call();
      try (Connection c = ds.getConnection()) {
        try (Statement stmt = c.createStatement()) {
          stmt.execute("select pg_advisory_lock(" + SYNC_LOCK + ")");
        }
        try {
          return body.call();
        } finally {
          try (Statement stmt = c.createStatement()) {
            stmt.execute("select pg_advisory_unlock(" + SYNC_LOCK + ")");
          }
        }
      }
    } finally {
      syncLock.unlock();
    }
  }

  static final String UPSERT = "insert into dj_config (layer, tbl, id, doc, deleted)"
      + " values (?, ?, ?, ?, ?) on conflict (layer, tbl, id) do update set doc = excluded.doc,"
      + " deleted = excluded.deleted";

  void upsert(Connection c, String layer, String tbl, String id, String doc, boolean deleted)
      throws Exception {
    try (PreparedStatement stmt = c.prepareStatement(UPSERT)) {
      stmt.setString(1, layer);
      stmt.setString(2, tbl);
      stmt.setString(3, id);
      stmt.setString(4, doc);
      stmt.setBoolean(5, deleted);
      stmt.executeUpdate();
    }
  }

  void createTables() throws Exception {
    try (Connection c = ds.getConnection(); Statement stmt = c.createStatement()) {
      stmt.execute("create table if not exists dj_config (layer varchar(16) not null,"
          + " tbl varchar(255) not null, id varchar(1024) not null, doc text,"
          + " deleted boolean not null default false, primary key (layer, tbl, id))");
      stmt.execute("create table if not exists dj_app_sync (synced varchar(64) not null,"
          + " app_commit varchar(64), report text)");
      stmt.execute("create table if not exists dj_config_meta (k varchar(64) not null primary key,"
          + " v text)");
    }
    if (postgres)
      // queries of the effective records run in SQL (see JSONSqlDatabase.allEffective)
      for (String ddl : new String[] {
          "alter table dj_config add column if not exists docb jsonb"
              + " generated always as (doc::jsonb) stored",
          "create index if not exists dj_config_docb on dj_config using gin (docb jsonb_path_ops)"
              + " where layer = 'effective'"})
        try (Connection c = ds.getConnection(); Statement stmt = c.createStatement()) {
          stmt.execute(ddl);
        } catch (Exception e) {
          // another instance did it at the same time
          logger.info("Not executed (" + e.getMessage() + "): " + ddl);
        }
  }

  String getMeta(String k) throws Exception {
    try (Con c = con();
        PreparedStatement stmt = c.con.prepareStatement("select v from dj_config_meta where k = ?")) {
      stmt.setString(1, k);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getString(1) : null;
      }
    }
  }

  void setMeta(String k, String v) throws Exception {
    try (Con c = con(); PreparedStatement stmt = c.con.prepareStatement(
        "insert into dj_config_meta (k, v) values (?, ?) on conflict (k) do update set v = excluded.v")) {
      stmt.setString(1, k);
      stmt.setString(2, v);
      stmt.executeUpdate();
    }
  }

  // ---------------------------------------------------------------- effective records

  /**
   * a record of the local or app layer was written. Its effective record is recomputed before the
   * transaction commits (or right away outside of a transaction)
   */
  void changed(String tbl, String id) throws Exception {
    if (base == null) {
      // before attach: all effective records are rebuilt then
      setMeta("rebuild", "true");
      return;
    }
    if (tx.get() != null)
      dirty.get().add(List.of(tbl, id));
    else
      locked(key(tbl, id), () -> {
        dirty.get().add(List.of(tbl, id));
        return null;
      });
  }

  void flush(Connection c) throws Exception {
    Set<List<String>> d = dirty.get();
    while (!d.isEmpty()) {
      List<String> k = d.iterator().next();
      d.remove(k);
      advisoryLock(c, key(k.get(0), k.get(1)));
      computeEffective(c, k.get(0), k.get(1));
    }
  }

  /**
   * the local layer as the union database reads it: passwords are masked (PasswordDatabase). The
   * encrypted password stays in the local layer only
   */
  static Map<String, Object> masked(Map<String, Object> local) {
    if (local != null && local.get("password") instanceof String) {
      local = new LinkedHashMap<>(local);
      local.put("password", "********");
    }
    return local;
  }

  /** merge all layers of one record, like UnionDatabase.read */
  void computeEffective(Connection c, String tbl, String id) throws Exception {
    Table t = Table.ofName(tbl);
    Map<String, Object> search = Map.of("ID", id);
    Map<String, Object> res = null;
    if (!local.isDeleted(t, id)) {
      for (JSONDatabase d : base)
        res = UnionDatabase.merge(res, d.read(t, search));
      res = UnionDatabase.merge(res, masked(local.read(t, search)));
    }
    if (res == null || res.get("ID") == null)
      try (PreparedStatement stmt =
          c.prepareStatement("delete from dj_config where layer = ? and tbl = ? and id = ?")) {
        stmt.setString(1, EFFECTIVE);
        stmt.setString(2, tbl);
        stmt.setString(3, id);
        stmt.executeUpdate();
      }
    else
      upsert(c, EFFECTIVE, tbl, id, JSONDatabase.toJsonString(res), false);
  }

  /** tables a layer that is not stored in SQL has records in */
  static Set<String> layerTables(JSONDatabase d) {
    if (d instanceof JSONClassloaderDatabase)
      return ((JSONClassloaderDatabase) d).tables();
    if (d instanceof PolymorphismDatabase)
      return Set.of("dj-database");
    return Set.of();
  }

  static final ObjectMapper om = new ObjectMapper();

  /** hash of the layers that are not stored in SQL (platform defaults, polymorphism) */
  String platformHash() throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(EFFECTIVE_VERSION.getBytes());
    for (JSONDatabase d : base)
      if (!(d instanceof JSONSqlDatabase)) {
        md.update(d.getClass().getName().getBytes());
        for (String table : new TreeSet<>(layerTables(d))) {
          QueryMeta q = new QueryMeta();
          q.query = table;
          md.update(table.getBytes());
          md.update(om.writeValueAsBytes(new TreeMap<>(d.queryMap(q, null))));
        }
      }
    return HexFormat.of().formatHex(md.digest());
  }

  /**
   * on attach: rebuild the effective records if the platform defaults changed, if they were never
   * built or if records were written before attach
   */
  void maintainEffective() throws Exception {
    if (platformHash().equals(getMeta("platform")) && getMeta("rebuild") == null)
      return;
    transaction(c -> {
      rebuildEffective(c);
      return null;
    });
  }

  /** recompute all effective records (in the transaction of c) */
  void rebuildEffective(Connection c) throws Exception {
    long start = System.currentTimeMillis();
    try (PreparedStatement stmt = c.prepareStatement("delete from dj_config where layer = ?")) {
      stmt.setString(1, EFFECTIVE);
      stmt.executeUpdate();
    }
    Set<String> tables = new TreeSet<>();
    try (PreparedStatement stmt =
        c.prepareStatement("select distinct tbl from dj_config where layer in (?, ?)")) {
      stmt.setString(1, APP);
      stmt.setString(2, LOCAL);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next())
          tables.add(rs.getString(1));
      }
    }
    for (JSONDatabase d : base)
      tables.addAll(layerTables(d));

    int n = 0;
    try (PreparedStatement stmt = c.prepareStatement(UPSERT)) {
      for (String table : tables) {
        QueryMeta q = new QueryMeta();
        q.query = table;
        Map<String, Map<String, Object>> res = new LinkedHashMap<>();
        for (JSONDatabase d : base)
          for (Entry<String, Map<String, Object>> e : d.queryMap(q, null).entrySet())
            res.put(e.getKey(), UnionDatabase.merge(res.get(e.getKey()), e.getValue()));
        for (Entry<String, Map<String, Object>> e : local.queryMap(q, null).entrySet())
          res.put(e.getKey(), UnionDatabase.merge(res.get(e.getKey()), masked(e.getValue())));
        res.keySet().removeAll(local.deletedIds(table));
        for (Entry<String, Map<String, Object>> e : res.entrySet())
          if (e.getValue() != null && e.getValue().get("ID") != null) {
            stmt.setString(1, EFFECTIVE);
            stmt.setString(2, table);
            stmt.setString(3, e.getKey());
            stmt.setString(4, JSONDatabase.toJsonString(e.getValue()));
            stmt.setBoolean(5, false);
            stmt.addBatch();
            n++;
          }
        stmt.executeBatch();
      }
    }
    setMeta("platform", platformHash());
    try (PreparedStatement stmt = c.prepareStatement("delete from dj_config_meta where k = ?")) {
      stmt.setString(1, "rebuild");
      stmt.executeUpdate();
    }
    logger.info("Rebuilt " + n + " effective records of the SQL config database in "
        + (System.currentTimeMillis() - start) + " ms");
  }

  // ---------------------------------------------------------------- reading a model folder

  /**
   * all records of a model folder: table -> ID -> record. Externalized strings are inlined
   */
  static Map<String, Map<String, Map<String, Object>>> readModel(File root, Set<String> skip)
      throws Exception {
    Map<String, Map<String, Map<String, Object>>> res = new TreeMap<>();
    File[] dirs = new File(root, "model").listFiles(File::isDirectory);
    if (dirs == null)
      return res;
    Arrays.sort(dirs);
    JSONFileDatabase db = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return new File(root, path);
      }
    };
    for (File dir : dirs) {
      String table = dir.getName();
      if (table.startsWith(".") || skip.contains(table))
        continue;
      QueryMeta q = new QueryMeta();
      q.query = table;
      Map<String, Map<String, Object>> records = new LinkedHashMap<>();
      for (Map<String, Object> record : db.queryMap(q, null).values()) {
        if (record.get("ID") == null)
          continue;
        stripPointers(record);
        records.put(JSONSqlDatabase.id(record.get("ID")), record);
      }
      if (!records.isEmpty())
        res.put(table, records);
    }
    return res;
  }

  /** the SQL store holds externalized strings inline; drop the X-pointer keys */
  @SuppressWarnings("unchecked")
  static void stripPointers(Object o) {
    if (o instanceof Map) {
      ((Map<String, Object>) o).keySet().removeIf(k -> k.endsWith("-pointer"));
      for (Object v : ((Map<String, Object>) o).values())
        stripPointers(v);
    }
    if (o instanceof List)
      for (Object v : (List<Object>) o)
        stripPointers(v);
  }

  /**
   * exports the model folder of the HEAD commit of the checkout in home to a temp folder
   */
  static Path exportHead(String home, String[] commit) throws Exception {
    Path tmp = Files.createTempDirectory("dj-app-");
    try (Repository repo = new FileRepository(new File(home, ".git"))) {
      ObjectId head = repo.resolve("HEAD");
      if (head == null)
        throw new Exception("The app checkout in " + home + " has no commits");
      commit[0] = head.getName();
      try (RevWalk rw = new RevWalk(repo); TreeWalk tw = new TreeWalk(repo)) {
        RevCommit rc = rw.parseCommit(head);
        tw.addTree(rc.getTree());
        tw.setRecursive(true);
        tw.setFilter(PathFilter.create("model"));
        while (tw.next()) {
          File f = tmp.resolve(tw.getPathString()).toFile();
          f.getParentFile().mkdirs();
          try (OutputStream out = new FileOutputStream(f)) {
            repo.open(tw.getObjectId(0)).copyTo(out);
          }
        }
      }
    }
    return tmp;
  }

  static boolean isGit(String home) {
    return new File(home, ".git").exists();
  }

  // ---------------------------------------------------------------- bootstrap, import, sync

  void bootstrap(String home, boolean hasAppUrl) throws Exception {
    createTables();
    syncLocked(() -> {
      if (lastSync() == null)
        importFromHome(home);
      else if (hasAppUrl && isGit(home)) {
        // Home pulled the app at startup: bring the app layer to the new HEAD
        Map<String, Object> report = sync(home, false, false);
        logger.info("App sync at startup: " + summary(report));
      }
      return null;
    });
  }

  /** commit and time of the last import / sync, null if the store is empty */
  Map<String, Object> lastSync() throws Exception {
    try (Connection c = ds.getConnection();
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(
            "select synced, app_commit from dj_app_sync order by synced desc")) {
      if (!rs.next())
        return null;
      Map<String, Object> res = new LinkedHashMap<>();
      res.put("synced", rs.getString(1));
      res.put("commit", rs.getString(2));
      return res;
    }
  }

  /**
   * first start: the HEAD commit of the checkout becomes the app layer; records that differ in the
   * working tree become the local layer (only the changed fields), records deleted in the working
   * tree become deletion markers. Without git, all records go to the local layer
   */
  Map<String, Object> importFromHome(String home) throws Exception {
    Map<String, Map<String, Map<String, Object>>> working = readModel(new File(home), Set.of());

    String[] commit = new String[1];
    Map<String, Map<String, Map<String, Object>>> appRecords = new TreeMap<>();
    if (isGit(home)) {
      Path tmp = exportHead(home, commit);
      try {
        appRecords = readModel(tmp.toFile(), exclude);
      } finally {
        FileUtils.deleteQuietly(tmp.toFile());
      }
    }

    List<String> lostFields = new ArrayList<>();
    Map<String, Map<String, Map<String, Object>>> app = appRecords;
    return transaction(c -> {
      int nApp = 0, nLocal = 0, nDeleted = 0;
      for (Entry<String, Map<String, Map<String, Object>>> t : app.entrySet())
        for (Entry<String, Map<String, Object>> r : t.getValue().entrySet()) {
          upsert(c, APP, t.getKey(), r.getKey(), JSONDatabase.toJsonString(r.getValue()), false);
          nApp++;
        }
      for (Entry<String, Map<String, Map<String, Object>>> t : working.entrySet())
        for (Entry<String, Map<String, Object>> r : t.getValue().entrySet()) {
          Map<String, Object> appRecord = app.getOrDefault(t.getKey(), Map.of()).get(r.getKey());
          Map<String, Object> diff = diff(appRecord, r.getValue());
          if (appRecord != null)
            for (String k : appRecord.keySet())
              if (!r.getValue().containsKey(k))
                lostFields.add(t.getKey() + "/" + r.getKey() + "." + k);
          if (diff != null) {
            upsert(c, LOCAL, t.getKey(), r.getKey(), JSONDatabase.toJsonString(diff), false);
            nLocal++;
          }
        }
      for (Entry<String, Map<String, Map<String, Object>>> t : app.entrySet())
        for (String id : t.getValue().keySet())
          if (!working.getOrDefault(t.getKey(), Map.of()).containsKey(id)) {
            upsert(c, LOCAL, t.getKey(), id, null, true);
            nDeleted++;
          }
      Map<String, Object> report = new LinkedHashMap<>();
      report.put("import", true);
      report.put("commit", commit[0]);
      report.put("appRecords", nApp);
      report.put("localRecords", nLocal);
      report.put("deletedRecords", nDeleted);
      if (!lostFields.isEmpty())
        report.put("fieldsRemovedLocallyNotImported", lostFields);
      recordSync(c, commit[0], report);
      if (base != null)
        rebuildEffective(c);
      else
        setMeta("rebuild", "true");
      logger.info("Imported the model folder into the SQL config database: " + summary(report));
      if (!lostFields.isEmpty())
        logger.warning("Fields removed in the working tree cannot be represented in the local"
            + " layer and are visible again: " + lostFields);
      return report;
    });
  }

  /**
   * fields of local that differ from base (with ID), the whole record if there is no base, null if
   * there is no difference
   */
  static Map<String, Object> diff(Map<String, Object> base, Map<String, Object> local) {
    if (base == null)
      return local;
    Map<String, Object> res = new LinkedHashMap<>();
    for (Entry<String, Object> e : local.entrySet())
      if (!e.getKey().equals("ID") && !Objects.equals(e.getValue(), base.get(e.getKey())))
        res.put(e.getKey(), e.getValue());
    if (res.isEmpty())
      return null;
    Map<String, Object> withId = new LinkedHashMap<>();
    withId.put("ID", local.get("ID"));
    withId.putAll(res);
    return withId;
  }

  void recordSync(Connection c, String commit, Map<String, Object> report) throws Exception {
    try (PreparedStatement stmt =
        c.prepareStatement("insert into dj_app_sync (synced, app_commit, report) values (?, ?, ?)")) {
      stmt.setString(1, Instant.now().toString());
      stmt.setString(2, commit);
      stmt.setString(3, JSONDatabase.toJsonString(report));
      stmt.executeUpdate();
    }
  }

  /**
   * replaces the app layer with the model folder of the HEAD commit (after a git pull if pull is
   * set). The local layer stays. Returns a report: added, changed and removed app records and the
   * local overrides of fields the app changed
   */
  public Map<String, Object> sync(String home, boolean pull, boolean dryRun) throws Exception {
    return syncLocked(() -> {
      if (!isGit(home))
        throw new Exception("The app folder " + home + " is not a git checkout");
      if (pull && !dryRun)
        try (Git git = new Git(new FileRepository(new File(home, ".git")))) {
          git.pull().call();
        }

      String[] commit = new String[1];
      Map<String, Map<String, Map<String, Object>>> newApp;
      Path tmp = exportHead(home, commit);
      try {
        newApp = readModel(tmp.toFile(), exclude);
      } finally {
        FileUtils.deleteQuietly(tmp.toFile());
      }

      Map<String, Map<String, Map<String, Object>>> oldApp = readLayer(APP);
      Map<String, Map<String, Map<String, Object>>> localLayer = readLayer(LOCAL);
      Map<String, Object> last = lastSync();

      List<String> added = new ArrayList<>(), changed = new ArrayList<>(),
          removed = new ArrayList<>(), overrides = new ArrayList<>();
      Set<String> changedDatabases = new HashSet<>();
      for (Entry<String, Map<String, Map<String, Object>>> t : newApp.entrySet())
        for (Entry<String, Map<String, Object>> r : t.getValue().entrySet()) {
          Map<String, Object> old = oldApp.getOrDefault(t.getKey(), Map.of()).get(r.getKey());
          String key = t.getKey() + "/" + r.getKey();
          if (old == null)
            added.add(key);
          else if (!old.equals(r.getValue())) {
            changed.add(key);
            Map<String, Object> loc =
                localLayer.getOrDefault(t.getKey(), Map.of()).get(r.getKey());
            if (loc != null) {
              List<String> fields = new ArrayList<>();
              for (String f : loc.keySet())
                if (!f.equals("ID") && !Objects.equals(old.get(f), r.getValue().get(f)))
                  fields.add(f);
              if (!fields.isEmpty())
                overrides.add(key + ": " + String.join(", ", fields));
            }
          } else
            continue;
          if (t.getKey().equals("dj-database"))
            changedDatabases.add(r.getKey());
        }
      for (Entry<String, Map<String, Map<String, Object>>> t : oldApp.entrySet())
        for (String id : t.getValue().keySet())
          if (!newApp.getOrDefault(t.getKey(), Map.of()).containsKey(id)) {
            removed.add(t.getKey() + "/" + id);
            if (t.getKey().equals("dj-database"))
              changedDatabases.add(id);
          }

      Map<String, Object> report = new LinkedHashMap<>();
      report.put("commit", commit[0]);
      report.put("previousCommit", last == null ? null : last.get("commit"));
      report.put("dryRun", dryRun);
      report.put("added", added);
      report.put("changed", changed);
      report.put("removed", removed);
      report.put("localOverridesOfChangedFields", overrides);
      report.put("changedDatabases", new ArrayList<>(changedDatabases));
      if (dryRun)
        return report;

      transaction(c -> {
        try (PreparedStatement stmt = c.prepareStatement("delete from dj_config where layer = ?")) {
          stmt.setString(1, APP);
          stmt.executeUpdate();
        }
        for (Entry<String, Map<String, Map<String, Object>>> t : newApp.entrySet())
          for (Entry<String, Map<String, Object>> r : t.getValue().entrySet()) {
            upsert(c, APP, t.getKey(), r.getKey(), JSONDatabase.toJsonString(r.getValue()), false);
            if (!r.getValue().equals(oldApp.getOrDefault(t.getKey(), Map.of()).get(r.getKey())))
              changed(t.getKey(), r.getKey());
          }
        for (Entry<String, Map<String, Map<String, Object>>> t : oldApp.entrySet())
          for (String id : t.getValue().keySet())
            if (!newApp.getOrDefault(t.getKey(), Map.of()).containsKey(id))
              changed(t.getKey(), id);
        recordSync(c, commit[0], report);
        // the effective records of the changed app records are recomputed before the commit
        return null;
      });
      return report;
    });
  }

  /** all records of a layer (without deletion markers): table -> ID -> record */
  Map<String, Map<String, Map<String, Object>>> readLayer(String layer) throws Exception {
    Map<String, Map<String, Map<String, Object>>> res = new TreeMap<>();
    try (Connection c = ds.getConnection();
        PreparedStatement stmt = c.prepareStatement(
            "select tbl, id, doc from dj_config where layer = ? and deleted = ?")) {
      stmt.setString(1, layer);
      stmt.setBoolean(2, false);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next())
          res.computeIfAbsent(rs.getString(1), k -> new LinkedHashMap<>()).put(rs.getString(2),
              JSONDatabase.fromJsonString(rs.getString(3)));
      }
    }
    return res;
  }

  static String summary(Map<String, Object> report) {
    Map<String, Object> res = new LinkedHashMap<>();
    for (Entry<String, Object> e : report.entrySet())
      res.put(e.getKey(), e.getValue() instanceof List ? ((List<?>) e.getValue()).size()
          : e.getValue());
    return res.toString();
  }
}
