package org.dashjoin.expression;

import static java.util.Arrays.asList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import org.dashjoin.function.Index;
import org.dashjoin.mapping.Mapping;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MappingTest {

  @Inject
  ExpressionService s;

  Mapping newMapping() {
    Mapping m = new Mapping();
    return m;
  }

  @BeforeAll
  public static void init() {
    Index.reset();
  }

  @Test
  public void testApply() throws Exception {
    Mapping m = newMapping();

    // rename y to x
    m.rowMapping = ImmutableMap.of("x", "y+1");

    Map<String, Object> res = m.apply(s, Mockito.mock(SecurityContext.class), MapUtil.of("y", 1));
    Assertions.assertEquals("{x=2}", res.toString());
  }

  @Test
  public void testConcat() throws Exception {
    Mapping m = newMapping();

    // rename y to x
    m.rowMapping = ImmutableMap.of("z", "z", "x", "y&z");

    Map<String, Object> res =
        m.apply(s, Mockito.mock(SecurityContext.class), MapUtil.of("y", 1, "z", "a"));
    Assertions.assertEquals("{z=a, x=1a}", res.toString());
  }

  @Test
  public void testMappings() throws Exception {
    Map<String, Mapping> mappings = new LinkedHashMap<>();
    mappings.put("t", newMapping());
    mappings.get("t").rowFilter = "age > 10";
    mappings.get("t").rowMapping = ImmutableMap.of("AGE", "age", "age", "age");

    Map<String, List<Map<String, Object>>> source = new LinkedHashMap<>();
    source.put("t", new ArrayList<>());
    source.get("t").add(MapUtil.of("age", 1));
    source.get("t").add(MapUtil.of("age", 22));

    // null mapping is noop (include all tables 1:1)
    Assertions.assertEquals("{t=[{age=1}, {age=22}]}",
        Mapping.apply(s, null, source, null).toString());

    Assertions.assertEquals("{t=[{AGE=22, age=22}]}",
        Mapping.apply(s, Mockito.mock(SecurityContext.class), source, mappings).toString());

    // null mapping is noop (include table 1:1)
    mappings.put("s", null);
    source.put("s", new ArrayList<>());
    source.get("s").add(MapUtil.of("x", 1));

    Assertions.assertEquals("{t=[{AGE=22, age=22}], s=[{x=1}]}",
        Mapping.apply(s, Mockito.mock(SecurityContext.class), source, mappings).toString());
  }

  @Test
  public void testExtract() throws Exception {
    Map<String, Mapping> mappings = new LinkedHashMap<>();
    mappings.put("t", newMapping());
    mappings.put("list", newMapping());
    mappings.get("list").extractKey = "id";
    mappings.get("list").extractColumn = "list";
    mappings.get("list").sourceTable = "t";

    Map<String, List<Map<String, Object>>> source = new LinkedHashMap<>();
    source.put("t", new ArrayList<>());
    source.get("t").add(ImmutableMap.of("id", "a", "list", asList(1, 2)));
    source.get("t").add(ImmutableMap.of("id", "b", "list", asList(3)));

    Assertions.assertEquals("[{parent_id=a, list=1}, {parent_id=a, list=2}, {parent_id=b, list=3}]",
        Mapping.apply(s, null, source, mappings).get("list").toString());
  }

  @Test
  public void testExtract2() throws Exception {
    Map<String, Mapping> mappings = new LinkedHashMap<>();
    mappings.put("t", newMapping());
    mappings.put("list", newMapping());
    mappings.get("list").extractKey = "id";
    mappings.get("list").extractColumn = "list";
    mappings.get("list").sourceTable = "t";

    Map<String, List<Map<String, Object>>> source = new LinkedHashMap<>();
    source.put("t", new ArrayList<>());
    source.get("t").add(ImmutableMap.of("id", "a", "list",
        asList(ImmutableMap.of("x", 1), ImmutableMap.of("y", 2))));
    source.get("t").add(ImmutableMap.of("id", "b"));
    source.get("t").add(ImmutableMap.of("id", "c", "list", asList(ImmutableMap.of("x", 1))));

    Assertions.assertEquals("[{parent_id=a, x=1}, {parent_id=a, y=2}, {parent_id=c, x=1}]",
        Mapping.apply(s, null, source, mappings).get("list").toString());
  }

  @Test
  public void testRowNumber() throws Exception {
    Map<String, Mapping> mappings = new LinkedHashMap<>();
    mappings.put("t", newMapping());
    mappings.get("t").pk = "id";
    mappings.get("t").rowMapping = ImmutableMap.of("a", "a", "rid", "$index()");

    Map<String, List<Map<String, Object>>> source = new LinkedHashMap<>();
    source.put("t", new ArrayList<>());
    source.get("t").add(ImmutableMap.of("a", 1));
    source.get("t").add(ImmutableMap.of("a", 2));

    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    // Reset index
    Index.reset();
    Assertions.assertEquals("[{a=1, rid=0}, {a=2, rid=1}]",
        Mapping.apply(s, sc, source, mappings).get("t").toString());
  }
}
