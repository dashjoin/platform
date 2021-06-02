package org.dashjoin.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.h2.Driver;

/**
 * excel / CSV read only jdbc driver that loads file data into H2
 */
public class Excel extends Driver {

  /**
   * register JDBC driver
   */
  public Excel() throws SQLException {
    DriverManager.registerDriver(this);
  }

  /**
   * jdbc accept URL callback
   */
  @Override
  public boolean acceptsURL(String url) {
    if (url != null) {
      if (url.startsWith("jdbc:excel:"))
        return true;
    }
    return false;
  }

  /**
   * when we connect, instantiate in mem H2
   */
  @Override
  public Connection connect(String url, Properties info) throws SQLException {
    if (!acceptsURL(url)) {
      return null;
    }
    Connection con = null;
    url = url.substring("jdbc:excel:".length());
    try {
      for (String p : url.split(",")) {
        URL file = new URL(p);
        if (con == null)
          con = DriverManager.getConnection("jdbc:h2:mem:" + new File(file.getFile()).getName(),
              info);
        try {
          Workbook wb = WorkbookFactory.create(file.openStream());
          for (Sheet s : wb) {
            table(con, s.getSheetName(), s);
          }
        } catch (IOException no) {
          Reader in = new InputStreamReader(file.openStream(), StandardCharsets.UTF_8);
          Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
          String noExt = new File(file.getFile()).getName();
          if (noExt.contains("."))
            noExt = noExt.substring(0, noExt.indexOf('.'));
          table(con, noExt, records);
        }
      }
    } catch (IOException e) {
      throw new SQLException(e);
    }
    return con;
  }

  /**
   * load table into H2
   */
  void table(Connection con, String name, Iterable<? extends Iterable<?>> data)
      throws SQLException {

    if (!Character.isAlphabetic(name.charAt(0)))
      name = "T" + name;
    name = name.replace('-', '_');

    List<String> cols = new ArrayList<>();
    List<String> qm = new ArrayList<>();
    for (Iterable<?> row : data) {
      for (Object cell : row) {
        cols.add(cell.toString().replace('-', '_') + " varchar(255)");
        qm.add("?");
      }
      break;
    }
    try (Statement create = con.createStatement()) {
      create.execute("create table " + name + "(" + String.join(",", cols) + ")");
    }
    try (PreparedStatement insert =
        con.prepareStatement("insert into " + name + " values (" + String.join(",", qm) + ")")) {
      for (Iterable<?> row : data) {
        int index = 1;
        for (Object cell : row) {
          insert.setObject(index++, cell + "");
        }
        insert.execute();
      }
    }
  }
}
