package org.dashjoin.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dashjoin.util.Escape;
import com.google.common.collect.ImmutableMap;

/**
 * simple DB metadata class. might switch to schemaspy or schemacrawler later
 */
public class Metadata {

  private final static Logger logger = Logger.getLogger(Metadata.class.getName());

  /**
   * (composite) DB key
   */
  public static class Key {

    /**
     * key column
     */
    public List<String> col = new ArrayList<>();

    /**
     * set the sequence-th key col
     * 
     * @param c DB column
     * @param sequence the index of the col within the key (counting from 1)
     */
    public void set(String c, short sequence) {
      sequence--;
      while (!(sequence < col.size()))
        col.add(null);
      col.set(sequence, c);
    }

    @Override
    public String toString() {
      return col.toString();
    }
  }

  public static class TableAndKey {
    public String table;
    public Key key;
  }

  public static class FKList {

    List<TableAndKey> list = new ArrayList<>();

    public boolean containsKey(String table) {
      return get(table) != null;
    }

    public Key get(String table) {
      for (TableAndKey tk : list)
        if (tk.table.equals(table))
          return tk.key;
      return null;
    }

    public void put(String table, Key key) {
      TableAndKey tk = new TableAndKey();
      tk.key = key;
      tk.table = table;
      list.add(tk);
    }
  }

  /**
   * holds SQL column metadata
   */
  public static class Column {

    /**
     * DB column name
     */
    public String name;

    /**
     * https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html
     */
    public String typeName;

    /**
     * NULLABLE
     */
    public boolean required;

    /**
     * IS_AUTOINCREMENT
     */
    public boolean readOnly;
  }

  /**
   * a DB table
   */
  public static class MdTable {

    public MdTable(String _name) {
      this.name = _name;
    }

    /**
     * table name, same as Metadata.tables key
     */
    public String name;

    /**
     * table's PK
     */
    public Key pk = new Key();

    /**
     * table's FKs: referenced table name and the foreign key is stored with the table where is it
     * defined: create table t (pk int primary key, fk int references r(id)). Both pk and fk columns
     * are stored in the object of table t
     */
    public FKList fks = new FKList();

    /**
     * like fks.get(table) but also puts new map entry if table does not yet exist
     */
    public Key getOrCreateFk(String table) {
      if (fks.containsKey(table))
        return fks.get(table);
      Key key = new Key();
      fks.put(table, key);
      return key;
    }

    public List<Column> columns = new ArrayList<>();

    @Override
    public String toString() {
      Map<String, Key> fk = new LinkedHashMap<>();
      for (TableAndKey i : fks.list)
        fk.put(i.table, i.key);
      return "Table " + name + " {pk=" + pk + ", fk=" + fk + "}";
    }
  }

  /**
   * main metadata structure
   */
  public Map<String, MdTable> tables = new LinkedHashMap<>();

  /**
   * default constructor
   */
  public Metadata() {

  }

