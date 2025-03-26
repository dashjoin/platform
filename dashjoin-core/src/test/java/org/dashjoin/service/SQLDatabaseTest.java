package org.dashjoin.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Aggregation;
import org.dashjoin.service.Data.ColInfo;
import org.dashjoin.service.Data.Filter;
import org.dashjoin.service.Database.QueryAndParams;
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

  ColInfo alias(String alias, String name, Filter filter, Object arg1, Aggregation aggregation) {
    ColInfo c = col(name, filter, arg1, aggregation);
    c.alias = alias;
    return c;
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
  public void testAnalyticsDate() throws Exception {
    Assertions.assertEquals(
        "select \"HIRE_DATE\" from \"EMP\" where \"HIRE_DATE\" = ${_dj_0} limit 1000",
        db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("HIRE_DATE", Filter.EQUALS, new Date(0), null))).query);
  }

  List<Map<String, Object>> go(QueryAndParams qp) throws Exception {
    return db().query(QueryMeta.ofQuery(qp.query), qp.params);
  }

  @Test
  public void testAnalytics() throws Exception {
    // project
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]", "" + go(
        db().analytics(null, Table.ofName("EMP"), Arrays.asList(col("ID", null, null, null)))));
    // filter
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]", "" + go(db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("ID", Filter.IN, Arrays.asList(1, 2, 3), null)))));
    Assertions.assertEquals("[{EMP.ID=1}]", "" + go(db().analytics(null, Table.ofName("EMP"),
        Arrays.asList(col("ID", Filter.EQUALS, 1, null)))));
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]", "" + go(db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("ID", Filter.GREATER_EQUAL, 1, null)))));
    Assertions.assertEquals("[{EMP.ID=1}]", "" + go(db().analytics(null, Table.ofName("EMP"),
        Arrays.asList(col("ID", Filter.SMALLER_EQUAL, 1, null)))));
    Assertions.assertEquals("[{EMP.ID=2}]", "" + go(db().analytics(null, Table.ofName("EMP"),
        Arrays.asList(col("ID", Filter.NOT_EQUALS, 1, null)))));
    Assertions.assertEquals("[{EMP.NAME=mike}]", "" + go(db().analytics(null, Table.ofName("EMP"),
        Arrays.asList(col("NAME", Filter.LIKE, "%IK%", null)))));
    Assertions.assertEquals("[{EMP.ID=1}]", "" + go(
        db().analytics(null, Table.ofName("EMP"), Arrays.asList(col("ID", Filter.LIKE, 1, null)))));
    Assertions.assertEquals("[{EMP.ID=1}, {EMP.ID=2}]", "" + go(db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("ID", Filter.IS_NOT_NULL, 1, null)))));
    Assertions.assertEquals("[]", "" + go(db().analytics(null, Table.ofName("EMP"),
        Arrays.asList(col("ID", Filter.IS_NULL, 1, null)))));
    // aggregation
    Assertions.assertEquals("[{COUNT(ID)=2, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.COUNT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{AVG(ID)=1.5, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.AVG),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{SUM(ID)=3, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.SUM),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{MIN(ID)=1, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.MIN),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{MAX(ID)=2, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.MAX),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{COUNT(DISTINCT ID)=2, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.COUNT_DISTINCT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{X=1,2, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(alias("X", "ID", null, null, Aggregation.GROUP_CONCAT_DISTINCT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{STDDEV_SAMP(ID)=0.7071067811865476, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", null, null, Aggregation.STDDEV),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    Assertions.assertEquals("[{Y=1,2, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(alias("Y", "ID", null, null, Aggregation.GROUP_CONCAT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
    // filter and aggregation
    Assertions.assertEquals("[{COUNT(ID)=1, EMP.WORKSON=1000}]",
        "" + go(db().analytics(null, Table.ofName("EMP"),
            Arrays.asList(col("ID", Filter.EQUALS, 1, Aggregation.COUNT),
                col("WORKSON", null, null, Aggregation.GROUP_BY)))));
  }

  @Test
  public void testAnalyticsWhereNull() throws Exception {
    for (Filter f : Filter.values()) {
      if (!f.toString().contains("NULL"))
        Assertions.assertFalse(
            db().analytics(null, Table.ofName("EMP"), Arrays.asList(col("ID", f, null, null))).query
                .contains("where"));
    }
  }

  @Test
  public void testInject() throws Exception {
    Assertions.assertThrows(IllegalArgumentException.class, () -> db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("te//st", null, null, null))));
    Assertions.assertThrows(IllegalArgumentException.class, () -> db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("te()st", null, null, null))));
    Assertions.assertThrows(IllegalArgumentException.class, () -> db().analytics(null,
        Table.ofName("EMP"), Arrays.asList(col("äää", null, null, null))));
    db().analytics(null, Table.ofName("EMP"), Arrays.asList(col("_a A 1", null, null, null)));
  }
}
