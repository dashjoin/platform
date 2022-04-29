package org.dashjoin.sdk;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * database REST skeleton
 * 
 * there's a key difference in how the state of the database is handled, since (1) the DB cannot
 * directly access the schema and (2) the container / VM might be restarted at any time. The
 * database can be in 3 states:
 * 
 * initial: we were just started, no schema and no connection is present (any call - exception
 * connectAndCollectMetadata() will fail). We reach this state upon startup and if close() is
 * called.
 * 
 * connected: connectAndCollectMetadata() was called but we might not have received the schema. This
 * is a chicken and egg type problem, since the schema can only be computed in the onfig DB once
 * connectAndCollectMetadata() was called and the data is merged. We reach this state if the
 * connectAndCollectMetadata() is called which happens upon startup of the central system and if the
 * DB is created / updated
 * 
 * ready: connected and schema present. We reach this state if the central system calls setSchema()
 * - which in turn is called when another call fails with the respective error message indicating
 * that we are in one of the states above
 */
@Path("/database")
// @RolesAllowed("admin")
public class DatabaseService {

  /**
   * equivalent to djClassName in JSON config
   */
  @ConfigProperty(name = "dashjoin.database.djClassName")
  Optional<String> database;

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
    return db(true);
  }

  synchronized AbstractDatabase db(boolean expectSchema) throws Exception {
    if (db == null && database.isPresent()) {
      db = (AbstractDatabase) Class.forName(database.get()).getDeclaredConstructor().newInstance();

      // set db.tables to null to indicate that the field still needs to be set from the central
      // system
      db.tables = null;

      String password = null;

      for (String key : ConfigProvider.getConfig().getPropertyNames())
        if (key.startsWith("dashjoin.database.")) {
          String prop = key.substring("dashjoin.database.".length());
          Object value = db.getClass().getField(prop).getType().equals(List.class)
              ? Arrays.asList(ConfigProvider.getConfig().getValue(key, String.class).split(","))
              : ConfigProvider.getConfig().getValue(key, db.getClass().getField(prop).getType());
          db.getClass().getField(prop).set(db, value);
          if (prop.equals("password"))
            password = (String) value;
        }

      services = new SDKServices(db, password);
      db.init(services);
    }

    if (expectSchema)
      if (db.tables == null)
        throw new WebApplicationException("Schema not set", 572);

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
  @Path("/queryGraph")
  public List<Map<String, Object>> queryGraph(Query q) throws Exception {
    return db().queryGraph(qi(q), q.arguments);
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
    db(false).tables = null;
    db(false).ID = id;
    System.out.println("ID=" + id);
    Map<String, Object> res = db(false).connectAndCollectMetadata();
    return res;
  }

  @POST
  @Path("/setSchema/{id}")
  public void setSchema(@PathParam("id") String id, Map<String, Table> tables) throws Exception {
    if (db(false).ID == null)
      this.connectAndCollectMetadata(id);
    db(false).tables = tables;
  }

  @POST
  @Path("/close")
  public void close() throws Exception {
    db().close();
    db().ID = null;
    db().tables = null;
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

  @POST
  @Path("/getInitialQuery")
  public QueryResponse getInitialQuery(@Context SecurityContext sc, InitialQueryRequest ac)
      throws Exception {
    return db().getQueryEditor().getInitialQuery(ac);
  }

  @POST
  @Path("/distinct")
  public QueryResponse distinct(DistinctRequest ac) throws Exception {
    return db().getQueryEditor().distinct(ac);
  }

  @POST
  @Path("/noop")
  public QueryResponse noop(QueryDatabase query) throws Exception {
    return db().getQueryEditor().noop(query);
  }

  @POST
  @Path("/moveColumn")
  public QueryResponse moveColumn(@Context SecurityContext sc, MoveColumnRequest ac)
      throws Exception {
    return db().getQueryEditor().moveColumn(ac);
  }

  @POST
  @Path("/addColumn")
  public QueryResponse addColumn(@Context SecurityContext sc, AddColumnRequest ac)
      throws Exception {
    return db().getQueryEditor().addColumn(ac);
  }

  @POST
  @Path("/removeColumn")
  public QueryResponse removeColumn(@Context SecurityContext sc, RemoveColumnRequest ac)
      throws Exception {
    return db().getQueryEditor().removeColumn(ac);
  }

  @POST
  @Path("/setWhere")
  public QueryResponse setWhere(@Context SecurityContext sc, SetWhereRequest ac) throws Exception {
    return db().getQueryEditor().setWhere(ac);
  }

  @POST
  @Path("/setGroupBy")
  public QueryResponse setGroupBy(@Context SecurityContext sc, SetWhereRequest ac)
      throws Exception {
    return db().getQueryEditor().setGroupBy(ac);
  }

  @POST
  @Path("/rename")
  public QueryResponse rename(@Context SecurityContext sc, RenameRequest ac) throws Exception {
    return db().getQueryEditor().rename(ac);
  }

  @POST
  @Path("/sort")
  public QueryResponse sort(@Context SecurityContext sc, SortRequest ac) throws Exception {
    return db().getQueryEditor().sort(ac);
  }
}
