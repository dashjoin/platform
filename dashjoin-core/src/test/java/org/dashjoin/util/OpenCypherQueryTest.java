package org.dashjoin.util;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.Data;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.util.OpenCypherQuery.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class OpenCypherQueryTest {

  @Inject
  Services services;

  @Inject
  Data data;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testAggregate() throws Exception {
    eq("MATCH p=(tom:Person {name: 'Tom Hanks'})-[directed:DIRECTED]->(movie:Movie) RETURN p");
    eq("MATCH (var:A1_B {key: 'value'}) RETURN var");
    eq("MATCH (var:A1_B) RETURN var");
    eq("MATCH (var:`dj/rdf4j/http:%2F%2Fex.org/urn:A`) RETURN var");
    eq("MATCH (nicole:Actor {name: $parameter})-[:ACTED_IN]->(movie:Movie) RETURN movie");
    eq("MATCH (john:Person {name: \"John\"})-[:friend *1..2]->(friend: Person) RETURN friend.name, friend.age");
    eq("MATCH (bob:User)-[:IN*0..]->(group)-[:AXO]->(res1)-[:HAS*0..]->(res2) RETURN count(*)");
  }

  void eq(String query) throws Exception {
    OpenCypherQuery q = new OpenCypherQuery(query);
    Assert.assertEquals(query, q.toString());
  }

  @Test
  public void testTable() {
    table("(a)");
    table("(a:b)");
    table("(a:`b/c`)");
    table("(a:b {key: value})");
  }

  @Test
  public void testEdge() {
    edge("[*]");
    edge("[a]");
    edge("[*..5]");
    edge("[*3..]");
    edge("[*3..5]");
    edge("[a:`b/c` *3..5]");
  }

  void table(String s) {
    Table t = new Table(s, false);
    Assert.assertEquals(s, t.toString());
  }

  void edge(String s) {
    Table t = new Table(s, true);
    Assert.assertEquals(s, t.toString());
  }

  List<Map<String, Object>> run(String s) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    OpenCypherQuery q = new OpenCypherQuery(s);
    return objectMapper.convertValue(q.run(services, data, sc, null), JSONDatabase.trTable);
  }

  @Test
  public void testObjectsContainResource() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`) RETURN p");
    Assert.assertEquals(2, res.size());
    Assert.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + res.get(0).get("p"));
  }

  @Test
  public void testProjectField() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`) RETURN p.NAME, p.WORKSON");
    Assert.assertEquals("{p.NAME=mike, p.WORKSON=1000}", "" + res.get(0));
  }

  @Test
  public void testTraverse() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`)-[e:WORKSON]->(project) RETURN p, e, project");
    Assert.assertEquals("{_dj_edge=WORKSON, _dj_outbound=true}", "" + res.get(0).get("e"));
    Assert.assertEquals("{ID=1000, NAME=dev-project}", "" + res.get(0).get("project"));
  }

  @Test
  public void testPath() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH path=(p:`dj/junit/EMP`)-[e:WORKSON]->(project) RETURN path");
    System.out.println(res);
    @SuppressWarnings("unchecked")
    Map<String, Object> x = (Map<String, Object>) res.get(0).get("path");
    Assert.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + x.get("start"));
    Assert.assertEquals(
        "[{edge={_dj_edge=WORKSON, _dj_outbound=true}, end={ID=1000, NAME=dev-project}}]",
        "" + x.get("steps"));
  }
}
