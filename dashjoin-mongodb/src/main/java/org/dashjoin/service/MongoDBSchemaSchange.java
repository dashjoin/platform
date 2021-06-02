package org.dashjoin.service;

import static com.mongodb.client.model.Filters.eq;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.Document;
import org.dashjoin.function.AbstractDatabaseTrigger.Config;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.MapUtil;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCursor;

public class MongoDBSchemaSchange implements SchemaChange {

  MongoDB db;

  public MongoDBSchemaSchange(MongoDB db) {
    this.db = db;
  }

  @Override
  public void createTable(String table, String keyName, String keyType) throws Exception {
    Map<String, Object> object = new LinkedHashMap<>();
    object.put("_id", table);
    object.put("ID", db.ID + "/" + table);
    object.put("parent", db.ID);
    object.put("type", "object");
    object.put("name", table);

    Map<String, Object> properties = new LinkedHashMap<>();
    if (keyName == null) {
      Map<String, Object> key = new LinkedHashMap<>();
      key.put("ID", db.ID + "/" + table + "/_id");
      key.put("parent", db.ID + "/" + table);
      key.put("type", "number");
      key.put("dbType", "number");
      key.put("name", "_id");
      key.put("pkpos", 0);
      key.put("createOnly", true);
      properties.put("_id", key);
      Map<String, Object> name = new LinkedHashMap<>();
      name.put("ID", db.ID + "/" + table + "/name");
      name.put("parent", db.ID + "/" + table);
      name.put("type", "string");
      name.put("dbType", "string");
      name.put("name", "name");
      properties.put("name", name);
    } else {
      if (!keyName.equals("_id"))
        throw new Exception("Primary key must be called _id");
      Map<String, Object> key = new LinkedHashMap<>();
      key.put("ID", db.ID + "/" + table + "/" + keyName);
      key.put("parent", db.ID + "/" + table);
      key.put("type", t(keyType));
      key.put("dbType", keyType);
      key.put("name", keyName);
      key.put("pkpos", 0);
      key.put("createOnly", true);
      properties.put(keyName, key);
    }
    object.put("required", Arrays.asList(keyName == null ? "_id" : keyName));
    object.put("properties", properties);
    db.collection("__schema").insertOne(db.document(object, false));
    db.con().createCollection(table);
  }

  void checkTableEmpty(String op, String table) throws Exception {
    List<Map<String, Object>> existing = db.all(db.tables.get(table), null, 1, null, false, null);
    if (!existing.isEmpty())
      throw new Exception(op + " is only supported if the table has no data");
  }

  Document checkManaged(String _table) throws Exception {
    MongoCursor<Document> table = db.collection("__schema").find(eq("_id", _table)).iterator();
    if (!table.hasNext())
      throw new Exception("Table " + _table + " is not managed by Dashjoin and cannot be edited");
    return table.next();
  }

  @Override
  public void dropTable(String table) throws Exception {
    checkManaged(table);
    db.collection("__schema").deleteOne(eq("_id", table));
    db.collection(table).drop();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void renameTable(String table, String newName) throws Exception {

    Map<String, Object> r = checkManaged(table);

    r.put("name", newName);
    String tableID = r.get("parent") + "/" + newName;
    r.put("ID", tableID);
    r.put("_id", newName);
    Map<String, Object> props = (Map<String, Object>) r.get("properties");

    for (Entry<String, Object> p : props.entrySet()) {
      Map<String, Object> prop = (Map<String, Object>) p.getValue();
      prop.put("parent", tableID);
      prop.put("ID", tableID + "/" + p.getKey());
    }

    db.collection("__schema").deleteOne(eq("_id", table));
    db.collection("__schema").insertOne(db.document(r, false));

    db.collection(table).renameCollection(new MongoNamespace(db.database, newName));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void createColumn(String table, String columnName, String columnType) throws Exception {

    Document doc = checkManaged(table);

    Map<String, Object> newcol = MapUtil.of("dbType", columnType, "type", t(columnType), "ID",
        doc.get("ID") + "/" + columnName, "name", columnName, "parent", doc.get("ID"));

    if (columnType.equals("date"))
      newcol.put("widget", "date");

    ((Map<String, Object>) doc.get("properties")).put(columnName, newcol);

    db.collection("__schema").replaceOne(eq("_id", table), doc);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void renameColumn(String table, String column, String newName) throws Exception {
    checkTableEmpty("Rename column", table);

    Document r = checkManaged(table);

    List<String> required = (List<String>) r.get("required");
    int index = required.indexOf(column);
    if (index >= 0)
      required.set(index, newName);

    Map<String, Object> props = (Map<String, Object>) r.get("properties");
    Map<String, Object> prop = (Map<String, Object>) props.remove(column);
    prop.put("name", newName);
    prop.put("ID", prop.get("parent") + "/" + newName);
    props.put(newName, prop);

    db.collection("__schema").replaceOne(eq("_id", table), r);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void alterColumn(String table, String column, String newType) throws Exception {
    checkTableEmpty("Alter column", table);

    Document r = checkManaged(table);

    Map<String, Object> props = (Map<String, Object>) r.get("properties");
    Map<String, Object> prop = (Map<String, Object>) props.get(column);

    if (newType.equals("date")) {
      prop.put("type", "string");
      prop.put("dbType", newType);
      prop.put("widget", "date");
    } else {
      prop.put("type", newType);
      prop.put("dbType", newType);
      prop.remove("widget");
    }

    db.collection("__schema").replaceOne(eq("_id", table), r);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void dropColumn(String _table, String column) throws Exception {
    Document table = checkManaged(_table);

    Map<String, Object> newprops = (Map<String, Object>) table.get("properties");

    Object check = newprops.get(column);
    if (check instanceof Map<?, ?>)
      if (((Map<String, Object>) check).containsKey("pkpos"))
        throw new Exception("Cannot drop the primary key column");

    newprops.remove(column);

    db.collection("__schema").replaceOne(eq("_id", _table), table);
  }

  String t(String s) {
    if ("date".equals(s))
      return "string";
    else
      return s;
  }

  @Override
  public void check(Config arg) throws Exception {
    // simply ignore pkpos edits
    arg.object.remove("pkpos");
  }
}
