package org.dashjoin.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QueryTest {

  @Test
  public void testAggregate() throws Exception {
    eq("FOR i IN col RETURN {\"x\": i.x}");
    eq("FOR i IN col LIMIT 1,2 RETURN {\"x\": i.x}");
    eq("FOR i IN col LIMIT 1 RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name LIMIT 1 RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name RETURN {\"x\": i.x}");
    eq("FOR i IN col SORT i.name DESC RETURN {\"x\": i.x}");
    eq("FOR i IN col RETURN {\"col\": i.col}");
    eq("FOR i IN col FILTER field==5 RETURN {\"x\": i.x}");
    eq("FOR i IN col FILTER field==\"5\" RETURN {\"x\": i.x}");
    eq("FOR i IN col FILTER field==\"so what\" RETURN {\"x\": i.x}");
  }

  void eq(String query) {
    ArangoDBQuery q = new ArangoDBQuery(query);
    Assert.assertEquals(query.replaceAll(" ", "").replaceAll("\"", ""),
        q.toString().replaceAll("\\['emp'\\]", "emp").replaceAll("\\['orders'\\]", "orders")
            .replaceAll(" ", "").replaceAll("\"", ""));
  }

  @Test
  public void testBetween() {
    Assert.assertEquals(" 1,2Aa ",
        ArangoDBQuery.between(".. LIMIT 1,2Aa RETURN ..", "limit", "return"));
  }
}
