package org.dashjoin.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RDF4JTest extends DBTest {

  @Override
  protected String idQuery() {
    return "ID";
  }

  @Override
  protected Object toID(Object o) {
    if ("ID".equals(o))
      return o;
    if (o.equals("EMP"))
      return "http%3A%2F%2Fex.org%2FEMP";
    if (o.equals("PRJ"))
      return "http%3A%2F%2Fex.org%2FPRJ";
    if (o.equals("NOKEY"))
      return "http%3A%2F%2Fex.org%2FNOKEY";
    if (o.equals("T"))
      return "http%3A%2F%2Fex.org%2FT";
    if (o.equals("U"))
      return "http%3A%2F%2Fex.org%2FU";
    return "http://ex.org/" + o;
  }

  @Override
  @Test
  public void testIncoming() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testGetTables() throws Exception {
    Assert.assertTrue(db.tables().contains("dj/junit/http%3A%2F%2Fex.org%2FEMP"));
    Assert.assertTrue(db.tables().contains("dj/junit/http%3A%2F%2Fex.org%2FPRJ"));
    Assert.assertTrue(db.tables().contains("dj/junit/http%3A%2F%2Fex.org%2FNOKEY"));
    Assert.assertTrue(db.tables().contains("dj/junit/http%3A%2F%2Fex.org%2FT"));
    Assert.assertTrue(db.tables().contains("dj/junit/http%3A%2F%2Fex.org%2FU"));
  }

  @Override
  @Test
  public void testAllOffset() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testSearchQuery() throws Exception {
    // TODO: need to add search.json
  }

  @Override
  @Test
  public void testCRUD() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testCRUD2() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testSearch() throws Exception {
    // TODO
  }
}
