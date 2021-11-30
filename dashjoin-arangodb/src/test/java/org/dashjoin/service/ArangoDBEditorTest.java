package org.dashjoin.service;

import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

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
    // TODO
  }

  @Override
  @Test
  public void testGetIDsOfClassQuery() throws Exception {
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/EMP";
    Assert.assertEquals("FOR i IN EMP RETURN {\"_key\": i._key}", e.getInitialQuery(r).query);
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
    // TODO
  }

  @Override
  @Test
  public void testAddWhere() throws Exception {
    // TODO
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

    query.query = "FOR i IN EMP RETURN {\"_key\": i._key}";
    res = e.noop(query);
    Assert.assertEquals("EMP._key", res.metadata.get(0).col.toString());
    Assert.assertEquals("_key", res.fieldNames.get(0));
  }

  @Override
  @Test
  public void testAssertions() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testRemove() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testMove() throws Exception {
    // TODO
  }

  @Override
  @Test
  public void testDistinct() throws Exception {
    // TODO
  }

  @Override
  void eq(String expected, String actual) throws Exception {
    Assert.assertEquals(expected, actual);
  }
}
