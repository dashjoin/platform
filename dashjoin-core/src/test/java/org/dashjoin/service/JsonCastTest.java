package org.dashjoin.service;

import java.util.Arrays;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableMap;

public class JsonCastTest {

  AbstractDatabase db() {
    return new UnionDatabase();
  }

  Property p(String type, String dbType) {
    Property p = new Property();
    p.type = type;
    p.dbType = dbType;
    return p;
  }

  @Test
  public void testBoolean() throws Exception {
    Assertions.assertEquals(true, db().cast(p("boolean", "BIT"), "true"));
  }

  @Test
  public void testInt() throws Exception {
    Assertions.assertEquals(5, db().cast(p("integer", "INT"), "5"));

    // allow casting of .0 values to int
    Assertions.assertEquals(5, db().cast(p("integer", "INT"), "5.0"));
  }

  @Test
  public void testLong() throws Exception {
    Assertions.assertEquals(123, db().cast(p("number", "BIGINT"), "123"));
    Assertions.assertEquals(99999999999l, db().cast(p("number", "BIGINT"), "99999999999"));
  }

  @Test
  public void testFloat() throws Exception {
    // uses double at the moment
    Assertions.assertEquals(3.14d, db().cast(p("number", "FLOAT"), "3.14"));
  }

  @Test
  public void testDouble() throws Exception {
    Assertions.assertEquals(Double.MAX_VALUE,
        db().cast(p("number", "DOUBLE"), "" + Double.MAX_VALUE));
  }

  @Test
  public void testArray() throws Exception {
    Property p = p("array", null);
    p.items = p("string", null);
    Assertions.assertEquals(Arrays.asList("a"), db().cast(p, "a"));
    Assertions.assertEquals(Arrays.asList("a", "b"), db().cast(p, "a,b"));
    Assertions.assertEquals(Arrays.asList("a", "b"), db().cast(p, "[\"a\", \"b\"]"));
    p.items.type = "integer";
    Assertions.assertEquals(Arrays.asList(1), db().cast(p, "1"));
    Assertions.assertEquals(Arrays.asList(1, 2), db().cast(p, "[1,2]"));
    Assertions.assertEquals(Arrays.asList(1, 2), db().cast(p, "1,2"));
  }

  @Test
  public void testObject() throws Exception {
    Property p = p("object", null);
    p.properties = ImmutableMap.of("name", p("string", null));
    Assertions.assertEquals(MapUtil.of("name", "test"), db().cast(p, "{\"name\": \"test\"}"));
  }
}
