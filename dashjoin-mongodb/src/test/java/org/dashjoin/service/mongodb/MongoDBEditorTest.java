package org.dashjoin.service.mongodb;

import static org.dashjoin.service.QueryEditor.Col.col;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.dashjoin.service.QueryEditorInternal;
import org.dashjoin.service.QueryEditorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MongoDBEditorTest extends QueryEditorTest {

  @Override
  protected QueryEditorInternal getQueryEditor() {
    try {
      MongoDB db = (MongoDB) services.getConfig().getDatabase("dj/junit");
      return new MongoDBEditor(services, db);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Test
  public void testRename() throws Exception {
    RenameRequest r = json("{'col':{'table':'T', 'column':'A'}, 'name':'N'}", RenameRequest.class);
    r.query = "db.T.aggregate([{$project:{A:\"$A\"}}])";
    eq("db.['T'].aggregate([{\"$project\": {\"N\": \"$A\"}}])", e.rename(r).query);
  }

  @Override
  @Test
  public void testRenameRenamed() throws Exception {
    RenameRequest r = json("{'col':{'table':'T', 'column':'N'}, 'name':'A'}", RenameRequest.class);
    r.query = "db.T.aggregate([{$project:{N:\"$A\"}}])";
    eq("db.['T'].aggregate([{\"$project\": {\"A\": \"$A\"}}])", e.rename(r).query);
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
    RenameRequest r = json("{'col':{'table':'T', 'column':'A'}, 'name':'A'}", RenameRequest.class);
    r.query = "db.T.aggregate([{$project:{A:\"$A\"}}])";
    eq("db.['T'].aggregate([{\"$project\": {\"A\": \"$A\"}}])", e.rename(r).query);
  }

  @Override
  @Test
  public void testAddColumn() throws Exception {
    String sql = "db.T.aggregate([{$project:{A:'$A'}}])";
    // add a column from the same table
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("T", "A"), col("T", "B")));
    eq("db.['T'].aggregate([{\"$project\": {\"A\": \"$A\", \"B\": \"$B\"}}])", res.query);
  }

  @Override
  @Test
  public void testGetIDsOfClassQuery() throws Exception {
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/EMP";
    Assertions.assertEquals("db.['EMP'].aggregate([{$project:{_id: \"$_id\"}}])",
        e.getInitialQuery(r).query);
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
    SortRequest r =
        json("{'order':'asc', 'query':'db.EMP.aggregate()', 'col':{'table':'T', 'column':'A'}}",
            SortRequest.class);
    eq("db.['EMP'].aggregate([{\"$sort\": {\"A\": 1}}])", e.sort(r).query);
  }

  @Override
  @Test
  public void testAddWhere() throws Exception {
    SetWhereRequest r = json(
        "{'query':'db.T.aggregate()', 'cols':[{'col':{'table':'T', 'column':'A'}, 'condition':'=3'}]}",
        SetWhereRequest.class);
    eq("db.['T'].aggregate([{\"$match\": {\"A\": 3}}])", e.setWhere(r).query);
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

    query.query = "db.EMP.aggregate([{$project: {_id:1}}])";
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

    ac.query = "db.T.aggregate([{$project:{A:1, B:1}}])";
    eq("db.['T'].aggregate([{\"$project\": {\"B\": 1}}])", e.removeColumn(ac).query);
  }

  @Override
  @Test
  public void testMove() throws Exception {
    MoveColumnRequest ac = new MoveColumnRequest();
    ac.database = "db";
    ac.position = 1;
    ac.col = col("T", "A");

    ac.query = "db.T.aggregate([{$project:{A:1, B:1}}])";
    eq("db.['T'].aggregate([{\"$project\": {\"B\": 1, \"A\": 1}}])", e.moveColumn(ac).query);
  }

  @Override
  @Test
  public void testDistinct() throws Exception {
    DistinctRequest r = json("{'querylimit':1234}", DistinctRequest.class);
    r.query = "db.T.aggregate()";
    eq("db.['T'].aggregate([{\"$limit\": 1234}])", e.distinct(r).query);
  }

  @Override
  public void eq(String expected, String actual) throws Exception {
    Assertions.assertEquals(expected, actual);
  }
}
