package org.dashjoin.function;

import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class Excel2dataTest {

  @Test
  public void xlsx() throws Exception {
    Excel2data f = new Excel2data();
    @SuppressWarnings("unchecked")
    Map<String, List<Map<String, Object>>> res = (Map<String, List<Map<String, Object>>>) f
        .parse(new URL("file:src/test/resources/data/import.xlsx"));
    Assertions.assertEquals("mike", res.get("Tabelle1").get(1).get("name"));
  }
}
