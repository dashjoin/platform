package org.dashjoin.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FunctionServiceTest {

  @Test
  public void testConfig() {
    Map<String, Object> map = new LinkedHashMap<>();
    List<String> path = new ArrayList<>(Arrays.asList("email", "properties", "prop"));
    new FunctionService().add(map, path, 123);
    Assertions.assertEquals("{email={properties={prop=123}}}", map.toString());
  }
}
