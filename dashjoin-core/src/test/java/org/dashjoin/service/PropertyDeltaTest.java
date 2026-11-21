package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Baseline: documents how column (Property) settings are stored when the platform ships the column
 * (in the tables of a dj-database record on the classpath), as of 7.1.
 *
 * Like Table, Property records are updated by their own branch of PojoDatabase.update. It keeps
 * only the whitelisted keys (pkpos, ref, displayWith, createOnly, comment, title), copies them into
 * tables.TABLE.properties.COLUMN of the app's dj-database record without comparing them with the
 * platform, and removes empty levels.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
public class PropertyDeltaTest {

  static final String DB = "dj/northwind";
  static final String COLUMN = "dj/northwind/EMPLOYEES/NAME";

  static Map<String, Object> columnV1() {
    return m("ID", COLUMN, "name", "NAME", "parent", "dj/northwind/EMPLOYEES", "type", "string",
        "title", "Name");
  }

  static Map<String, Object> columnV2() {
    return m("ID", COLUMN, "name", "NAME", "parent", "dj/northwind/EMPLOYEES", "type", "string",
        "title", "Full name", "comment", "first and last name");
  }

  static Map<String, Object> database(Map<String, Object> column) {
    return m("ID", DB, "name", "northwind", "tables", m("EMPLOYEES", m("ID",
        "dj/northwind/EMPLOYEES", "name", "EMPLOYEES", "properties", m("NAME", column))));
  }

  MemoryDatabase platform;
  MemoryDatabase app;
  PojoDatabase config;

  @BeforeEach
  public void setup() {
    platform = new MemoryDatabase(true);
    platform.table("dj-database").put(DB, database(columnV1()));
    app = new MemoryDatabase(false);
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = Arrays.asList(platform);
  }

  void upgrade() {
    platform.table("dj-database").put(DB, database(columnV2()));
  }

  void update(Map<String, Object> change) throws Exception {
    Map<String, Object> object = m("ID", COLUMN);
    object.putAll(change);
    config.update(Table.ofName("Property"), m("ID", COLUMN), object);
  }

  Map<String, Object> stored() {
    return app.table("dj-database").get(DB);
  }

  Map<String, Object> column() throws Exception {
    return config.read(Table.ofName("Property"), m("ID", COLUMN));
  }

  /** what the app stores for the column, wrapped in its dj-database record */
  static Map<String, Object> storedColumn(Map<String, Object> column) {
    return m("ID", DB, "tables", m("EMPLOYEES", m("properties", m("NAME", column))));
  }

  @Test
  public void noChangeStoresNothing() throws Exception {
    Assertions.assertEquals(columnV1(), column());
    Assertions.assertNull(stored());
  }

  @Test
  public void changeStoresOnlyThatKey() throws Exception {
    update(m("title", "Employee name"));
    Assertions.assertEquals(storedColumn(m("title", "Employee name")), stored());
    Assertions.assertEquals("Employee name", column().get("title"));

    // after the upgrade, the app's title wins, new keys come from the platform
    upgrade();
    Assertions.assertEquals("Employee name", column().get("title"));
    Assertions.assertEquals("first and last name", column().get("comment"));
  }

  @Test
  public void unchangedValueIsStored() throws Exception {
    // status quo: a value equal to the platform's is stored anyway and then hides the upgrade
    update(m("title", "Name"));
    Assertions.assertEquals(storedColumn(m("title", "Name")), stored());

    upgrade();
    Assertions.assertEquals("Name", column().get("title"));
  }

  @Test
  public void nullRemovesOverride() throws Exception {
    update(m("title", "Employee name"));
    update(m("title", null));
    // the key, the column, the table and the record are removed from the app
    Assertions.assertNull(stored());
    Assertions.assertEquals("Name", column().get("title"));
  }

  @Test
  public void otherKeysAreIgnored() throws Exception {
    // only pkpos, ref, displayWith, createOnly, comment and title can be changed
    update(m("type", "number", "name", "X"));
    Assertions.assertNull(stored());
    Assertions.assertEquals(columnV1(), column());
  }

  @Test
  public void severalColumnsShareTheRecord() throws Exception {
    update(m("title", "Employee name"));
    config.update(Table.ofName("Property"), m("ID", "dj/northwind/EMPLOYEES/AGE"),
        m("ID", "dj/northwind/EMPLOYEES/AGE", "comment", "in years"));
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m("properties",
        m("NAME", m("title", "Employee name"), "AGE", m("comment", "in years"))))), stored());

    update(m("title", null));
    Assertions.assertEquals(
        m("ID", DB, "tables", m("EMPLOYEES", m("properties", m("AGE", m("comment", "in years"))))),
        stored());
  }

  @Test
  public void columnAndLayoutShareTheTableEntry() throws Exception {
    config.update(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES"),
        m("ID", "dj/northwind/EMPLOYEES", "instanceLayout", m("widget", "page")));
    update(m("title", "Employee name"));
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m("instanceLayout",
        m("widget", "page"), "properties", m("NAME", m("title", "Employee name"))))), stored());
  }
}
