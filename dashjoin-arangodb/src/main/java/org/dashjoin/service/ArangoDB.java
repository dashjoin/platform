package org.dashjoin.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Metadata.Column;
import org.dashjoin.service.Metadata.Key;
import org.dashjoin.service.Metadata.MdTable;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.Template;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.model.CollectionSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ArangoDB implementation
 */
@JsonSchema(required = {"hostname", "database"},
    order = {"hostname", "port", "database", "username", "password"})
public class ArangoDB extends AbstractDatabase {

  private final static Logger logger = Logger.getLogger(ArangoDB.class.getName());

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * JSON config field
   */
  public String username;

  /**
   * JSON config field
   */
  @JsonSchema(widget = "password")
  public String password;

  @JsonSchema(style = {"width", "400px"})
  public String hostname;

  @JsonSchema(style = {"width", "400px"})
  public Integer port;

  @JsonSchema(style = {"width", "400px"})
  public String database;

  public List<String> datasets;

  /**
   * shared connection pool
   */
  com.arangodb.ArangoDB arangoDB;
  ArangoDatabase con;

  synchronized ArangoDatabase con() {
    try {
      ArangoDB x = services.getConfig().getCachedForce(ID, getClass());
      if (x.con == null)
        throw new Exception("Database not yet initialized: " + ID);
      return x.con;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error opening connection to ArangoDB", e);
      throw new RuntimeException();
    }
  }

