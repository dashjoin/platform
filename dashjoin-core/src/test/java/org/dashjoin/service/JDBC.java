package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.service.QueryEditor.QueryColumn;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * test class we use when testing a new database for the first time. Not a formal unit test. Simply
 * connects to the DB via JDBC and tests the metadata extraction and a query
 */
public class JDBC {

  public static void main(String[] args) throws Exception {
    SQLDatabase db = new SQLDatabase() {
      @Override
      public Connection getConnection(QueryMeta meta) throws SQLException {
        return _cp.getConnection();
      }
    };

    db.ID = "ID";

    // SQL Server
    db.url = "jdbc:jtds:sqlserver://localhost:1433";
    db.username = "sa";
    db.password = "...";

    // MySQL
    db.url = "jdbc:mariadb://...:3306/northwind";
    db.username = "admin";
    db.password = "...";

    // Oracle
    db.url = "jdbc:oracle:thin:@...:1521/ORCL";
    db.username = "admin";
    db.password = "...";

    // MariaDB
    db.url = "jdbc:mariadb://...:3306/northwind";
    db.username = "admin";
    db.password = "...";

    // SQLLite
    db.url = "jdbc:sqlite:C:/tmp/chinook.db";

    // DB2
    db.url = "jdbc:db2://localhost:50000/testdb";
    db.username = "db2inst1";
    db.password = "...";

    // Access
    db.url = "jdbc:ucanaccess://C:/tmp/Books2010.accdb";

    // Excel
    db.url = "jdbc:excel://C:/tmp/w.xls";
    db.url = "jdbc:excel://C:/tmp/w.csv";

    // HANA
    db.url = "jdbc:sap://...:39015/";
    db.username = "SYSTEM";
    db.password = "...";

    // Calcite
    db.url = "jdbc:calcite:model=model.json";

    // SQL Server - new Microsoft JDBC driver (not JTDS)
    db.url = "jdbc:sqlserver://hostname:1433;databaseName=testdb;";
    db.username = "sqlserver";
    db.password = "...";

    Map<String, Object> res = db.connectAndCollectMetadata();
    for (Object e : res.values())
      System.out.println(e);

    db.tables = new ObjectMapper().convertValue(of("tables", res), SQLDatabase.class).tables;

    for (QueryColumn ci : db.getMetadata("select EMP.I from EMP")) {
      System.out.println(ci.columnID);
      System.out.println(ci.keyTable);
    }
  }
}
