package org.dashjoin.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Date;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import org.h2.Driver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SQLCastTest extends JsonCastTest {

  @Override
  AbstractDatabase db() {
    return new SQLDatabase();
  }

  @Test
  public void testDate() throws Exception {
    Assertions.assertTrue(db().cast(p("date", "time"), "10:33:26") instanceof LocalTime);
    Assertions.assertTrue(db().cast(p("date", "timetz"), "10:33:26Z") instanceof OffsetTime);
    Assertions.assertTrue(db().cast(p("date", "timetz"), "10:33:26+00:00") instanceof OffsetTime);
    Assertions.assertTrue(db().cast(p("date", "DATE"), "2020-02-12") instanceof Date);
    Assertions
        .assertTrue(db().cast(p("date", "DATE"), "2021-01-19T10:33:26+00:00") instanceof Date);
    Assertions.assertTrue(db().cast(p("date", "DATE"), "2021-01-19T10:33:26Z") instanceof Date);
    Assertions.assertTrue(db().cast(p("date", "DATE"), "2021-11-06 05:20:44") instanceof Date);
    Assertions.assertTrue(db().cast(p("date", "DATE"), "2021-11-06 05:20") instanceof Date);
    // System.out.println(db().cast(p("date", "DATE"), "2021-11-06 05:20"));
  }

  @Test
  public void testDatePostgres() throws Exception {
    SQLDatabase db = (SQLDatabase) db();
    db.url = "jdbc:postgresql:blabla";
    Assertions.assertTrue(db.cast(p("date", "DATE"), "2021-01-19T10:33:26Z") instanceof LocalDate);
    Assertions.assertTrue(db.cast(p("date", "time"), "10:33:26") instanceof LocalTime);
    Assertions.assertTrue(
        db.cast(p("date", "timestamp"), "2021-01-19T10:33:26Z") instanceof LocalDateTime);
    Assertions.assertTrue(
        db.cast(p("date", "timestamptz"), "2021-01-19T10:33:26Z") instanceof OffsetDateTime);
  }

  @Test
  public void testSetObject() throws Exception {
    SQLDatabase db = (SQLDatabase) db();
    db.url = "jdbc:postgresql:blabla";
    Assertions.assertThrows(NullPointerException.class,
        () -> db.setObject(null, 0, Arrays.asList(1, 2, 3)));
    Assertions.assertThrows(NullPointerException.class,
        () -> db.setObject(null, 0, MapUtil.of("id", 1)));
  }

  @Test
  public void testSchema() {
    SQLDatabase db = (SQLDatabase) db();
    db.url = "jdbc:jtds://host:port;SCHEMA=TEST";
    Assertions.assertEquals("TEST.", db.schema());
  }

  @Test
  public void testSQLDatabase() throws Exception {
    String url = "jdbc:h2:mem:testSQLDatabase";
    Class.forName(Driver.class.getName());
    Connection con = DriverManager.getConnection(url);
    con.createStatement().executeUpdate("create table \"test\"(\"name\" varchar(255) primary key)");
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection() throws SQLException {
        return con;
      }
    };
    db.url = "jdbc:iamnotnull";
    Table table = Table.ofName("test");
    Property name = new Property();
    name.pkpos = 0;
    table.properties = MapUtil.of("name", name);
    db.create(table, Arrays.asList(MapUtil.of("name", "a"), MapUtil.of("name", "b")));
  }

  @Test
  public void testSQLDatabase2() throws Exception {
    String url = "jdbc:h2:mem:testSQLDatabase2";
    Class.forName(Driver.class.getName());
    Connection con = DriverManager.getConnection(url);
    con.createStatement().executeUpdate("create table \"test\"(\"name\" varchar(255) primary key)");
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection() throws SQLException {
        return con;
      }
    };
    db.url = "jdbc:iamnotnull";
    Table table = Table.ofName("test");
    Property name = new Property();
    name.pkpos = 0;
    table.properties = MapUtil.of("name", name);
    db.merge(table, Arrays.asList(MapUtil.of("name", "a"), MapUtil.of("name", "c")));
  }

  @Test
  public void testSerialize() throws Exception {
    String url = "jdbc:h2:mem:testSQLDatabase3";
    Class.forName(Driver.class.getName());
    Connection con = DriverManager.getConnection(url);
    con.createStatement().executeUpdate("create table test(b blob, c clob)");
    con.createStatement().executeUpdate("insert into test (b,c) values('bbbb', 'cccc')");
    ResultSet res = con.createStatement().executeQuery("select * from test");
    res.next();
    new SQLDatabase().serialize(res.getMetaData(), res, 1);
    new SQLDatabase().serialize(res.getMetaData(), res, 2);
  }
}
