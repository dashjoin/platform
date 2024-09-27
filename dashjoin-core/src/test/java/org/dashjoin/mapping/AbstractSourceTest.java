package org.dashjoin.mapping;

import static java.util.Arrays.asList;
import static org.dashjoin.util.MapUtil.of;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractSourceTest {

  List<Map<String, Object>> table = asList(of("x", 1), of("y", "string", "z", new Date()));

  @Test
  public void testCols() {
    Assertions.assertEquals(asList("_dj_source", "x", "y", "z"), Provider.cols(table, true));
  }

  @Test
  public void testType() {
    Assertions.assertEquals("date", Provider.type("z", table));
  }

  @Test
  public void testMixed() {
    Assertions.assertEquals("string", Provider.type("x", asList(of("x", 1), of("x", "string"))));
  }

  @Test
  public void testIntNumber() {
    Assertions.assertEquals("integer", Provider.type("x", asList(of("x", 1))));
    Assertions.assertEquals("integer", Provider.type("x", asList(of("x", 1), of("x", 1))));
  }

  @Test
  public void testNumber() {
    Assertions.assertEquals("number", Provider.type("x", asList(of("x", 1.2))));
    Assertions.assertEquals("number", Provider.type("x", asList(of("x", 1), of("x", 1.2))));
  }

  @Test
  public void testNull() {
    Assertions.assertEquals("string", Provider.type("x", asList(of("x", null), of("x", null))));
  }
}