  /**
   * read the schema from con
   */
  public Metadata(Connection con, String url, List<String> excludeTables) throws SQLException {
    DatabaseMetaData md = con.getMetaData();
    String schema = getSchema(con, url);
    try (ResultSet res = md.getTables(url.startsWith("jdbc:mariadb") ? con.getCatalog() : null, schema, null, null)) {
      while (res.next()) {
        String tableType = res.getString("TABLE_TYPE");
        // Note - H2 2.x introduced "BASE TABLE" as default table type:
        // https://h2database.com/html/systemtables.html#information_schema_tables
        if ("TABLE".equals(tableType) || "BASE TABLE".equals(tableType)
            || "VIEW".equals(tableType)) {
          String name = res.getString("TABLE_NAME");
          if (excludeTables != null && excludeTables.contains(name))
            continue;
          tables.put(name, new MdTable(name));
        }
      }
    }
    // H2 and jTDS require table keys to be queried individually
    // Note: before we used table==null for Postgres, but this was the
    // only DB which allowed this. Thus we are now doing the safe way for all DBs.
    for (String table : tables.keySet()) {
      try (ResultSet res = md.getPrimaryKeys(null, schema, table)) {
        while (res.next()) {
          tables.get(table).pk.set(res.getString("COLUMN_NAME"), res.getShort("KEY_SEQ"));
        }
      }
      try {
        try (ResultSet res = md.getImportedKeys(null, schema, table)) {
          while (res.next()) {
            String pktable = res.getString("PKTABLE_NAME");
            if (!this.tables.containsKey(pktable))
              // DB2 allows defining table alias - the FK definition might point to the alias - skip
              continue;
            tables.get(res.getString("FKTABLE_NAME")).getOrCreateFk(pktable)
                .set(res.getString("FKCOLUMN_NAME"), res.getShort("KEY_SEQ"));
          }
        }
      } catch (Exception e) {
        logger.log(Level.WARNING, "Error gathering keys", e);
      }
      try (ResultSet res = md.getColumns(null, schema, table, null)) {
        while (res.next()) {
          MdTable t = tables.get(res.getString("TABLE_NAME"));

          // column might be from a view
          if (t == null)
            continue;

          Column col = new Column();
          col.name = res.getString("COLUMN_NAME");
          // unused: col.type = res.getInt("DATA_TYPE");
          col.typeName = res.getString("TYPE_NAME");
          // col.columnSize = res.getInt("COLUMN_SIZE");
          col.required = DatabaseMetaData.columnNoNulls == res.getInt("NULLABLE");
          col.readOnly = "YES".equals(res.getString("IS_AUTOINCREMENT"));
          // H2 does not have: col.isGenerated = res.getString("IS_GENERATEDCOLUMN");

          t.columns.add(col);
        }
      }

      // If metadata had no info about the table,
      // try to get metadata from querying data
      MdTable t = tables.get(table);
      if (t.columns.isEmpty()) {
        try (Statement stmt = con.createStatement()) {
          stmt.setMaxRows(1);
          try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {
            for (int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
              Column c = new Column();
              c.name = rs.getMetaData().getColumnName(col);
              c.typeName = rs.getMetaData().getColumnTypeName(col);
              t.columns.add(c);
            }
            break;
          }
        } catch (Throwable ex) {
          logger.log(Level.WARNING,
              "Error gathering table metadata from data query for table " + table, ex);
        }
      }
    }

  }

  /**
   * get the schema the DB connection points to. Some JDBC drivers require workarounds
   */
  String getSchema(Connection con, String url) {

    if (url.startsWith("jdbc:jtds:") || url.startsWith("jdbc:sqlserver")) {
      for (String part : url.split(";"))
        if (part.startsWith("SCHEMA="))
          return part.substring("SCHEMA=".length());
      return "dbo";
    }

    try {
      String res = con.getSchema();

      if (res == null)
        if (url.startsWith("jdbc:postgresql:"))
          throw new SQLException("schema must not be null");

      return res;
    } catch (SQLException ignore) {
      for (String part : url.split(";"))
        if (part.startsWith("SCHEMA="))
          return part.substring("SCHEMA=".length());

      // For Sybase ASE, the default schema is the username
      if (url.startsWith("jdbc:jtds:sybase"))
        try {
          return con.getMetaData().getUserName();
        } catch (SQLException e) {
          // ignore
        }

      return "PUBLIC";
    }
  }


  /**
   * delegate to tables
   */
  @Override
  public String toString() {
    return tables.toString();
  }

