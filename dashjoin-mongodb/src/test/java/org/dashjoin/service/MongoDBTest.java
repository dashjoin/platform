package org.dashjoin.service;

import org.junit.jupiter.api.Test;
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

  @Override
  @Test
  public void testSearchQuery() throws Exception {
    // TODO: need to add search.json
  }
}
