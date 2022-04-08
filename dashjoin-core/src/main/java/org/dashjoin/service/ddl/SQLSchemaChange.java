package org.dashjoin.service.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.dashjoin.service.SQLDatabase;

/**
 * SQL schema change implementation
 */
public class SQLSchemaChange implements SchemaChange {

  SQLDatabase db;

  public SQLSchemaChange(SQLDatabase db) {
    if (db.url.startsWith("jdbc:h2:mem:"))
      throw new RuntimeException("Schema changes are not supported on this database: " + db.url);
    this.db = db;
  }

  @Override
  public void createTable(String table, String keyName, String keyType) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (java.sql.Statement stmt = con.createStatement()) {
        if (keyName == null)
          stmt.execute("create table " + db.q(table) + "(ID int primary key not null, \"name\" "
              + t(null, null, "string") + ")");
        else {
          stmt.execute("create table " + db.q(table) + "(" + db.q(keyName) + " "
              + t(table, keyName, keyType) + " primary key)");
        }
      }
    }
  }

  @Override
  public void dropTable(String table) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        stmt.execute("drop table " + db.q(table));
      }
    }
  }

  @Override
  public void renameTable(String table, String newName) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        if (db.url.startsWith("jdbc:sqlserver") || db.url.startsWith("jdbc:jtds"))
          stmt.execute("EXEC sp_rename " + db.q(table) + ", " + db.q(newName));
        else
          stmt.execute("ALTER TABLE " + db.q(table) + " RENAME TO " + db.q(newName));
      }
    }
  }

  @Override
  public void createColumn(String table, String columnName, String columnType) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        stmt.execute("alter table " + db.q(table) + " add " + db.q(columnName) + " "
            + t(table, columnName, columnType));
      }
    }
  }

  @Override
  public void renameColumn(String table, String column, String newName) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        if (db.url.startsWith("jdbc:sqlserver") || db.url.startsWith("jdbc:jtds"))
          stmt.execute(
              "EXEC sp_rename '" + db.q(table) + "." + db.q(column) + "', '" + db.q(newName) + "'");
        else
          stmt.execute("ALTER TABLE " + db.q(table) + " RENAME COLUMN " + db.q(column) + " TO "
              + db.q(newName));
      }
    }
  }

  @Override
  public void alterColumn(String table, String column, String newType) throws SQLException {
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        String typeKeyword = db.url.startsWith("jdbc:postgres") ? "TYPE " : "";
        String alterColumn =
            db.url.startsWith("jdbc:mysql") || db.url.startsWith("jdbc:mariadb") ? " MODIFY "
                : " ALTER COLUMN ";
        stmt.execute("ALTER TABLE " + db.q(table) + alterColumn + db.q(column) + " " + typeKeyword
            + t(table, column, newType));
      }
    }
  }

  @Override
  public void dropColumn(String table, String column) throws SQLException {
    if (db.url.startsWith("jdbc:sqlite"))
      throw new SQLException("SQLite does not support drop column");
    try (Connection con = db.getConnection()) {
      try (Statement stmt = con.createStatement()) {
        stmt.execute("alter table " + db.q(table) + " DROP COLUMN " + db.q(column));
      }
    }
  }

  /**
   * get the suitable column type
   */
  String t(String t, String c, Object s) {
    if (db.url.startsWith("jdbc:postgres")) {
      if ("object".equals(s))
        return "jsonb";
      if ("array".equals(s))
        return "jsonb";
    }
    if ("integer".equals(s))
      return "INTEGER";
    if ("boolean".equals(s))
      return db.url.startsWith("jdbc:sqlserver") ? "BIT" : "BOOLEAN";
    if ("number".equals(s))
      return db.url.startsWith("jdbc:postgres") ? "DOUBLE PRECISION" : "DOUBLE";
    if ("date".equals(s))
      return db.url.startsWith("jdbc:postgres") ? "TIMESTAMP" : "DATETIME";
    if ("string".equals(s))
      return db.url.startsWith("jdbc:postgres") ? "TEXT"
          : (db.url.startsWith("jdbc:sqlserver") ? "NVARCHAR(1023)" : "VARCHAR(1023)");
    throw new RuntimeException("unknown type for column " + t + "." + c + ": " + s);
  }
}
