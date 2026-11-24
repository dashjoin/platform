package org.dashjoin.util.gql;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.dashjoin.service.Data;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

/**
 * GQL over the junit database (sql/junit.sql): EMP 1, 2 -WORKSON-> PRJ 1000; NODE 2, 3 -REF-> 1,
 * 4 -REF-> 3; ARR 1 -KIDS-> 2
 */
@QuarkusTest
public class GqlTest {

  @Inject
  Services services;

  @Inject
  Data data;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static final String WORKSON =
      "_dj_edge=dj/junit/EMP/WORKSON, _dj_outbound=";

  /* =============================================================================== parser */

  void same(String query) {
    Assertions.assertEquals(query, GqlQuery.parse(query, null).toString());
  }

  void same(String query, String canonical) {
    Assertions.assertEquals(canonical, GqlQuery.parse(query, null).toString());
  }

  void parseError(String query, String message) {
    Exception e =
        Assertions.assertThrows(IllegalArgumentException.class, () -> GqlQuery.parse(query, null));
    Assertions.assertTrue(e.getMessage().contains(message), e.getMessage());
  }

  @Test
  public void testParse() {
    same("MATCH (prj:`dj/junit/PRJ`)<-[rel]-(emp) RETURN emp.NAME");
    same("MATCH (a:EMP {ID: 1, NAME: 'mike'})->(b)<-(c)-(d) RETURN a");
    same("MATCH p = (a:T)->(b) RETURN p");
    same("MATCH p = ANY SHORTEST (a:EMP {ID: 1})-[e:WORKSON|REF]-{1,6}(b:EMP {ID: 2}) RETURN p");
    same("MATCH ALL SHORTEST ACYCLIC (a)->+(b:T) WHERE (a.X > 1 AND b.Y IS NOT NULL) RETURN DISTINCT a, b.NAME AS name LIMIT 5");
    same("MATCH SHORTEST 3 TRAIL (a:T)-[:FK]->{2}(b) RETURN a");
    same("MATCH ANY 2 (a:T)<-[:`dj/junit/T/FK`]-{0,3}(b) RETURN a");
    same("MATCH TRAIL (a:T)-[:FK]->*(b) RETURN a, PATH_LENGTH(p)");
    same("MATCH (a:T WHERE a.ID IN [1, 2]) RETURN a");
    same("MATCH (a:T) WHERE NOT a.NAME = 'x' RETURN a");
  }

  @Test
  public void testParseVariants() {
    same("MATCH ( prj : `dj/junit/PRJ` ) <- [ rel ] - (emp) RETURN emp.NAME",
        "MATCH (prj:`dj/junit/PRJ`)<-[rel]-(emp) RETURN emp.NAME");
    same("match (a is EMP) return a", "MATCH (a:EMP) RETURN a");
    same("MATCH ANY SHORTEST PATHS ACYCLIC PATH (a:T)-->{1,}(b) RETURN a",
        "MATCH ANY SHORTEST ACYCLIC (a:T)->+(b) RETURN a");
    // Cypher style abbreviations, either direction
    same("MATCH (a:T)-->(b)<--(c)--(d)<->(e) RETURN a", "MATCH (a:T)->(b)<-(c)-(d)-(e) RETURN a");
    same("MATCH (a:T) WHERE a.X != \"it's\" RETURN a",
        "MATCH (a:T) WHERE a.X <> 'it\\'s' RETURN a");
    Assertions.assertEquals("MATCH (a:T {ID: 7}) RETURN a",
        GqlQuery.parse("MATCH (a:T {ID: $id}) RETURN a", MapUtil.of("id", 7)).toString());
    Assertions.assertEquals("MATCH (a:T {ID: 7}) RETURN a",
        GqlQuery.parse("MATCH (a:T {ID: ${id}}) RETURN a", MapUtil.of("id", 7)).toString());
  }

