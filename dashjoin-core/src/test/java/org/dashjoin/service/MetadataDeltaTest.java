package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Baseline: documents what happens to the app's table and column settings (layouts, titles, ...)
 * when the schema of a connected database changes outside of Dashjoin (ETL, DDL on the database)
 * or the database cannot be reached, as of 7.1.
 *
 * The tables and columns come from the database's metadata, which metadataCollection puts into a
 * ProviderDatabase layer ({status, tables}, no ID). The app's settings live in tables.TABLE of the
 * app's dj-database record. Entries without an ID (settings whose table or column is not in the
 * metadata) are hidden on reads by PojoDatabase.cleanDatabase / cleanTable, but stay in the app.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
public class MetadataDeltaTest {

  static final String DB = "dj/northwind";
  static final String EMPLOYEES = "dj/northwind/EMPLOYEES";

  MemoryDatabase app;
  PojoDatabase config;
  ProviderDatabase provider;

  static Map<String, Object> column(String table, String name) {
    return m("ID", DB + "/" + table + "/" + name, "name", name, "parent", DB + "/" + table, "type",
        "string");
  }

  static Map<String, Object> table(String name, String... columns) {
    Map<String, Object> props = m();
    for (String c : columns)
      props.put(c, column(name, c));
    return m("ID", DB + "/" + name, "name", name, "parent", DB, "properties", props);
  }

  /** the metadata of the connected database, as metadataCollection provides it */
  @SuppressWarnings({"unchecked", "rawtypes"})
  void metadata(String status, Map<String, Object> tables) {
    if (provider != null)
      config._dbs.remove(provider);
    provider = new ProviderDatabase(DB,
        (Map) m("dj-database", m(DB, m("status", status, "tables", tables))));
    config._dbs.add(provider);
  }

  @BeforeEach
  public void setup() throws Exception {
    app = new MemoryDatabase(false);
    // the database definition is part of the app
    app.table("dj-database").put(DB, m("ID", DB, "name", "northwind", "parent", "dj",
        "djClassName", "org.dashjoin.service.SQLDatabase", "url", "jdbc:..."));
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = new CopyOnWriteArrayList<>();
    config._dbs.add(new MemoryDatabase(true));
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME", "AGE")));

    // the app's settings
    config.update(Table.ofName("Table"), m("ID", EMPLOYEES),
        m("ID", EMPLOYEES, "instanceLayout", m("widget", "page"), "title", "Staff"));
    config.update(Table.ofName("Property"), m("ID", EMPLOYEES + "/AGE"),
        m("ID", EMPLOYEES + "/AGE", "title", "Age in years"));
  }

  Map<String, Object> table(String id) throws Exception {
    return config.read(Table.ofName("Table"), m("ID", id));
  }

  Map<String, Object> column(String id) throws Exception {
    return config.read(Table.ofName("Property"), m("ID", id));
  }

