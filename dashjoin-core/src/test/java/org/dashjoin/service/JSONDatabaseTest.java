package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.dashjoin.model.Table.ofName;
import static org.dashjoin.service.UnionDatabase.merge;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang3.NotImplementedException;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.quarkus.test.junit.QuarkusTest;

/**
 * tests various aspects of the config database (merging, handling of pojos etc...)
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class JSONDatabaseTest extends AbstractDatabaseTest {

  @Inject
  Services services;

  @Override
  PojoDatabase db() {
    return (PojoDatabase) services.getConfig();
  }

  @Override
  String getQuery() {
    return "EMP";
  }

  @Override
  String getQuery2() {
    return "EMP/1";
  }

  @Test
  public void testDeleteReadOnly() throws Exception {
    Database db = db();
    Table s = new Table();
    s.name = "EMP";
    db.update(s, of("ID", 1), of("NAME", "newname"));
    Assertions.assertEquals("newname", db.read(s, of("ID", 1)).get("NAME"));
    Assertions.assertEquals(1000, db.read(s, of("ID", 1)).get("WORKSON"));
    db.delete(s, of("ID", 1));

    // cleanup
    new File("model/EMP/1.deleted").delete();
  }

  @Test
  public void mergeAdd() {
    Map<String, Object> old = ImmutableMap.of("a", 1);
    Map<String, Object> value = ImmutableMap.of("b", "B");
    Map<String, Object> merge = merge(old, value);
    Assertions.assertEquals("{a=1, b=B}", merge.toString());
  }

  @Test
  public void mergeOverride() {
    Map<String, Object> old = ImmutableMap.of("a", 1);
    Map<String, Object> value = ImmutableMap.of("a", "B");
    Map<String, Object> merge = merge(old, value);
    Assertions.assertEquals("{a=B}", merge.toString());
  }

  @Test
  public void mergeLeftNull() {
    Map<String, Object> merge = merge(null, of("a", 1));
    Assertions.assertEquals("{a=1}", merge.toString());
  }

  @Test
  public void mergeRightNull() {
    Map<String, Object> merge = merge(of("a", 1), null);
    Assertions.assertEquals("{a=1}", merge.toString());
  }

  @Test
  public void mergeNested() {
    Map<String, Object> merge = merge(of("a", of("b", 1)), of("a", of("c", 1)));
    Assertions.assertEquals("{a={b=1, c=1}}", merge.toString());
  }

  @Test
  public void mergeDelete() {
    Map<String, Object> merge = merge(of("a", 1), singletonMap("a", null));
    Assertions.assertEquals("{}", merge.toString());
  }

  @Test
  public void mergeArray() {
    // lists are not merged, since otherwise there is no way to delete a list
    Map<String, Object> merge = merge(of("a", asList(1)), of("a", asList(2)));
    Assertions.assertEquals("{a=[2]}", merge.toString());
  }

  @Test
  public void mergeArray2() {
    // lists are not merged, since otherwise there is no way to delete a list
    Map<String, Object> merge = UnionDatabase.mergeArray(of("a", newArrayList(asList(1))),
        of("a", newArrayList(asList(2))));
    Assertions.assertEquals("{a=[1, 2]}", merge.toString());
  }

  @Test
  public void deleteArray() {
    // lists are not merged, since otherwise there is no way to delete a list
    Map<String, Object> merge = merge(of("a", asList(1)), singletonMap("a", null));
    Assertions.assertEquals("{}", merge.toString());
  }

  @Test
  public void pojo() throws Exception {
    PojoDatabase db = db();
    Database d = db.getDatabase("dj/junit");
    Assertions.assertEquals("jdbc:h2:mem:test", ((SQLDatabase) d).url);
  }

  @Test
  public void pojoAll() throws Exception {
    PojoDatabase db = db();
    List<AbstractDatabase> d = db.getDatabases();
    Assertions.assertEquals("jdbc:h2:mem:test", ((SQLDatabase) d.get(0)).url);
  }

  @Test
  public void pojoCache() throws Exception {
    PojoDatabase db = db();

    Table m = new Table();
    m.name = "dj-database";
    db.create(m, of("ID", "c", "djClassName", SQLDatabase.class.getName(), "url", "jdbc:h2:mem:c"));
    Assertions.assertNotNull(db.getCached("c"));

    Object beforeUpdate = db.getCached("c");
    db.update(m, of("ID", "c"), MapUtil.of("url", "jdbc:h2:mem:newurl"));
    Object afterUpdate = db.getCached("c");
    Assertions.assertNotNull(afterUpdate);
    Assertions.assertFalse(beforeUpdate == afterUpdate);

    db.delete(m, of("ID", "c"));
    Assertions.assertNull(db.getCached("c"));
    try {
      db.getDatabase("c");
      Assertions.fail();
    } catch (IllegalArgumentException shouldOccurr) {
    }
  }

  @Test
  public void saveOnlyWhatIsRequired() throws Exception {
    UnionDatabase u = new UnionDatabase();
    u._user = JSONDatabaseFactory.getPersistantInstance();
    JSONDatabase cp = JSONDatabaseFactory.getReadOnlyInstance();
    u._dbs = Arrays.asList(cp);

    Table schema = new Table();
    schema.name = "EMP";
    u.update(schema, Maps.newHashMap(of("ID", 1)), Maps.newHashMap(of("NAME", "mike", "x", 7)));
    Assertions.assertEquals(null, u._user.read(schema, of("ID", 1)).get("NAME"));
    u.delete(schema, of("ID", 1));
    new File("model/EMP/1.deleted").delete();

    u.update(schema, Maps.newHashMap(of("ID", 2)), Maps.newHashMap(of("NAME", "joe2")));
    Assertions.assertEquals("{ID=2, NAME=joe2}", "" + u._user.read(schema, of("ID", 2)));

    // this save is a noop (only redundant data) - user entry actually gets deleted
    u.update(schema, Maps.newHashMap(of("ID", 2)), Maps.newHashMap(of("NAME", "joe")));
    Assertions.assertNull(u._user.read(schema, of("ID", 2)));
    Assertions.assertEquals("{ID=2, NAME=joe, WORKSON=1000}", "" + u.read(schema, of("ID", 2)));
  }

  @Test
  public void mapEquality() {
    Assertions.assertEquals(Collections.EMPTY_MAP, of());
    Assertions.assertEquals(of("a", 1), of("a", 1));
    Assertions.assertEquals(of("a", of("a", 1)), of("a", of("a", 1)));
    Map<String, Map<String, Object>> map = new HashMap<>();
    Map<String, Object> map2 = new LinkedHashMap<>();
    map2.put("a", 1);
    map.put("a", map2);
    Assertions.assertEquals(of("a", of("a", 1)), map);
  }

  @Test
  public void testQuery() throws Exception {
    JSONDatabase db = JSONDatabaseFactory.getReadOnlyInstance();

    QueryMeta info = new QueryMeta();
    info.query = "widget";
    List<Map<String, Object>> res = db.query(info, null);
    Assertions.assertTrue(res.toString().contains("ID=Test"));

    Table table = new Table();
    table.name = "widget";
    List<Map<String, Object>> res2 = db.all(table, 0, 9, null, false, null);
    Assertions.assertTrue(res2.toString().contains("ID=Test"));
  }

  @Test
  public void testCreate() throws Exception {
    Assertions.assertThrows(IOException.class, () -> {
      JSONDatabase db = JSONDatabaseFactory.getReadOnlyInstance();
      db.create(null, null);
    });
  }

  // TODO: this test should throw IOException when updating -
  // but only if an existing obj should be updated,
  // not if a null object is given, thus NPE:
  @Test
  public void testUpdate() throws Exception {
    Assertions.assertThrows(NullPointerException.class, () -> {
      JSONDatabase db = JSONDatabaseFactory.getReadOnlyInstance();
      db.update(null, null, null);
    });
  }

  @Test
  public void testDelete() throws Exception {
    Assertions.assertThrows(IOException.class, () -> {
      JSONDatabase db = JSONDatabaseFactory.getReadOnlyInstance();
      db.delete(null, null);
    });
  }

  @Test
  public void testQueryEditor() throws Exception {
    Assertions.assertThrows(NotImplementedException.class, () -> {
      JSONDatabase db = JSONDatabaseFactory.getPersistantInstance();
      db.getQueryEditor();
    });
  }

  @Test
  public void testQueryMeta() throws Exception {
    Assertions.assertThrows(NotImplementedException.class, () -> {
      JSONDatabase db = JSONDatabaseFactory.getPersistantInstance();
      db.queryMeta(null, null);
    });
  }

  @Test()
  public void testOtherOps() throws Exception {
    JSONDatabase db = JSONDatabaseFactory.getReadOnlyInstance();
    db.connectAndCollectMetadata();
    db.close();
    Assertions.assertEquals("table", db.getTablesInQuery("table/id").get(0));
  }

  @Test
  public void crudTest() throws Exception {
    new File("model/crud/x.json").delete();
    JSONDatabase db = JSONDatabaseFactory.getPersistantInstance();

    // create can contain a mix of keys and values
    db.create(ofName("crud"), of("ID", "x", "name", "tester"));

    // setting a key=null will remove it from JSON
    db.update(ofName("crud"), of("ID", "x"),
        MapUtil.of("name", null, "coord", MapUtil.of("x", 1, "y", 2)));

    // setting a new coord does NOT merge
    db.update(ofName("crud"), of("ID", "x"), MapUtil.of("coord", MapUtil.of("z", 3)));

    Assertions.assertEquals("{ID=x, coord={z=3}}", "" + db.read(ofName("crud"), of("ID", "x")));

    db.delete(ofName("crud"), of("ID", "x"));
  }

  @Test
  public void testJsonRead() throws Exception {
    String newline = "\n";

    // allow java comments
    Assertions.assertEquals("{}",
        "" + JSONDatabase.objectMapper.readValue("/* comment */ {}".getBytes(), JSONDatabase.tr));

    // allow single quote
    Assertions.assertEquals("{x=y}",
        "" + JSONDatabase.objectMapper.readValue("{'x': 'y'}".getBytes(), JSONDatabase.tr));

    // multi line string
    Assertions.assertEquals("{x=1" + newline + "2}", "" + JSONDatabase.objectMapper
        .readValue(("{\"x\": \"1" + newline + "2\"}").getBytes(), JSONDatabase.tr));

    // 'normal' string
    Assertions.assertEquals("{x=1\n2}", ""
        + JSONDatabase.objectMapper.readValue(("{\"x\": \"1\\n2\"}").getBytes(), JSONDatabase.tr));
  }

  @Test
  public void testSingleQuoteMultiLine() throws Exception {
    String newline = "\n";

    try {
      // single quote and multi line
      Assertions.assertEquals("{x=1" + newline + "2}", "" + JSONDatabase.objectMapper
          .readValue(("{\"x\": '1" + newline + "2'}").getBytes(), JSONDatabase.tr));
      Assertions.fail();
    } catch (JsonParseException ok) {

    }
  }

  public static class Payload {
    public String s;
  }

  @Test
  public void testExtraField() throws Exception {
    Payload p = JSONDatabase.fromMap(MapUtil.of("s", "hello world", "unknown", 42), Payload.class);
    Assertions.assertEquals(p.s, "hello world");
  }
}
