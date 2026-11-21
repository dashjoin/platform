package org.dashjoin.service;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.dashjoin.function.AppRepoTest;
import org.dashjoin.function.GitPull;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The platform ships a record on the classpath (v1), the app overrides part of it, then a new
 * platform version ships v2 of the record. The config API must return v2 with the app's changes on
 * top, both for changes committed in the app and for changes made in the UI. Exception (status
 * quo): a nested object changed in the UI keeps hiding the platform's values, see uiNestedDelta.
 * See AppRepoTest for the setup and how to run this with another storage.
 */
public class PlatformUpgradeTest extends AppRepoTest {

  final Table page = Table.ofName("page");

  static final String ASSET = "model/page/GitTestAsset.json";

  static Map<String, Object> v1() {
    return MapUtil.of("ID", "GitTestAsset", "title", "v1", "color", "red", "layout",
        MapUtil.of("a", 1, "b", 1));
  }

  static Map<String, Object> v2() {
    return MapUtil.of("ID", "GitTestAsset", "title", "v2", "color", "red", "size", 10, "layout",
        MapUtil.of("a", 5, "b", 1, "c", 3));
  }

  /** the asset as the running platform version ships it */
  Map<String, Object> shipped = v1();

  @Override
  protected void createApp() throws Exception {
    write(remote, "model/page/GitTestOther.json", "{\"ID\": \"GitTestOther\"}");
  }

  /**
   * the classpath of the running platform version: the asset only
   */
  @Override
  protected JSONDatabase platform() {
    Map<String, Object> asset = shipped;
    return new JSONDatabase() {
      @Override
      public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
        if ("page".equals(s.name) && asset.get("ID").equals(search.get("ID")))
          return copy(asset);
        return null;
      }

      @Override
      public Map<String, Map<String, Object>> queryMap(QueryMeta info,
          Map<String, Object> arguments) throws Exception {
        Map<String, Map<String, Object>> res = new HashMap<>();
        String[] parts = info.query.split("/");
        if ("page".equals(parts[0]) && (parts.length == 1 || asset.get("ID").equals(parts[1])))
          res.put((String) asset.get("ID"), copy(asset));
        return res;
      }
    };
  }

  static Map<String, Object> copy(Map<String, Object> m) {
    Map<String, Object> res = new HashMap<>();
    for (Map.Entry<String, Object> e : m.entrySet())
      res.put(e.getKey(),
          e.getValue() instanceof Map ? new HashMap<>((Map<?, ?>) e.getValue()) : e.getValue());
    return res;
  }

  /**
   * restart with the next platform version
   */
  void upgrade() throws Exception {
    shipped = v2();
    config = config();
  }

  Map<String, Object> read() throws Exception {
    return config.read(page, Map.of("ID", "GitTestAsset"));
  }

  Map<String, Object> listed() throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "page";
    for (Map<String, Object> r : config.query(q, null))
      if ("GitTestAsset".equals(r.get("ID")))
        return r;
    return null;
  }

  void assertAsset(Map<String, Object> expected) throws Exception {
    Assertions.assertEquals(expected, new HashMap<>(read()));
    Assertions.assertEquals(expected, new HashMap<>(listed()));
  }

  /**
   * deploy an app version that overrides part of the asset
   */
  void deployDelta(String json) throws Exception {
    write(remote, ASSET, json);
    commit(origin, "override asset");
    fn(new GitPull()).run(null);
  }

  @Test
  public void noDelta() throws Exception {
    assertAsset(v1());
    upgrade();
    assertAsset(v2());
  }

  @Test
  public void committedDelta() throws Exception {
    deployDelta("{\"ID\": \"GitTestAsset\", \"color\": \"blue\"}");
    assertAsset(MapUtil.of("ID", "GitTestAsset", "title", "v1", "color", "blue", "layout",
        MapUtil.of("a", 1, "b", 1)));

    upgrade();
    assertAsset(MapUtil.of("ID", "GitTestAsset", "title", "v2", "color", "blue", "size", 10,
        "layout", MapUtil.of("a", 5, "b", 1, "c", 3)));
  }

  @Test
  public void committedNestedDelta() throws Exception {
    deployDelta("{\"ID\": \"GitTestAsset\", \"layout\": {\"b\": 2}}");
    upgrade();
    assertAsset(MapUtil.of("ID", "GitTestAsset", "title", "v2", "color", "red", "size", 10,
        "layout", MapUtil.of("a", 5, "b", 2, "c", 3)));
  }

  @Test
  public void uiDelta() throws Exception {
    config.update(page, Map.of("ID", "GitTestAsset"), new HashMap<>(Map.of("color", "blue")));

    // only the change is stored in the app
    Assertions.assertEquals(Map.of(ASSET, "untracked"), status());
    String file = Files.readString(appHome.resolve(ASSET));
    Assertions.assertTrue(file.contains("blue"), file);
    Assertions.assertFalse(file.contains("title"), file);

    upgrade();
    assertAsset(MapUtil.of("ID", "GitTestAsset", "title", "v2", "color", "blue", "size", 10,
        "layout", MapUtil.of("a", 5, "b", 1, "c", 3)));
  }

  @Test
  public void uiNestedDelta() throws Exception {
    // the UI sends the whole nested object with one value changed
    config.update(page, Map.of("ID", "GitTestAsset"),
        new HashMap<>(Map.of("layout", MapUtil.of("a", 1, "b", 2))));

    upgrade();
    // status quo (since at least 2021): UnionDatabase.updateLocked stores the whole nested object,
    // not only the nested keys that differ, so the v1 value layout.a = 1 hides v2's a = 5. Keys new
    // in v2 (c) come through. With nested diffs, layout would be {a: 5, b: 2, c: 3}
    assertAsset(MapUtil.of("ID", "GitTestAsset", "title", "v2", "color", "red", "size", 10,
        "layout", MapUtil.of("a", 1, "b", 2, "c", 3)));
  }

  @Test
  public void uiChangeBackToDefaultRemovesDelta() throws Exception {
    config.update(page, Map.of("ID", "GitTestAsset"), new HashMap<>(Map.of("color", "blue")));
    config.update(page, Map.of("ID", "GitTestAsset"), new HashMap<>(Map.of("color", "red")));
    Assertions.assertEquals(Map.of(), status());

    upgrade();
    assertAsset(v2());
  }
}
