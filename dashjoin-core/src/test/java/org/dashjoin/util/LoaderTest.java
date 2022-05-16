package org.dashjoin.util;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class LoaderTest {

  static ObjectMapper om = new ObjectMapper();

  @Test
  public void testOpen() throws Exception {
    om.readTree(Loader.open("/data/json.json"));

    // only upload folder is allowed
    Assertions.assertThrows(RuntimeException.class, () -> {
      om.readTree(Loader.open("upload/../src/test/resources/data/json.json"));
    });
    // upload/src is not present
    Assertions.assertThrows(IOException.class, () -> {
      om.readTree(Loader.open("upload/src/test/resources/data/json.json"));
    });
  }

  // @Test
  public void testUrl() throws Exception {
    Loader.open("https://github.com/dashjoin/dbpedia/blob/main/dbpedia.n3");
  }
}
