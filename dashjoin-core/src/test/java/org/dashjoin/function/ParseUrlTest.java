package org.dashjoin.function;

import org.dashjoin.function.ParseUrl.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParseUrlTest {

  @Test
  public void testAnchor() {
    Result res = new ParseUrl().run("http://example.org/x#anchor");
    Assertions.assertEquals("anchor", res.ref);
  }
}
