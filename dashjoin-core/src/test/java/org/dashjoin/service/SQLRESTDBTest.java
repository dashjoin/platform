package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static net.sf.jsqlparser.parser.CCJSqlParserUtil.parse;
import static org.dashjoin.service.SQLEditor.compatibilityError;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.SQLDatabase.PreparedStmt;
import org.h2.Driver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.google.common.collect.Maps;
import io.quarkus.test.junit.QuarkusTest;

/**
 * tests some specialized features of SQL DBs in conjunction with REST (query catalog parameter
 * replacement, insert key autoincrement, SQL editor compatibility)
 */
@QuarkusTest
public class SQLRESTDBTest {

  @Test
  public void testPrep() throws Exception {

    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("limit", 10);
    arguments.put("offset", 20);
    PreparedStmt ps =
        SQLDatabase.prepareStatement("select * from t limit ${limit} offset ${offset}", arguments);
    Assertions.assertArrayEquals(new Object[] {10, 20}, ps.arguments);
    Assertions.assertEquals("select * from t limit ? offset ?", ps.query);
  }

  @Test
  public void testPrepEscape() throws Exception {

    PreparedStmt ps = SQLDatabase.prepareStatement("select * from t limit \\${limit}", null);
    Assertions.assertArrayEquals(new Object[] {}, ps.arguments);
    Assertions.assertEquals("select * from t limit \\${limit}", ps.query);
  }

  @Test
  public void testTablesInQuery() throws Exception {
    List<String> list = new SQLDatabase().getTablesInQuery("select * from t,u");
    Assertions.assertEquals("[t, u]", list.toString());

    list = new SQLDatabase().getTablesInQuery("delete from t");
    Assertions.assertEquals("[t]", list.toString());

    list = new SQLDatabase().getTablesInQuery("insert into t values (1)");
    Assertions.assertEquals("[t]", list.toString());

    list = new SQLDatabase().getTablesInQuery("update t set x=1");
    Assertions.assertEquals("[t]", list.toString());
  }

  @Test
  public void testAutoInc() throws Exception {

    String url = "jdbc:h2:mem:autoinc";
    Class.forName(Driver.class.getName());
    Connection con = DriverManager.getConnection(url);
    con.createStatement()
        .executeUpdate("create table \"test\"(\"id\" int auto_increment, \"name\" varchar(255))");
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection() throws SQLException {
        return con;
      }
    };
    db.url = "jdbc:iamnotnull";
    Table m = new Table();
    m.name = "test";
    Property id = new Property();
    id.pkpos = 0;
    id.name = "id";
    id.readOnly = true; // Mark auto-increment fields (otherwise constraint violation)
    Property name = new Property();
    m.properties = of("id", id, "name", name);
    Map<String, Object> object = Maps.newHashMap(of("name", "test"));
    db.create(m, object);
    Assertions.assertEquals("{name=test, id=1}", "" + object);
  }

  @Test
  public void testComp() throws Exception {
    Assertions.assertEquals("Only plain SELECT queries are supported (no WITH or VALUES clauses)",
        compatibilityError(parse("create table t (i int)")));
    Assertions.assertEquals("Select * and TABLE.* are not supported",
        compatibilityError(parse("select * from t")));
    Assertions.assertEquals("Only select from table is supported",
        compatibilityError(parse("select * from (select * from t)")));
    Assertions.assertEquals("Only select from table is supported",
        compatibilityError(parse("select * from t, (select * from t)")));
    Assertions.assertEquals("Having not supported",
        compatibilityError(parse("select * from t group by id having count(id) > 7")));
    Assertions.assertEquals("Unsupported expression: a + b",
        compatibilityError(parse("select a+b from t")));
    Assertions.assertEquals("Unsupported expression: sum(a + b)",
        compatibilityError(parse("select sum(a+b) from t")));
    Assertions.assertEquals("Unsupported expression: f(a, b)",
        compatibilityError(parse("select f(a,b) from t")));
    Assertions.assertEquals("Left side of where expressions must be a column",
        compatibilityError(parse("select t.a from t where 1 < t.a")));
    Assertions.assertNull(compatibilityError(
        parse("select t.a from t where t.a = 3 and (t.b like '%test' or t.b < 5)")));
    Assertions.assertEquals("Unsupported expression: count(*)",
        compatibilityError(parse("select count(*) from t")));
  }

  @Test
  public void testUnion() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.query = "select * from t union select * from s";
    Assertions.assertThrows(NumberFormatException.class,
        () -> new SQLEditor(null, db()).noop(query));
  }

  @Test
  public void testTableMetadata() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.query = "select * from t where id like 'x%'";
    Assertions.assertThrows(NumberFormatException.class,
        () -> new SQLEditor(null, db()).noop(query));
  }

  @Test
  public void testParseWhere() throws Exception {
    QueryDatabase query = new QueryDatabase();
    query.query = "select t.id from t where id is null";
    Assertions.assertThrows(NumberFormatException.class,
        () -> new SQLEditor(null, db()).noop(query));
  }

  SQLDatabase db() {
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection() throws SQLException {
        throw new NumberFormatException();
      }
    };
    db.url = "dummy";
    db.tables.put("t", new Table());
    db.tables.get("t").properties = new HashMap<>();
    return db;
  }
}
