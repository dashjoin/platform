package org.dashjoin.util;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.Data;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.util.OpenCypherQuery.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
    eq("MATCH (prj:`dj/junit/PRJ`)<-[rel]-(emp) RETURN emp.NAME");
    eq("MATCH (prj:`dj/junit/EMP`)-[]->(prj) RETURN prj.NAME");
    eq("MATCH p=(tom:Person {name: 'Tom Hanks'})-[directed:DIRECTED]->(movie:Movie) RETURN p");
    eq("MATCH (var:A1_B {key: 'value'}) RETURN var");
    eq("MATCH (var:A1_B) RETURN var");
    eq("MATCH (var:`dj/rdf4j/http:%2F%2Fex.org/urn:A`) RETURN var");
    eq("MATCH (nicole:Actor {name: $parameter})-[:ACTED_IN]->(movie:Movie) RETURN movie");
    eq("MATCH (john:Person {name: \"John\"})-[:friend *1..2]->(friend: Person) RETURN friend.name, friend.age");
    eq("MATCH (bob:User)-[:IN*0..]->(group)-[:AXO]->(res1)-[:HAS*0..]->(res2) RETURN count(*)");
  }

  void eq(String query) throws Exception {
    OpenCypherQuery q = new OpenCypherQuery(query, null);
    Assertions.assertEquals(query, q.toString());
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

  @Test
  public void testProject() throws Exception {
    OpenCypherQuery q = new OpenCypherQuery("MATCH (a:B) RETURN a, a.b, a.b.c", null);
    Assertions.assertEquals("{a=null, a.b=null, a.b.c=null}", "" + q.project(MapUtil.of()));
    Assertions.assertEquals("{a=1, a.b=null, a.b.c=null}", "" + q.project(MapUtil.of("a", 1)));
    Assertions.assertEquals("{a={b=1}, a.b=1, a.b.c=null}",
        "" + q.project(MapUtil.of("a", MapUtil.of("b", 1))));
    Assertions.assertEquals("{a={b={c=1}}, a.b={c=1}, a.b.c=1}",
        "" + q.project(MapUtil.of("a", MapUtil.of("b", MapUtil.of("c", 1)))));
  }

  void table(String s) {
    Table t = new Table(s, false);
    Assertions.assertEquals(s, t.toString());
  }

  void edge(String s) {
    Table t = new Table(s, true);
    Assertions.assertEquals(s, t.toString());
  }

  List<Map<String, Object>> run(String s) throws Exception {
    return run(s, null);
  }

  List<Map<String, Object>> run(String s, Map<String, Object> arguments) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    OpenCypherQuery q = new OpenCypherQuery(s, arguments);
    return objectMapper.convertValue(q.run(services, data, sc), JSONDatabase.trTable);
  }

  @Test
  public void testObjectsContainResource() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`) RETURN p");
    Assertions.assertEquals(2, res.size());
    Assertions.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + res.get(0).get("p"));
  }

  @Test
  public void testMatchEquality() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`{ID:1}) RETURN p");
    Assertions.assertEquals(1, res.size());
    res = run("MATCH (p:`dj/junit/EMP`{NAME:'mike'}) RETURN p");
    Assertions.assertEquals(1, res.size());
  }

  @Test
  public void testMatchEqualityParameter() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`{ID:${ID}}) RETURN p", MapUtil.of("ID", 1));
    Assertions.assertEquals(1, res.size());
    res = run("MATCH (p:`dj/junit/EMP`{NAME:${NAME}}) RETURN p", MapUtil.of("NAME", "mike"));
    Assertions.assertEquals(1, res.size());
  }

  @Test
  public void testProjectField() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`) RETURN p.NAME, p.WORKSON");
    Assertions.assertEquals("{p.NAME=mike, p.WORKSON=1000}", "" + res.get(0));
  }

  @Test
  public void testTraverse() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`)-[e:WORKSON]->(project) RETURN p, e, project");
    Assertions.assertEquals("{_dj_edge=WORKSON, _dj_outbound=true}", "" + res.get(0).get("e"));
    Assertions.assertEquals(
        "{ID=1000, NAME=dev-project, _dj_resource={database=junit, table=PRJ, pk=[1000]}}",
        "" + res.get(0).get("project"));
  }

  @Test
  public void testTraverseCheckType() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`)-[e:WORKSON]->(project:`dj/junit/EMP`) RETURN project");
    // no result due to type mismatch
    Assertions.assertEquals(0, res.size());
  }

  @Test
  public void testPath() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH path=(p:`dj/junit/EMP`)-[e:WORKSON]->(project) RETURN path");
    @SuppressWarnings("unchecked")
    Map<String, Object> x = (Map<String, Object>) res.get(0).get("path");
    Assertions.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + x.get("start"));
    Assertions.assertEquals(
        "[{edge={_dj_edge=WORKSON, _dj_outbound=true}, end={ID=1000, NAME=dev-project, _dj_resource={database=junit, table=PRJ, pk=[1000]}}}]",
        "" + x.get("steps"));
  }

  @Test
  public void testPathIn() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (prj:`dj/junit/PRJ`)<-[wo:`dj/junit/EMP/WORKSON`]-(emp) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=mike}, {emp.NAME=joe}]", "" + res);
  }

  @Test
  public void testPathInWhere() throws Exception {
    List<Map<String, Object>> res = run(
        "MATCH (prj:`dj/junit/PRJ`)<-[wo:`dj/junit/EMP/WORKSON`]-(emp {NAME:'joe'}) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=joe}]", "" + res);
    res =
        run("MATCH (prj:`dj/junit/PRJ`)<-[wo:`dj/junit/EMP/WORKSON`]-(emp {ID:2}) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=joe}]", "" + res);
  }

  @Test
  public void testPathInCheckType() throws Exception {
    List<Map<String, Object>> res = run(
        "MATCH (prj:`dj/junit/PRJ`)<-[wo:`dj/junit/EMP/WORKSON`]-(emp:`dj/junit/PRJ`) RETURN emp.NAME");
    // no result due to type mismatch
    Assertions.assertEquals(0, res.size());
  }


  // same tests as above but without a specific edge type


  @Test
  public void testTraverse2() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`)-[e]->(project) RETURN p, e, project");
    Assertions.assertEquals("{_dj_edge=WORKSON, _dj_outbound=true}", "" + res.get(0).get("e"));
    Assertions.assertEquals(
        "{ID=1000, NAME=dev-project, _dj_resource={database=junit, table=PRJ, pk=[1000]}}",
        "" + res.get(0).get("project"));
  }

  @Test
  public void testTraverseCheckType2() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:`dj/junit/EMP`)-[e]->(project:`dj/junit/EMP`) RETURN project");
    // no result due to type mismatch
    Assertions.assertEquals(0, res.size());
  }

  @Test
  public void testPath2() throws Exception {
    List<Map<String, Object>> res = run("MATCH path=(p:`dj/junit/EMP`)-[e]->(project) RETURN path");
    @SuppressWarnings("unchecked")
    Map<String, Object> x = (Map<String, Object>) res.get(0).get("path");
    Assertions.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + x.get("start"));
    Assertions.assertEquals(
        "[{edge={_dj_edge=WORKSON, _dj_outbound=true}, end={ID=1000, NAME=dev-project, _dj_resource={database=junit, table=PRJ, pk=[1000]}}}]",
        "" + x.get("steps"));
  }

  @Test
  public void testPathIn2() throws Exception {
    List<Map<String, Object>> res = run("MATCH (prj:`dj/junit/PRJ`)<-[wo]-(emp) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=mike}, {emp.NAME=joe}]", "" + res);
  }

  @Test
  public void testPathInWhere2() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (prj:`dj/junit/PRJ`)<-[wo]-(emp {NAME:'joe'}) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=joe}]", "" + res);
    res = run("MATCH (prj:`dj/junit/PRJ`)<-[wo]-(emp {ID:2}) RETURN emp.NAME");
    Assertions.assertEquals("[{emp.NAME=joe}]", "" + res);
  }

  @Test
  public void testPathInCheckType2() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (prj:`dj/junit/PRJ`)<-[wo]-(emp:`dj/junit/PRJ`) RETURN emp.NAME");
    // no result due to type mismatch
    Assertions.assertEquals(0, res.size());
  }
}
