package org.dashjoin.service;

import javax.inject.Inject;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import io.quarkus.test.junit.QuarkusTest;

/**
 * some basic tests for SQL Database
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class SQLDatabaseTest extends AbstractDatabaseTest {

  @Inject
  Services services;

  @Override
  String getQuery() {
    return "select * from \"EMP\"";
  }

  @Override
  String getQuery2() {
    return "select * from \"EMP\" where \"ID\"=1";
  }

  @Override
  Database db() throws Exception {
    services.getConfig().metadataCollection();
    return services.getConfig().getDatabase("dj/junit");
  }
}
