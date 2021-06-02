package org.dashjoin.service;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Metadata.Column;
import org.dashjoin.service.Metadata.Key;
import org.dashjoin.service.Metadata.MdTable;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.ddl.SchemaChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * MongoDB implementation
 */
@JsonSchema(required = {"hostname", "database"}, order = {"hostname", "port", "database"})
public class MongoDB extends AbstractDatabase {

  private final static Logger logger = Logger.getLogger(MongoDB.class.getName());

  private static final ObjectMapper objectMapper = new ObjectMapper();

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
  MongoDatabase con;

  synchronized MongoDatabase con() {
    try {
      MongoDB x = services.getConfig().getCachedForce(ID, getClass());
      if (x.con == null)
        throw new Exception("Database not yet initialized: " + ID);
      return x.con;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error opening connection to MongoDB", e);
      throw new RuntimeException();
    }
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    if (datasets != null) {
      MongoServer server = new MongoServer(new MemoryBackend());
      server.bind(hostname, port == null ? 27017 : port);
    }
    con = MongoClients.create("mongodb://" + hostname + (port == null ? "" : ":" + port))
        .getDatabase(database);

    if (datasets != null)
      for (String s : datasets) {
        MongoCollection<Document> col = con.getCollection(FilenameUtils.getBaseName(s));
        InputStream ddl = getClass().getResourceAsStream(s);
        ArrayNode arr = (ArrayNode) objectMapper.readTree(ddl);
        for (JsonNode i : arr) {
          Document doc = Document.parse(i.toString());
          col.insertOne(doc);
        }
      }

    // schema info is combined from user defined tables (which might not have any data yet) and
    // existing data
    Map<String, Object> schema = new HashMap<>();
    for (Document map : collection("__schema").find())
      schema.put("" + map.get("name"), map);

    Metadata meta = new Metadata();
    for (String c : con.listCollectionNames()) {

      if (c.equals("__schema"))
        continue;

      if (schema.containsKey(c))
        continue;

      Document sample = con.getCollection(c).find().first();
      if (sample != null) {
        MdTable prj = new MdTable(c);
        prj.pk = new Key();
        prj.pk.col.add("_id");

        prj.columns = new ArrayList<>();
        for (Entry<String, Object> e : sample.entrySet()) {
          Column col = new Column();
          col.name = e.getKey();
          if (col.name.equals("_id"))
            col.required = true;
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
        meta.tables.put(c, prj);
      }
    }

    return UnionDatabase.merge(schema, meta.getTables(ID));
  }

  MongoCollection<Document> collection(String c) {
    return con().getCollection(c);
  }

  List<Document> doc(Table t, Map<String, Object> search, Integer offset, Integer limit) {
    List<Bson> filters = filters(t, search);
    List<Document> res = new ArrayList<>();
    FindIterable<Document> i =
        filters.isEmpty() ? collection(t.name).find() : collection(t.name).find(and(filters));

    if (offset != null)
      i = i.skip(offset);
    if (limit != null)
      i = i.limit(limit);

    MongoCursor<Document> iter = i.iterator();

    while (iter.hasNext())
      res.add(iter.next());
    return res;
  }

  List<Bson> filters(Table t, Map<String, Object> search) {
    List<Bson> filters = new ArrayList<>();
    if (search != null)
      for (Entry<String, Object> e : search.entrySet()) {
        filters.add(eq(e.getKey(),
            "_id".equals(e.getKey()) && e.getValue() instanceof String
                && ((String) e.getValue()).startsWith("_")
                    ? new ObjectId(((String) e.getValue()).substring(1))
                    : e.getValue()));
      }
    return filters;
  }

  Document document(Map<String, Object> object, boolean set) {
    Document doc = new Document();
    for (Entry<String, Object> e : object.entrySet())
      doc.append(e.getKey(), e.getValue());
    if (set) {
      Document setd = new Document();
      setd.append("$set", doc);
      return setd;
    } else
      return doc;
  }

  @Override
  public void close() {

  }

  public QueryResponse getIDsOfClassQuery(String clazz) {
    QueryResponse res = new QueryResponse();
    res.query = "db.['" + clazz + "'].find( {}, {_id : 1} )";
    return res;
  }

  public Collection<String> getClasses() throws Exception {
    Collection<String> res = new ArrayList<>();
    for (String s : con().listCollectionNames())
      res.add(s);
    return res;
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    MongoDBQuery q = new MongoDBQuery(info.query);

    List<Map<String, Object>> res = new ArrayList<>();

    MongoCursor<Document> i = collection(q.collection).aggregate(q.array()).iterator();
    while (i.hasNext()) {
      Document d = i.next();
      Map<String, Object> m = new LinkedHashMap<>();
      for (Entry<String, Object> e : d.entrySet())
        m.put(e.getKey(), e.getValue());
      res.add(m);
    }
    return res;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    collection(m.name).insertOne(document(object, false));
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    List<Map<String, Object>> d = readInternal(s, null, null, search);
    if (d.isEmpty())
      return null;
    return d.get(0);
  }

  List<Map<String, Object>> readInternal(Table s, Integer offset, Integer limit,
      Map<String, Object> search) throws Exception {
    List<Map<String, Object>> list = new ArrayList<>();
    for (Document d : doc(s, search, offset, limit)) {
      if (limit != null && list.size() == limit)
        break;
      Map<String, Object> res = new LinkedHashMap<>();
      for (Entry<String, Object> e : d.entrySet()) {
        String prop = e.getKey();
        if (e.getValue() instanceof ObjectId)
          res.put(prop, "_" + ((ObjectId) e.getValue()).toString());
        else
          res.put(prop, e.getValue());
      }
      list.add(res);
    }
    return list;
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    List<Bson> filters = filters(schema, search);
    UpdateResult res = collection(schema.name).updateOne(and(filters), document(object, true));
    return res.getMatchedCount() > 0;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    List<Bson> filters = filters(s, search);
    DeleteResult res = collection(s.name).deleteOne(and(filters));
    return res.getDeletedCount() > 0;
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    MongoDBQuery q = new MongoDBQuery(query);
    return Arrays.asList(q.collection);
  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new MongoDBEditor(services, this);
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    return readInternal(s, offset, limit, arguments);
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {

    MongoDBQuery parse = new MongoDBQuery(info.query);
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
            if (parse.lookup.get("as") != null)
              if (c.getKey().equals(parse.lookup.get("as").asString().getValue())) {
                Table join = tables.get(parse.lookup.get("from").asString().getValue());
                if (join != null)
                  meta.put(c.getKey(),
                      join.properties.get(parse.lookup.get("foreignField").asString().getValue()));
                continue;
              }
            Property prop = new Property();
            prop.name = c.getKey();
            prop.type = c.getValue() instanceof Number ? "number" : "string";
            meta.put(c.getKey(), prop);
          }
        }
      }
    }
    return meta;
  }

  @Override
  public String displayUrl() {
    return "mongodb://" + hostname + (port == null ? "" : ":" + port) + "/" + database;
  }

  @Override
  public SchemaChange getSchemaChange() {
    return new MongoDBSchemaSchange(this);
  }
}
