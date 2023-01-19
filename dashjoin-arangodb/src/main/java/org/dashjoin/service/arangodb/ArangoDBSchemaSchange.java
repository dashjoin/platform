package org.dashjoin.service.arangodb;

import java.util.Map;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.MapUtil;
import com.arangodb.ArangoCollection;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.CollectionSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArangoDBSchemaSchange implements SchemaChange {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  ArangoDB db;

  public ArangoDBSchemaSchange(ArangoDB db) {
    this.db = db;
  }

  @Override
  public void createTable(String table, String keyName, String keyType) throws Exception {
    if (keyName == null) {
      CollectionCreateOptions opts = new CollectionCreateOptions();
      CollectionSchema schema = new CollectionSchema();
      schema.setRule("{ \"properties\": { \"name\": { \"type\": \"string\" } } }");
      opts.schema(schema);
      db.con().createCollection(table, opts);
    } else {
      if (!keyName.equals("_id"))
        throw new Exception("Primary key must be called _id");
      db.con().createCollection(table, null);
    }
  }

  @Override
  public void dropTable(String table) throws Exception {
    db.con().collection(table).drop();
  }

  @Override
  public void renameTable(String table, String newName) throws Exception {
    db.con().collection(table).rename(newName);
  }

  @Override
  public void createColumn(String table, String columnName, String columnType) throws Exception {
    ArangoCollection collection = db.con().collection(table);
    Map<String, Map<String, Object>> schema = getSchemaOrDefault(collection);
    schema.get("properties").put(columnName, MapUtil.of("type", columnType));
    writeSchema(collection, schema);
  }

  @Override
  public void renameColumn(String table, String column, String newName) throws Exception {
    ArangoCollection collection = db.con().collection(table);
    Map<String, Map<String, Object>> schema = getSchemaOrDefault(collection);
    Object keep = schema.get("properties").remove(column);
    schema.get("properties").put(newName, keep);
    writeSchema(collection, schema);
  }

  @Override
  public void alterColumn(String table, String column, String newType) throws Exception {
    ArangoCollection collection = db.con().collection(table);
    Map<String, Map<String, Object>> schema = getSchemaOrDefault(collection);
    @SuppressWarnings("unchecked")
    Map<String, Object> col = (Map<String, Object>) schema.get("properties").get(column);
    col.put("type", newType);
    writeSchema(collection, schema);
  }

  @Override
  public void dropColumn(String table, String column) throws Exception {
    ArangoCollection collection = db.con().collection(table);
    Map<String, Map<String, Object>> schema = getSchemaOrDefault(collection);
    schema.get("properties").remove(column);
    writeSchema(collection, schema);
  }

  void writeSchema(ArangoCollection collection, Map<String, Map<String, Object>> schema)
      throws Exception {
    CollectionPropertiesOptions opts = new CollectionPropertiesOptions();
    CollectionSchema rule = new CollectionSchema();
    rule.setRule(objectMapper.writeValueAsString(schema));
    opts.schema(rule);
    collection.changeProperties(opts);
  }

  Map<String, Map<String, Object>> getSchemaOrDefault(ArangoCollection collection)
      throws Exception {
    if (collection.getProperties().getSchema() != null)
      return objectMapper.readValue(collection.getProperties().getSchema().getRule(),
          JSONDatabase.trr);
    else
      return MapUtil.of("properties", MapUtil.of());
  }
}
