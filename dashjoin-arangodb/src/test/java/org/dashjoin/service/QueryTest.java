package org.dashjoin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryTest {

  @Test
  public void testAggregate() throws Exception {
    eq("FOR i IN col RETURN {\"x\": i.x}");
    eq("FOR i IN col LIMIT 1,2 RETURN {\"x\": i.x}");
    eq("FOR i IN col LIMIT 1 RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name LIMIT 1 RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name LIMIT 1 FILTER i.field==5 RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name DESC RETURN {\"x\": i.x}");
    eq("FOR i IN col RETURN {\"col\": i.col}");
    eq("FOR i IN col FILTER i.field==5 RETURN {\"x\": i.x}");
    eq("FOR i IN col FILTER i.field==\"5\" RETURN {\"x\": i.x}");
    eq("FOR i IN col FILTER i.field==\"so what\" RETURN {\"x\": i.x}");
    eq("FOR i IN col RETURN DISTINCT {\"x\": i.x}");
  }

  void eq(String query) throws Exception {
    ArangoDBQuery q = new ArangoDBQuery(query);
    Assertions.assertEquals(query.replaceAll(" ", "").replaceAll("\"", ""),
        q.toString().replaceAll("\\['emp'\\]", "emp").replaceAll("\\['orders'\\]", "orders")
            .replaceAll(" ", "").replaceAll("\"", ""));
  }
}
