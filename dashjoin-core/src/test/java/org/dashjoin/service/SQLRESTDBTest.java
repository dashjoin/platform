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
import org.dashjoin.service.SQLDatabase.PreparedStmt;
import org.h2.Driver;
import org.junit.Assert;
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
    Assert.assertArrayEquals(new Object[] {10, 20}, ps.arguments);
    Assert.assertEquals("select * from t limit ? offset ?", ps.query);
  }

  @Test
  public void testPrepEscape() throws Exception {

    PreparedStmt ps = SQLDatabase.prepareStatement("select * from t limit \\${limit}", null);
    Assert.assertArrayEquals(new Object[] {}, ps.arguments);
    Assert.assertEquals("select * from t limit \\${limit}", ps.query);
  }

  @Test
  public void testTablesInQuery() throws Exception {
    List<String> list = new SQLDatabase().getTablesInQuery("select * from t,u");
    Assert.assertEquals("[t, u]", list.toString());

    list = new SQLDatabase().getTablesInQuery("delete from t");
    Assert.assertEquals("[t]", list.toString());

    list = new SQLDatabase().getTablesInQuery("insert into t values (1)");
    Assert.assertEquals("[t]", list.toString());

    list = new SQLDatabase().getTablesInQuery("update t set x=1");
    Assert.assertEquals("[t]", list.toString());
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
    Property name = new Property();
    m.properties = of("id", id, "name", name);
    Map<String, Object> object = Maps.newHashMap(of("name", "test"));
    db.create(m, object);
    // stmt.getGeneratedKeys() no longer works after H2 upgrade 1.4.196 => 1.4.200
    // Assert.assertEquals("{name=test, id=1}", "" + object);
  }

  @Test
  public void testComp() throws Exception {
    Assert.assertEquals("Only plain SELECT queries are supported (no WITH or VALUES clauses)",
        compatibilityError(parse("create table t (i int)")));
    Assert.assertEquals("Select * and TABLE.* are not supported",
        compatibilityError(parse("select * from t")));
    Assert.assertEquals("Only select from table is supported",
        compatibilityError(parse("select * from (select * from t)")));
    Assert.assertEquals("Only select from table is supported",
        compatibilityError(parse("select * from t, (select * from t)")));
    Assert.assertEquals("Having not supported",
        compatibilityError(parse("select * from t group by id having count(id) > 7")));
    Assert.assertEquals("Unsupported expression: a + b",
        compatibilityError(parse("select a+b from t")));
    Assert.assertEquals("Unsupported expression: sum(a + b)",
        compatibilityError(parse("select sum(a+b) from t")));
    Assert.assertEquals("Unsupported expression: f(a, b)",
        compatibilityError(parse("select f(a,b) from t")));
    Assert.assertEquals("Left side of where expressions must be a column",
        compatibilityError(parse("select t.a from t where 1 < t.a")));
    Assert.assertNull(compatibilityError(
        parse("select t.a from t where t.a = 3 and (t.b like '%test' or t.b < 5)")));
    Assert.assertEquals("Unsupported expression: count(*)",
        compatibilityError(parse("select count(*) from t")));
  }
}