  @Test
  public void testParseErrors() {
    parseError("MATCH (a)-[:R*1..2]->(b) RETURN a", "-[:REL]->{1,5}");
    parseError("MATCH (a:T)-[:R]->+(b) RETURN a", "unbounded quantifier");
    parseError("MATCH (a:T {ID: $x}) RETURN a", "no value for parameter $x");
    parseError("MATCH (a:T)-[:R {X: 1}]->(b) RETURN a", "no properties");
    parseError("MATCH (a:T), (b:T) RETURN a", "only one path pattern");
    parseError("MATCH (a:T)-->(a) RETURN a", "used twice");
    parseError("MATCH (a:T)-->{3,1}(b) RETURN a", "smaller than the minimum");
    parseError("MATCH (a:T) RETURN", "expected a value or variable at the end");
    parseError("MATCH (a:T)\nRETURN a b", "GQL 2:10");
    // reserved words are variables only in backticks
    parseError("MATCH path = (a:T) RETURN path", "found 'path'");
    same("MATCH `path` = (a:T) RETURN `path`");
  }

  /* ============================================================================== queries */

  List<Map<String, Object>> run(String s) throws Exception {
    return run(s, null, null);
  }

  List<Map<String, Object>> run(String s, Map<String, Object> arguments,
      Consumer<GqlEngine> config) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    GqlEngine engine = new GqlEngine(services, data, sc);
    if (config != null)
      config.accept(engine);
    return objectMapper.convertValue(engine.run(GqlQuery.parse(s, arguments)),
        JSONDatabase.trTable);
  }

  void eq(String expected, String query) throws Exception {
    Assertions.assertEquals(expected, "" + run(query));
  }

  void error(String query, String message, Consumer<GqlEngine> config) {
    Exception e = Assertions.assertThrows(Exception.class, () -> run(query, null, config));
    Assertions.assertTrue(e.getMessage().contains(message), e.getMessage());
  }

  @Test
  public void testNodes() throws Exception {
    List<Map<String, Object>> res = run("MATCH (p:`dj/junit/EMP`) RETURN p");
    Assertions.assertEquals(2, res.size());
    Assertions.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + res.get(0).get("p"));
    eq("[{p.NAME=mike, p.WORKSON=1000}, {p.NAME=joe, p.WORKSON=1000}]",
        "MATCH (p:EMP) RETURN p.NAME, p.WORKSON");
  }

  @Test
  public void testProperties() throws Exception {
    eq("[{p.NAME=mike}]", "MATCH (p:EMP {ID: 1}) RETURN p.NAME");
    eq("[{p.ID=2}]", "MATCH (p:EMP {NAME: 'joe'}) RETURN p.ID");
    Assertions.assertEquals("[{p.NAME=mike}]",
        "" + run("MATCH (p:EMP {ID: $ID}) RETURN p.NAME", MapUtil.of("ID", 1), null));
    Assertions.assertEquals("[{p.ID=2}]",
        "" + run("MATCH (p:EMP {NAME: '${NAME}'}) RETURN p.ID", MapUtil.of("NAME", "joe"), null));
  }

  @Test
  public void testWhere() throws Exception {
    eq("[{a.ID=2}]", "MATCH (a:EMP) WHERE a.NAME = 'joe' OR a.ID > 5 RETURN a.ID");
    eq("[{a.ID=2}]", "MATCH (a:EMP WHERE a.ID <> 1) RETURN a.ID");
    eq("[{a.ID=1}, {a.ID=2}]", "MATCH (a:EMP) WHERE a.ID IN [1, 2] RETURN a.ID");
    eq("[{x=mike}]", "MATCH (a:EMP)-->(p:PRJ) WHERE a.ID < 2 RETURN a.NAME AS x");
  }

  @Test
  public void testOutgoing() throws Exception {
    List<Map<String, Object>> res =
        run("MATCH (p:EMP)-[e:WORKSON]->(project) RETURN p, e, project");
    Assertions.assertEquals(2, res.size());
    Assertions.assertEquals("{" + WORKSON + "true}", "" + res.get(0).get("e"));
    Assertions.assertEquals(
        "{ID=1000, NAME=dev-project, BUDGET=null, _dj_resource={database=junit, table=PRJ, pk=[1000]}}",
        "" + res.get(0).get("project"));
    eq("[{x.ID=1000}]", "MATCH (e:EMP {ID: 1})-->(x) RETURN x.ID");
  }

  @Test
  public void testIncoming() throws Exception {
    eq("[{emp.NAME=mike}, {emp.NAME=joe}]",
        "MATCH (prj:PRJ)<-[:WORKSON]-(emp) RETURN emp.NAME");
    // the same relationship by its ID
    eq("[{emp.NAME=mike}, {emp.NAME=joe}]",
        "MATCH (prj:PRJ)<-[:`dj/junit/EMP/WORKSON`]-(emp) RETURN emp.NAME");
    Assertions.assertEquals("{" + WORKSON + "false}",
        "" + run("MATCH (prj:PRJ)<-[e]-(emp {ID: 2}) RETURN e").get(0).get("e"));
    // direction matters: nothing points to an employee via WORKSON
    eq("[]", "MATCH (e:EMP)<-[:WORKSON]-(p) RETURN p");
    eq("[]", "MATCH (p:PRJ)-[:WORKSON]->(e) RETURN e");
  }

  @Test
  public void testEitherDirection() throws Exception {
    eq("[{e.ID=1}, {e.ID=2}]", "MATCH (p:PRJ {ID: 1000})--(e) RETURN e.ID");
    eq("[{x.ID=1000}]", "MATCH (e:EMP {ID: 1})-[:WORKSON]-(x) RETURN x.ID");
  }

  @Test
  public void testLabels() throws Exception {
    eq("[]", "MATCH (p:EMP)-[:WORKSON]->(project:EMP) RETURN project");
    eq("[]", "MATCH (prj:PRJ)<-[:WORKSON]-(emp:PRJ) RETURN emp.NAME");
    eq("[{x.ID=1000}, {x.ID=1000}]", "MATCH (e:EMP)-->(x:PRJ|NODE) RETURN x.ID");
    error("MATCH (a:NOPE) RETURN a", "Unknown table: NOPE", null);
    error("MATCH (a:EMP)-[:NOPE]->(b) RETURN a", "Unknown relationship", null);
    error("MATCH (a)-->(b) RETURN a", "needs a label", null);
  }

  @Test
  public void testPath() throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, Object> path = (Map<String, Object>) run(
        "MATCH p = (x:`dj/junit/EMP`)-[e:WORKSON]->(project) RETURN p").get(0).get("p");
    Assertions.assertEquals(
        "{ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}",
        "" + path.get("start"));
    Assertions.assertEquals("[{edge={" + WORKSON
        + "true}, end={ID=1000, NAME=dev-project, BUDGET=null, _dj_resource={database=junit, table=PRJ, pk=[1000]}}}]",
        "" + path.get("steps"));
  }

  @Test
  public void testQuantifiers() throws Exception {
    eq("[{to.ID=1}]", "MATCH (from:NODE {ID: 4})-[:REF]->{2}(to) RETURN to.ID");
    eq("[{to.ID=4}]", "MATCH (from:NODE {ID: 1})<-[:REF]-{2}(to) RETURN to.ID");
    eq("[{to.ID=4}, {to.ID=3}, {to.ID=1}]",
        "MATCH (from:NODE {ID: 4})-[:REF]->{0,2}(to) RETURN to.ID");
    eq("[{b.ID=3}, {b.ID=1}]", "MATCH TRAIL (a:NODE {ID: 4})-[:REF]->+(b) RETURN b.ID");
    // a quantified edge pattern binds the list of its edges
    List<Map<String, Object>> res = run("MATCH (a:NODE {ID: 4})-[e:REF]->{2}(b) RETURN e");
    Assertions.assertEquals(2, ((List<?>) res.get(0).get("e")).size());
  }

  @Test
  public void testStartAtTheEnd() throws Exception {
    // only the last node is known: the search starts there, the result reads as written
    eq("[{a.ID=2, b.ID=1}, {a.ID=3, b.ID=1}]",
        "MATCH (a)-[:REF]->(b:NODE {ID: 1}) RETURN a.ID, b.ID");
    @SuppressWarnings("unchecked")
    Map<String, Object> path = (Map<String, Object>) run(
        "MATCH p = (a)-[:REF]->{2}(b:NODE {ID: 1}) RETURN p").get(0).get("p");
    Assertions.assertEquals(4, ((Map<?, ?>) path.get("start")).get("ID"));
    Assertions.assertEquals("[{edge={_dj_edge=dj/junit/NODE/REF, _dj_outbound=true}, end={ID=3, REF=1, _dj_resource={database=junit, table=NODE, pk=[3]}}}, "
        + "{edge={_dj_edge=dj/junit/NODE/REF, _dj_outbound=true}, end={ID=1, REF=null, _dj_resource={database=junit, table=NODE, pk=[1]}}}]",
        "" + path.get("steps"));
  }

  @Test
  public void testArrayForeignKey() throws Exception {
    eq("[{from.ID=1, to.ID=2}]", "MATCH (from:ARR)-->(to) RETURN from.ID, to.ID");
    eq("[{from.ID=2, to.ID=1}]", "MATCH (from:ARR)<--(to) RETURN from.ID, to.ID");
    eq("[{from.ID=1, to.ID=2}, {from.ID=2, to.ID=1}]",
        "MATCH (from:ARR)--(to) RETURN from.ID, to.ID");
  }

  @Test
  public void testDistinctLimit() throws Exception {
    eq("[{p.ID=1000}]", "MATCH (a:EMP)-->(p) RETURN DISTINCT p.ID");
    eq("[{a.ID=1}]", "MATCH (a:EMP) RETURN a.ID LIMIT 1");
  }

  /* ========================================================================= path search */

  static final String CONNECTION = "{start={ID=1, NAME=mike, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[1]}}, steps=["
      + "{edge={" + WORKSON
      + "true}, end={ID=1000, NAME=dev-project, BUDGET=null, _dj_resource={database=junit, table=PRJ, pk=[1000]}}}, "
      + "{edge={" + WORKSON
      + "false}, end={ID=2, NAME=joe, WORKSON=1000, _dj_resource={database=junit, table=EMP, pk=[2]}}}]}";

  @Test
  public void testConnection() throws Exception {
    // both ends given: searched from both sides; the record in between is no employee
    eq("[{p=" + CONNECTION + "}]",
        "MATCH p = ANY SHORTEST (a:EMP {ID: 1})-{1,4}(b:EMP {ID: 2}) RETURN p");
    eq("[{p=" + CONNECTION + "}]",
        "MATCH p = ALL SHORTEST (a:EMP {ID: 1})-[:WORKSON]-{1,4}(b:EMP {ID: 2}) RETURN p");
    eq("[{p=" + CONNECTION + "}]",
        "MATCH p = ANY SHORTEST (a:EMP {ID: 1})--+(b:EMP {ID: 2}) RETURN p");
    // the same from one side (the end is not given by its properties)
    eq("[{p=" + CONNECTION + "}]",
        "MATCH p = ANY SHORTEST (a:EMP {ID: 1})--{1,4}(b:EMP) WHERE b.ID = 2 RETURN p");
    // too short
    eq("[]", "MATCH p = ANY SHORTEST (a:EMP {ID: 1})--{1}(b:EMP {ID: 2}) RETURN p");
  }

  @Test
  public void testConnectionWhitelist() throws Exception {
    // only the listed relationships connect
    eq("[]", "MATCH p = ANY SHORTEST (a:EMP {ID: 1})-[:REF|KIDS]-{1,4}(b:EMP {ID: 2}) RETURN p");
    eq("[]", "MATCH p = ANY SHORTEST (a:EMP {ID: 1})-[:REF]-{1,4}(b:EMP) WHERE b.ID = 2 RETURN p");
  }

  @Test
  public void testShortestK() throws Exception {
    // 4 - 3 - 1 - 2, and as a walk also 4 - 3 - 4 - 3 - 1 - 2
    eq("[{len=3}, {len=5}]",
        "MATCH p = SHORTEST 2 (a:NODE {ID: 4})-[:REF]-{1,6}(b:NODE {ID: 2}) RETURN PATH_LENGTH(p) AS len");
    eq("[{len=3}]",
        "MATCH p = SHORTEST 2 ACYCLIC (a:NODE {ID: 4})-[:REF]-{1,6}(b:NODE {ID: 2}) RETURN PATH_LENGTH(p) AS len");
    // per end record: the shortest path to each node
    eq("[{b.ID=4, len=0}, {b.ID=3, len=1}, {b.ID=1, len=2}, {b.ID=2, len=3}]",
        "MATCH p = ANY SHORTEST (a:NODE {ID: 4})-[:REF]-*(b:NODE) RETURN b.ID, PATH_LENGTH(p) AS len");
  }

  @Test
  public void testLimits() {
    error("MATCH p = ANY SHORTEST (a:PRJ {ID: 1000})-{1,4}(b:EMP {ID: 2}) RETURN p",
        "has more than 1 links via dj/junit/EMP/WORKSON", e -> e.maxFanOut = 1);
    error("MATCH (a:EMP)-->(b) RETURN b", "reached more than 1 records", e -> e.maxRecords = 1);
  }
}
