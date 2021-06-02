package org.dashjoin.service;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MongoDBTest extends DBTest {

  @Override
  protected String idRead() {
    return "_id";
  }

  @Override
  protected String idQuery() {
    return "_id";
  }
}
