package org.dashjoin.service;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * oracle and sql server do not provide resultSet.getTableName(). This class provides the default
 * implementation for other database which simple delegates to resultSet.getTableName()
 */
public class TableName {

  /**
   * create the right instance for computing the table name
   */
  public static TableName create(String url, String query) throws SQLException {
    if (url.startsWith("jdbc:jtds:") || url.startsWith("jdbc:oracle:"))
      return new JTDSTableName(query);
    if (url.startsWith("jdbc:postgresql"))
      return new PostgresTableName(query);
    return new TableName();
  }

  /**
   * simply delegate to meta.getTableName()
   */
  public String getTableName(ResultSetMetaData meta, int column) throws SQLException {
    return meta.getTableName(column);
  }

  /**
   * simply delegate to meta.getColumnLabel()
   */
  public String getColumnLabel(ResultSetMetaData meta, int column) throws SQLException {
    return meta.getColumnLabel(column);
  }

  /**
   * base implementation which handles query parsing in the constructor
   */
  public static class AbstractTableName extends TableName {

    /**
     * parsed query
     */
    PlainSelect body;

    /**
     * construct the instance and parse the query so we can lookup the table info
     */
    public AbstractTableName(String query) throws SQLException {
      try {
        Select select = (Select) CCJSqlParserUtil.parse(query);
        body = (PlainSelect) select.getSelectBody();
      } catch (JSQLParserException e) {
        throw new SQLException(e);
      }
    }
  }

  /**
   * table name implementation for jtds and oracle
   */
  public static class PostgresTableName extends AbstractTableName {

    public PostgresTableName(String query) throws SQLException {
      super(query);
    }

    @Override
    public String getColumnLabel(ResultSetMetaData meta, int i) throws SQLException {
      String label = meta.getColumnLabel(i);
      if (i - 1 < body.getSelectItems().size()) {
        SelectItem selex = body.getSelectItems().get(i - 1);
        if (selex instanceof SelectExpressionItem)
          if (((SelectExpressionItem) selex).getExpression() instanceof Function) {
            Function f = (Function) ((SelectExpressionItem) selex).getExpression();
            if (f.getName().toLowerCase().equals(label))
              // postgres returns label "min" for min(x)
              return f.toString();
          }
      }
      return label;
    }
  }

  /**
   * table name implementation for jtds and oracle
   */
  public static class JTDSTableName extends AbstractTableName {

    public JTDSTableName(String query) throws SQLException {
      super(query);
    }

    /**
     * lookup the table name in the parsed query. note that this requires all selects to be written
     * as table.column, also no select * is allowed
     */
    @Override
    public String getTableName(ResultSetMetaData meta, int i) throws SQLException {
      String tablename = meta.getTableName(i);
      if ("".equals(tablename))
        if (i - 1 < body.getSelectItems().size()) {
          SelectItem selex = body.getSelectItems().get(i - 1);
          if (selex instanceof SelectExpressionItem)
            if (((SelectExpressionItem) selex).getExpression() instanceof Function)
              return tablename;
          String sel = "" + selex;
          if (sel.split("\\.").length == 2)
            tablename = SQLDatabase.s(sel.split("\\.")[0]);
        }
      return tablename;
    }
  }
}
