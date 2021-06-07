package org.dashjoin.service.ddl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Inject;
import org.dashjoin.service.SQLDatabase;
import org.dashjoin.service.Services;
import org.h2.Driver;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SQLSchemaChangeTest {

  @Inject
  Services services;

  @Test
  public void ddl() throws Exception {
    Class.forName(Driver.class.getName());
    Connection con = DriverManager.getConnection("jdbc:h2:mem:ddl");
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection() throws SQLException {
        return con;
      }
    };
    db.url = "jdbc:";
    SQLSchemaChange s = new SQLSchemaChange(db);
    db.url = "jdbc:h2:mem:ddl";
    s.createTable("T", "ID", "integer");
  }

  @Test
  public void testPostgresTypes() {
    SQLDatabase db = new SQLDatabase();
    db.url = "jdbc:postgres:...";
    SQLSchemaChange s = new SQLSchemaChange(db);
    Assert.assertEquals("jsonb", s.t(null, null, "object"));
    Assert.assertEquals("jsonb", s.t(null, null, "array"));
    Assert.assertEquals("INTEGER", s.t(null, null, "integer"));
    Assert.assertEquals("BOOLEAN", s.t(null, null, "boolean"));
    Assert.assertEquals("DOUBLE PRECISION", s.t(null, null, "number"));
    Assert.assertEquals("TIMESTAMP", s.t(null, null, "date"));
    Assert.assertEquals("TEXT", s.t(null, null, "string"));
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assert.assertEquals("BOOLEAN", s.t(null, null, "unknown"));
    });
  }

  @Test
  public void testOtherTypes() {
    SQLDatabase db = new SQLDatabase();
    db.url = "jdbc:other:...";
    SQLSchemaChange s = new SQLSchemaChange(db);
    Assert.assertEquals("INTEGER", s.t(null, null, "integer"));
    Assert.assertEquals("BOOLEAN", s.t(null, null, "boolean"));
    Assert.assertEquals("DOUBLE", s.t(null, null, "number"));
    Assert.assertEquals("DATETIME", s.t(null, null, "date"));
    Assert.assertEquals("VARCHAR(255)", s.t(null, null, "string"));
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assert.assertEquals("jsonb", s.t(null, null, "object"));
    });
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assert.assertEquals("jsonb", s.t(null, null, "array"));
    });
  }
}
