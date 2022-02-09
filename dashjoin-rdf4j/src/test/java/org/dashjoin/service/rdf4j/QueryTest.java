package org.dashjoin.service.rdf4j;

import org.dashjoin.service.RDF4JEditorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryTest {

  @Test
  public void testFilterOrder() throws Exception {
    query(
        "select ?PRJ where { ?PRJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:PRJ> filter (( ?PRJ = <:1000> ) && ( ?PRJ = \"string\" )) }");
  }

  @Test
  public void testBound() throws Exception {
    query("select ?s where { ?s ?p ?o filter bound(?o) } order by ?s");
  }

  @Test
  public void testNotBound() throws Exception {
    query("select ?s where { ?s ?p ?o filter (!bound(?o)) } order by ?s");
  }

  @Test
  public void testRegex() throws Exception {
    query("select ?s where { ?s ?p ?o filter regex(?o, \"pattern\") } order by ?s");
  }

  @Test
  public void testParseSelectAllOrderBy() throws Exception {
    query("select ?s where { ?s ?p ?o } order by ?s");
  }

  @Test
  public void testOrderByCount() throws Exception {
    query(
        "select (count(?A) as ?C) where { ?A <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:T> . ?A <:B> ?B } group by ?B order by ?C");
  }

  @Test
  public void testParseSelectAll() throws Exception {
    query("select ?s where { ?s ?p ?o }");
  }

  @Test
  public void testDistinct() throws Exception {
    query("select distinct ?s where { ?s ?p ?o }");
  }

  @Test
  public void testCountDistinct() throws Exception {
    query("select (count(distinct ?s) as ?cd) where { ?s ?p ?o } group by ?o");
  }

  @Test
  public void testLimit() throws Exception {
    query("select distinct ?s where { ?s ?p ?o } limit 7");
    query("select ?s where { ?s ?p ?o } limit 7");
  }

  @Test
  public void testParseSelectAtype() throws Exception {
    query("select ?s where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:T> }");
  }

  @Test
  public void testParseSelectAtypeJoin() throws Exception {
    query(
        "select ?s where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:T> . ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:T> . ?s <http://www.w3.org/2000/01/rdf-schema#label> ?l }");
  }

  @Test
  public void testParseFilter() throws Exception {
    query(
        "select ?s where { ?s ?p ?o filter ( ?o < \"300\"^^<http://www.w3.org/2001/XMLSchema#integer> ) }");
  }

  @Test
  public void testParseFilterAnd() throws Exception {
    query(
        "select ?s where { ?s ?p ?o filter (( \"100\"^^<http://www.w3.org/2001/XMLSchema#integer> < ?o ) && ( ?o < \"300\"^^<http://www.w3.org/2001/XMLSchema#integer> )) }");
  }

  @Test
  public void testParseFilterAnd2() throws Exception {
    query(
        "select ?s where { ?s ?p ?o . ?s ?p2 ?o2 filter (( ?o2 != ?o ) && ( ?o < \"300\"^^<http://www.w3.org/2001/XMLSchema#integer> )) }");
  }

  @Test
  public void testCount() throws Exception {
    query("select (count(?s) as ?c) where { ?s ?p ?o }");
  }

  @Test
  public void testCountGroupby() throws Exception {
    query("select (count(?s) as ?c) where { ?s ?p ?o } group by ?o");
  }

  @Test
  public void testRename() throws Exception {
    Query q = new Query("select (count(?s) as ?c) where { ?s ?p ?o } group by ?o");
    q.rename("o", "tmp");
    Assertions.assertEquals("select (count(?s) as ?c) where { ?s ?p ?tmp } group by ?tmp",
        spaces(q.toString()));
    q.rename("tmp", "oo");
    Assertions.assertEquals("select (count(?s) as ?c) where { ?s ?p ?oo } group by ?oo",
        spaces(q.toString()));
    q.rename("c", "cc");
    Assertions.assertEquals("select (count(?s) as ?cc) where { ?s ?p ?oo } group by ?oo",
        spaces(q.toString()));
    q.rename("s", "ss");
    Assertions.assertEquals("select (count(?ss) as ?cc) where { ?ss ?p ?oo } group by ?oo",
        spaces(q.toString()));
  }

  @Test
  public void testJoin() throws Exception {
    Query q = new Query("select ?s where { ?s a <:T> }");
    q.join("s", ":C");
    Assertions.assertEquals(
        "select ?s ?C where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <:T> . ?s <:C> ?C }",
        spaces(q.toString()));
  }

  void query(String query) throws Exception {
    Assertions.assertEquals(query, spaces(new Query(query).toString()));
    new RDF4JEditorTest().eq(query, new Query(query).toString());
  }

  Object spaces(String string) {
    string = string.replaceAll("\n", " ");
    while (string.contains("  "))
      string = string.replaceAll("  ", " ");
    return string;
  }
}
