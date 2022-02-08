package org.dashjoin.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import org.h2.Driver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * tests some workarounds that are required to support multiple DBs (e.g. need to parse queries for
 * SQL Server since the driver does not support resultSetMetadata.getTable(column))
 */
public class TableNameTest {

  @Test
  public void testTableName() throws Exception {
    Class.forName(Driver.class.getName());
    String url = "jdbc:h2:mem:x";
    String query = "select T.I from T";
    Connection con = DriverManager.getConnection(url);
    con.createStatement().executeUpdate("create table t(i int)");
    ResultSet res = con.createStatement().executeQuery(query);
    TableName tn = TableName.create(url, query);
    Assertions.assertEquals("T", tn.getTableName(res.getMetaData(), 1));

    // this forces meta.getTableName() to be ""
    res = con.createStatement().executeQuery("select count(*) from T");
    url = "jdbc:jtds:";
    tn = TableName.create(url, query);
    Assertions.assertEquals("T", tn.getTableName(res.getMetaData(), 1));
  }
}
