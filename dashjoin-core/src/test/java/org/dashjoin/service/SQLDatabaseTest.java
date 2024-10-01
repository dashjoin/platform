package org.dashjoin.service;

import java.util.Arrays;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Aggregation;
import org.dashjoin.service.Data.ColInfo;
import org.dashjoin.service.Data.Filter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

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

  ColInfo col(String name, Filter filter, Object arg1, Aggregation aggregation) {
    ColInfo col = new ColInfo();
    col.name = name;
    col.project = true;
    col.filter = filter;
    col.arg1 = arg1;
    col.aggregation = aggregation;
    return col;
  }

  @Test
  public void testAnalytics() throws Exception {
    // project
    Assertions
        .assertEquals("[{EMP.ID=1}, {EMP.ID=2}]",
            "" + db().query(QueryMeta.ofQuery(
                db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", null, null, null)))),
                null));
    // filter
    Assertions.assertEquals("[{EMP.ID=1}]",
        "" + db().query(QueryMeta.ofQuery(
            db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", Filter.EQUALS, 1, null)))),
            null));
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]",
        "" + db().query(QueryMeta.ofQuery(db().analytics(Table.ofName("EMP"),
            Arrays.asList(col("ID", Filter.GREATER_EQUAL, 1, null)))), null));
    Assertions.assertEquals("[{EMP.ID=1}]",
        "" + db().query(QueryMeta.ofQuery(db().analytics(Table.ofName("EMP"),
            Arrays.asList(col("ID", Filter.SMALLER_EQUAL, 1, null)))), null));
    Assertions.assertEquals("[{EMP.ID=2}]", "" + db().query(QueryMeta.ofQuery(
        db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", Filter.NOT_EQUALS, 1, null)))),
        null));
    Assertions.assertEquals("[{EMP.NAME=mike}]", "" + db().query(QueryMeta.ofQuery(
        db().analytics(Table.ofName("EMP"), Arrays.asList(col("NAME", Filter.LIKE, "%IK%", null)))),
        null));
    Assertions.assertEquals("[{EMP.ID=1}]",
        "" + db().query(QueryMeta.ofQuery(
            db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", Filter.LIKE, 1, null)))),
            null));
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]", "" + db().query(QueryMeta.ofQuery(
        db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", Filter.IS_NOT_NULL, 1, null)))),
        null));
    Assertions.assertEquals("[]",
        "" + db().query(QueryMeta.ofQuery(
            db().analytics(Table.ofName("EMP"), Arrays.asList(col("ID", Filter.IS_NULL, 1, null)))),
            null));
    // aggregation
    Assertions.assertEquals("[{COUNT(ID)=2, EMP.WORKSON=1000}]",
        "" + db().query(QueryMeta.ofQuery(db().analytics(Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.COUNT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))),
            null));
  }
}
