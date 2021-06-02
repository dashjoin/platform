package org.dashjoin.service;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QueryTest {

  @Test
  public void testAggregate() throws Exception {
    eq("db.orders.aggregate([])");
    eq("db.orders.aggregate([{ $match: { status: \"A\" } }])");
    eq("db.orders.aggregate([{ $match: { status: \"A\" } }, { $group: { _id: \"$cust_id\", total: { $sum: \"$amount\" } } }])");
    eq("db.orders.aggregate([{ $match: { status: \"A\" } }, { $group: { _id: \"$cust_id\", total: { $sum: \"$amount\" } } }, { $sort: { total: -1 } }])");
    eq("db.orders.aggregate([{ $match: { status: \"A\" } }, { $group: { _id: \"$cust_id\", total: { $sum: \"$amount\" } } }, { $sort: { total: -1 } }, { $limit: 2 }])");
    eq("db.orders.aggregate([{ $match: { status: \"A\" } }, { $group: { _id: \"$cust_id\", total: { $sum: \"$amount\" } } }, { $project: { _id: 1 } }, { $sort: { total: -1 } }, { $limit: 2 }])");
    eq("db.orders.aggregate([{$lookup:{from:\"emp\", localField:\"manager\", foreignField:\"_id\", as:\"join\"}}, { $match: { status: \"A\" } }, { $group: { _id: \"$cust_id\", total: { $sum: \"$amount\" } } }, { $project: { _id: 1 } }, { $sort: { total: -1 } }, { $limit: 2 }])");
  }

  void eq(String query) {
    MongoDBQuery q = new MongoDBQuery(query);
    Assert.assertEquals(query.replaceAll(" ", "").replaceAll("\"", ""),
        q.toString().replaceAll("\\['emp'\\]", "emp").replaceAll("\\['orders'\\]", "orders")
            .replaceAll(" ", "").replaceAll("\"", ""));
  }
}

