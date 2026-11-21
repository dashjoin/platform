package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Baseline: documents how UnionDatabase.update stores changes to a record that a read-only layer
 * (platform classpath) provides, as of 7.1. The user layer (app) only keeps what differs from the
 * read-only layers, compared per top-level field. A nested object that differs in any key is
 * stored whole, including the keys the user did not change. After a platform upgrade, those copied
 * keys hide the new platform values.
 *
 * These tests describe the current behaviour, not necessarily the desired one. Tests marked
 * "status quo" are expected to change when nested changes are stored as a diff.
 */
public class UnionDatabaseNestedDeltaTest {

  /** simple in-memory JSON layer, returns copies like the file and classpath layers do */
  static class MemoryDatabase extends JSONDatabase {

    final Map<String, Map<String, Map<String, Object>>> tables = new HashMap<>();
    final boolean readOnly;

    MemoryDatabase(boolean readOnly) {
      this.readOnly = readOnly;
    }

    Map<String, Map<String, Object>> table(String name) {
      return tables.computeIfAbsent(name, k -> new LinkedHashMap<>());
    }

    @Override
    public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
      Map<String, Object> res = table(s.name).get("" + search.get("ID"));
      return res == null ? null : copy(res);
    }

    @Override
    public Map<String, Map<String, Object>> queryMap(QueryMeta info,
        Map<String, Object> arguments) throws Exception {
      Map<String, Map<String, Object>> res = new LinkedHashMap<>();
      for (Entry<String, Map<String, Object>> e : table(info.query).entrySet())
        res.put(e.getKey(), copy(e.getValue()));
      return res;
    }

    @Override
    public void create(Table s, Map<String, Object> object) throws Exception {
      if (readOnly)
        super.create(s, object);
      table(s.name).put("" + object.get("ID"), copy(object));
    }

    @Override
    public boolean delete(Table s, Map<String, Object> search) throws Exception {
      if (readOnly)
        return super.delete(s, search);
      return table(s.name).remove("" + search.get("ID")) != null;
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T copy(T o) {
    if (o instanceof Map) {
      Map<String, Object> res = new LinkedHashMap<>();
      for (Entry<String, Object> e : ((Map<String, Object>) o).entrySet())
        res.put(e.getKey(), copy(e.getValue()));
      return (T) res;
    }
    if (o instanceof List) {
      List<Object> res = new ArrayList<>();
      for (Object i : (List<Object>) o)
        res.add(copy(i));
      return (T) res;
    }
    return o;
  }

  /** builds a mutable map from key value pairs */
  static Map<String, Object> m(Object... kv) {
    Map<String, Object> res = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2)
      res.put((String) kv[i], kv[i + 1]);
    return res;
  }

  final Table page = Table.ofName("page");

  /** platform defaults (classpath) */
  MemoryDatabase platform;

  /** app / user layer */
  MemoryDatabase app;

  UnionDatabase db;

  static Map<String, Object> v1() {
    return m("ID", "asset", "title", "v1", "color", "red", "roles", Arrays.asList("a", "b"),
        "layout", m("a", 1, "b", 1));
  }

  static Map<String, Object> v2() {
    return m("ID", "asset", "title", "v2", "color", "red", "size", 10, "roles",
        Arrays.asList("a", "b"), "layout", m("a", 5, "b", 1, "c", 3));
  }

  @BeforeEach
  public void setup() throws Exception {
    platform = new MemoryDatabase(true);
    platform.table("page").put("asset", v1());
    app = new MemoryDatabase(false);
    db = new UnionDatabase();
    db._dbs = Arrays.asList(platform);
    db._user = app;
  }

  /** the next platform version ships v2 of the asset */
  void upgrade() {
    platform.table("page").put("asset", v2());
  }

  void update(Map<String, Object> change) throws Exception {
    db.update(page, m("ID", "asset"), change);
  }

  /** what the app layer stores for the asset (null: nothing) */
  Map<String, Object> stored() {
    return app.table("page").get("asset");
  }

  Map<String, Object> read() throws Exception {
    return db.read(page, m("ID", "asset"));
  }

  @Test
  public void noChangeStoresNothing() throws Exception {
    Assertions.assertEquals(v1(), read());
    Assertions.assertNull(stored());
  }

  @Test
  public void topLevelChangeStoresOnlyThatField() throws Exception {
    update(m("color", "blue"));
    Assertions.assertEquals(m("ID", "asset", "color", "blue"), stored());

    upgrade();
    Assertions.assertEquals("v2", read().get("title"));
    Assertions.assertEquals("blue", read().get("color"));
    Assertions.assertEquals(m("a", 5, "b", 1, "c", 3), read().get("layout"));
  }

  @Test
  public void sendingTheWholeRecordStoresOnlyChangedFields() throws Exception {
    // the UI sends all fields of the record
    Map<String, Object> form = v1();
    form.put("color", "blue");
    update(form);
    Assertions.assertEquals(m("ID", "asset", "color", "blue"), stored());
  }

