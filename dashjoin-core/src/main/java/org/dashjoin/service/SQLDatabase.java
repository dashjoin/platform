package org.dashjoin.service;

import static org.dashjoin.service.QueryEditor.Col.col;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.dbcp2.BasicDataSource;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Aggregation;
import org.dashjoin.service.Data.Choice;
import org.dashjoin.service.Data.ColInfo;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Data.SearchResult;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.QueryColumn;
import org.dashjoin.service.ddl.SQLSchemaChange;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.Loader;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.Template;
import org.h2.tools.RunScript;
import org.jboss.logmanager.Level;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.java.Log;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

/**
 * SQL Reference implementation
 */
@Log
@JsonSchema(required = {"url"}, order = {"url", "username", "password"})
public class SQLDatabase extends AbstractDatabase {

  private static final ObjectMapper om = new ObjectMapper();

  /**
   * JSON config field
   */
  @JsonSchema(choices = {"jdbc:postgresql://localhost:5432/postgres",
      "jdbc:h2:localhost:9092/path/your_database", "jdbc:sqlite:path/your_database.db"
      /*
       * "jdbc:jtds:sqlserver://your_host:1433/your_database;SCHEMA=your_schema",
       * "jdbc:oracle:thin:@your_host:1521/ORCL", "jdbc:mariadb://your_host:3306/your_database",
       * "jdbc:db2://your_host:50000/your_database", "jdbc:sap://your_host:39015/",
       * "jdbc:ucanaccess://path/your_database.accdb", "jdbc:excel:url_to_your_csv_or_xls",
       * "jdbc:hsqldb:hsql://your_host/your_database",
       */}, style = {"width", "600px"})
  public String url;

  /**
   * JSON config field
   */
  public String username;

  /**
   * JSON config field
   */
  @JsonSchema(widget = "password")
  public String password;

  /**
   * JSON config field
   */
  public List<String> initScripts;

  /**
   * JSON config field
   */
  public List<String> excludeTables;

  /**
   * DB connection pool
   */
  BasicDataSource _cp;

  /**
   * dynamic proxy used to intercept and trace JDBC calls
   */
  public class Aspect implements InvocationHandler {

    long start = System.currentTimeMillis();
    Connection con;
    QueryMeta meta;
    java.sql.Statement stmt;
    Integer maxRows;
    Integer queryTimeout;
    String query;
    String error;

