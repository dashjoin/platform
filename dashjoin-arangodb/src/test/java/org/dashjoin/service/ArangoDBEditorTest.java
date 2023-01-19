package org.dashjoin.service;

import static org.dashjoin.service.QueryEditor.Col.col;
import java.util.Arrays;
import org.dashjoin.service.QueryEditor.ColCondition;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@Disabled("Requires a local ArangoDB instance running")
@QuarkusTest
public class ArangoDBEditorTest extends QueryEditorTest {

  @Override
  protected QueryEditorInternal getQueryEditor() {
    try {
      ArangoDB db = (ArangoDB) services.getConfig().getDatabase("dj/junit");
      return new ArangoDBEditor(services, db);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Test
  public void testRename() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRenameRenamed() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRenameAggr() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRenameRenamedAggr() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRenameNoop() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testAddColumn() throws Exception {
    String sql = "FOR i IN T RETURN {\"A\": i.A}";
    // add a column from the same table
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("T", "A"), col("T", "B")));
    eq("FOR i IN T RETURN {\"A\": i.A, \"B\": i.B}", res.query);
  }

  @Override
  @Test
  public void testGetIDsOfClassQuery() throws Exception {
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/EMP";
    Assertions.assertEquals("FOR i IN EMP RETURN {\"_key\": i._key}", e.getInitialQuery(r).query);
  }

  @Override
  @Test
  public void testAddColumnJoin() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testAddColumnJoin2() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testAddColumnJoin3() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testAddColumnJoin4() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testOrderBy() throws Exception {
    SortRequest r = json(
        "{'order':'asc', 'query':'FOR i IN EMP RETURN {\\\"NAME\\\": i.NAME}', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("FOR i IN EMP SORT i.A RETURN {\"NAME\": i.NAME}", e.sort(r).query);
  }

  @Override
  @Test
  public void testAddWhere() throws Exception {
    SetWhereRequest r = new SetWhereRequest();
    r.cols = Arrays.asList(new ColCondition());
    r.cols.get(0).col = col("EMP", "NAME");
    r.cols.get(0).condition = "='x'";
    r.query = "FOR i IN EMP RETURN { \"NAME\": i.NAME }";
    eq("FOR i IN EMP FILTER i.NAME==\"x\" RETURN {\"NAME\": i.NAME}", e.setWhere(r).query);
  }

  @Override
  @Test
  public void testGroupBy() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testMetadata() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.database = "db";
    QueryResponse res;

    query.query = "FOR i IN EMP RETURN {\"_id\": i._id}";
    res = e.noop(query);
    Assertions.assertEquals("EMP._id", res.metadata.get(0).col.toString());
    Assertions.assertEquals("_id", res.fieldNames.get(0));
  }

  @Override
  @Test
  public void testAssertions() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRemove() throws Exception {
    RemoveColumnRequest ac = new RemoveColumnRequest();
    ac.database = "db";
    ac.col = col("T", "A");

    ac.query = "FOR i IN EMP RETURN {\"_key\": i._key, \"A\": i.A}";
    eq("FOR i IN EMP RETURN {\"_key\": i._key}", e.removeColumn(ac).query);
  }

  @Override
  @Test
  public void testMove() throws Exception {
    MoveColumnRequest ac = new MoveColumnRequest();
    ac.database = "db";
    ac.col = col("EMP", "A");
    ac.position = 0;

    ac.query = "FOR i IN EMP RETURN {\"_key\": i._key, \"A\": i.A}";
    eq("FOR i IN EMP RETURN {\"A\": i.A, \"_key\": i._key}", e.moveColumn(ac).query);
  }

  @Override
  @Test
  public void testDistinct() throws Exception {
    DistinctRequest ac = new DistinctRequest();
    ac.database = "db";
    ac.querylimit = 88;
    ac.distinct = true;

    ac.query = "FOR i IN EMP RETURN {\"_key\": i._key}";
    eq("FOR i IN EMP LIMIT 88 RETURN DISTINCT {\"_key\": i._key}", e.distinct(ac).query);
  }

  @Override
  public void eq(String expected, String actual) throws Exception {
    Assertions.assertEquals(expected, actual);
  }
}
