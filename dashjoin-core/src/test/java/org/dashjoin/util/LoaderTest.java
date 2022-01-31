package org.dashjoin.util;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoaderTest {

  static ObjectMapper om = new ObjectMapper();

  @Test
  public void testOpen() throws Exception {
    om.readTree(Loader.open("/data/json.json"));
    om.readTree(Loader.open("src/test/resources/data/json.json"));
    om.readTree(Loader.open("file:src/test/resources/data/json.json"));
  }
}