  /**
   * get table metadata
   */
  public Map<String, Object> getTables(String parent) {
    Map<String, Object> res = new LinkedHashMap<>();
    for (MdTable t : tables.values()) {
      Map<String, Object> schema = new LinkedHashMap<>();
      schema.put("name", t.name);
      schema.put("ID", parent + "/" + Escape.encodeTableOrColumnName(t.name));
      schema.put("parent", parent);
      schema.put("type", "object");

      Map<String, Object> properties = new LinkedHashMap<>();
      schema.put("properties", properties);

      if (t.columns != null) {

        List<String> required = new ArrayList<String>();
        for (Column column : t.columns)
          if (column.required && !column.readOnly)
            required.add(column.name);
        if (!required.isEmpty())
          schema.put("required", required);
        for (Column column : t.columns) {
          String pk = column.name;
          Map<String, Object> pkm = new LinkedHashMap<>();
          properties.put(pk, pkm);
          pkm.put("ID", schema.get("ID") + "/" + Escape.encodeTableOrColumnName(pk));
          pkm.put("name", pk);
          pkm.put("parent", schema.get("ID"));
          if (column.readOnly)
            pkm.put("readOnly", true);
          for (Column tc : t.columns)
            if (tc.name.equals(pk)) {
              String type = "string";
              type = convert(tc.typeName);
              pkm.put("dbType", tc.typeName);
              if ("date".equals(type)) {
                pkm.put("widget", "date");
                pkm.put("style", ImmutableMap.of("width", "180px"));
                pkm.put("type", "string");
              } else
                pkm.put("type", type);
            }
          if (t.pk.col.contains(pk)) {
            pkm.put("pkpos", t.pk.col.indexOf(pk));
            pkm.put("createOnly", true);
          }
          for (TableAndKey e : t.fks.list) {
            if (e.key.col.contains(pk)) {
              // within the fk key group, this is the pos-th key
              int pos = e.key.col.indexOf(pk);
              String x = tables.get(e.table).pk.col.get(pos);
              pkm.put("ref", parent + "/" + e.table + "/" + x);
              pkm.put("displayWith", "fk");
            }
          }
        }
      }
      res.put(t.name, schema);
    }
    return res;
  }

  /**
   * convert SQL to json schema type. Note that we use the type "date". This is not a legal JSON
   * type. Choosing this type here causes it to be replaced with "string" in getTables; widget is
   * set to date instead causing the UI to display a date picker.
   */
  static String convert(String typeName) {
    if (typeName == null)
      return null;

    // Postgres returns lower case types
    typeName = typeName.toUpperCase();

    if (typeName.endsWith(" UNSIGNED"))
      typeName = typeName.substring(0, typeName.length() - " UNSIGNED".length());

    switch (typeName) {
      case "CHAR":
        return "string";
      case "CHARACTER VARYING": // H2
      case "STRING": // H2
      case "VARCHAR2": // ORCL
      case "VARCHAR":
        return "string";
      case "CHARACTER LARGE OBJECT": // H2
      case "CLOB":
        return "string";
      case "BINARY VARYING": // H2
      case "LONGBLOB": // MySQL
      case "BLOB":
        return "string";
      case "LONGVARCHAR":
        return "string";
      case "NUMERIC":
        return "number";
      case "DECIMAL":
        return "number";
      case "BIT":
        return "boolean";
      case "BOOL":
        return "boolean";
      case "BOOLEAN":
        return "boolean";
      case "TINYINT":
        return "number";
      case "SMALLINT":
        return "number";
      case "INTEGER":
        return "number";
      case "BIGINT":
        return "number";
      case "REAL":
        return "number";
      case "FLOAT":
        return "number";
      case "DOUBLE":
        return "number";
      case "BINARY":
        return "string";
      case "VARBINARY":
        return "string";
      case "LONGVARBINARY":
        return "string";
      case "DATE":
        return "date";
      case "TIME":
        // postgres time like 12:34:56
        return "string";
      case "DATETIME": // Sybase
        return "date";
      case "TIMESTAMP":
        return "date";
      case "TIMESTAMPTZ":
        return "date";
      case "INT2": // Postgres
      case "INT4": // Postgres
      case "INT8": // Postgres
      case "FLOAT4": // Postgres
      case "FLOAT8": // Postgres
        return "number";
      case "ENUM": // MySQL
      case "LONGTEXT": // MySQL
      case "MEDIUMTEXT": // MySQL
      case "TEXT": // Postgres
        return "string";
      case "SERIAL": // Postgres
        return "number";
      case "BYTEA":
        return "binary";
      case "BPCHAR": // Postgres
        return "string";
      case "JSON": // Postgres
        return "string";
      case "JSONB": // Postgres
        return "string";
      case "POINT": // Postgres
        return "string";
      case "NVARCHAR": // Sybase
      case "NCHAR": // Sybase
        return "string";
      case "INT": // Sybase
      case "INT IDENTITY": // Sybase
      case "MONEY": // Sybase
        return "number";
      case "NUMBER": // ORCL
        return "number";
    }

    if (typeName.startsWith("VARCHAR(") && typeName.endsWith(")"))
      return "string";

    logger.severe("Type unknown: " + typeName);
    return "string";
  }
}