  @Test
  public void nestedChangeStoresWholeObject() throws Exception {
    // status quo: the UI sends the whole nested object with one key changed, the app stores all
    // keys, including a = 1 which equals the platform value
    update(m("layout", m("a", 1, "b", 2)));
    Assertions.assertEquals(m("ID", "asset", "layout", m("a", 1, "b", 2)), stored());
    Assertions.assertEquals(m("a", 1, "b", 2), read().get("layout"));
  }

  @Test
  public void nestedChangeHidesPlatformUpgrade() throws Exception {
    // status quo: after the upgrade, the copied a = 1 hides the new platform value a = 5. New keys
    // (c) and top-level fields still come from the platform
    update(m("layout", m("a", 1, "b", 2)));
    upgrade();
    Assertions.assertEquals(m("a", 1, "b", 2, "c", 3), read().get("layout"));
    Assertions.assertEquals("v2", read().get("title"));
  }

  @Test
  public void partialNestedChangeIsMerged() throws Exception {
    // a client that sends only the changed nested key gets a diff in the app, which merges with
    // the platform values before and after the upgrade
    update(m("layout", m("b", 2)));
    Assertions.assertEquals(m("ID", "asset", "layout", m("b", 2)), stored());
    Assertions.assertEquals(m("a", 1, "b", 2), read().get("layout"));

    upgrade();
    Assertions.assertEquals(m("a", 5, "b", 2, "c", 3), read().get("layout"));
  }

  @Test
  public void secondNestedChangeReplacesStoredObject() throws Exception {
    update(m("layout", m("b", 2)));
    // status quo: once the field is in the app, the next nested change replaces the stored object
    // with whatever is sent, here the whole object
    update(m("layout", m("a", 1, "b", 3)));
    Assertions.assertEquals(m("ID", "asset", "layout", m("a", 1, "b", 3)), stored());
  }

  @Test
  public void nestedObjectEqualToPlatformStoresNothing() throws Exception {
    update(m("layout", m("a", 1, "b", 1)));
    Assertions.assertNull(stored());
  }

  @Test
  public void nestedChangeBackToPlatformRemovesIt() throws Exception {
    update(m("layout", m("a", 1, "b", 2)));
    update(m("layout", m("a", 1, "b", 1)));
    // the field is removed from the app, and the record with it since only the ID is left
    Assertions.assertNull(stored());

    upgrade();
    Assertions.assertEquals(m("a", 5, "b", 1, "c", 3), read().get("layout"));
  }

  @Test
  public void changeBackAfterUpgradeKeepsStaleCopy() throws Exception {
    // status quo: after the upgrade, sending the old v1 layout back is no longer equal to the
    // platform value, so the stale copy is stored again instead of being removed
    update(m("layout", m("a", 1, "b", 2)));
    upgrade();
    update(m("layout", m("a", 1, "b", 1, "c", 3)));
    Assertions.assertEquals(m("ID", "asset", "layout", m("a", 1, "b", 1, "c", 3)), stored());
    Assertions.assertEquals(1, ((Map<?, ?>) read().get("layout")).get("a"));
  }

  @Test
  public void removedNestedKeyComesBackFromPlatform() throws Exception {
    // status quo: removing a nested key the platform has is not possible, the stored object lacks
    // the key and the merge adds it back from the platform
    update(m("layout", m("a", 1)));
    Assertions.assertEquals(m("ID", "asset", "layout", m("a", 1)), stored());
    Assertions.assertEquals(m("a", 1, "b", 1), read().get("layout"));
  }

  @Test
  public void removedTopLevelFieldComesBackFromPlatform() throws Exception {
    // status quo: null removes the app's override, not the platform's field
    update(m("color", "blue"));
    update(m("color", null));
    Assertions.assertNull(stored());
    Assertions.assertEquals("red", read().get("color"));
  }

  @Test
  public void arrayIsStoredWhole() throws Exception {
    // arrays are not merged: the app stores and wins with the whole array
    update(m("roles", Arrays.asList("a", "b", "c")));
    Assertions.assertEquals(m("ID", "asset", "roles", Arrays.asList("a", "b", "c")), stored());

    upgrade();
    Assertions.assertEquals(Arrays.asList("a", "b", "c"), read().get("roles"));
  }

  @Test
  public void nestedChangeOfAppOnlyRecordReplacesObject() throws Exception {
    // a record the platform does not have: a nested change replaces the stored object
    db.create(page, m("ID", "own", "layout", m("a", 1, "b", 1)));
    db.update(page, m("ID", "own"), m("layout", m("b", 2)));
    Assertions.assertEquals(m("ID", "own", "layout", m("b", 2)), app.table("page").get("own"));
  }
}
