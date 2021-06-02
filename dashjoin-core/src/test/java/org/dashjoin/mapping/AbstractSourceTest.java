package org.dashjoin.mapping;

import static java.util.Arrays.asList;
import static org.dashjoin.util.MapUtil.of;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class AbstractSourceTest {

  List<Map<String, Object>> table = asList(of("x", 1), of("y", "string", "z", new Date()));

  @Test
  public void testCols() {
    Assert.assertEquals(asList("_dj_source", "x", "y", "z"), Provider.cols(table, true));
  }

  @Test
  public void testType() {
    Assert.assertEquals("date", Provider.type("z", table));
  }
}
