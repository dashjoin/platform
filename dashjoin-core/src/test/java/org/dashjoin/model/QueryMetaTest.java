package org.dashjoin.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class QueryMetaTest {

  @Test
  public void testOfName() {
    Assert.assertEquals("select", QueryMeta.ofQuery("select").query);
  }
}
