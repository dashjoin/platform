package org.dashjoin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.SearchResult;
import org.dashjoin.service.Metadata.Column;
import org.dashjoin.service.Metadata.Key;
import org.dashjoin.service.Metadata.MdTable;
import org.dashjoin.service.ddl.SchemaChange;

/**
 * fake database used for tests in the polymorphism DB tests and schema change tests
 */
public class TestDatabase extends AbstractDatabase {

  public static void init() {
    data = new HashMap<>();
    schema = new NoNullMap<>();
  }

  static Map<String, List<Map<String, Object>>> data = new HashMap<>();

  static class Col {
    Col(String type) {
      this.type = type;
    }

    public String type;
  }

  static class NoNullMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = 1L;

    @Override
    public V put(K k, V v) {
      if (k == null || v == null)
        throw new NullPointerException();
      return super.put(k, v);
    }

    public V create(K k, V v) {
      if (get(k) != null)
        throw new RuntimeException("key already exists: " + k);
      return put(k, v);
    }

    public V delete(K k) {
      if (get(k) == null)
        throw new RuntimeException("key does not exists: " + k);
      return remove(k);
    }
  }

  static NoNullMap<String, NoNullMap<String, Col>> schema = new NoNullMap<>();

  @Override
  public SchemaChange getSchemaChange() {
    return new SchemaChange() {

      @Override
      public void renameTable(String table, String newName) throws Exception {
        schema.create(newName, schema.delete(table));
      }

      @Override
      public void renameColumn(String table, String column, String newName) throws Exception {
        NoNullMap<String, Col> t = schema.get(table);
        t.create(newName, t.delete(column));
      }

      @Override
      public void dropTable(String table) throws Exception {
        schema.delete(table);
      }

      @Override
      public void dropColumn(String table, String column) throws Exception {
        schema.get(table).delete(column);
      }

      @Override
      public void createTable(String table, String keyName, String keyType) throws Exception {
        NoNullMap<String, Col> t = new NoNullMap<>();
        if (keyName != null)
          t.put(keyName, new Col(keyType));
        else {
          t.put("ID", new Col("number"));
          t.put("name", new Col("string"));
        }
        schema.create(table, t);
      }

      @Override
      public void createColumn(String table, String columnName, String columnType)
          throws Exception {
        schema.get(table).create(columnName, new Col(columnType));
      }

      @Override
      public void alterColumn(String table, String column, String newType) throws Exception {
        schema.get(table).get(column).type = newType;
      }
    };
  }

  /**
   * same field as in SQLDatabase - make sure password property definition case includes both
   * classes
   */
  public String password;

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return null;
  }

  void check(Table m, Map<String, Object> record) {
    if (!tables.containsKey(m.name))
      throw new RuntimeException("Table not found: " + m.name);
    if (record != null)
      for (String col : record.keySet())
        if (!tables.get(m.name).properties.keySet().contains(col))
          throw new RuntimeException("Column not found: " + m.name + "." + col);
    if (!data.containsKey(m.name))
      data.put(m.name, new ArrayList<>());
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    check(s, arguments);
    List<Map<String, Object>> res = new ArrayList<>();
    for (Map<String, Object> row : data.get(s.name))
      if (arguments == null || row.get("ID").equals(arguments.get("ID")))
        res.add(row);
    return res;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    check(m, object);
    if (read(m, object) != null)
      throw new Exception("pk violation");
    data.get(m.name).add(object);
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    check(s, search);
    for (Map<String, Object> row : data.get(s.name))
      if (row.get("ID").equals(search.get("ID")))
        return row;
    return null;
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    check(schema, search);
    check(schema, object);
    if (read(schema, search) != null)
      read(schema, search).putAll(object);
    return false;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    check(s, search);
    for (Map<String, Object> row : data.get(s.name))
      if (row.get("ID").equals(search.get("ID"))) {
        data.get(s.name).remove(row);
        return true;
      }
    return false;
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    return null;
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    Metadata meta = new Metadata();
    for (Entry<String, NoNullMap<String, Col>> entry : schema.entrySet()) {
      MdTable prj = new MdTable(entry.getKey());
      prj.pk = new Key();
      prj.pk.col.add("ID");

      prj.columns = new ArrayList<>();

      for (Entry<String, Col> e : entry.getValue().entrySet()) {
        Column col = new Column();
        col.name = e.getKey();
        col.typeName = e.getValue().type;
        prj.columns.add(col);
      }
      meta.tables.put(entry.getKey(), prj);
    }
    return meta.getTables(ID);
  }

  @Override
  public void close() throws Exception {}

  @Override
  public QueryEditorInternal getQueryEditor() {
    return null;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return null;
  }

  @Override
  public List<SearchResult> search(@Context SecurityContext sc, String search, Integer limit)
      throws Exception {
    // disable search so tests with this DB do not influence other search results
    return new ArrayList<>();
  }
}
