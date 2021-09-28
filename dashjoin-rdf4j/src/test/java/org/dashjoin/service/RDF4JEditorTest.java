package org.dashjoin.service;

import static org.dashjoin.service.QueryEditor.Col.col;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class RDF4JEditorTest extends QueryEditorTest {

  @Override
  protected QueryEditorInternal getQueryEditor() {
    try {
      services.persistantDB = new JSONFileDatabase();
      services.readonlyDB = new JSONClassloaderDatabase();

      RDF4J db = services.getConfig().getCachedForce("dj/junit", RDF4J.class);
      return new RDF4JEditor(services, db);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Test
  public void testGetIDsOfClassQuery() throws Exception {
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/urn:EMP";
    Assert.assertEquals("select ?EMP where { ?EMP a <urn:EMP> }", e.getInitialQuery(r).query);
  }

  // T(ID, FK)
  // U(ID, C)

  @Override
  @Test
  public void testAddColumn() throws Exception {
    String sql = "SELECT ?A WHERE { ?A a <:T> }";
    // add a column from the same table
    QueryResponse res = e.addColumn(addColumnRequest(sql, col(null, "A"), col(null, ":B")));
    eq("SELECT ?A ?B WHERE { ?A a <:T> . ?A <:B> ?B }", res.query);
  }

  @Override
  @Test
  public void testAddColumnJoin() throws Exception {
    // makes no sense in SPARQL (PK = FK not required)
  }

  @Override
  @Test
  public void testAddColumnJoin2() throws Exception {
    String sql = "SELECT ?A ?FK WHERE { ?A a <:T> . ?A <:FK> ?FK }";
    // FK -> C
    QueryResponse res = e.addColumn(addColumnRequest(sql, col(null, "FK"), col(null, ":C")));
    eq("SELECT ?A ?FK ?C WHERE { ?A a <:T> . ?A <:FK> ?FK . ?FK <:C> ?C }", res.query);
  }

  @Override
  @Test
  public void testAddColumnJoin3() throws Exception {
    // makes no sense in SPARQL (PK = FK not required)
  }

  @Override
  @Test
  public void testAddColumnJoin4() throws Exception {
    // not supported in SPARQL - needs to be 2 operations, join FK, then target column
  }

  @Override
  @Test
  public void testAssertions() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testMetadata() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.database = "db";
    QueryResponse res;

    query.query = "select ?EMP where { ?EMP a <:EMP> }";
    res = e.noop(query);
    Assert.assertEquals("null.EMP", res.metadata.get(0).col.toString());
    Assert.assertEquals("EMP", res.fieldNames.get(0));

    query.query = "select (COUNT(?EMP)as ?count) where { ?EMP a <:EMP> }";
    res = e.noop(query);
    Assert.assertEquals("null.count", res.metadata.get(0).col.toString());
    Assert.assertEquals("count", res.fieldNames.get(0));

    query.query =
        "select (count(?emp) as ?w) ?workson where { ?emp a <:EMP> . ?emp <:WORKSON> ?workson filter (?workson = <:1000>) } group by ?workson";
    res = e.noop(query);
    Assert.assertEquals("= <:1000>", res.metadata.get(1).where);
  }

  @Override
  @Test
  public void testRenameAggr() throws Exception {
    RenameRequest r = json(
        "{'query':'SELECT (COUNT(?A) as ?N) where {?A a <:T> . ?A <:B> ?B } GROUP BY ?B', 'col':{'table':'T', 'column':'N'}, 'name':'N2'}",
        RenameRequest.class);
    eq("SELECT (COUNT(?A) as ?N2) where {?A a <:T> . ?A <:B> ?B } GROUP BY ?B", e.rename(r).query);
  }

  @Override
  @Test
  public void testMove() throws Exception {

    MoveColumnRequest ac = new MoveColumnRequest();
    ac.database = "db";
    ac.position = 1;
    ac.col = col("T", "A");

    ac.query = "SELECT ?A ?B where { ?A a <:T> . ?A <:B> ?B }";
    eq("SELECT ?B ?A where { ?A a <:T> . ?A <:B> ?B }", e.moveColumn(ac).query);

    ac.col = col("T", "T");
    ac.query = "SELECT (COUNT(?A) as ?T) ?B where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B";
    eq("SELECT ?B (COUNT(?A) as ?T) where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B",
        e.moveColumn(ac).query);
  }

  @Override
  @Test
  public void testOrderBy() throws Exception {
    SortRequest r = json(
        "{'order':'asc', 'query':'SELECT ?A where { ?T a <:T> }', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT ?A where { ?T a <:T> } ORDER BY ?A", e.sort(r).query);

    r = json(
        "{'order':'asc', 'query':'SELECT ?A where { ?A a <:T> } ORDER BY DESC(?A)', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT ?A where { ?A a <:T> } ORDER BY ?A", e.sort(r).query);

    r = json(
        "{'order':'asc', 'query':'SELECT ?A where { ?A a <:T> } ORDER BY ?A', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT ?A where { ?A a <:T> } ORDER BY ?A", e.sort(r).query);

    r = json(
        "{'order':'desc', 'query':'SELECT ?A where { ?A a <:T> } ORDER BY ?A', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT ?A where { ?A a <:T> } ORDER BY DESC(?A)", e.sort(r).query);

    r = json(
        "{'order':'', 'query':'SELECT ?A where { ?A a <:T> } ORDER BY ?A', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT ?A where { ?A a <:T> }", e.sort(r).query);

    r = json(
        "{'query':'SELECT (count(?A) as ?C) where { ?A a <:T> } ORDER BY ?C', 'col':{'table':'T', 'column':'C'}}",
        SortRequest.class);
    eq("SELECT (count(?A) as ?C) where { ?A a <:T> }", e.sort(r).query);
  }

  @Override
  @Test
  public void testRemove() throws Exception {

    RemoveColumnRequest ac = new RemoveColumnRequest();
    ac.database = "db";
    ac.col = col("T", "A");

    ac.query = "SELECT ?A ?B where { ?A a <:T> . ?A <:B> ?B }";
    eq("SELECT ?B where { ?A a <:T> . ?A <:B> ?B }", e.removeColumn(ac).query);

    ac.col = col("T", "T");
    ac.query = "SELECT (COUNT(?A) as ?T) ?B where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B";
    eq("SELECT ?B where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B", e.removeColumn(ac).query);
  }

  @Override
  @Test
  public void testDistinct() throws Exception {
    DistinctRequest r =
        json("{'query':'SELECT ?A where { ?A a <:T> }', 'distinct':true}", DistinctRequest.class);
    eq("SELECT DISTINCT ?A where { ?A a <:T> }", e.distinct(r).query);

    r = json("{'query':'SELECT DISTINCT ?A where { ?A a <:T> }', 'distinct':false}",
        DistinctRequest.class);
    eq("SELECT ?A where { ?A a <:T> }", e.distinct(r).query);

    r = json(
        "{'query':'SELECT DISTINCT ?A where { ?A a <:T> }', 'distinct':false, 'querylimit':1234}",
        DistinctRequest.class);
    eq("SELECT ?A where { ?A a <:T> } LIMIT 1234", e.distinct(r).query);

    r = json("{'query':'SELECT DISTINCT ?A where { ?A a <:T> } LIMIT 1234', 'distinct':false}",
        DistinctRequest.class);
    eq("SELECT ?A where { ?A a <:T> }", e.distinct(r).query);

    r = json(
        "{'query':'SELECT (COUNT(?A) as ?C) where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B ORDER BY ?C', 'distinct':true}",
        DistinctRequest.class);
    eq("SELECT (COUNT(DISTINCT ?A) as ?C) where { ?A a <:T> . ?A <:B> ?B } GROUP BY ?B ORDER BY ?C",
        e.distinct(r).query);
  }

  @Override
  @Test
  public void testRenameRenamed() throws Exception {
    // not applicable
  }

  @Override
  @Test
  public void testGroupBy() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRenameRenamedAggr() throws Exception {
    // not applicable
  }

  @Override
  @Test
  public void testAddWhere() throws Exception {

    SetWhereRequest r = json(
        "{'query':'SELECT ?A where {?A a <:T>}', 'cols':[{'col':{'table':'T', 'column':'A'}, 'condition':'IS NOT NULL'}]}",
        SetWhereRequest.class);
    eq("SELECT ?A where {?A a <:T> FILTER ( bound(?A) )}", e.setWhere(r).query);
  }

  @Override
  @Test
  public void testRename() throws Exception {
    RenameRequest r =
        json("{'query':'SELECT ?A WHERE {?A a <:T>}', 'col':{'column':'A'}, 'name':'N'}",
            RenameRequest.class);
    eq("SELECT ?N WHERE {?N a <:T>}", e.rename(r).query);

    // set name to "" (remove rename) not supported in SPARQL

    // aggregation must have an alias

    r = json(
        "{'query':'SELECT (COUNT(?A) AS ?N) WHERE { ?A a <:T> }', 'col':{'table':'T', 'column':'N'}, 'name':'C'}",
        RenameRequest.class);
    eq("SELECT (COUNT(?A) AS ?C) WHERE { ?A a <:T> }", e.rename(r).query);
  }

  @Override
  @Test
  public void testRenameNoop() throws Exception {
    RenameRequest r = json(
        "{'query':'SELECT ?A WHERE { ?T a <:T> }', 'col':{'table':'T', 'column':'A'}, 'name':'A'}",
        RenameRequest.class);
    eq("SELECT ?A WHERE { ?T a <:T> }", e.rename(r).query);
  }

  @Override
  public void eq(String expected, String actual) throws Exception {
    SPARQLQueryRenderer r = new SPARQLQueryRenderer();
    Assert.assertEquals(
        r.render(QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, expected, null)),
        r.render(QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, actual, null)));
  }

  @Override
  @Test
  public void testDelegate() throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    QueryEditor.Delegate d = new QueryEditor.Delegate();
    d.services = services;
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/urn:EMP";
    QueryResponse q = d.getInitialQuery(sc, r);
    QueryDatabase query = new QueryDatabase();
    query.database = "dj/junit";
    query.query = q.query;
    d.noop(sc, query);
  }
}
