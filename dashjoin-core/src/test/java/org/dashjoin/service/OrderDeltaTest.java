package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.dashjoin.util.Escape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Baseline: documents the order in which config queries return records, as of 7.1. Pages, the
 * navigation and the config tables list records in this order.
 *
 * The file store lists a table's records sorted by file name (the escaped ID), ignoring case. The
 * union returns the records of the read-only layers first, in their order, then the records only
 * the app has. A record the app changes keeps the position of the platform's record. all() can
 * sort by a field, with missing values last (ascending) or first (descending), and pages with
 * offset and limit after sorting.
 */
public class OrderDeltaTest {

  @TempDir
  Path root;

  Path pages;
  MemoryDatabase platform;
  PojoDatabase config;

  final Table page = Table.ofName("page");

  @BeforeEach
  public void setup() throws Exception {
    JSONFileDatabase app = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return root.resolve(path).toFile();
      }
    };
    pages = Files.createDirectories(root.resolve("model/page"));
    platform = new MemoryDatabase(true);
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = Arrays.asList(platform);
  }

  void appRecord(String id, String json) throws Exception {
    Files.writeString(pages.resolve(Escape.filename(id) + ".json"), json);
  }

  void appRecord(String id) throws Exception {
    appRecord(id, "{\"ID\": \"" + id + "\"}");
  }

  List<Object> ids(List<Map<String, Object>> records) {
    List<Object> res = new ArrayList<>();
    for (Map<String, Object> r : records)
      res.add(r.get("ID"));
    return res;
  }

  List<Object> query() throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "page";
    return ids(config.query(q, null));
  }

  @Test
  public void appRecordsSortedByFileNameIgnoringCase() throws Exception {
    for (String id : new String[] {"b", "C", "a", "D", "a b"})
      appRecord(id);
    // "a b" is stored as a+b.json, and + sorts before .
    Assertions.assertEquals("a+b", Escape.filename("a b"));
    Assertions.assertEquals(Arrays.asList("a b", "a", "b", "C", "D"), query());
  }

  @Test
  public void platformRecordsFirstThenAppRecords() throws Exception {
    platform.table("page").put("z", m("ID", "z"));
    platform.table("page").put("m", m("ID", "m"));
    appRecord("a");
    appRecord("n");
    Assertions.assertEquals(Arrays.asList("z", "m", "a", "n"), query());
  }

  @Test
  public void changedPlatformRecordKeepsItsPosition() throws Exception {
    platform.table("page").put("z", m("ID", "z", "title", "v1"));
    platform.table("page").put("m", m("ID", "m"));
    appRecord("a");
    appRecord("z", "{\"ID\": \"z\", \"title\": \"changed\"}");
    Assertions.assertEquals(Arrays.asList("z", "m", "a"), query());
  }

  @Test
  public void allSortsByField() throws Exception {
    appRecord("a", "{\"ID\": \"a\", \"n\": 2}");
    appRecord("b", "{\"ID\": \"b\"}");
    appRecord("c", "{\"ID\": \"c\", \"n\": 1}");
    appRecord("d", "{\"ID\": \"d\", \"n\": 3}");

    // missing values last when ascending, first when descending
    Assertions.assertEquals(Arrays.asList("c", "a", "d", "b"),
        ids(config.all(page, null, null, "n", false, null)));
    Assertions.assertEquals(Arrays.asList("b", "d", "a", "c"),
        ids(config.all(page, null, null, "n", true, null)));

    // offset and limit apply after sorting
    Assertions.assertEquals(Arrays.asList("a", "d"),
        ids(config.all(page, 1, 2, "n", false, null)));
  }

  @Test
  public void allSortsIdCaseSensitive() throws Exception {
    // unlike the file listing, sorting by a field compares strings case-sensitively
    for (String id : new String[] {"b", "C", "a", "D"})
      appRecord(id);
    Assertions.assertEquals(Arrays.asList("C", "D", "a", "b"),
        ids(config.all(page, null, null, "ID", false, null)));
  }

  @Test
  public void allFiltersByArguments() throws Exception {
    appRecord("a", "{\"ID\": \"a\", \"k\": \"x\"}");
    appRecord("b", "{\"ID\": \"b\", \"k\": \"y\"}");
    appRecord("c", "{\"ID\": \"c\", \"k\": \"x\"}");
    Assertions.assertEquals(Arrays.asList("a", "c"),
        ids(config.all(page, null, null, null, false, m("k", "x"))));
  }
}
