package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Baseline: documents how a layout edited in the UI (instanceLayout and tableLayout) is stored when
 * the platform ships the layout (in the tables of a dj-database record on the classpath), as of
 * 7.1. Every test runs for both layout keys.
 *
 * Table records are not updated by UnionDatabase.update but by the Table branch of
 * PojoDatabase.update. It copies the sent keys into tables.TABLE of the app's dj-database record
 * without comparing them with the platform. The UI sends the whole layout on save (RecordPage.tsx,
 * DefaultRecord.tsx, TablePage.tsx, DefaultTable.tsx) and null on delete, so the app always stores
 * a full copy of the layout, even if it is unchanged. The layout is a widget tree: its children are
 * an array, so the app's copy replaces the platform's children completely, while object keys still
 * merge.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
public class LayoutDeltaTest {

  static final String DB = "dj/northwind";
  static final String TABLE = "dj/northwind/EMPLOYEES";

  static Map<String, Object> layoutV1() {
    return m("widget", "page", "title", "Employee", "children",
        Arrays.asList(m("widget", "display"), m("widget", "text", "text", "v1")));
  }

  static Map<String, Object> layoutV2() {
    return m("widget", "page", "title", "Employee v2", "style", m("color", "red"), "children",
        Arrays.asList(m("widget", "display"), m("widget", "text", "text", "v2"),
            m("widget", "chart")));
  }

  /** the dj-database record the platform ships, with the given layout (null: none) */
  static Map<String, Object> database(String key, Map<String, Object> layout) {
    Map<String, Object> table = m("ID", TABLE, "name", "EMPLOYEES", "parent", DB);
    if (layout != null)
      table.put(key, layout);
    return m("ID", DB, "name", "northwind", "tables", m("EMPLOYEES", table));
  }

  /** v1 layout with the text of the second child changed, as the layout editor saves it */
  static Map<String, Object> edited() {
    return m("widget", "page", "title", "Employee", "children",
        Arrays.asList(m("widget", "display"), m("widget", "text", "text", "edited")));
  }

  MemoryDatabase platform;
  MemoryDatabase app;
  PojoDatabase config;

  @BeforeEach
  public void setup() {
    platform = new MemoryDatabase(true);
    app = new MemoryDatabase(false);
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = Arrays.asList(platform);
  }

  /** the platform ships the layout */
  void ship(String key, Map<String, Object> layout) {
    platform.table("dj-database").put(DB, database(key, layout));
  }

  /** what the UI sends when saving or deleting the layout */
  void save(String key, Map<String, Object> layout) throws Exception {
    config.update(Table.ofName("Table"), m("ID", TABLE), m("ID", TABLE, key, layout));
  }

  /** what the app stores for the database (null: nothing) */
  Map<String, Object> stored() {
    return app.table("dj-database").get(DB);
  }

  /** the layout as read by ID, checked against the query */
  @SuppressWarnings("unchecked")
  Map<String, Object> layout(String key) throws Exception {
    Map<String, Object> read = config.read(Table.ofName("Table"), m("ID", TABLE));
    QueryMeta q = new QueryMeta();
    q.query = "Table/EMPLOYEES";
    List<Map<String, Object>> queried = config.query(q, null);
    Assertions.assertEquals(1, queried.size());
    Assertions.assertEquals(read.get(key), queried.get(0).get(key));
    return (Map<String, Object>) read.get(key);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void noEditShowsPlatformLayout(String key) throws Exception {
    ship(key, layoutV1());
    Assertions.assertEquals(layoutV1(), layout(key));
    Assertions.assertNull(stored());
    ship(key, layoutV2());
    Assertions.assertEquals(layoutV2(), layout(key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void layoutEditStoresWholeLayout(String key) throws Exception {
    ship(key, layoutV1());
    save(key, edited());
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m(key, edited()))), stored());
    Assertions.assertEquals(edited(), layout(key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void layoutEditHidesPlatformUpgrade(String key) throws Exception {
    // status quo: after the upgrade, the app's copy wins for every key it has. title stays at the
    // v1 value, the children array is the app's (v2's text and chart are gone). Only keys that are
    // new in v2 (style) come through
    ship(key, layoutV1());
    save(key, edited());
    ship(key, layoutV2());
    Assertions.assertEquals(m("widget", "page", "title", "Employee", "style", m("color", "red"),
        "children", Arrays.asList(m("widget", "display"), m("widget", "text", "text", "edited"))),
        layout(key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void unchangedLayoutSaveIsStored(String key) throws Exception {
    // status quo: unlike other config records, saving a layout equal to the platform's still
    // stores a full copy, which then hides the platform upgrade
    ship(key, layoutV1());
    save(key, layoutV1());
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m(key, layoutV1()))), stored());

    ship(key, layoutV2());
    Map<String, Object> layout = layout(key);
    Assertions.assertEquals("Employee", layout.get("title"));
    Assertions.assertEquals(layoutV1().get("children"), layout.get("children"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void deleteLayoutRestoresPlatformLayout(String key) throws Exception {
    ship(key, layoutV1());
    save(key, edited());
    save(key, null);
    // the layout, the table entry and the record are removed from the app
    Assertions.assertNull(stored());
    Assertions.assertEquals(layoutV1(), layout(key));

    ship(key, layoutV2());
    Assertions.assertEquals(layoutV2(), layout(key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void otherTableFieldStoresOnlyThatField(String key) throws Exception {
    ship(key, layoutV1());
    config.update(Table.ofName("Table"), m("ID", TABLE), m("ID", TABLE, "title", "Mitarbeiter"));
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m("title", "Mitarbeiter"))),
        stored());

    ship(key, layoutV2());
    Assertions.assertEquals(layoutV2(), layout(key));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  public void layoutEditOnTableWithoutPlatformLayout(String key) throws Exception {
    // the platform has the table but no layout: the app stores the layout, the upgrade adds one
    // and the app's copy wins key by key as above
    ship(key, null);
    save(key, edited());
    Assertions.assertEquals(edited(), layout(key));

    ship(key, layoutV2());
    Assertions.assertEquals("Employee", layout(key).get("title"));
    Assertions.assertEquals(edited().get("children"), layout(key).get("children"));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"instanceLayout", "tableLayout"})
  @SuppressWarnings("unchecked")
  public void editingOneLayoutLeavesTheOtherAlone(String key) throws Exception {
    // the platform ships both layouts, the app edits one of them
    String other = key.equals("instanceLayout") ? "tableLayout" : "instanceLayout";
    Map<String, Object> db = database(key, layoutV1());
    ((Map<String, Map<String, Object>>) (Map<?, ?>) db.get("tables")).get("EMPLOYEES").put(other,
        layoutV1());
    platform.table("dj-database").put(DB, db);

    save(key, edited());
    Assertions.assertEquals(m("ID", DB, "tables", m("EMPLOYEES", m(key, edited()))), stored());
    Assertions.assertEquals(layoutV1(), layout(other));
  }
}
