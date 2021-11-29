package org.dashjoin.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QueryTest {

  @Test
  public void testAggregate() throws Exception {
    eq("FOR i IN col RETURN i");
    eq("FOR i IN col RETURN {\"col\": i.col}");
    eq("FOR i IN col FILTER field==5 RETURN i");
    eq("FOR i IN col FILTER field==\"5\" RETURN i");
    eq("FOR i IN col FILTER field==\"so what\" RETURN i");
  }

  void eq(String query) {
    ArangoDBQuery q = new ArangoDBQuery(query);
    Assert.assertEquals(query.replaceAll(" ", "").replaceAll("\"", ""),
        q.toString().replaceAll("\\['emp'\\]", "emp").replaceAll("\\['orders'\\]", "orders")
            .replaceAll(" ", "").replaceAll("\"", ""));
  }
}