  @SuppressWarnings("unchecked")
  List<Map<String, Object>> query(String query) {
    @SuppressWarnings("rawtypes")
    ArangoCursor<Map> cursor = con().query(query, Map.class);
    List<Map<String, Object>> res = new ArrayList<>();
    while (cursor.hasNext()) {
      @SuppressWarnings("rawtypes")
      Map row = new LinkedHashMap(cursor.next());
      row.remove("_rev");
      row.remove("_key");
      res.add(row);
    }
    return res;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> connectAndCollectMetadata() throws Exception {

    arangoDB = new com.arangodb.ArangoDB.Builder().host(hostname, port).user(username)
        .password(password() == null && name.equals("junit") ? password : password()).build();
    con = arangoDB.db(database);

    if (datasets != null)
      for (String s : datasets) {
        ArangoCollection drop = con.collection(FilenameUtils.getBaseName(s));
        if (drop.exists())
          drop.drop();
        CollectionEntity col = con.createCollection(FilenameUtils.getBaseName(s));
        InputStream ddl = getClass().getResourceAsStream(s);
        List<Map<String, Object>> arr = objectMapper.readValue(ddl, JSONDatabase.trTable);
        for (Map<String, Object> i : arr) {
          con.collection(col.getName()).insertDocument(i);
        }
      }

    Metadata meta = new Metadata();
    for (CollectionEntity c : con.getCollections()) {
      if (c.getIsSystem())
        continue;
      MdTable prj = new MdTable(c.getName());
      prj.pk = new Key();
      prj.pk.col.add("_id");

      // need extra call since schema is not part of CollectionEntity
      CollectionSchema sc = con().collection(c.getName()).getProperties().getSchema();

      if (sc != null && sc.getRule() != null) {
        Map<String, Map<String, Object>> schema =
            objectMapper.readValue(sc.getRule(), JSONDatabase.trr);
        Map<String, Object> properties = schema.get("properties");
        Column id = new Column();
        id.name = "_id";
        id.readOnly = true;
        id.typeName = "VARCHAR";
        prj.columns.add(id);

        Column cfrom = new Column();
        cfrom.name = "_from";
        cfrom.typeName = "VARCHAR";
        prj.columns.add(cfrom);

        Column cto = new Column();
        cto.name = "_to";
        cto.typeName = "VARCHAR";
        prj.columns.add(cto);

        for (Entry<String, Object> e : properties.entrySet()) {
          String type = (String) ((Map<String, Object>) e.getValue()).get("type");
          Column col = new Column();
          col.name = e.getKey();
          if (schema.get("required") != null)
            id.readOnly = ((List<String>) schema.get("required")).contains(col.name);
          if (type.equals("integer"))
            col.typeName = "INTEGER";
          else if (type.equals("number"))
            col.typeName = "DOUBLE";
          else if (type.equals("boolean"))
            col.typeName = "BIT";
          else
            col.typeName = "VARCHAR";
          prj.columns.add(col);
        }

        if (c.getType().equals(CollectionType.EDGES)) {
          Key from = new Key();
          from.col.add("_from");
          Key to = new Key();
          to.col.add("_to");
          for (Map<String, Object> sample : query(
              "for t in " + c.getName() + " limit 1 return t")) {
            prj.fks.put(sample.get("_from").toString().split("/")[0], from);
            prj.fks.put(sample.get("_to").toString().split("/")[0], to);
            break;
          }
        }

      } else
        for (Map<String, Object> sample : query("for t in " + c.getName() + " limit 1 return t")) {
          for (Entry<String, Object> e : sample.entrySet()) {
            Column col = new Column();
            col.name = e.getKey();
            if (col.name.equals("_id"))
              col.readOnly = true;
            if (e.getValue() instanceof Integer)
              col.typeName = "INTEGER";
            else if (e.getValue() instanceof Long)
              col.typeName = "INTEGER";
            else if (e.getValue() instanceof Double)
              col.typeName = "DOUBLE";
            else if (e.getValue() instanceof Boolean)
              col.typeName = "BIT";
            else
              // TODO: other types
              col.typeName = "VARCHAR";
            prj.columns.add(col);
          }

          if (c.getType().equals(CollectionType.EDGES)) {
            Key from = new Key();
            from.col.add("_from");
            Key to = new Key();
            to.col.add("_to");
            prj.fks.put(sample.get("_from").toString().split("/")[0], from);
            prj.fks.put(sample.get("_to").toString().split("/")[0], to);
          }

          break;
        }
      meta.tables.put(c.getName(), prj);
    }
    return meta.getTables(ID);
  }

  @Override
  public void close() {
    arangoDB.shutdown();
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    ArangoDBQuery q =
        new ArangoDBQuery("" + Template.replace(info.query, Template.quoteStrings(arguments)));

    return query(q.toString());
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    object = new LinkedHashMap<>(object);
    String key = search2key(object);
    if (key != null) {
      object.put("_key", key);
      object.remove("_id");
    }
    con().collection(m.name).insertDocument(object);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    Map<String, Object> tmp = con().collection(s.name).getDocument(search2key(search), Map.class);
    if (tmp == null)
      return null;
    Map<String, Object> m = new LinkedHashMap<>(tmp);
    m.remove("_rev");
    m.remove("_key");
    return m;
  }

  String search2key(Map<String, Object> search) {
    String id = (String) search.get("_id");
    if (id == null)
      return null;
    int slash = id.indexOf('/');
    return id.substring(slash + 1, id.length());
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    con().collection(schema.name).updateDocument(search2key(search), object);
    return true;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    con().collection(s.name).deleteDocument(search2key(search));
    return true;
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    ArangoDBQuery q = new ArangoDBQuery(query);
    return Arrays.asList(q.collection);
  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new ArangoDBEditor(services, this);
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {

    String sorts = sort == null ? "" : " sort t." + sort + (descending ? " desc " : " asc ");
    List<String> filters = new ArrayList<>();
    if (arguments != null)
      for (Entry<String, Object> e : arguments.entrySet())
        if (e.getValue() instanceof String)
          filters.add("t." + e.getKey() + "==\"" + e.getValue() + "\"");
        else
          filters.add("t." + e.getKey() + "==" + e.getValue());
    String filter = filters.isEmpty() ? "" : " filter " + String.join(" && ", filters) + " ";

    String limits = "";
    if (limit != null || offset != null) {
      if (offset == null)
        limits = " limit " + limit;
      else
        limits = " limit " + offset + ", " + limit;
    }

    String query = "for t in " + s.name + sorts + limits + filter + " return t";

    List<Map<String, Object>> list = new ArrayList<>();
    for (Map<String, Object> d : query(query)) {
      list.add(d);
    }
    return list;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    ArangoDBQuery parse =
        new ArangoDBQuery("" + Template.replace(info.query, Template.quoteStrings(arguments)));
    Table table = tables.get(parse.collection);

    Map<String, Property> meta = new LinkedHashMap<>();
    List<Map<String, Object>> res = query(info, arguments);
    for (Map<String, Object> row : res) {
      for (Entry<String, Object> c : row.entrySet()) {
        Property p = meta.get(c.getKey());
        if (p == null) {
          if (table.properties.containsKey(c.getKey()))
            meta.put(c.getKey(), table.properties.get(c.getKey()));
          else {
            Property prop = new Property();
            prop.name = c.getKey();
            prop.type = c.getValue() instanceof Number ? "number" : "string";
            meta.put(c.getKey(), prop);
          }
        }
      }
      break;
    }
    return meta;
  }

  @Override
  public String displayUrl() {
    return "arangodb://" + hostname + (port == null ? "" : ":" + port) + "/" + database;
  }

  @Override
  public SchemaChange getSchemaChange() {
    return new ArangoDBSchemaSchange(this);
  }
}
