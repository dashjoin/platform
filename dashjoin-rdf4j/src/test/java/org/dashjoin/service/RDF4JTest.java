package org.dashjoin.service;

import org.dashjoin.model.Table;
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
      return "http://ex.org/EMP";
    if (o.equals("PRJ"))
      return "http://ex.org/PRJ";
    if (o.equals("NOKEY"))
      return "http://ex.org/NOKEY";
    if (o.equals("T"))
      return "http://ex.org/T";
    if (o.equals("U"))
      return "http://ex.org/U";
    return "http://ex.org/" + o;
  }

  @Override
  @Test
  public void testMetadata() throws Exception {
    String ns = "http:%2F%2Fex.org%2F";
    Table t = services.getConfig().getDatabase("dj/junit").tables.get("http://ex.org/EMP");
    Assert.assertEquals(idRead(), t.properties.get(idRead()).name);
    Assert.assertEquals(0, t.properties.get(idRead()).pkpos.intValue());
    Assert.assertNull(t.properties.get(idRead()).ref);
    Assert.assertEquals("dj/junit/" + ns + "EMP/" + idRead(), t.properties.get(idRead()).ID);

    Assert.assertEquals("http://ex.org/WORKSON", t.properties.get("http://ex.org/WORKSON").name);
    Assert.assertNull(t.properties.get("http://ex.org/WORKSON").pkpos);
    Assert.assertEquals("dj/junit/" + ns + "PRJ/" + idRead(),
        t.properties.get("http://ex.org/WORKSON").ref);
    Assert.assertEquals("dj/junit/" + ns + "EMP/" + ns + "WORKSON",
        t.properties.get("http://ex.org/WORKSON").ID);
  }

  @Override
  @Test
  public void testGetTables() throws Exception {
    Assert.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FEMP"));
    Assert.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FPRJ"));
    Assert.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FNOKEY"));
    Assert.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FT"));
    Assert.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FU"));
  }
}