    public Aspect(Connection con, QueryMeta meta) {
      this.con = con;
      this.meta = meta;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.equals(
          java.sql.Connection.class.getMethod("prepareStatement", new Class[] {String.class}))) {
        query = (String) args[0];
        stmt = con.prepareStatement(query);
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[] {PreparedStatement.class}, this);
      }
      if (method.equals(java.sql.Connection.class.getMethod("prepareStatement",
          new Class[] {String.class, int.class}))) {
        query = (String) args[0];
        stmt = con.prepareStatement(query, (int) args[1]);
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[] {PreparedStatement.class}, this);
      }
      if (method.getName().equals("createStatement")) {
        stmt = con.createStatement();
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[] {java.sql.Statement.class}, this);
      }
      if (method.getName().equals("execute"))
        query = (String) args[0];
      if (method.getName().equals("setMaxRows"))
        maxRows = (Integer) args[0];
      if (method.getName().equals("setQueryTimeout"))
        queryTimeout = (Integer) args[0];
      if (method.equals(Connection.class.getMethod("close")))
        PerformanceDatabase.add(ID + ": " + query, meta, System.currentTimeMillis() - start,
            maxRows, queryTimeout, error);
      if (method
          .equals(java.sql.Statement.class.getMethod("executeQuery", new Class[] {String.class})))
        query = (String) args[0];

      try {
        return method.invoke(method.getDeclaringClass().equals(Connection.class) ? con : stmt,
            args);
      } catch (InvocationTargetException e) {
        error = e.getCause().toString();
        throw e.getCause();
      }
    }
  }

  public Connection getConnection() throws SQLException {
    return getConnection(null);
  }

  public Connection getConnection(QueryMeta meta) throws SQLException {
    try {
      SQLDatabase x = services.getConfig().getCachedForce(ID, getClass());
      if (x._cp == null)
        throw new Exception("Database not yet initialized: " + ID);
      return (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(),
          new Class[] {Connection.class}, new Aspect(x._cp.getConnection(), meta));
    } catch (SQLException e) {
      throw e;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  /**
   * get reference to DB connection pool
   * 
   * @return
   */
  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {

    if (url.startsWith("jdbc:mysql"))
      throw new Exception("Please use jdbc:mariadb when connecting to MariaDB or MySQL");

    // there are several places that check url.startsWith(), make sure there are no leading spaces
    url = url.trim();

    BasicDataSource ds = new BasicDataSource();
    ds.setUrl(FileSystem.getJdbcUrl(services, url));
    ds.setUsername(username == null ? null : username.trim());
    if (url.startsWith("jdbc:jtds")) {
      ds.setDriverClassName("net.sourceforge.jtds.jdbc.Driver");
      ds.setValidationQuery("select 1");
    }
    if (url.startsWith("jdbc:calcite")) {
      ds.setRollbackOnReturn(false);
    }

    ds.setPassword(password() == null && name.equals("junit") ? password : password());
    if (url.startsWith("jdbc:mariadb"))
      ds.setConnectionProperties(
          "sessionVariables=sql_mode=ANSI_QUOTES;allowPublicKeyRetrieval=true");
    try (Connection con = ds.getConnection()) {
      if (initScripts != null)
        for (String s : initScripts) {
          InputStream ddl = Loader.open(s);
          RunScript.execute(con, new InputStreamReader(ddl, StandardCharsets.UTF_8));
        }
      Metadata meta = new Metadata(con, url, excludeTables);
      Map<String, Object> res = meta.getTables(ID);
      _cp = ds;
      return res;
    }
  }

  /**
   * tuple holder class to return prepared statement with the arguments array - passed to JDBC
   */
  public static class PreparedStmt {
    public String query;
    public Object[] arguments;

    /**
     * handle common casting issues when JSON parameters are fed into SQL
     */
    public void cast(ParameterMetaData pmd) {
      try {
        for (int i = 0; i < pmd.getParameterCount(); i++) {
          int parType = pmd.getParameterType(i + 1);
          if (i < arguments.length) {
            if (parType == Types.INTEGER)
              if (arguments[i] instanceof Long)
                arguments[i] = ((Long) arguments[i]).intValue();
            if (parType == Types.SMALLINT || parType == Types.TINYINT)
              if (arguments[i] instanceof Long)
                arguments[i] = (short) ((long) arguments[i]);
          }
        }
      } catch (SQLException e) {
        // not supported for all DBs
      }
    }
  }

  /**
   * translate Dashjoin query named parameters to JDBC ordered parameter array
   * 
   * TODO: support nested parameters: ${email[0]} or ${contact.id}
   */
  static PreparedStmt prepareStatement(String query, Map<String, Object> arguments) {
    PreparedStmt ps = new PreparedStmt();
    ps.query = query;
    List<Object> args = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\$\\{\\w*\\}");
    for (;;) {
      Matcher m = pattern.matcher(ps.query);
      if (!m.find())
        break;

      if (m.start() > 0)
        if (ps.query.charAt(m.start() - 1) == '\\')
          break;

      String x = m.group().substring(2, m.group().length() - 1);
      args.add(arguments == null ? null : arguments.get(x));
      ps.query = m.replaceFirst("?");
    }
    ps.arguments = args.toArray();

    // handle special case where we have a query with where col = ? and arg is [null]
    if (args.size() == 1 && args.get(0) == null && ps.query.contains(" = ?")) {
      ps.arguments = new Object[0];
      ps.query = ps.query.replace(" = ?", " is null");
    }

    return ps;
  }

  Property property(String table, String column, String type) throws SQLException {
    try {
      if (table != null) {
        Table t = services.getConfig().getSchema(this.ID + "/" + table);
        // query metadata sometimes does not match schema
        // https://github.com/dashjoin/platform/issues/280
        // make sure map value is not null
        if (t != null && t.properties.get(column) != null)
          return t.properties.get(column);
      }
    } catch (Exception e) {
      throw new SQLException(e);
    }
    Property p = new Property();
    p.dbType = type;
    p.type = Metadata.convert(type);
    return p;
  }

  /**
   * Serialize the JDBC result object.
   * 
   * This is a central place to mangle certain native DB types if required. Specifically, shorten
   * CLOB/BLOBs for preview/display to avoid DoS in the backend.
   * 
   * TODO: do we need a way to mark mangled results?
   * 
   * @param rsmd Result metadata
   * @param res ResultSet object
   * @param c Column index to read and serialize
   * @return The value of column c, serialized/mangled if required
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  Object serialize(ResultSetMetaData rsmd, ResultSet res, int c) throws SQLException {

    final long MAX_BLOB_SIZE = 256 * 1024L;
    final long MAX_CLOB_SIZE = 64 * 1024L;

    if (false) { // DEBUG
      String col = rsmd.getColumnName(c);
      System.err
          .println(col + ": " + rsmd.getColumnClassName(c) + " (" + rsmd.getColumnType(c) + ")");
    }

    int ty = rsmd.getColumnType(c);
    Object obj = null;

    if (ty == Types.BINARY) {
      // BINARY is up to 65535 bytes, so safe to get
      obj = res.getObject(c);
    } else if (ty == Types.VARBINARY || ty == Types.LONGVARBINARY || ty == Types.BLOB) {
      Blob blob = res.getBlob(c);
      if (blob == null)
        return null;
      long len = blob.length();
      obj = blob.getBytes(1, (int) Math.min(len, MAX_BLOB_SIZE));
    } else if (ty == Types.CLOB || ty == Types.NCLOB) {
      Clob clob = res.getClob(c);
      if (clob == null)
        return null;
      long len = clob.length();
      obj = clob.getSubString(1, (int) Math.min(len, MAX_CLOB_SIZE));
    } else if (ty == Types.SQLXML) {
      SQLXML clob = res.getSQLXML(c);
      if (clob == null)
        return null;
      obj = clob.getString();
    } else {
      if (url.startsWith("jdbc:sqlite:"))
        if (ty == Types.DATE)
          obj = res.getDate(c);
        else if (ty == Types.TIME)
          obj = res.getTime(c);
        else if (ty == Types.TIMESTAMP)
          obj = res.getTimestamp(c);
        else
          obj = res.getObject(c);
      else
        obj = res.getObject(c);
      if (obj instanceof PGobject) {
        PGobject pg = (PGobject) obj;
        if ("json".equals(pg.getType()) || "jsonb".equals(pg.getType()))
          try {
            obj = om.readValue(pg.getValue(), Object.class);
          } catch (JsonProcessingException e) {
            throw new SQLException(e);
          }
      } else if (obj instanceof PgArray) {
        PgArray arr = (PgArray) obj;
        obj = arr.getArray();
      } else if (obj instanceof BigDecimal) {
        // convert to double
        obj = ((BigDecimal) obj).doubleValue();

        if ((double) obj == Math.round((double) obj)) {
          // natural number, convert to long
          obj = Math.round((double) obj);

          // small natural number, conver to int
          if (Integer.MIN_VALUE < (long) obj && (long) obj < Integer.MAX_VALUE)
            obj = Math.toIntExact((long) obj);
        }
      }
    }

    return obj;
  }

  boolean supportsIlike() {
    if (url.startsWith("jdbc:mariadb:") || url.startsWith("jdbc:jtds:")
        || url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:sqlite")
        || url.startsWith("jdbc:db2:") || url.startsWith("jdbc:oracle:thin:"))
      return false;
    else
      return true;
  }

  @Override
  public List<SearchResult> search(SecurityContext sc, String search, Integer limit)
      throws Exception {
    return search(sc, null, search, limit);
  }

  @Override
  public List<SearchResult> search(SecurityContext sc, Table filter, String search, Integer limit)
      throws Exception {

    long start = System.currentTimeMillis();
    Integer timeout = services.getConfig().getSearchTimeoutMs();

    List<SearchResult> ret = new ArrayList<>();
    search = search.toLowerCase();
    Map<Table, List<String>> tables = new LinkedHashMap<>();

    // make sure all tables are added
    for (Table c : services.getConfig().searchTables(this)) {
      List<String> l = new ArrayList<>();
      tables.put(c, l);

      if (c.properties != null)
        for (Property p : c.properties.values()) {
          // cast column in case it is not a string
          if (supportsIlike())
            l.add("cast(" + q(p.name) + " as VARCHAR) ILIKE '%" + search + "%'");
          else
            l.add(q(p.name) + " LIKE '%" + search + "%'");
        }
    }

    for (Entry<Table, List<String>> e : tables.entrySet()) {
      if (filter != null && !filter.name.equals(e.getKey().name))
        continue;
      try {
        ACLContainerRequestFilter.check(sc, this, e.getKey());
      } catch (NotAuthorizedException ex) {
        continue;
      }
      String sql = "SELECT * FROM " + q(e.getKey().name) + " WHERE "
          + (ACLContainerRequestFilter.hasTenantFilter(sc, e.getKey())
              ? q(e.getKey().tenantColumn) + "=? and (" + String.join(" or ", e.getValue()) + ")"
              : String.join(" or ", e.getValue()));
      try (Connection con = getConnection()) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
          if (limit != null)
            pstmt.setMaxRows(limit - ret.size());

          if (ACLContainerRequestFilter.hasTenantFilter(sc, e.getKey()))
            pstmt.setObject(1, ACLContainerRequestFilter.tenantValue(sc, e.getKey()));

          if (timeout != null)
            pstmt.setQueryTimeout(timeout / 1000);

          try (ResultSet res = pstmt.executeQuery()) {
            ResultSetMetaData md = res.getMetaData();
            while (res.next()) {
              for (int i = 1; i <= md.getColumnCount(); i++) {
                Object s = res.getObject(i);
                if (s != null) {
                  Object[] key = new Object[] {null, null, null, null};
                  for (Property p : e.getKey().properties.values())
                    if (p.pkpos != null)
                      key[p.pkpos] = res.getObject(p.name);

                  Resource url = new Resource();
                  url.database = name;
                  url.table = e.getKey().name;
                  for (Object p : key)
                    if (p != null)
                      url.pk.add(p);

                  if (s.toString().toLowerCase().contains(search)) {
                    ret.add(SearchResult.of(url, md.getColumnName(i), serialize(md, res, i)));
                    if (limit != null && ret.size() >= limit)
                      return ret;
                    break;
                  }
                }
              }
            }
          }
        }
        if (timeout != null)
          if (System.currentTimeMillis() - start > timeout)
            return ret;
      }
    }
    return ret;
  }

  @Override
  public String analytics(QueryMeta info, Table s, List<ColInfo> arguments) {
    List<String> project = new ArrayList<>();

    boolean isGroupBy = false;
    for (ColInfo a : arguments)
      if (a.project)
        if (a.aggregation != null)
          isGroupBy = true;

    for (ColInfo a : arguments) {
      String col = q(a.name);
      if (a.project) {
        String add = col;
        if (a.aggregation != null)
          switch (a.aggregation) {
            case AVG:
              add = "avg(" + col + ")";
              break;
            case COUNT:
              add = "count (" + col + ")";
              break;
            case COUNT_DISTINCT:
              add = "count (distinct " + col + ")";
              break;
            case GROUP_BY:
              add = col;
              break;
            case GROUP_CONCAT:
              add = "group_concat (" + col + ")";
              break;
            case GROUP_CONCAT_DISTINCT:
              add = "group_concat (distinct " + col + ")";
              break;
            case MAX:
              add = "max(" + col + ")";
              break;
            case MIN:
              add = "min(" + col + ")";
              break;
            case STDDEV:
              add = "stddev(" + col + ")";
              break;
            case SUM:
              add = "sum(" + col + ")";
              break;
          }
        else if (isGroupBy)
          add = "count (" + col + ")";

        if (a.alias != null)
          add = add + " as " + q(a.alias);
        project.add(add);
      }
    }

    List<String> where = new ArrayList<>();
    for (ColInfo a : arguments)
      if (a.filter != null) {
        String col = q(a.name);
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
        if (a.arg1 instanceof Date)
          a.arg1 = sm.format((Date) a.arg1);
        if (a.arg2 instanceof Date)
          a.arg2 = sm.format((Date) a.arg2);
        Object arg1 = a.arg1 instanceof String ? "'" + a.arg1 + "'" : a.arg1;
        Object arg2 = a.arg2 instanceof String ? "'" + a.arg2 + "'" : a.arg2;
        switch (a.filter) {
          case BETWEEN:
            if (arg1 != null && arg2 != null)
              where.add(col + " between " + arg1 + " and " + arg2);
            break;
          case EQUALS:
            if (arg1 != null)
              where.add(col + " = " + arg1);
            break;
          case GREATER_EQUAL:
            if (arg1 != null)
              where.add(col + " >= " + arg1);
            break;
          case IS_NOT_NULL:
            where.add(col + " is not null");
            break;
          case IS_NULL:
            where.add(col + " is null");
            break;
          case LIKE:
            if (arg1 != null)
              if (supportsIlike())
                where.add(col + " ILIKE " + arg1);
              else
                where.add(col + " LIKE " + arg1);
            break;
          case NOT_EQUALS:
            if (arg1 != null)
              where.add(col + " <> " + arg1);
            break;
          case SMALLER_EQUAL:
            if (arg1 != null)
              where.add(col + " <= " + arg1);
            break;
        }
      }

    String subselect = info == null ? "" : "(" + info.query + ") as ";
    String select =
        "select " + String.join(", ", project) + " from " + subselect + schema() + q(s.name);

    if (!where.isEmpty())
      select += " where " + String.join(" and ", where);

    List<String> groupBy = new ArrayList<>();
    for (ColInfo a : arguments) {
      String col = q(a.name);
      if (a.aggregation == Aggregation.GROUP_BY)
        groupBy.add(col);
    }
    if (!groupBy.isEmpty())
      select += " group by " + String.join(",", groupBy);

    // System.out.println(select);

    return select + " limit 1000";
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    return readInternal(s, offset, limit, sort, descending, arguments);
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws SQLException {
    if (info.query == null)
      throw new SQLException("Query is empty");
    return query(info, arguments, null);
  }

  protected List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments,
      Integer limit) throws SQLException {
    PreparedStmt ps = prepareStatement(info.query, arguments);

    List<Map<String, Object>> data = new ArrayList<>();
    List<Map<String, Object>> multidata = null;
    try (Connection con = getConnection(info)) {
      try (PreparedStatement pstmt = con.prepareStatement(ps.query)) {
        ps.cast(pstmt.getParameterMetaData());
        if (limit != null)
          pstmt.setMaxRows(limit);
        int idx = 1;
        for (Object x : ps.arguments)
          pstmt.setObject(idx++, x);

        if ("write".equals(info.type)) {
          Map<String, Object> row = new HashMap<>();
          row.put("rowcount", pstmt.executeUpdate());
          data.add(row);
        } else {
          TableName tn = TableName.create(url, ps.query);

          // if no limit is provided, protect against rogue queries that sometime cause
          // extreme CPU / memory load during stmt.execute() already
          if (limit == null) {
            limit = tn.getLimit();
            if (limit == null) {
              limit = 1000;
            }
          }
          try (ResultSet _res = pstmt.executeQuery()) {
            ResultSet res = _res;
            for (;;) {
              ResultSetMetaData m = res.getMetaData();
              while (res.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int c = 1; c <= m.getColumnCount(); c++) {
                  String display = tn.getColumnLabel(m, c);
                  String column = tn.getColumnName(m, c);
                  String table = tn.getTableName(m, c);
                  if (!column.equals(display))
                    row.put(display, serialize(m, res, c));
                  else if (table == null || table.isEmpty())
                    row.put(column, serialize(m, res, c));
                  else
                    row.put(table + "." + column, serialize(m, res, c));
                }
                data.add(row);
              }
              if (pstmt.getMoreResults()) {
                // we have multiple result sets
                if (multidata == null)
                  multidata = new ArrayList<>();
                multidata.add(MapUtil.of("table", data));
                data = new ArrayList<>();
                res = pstmt.getResultSet();
              } else {
                if (multidata != null)
                  // add the last table if we are in multi result set mode
                  multidata.add(MapUtil.of("table", data));
                break;
              }
            }
          }
        }
      }
    }
    return multidata == null ? data : multidata;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws SQLException {

    if (!"read".equals(info.type)) {
      throw new IllegalArgumentException("Can only determine column meta for read queries");
    }

    PreparedStmt ps = prepareStatement(info.query, arguments);
    TableName tn = TableName.create(url, ps.query);

    try (Connection con = getConnection()) {
      try (PreparedStatement pstmt = con.prepareStatement(ps.query)) {
        pstmt.setMaxRows(1);
        int idx = 1;
        for (Object x : ps.arguments)
          pstmt.setObject(idx++, x);

        try (ResultSet res = pstmt.executeQuery()) {
          ResultSetMetaData m = res.getMetaData();
          Map<String, Property> row = new LinkedHashMap<>();
          for (int c = 1; c <= m.getColumnCount(); c++) {
            String display = tn.getColumnLabel(m, c);
            String column = tn.getColumnName(m, c);
            String table = tn.getTableName(m, c);
            String type = m.getColumnTypeName(c);
            if (!column.equals(display))
              row.put(display, property(table, column, type));
            else if (table == null || table.isEmpty())
              row.put(column, property(table, column, type));
            else
              row.put(table + "." + column, property(table, column, type));
          }
          return row;
        }
      }
    }
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws SQLException {
    try (Connection con = getConnection()) {
      try (PreparedStatement stmt =
          con.prepareStatement(getInsertSQL(m), java.sql.Statement.RETURN_GENERATED_KEYS)) {
        int i = 1;
        for (Object o : getArgs(m, object))
          stmt.setObject(i++, o);
        stmt.executeUpdate();

        int idx = 0;
        try (ResultSet keys = stmt.getGeneratedKeys()) {
          while (keys.next()) {
            Object key = keys.getObject(1);
            for (Property p : m.properties.values()) {
              if (p.pkpos != null && p.readOnly != null)
                if (idx == p.pkpos) {
                  object.put(p.name, key);
                }
            }
            idx++;
          }
        }
      }
    }
  }

  @Override
  public void create(Table m, List<Map<String, Object>> objects) throws Exception {
    try (Connection con = getConnection()) {
      if (url.startsWith("jdbc:sqlite:"))
        // despite using a batch, sqlite seems to do transactions for each row - turn off autocommit
        con.setAutoCommit(false);
      try (PreparedStatement stmt = con.prepareStatement(getInsertSQL(m))) {
        for (Map<String, Object> object : objects) {
          int i = 1;
          for (Object o : getArgs(m, object))
            stmt.setObject(i++, o);
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
      if (url.startsWith("jdbc:sqlite:")) {
        con.commit();
        con.setAutoCommit(true);
      }
    }
  }

  String getInsertSQL(Table m) {
    String insert = "insert into " + schema() + q(m.name) + " (";
    for (String k : m.properties.keySet()) {
      // ignore autoinc cols
      if (Boolean.TRUE.equals(m.properties.get(k).readOnly))
        continue;
      insert = insert + q(k) + ",";
    }
    insert = insert.substring(0, insert.length() - 1);
    insert = insert + ") values (";
    for (String k : m.properties.keySet()) {
      if (Boolean.TRUE.equals(m.properties.get(k).readOnly))
        continue;
      insert = insert + "?,";
    }
    insert = insert.substring(0, insert.length() - 1);
    insert = insert + ")";

    if (log.isLoggable(Level.DEBUG))
      log.fine("insert=" + insert);

    return insert;
  }

  List<Object> getArgs(Table m, Map<String, Object> object) {
    List<Object> args = new ArrayList<>();
    for (String k : m.properties.keySet()) {
      if (Boolean.TRUE.equals(m.properties.get(k).readOnly))
        continue;
      args.add(object.get(k));
    }
    return args;
  }

  @Override
  public void merge(Table m, List<Map<String, Object>> objects) throws Exception {
    try {
      create(m, objects);
    } catch (Exception e) {
      // we assume that batch processing is transactional
      super.merge(m, objects);
    }
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws SQLException {
    List<Map<String, Object>> res = readInternal(s, null, null, null, false, search);
    if (res.isEmpty())
      return null;
    else
      return res.get(0);
  }

  List<Map<String, Object>> readInternal(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> search) throws SQLException {
    Integer timeout = services.getConfig().getAllTimeoutMs();
    List<Map<String, Object>> ret = new ArrayList<>();
    try (Connection con = getConnection()) {
      String select = "select * from " + schema() + q(s.name);
      List<Object> args = new ArrayList<>();
      if (search != null && !search.isEmpty()) {
        select = select + " where ";
        for (String k : search.keySet()) {
          // for array columns, treat equality to be array contains
          Object value = search.get(k);
          Object firstValue = null;
          if (value instanceof PGobject)
            try {
              value = om.readValue(((PGobject) value).getValue(), Object.class);
            } catch (Exception e) {
              throw new SQLException(e);
            }
          if (value instanceof List)
            if (((List<?>) value).size() == 1)
              firstValue = ((List<?>) value).get(0);
          if (value.getClass().isArray())
            if (Array.getLength(value) == 1)
              firstValue = Array.get(value, 0);
          if (url.startsWith("jdbc:postgresql:") && s.properties.get(k).dbType.equals("jsonb")) {
            select = select + "\"" + k + "\"::jsonb ??" + " ? and ";
            if (firstValue != null)
              args.add(firstValue);
            else
              args.add(value);
          } else if (url.startsWith("jdbc:postgresql:")
              && s.properties.get(k).type.equals("array")) {
            select = select + "? = any(" + q(k) + ") and ";
            if (firstValue != null)
              args.add(firstValue);
            else
              args.add(value);
          } else {
            select = select + q(k) + "=? and ";
            args.add(search.get(k));
          }
        }
        select = select.substring(0, select.length() - "and ".length());
      }
      if (sort != null)
        select = select + " order by " + q(sort) + (descending ? " desc" : "");
      if (url.startsWith("jdbc:jtds:") || url.startsWith("jdbc:sqlserver")) {
        // SQL server uses "select * from table order by x offset 5 rows fetch next 5 rows only
        if (offset != null) {
          if (sort == null) {
            // OFFSET always needs an ORDER BY
            // In case no sorting is active, add the primary key order
            String orderBy = null;
            for (Property p : s.properties.values()) {
              orderBy = p.name;
              if (p.pkpos != null)
                break;
            }
            select = select + " order by " + orderBy;
          }
          select = select + " offset " + offset + " rows fetch next "
              + (limit == null ? Integer.MAX_VALUE : limit) + " rows only";
        }
      } else {
        if (url.startsWith("jdbc:db2:") || url.startsWith("jdbc:sqlite")
            || url.startsWith("jdbc:mariadb"))
          // DB2 offset only works with limit
          select = select + " limit " + (limit == null ? Integer.MAX_VALUE : limit);
        if (offset != null)
          select =
              select + " offset " + offset + (url.startsWith("jdbc:oracle:thin:") ? " rows" : "");
      }

      if (log.isLoggable(Level.DEBUG))
        log.fine("select=" + select);
      try (PreparedStatement stmt = con.prepareStatement(select)) {
        if (limit != null)
          stmt.setMaxRows(limit);

        if (timeout != null)
          stmt.setQueryTimeout(timeout / 1000);

        int i = 1, rows = 0;
        for (Object o : args)
          stmt.setObject(i++, o);
        try (ResultSet res = stmt.executeQuery()) {
          ResultSetMetaData m = res.getMetaData();
          while (res.next()) {
            if (limit != null && rows++ >= limit)
              break;
            Map<String, Object> row = new LinkedHashMap<>();
            for (int c = 1; c <= m.getColumnCount(); c++) {
              row.put(m.getColumnName(c), serialize(m, res, c));
            }
            ret.add(row);
          }
        }
      }
    }
    return ret;
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws SQLException {
    try (Connection con = getConnection()) {
      boolean set = false;
      List<Object> args = new ArrayList<>();
      String update = "update " + schema() + q(schema.name) + " set ";

      for (String k : schema.properties.keySet()) {
        if (search.keySet().contains(k))
          continue;

        // do not set omitted columns to null
        if (!object.containsKey(k))
          continue;

        update = update + q(k) + "=?,";
        set = true;

        Object val = object.get(k);
        args.add(val);
      }

      if (!set)
        // nothing to set - noop
        return true;

      update = update.substring(0, update.length() - 1);
      update = update + " where ";
      for (String k : search.keySet()) {
        update = update + q(k) + "=? and ";
        args.add(search.get(k));
      }
      update = update.substring(0, update.length() - "and ".length());

      try (PreparedStatement stmt = con.prepareStatement(update)) {
        int i = 1;
        for (Object o : args)
          stmt.setObject(i++, o);
        int count = stmt.executeUpdate();
        if (count == 0)
          return false;
      }
    } catch (SQLException e) {
      // append the record that we tried to write to the exception
      throw new SQLException(e.getMessage() + ": " + object, e);
    }
    return true;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws SQLException {
    try (Connection con = getConnection()) {
      String select = "delete from " + schema() + q(s.name) + " where ";
      List<Object> args = new ArrayList<>();
      for (String k : search.keySet()) {
        Property prop = s.properties.get(k);
        if (prop != null && prop.pkpos != null) {
          select = select + q(k) + "=? and ";
          args.add(search.get(k));
        }
      }
      select = select.substring(0, select.length() - "and ".length());

      if (log.isLoggable(Level.DEBUG))
        log.fine("delete=" + select);

      try (PreparedStatement stmt = con.prepareStatement(select)) {
        int i = 1;
        for (Object o : args)
          stmt.setObject(i++, o);
        int count = stmt.executeUpdate();
        if (count == 0)
          return false;
      }
    }
    return true;
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    List<String> res = new ArrayList<>();
    Statement parsed = CCJSqlParserUtil.parse(query);

    if (parsed instanceof Insert)
      return Arrays.asList(s(((Insert) parsed).getTable().getName()));
    if (parsed instanceof Update)
      return Arrays.asList(s(((Update) parsed).getTable().getName()));
    if (parsed instanceof Delete)
      return Arrays.asList(s(((Delete) parsed).getTable().getName()));
    if (parsed instanceof CreateView)
      return getTablesInQuery(((CreateView) parsed).getSelect().toString());

    SelectBody body = ((Select) parsed).getSelectBody();
    res.add(s(((net.sf.jsqlparser.schema.Table) ((PlainSelect) body).getFromItem()).getName()));
    if (((PlainSelect) body).getJoins() != null)
      for (Join j : ((PlainSelect) body).getJoins())
        res.add(s(((net.sf.jsqlparser.schema.Table) j.getRightItem()).getName()));
    return res;
  }

  static String s(String name) {
    if (name.startsWith("\"") && name.endsWith("\""))
      name = name.substring(1, name.length() - 1);
    return name;
  }

  @Override
  public void close() throws Exception {
    if (_cp != null)
      _cp.close();
  }

  /**
   * returns the primary key of table, null if the table does not exist
   */
  public Col getPk(String table) {
    // TODO: support composite keys
    Table t = tables.get(table);
    if (t == null || t.properties == null)
      return null;
    for (Property p : t.properties.values())
      if (p.pkpos != null)
        return col(table, p.name);
    return null;
  }

  /**
   * given a pair of pks, returns the fk that links them, null otherwise
   */
  public Col getFk(String from, String to) throws Exception {
    if (tables.get(to).properties != null)
      for (Property e : tables.get(to).properties.values())
        if (e.ref != null) {
          Table fkSchema = services.getConfig().getSchema(e.ref);
          if (from.equals(fkSchema.name))
            // TODO: support composite keys
            return col(to, e.name);
        }
    return null;
  }

  /**
   * checks whether the column is a primary key
   */
  boolean isPk(Col pk) throws SQLException {
    Table t = tables.get(pk.table);
    if (t == null || t.properties == null)
      return false;
    Property prop = t.properties.get(pk.column);
    if (prop == null)
      return false;
    return prop.pkpos != null;
  }

  /**
   * if fk is a foreign key, return the referenced pk, null otherwise
   */
  Col getFkRef(Col fk) throws SQLException {
    Table x = tables.get(fk.table);
    if (x == null || x.properties == null)
      return null;
    for (Property e : x.properties.values()) {
      if (e.name.equals(fk.column)) {
        if (e.ref != null) {
          String[] table = e.ref.split("/");
          // TODO: support composite keys
          return col(table[2], table[3]);
        }
      }
    }
    return null;
  }

  /**
   * returns all tables that are related other than the table itself.
   * 
   * out == null: regardless of the direction
   * 
   * out == true: table.fk = related.pk
   * 
   * out == false: table.pk = related.fk
   */
  Set<Table> getRelatedTables(Table table, Boolean out) {

    Set<Table> res = new HashSet<>();
    for (Table test : tables.values()) {
      if (table == test)
        continue;

      if (out == null || out == true)
        if (table.properties != null)
          for (Property fk : table.properties.values())
            if (fk.ref != null) {
              String[] split = fk.ref.split("/");
              if (split[2].equals(test.name))
                res.add(test);
            }
      if (out == null || out == false)
        if (test.properties != null)
          for (Property fk : test.properties.values())
            if (fk.ref != null) {
              String[] split = fk.ref.split("/");
              if (split[2].equals(table.name))
                res.add(test);
            }
    }
    return res;
  }

  protected List<QueryColumn> getMetadata(String query) throws Exception {

    Select select = (Select) CCJSqlParserUtil.parse(query);
    PlainSelect body = (PlainSelect) select.getSelectBody();
    Map<Col, String> map = new HashMap<>();
    SQLEditor.parseWhere(true, map, body.getWhere(), body);

    TableName tn = TableName.create(url, query);
    List<QueryColumn> table = new ArrayList<>();
    try (Connection con = getConnection()) {
      try (java.sql.Statement stmt = con.createStatement()) {
        stmt.setMaxRows(1);
        try (ResultSet res = stmt.executeQuery(query)) {
          ResultSetMetaData meta = res.getMetaData();
          for (int i = 1; i <= meta.getColumnCount(); i++) {
            QueryColumn c = new QueryColumn();
            c.database = this.ID;

            // Set default metadata for col (column/table) and displayName
            // Might be updated from query metadata below
            c.col = col(tn.getTableName(meta, i), tn.getColumnName(meta, i));
            c.displayName = tn.getColumnLabel(meta, i);

            if (i - 1 < body.getSelectItems().size()) {
              SelectItem sei = body.getSelectItems().get(i - 1);
              if (sei instanceof SelectExpressionItem) {
                SelectExpressionItem _s = (SelectExpressionItem) sei;
                Expression expr = _s.getExpression();
                if (expr instanceof Function) {
                  c.groupBy = ((Function) expr).getName().toUpperCase();
                  if (((Function) expr).isDistinct())
                    c.groupBy += " DISTINCT";
                } else if (expr instanceof MySQLGroupConcat) {
                  c.groupBy = "GROUP_CONCAT";
                  if (((MySQLGroupConcat) expr).isDistinct())
                    c.groupBy += " DISTINCT";
                } else {
                  if (body.getGroupBy() != null)
                    for (Expression g : body.getGroupBy().getGroupByExpressions()) {
                      if (g instanceof Column) {
                        Column gg = (Column) g;
                        if (gg.getTable() != null
                            && SQLEditor.equals(gg.getTable().getName(), c.col.table))
                          if (SQLEditor.equals(gg.getColumnName(), c.col.column))
                            c.groupBy = "GROUP BY";
                      }
                    }
                }

                // Get metadata from query if available:
                // This is more accurate and also works for complex functions,
                // where some JDBC (i.e. Postgres) will not return metadata

                // Step 1: if we have a function like COUNT(column),
                // extract the column
                if (expr instanceof Function) {
                  Function f = (Function) expr;
                  Expression e2 =
                      f.getParameters() == null ? null : f.getParameters().getExpressions().get(0);
                  if (e2 instanceof Column)
                    expr = e2;
                } else if (expr instanceof MySQLGroupConcat) {
                  MySQLGroupConcat gr = (MySQLGroupConcat) expr;
                  Expression e2 = gr.getExpressionList().getExpressions().get(0);
                  if (e2 instanceof Column)
                    expr = e2;
                }

                // From the column, extract table and column name
                if (expr instanceof Column) {
                  if (((Column) expr).getTable() != null) {
                    String tab = s(((Column) expr).getTable().getName());
                    String col = s(((Column) expr).getColumnName());
                    c.col = col(tab, col);
                  }
                  if (_s.getAlias() != null)
                    c.displayName = s(_s.getAlias().getName());
                }
              }
            }

            c.where = map.get(c.col);

            c.dbType = meta.getColumnTypeName(i);
            c.type = Metadata.convert(c.dbType);

            // In case this is not a simple column, but an expression,
            // there is no table information.
            // I.e. an aggregation SUM(TABLE.COLUMN)
            if (!c.col.table.equals("")) {

              Table schema = tables.get(c.col.table);
              if (schema != null && schema.properties != null) {
                Property property = schema.properties.get(c.col.column);
                if (property != null)
                  c.columnID = property.ID;
              }

              if (isPk(c.col))
                c.keyTable = c.col.table;
              else {
                Col fk = getFkRef(c.col);
                if (fk != null)
                  c.keyTable = fk.table;
              }
            }

            table.add(c);
          }
        }
      }
    }
    return table;
  }

  public Table getSchema(String table) {
    return tables.get(table);
  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new SQLEditor(services, this);
  }

  @Override
  public SchemaChange getSchemaChange() {
    return new SQLSchemaChange(this);
  }

  @Override
  public List<Choice> keys(Table s, String prefix, Integer limit, Map<String, Object> arguments)
      throws Exception {
    Integer timeout = services.getConfig().getAutocompleteTimeoutMs();
    List<Choice> ret = new ArrayList<>();
    for (Property p : s.properties.values())
      if (p.pkpos != null)
        try (Connection con = getConnection()) {
          String label = Template.sql(this, q(p.name), s.djLabel);
          String select = "select " + q(p.name) + ", " + label + " from " + schema() + q(s.name);
          if (prefix != null)
            // not all DBs support ILIKE, lower(col) causes the index to be useless
            if (supportsIlike())
              select = select + " where " + label + " ILIKE ?";
            else
              select = select + " where " + label + " LIKE ?";

          if (arguments != null && !arguments.isEmpty())
            select = select + " and " + q(arguments.keySet().iterator().next()) + "=?";

          try (PreparedStatement stmt = con.prepareStatement(select)) {
            if (arguments != null && !arguments.isEmpty())
              stmt.setObject(2, arguments.values().iterator().next());
            if (prefix != null)
              stmt.setString(1, prefix + "%");
            if (limit != null)
              stmt.setMaxRows(limit);
            if (timeout != null)
              stmt.setQueryTimeout(timeout / 1000);
            try (ResultSet res = stmt.executeQuery()) {
              while (res.next()) {
                Choice choice = new Choice();
                choice.value = res.getObject(1);
                choice.name = res.getString(2);
                ret.add(choice);
              }
            }
          }
        }
    return ret;
  }

  public String q(String column) {
    if (url.startsWith("jdbc:ucanaccess:"))
      return column;
    return "\"" + column + "\"";
  }

  String schema() {
    if (url.startsWith("jdbc:jtds:") || url.startsWith("jdbc:sqlserver"))
      for (String part : url.split(";"))
        if (part.startsWith("SCHEMA="))
          return part.substring("SCHEMA=".length()) + ".";
    return "";
  }

  @Override
  public String displayUrl() {
    return url;
  }

  public void castArray(Table m, Map<String, Object> object) {
    if (m == null)
      throw new IllegalArgumentException("Unknown table");
    if (m.properties != null && object != null)
      for (Entry<String, Property> p : m.properties.entrySet())
        if ("jsonb".equals(p.getValue().dbType)) {
          Object obj = object.get(p.getKey());
          if ((obj instanceof Map) || (obj instanceof List))
            // value is a json array or object - no cast required
            ;
          else
            // value is a simple type - makes no sense to be stored in a jsonb column
            // and likely caused by jsonata array "unrolling"
            // convert value to [value]
            object.put(p.getKey(), Arrays.asList(obj));
        }
  }

  @Override
  public Object cast(Property p, Object object) {

    if (object instanceof String) {
      String s = (String) object;

      if ("uuid".equals(p.dbType))
        return UUID.fromString((String) object);

      if ("time".equalsIgnoreCase(p.dbType))
        return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(s));

      if ("timetz".equalsIgnoreCase(p.dbType))
        return OffsetTime.from(DateTimeFormatter.ISO_TIME.parse(s));

      if ("date".equalsIgnoreCase(p.dbType) || "datetime".equalsIgnoreCase(p.dbType)
          || "timestamp".equalsIgnoreCase(p.dbType) || "timestamptz".equalsIgnoreCase(p.dbType)) {

        for (DateTimeFormatter f : new DateTimeFormatter[] {
            // 2011-12-03T10:15:30+01:00
            DateTimeFormatter.ISO_DATE_TIME,
            // 2011-12-03
            DateTimeFormatter.ISO_LOCAL_DATE,
            // 2021-11-06 05:20
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]")

        }) {
          try {
            TemporalAccessor accessor = f.parse((String) object);
            try {
              return date(p, Date.from(Instant.from(accessor)));
            } catch (DateTimeException e) {
              try {
                LocalDateTime localDateTime = LocalDateTime.from(accessor);
                ZonedDateTime zonedDateTime =
                    ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
                return date(p, Date.from(Instant.from(zonedDateTime)));
              } catch (DateTimeException e2) {
                LocalDate localDate = LocalDate.from(accessor);
                return date(p,
                    Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
              }
            }
          } catch (Exception e) {
            // ignore and try the other formats
          }
        }
      }
    }

    object = super.cast(p, object);

    if (object instanceof Map<?, ?> || object instanceof List<?>)
      if (url.startsWith("jdbc:postgresql:")) {

        if (p != null && p.dbType != null && p.dbType.startsWith("_"))
          if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            for (Object i : list)
              if (i != null)
                return list.toArray((Object[]) Array.newInstance(i.getClass(), 0));
          }

        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        try {
          jsonObject.setValue(om.writeValueAsString(object));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return jsonObject;
      }

    return object;
  }

  Object date(Property p, Date date) {
    if (url != null && url.startsWith("jdbc:postgresql:") && date != null) {
      if ("date".equalsIgnoreCase(p.dbType))
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if ("timestamp".equalsIgnoreCase(p.dbType))
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      if ("timestamptz".equalsIgnoreCase(p.dbType))
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }
    return date;
  }
}
