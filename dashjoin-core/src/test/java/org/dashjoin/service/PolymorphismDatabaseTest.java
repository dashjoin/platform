package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

/**
 * tests the polymorphism database which translates database classes into JSON schema
 */
@QuarkusTest
public class PolymorphismDatabaseTest {
  @SuppressWarnings("unchecked")
  @Test
  public void jsonSchemaAnnotation() {
    Assert.assertEquals("{widget=password, type=string, case=[org.dashjoin.service.SQLDatabase]}",
        "" + ((Map<String, Object>) PolymorphismDatabase.jsonSchema(SQLDatabase.class)
            .get("properties")).get("password"));
  }

  @Test
  public void query() throws Exception {

    SQLDatabase res = new ObjectMapper().convertValue(new PolymorphismDatabase().tableProperties(),
        SQLDatabase.class);

    Assert.assertEquals(
        "[org.dashjoin.service.TestDatabase, org.dashjoin.service.SQLDatabase, org.dashjoin.service.RemoteDatabase]",
        res.tables.get("dj-database").properties.get("password")._case.toString());
  }

  @Test
  public void parse() {
    Map<String, Object> s = PolymorphismDatabase.jsonSchema(String[].class);
    Assert.assertEquals("{type=array, items={type=string}}", s.toString());
  }

  @Test
  public void parse2() {
    Map<String, Object> s = PolymorphismDatabase.jsonSchema(Map.class);
    Assert.assertEquals("{type=object, additionalProperties={type=string}}", s.toString());
  }

  public static class C {
    @JsonSchema(enums = {"a", "b"})
    public String s;

    public Boolean b;

    public Integer i;

    public Long l;

    public List<String> list;

    public Integer[] array;
  }

  public static class D {
    @JsonSchema(required = {"s", "i"})
    public C c;
  }

  @Test
  public void parseClass() throws Exception {
    JsonSchema s = C.class.getField("s").getAnnotation(JsonSchema.class);
    Class<?> c = C.class.getField("s").getType();
    Map<String, Object> res = PolymorphismDatabase.item(s, c, null);
    Assert.assertEquals("[a, b]", "" + res.get("enum"));

    Assert.assertEquals("{type=boolean}",
        "" + PolymorphismDatabase.item(null, C.class.getField("b").getType(), null));
    Assert.assertEquals("{type=number}",
        "" + PolymorphismDatabase.item(null, C.class.getField("i").getType(), null));
    Assert.assertEquals("{type=number}",
        "" + PolymorphismDatabase.item(null, C.class.getField("l").getType(), null));

    Assert.assertEquals(Arrays.asList("s", "i"),
        PolymorphismDatabase.item(D.class.getField("c").getAnnotation(JsonSchema.class),
            D.class.getField("c").getType(), null).get("required"));

    Assert.assertEquals("{type=array, items={type=string}}",
        "" + PolymorphismDatabase.item(null, C.class.getField("list").getType(), null));
    Assert.assertEquals("{type=array, items={type=number}}",
        "" + PolymorphismDatabase.item(null, C.class.getField("array").getType(), null));
  }

  @Test
  public void testPut() {
    PolymorphismDatabase.put(of("read", "only"), "key", (String) null);
  }

  @Test
  public void testRead() throws Exception {
    PolymorphismDatabase p = new PolymorphismDatabase();
    Table s = new Table();
    s.name = "t";
    Assert.assertNull(p.read(s, of("x", "y")));
    s.name = "dj-database";
    Object res = p.read(s, of("ID", "dj/config")).get("tables");
    Assert.assertNotNull(res);
  }

  @Test
  public void testQuery() throws Exception {
    PolymorphismDatabase p = new PolymorphismDatabase();
    QueryMeta s = new QueryMeta();
    s.query = "dj-database";
    Object res = p.queryMap(s, of("ID", "dj/config"));
    Assert.assertNotNull(res);
  }

  @JsonSchema(style = {"color", "red"})
  public Object dummy;

  @Test
  public void testClassInheritance() throws Exception {
    Map<String, Object> font = new HashMap<>();
    font.put("font", "courier");
    Map<String, Object> res = new HashMap<>();
    res.put("style", font);

    Map<String, Object> tmp = new HashMap<>();
    PolymorphismDatabase.put(tmp, getClass().getField("dummy").getAnnotation(JsonSchema.class));

    Assert.assertEquals("{style={font=courier, color=red}}",
        "" + UnionDatabase.mergeArray(res, tmp));
  }

  @JsonSchema(order = {"b"})
  public Object dummy2;

  @Test
  public void testClassInheritance2() throws Exception {
    Map<String, Object> res = new HashMap<>();
    res.put("order", new ArrayList<>(Arrays.asList("a")));

    Map<String, Object> tmp = new HashMap<>();
    PolymorphismDatabase.put(tmp, getClass().getField("dummy2").getAnnotation(JsonSchema.class));

    Assert.assertEquals("[a, b]", "" + UnionDatabase.mergeArray(res, tmp).get("order"));
  }
}
