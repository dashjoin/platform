package org.dashjoin.service.ddl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Inject;
import org.dashjoin.service.SQLDatabase;
import org.dashjoin.service.Services;
import org.h2.Driver;
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
    Assertions.assertEquals("jsonb", s.t(null, null, "object"));
    Assertions.assertEquals("jsonb", s.t(null, null, "array"));
    Assertions.assertEquals("INT", s.t(null, null, "integer"));
    Assertions.assertEquals("BOOLEAN", s.t(null, null, "boolean"));
    Assertions.assertEquals("DOUBLE PRECISION", s.t(null, null, "number"));
    Assertions.assertEquals("TIMESTAMP", s.t(null, null, "date"));
    Assertions.assertEquals("TEXT", s.t(null, null, "string"));
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assertions.assertEquals("BOOLEAN", s.t(null, null, "unknown"));
    });
  }

  @Test
  public void testOtherTypes() {
    SQLDatabase db = new SQLDatabase();
    db.url = "jdbc:other:...";
    SQLSchemaChange s = new SQLSchemaChange(db);
    Assertions.assertEquals("INT", s.t(null, null, "integer"));
    Assertions.assertEquals("BOOLEAN", s.t(null, null, "boolean"));
    Assertions.assertEquals("DOUBLE", s.t(null, null, "number"));
    Assertions.assertEquals("DATETIME", s.t(null, null, "date"));
    Assertions.assertEquals("VARCHAR(1023)", s.t(null, null, "string"));
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assertions.assertEquals("jsonb", s.t(null, null, "object"));
    });
    Assertions.assertThrows(RuntimeException.class, () -> {
      Assertions.assertEquals("jsonb", s.t(null, null, "array"));
    });
  }
}
