package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Baseline: documents what deleting a config record does when the platform ships it, as of 7.1.
 * UnionDatabase.delete only deletes the app's part. A record the platform ships cannot be deleted:
 * the delete removes the app's changes (a reset) and the platform's version stays.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
public class DeleteDeltaTest {

  final Table page = Table.ofName("page");

  MemoryDatabase platform;
  MemoryDatabase app;
  PojoDatabase config;

  @BeforeEach
  public void setup() {
    platform = new MemoryDatabase(true);
    platform.table("page").put("shipped", m("ID", "shipped", "title", "v1"));
    app = new MemoryDatabase(false);
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = Arrays.asList(platform);
  }

  Map<String, Object> read(String id) throws Exception {
    return config.read(page, m("ID", id));
  }

  boolean listed(String id) throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "page";
    for (Map<String, Object> r : config.query(q, null))
      if (id.equals(r.get("ID")))
        return true;
    return false;
  }

  @Test
  public void deletePlatformRecordKeepsIt() throws Exception {
    // status quo: the delete reports success, but the record is still there
    Assertions.assertTrue(config.delete(page, m("ID", "shipped")));
    Assertions.assertEquals(m("ID", "shipped", "title", "v1"), read("shipped"));
    Assertions.assertTrue(listed("shipped"));
  }

  @Test
  public void deleteChangedPlatformRecordResetsIt() throws Exception {
    config.update(page, m("ID", "shipped"), m("title", "changed"));
    Assertions.assertEquals("changed", read("shipped").get("title"));

    Assertions.assertTrue(config.delete(page, m("ID", "shipped")));
    Assertions.assertNull(app.table("page").get("shipped"));
    Assertions.assertEquals(m("ID", "shipped", "title", "v1"), read("shipped"));
  }

  @Test
  public void deleteAppRecordRemovesIt() throws Exception {
    config.create(page, m("ID", "own", "title", "mine"));
    Assertions.assertTrue(config.delete(page, m("ID", "own")));
    Assertions.assertNull(read("own"));
    Assertions.assertFalse(listed("own"));
  }

  @Test
  public void deleteMissingRecord() throws Exception {
    Assertions.assertFalse(config.delete(page, m("ID", "missing")));
  }

  @Test
  public void createExistingPlatformRecordFails() throws Exception {
    // a platform record cannot be deleted, so it cannot be created again either
    Exception e = Assertions.assertThrows(Exception.class,
        () -> config.create(page, m("ID", "shipped", "title", "new")));
    Assertions.assertTrue(e.getMessage().contains("already exists"), e.getMessage());
  }

  @Test
  public void deleteTableResetsAppChanges() throws Exception {
    // the platform ships a table, the app changed its layout and a column title
    platform.table("dj-database").put("dj/northwind",
        m("ID", "dj/northwind", "name", "northwind", "tables",
            m("EMPLOYEES", m("ID", "dj/northwind/EMPLOYEES", "name", "EMPLOYEES", "properties",
                m("NAME", m("ID", "dj/northwind/EMPLOYEES/NAME", "name", "NAME"))))));
    config.update(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES"),
        m("ID", "dj/northwind/EMPLOYEES", "instanceLayout", m("widget", "page")));
    config.update(Table.ofName("Property"), m("ID", "dj/northwind/EMPLOYEES/NAME"),
        m("ID", "dj/northwind/EMPLOYEES/NAME", "title", "Name"));

    // status quo: deleting the table removes the app's table entry (layout and column settings),
    // but leaves the app's dj-database record with only its ID behind
    Assertions.assertTrue(config.delete(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES")));
    Assertions.assertEquals(m("ID", "dj/northwind"), app.table("dj-database").get("dj/northwind"));

    Map<String, Object> table =
        config.read(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES"));
    Assertions.assertNotNull(table);
    Assertions.assertNull(table.get("instanceLayout"));
  }

  @Test
  public void deletePropertyResetsAppChanges() throws Exception {
    platform.table("dj-database").put("dj/northwind",
        m("ID", "dj/northwind", "name", "northwind", "tables",
            m("EMPLOYEES", m("ID", "dj/northwind/EMPLOYEES", "name", "EMPLOYEES", "properties",
                m("NAME", m("ID", "dj/northwind/EMPLOYEES/NAME", "name", "NAME", "title", "v1"))))));
    config.update(Table.ofName("Property"), m("ID", "dj/northwind/EMPLOYEES/NAME"),
        m("ID", "dj/northwind/EMPLOYEES/NAME", "title", "changed"));

    Assertions.assertTrue(
        config.delete(Table.ofName("Property"), m("ID", "dj/northwind/EMPLOYEES/NAME")));
    Assertions.assertEquals("v1", config
        .read(Table.ofName("Property"), m("ID", "dj/northwind/EMPLOYEES/NAME")).get("title"));
  }
}
