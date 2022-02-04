package org.dashjoin.service;


import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.inova8.intelligentgraph.vocabulary.PATHQL;

import io.quarkus.test.junit.QuarkusTest;

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
			"query": "getPaths?pathQL=(<http://ex.org/WORKSON>|^<http://ex.org/WORKSON>){0,3}",
			"type": "read",
			"roles": ["user"],
			"arguments" : {
    			"subject" : iri("http://ex.org/1"),
    			 "object" : null
  			}
	}
*/
	  
	  
	  QueryMeta queryMeta =QueryMeta.ofQuery("getPaths?pathQL=(<http://ex.org/WORKSON>|^<http://ex.org/WORKSON>){1,3}");
	  queryMeta.ID= "intelligentGraph.PathQL1";
	  queryMeta.database = "dj/junit";
	  Map<String, Object>  arguments = new HashMap<>();
	  arguments.put("subject", iri("http://ex.org/1"));
	  arguments.put("object", null);
	  queryMeta.arguments=arguments;
	  
	  
	  AbstractDatabase database = services.getConfig().getDatabase( queryMeta.database);

	  List<Map<String, Object>> res = database.queryGraph(queryMeta, null);
	  
//	    SecurityContext sc = Mockito.mock(SecurityContext.class);
//	    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
//	    List<Map<String, Object>> res = db.queryGraph(sc, "junit", "path", null);
//	    Assert.assertEquals(2, res.size());
//	    Map<String, Object> first = getMap(res.get(0), "path");
//	    Assert.assertTrue(getMap(first, "start").containsKey("_dj_resource"));
//	    List<Map<String, Object>> steps = getList(first, "steps");
//	    Assert.assertEquals(1, steps.size());
//	    Assert.assertEquals("{_dj_edge=WORKSON, _dj_outbound=true}", "" + steps.get(0).get("edge"));
//	    Assert.assertTrue(getMap(steps.get(0), "end").containsKey("_dj_resource"));
  }
}
