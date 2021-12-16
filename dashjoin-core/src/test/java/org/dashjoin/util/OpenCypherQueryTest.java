package org.dashjoin.util;

import org.dashjoin.util.OpenCypherQuery.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class OpenCypherQueryTest {

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
}
