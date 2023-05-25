package org.dashjoin.service.rdf4j;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.SecurityContext;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.service.DBTest;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

@Disabled
@QuarkusTest
public class RDF4JPathQLTest extends DBTest {

  @Override
  @Test
  public void testQuery() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> res = db.query(sc, "junit", "list", null);
    Assertions.assertEquals(8, res.size());
    Assertions.assertEquals(toID(1), res.get(0).get(idQuery()));
  }

  @Override
  @Test
  public void testGraph() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> res = db.queryGraph(sc, "junit", "graph", null);
    Assertions.assertEquals(8, res.size());
    Assertions.assertEquals(toID(1), ((Map<?, ?>) res.get(0).get("x")).get(idRead()));
    Resource r = (Resource) ((Map<?, ?>) res.get(0).get("x")).get("_dj_resource");
    Assertions.assertEquals("junit", r.database);
    name("EMP", r.table);
    name("1", "" + r.pk.get(0));
  }

  @Override
  @Test
  public void testAll() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> x =
        db.all(sc, "junit", (String) toID("EMP"), 0, 10, null, false, null);
    map("{WORKSON=1000, ID=1, NAME=mike, REPORTSTO=6}", x.get(0));
    Assertions.assertEquals(9, x.size());
    x = db.getall(sc, "junit", (String) toID("EMP"), 0, 10, null, false, null);
    map("{WORKSON=1000, ID=1, NAME=mike, REPORTSTO=6}", x.get(0));
    Assertions.assertEquals(9, x.size());
  }

  @Override
  @Test
  public void testAllWhere() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> x = db.all(sc, "junit", (String) toID("EMP"), 0, 10, null, false,
        MapUtil.of((String) toID("NAME"), "mike"));
    map("{WORKSON=1000, ID=1, NAME=mike, REPORTSTO=6}", x.get(0));
    Assertions.assertEquals(1, x.size());
  }

  @Override
  @Test
  public void testList() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Map<String, Map<String, Object>> res =
        db.list(sc, "junit", (String) toID("EMP"), Arrays.asList((String) toID("1")));
    map("{WORKSON=1000, ID=1, NAME=mike, REPORTSTO=6}", res.get(toID("1")));
  }

  @Override
  @Test
  public void testAllOffset() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    db.all(sc, "junit", (String) toID("EMP"), 0, 1, null, false, null);
    List<Map<String, Object>> x =
        db.all(sc, "junit", (String) toID("EMP"), 1, 10, null, false, null);
    map("{ID=2, NAME=joe, WORKSON=1000, REPORTSTO=6}", x.get(0));
  }

  @Override
  @Test
  public void testPath() throws Exception {
    /* Mock QueryMeta */
    QueryMeta queryMeta = QueryMeta.ofQuery("getPaths?pathQL=(<http://ex.org/REPORTSTO>){1,2}");
    queryMeta.ID = "testPath";
    queryMeta.database = "dj/pathql";
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("subject", iri("http://ex.org/1"));
    arguments.put("object", null);
    queryMeta.arguments = arguments;

    AbstractDatabase database = services.getConfig().getDatabase(queryMeta.database);

    List<Map<String, Object>> res = database.queryGraph(queryMeta, null);

    // AbstractDatabase pathql = services.getConfig().getDatabase("dj/pathql");
    // Map<String, Object> res =
    // pathql.read(pathql.tables.get("http://ex.org/EMP"), MapUtil.of("ID", "http://ex.org/1"));

    ObjectMapper mapper = new ObjectMapper();
    String resJSON = mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(mapper.convertValue(res, JsonNode.class));

    String expectedJSON = Files
        .readString(Paths.get("./src/test/resources/results/", "junit", queryMeta.ID + ".json"));

    assertEquals(expectedJSON, resJSON);
  }
}
