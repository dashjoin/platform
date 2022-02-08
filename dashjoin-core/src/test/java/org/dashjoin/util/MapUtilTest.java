package org.dashjoin.util;

import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapUtilTest {

  @Test
  public void testClean() {
    Map<String, Object> map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of());
    map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of(), "e", Arrays.asList());
    map = MapUtil.of("a", 1, "b", 2, "c", 3, "d", MapUtil.of(), "e", Arrays.asList(), "f", "");
    MapUtil.clean(map);
    Assertions.assertNull(map.get("d"));
    Assertions.assertNull(map.get("e"));
    Assertions.assertNull(map.get("f"));
  }
}
