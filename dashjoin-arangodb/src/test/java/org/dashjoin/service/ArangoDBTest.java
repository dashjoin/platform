package org.dashjoin.service;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ArangoDBTest extends DBTest {

  @Override
  protected Object toID(Object o) {
    if (o instanceof Integer)
      return "" + o;
    return o;
  }

  @Override
  protected Object thousand() {
    return 1000l;
  }

  @Override
  protected String idRead() {
    return "_key";
  }

  @Override
  protected String idQuery() {
    return "_key";
  }
}