  List<Map<String, Object>> query(String query) throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = query;
    return config.query(q, null);
  }

  boolean listedTable(String name) throws Exception {
    for (Map<String, Object> t : query("Table"))
      if (name.equals(t.get("name")))
        return true;
    return false;
  }

  /** the app's settings of the database */
  Map<String, Object> stored() {
    return app.table("dj-database").get(DB);
  }

  /** the app's settings */
  static Map<String, Object> settings() {
    return m("EMPLOYEES", m("instanceLayout", m("widget", "page"), "title", "Staff", "properties",
        m("AGE", m("title", "Age in years"))));
  }

  @Test
  public void settingsMergeWithMetadata() throws Exception {
    Assertions.assertEquals(m("widget", "page"), table(EMPLOYEES).get("instanceLayout"));
    Assertions.assertEquals("Staff", table(EMPLOYEES).get("title"));
    Assertions.assertEquals("Age in years", column(EMPLOYEES + "/AGE").get("title"));
    Assertions.assertEquals("string", column(EMPLOYEES + "/AGE").get("type"));
    Assertions.assertEquals(settings(), stored().get("tables"));
    // the status comes from the metadata
    Assertions.assertEquals("OK", query("dj-database").stream()
        .filter(d -> DB.equals(d.get("ID"))).findFirst().get().get("status"));
  }

  @Test
  public void columnDroppedOutside() throws Exception {
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME")));

    // status quo: the column's settings are hidden, but stay in the app
    Assertions.assertNull(column(EMPLOYEES + "/AGE"));
    @SuppressWarnings("unchecked")
    Map<String, Object> props = (Map<String, Object>) table(EMPLOYEES).get("properties");
    Assertions.assertFalse(props.containsKey("AGE"));
    Assertions.assertEquals(settings(), stored().get("tables"));

    // status quo: a new column with the same name gets the old settings
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME", "AGE")));
    Assertions.assertEquals("Age in years", column(EMPLOYEES + "/AGE").get("title"));
  }

  @Test
  public void columnRenamedOutside() throws Exception {
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME", "YEARS")));

    // status quo: the settings stay under the old name, the renamed column has none
    Assertions.assertNull(column(EMPLOYEES + "/AGE"));
    Assertions.assertNull(column(EMPLOYEES + "/YEARS").get("title"));
    Assertions.assertEquals(settings(), stored().get("tables"));
  }

  @Test
  public void tableDroppedOutside() throws Exception {
    metadata("OK", m());

    // status quo: the table's settings are hidden, but stay in the app
    Assertions.assertNull(table(EMPLOYEES));
    Assertions.assertFalse(listedTable("EMPLOYEES"));
    Assertions.assertEquals(settings(), stored().get("tables"));

    // status quo: a new table with the same name gets the old settings
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "ID")));
    Assertions.assertEquals(m("widget", "page"), table(EMPLOYEES).get("instanceLayout"));
  }

  @Test
  public void tableRenamedOutside() throws Exception {
    metadata("OK", m("STAFF", table("STAFF", "NAME", "AGE")));

    // status quo: the settings stay under the old name, the renamed table has none
    Assertions.assertNull(table(EMPLOYEES));
    Assertions.assertNull(table(DB + "/STAFF").get("instanceLayout"));
    Assertions.assertNull(column(DB + "/STAFF/AGE").get("title"));
    Assertions.assertEquals(settings(), stored().get("tables"));
  }

  @Test
  public void databaseUnreachable() throws Exception {
    // metadataCollection failed: no tables, the error in status
    metadata("Error: connection refused", m());

    // status quo: while the database is down, its tables and their settings are not readable
    Assertions.assertNull(table(EMPLOYEES));
    Assertions.assertNull(column(EMPLOYEES + "/AGE"));
    Assertions.assertFalse(listedTable("EMPLOYEES"));
    Assertions.assertEquals(settings(), stored().get("tables"));

    // after reconnecting, everything is back
    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME", "AGE")));
    Assertions.assertEquals(m("widget", "page"), table(EMPLOYEES).get("instanceLayout"));
    Assertions.assertEquals("Age in years", column(EMPLOYEES + "/AGE").get("title"));
  }

  @Test
  public void saveWhileUnreachable() throws Exception {
    metadata("Error: connection refused", m());

    // status quo: settings of a table that is not in the metadata can still be saved, they are
    // stored but not readable until the table is back
    config.update(Table.ofName("Table"), m("ID", EMPLOYEES),
        m("ID", EMPLOYEES, "instanceLayout", m("widget", "changed")));
    Assertions.assertNull(table(EMPLOYEES));
    @SuppressWarnings("unchecked")
    Map<String, Object> tables = (Map<String, Object>) stored().get("tables");
    Assertions.assertEquals(m("widget", "changed"),
        ((Map<?, ?>) tables.get("EMPLOYEES")).get("instanceLayout"));

    metadata("OK", m("EMPLOYEES", table("EMPLOYEES", "NAME", "AGE")));
    Assertions.assertEquals(m("widget", "changed"), table(EMPLOYEES).get("instanceLayout"));
  }

  @Test
  public void databaseRecordKeepsDefinitionAndSettings() throws Exception {
    // the app's dj-database record holds both the connection and the table settings
    Map<String, Object> s = stored();
    Assertions.assertEquals("jdbc:...", s.get("url"));
    Assertions.assertEquals(settings(), s.get("tables"));
    Assertions.assertNull(s.get("status"));
  }
}
