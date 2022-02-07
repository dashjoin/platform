package org.dashjoin.service;


import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.rdf4j.model.util.Values.iri;

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

  @Override
  @Test
  public void testPath() throws Exception {
	  
/*	  
 	{
			"ID": "intelligentGraph.PathQL1",
			"query": "getPaths?pathQL=(<http://ex.org/REPORTSTO>){1,2}",
			"type": "read",
			"roles": ["user"],
			"arguments" : {
    			"subject" : iri("http://ex.org/1"),
    			 "object" : null
  			}
	}
*/
	  
	  QueryMeta queryMeta =QueryMeta.ofQuery("getPaths?pathQL=(<http://ex.org/REPORTSTO>){1,2}");
	  queryMeta.ID= "testPath";
	  queryMeta.database = "dj/junit";
	  Map<String, Object>  arguments = new HashMap<>();
	  arguments.put("subject", iri("http://ex.org/1"));
	  arguments.put("object", null);
	  queryMeta.arguments=arguments;
	  
	  
	  AbstractDatabase database = services.getConfig().getDatabase( queryMeta.database);

	  List<Map<String, Object>> res = database.queryGraph(queryMeta, null);
	  
	  ObjectMapper mapper = new ObjectMapper();
	  String resJSON = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.convertValue(res, JsonNode.class));

	  String expectedJSON = Files.readString( Paths.get("./src/test/resources/results/", "junit", queryMeta.ID +".json"));

	  Assert.assertEquals( expectedJSON, resJSON);
  }
}
