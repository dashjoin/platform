package org.dashjoin.sdk;

import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * database REST skeleton
 */
@Path("/database")
// @RolesAllowed("admin")
public class DatabaseService {

  /**
   * equivalent to djClassName in JSON config
   */
  @ConfigProperty(name = "dashjoin.database.djClassName")
  String database;

  /**
   * contained DB instance
   */
  AbstractDatabase db;

  /**
   * pseudo config DB injected into db
   */
  SDKServices services;

  /**
   * get and init db
   */
  synchronized AbstractDatabase db() throws Exception {

    if (db == null) {
      db = (AbstractDatabase) Class.forName(database).getDeclaredConstructor().newInstance();
      String password = null;

      for (String key : ConfigProvider.getConfig().getPropertyNames())
        if (key.startsWith("dashjoin.database.")) {
          String prop = key.substring("dashjoin.database.".length());
          Object value =
              ConfigProvider.getConfig().getValue(key, db.getClass().getField(prop).getType());
          db.getClass().getField(prop).set(db, value);
          if (prop.equals("password"))
            password = (String) value;
        }

      services = new SDKServices(db, password);
      db.init(services);
    }
    return db;
  }

  /**
   * create a QueryMeta object with defaults for ID and type
   */
  QueryMeta qi(Query q) throws Exception {
    QueryMeta qi = QueryMeta.ofQuery(q.query);
    qi.type = "read";
    qi.database = db().ID;
    return qi;
  }

  /**
   * lookup table metadata
   */
  Table table(String table) throws Exception {
    return table(db(), table);
  }

  /**
   * lookup table metadata
   * 
   * @throws Exception if db.tables is null, must throw an exception "Schema not set" to signal the
   *         caller that setSchema must be called and that the current call must be retried
   *         afterwards
   */
  public static Table table(AbstractDatabase db, String table) throws Exception {
    if (db.tables == null)
      throw new Exception("Schema not set");
    Table res = db.tables.get(table);
    if (res == null)
      throw new Exception("Table not found: " + table);
    return res;
  }

  /**
   * query / arguments holder
   */
  public static class Query {
    public String query;
    public Map<String, Object> arguments;
  }

  @POST
  @Path("/query")
  public List<Map<String, Object>> query(Query q) throws Exception {
    return db().query(qi(q), q.arguments);
  }

  @POST
  @Path("/queryMeta")
  public Map<String, Property> queryMeta(Query q) throws Exception {
    return db().queryMeta(qi(q), q.arguments);
  }

  @POST
  @Path("/connectAndCollectMetadata/{id}")
  public Map<String, Object> connectAndCollectMetadata(@PathParam("id") String id)
      throws Exception {
    // invalidate the table metadata since we need to fetch them from the config DB
    db().tables = null;
    db().ID = id;
    return db().connectAndCollectMetadata();
  }

  @POST
  @Path("/setSchema")
  public void setSchema(Map<String, Table> tables) throws Exception {
    db().tables = tables;
  }

  @POST
  @Path("/close")
  public void close() throws Exception {
    db().close();
  }

  @POST
  @Path("/all/{table}")
  public List<Map<String, Object>> all(@PathParam("table") String table,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
      @QueryParam("sort") String sort, @QueryParam("descending") boolean descending,
      Map<String, Object> arguments) throws Exception {
    return db().all(table(table), offset, limit, sort, descending, arguments);
  }

  @POST
  @Path("/create/{table}")
  public Map<String, Object> create(@PathParam("table") String table, Map<String, Object> object)
      throws Exception {
    db.cast(table(table), object);
    db().create(table(table), object);
    return object;
  }

  @POST
  @Path("/read/{table}")
  public Map<String, Object> read(@PathParam("table") String table, Map<String, Object> search)
      throws Exception {
    db.cast(table(table), search);
    return db().read(table(table), search);
  }

  /**
   * search / object holder
   */
  public static class SearchObject {
    public Map<String, Object> search;
    public Map<String, Object> object;
  }

  @POST
  @Path("/update/{table}")
  public boolean update(@PathParam("table") String table, SearchObject so) throws Exception {
    db.cast(table(table), so.object);
    db.cast(table(table), so.search);
    return db().update(table(table), so.search, so.object);
  }

  @POST
  @Path("/delete/{table}")
  public boolean delete(@PathParam("table") String table, Map<String, Object> search)
      throws Exception {
    db.cast(table(table), search);
    return db().delete(table(table), search);
  }

  @POST
  @Path("/createColumn/{table}/{columnName}/{columnType}")
  public void createColumn(@PathParam("table") String table,
      @PathParam("columnName") String columnName, @PathParam("columnType") String columnType)
      throws Exception {
    db().getSchemaChange().createColumn(table, columnName, columnType);
  }

  @POST
  @Path("/alterColumn/{table}/{column}/{newType}")
  public void alterColumn(@PathParam("table") String table, @PathParam("column") String column,
      @PathParam("newType") String newType) throws Exception {
    db().getSchemaChange().alterColumn(table, column, newType);
  }

  @POST
  @Path("/renameColumn/{table}/{column}/{newName}")
  public void renameColumn(@PathParam("table") String table, @PathParam("column") String column,
      @PathParam("newName") String newName) throws Exception {
    db().getSchemaChange().renameColumn(table, column, newName);
  }

  @POST
  @Path("/dropColumn/{table}/{column}")
  public void dropColumn(@PathParam("table") String table, @PathParam("column") String column)
      throws Exception {
    db().getSchemaChange().dropColumn(table, column);
  }

  @POST
  @Path("/createTable/{table}/{keyName}/{keyType}")
  public void createTable(@PathParam("table") String table, @PathParam("keyName") String keyName,
      @PathParam("keyType") String keyType) throws Exception {
    db().getSchemaChange().createTable(table, keyName, keyType);
  }

  @POST
  @Path("/createTable/{table}")
  public void createTable(@PathParam("table") String table) throws Exception {
    db().getSchemaChange().createTable(table, null, null);
  }

  @POST
  @Path("/renameTable/{table}/{newName}")
  public void renameTable(@PathParam("table") String table, @PathParam("newName") String newName)
      throws Exception {
    db().getSchemaChange().renameTable(table, newName);
  }

  @POST
  @Path("/dropTable/{table}")
  public void dropTable(@PathParam("table") String table) throws Exception {
    db().getSchemaChange().dropTable(table);
  }
}
