package org.dashjoin.service;

import static org.dashjoin.service.QueryEditor.Col.col;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.ColCondition;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

/**
 * tests the query editor implementation. Apps that provide an AbstractDatabase implementation
 * should have a test case that extends from this class
 */
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class QueryEditorTest {

  @Inject
  Services services;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public void before() throws Exception {
    // services.persistantDB = new JSONFileDatabase();
    // services.readonlyDB = new JSONClassloaderDatabase();
    services.getConfig().metadataCollection();
    e = getQueryEditor();
  }

  protected QueryEditorInternal getQueryEditor() {
    try {
      SQLDatabase db = (SQLDatabase) services.getConfig().getDatabase("dj/junit");
      return new SQLEditor(services, db);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected QueryEditorInternal e;

  @Test
  public void testDelegate() throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    QueryEditor.Delegate d = new QueryEditor.Delegate();
    d.services = services;
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/EMP";
    QueryResponse q = d.getInitialQuery(sc, r);
    QueryDatabase query = new QueryDatabase();
    query.database = "dj/junit";
    query.query = q.query;
    d.noop(sc, query);
  }

  @Test
  public void testGetIDsOfClassQuery() throws Exception {
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/junit/EMP";
    Assertions.assertEquals(
        "SELECT\n" + "  \"EMP\".\"ID\", \"EMP\".\"NAME\"\n" + "FROM\n" + "  \"EMP\"",
        e.getInitialQuery(r).query);
  }

  @Test
  public void testEquals() {
    Assertions.assertFalse(SQLEditor.equals("a", "A"));
    Assertions.assertTrue(SQLEditor.equals("\"a\"", "a"));
    Assertions.assertFalse(SQLEditor.equals("\"a", "a"));
  }

  @Test
  public void testEqualsTable() {
    FromItem fi = new net.sf.jsqlparser.schema.Table("\"T\"");
    Assertions.assertTrue(SQLEditor.equals(fi, "T"));
    Assertions.assertFalse(SQLEditor.equals(fi, "\"T"));
    Assertions.assertFalse(SQLEditor.equals(fi, "'T'"));
    Assertions.assertFalse(SQLEditor.equals(fi, "t"));
  }

  @Test
  public void testEqualsSelect() throws Exception {
    SelectExpressionItem si =
        new SelectExpressionItem(new Column(new net.sf.jsqlparser.schema.Table("T"), "C"));
    Assertions.assertTrue(SQLEditor.equals(si, col("T", "C")));

    si = new SelectExpressionItem(CCJSqlParserUtil.parseExpression("count(T.A)"));
    Assertions.assertTrue(SQLEditor.equals(si, col("T", "A")));

    Alias alias = new Alias("X");
    si.setAlias(alias);
    Assertions.assertTrue(SQLEditor.equals(si, col("T", "A")));
  }

  // T(ID, FK)
  // U(ID, C)

  @Test
  public void testAddColumn() throws Exception {
    String sql = "SELECT T.A AS RENAME FROM T";
    // add a column from the same table
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("T", "A"), col("T", "B")));
    eq("SELECT T.A AS RENAME, T.B FROM T", res.query);
  }

  @Test
  public void testAddColumnJoin() throws Exception {
    String sql = "SELECT T.A AS RENAME, T.FK FROM T";
    // FK -> ID
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("T", "FK"), col("U", "ID")));
    eq("SELECT T.A AS RENAME, T.FK, U.ID FROM T INNER JOIN U ON T.FK = U.ID", res.query);
  }

  @Test
  public void testAddColumnJoin2() throws Exception {
    String sql = "SELECT A, FK FROM T";
    // FK -> C
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("T", "FK"), col("U", "C")));
    eq("SELECT A, FK, U.C FROM T INNER JOIN U ON T.FK = U.ID", res.query);
  }

  @Test
  public void testAddColumnJoin3() throws Exception {
    String sql = "SELECT U.ID FROM U";
    // U.ID -> T.ID
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("U", "ID"), col("T", "ID")));
    eq("SELECT U.ID, T.ID FROM U INNER JOIN T ON U.ID = T.FK", res.query);
  }

  @Test
  public void testAddColumnJoin4() throws Exception {
    String sql = "SELECT U.ID AS RENAME FROM U";
    // U.ID -> T.C
    QueryResponse res = e.addColumn(addColumnRequest(sql, col("U", "ID"), col("T", "C")));
    eq("SELECT U.ID AS RENAME, T.C FROM U INNER JOIN T ON U.ID = T.FK", res.query);
  }

  @Test
  public void testOrderBy() throws Exception {
    SortRequest r = json(
        "{'order':'asc', 'query':'SELECT T.A AS RENAME FROM T', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT T.A AS RENAME FROM T ORDER BY T.A", e.sort(r).query);

    r = json(
        "{'order':'asc', 'query':'SELECT T.A AS RENAME FROM T ORDER BY EMP.ID DESC', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT T.A AS RENAME FROM T ORDER BY T.A", e.sort(r).query);

    r = json(
        "{'order':'asc', 'query':'SELECT T.A FROM T ORDER BY X', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT T.A FROM T ORDER BY T.A", e.sort(r).query);

    r = json(
        "{'order':'desc', 'query':'SELECT T.A FROM T ORDER BY X', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT T.A FROM T ORDER BY T.A DESC", e.sort(r).query);

    r = json("{'query':'SELECT T.A FROM T ORDER BY X', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT T.A FROM T", e.sort(r).query);

    r = json(
        "{'query':'SELECT COUNT(T.A) FROM T GROUP BY A ORDER BY COUNT(T.A)', 'col':{'table':'T', 'column':'A'}}",
        SortRequest.class);
    eq("SELECT COUNT(T.A) FROM T GROUP BY A", e.sort(r).query);
  }

  @Test
  public void testDistinct() throws Exception {
    DistinctRequest r =
        json("{'query':'SELECT T.A FROM T', 'distinct':true}", DistinctRequest.class);
    eq("SELECT DISTINCT T.A FROM T", e.distinct(r).query);

    r = json("{'query':'SELECT DISTINCT T.A FROM T', 'distinct':false}", DistinctRequest.class);
    eq("SELECT T.A FROM T", e.distinct(r).query);

    r = json("{'query':'SELECT DISTINCT T.A FROM T', 'distinct':false, 'querylimit':1234}",
        DistinctRequest.class);
    eq("SELECT T.A FROM T LIMIT 1234", e.distinct(r).query);

    r = json("{'query':'SELECT DISTINCT T.A FROM T LIMIT 1234', 'distinct':false}",
        DistinctRequest.class);
    eq("SELECT T.A FROM T", e.distinct(r).query);

    r = json("{'query':'SELECT COUNT(T.A) FROM T GROUP BY T.B ORDER BY MAX(T.A)', 'distinct':true}",
        DistinctRequest.class);
    eq("SELECT DISTINCT COUNT(T.A) FROM T GROUP BY T.B", e.distinct(r).query);
  }

  @Test
  public void testRename() throws Exception {
    RenameRequest r =
        json("{'query':'SELECT T.A FROM T', 'col':{'table':'T', 'column':'A'}, 'name':'N'}",
            RenameRequest.class);
    eq("SELECT T.A AS N FROM T", e.rename(r).query);

    r = json("{'query':'SELECT T.A AS N FROM T', 'col':{'table':'T', 'column':'A'}, 'name':''}",
        RenameRequest.class);
    eq("SELECT T.A FROM T", e.rename(r).query);

    r = json("{'query':'SELECT COUNT(T.A) FROM T', 'col':{'table':'T', 'column':'A'}, 'name':'N'}",
        RenameRequest.class);
    eq("SELECT COUNT(T.A) AS N FROM T", e.rename(r).query);

    r = json(
        "{'query':'SELECT COUNT(T.A) AS N FROM T', 'col':{'table':'T', 'column':'A'}, 'name':''}",
        RenameRequest.class);
    eq("SELECT COUNT(T.A) FROM T", e.rename(r).query);
  }

  @Test
  public void testRenameRenamed() throws Exception {
    RenameRequest r = json(
        "{'query':'SELECT T.A AS RENAME FROM T', 'col':{'table':'T', 'column':'A'}, 'name':'N'}",
        RenameRequest.class);
    eq("SELECT T.A AS N FROM T", e.rename(r).query);
  }

  @Test
  public void testRenameAggr() throws Exception {
    RenameRequest r = json(
        "{'query':'SELECT COUNT(T.A) FROM T GROUP BY T.A', 'col':{'table':'T', 'column':'A'}, 'name':'N'}",
        RenameRequest.class);
    eq("SELECT COUNT(T.A) AS N FROM T GROUP BY T.A", e.rename(r).query);
  }

  @Test
  public void testRenameRenamedAggr() throws Exception {
    RenameRequest r = json(
        "{'query':'SELECT COUNT(T.A) AS RENAME FROM T GROUP BY T.A', 'col':{'table':'T', 'column':'A'}, 'name':'N'}",
        RenameRequest.class);
    eq("SELECT COUNT(T.A) AS N FROM T GROUP BY T.A", e.rename(r).query);
  }

  @Test
  public void testRenameNoop() throws Exception {
    RenameRequest r =
        json("{'query':'SELECT T.A FROM T', 'col':{'table':'T', 'column':'A'}, 'name':'A'}",
            RenameRequest.class);
    eq("SELECT T.A FROM T", e.rename(r).query);
  }

  @Test
  public void testAddWhere() throws Exception {
    SetWhereRequest r = json(
        "{'query':'SELECT T.A FROM T', 'cols':[{'col':{'table':'T', 'column':'A'}, 'condition':'!=null'}]}",
        SetWhereRequest.class);
    eq("SELECT T.A FROM T\nWHERE T.A!=NULL", e.setWhere(r).query);
  }

  @Test
  public void testGroupBy() throws Exception {
    SetWhereRequest ac;
    String q, qr, rq;

    q = "SELECT\r\n" + "  \"EMP\".\"ID\", \"EMP\".\"WORKSON\", \"PRJ\".\"ID\"\r\n" + "FROM\r\n"
        + "  \"EMP\" INNER JOIN \"PRJ\" ON \"EMP\".\"WORKSON\" = \"PRJ\".\"ID\"";
    q = q.replaceAll("\"", "");

    // agg = MAX(EMP.WORKSON)
    ac = json("{'database': 'db', 'query':'" + normalize(q)
        + "', 'cols':[{'col':{'table':'EMP', 'column':'ID'}, 'condition':'GROUP BY'}, {'col':{'table':'EMP', 'column':'WORKSON'}, 'condition':'MAX'}, {'col':{'table':'PRJ', 'column':'ID'}, 'condition':'GROUP BY'}]}",
        SetWhereRequest.class);
    rq = e.setGroupBy(ac).query;
    // System.out.println(rq);

    qr = "SELECT\r\n" + "  EMP.ID, MAX(EMP.WORKSON), PRJ.ID\r\n" + "FROM\r\n"
        + "  EMP INNER JOIN PRJ ON EMP.WORKSON = PRJ.ID GROUP BY EMP.ID, PRJ.ID";
    eq(qr, rq);

    // agg = AVG(PRJ.ID)
    ac = json("{'database': 'db', 'query':'" + normalize(qr)
        + "', 'cols':[{'col':{'table':'EMP', 'column':'ID'}, 'condition':'GROUP BY'}, {'col':{'table':'EMP', 'column':'WORKSON'}, 'condition':'MAX'}, {'col':{'table':'PRJ', 'column':'ID'}, 'condition':'AVG'}]}",
        SetWhereRequest.class);
    rq = e.setGroupBy(ac).query;
    // System.out.println(rq);

    qr = "SELECT\r\n" + "  EMP.ID, MAX(EMP.WORKSON), AVG(PRJ.ID)\r\n" + "FROM\r\n"
        + "  EMP INNER JOIN PRJ ON EMP.WORKSON = PRJ.ID GROUP BY EMP.ID";
    eq(qr, rq);

    ac = json("{'database': 'db', 'query':'"
        + normalize("SELECT EMP.ID, MAX(EMP.WORKSON) FROM EMP ORDER BY MAX(EMP.WORKSON)")
        + "', 'cols':[{'col':{'table':'EMP', 'column':'ID'}, 'condition':'GROUP BY'}, {'col':{'table':'EMP', 'column':'WORKSON'}, 'condition':'GROUP BY'}]}",
        SetWhereRequest.class);
    rq = e.setGroupBy(ac).query;
    eq("SELECT EMP.ID, EMP.WORKSON FROM EMP", rq);
  }

  @Test
  public void testMove() throws Exception {

    MoveColumnRequest ac = new MoveColumnRequest();
    ac.database = "db";
    ac.position = 1;
    ac.col = col("T", "A");

    ac.query = "SELECT T.A, T.B FROM T";
    eq("SELECT T.B, T.A FROM T", e.moveColumn(ac).query);

    ac.query = "SELECT COUNT(T.A), T.B FROM T GROUP BY T.A";
    eq("SELECT T.B, COUNT(T.A) FROM T GROUP BY T.A", e.moveColumn(ac).query);

    ac.query = "SELECT COUNT(T.A) AS RENAME, T.B FROM T GROUP BY T.A";
    eq("SELECT T.B, COUNT(T.A) AS RENAME FROM T GROUP BY T.A", e.moveColumn(ac).query);
  }

  @Test
  public void testRemove() throws Exception {

    RemoveColumnRequest ac = new RemoveColumnRequest();
    ac.database = "db";
    ac.col = col("T", "A");

    ac.query = "SELECT T.A, T.B FROM T";
    eq("SELECT T.B FROM T", e.removeColumn(ac).query);

    ac.query = "SELECT COUNT(T.A), T.B FROM T GROUP BY T.A";
    eq("SELECT T.B FROM T GROUP BY T.A", e.removeColumn(ac).query);

    ac.query = "SELECT COUNT(T.A) AS RENAME, T.B FROM T GROUP BY T.A";
    eq("SELECT T.B FROM T GROUP BY T.A", e.removeColumn(ac).query);
  }

  @Test
  public void testMetadata() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.database = "db";
    QueryResponse res;

    query.query = "select EMP.ID from EMP";
    res = e.noop(query);
    Assertions.assertEquals("EMP.ID", res.metadata.get(0).col.toString());
    Assertions.assertEquals("EMP.ID", res.fieldNames.get(0));

    query.query = "select EMP.ID AS RENAME from EMP";
    res = e.noop(query);
    Assertions.assertEquals("EMP.ID", res.metadata.get(0).col.toString());
    Assertions.assertEquals("RENAME", res.fieldNames.get(0));

    query.query = "select COUNT(EMP.ID) from EMP";
    res = e.noop(query);
    Assertions.assertEquals("EMP.ID", res.metadata.get(0).col.toString());
    Assertions.assertEquals("COUNT(EMP.ID)", res.fieldNames.get(0));

    query.query = "select COUNT(EMP.ID) AS RENAME from EMP";
    res = e.noop(query);
    Assertions.assertEquals("EMP.ID", res.metadata.get(0).col.toString());
    Assertions.assertEquals("RENAME", res.fieldNames.get(0));

    query.query = "select * from EMP";
    res = e.noop(query);
    Assertions.assertTrue(res.query.contains("WORKSON"));

    query.query = "select * from EMP, PRJ where EMP.WORKSON = PRJ.ID";
    res = e.noop(query);
    Assertions.assertTrue(res.query.contains("WORKSON"));

    query.query =
        "select emp.id, count(emp.workson) from EMP where emp.workson > 0 group by emp.id";
    res = e.noop(query);
    Assertions.assertEquals("> 0", res.metadata.get(1).where);
  }

  @Test
  public void testAssertions() throws Exception {

    // null parameter
    try {
      e.sort(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.addColumn(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.moveColumn(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.removeColumn(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.setWhere(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.setGroupBy(null);
    } catch (IllegalArgumentException e) {
    }
    try {
      e.rename(null);
    } catch (IllegalArgumentException e) {
    }

    // empty object
    try {
      SortRequest ac = new SortRequest();
      e.sort(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      AddColumnRequest ac = new AddColumnRequest();
      e.addColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      MoveColumnRequest ac = new MoveColumnRequest();
      e.moveColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      RemoveColumnRequest ac = new RemoveColumnRequest();
      e.removeColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      SetWhereRequest ac = new SetWhereRequest();
      e.setWhere(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      SetWhereRequest ac = new SetWhereRequest();
      e.setGroupBy(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      RenameRequest ac = new RenameRequest();
      e.rename(ac);
    } catch (IllegalArgumentException e) {
    }

    // empty col.table
    try {
      SortRequest ac = new SortRequest();
      ac.col = col("", "count(x)");
      e.sort(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      AddColumnRequest ac = new AddColumnRequest();
      ac.query = "select * from t";
      ac.add = col("a", "b");
      ac.col = col("", "count(x)");
      e.addColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      MoveColumnRequest ac = new MoveColumnRequest();
      ac.col = col("", "count(x)");
      e.moveColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      RemoveColumnRequest ac = new RemoveColumnRequest();
      ac.col = col("", "count(x)");
      e.removeColumn(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      SetWhereRequest ac = new SetWhereRequest();
      ColCondition cond = new ColCondition();
      cond.col = col("", "count(x)");
      ac.cols = Arrays.asList(cond);
      e.setWhere(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      SetWhereRequest ac = new SetWhereRequest();
      ColCondition cond = new ColCondition();
      cond.col = col("", "count(x)");
      ac.cols = Arrays.asList(cond);
      e.setGroupBy(ac);
    } catch (IllegalArgumentException e) {
    }
    try {
      RenameRequest ac = new RenameRequest();
      ac.col = col("", "count(x)");
      ac.name = "rename";
      e.rename(ac);
    } catch (IllegalArgumentException e) {
    }

    // other checks
    try {
      AddColumnRequest ac = new AddColumnRequest();
      ac.add = col("a", "b");
      ac.col = col("a", "s");
      ac.query = "select count(*) from a group by b";
      e.addColumn(ac);
    } catch (Exception e) {
      Assertions.assertEquals("Cannot join an aggregated query", e.getMessage());
    }
  }

  public static AddColumnRequest addColumnRequest(String query, Col selected, Col add) {
    AddColumnRequest res = new AddColumnRequest();
    res.query = query;
    res.add = add;
    res.col = selected;
    res.database = "dj/junit";
    return res;
  }

  /**
   * parse and toString queries before comparing then (gets rid of quotation / pretty printing)
   */
  void eq(String expected, String actual) throws Exception {
    expected = expected.replace('"', '_');
    expected = expected.replaceAll("_", "");
    actual = actual.replace('"', '_');
    actual = actual.replaceAll("_", "");
    Assertions.assertEquals(normalize(expected), normalize(actual));
  }

  String normalize(String query) throws JSQLParserException {
    return CCJSqlParserUtil.parse(query).toString();
  }

  <T> T json(String s, Class<T> c) {
    s = s.replace('\'', '"');
    try {
      T t = objectMapper.readValue(s.getBytes(), c);
      if (t instanceof QueryDatabase)
        ((QueryDatabase) t).database = "dj/junit";
      return t;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
