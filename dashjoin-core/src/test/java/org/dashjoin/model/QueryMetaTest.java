package org.dashjoin.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryMetaTest {

  @Test
  public void testOfName() {
    Assertions.assertEquals("select", QueryMeta.ofQuery("select").query);
  }
}
