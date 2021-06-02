package org.dashjoin.util;

import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class MapUtilTest {

  @Test
  public void testClean() {
    Map<String, Object> map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of());
    map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of(), "e", Arrays.asList());
    map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of(), "e", Arrays.asList(), "f", "");
    MapUtil.clean(map);
    Assert.assertNull(map.get("d"));
    Assert.assertNull(map.get("e"));
    Assert.assertNull(map.get("f"));
  }
}
