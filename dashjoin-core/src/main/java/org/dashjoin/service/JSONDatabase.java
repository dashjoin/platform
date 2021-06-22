package org.dashjoin.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.commons.lang3.NotImplementedException;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.java.Log;

/**
 * Part of the config database. This class treats JSON files on the filesystem or the classpath like
 * a (config) database. The database is located in the folder model. The tables are subfolders in
 * models. The instances are files with name ID.json with the file contents being the object's data.
 */
@Log
public abstract class JSONDatabase implements Database {

  /**
   * helper when converting JSON to map(string, object)
   */
  public static TypeReference<Map<String, Object>> tr = new TypeReference<Map<String, Object>>() {};

  /**
   * helper when converting JSON to map(string, map)
   */
  public static TypeReference<Map<String, Map<String, Object>>> trr =
      new TypeReference<Map<String, Map<String, Object>>>() {};

  /**
   * helper when converting JSON to list(object)
   */
  public static TypeReference<List<Map<String, Object>>> trTable =
      new TypeReference<List<Map<String, Object>>>() {};

  /**
   * singleton mapper
   */
  protected static final ObjectMapper objectMapper =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  /**
   * like query, but for the "get all instances of table" queries, returns a map where the object ID
   * is the key and the object i the value. Called from the union database.
   */
  public abstract Map<String, Map<String, Object>> queryMap(QueryMeta info,
      Map<String, Object> arguments) throws Exception;

  /**
   * delegate to queryMap
   */
  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return new ArrayList<>(queryMap(info, arguments).values());
  }

  /**
   * update file on filesystem, read only for classloader
   */
  @Override
  public boolean update(Table s, Map<String, Object> search, Map<String, Object> object)
      throws Exception {

    Map<String, Object> r = read(s, search);
    if (r == null)
      return false;

    for (Entry<String, Object> e : object.entrySet()) {
      if (e.getValue() == null)
        r.remove(e.getKey());
      else
        r.put(e.getKey(), e.getValue());
    }
    return update(s, r);
  }

  /**
   * Update object operation. This is basically a create whether the object exists or not.
   * 
   * @param s
   * @param object
   * @return
   * @throws Exception
   */
  public boolean update(Table s, Map<String, Object> object) throws Exception {
    create(s, object);

    return true;
  }

  @Override
  public void create(Table s, Map<String, Object> object) throws Exception {
    throw new IOException("This DB is read only");
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    throw new IOException("This DB is read only");
  }

  /**
   * the config "query" is either "table" to get all or "table/id" to get one. return the first part
   * of the query to get the table
   */
  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    String[] parts = query.split("/");
    return Arrays.asList(parts[0]);
  }

  /**
   * noop
   */
  @Override
  public void close() throws Exception {}

  /**
   * not a top level DB, hence no editor
   */
  @Override
  public QueryEditorInternal getQueryEditor() {
    throw new NotImplementedException("not implemented");
  }

  /**
   * all delegates to query
   */
  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    QueryMeta info = new QueryMeta();
    info.query = s.name;
    return query(info, arguments);
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    throw new NotImplementedException("not implemented");
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    return null;
  }

  /**
   * Utility method that converts a POJO to its Map representation.
   * 
   * @param <T>
   * @param u
   * @return
   * @throws JsonProcessingException
   */
  public static <T> Map<String, Object> toMap(T u) throws JsonProcessingException {
    String json;
    try {
      json = objectMapper.writeValueAsString(u);
      Map<String, Object> o = objectMapper.readValue(json, JSONDatabase.tr);
      return o;
    } catch (JsonProcessingException e) {
      log.log(Level.WARNING, "Error converting object", e);
      throw e;
    }
  }

  /**
   * Utility method that converts a Map to its POJO representation.
   * 
   * @param <T>
   * @param info
   * @param clz
   * @return
   * @throws JsonProcessingException
   */
  public static <T> T fromMap(Map<String, Object> info, Class<T> clz)
      throws JsonProcessingException {
    String s;
    try {
      s = objectMapper.writeValueAsString(info);
      return objectMapper.readValue(s, clz);
    } catch (JsonProcessingException e) {
      log.log(Level.WARNING, "Error converting object", e);
      throw e;
    }
  }

  /**
   * Convert object map to JSON representation.
   * 
   * @param obj
   * @return JSON string
   * @throws JsonProcessingException
   */
  public static String toJsonString(Map<String, Object> obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }

  /**
   * Convert JSON to map representation.
   * 
   * @param json
   * @return
   * @throws JsonProcessingException
   */
  public static Map<String, Object> fromJsonString(String json) throws JsonProcessingException {
    return objectMapper.readValue(json, JSONDatabase.tr);
  }
}
