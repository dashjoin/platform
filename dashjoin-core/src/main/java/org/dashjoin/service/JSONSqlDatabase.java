package org.dashjoin.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * One layer of the config database stored in SQL (see SqlConfigStore). The "app" layer holds the
 * app definition from git and is read-only; the "local" layer holds everything written at runtime;
 * the "effective" layer holds the merged result of all layers (read-only, maintained by the store).
 *
 * Records are stored as JSON text, which keeps the field order. Deleting a record that comes from a
 * lower layer leaves a deletion marker in the local layer (see UnionDatabase), which read and
 * queryMap skip.
 */
public class JSONSqlDatabase extends JSONDatabase {

  final SqlConfigStore store;
  final String layer;
  final boolean readOnly;

  JSONSqlDatabase(SqlConfigStore store, String layer, boolean readOnly) {
    this.store = store;
    this.layer = layer;
    this.readOnly = readOnly;
  }

  static String id(Object id) {
    return "" + id;
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(
            "select doc from dj_config where layer = ? and tbl = ? and id = ? and deleted = ?")) {
      stmt.setString(1, layer);
      stmt.setString(2, s.name);
      stmt.setString(3, id(search.get("ID")));
      stmt.setBoolean(4, false);
      try (ResultSet res = stmt.executeQuery()) {
        if (res.next())
          return fromJsonString(res.getString(1));
        return null;
      }
    }
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = new LinkedHashMap<>();
    String[] parts = info.query.split("/", 2);
    if (parts.length == 1 && arguments != null && arguments.get("ID") != null)
      parts = new String[] {parts[0], id(arguments.get("ID"))};
    if (parts.length == 2) {
      Map<String, Object> i = read(Table.ofName(parts[0]), Map.of("ID", parts[1]));
      if (i != null)
        res.put(id(i.get("ID")), i);
      return res;
    }
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(
            "select id, doc from dj_config where layer = ? and tbl = ? and deleted = ? order by id"
                + store.binaryCollation())) {
      stmt.setString(1, layer);
      stmt.setString(2, parts[0]);
      stmt.setBoolean(3, false);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next())
          res.put(rs.getString(1), fromJsonString(rs.getString(2)));
      }
    }
    return res;
  }

  void checkWritable() throws Exception {
    if (readOnly)
      throw new Exception("The " + layer + " layer of the config database is read only");
  }

  /**
   * like the JSON files, create overwrites an existing record (and a deletion marker)
   */
  @Override
  public void create(Table s, Map<String, Object> object) throws Exception {
    checkWritable();
    if (object.get("ID") == null)
      throw new Exception("Object must contain an ID");
    try (SqlConfigStore.Con c = store.con()) {
      store.upsert(c.con, layer, s.name, id(object.get("ID")), toJsonString(object), false);
    }
    changed(s, object.get("ID"));
  }

  /** the effective record must be recomputed */
  void changed(Table s, Object id) throws Exception {
    if (layer.equals(SqlConfigStore.LOCAL))
      store.changed(s.name, id(id));
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    checkWritable();
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(
            "delete from dj_config where layer = ? and tbl = ? and id = ? and deleted = ?")) {
      stmt.setString(1, layer);
      stmt.setString(2, s.name);
      stmt.setString(3, id(search.get("ID")));
      stmt.setBoolean(4, false);
      boolean res = stmt.executeUpdate() > 0;
      if (inLowerLayer(s, search.get("ID"))) {
        // hide the record of the lower layer
        markDeleted(s, search.get("ID"));
        return true;
      }
      changed(s, search.get("ID"));
      return res;
    }
  }

  /** the local layer: does a layer below it (app, platform defaults) have the record? */
  boolean inLowerLayer(Table s, Object id) throws Exception {
    if (!layer.equals(SqlConfigStore.LOCAL) || store.base == null)
      return false;
    for (JSONDatabase d : store.base)
      if (d.read(s, Map.of("ID", id(id))) != null)
        return true;
    return false;
  }

  /**
   * hide the record of a lower layer
   */
  public void markDeleted(Table s, Object id) throws Exception {
    checkWritable();
    try (SqlConfigStore.Con c = store.con()) {
      store.upsert(c.con, layer, s.name, id(id), null, true);
    }
    changed(s, id);
  }

  public boolean isDeleted(Table s, Object id) throws Exception {
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(
            "select 1 from dj_config where layer = ? and tbl = ? and id = ? and deleted = ?")) {
      stmt.setString(1, layer);
      stmt.setString(2, s.name);
      stmt.setString(3, id(id));
      stmt.setBoolean(4, true);
      try (ResultSet res = stmt.executeQuery()) {
        return res.next();
      }
    }
  }

  public Set<String> deletedIds(String table) throws Exception {
    Set<String> res = new HashSet<>();
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(
            "select id from dj_config where layer = ? and tbl = ? and deleted = ?")) {
      stmt.setString(1, layer);
      stmt.setString(2, table);
      stmt.setBoolean(3, true);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next())
          res.add(rs.getString(1));
      }
    }
    return res;
  }

  /**
   * all() of the union database on the effective layer, run in SQL on PostgreSQL: the arguments
   * filter by field value (null matches a missing field), sort orders by a field (records without
   * the field come last, or first when descending)
   */
  public List<Map<String, Object>> allEffective(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    StringBuilder sql =
        new StringBuilder("select doc from dj_config where layer = ? and tbl = ? and deleted = ?");
    List<Object> params = new ArrayList<>(List.of(layer, s.name, false));
    if (arguments != null)
      for (Entry<String, Object> e : arguments.entrySet())
        if (e.getValue() == null) {
          sql.append(" and (docb -> ? is null or docb -> ? = 'null'::jsonb)");
          params.add(e.getKey());
          params.add(e.getKey());
        } else {
          // containment uses the index, equality makes it exact (for arrays and objects)
          String value = om.writeValueAsString(e.getValue());
          sql.append(" and docb @> jsonb_build_object(?::text, ?::jsonb) and docb -> ? = ?::jsonb");
          params.addAll(List.of(e.getKey(), value, e.getKey(), value));
        }
    if (sort != null) {
      sql.append(" order by docb -> ? " + (descending ? "desc" : "asc") + ", id"
          + store.binaryCollation());
      params.add(sort);
    } else
      sql.append(" order by id" + store.binaryCollation());
    if (limit != null) {
      sql.append(" limit ?");
      params.add(limit);
    }
    if (offset != null) {
      sql.append(" offset ?");
      params.add(offset);
    }
    List<Map<String, Object>> res = new ArrayList<>();
    try (SqlConfigStore.Con c = store.con();
        PreparedStatement stmt = c.con.prepareStatement(sql.toString())) {
      for (int i = 0; i < params.size(); i++)
        stmt.setObject(i + 1, params.get(i));
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next())
          res.add(fromJsonString(rs.getString(1)));
      }
    }
    return res;
  }

  static final ObjectMapper om = new ObjectMapper();

  @Override
  public <T> T locked(Table s, Object id, Callable<T> body) throws Exception {
    return store.locked(s.name + "/" + id(id), body);
  }

  @Override
  public String toString() {
    return "JSONSqlDatabase(" + layer + ")";
  }
}
