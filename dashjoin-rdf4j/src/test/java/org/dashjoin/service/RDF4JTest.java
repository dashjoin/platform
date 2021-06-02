package org.dashjoin.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class RDF4JTest extends DBTest {

  @Override
  protected String idQuery() {
    return "ID";
  }

  @Override
  protected Object toID(Object o) {
    if ("ID".equals(o))
      return o;
    return "urn:" + o;
  }

  @Override
  @Test
  public void testIncoming() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testGetTables() throws Exception {
    Assert.assertTrue(db.tables().contains("dj/junit/urn:EMP"));
    Assert.assertTrue(db.tables().contains("dj/junit/urn:PRJ"));
    Assert.assertTrue(db.tables().contains("dj/junit/urn:NOKEY"));
    Assert.assertTrue(db.tables().contains("dj/junit/urn:T"));
    Assert.assertTrue(db.tables().contains("dj/junit/urn:U"));
  }
}
