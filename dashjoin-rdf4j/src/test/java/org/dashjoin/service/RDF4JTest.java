package org.dashjoin.service;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
    return "http://ex.org/" + o;
  }

  @Override
  protected Object thousand() {
    return toID(1000);
  }

  @Override
  @Test
  public void testMetadata() throws Exception {
    String ns = "http:%2F%2Fex.org%2F";
    Table t = services.getConfig().getDatabase("dj/junit").tables.get("http://ex.org/EMP");
    Assertions.assertEquals(idRead(), t.properties.get(idRead()).name);
    Assertions.assertEquals(0, t.properties.get(idRead()).pkpos.intValue());
    Assertions.assertNull(t.properties.get(idRead()).ref);
    Assertions.assertEquals("dj/junit/" + ns + "EMP/" + idRead(), t.properties.get(idRead()).ID);

    Assertions.assertEquals("http://ex.org/WORKSON",
        t.properties.get("http://ex.org/WORKSON").name);
    Assertions.assertNull(t.properties.get("http://ex.org/WORKSON").pkpos);
    Assertions.assertEquals("dj/junit/" + ns + "PRJ/" + idRead(),
        t.properties.get("http://ex.org/WORKSON").ref);
    Assertions.assertEquals("dj/junit/" + ns + "EMP/" + ns + "WORKSON",
        t.properties.get("http://ex.org/WORKSON").ID);
  }

  @Override
  @Test
  public void testGetTables() throws Exception {
    Assertions.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FEMP"));
    Assertions.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FPRJ"));
    Assertions.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FNOKEY"));
    Assertions.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FT"));
    Assertions.assertTrue(db.tables().contains("dj/junit/http:%2F%2Fex.org%2FU"));
  }

  @Disabled
  @Override
  @Test
  public void testPath() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    // need a non null starting point
    List<Map<String, Object>> res =
        // db.queryGraph(sc, "junit", "path", MapUtil.of("subject", "http://ex.org/1"));
        db.queryGraph(sc, "junit", "path", null);
    // we have one path
    Assertions.assertEquals(1, res.size());
    Map<String, Object> first = getMap(res.get(0), "path");
    Assertions.assertTrue(getMap(first, "start").containsKey("_dj_resource"));
    List<Map<String, Object>> steps = getList(first, "steps");
    Assertions.assertEquals(1, steps.size());
    // WORKSON -> http://ex.org/WORKSON
    Assertions.assertEquals("{_dj_edge=http://ex.org/WORKSON, _dj_outbound=true}",
        "" + steps.get(0).get("edge"));
    Assertions.assertTrue(getMap(steps.get(0), "end").containsKey("_dj_resource"));
  }

  // @Override
  // @Test
  // public void testPath() throws Exception {
  //
  /// *
  // {
  // "ID": "intelligentGraph.PathQL1",
  // "query": "getPaths?pathQL=(<http://ex.org/REPORTSTO>){1,2}",
  // "type": "read",
  // "roles": ["user"],
  // "arguments" : {
  // "subject" : iri("http://ex.org/1"),
  // "object" : null
  // }
  // }
  // */
  //
  // QueryMeta queryMeta =QueryMeta.ofQuery("getPaths?pathQL=(<http://ex.org/REPORTSTO>){1,2}");
  // queryMeta.ID= "testPath";
  // queryMeta.database = "dj/junit";
  // Map<String, Object> arguments = new HashMap<>();
  // arguments.put("subject", iri("http://ex.org/1"));
  // arguments.put("object", null);
  // queryMeta.arguments=arguments;
  //
  //
  // AbstractDatabase database = services.getConfig().getDatabase( queryMeta.database);
  //
  // List<Map<String, Object>> res = database.queryGraph(queryMeta, null);
  //
  // ObjectMapper mapper = new ObjectMapper();
  // String resJSON =
  // mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.convertValue(res,
  // JsonNode.class));
  //
  // String expectedJSON = Files.readString( Paths.get("./src/test/resources/results/", "junit",
  // queryMeta.ID +".json"));
  //
  // assertEquals( expectedJSON, resJSON);
  // }

  @Override
  @Test
  public void testIncomingAcl() throws Exception {
    // class level ACLs not supported
  }
}
