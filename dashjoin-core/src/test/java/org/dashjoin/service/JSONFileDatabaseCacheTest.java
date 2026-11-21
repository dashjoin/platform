package org.dashjoin.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * record cache of the file config DB (dashjoin.configdb.cache)
 */
public class JSONFileDatabaseCacheTest {

  @TempDir
  Path tmp;

  JSONFileDatabase db;
  Path dir;
  final Table table = Table.ofName("t");

  @BeforeEach
  void setup() throws Exception {
    JSONFileCache.enabled = true;
    JSONFileCache.clear();
    db = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return tmp.resolve(path).toFile();
      }
    };
    dir = Files.createDirectories(tmp.resolve("model/t"));
  }

  @AfterEach
  void reset() {
    JSONFileCache.enabled = null;
    JSONFileCache.clear();
  }

  Map<String, Object> read(String id) throws Exception {
    return db.read(table, Map.of("ID", id));
  }

  Map<String, Map<String, Object>> query() throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "t";
    return db.queryMap(q, null);
  }

  void clear() {
    JSONFileCache.clear(tmp.resolve("model").toFile());
  }

  @Test
  public void readIsServedFromCache() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 1}");
    Assertions.assertEquals(1, read("a").get("x"));

    // changed behind our back: not seen until the cache is cleared
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 2}");
    Assertions.assertEquals(1, read("a").get("x"));
    clear();
    Assertions.assertEquals(2, read("a").get("x"));
  }

  @Test
  public void cacheIsOff() throws Exception {
    JSONFileCache.enabled = false;
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 1}");
    Assertions.assertEquals(1, read("a").get("x"));
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 2}");
    Assertions.assertEquals(2, read("a").get("x"));
    Assertions.assertTrue(JSONFileCache.cache.isEmpty());
  }

  @Test
  public void missingRecordOfCompleteTable() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\"}");
    Assertions.assertEquals(1, query().size());

    // the folder was read completely: a missing record is known not to exist
    Files.writeString(dir.resolve("b.json"), "{\"ID\": \"b\"}");
    Assertions.assertNull(read("b"));
    Assertions.assertEquals(1, query().size());

    clear();
    Assertions.assertNotNull(read("b"));
    Assertions.assertEquals(2, query().size());
  }

  @Test
  public void writeThrough() throws Exception {
    Assertions.assertEquals(0, query().size());

    db.create(table, new HashMap<>(Map.of("ID", "a", "x", 1)));
    Assertions.assertEquals(1, read("a").get("x"));
    Assertions.assertEquals(1, query().get("a").get("x"));

    db.update(table, Map.of("ID", "a"), new HashMap<>(Map.of("x", 2, "y", "z")));
    Assertions.assertEquals(2, read("a").get("x"));
    Assertions.assertEquals("z", query().get("a").get("y"));

    db.delete(table, Map.of("ID", "a"));
    Assertions.assertNull(read("a"));
    Assertions.assertTrue(query().isEmpty());
    Assertions.assertFalse(Files.exists(dir.resolve("a.json")));
  }

  @Test
  public void writeThroughResolvesPointers() throws Exception {
    Map<String, Object> o = new HashMap<>();
    o.put("ID", "q");
    o.put("query", "select 1");
    o.put("query-pointer", "query.sql");
    db.create(table, o);
    Assertions.assertEquals("select 1", Files.readString(dir.resolve("q.query.sql")));

    Map<String, Object> res = read("q");
    Assertions.assertEquals("select 1", res.get("query"));
    Assertions.assertEquals("query.sql", res.get("query-pointer"));

    db.update(table, Map.of("ID", "q"), new HashMap<>(Map.of("query", "select 2")));
    Assertions.assertEquals("select 2", read("q").get("query"));
    Assertions.assertEquals("select 2", Files.readString(dir.resolve("q.query.sql")));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void callersGetCopies() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"m\": {\"k\": [1, 2]}}");
    Map<String, Object> r = read("a");
    ((List<Object>) ((Map<String, Object>) r.get("m")).get("k")).add(3);
    r.put("x", 1);
    Assertions.assertEquals(Map.of("ID", "a", "m", Map.of("k", List.of(1, 2))), read("a"));

    query().get("a").put("y", 1);
    Assertions.assertEquals(Map.of("ID", "a", "m", Map.of("k", List.of(1, 2))), read("a"));
  }

  @Test
  public void sameOrderAsFileListing() throws Exception {
    for (String id : new String[] {"b", "C", "a", "D"})
      Files.writeString(dir.resolve(id + ".json"), "{\"ID\": \"" + id + "\"}");
    List<String> fromDisk = new ArrayList<>(query().keySet());
    List<String> fromCache = new ArrayList<>(query().keySet());
    Assertions.assertEquals(List.of("a", "b", "C", "D"), fromDisk);
    Assertions.assertEquals(fromDisk, fromCache);
  }

  @Test
  public void unreadableFileKeepsTableIncomplete() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\"}");
    Files.writeString(dir.resolve("broken.json"), "{");
    Assertions.assertEquals(1, query().size());

    // still reported as an error rather than as a missing record
    Assertions.assertThrows(Exception.class, () -> read("broken"));

    Files.writeString(dir.resolve("broken.json"), "{\"ID\": \"broken\"}");
    Assertions.assertEquals(2, query().size());
  }

  @Test
  public void fillCache() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\"}");
    Files.createDirectories(tmp.resolve("model/u"));
    Files.writeString(tmp.resolve("model/u/b.json"), "{\"ID\": \"b\"}");
    db.fillCache();

    Files.delete(dir.resolve("a.json"));
    Files.delete(tmp.resolve("model/u/b.json"));
    Assertions.assertNotNull(read("a"));
    Assertions.assertNotNull(db.read(Table.ofName("u"), Map.of("ID", "b")));
  }

  @Test
  public void clearOnlyAffectsThatModelFolder() throws Exception {
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 1}");
    read("a");
    Files.writeString(dir.resolve("a.json"), "{\"ID\": \"a\", \"x\": 2}");
    JSONFileCache.clear(tmp.resolve("other/model").toFile());
    Assertions.assertEquals(1, read("a").get("x"));
  }

  @Test
  public void concurrentUpdates() throws Exception {
    db.create(table, new HashMap<>(Map.of("ID", "a")));
    // fill the cache, then hammer the record
    query();
    ExecutorService ex = Executors.newFixedThreadPool(8);
    List<Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < 8; t++) {
      int thread = t;
      futures.add(ex.submit(() -> {
        for (int i = 0; i < 10; i++) {
          db.update(table, Map.of("ID", "a"), new HashMap<>(Map.of("f" + thread + "_" + i, i)));
          read("a");
          query();
        }
        return null;
      }));
    }
    for (Future<?> f : futures)
      f.get();
    ex.shutdown();

    Map<String, Object> cached = read("a");
    clear();
    Map<String, Object> disk = read("a");
    Assertions.assertEquals(81, disk.size());
    Assertions.assertEquals(disk, cached);
  }
}
