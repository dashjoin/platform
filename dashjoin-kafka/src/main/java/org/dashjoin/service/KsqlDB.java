package org.dashjoin.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Metadata.Column;
import org.dashjoin.service.Metadata.Key;
import org.dashjoin.service.Metadata.MdTable;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.QueryColumn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * KsqlDB implementation
 */
public class KsqlDB extends SQLDatabase {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments, Integer limit)
      throws SQLException {
    String tableName = "";
    try {
      Select select = (Select) CCJSqlParserUtil.parse(info.query);
      PlainSelect body = (PlainSelect) select.getSelectBody();
      tableName = ((net.sf.jsqlparser.schema.Table) body.getFromItem()).getName();
    } catch (JSQLParserException error) {
      throw new SQLException(error);
    }
    List<Map<String, Object>> res = new ArrayList<>();
    try {
      JsonNode o = call("query", prepare(info.query));
      String schema = o.get(0).get("header").get("schema").asText();
      JsonNode array = o.get(1).get("row").get("columns");
      Map<String, Object> row = new LinkedHashMap<>();
      int idx = 0;
      for (String col : schema.split(",")) {
        String colName = col.split("`")[1];
        row.put(tableName + "." + colName, array.get(idx));
        idx++;
      }
      res.add(row);
    } catch (IOException e) {
      throw new SQLException(e);
    }
    return res;
  }

  static String prepare(String q) {
    return q.replace("\"", "").replace("\n", " ") + ";";
  }

  @Override
  List<QueryColumn> getMetadata(String query) throws Exception {

    Select select = (Select) CCJSqlParserUtil.parse(query);
    PlainSelect body = (PlainSelect) select.getSelectBody();
    Map<Col, String> map = new HashMap<>();
    SQLEditor.parseWhere(true, map, body.getWhere(), body);

    List<QueryColumn> res = new ArrayList<>();
    for (SelectItem si : body.getSelectItems()) {
      net.sf.jsqlparser.schema.Column col =
          (net.sf.jsqlparser.schema.Column) ((SelectExpressionItem) si).getExpression();
      QueryColumn c = new QueryColumn();
      c.database = this.ID;

      String tn = col.getTable().getName();
      String cn = col.getColumnName();

      if (tn.startsWith("\""))
        tn = tn.substring(1, tn.length() - 1);
      if (cn.startsWith("\""))
        cn = cn.substring(1, cn.length() - 1);

      c.col = Col.col(tn, cn);
      c.displayName = cn;
      c.where = map.get(c.col);
      res.add(c);
    }

    return res;
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    List<Map<String, Object>> ret = new ArrayList<>();
    for (Property p : s.properties.values())
      if (p.pkpos != null) {
        // TODO: query requires existing key
        ret.add(read(s, ImmutableMap.of(p.name, "4a7c7b41")));
      }
    return ret;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws SQLException {
    throw new SQLException("Stream tables are read only");
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws SQLException {
    try {
      for (Property p : s.properties.values())
        if (p.pkpos != null) {
          JsonNode o = call("query",
              "select * from " + s.name + " where " + p.name + "='" + search.get(p.name) + "';");
          String schema = o.get(0).get("header").get("schema").asText();
          JsonNode array = o.get(1).get("row").get("columns");
          Map<String, Object> row = new LinkedHashMap<>();
          int idx = 0;
          for (String col : schema.split(",")) {
            String colName = col.split("`")[1];
            row.put(colName, array.get(idx));
            idx++;
          }
          return row;
        }
      return null;
    } catch (IOException e) {
      throw new SQLException(e);
    }
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws SQLException {
    throw new SQLException("Stream tables are read only");
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws SQLException {
    throw new SQLException("Stream tables are read only");
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {

    JsonNode res = call("ksql", "LIST TABLES;").get(0);
    Metadata meta = new Metadata();
    for (JsonNode table : res.get("tables")) {
      String name = table.get("name").asText();
      JsonNode fields =
          call("ksql", "DESCRIBE " + name + ";").get(0).get("sourceDescription").get("fields");
      MdTable t = new MdTable(name);
      t.columns = new ArrayList<>();
      for (JsonNode field : fields) {
        if (field.get("type") != null && field.get("type").asText().equals("KEY")) {
          t.pk = new Key();
          t.pk.col.add(field.get("name").asText());
        }
        Column e = new Column();
        t.columns.add(e);
        e.name = field.get("name").asText();
        e.typeName =
            field.get("schema").get("type").asText().equals("STRING") ? "VARCHAR" : "INTEGER";
      }
      meta.tables.put(name, t);
    }
    return meta.getTables(ID);
  }

  @Override
  public void close() throws Exception {}

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new KsqlDBEditor(services, this);
  }

  JsonNode call(String service, String ksql) throws IOException {
    HttpURLConnection con = (HttpURLConnection) new URL(url + service).openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/vnd.ksql.v1+json");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    try (OutputStream os = con.getOutputStream()) {
      os.write(("{\"ksql\": \"" + ksql + "\"}").getBytes("utf-8"));
    }
    try (InputStream is = con.getInputStream()) {
      return objectMapper.readTree(is);
    } catch (IOException e) {
      try (InputStream is = con.getErrorStream()) {
        throw new IOException(objectMapper.readTree(is).get("message").asText());
      }
    }
  }
}
