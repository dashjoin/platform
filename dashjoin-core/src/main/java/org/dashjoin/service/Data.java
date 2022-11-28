package org.dashjoin.service;

import static org.dashjoin.service.ACLContainerRequestFilter.Operation.CREATE;
import static org.dashjoin.service.ACLContainerRequestFilter.Operation.DELETE_ROW;
import static org.dashjoin.service.ACLContainerRequestFilter.Operation.UPDATE_ROW;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.function.AbstractDatabaseTrigger;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.OpenCypherQuery;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import lombok.extern.java.Log;

/**
 * The core REST data and metadata interface that sits on top of all database implementations. It
 * contains 4 parts:
 * 
 * 1) A object CRUD
 * 
 * 2) Metadata about the objects, fields and types
 * 
 * 3) A generic query interface
 * 
 * 4) metadata about the query parameters and results
 */
@Path(Services.REST_PREFIX + "database")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@ApplicationScoped
@Log
public class Data {

  @Inject
  Services services;

  public void setServices(Services services) {
    this.services = services;
  }

  @Inject
  ExpressionService expression;

  public ExpressionService getExpressionService() {
    return expression;
  }

  /**
   * searches all databases
   */
  @GET
  @Path("/search/{search}")
  @Operation(summary = "performs a full text search on all databases")
  @APIResponse(description = "Tabular query result (list of JSON objects)")
  public List<SearchResult> search(@Context SecurityContext sc, @PathParam("search") String search,
      @QueryParam("limit") Integer limit) throws Exception {
    List<SearchResult> res = new ArrayList<>();
    for (AbstractDatabase db : services.getConfig().getDatabases()) {
      try {
        if (db instanceof PojoDatabase)
          if (!sc.isUserInRole("admin"))
            // non admin user technically needs to have read access to the config
            // DB (otherwise he could not read any pages or query definitions)
            // however, only admins should be able to search the config DB during development
            continue;

        if (services.getConfig().excludeFromSearch(db))
          continue;
        String searchQuery = services.getConfig().databaseSearchQuery(db);

        List<SearchResult> tmp =
            searchQuery == null ? db.search(sc, search, limit == null ? null : limit - res.size())
                : searchQuery(sc, db, searchQuery, search);
        if (tmp != null)
          res.addAll(tmp);
        if (limit != null && res.size() >= limit)
          return res;
      } catch (Exception e) {
        // ignore exception on a single DB
      }
    }
    return res;
  }

  /**
   * searches single databases
   */
  @GET
  @Path("/search/{database}/{search}")
  @Operation(summary = "performs a full text search on all databases")
  @APIResponse(description = "Tabular query result (list of JSON objects)")
  public List<SearchResult> search(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @PathParam("search") String search, @QueryParam("limit") Integer limit) throws Exception {

    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    if (db instanceof PojoDatabase)
      if (!sc.isUserInRole("admin"))
        return Arrays.asList();

    if (services.getConfig().excludeFromSearch(db))
      return Arrays.asList();
    String searchQuery = services.getConfig().databaseSearchQuery(db);

    return searchQuery == null ? db.search(sc, search, limit == null ? null : limit)
        : searchQuery(sc, db, searchQuery, search);
  }

  /**
   * searches single table
   */
  @GET
  @Path("/search/{database}/{table}/{search}")
  @Operation(summary = "performs a full text search on all databases")
  @APIResponse(description = "Tabular query result (list of JSON objects)")
  public List<SearchResult> search(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @PathParam("search") String search, @QueryParam("limit") Integer limit) throws Exception {

    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m);
    return db.search(sc, m, search, limit);
  }

  List<SearchResult> searchQuery(SecurityContext sc, AbstractDatabase db, String searchQuery,
      String search) throws Exception {

    Map<String, Property> meta =
        this.queryMeta(sc, db.name, searchQuery, MapUtil.of("search", search));
    String table = null;
    String key = null;
    String column = null;
    for (Entry<String, Property> p : meta.entrySet())
      if (p.getValue().pkpos != null) {
        key = p.getKey();
        table = Escape.decodeTableOrColumnName(p.getValue().parent.split("/")[2]);
      } else {
        column = p.getKey();
      }

    List<Map<String, Object>> res =
        this.query(sc, db.name, searchQuery, MapUtil.of("search", search));
    List<SearchResult> projected = new ArrayList<>();

    if (table == null || key == null || column == null) {
      if (meta.size() < 3) {
        log.warning(
            "Search configuration issue: table=" + table + " key=" + key + " column=" + column);
      } else {
        for (Map<String, Object> m : res) {
          Iterator<Entry<String, Object>> i = m.entrySet().iterator();
          Entry<String, Object> t = i.next();
          Entry<String, Object> k = i.next();
          Entry<String, Object> c = i.next();
          projected.add(SearchResult.of(Resource.of(db.name, "" + t.getValue(), k.getValue()),
              c.getKey(), c.getValue()));
        }
      }
    } else {
      for (Map<String, Object> m : res) {
        projected
            .add(SearchResult.of(Resource.of(db.name, table, m.get(key)), column, m.get(column)));
      }
    }

    return projected;
  }

  /**
   * looks up the query with the given ID in the catalog, finds the right database, inserts the
   * arguments, runs the query and returns the result
   * 
   * The result has a tabular structure. If resources are returned (e.g. via a query MATCH (p:EMP)
   * return p), then the column p contains a JSON object which must include a _dj_resource field
   * pointing to dj/db/table/pk
   * 
   * consider the following query:
   * 
   * MATCH path=(start:`dj/northwind/EMPLOYEES`)-[r1:REPORTS_TO]->(boss)-[r2:REPORTS_TO]->(finish)
   * RETURN start._dj_resource, boss.LAST_NAME, path
   * 
   * On the northwind DB, this query yields the following output:
   * 
   * "start._dj_resource": { "database": "northwind", "table": "EMPLOYEES", "pk": [ 6 ] },
   * 
   * "boss.LAST_NAME": "Buchanan",
   * 
   * "path": { "start": record, "steps": [ { "edge": { "_dj_edge": "REPORTS_TO", "_dj_outbound":
   * true }, "end": record }, { "edge": { "_dj_edge": "REPORTS_TO", "_dj_outbound": true }, "end":
   * record } ] }
   */
  @POST
  @Path("/queryGraph/{database}/{queryId}")
  @Operation(
      summary = "looks up the query with the given ID in the catalog, finds the right database, inserts the arguments, runs the query and returns the result")
  @APIResponse(description = "Tabular query result (list of JSON objects)")
  public List<Map<String, Object>> queryGraph(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @PathParam("queryId") String queryId, Map<String, Object> arguments) throws Exception {
    return queryGraphInternal(sc, database, queryId, arguments, false);
  }

  /**
   * callable from JSONata (adding readOnly flag to make sure we do not run update queries in
   * preview)
   */
  public List<Map<String, Object>> queryGraphInternal(SecurityContext sc, String database,
      String queryId, Map<String, Object> arguments, boolean readOnly) throws Exception {
    if (arguments == null)
      arguments = new HashMap<>();
    QueryMeta info = services.getConfig().getQueryMeta(queryId);
    if (info == null)
      throw new Exception("Query " + queryId + " not found");

    ACLContainerRequestFilter.check(sc, info);

    // database * means: run query across all DBs using the opencypher engine
    if (database.equals("*")) {
      OpenCypherQuery q = new OpenCypherQuery(info.query, arguments);
      return q.run(services, this, sc);
    } else {
      // delegate to DB
      Database db = services.getConfig().getDatabase(dj(database));
      List<Map<String, Object>> res = db.queryGraph(info, arguments);
      if (res == null) {
        // null means that the delegate DB does not support graph queries - default to internal
        // engine (like with database='*')
        OpenCypherQuery q = new OpenCypherQuery(info.query, arguments);
        return q.run(services, this, sc);
      }
      return res;
    }
  }

  /**
   * looks up the query with the given ID in the catalog, finds the right database, inserts the
   * arguments, runs the query and returns the result
   */
  @POST
  @Path("/query/{database}/{queryId}")
  @Operation(
      summary = "looks up the query with the given ID in the catalog, finds the right database, inserts the arguments, runs the query and returns the result")
  @APIResponse(description = "Tabular query result (list of JSON objects)")
  public List<Map<String, Object>> query(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @PathParam("queryId") String queryId, Map<String, Object> arguments) throws Exception {
    return queryInternal(sc, database, queryId, arguments, false);
  }

  public List<Map<String, Object>> queryInternal(SecurityContext sc, String database,
      String queryId, Map<String, Object> arguments, boolean readOnly) throws Exception {
    if (arguments == null)
      arguments = new HashMap<>();
    QueryMeta info = services.getConfig().getQueryMeta(queryId);
    if (info == null)
      throw new Exception("Query " + queryId + " not found");

    if (readOnly && "write".equals(info.type))
      return null;

    ACLContainerRequestFilter.check(sc, info);
    Database db = services.getConfig().getDatabase(dj(database));
    return db.query(info, arguments);
  }

  /**
   * looks up the query with the given ID in the catalog, finds the right database, inserts the
   * arguments, and returns the result metadata
   */
  @POST
  @Path("/queryMeta/{database}/{queryId}")
  @Operation(
      summary = "looks up the query with the given ID in the catalog, finds the right database, inserts the arguments, and returns the result metadata")
  @APIResponse(
      description = "Map of column name (as they appear when running the query) to Property describing the column")
  public Map<String, Property> queryMeta(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @PathParam("queryId") String queryId, Map<String, Object> arguments) throws Exception {
    if (arguments == null)
      arguments = new HashMap<>();
    QueryMeta info = services.getConfig().getQueryMeta(queryId);
    ACLContainerRequestFilter.check(sc, info);
    Database db = services.getConfig().getDatabase(dj(database));
    return db.queryMeta(info, arguments);
  }

  /**
   * like read but returns all matches in a list (arguments are and-connected column equalities)
   */
  @POST
  @Path("/all/{database}/{table}")
  @Operation(
      summary = "like read but returns all matches in a list (arguments are and-connected column equalities)")
  @APIResponse(description = "Tabular result (list of JSON objects)")
  public List<Map<String, Object>> all(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
      @QueryParam("sort") String sort, @QueryParam("descending") boolean descending,
      Map<String, Object> arguments) throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m);
    db.cast(m, arguments);
    return db.all(m, offset, limit, sort, descending,
        ACLContainerRequestFilter.tenantFilter(sc, m, arguments));
  }

  // "Get all" as GET method. Enables browser cache.
  @GET
  @Path("/crud/{database}/{table}")
  @Operation(
      summary = "like read but returns all matches in a list (arguments are and-connected column equalities)")
  @APIResponse(description = "Tabular result (list of JSON objects)")
  public List<Map<String, Object>> getall(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit,
      @QueryParam("sort") String sort, @QueryParam("descending") boolean descending,
      @QueryParam("arguments") String argStr) throws Exception {
    Map<String, Object> arguments = argStr == null ? null : JSONDatabase.fromJsonString(argStr);
    return all(sc, database, table, offset, limit, sort, descending, arguments);
  }

  /**
   * return limit keys whose string representation begin with prefix
   */
  @GET
  @Path("/keys/{database}/{table}")
  @Operation(summary = "return limit keys whose string representation begin with prefix")
  @APIResponse(description = "key value and string display name (list of JSON objects)")
  public List<Choice> keys(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @QueryParam("prefix") String prefix, @QueryParam("limit") Integer limit) throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m);

    if (m.properties != null) {
      // only makes sense for tables with a single PK
      int count = 0;
      for (Property p : m.properties.values())
        if (p.pkpos != null)
          count++;
      if (count != 1)
        return Arrays.asList();
    }

    return db.keys(m, prefix, limit, ACLContainerRequestFilter.tenantFilter(sc, m, null));
  }

  /**
   * returns all table IDs from all databases known to the system
   */
  @GET
  @Path("/tables")
  @Operation(summary = "returns all table IDs from all databases known to the system")
  @APIResponse(description = "List of table ID")
  public List<String> tables() throws Exception {
    List<String> res = new ArrayList<>();
    for (AbstractDatabase s : services.getConfig().getDatabases())
      for (Table table : s.tables.values())
        if (table.ID != null)
          res.add(table.ID);
    return res;
  }

  /**
   * creates a new instance in the table associated with the table
   */
  @PUT
  @Path("/put/{database}/{table}")
  @Operation(summary = "creates a new instance in the table associated with the table")
  @APIResponse(
      description = "Returns the global identifier of the new record (dj/database/table/ID). The segments are URL encoded. ID is the primary key. For composite primary keys, ID is pk1/../pkn where pki is URL encoded again.")
  @Produces({MediaType.TEXT_PLAIN})
  public String create2(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      Map<String, Object> object) throws Exception {
    Resource res = create(sc, database, table, object);
    String s = "dj/" + res.database + "/" + Escape.encodeTableOrColumnName(res.table);
    for (Object k : res.pk)
      s = s + "/" + Escape.encodeTableOrColumnName("" + k);
    return s;
  }

  /**
   * creates a new instance in the table associated with the table
   */
  @PUT
  @Path("/crud/{database}/{table}")
  @Operation(summary = "creates a new instance in the table associated with the table")
  @APIResponse(
      description = "Returns the global identifier of the new record as an object containing database, table and primary key(s)")
  public Resource create(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      Map<String, Object> object) throws Exception {
    MapUtil.clean(object);
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m, CREATE);
    ACLContainerRequestFilter.checkRow(sc, m, object);

    // make sure PKs are present (unless this is a DDL create)
    if (!"config".equals(database))
      if (!("Table".equals(table) || "Property".equals(table)))
        if (m.properties != null)
          for (Property p : m.properties.values())
            if (p.pkpos != null)
              if (!object.containsKey(p.name))
                if (p.readOnly != null && p.readOnly)
                  // the key is an auto increment key, ok to omit it
                  ;
                else
                  throw new Exception("Record is missing the primary key column " + p.name);

    db.cast(m, object);

    if (dbTriggers(sc, "create", database, table, null, object, m.beforeCreate))
      db.create(m, object);
    dbTriggers(sc, "create", database, table, null, object, m.afterCreate);
    return Resource.of(db, m, object);
  }

  boolean dbTriggers(SecurityContext sc, String command, String database, String table,
      Map<String, Object> search, Map<String, Object> object, String t) throws Exception {
    if (t == null)
      // no trigger, continue
      return true;

    Function<?, ?> f = null;
    for (Function<?, ?> s : SafeServiceLoader.load(Function.class)) {
      if (s instanceof AbstractDatabaseTrigger)
        if (t.equals("$" + s.getID() + "()"))
          f = s;
    }

    if (f != null) {
      AbstractDatabaseTrigger.Config context = new AbstractDatabaseTrigger.Config();
      context.command = command;
      context.database = database;
      context.table = table;
      context.search = search;
      context.object = object;
      ((AbstractFunction<?, ?>) f).init(sc, services, expression, false);
      ACLContainerRequestFilter.check(sc, f);
      return ((AbstractDatabaseTrigger) f).run(context);
    }

    Map<String, Object> context = new HashMap<>();
    context.put("command", command);
    context.put("database", database);
    context.put("table", table);
    context.put("search", search);
    context.put("object", object);
    expression.resolve(sc, t, context);
    return true;
  }

  @GET
  @Path("/traverse/{database}/{table}/{objectId1}")
  @Operation(
      summary = "starting at the object defined by the given globally unique identifier, reads the object related via fk")
  @APIResponse(description = "JSON object representing the record")
  public Object traverse(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Primary key of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @QueryParam("fk") String fk) throws Exception {
    Map<String, Object> o = read(sc, database, table, Arrays.asList(objectId1));
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    Property p = m.properties.get(fk);
    if (p != null && p.ref != null) {
      // fk is an outgoing fk
      String[] parts = m.properties.get(fk).ref.split("/");
      if (o.get(fk) == null)
        return null;
      return read(sc, parts[1], parts[2], "" + o.get(fk));
    } else {
      String[] parts = fk.split("/");
      AbstractDatabase db2 = services.getConfig().getDatabase(dj(parts[1]));
      Table m2 = db2.tables.get(parts[2]);
      // get all of the related table where fk = pk
      return all(sc, parts[1], parts[2], null, null, null, false,
          MapUtil.of(fk(m2, pk(m).ID).name, o.get(pk(m).name)));
    }
  }

  Property pk(Table t) {
    for (Property p : t.properties.values())
      if (p.pkpos != null)
        return p;
    return null;
  }

  Property fk(Table t, String ref) {
    for (Property p : t.properties.values())
      if (ref.equals(p.ref))
        return p;
    return null;
  }

  /**
   * reads the object defined by the given globally unique identifier
   */
  @GET
  @Path("/crud/{database}/{table}/{objectId1}")
  @Operation(summary = "reads the object defined by the given globally unique identifier")
  @APIResponse(description = "JSON object representing the record")
  public Map<String, Object> read(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Primary key of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1)
      throws Exception {
    return read(sc, database, table, Arrays.asList(objectId1));
  }

  @GET
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}")
  @Operation(summary = "reads the object defined by the given globally unique identifier")
  @APIResponse(description = "JSON object representing the record")
  public Map<String, Object> read(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2)
      throws Exception {
    return read(sc, database, table, Arrays.asList(objectId1, objectId2));
  }

  @GET
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}")
  @Operation(summary = "reads the object defined by the given globally unique identifier")
  @APIResponse(description = "JSON object representing the record")
  public Map<String, Object> read(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3)
      throws Exception {
    return read(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3));
  }

  @GET
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}/{objectId4}")
  @Operation(summary = "reads the object defined by the given globally unique identifier")
  @APIResponse(description = "JSON object representing the record")
  public Map<String, Object> read(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      @Parameter(description = "Composite primary key 4 of the record to operate on",
          example = "1") @PathParam("objectId4") String objectId4)
      throws Exception {
    return read(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3, objectId4));
  }

  public Map<String, Object> read(@Context SecurityContext sc, String database, String table,
      List<String> objectId) throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m);
    Map<String, Object> search = key(m, objectId);
    db.cast(m, search);
    Map<String, Object> res = db.read(m, search);
    if (res == null)
      if ("config".equals(database) && "namespace".equals(search.get("ID")))
        // avoid 401 for namespace query so apps can still boostrap namespace.json
        return MapUtil.of("map", MapUtil.of());
      else
        throw new NotFoundException();
    ACLContainerRequestFilter.checkRow(sc, m, res);
    return res;
  }

  /**
   * represents an incoming (fk) link to an object
   */
  @Schema(title = "Origin: represents an incoming (fk) link to an object")
  public static class Origin {

    @Schema(title = "ID of the record where the link originates")
    public Resource id;

    @Schema(title = "ID of the pk column")
    public String pk;

    @Schema(title = "ID of the fk column")
    public String fk;
  }

  /**
   * represents an object ID
   */
  @Schema(title = "Resource: represents an object ID")
  public static class Resource {

    @Schema(title = "name of the database")
    public String database;

    @Schema(title = "name of the table")
    public String table;

    @Schema(title = "primary keys")
    public List<Object> pk = new ArrayList<>();

    public static Resource of(AbstractDatabase d, Table s, Map<String, Object> object) {
      Resource res = new Resource();
      res.database = d.name;
      res.table = s.name;
      for (int i = 0; i < s.properties.size(); i++)
        for (Property f : s.properties.values())
          if (f.pkpos != null)
            if (f.pkpos == i)
              res.pk.add(object.get(f.name));
      return res;
    }

    @SuppressWarnings("unchecked")
    public static Resource of(String d, String s, Object pk) {
      Resource res = new Resource();
      res.database = d;
      res.table = s;
      if (pk instanceof List)
        res.pk = (List<Object>) pk;
      else
        res.pk = Arrays.asList(pk);
      return res;
    }
  }

  /**
   * represents a search result
   */
  @Schema(title = "SearchResult: represents a search result")
  public static class SearchResult {

    @Schema(title = "ID of the record that matches")
    public Resource id;

    @Schema(title = "name of the column containing the match")
    public String column;

    @Schema(title = "matching value")
    public Object match;

    public static SearchResult of(Resource id, String column, Object match) {
      SearchResult res = new SearchResult();
      res.id = id;
      res.column = column;
      res.match = match;
      return res;
    }
  }

  /**
   * choice for helping the user pick a correct foreign key
   */
  @Schema(title = "Choice: represents a foreign key choice for select UIs")
  public static class Choice {

    @Schema(title = "string representation")
    public String name;

    @Schema(title = "key value")
    public Object value;

  }

  /**
   * returns IDs of objects who have a FK pointing to the object defined by the given globally
   * unique identifier
   */
  @GET
  @Path("/incoming/{database}/{table}/{objectId1}")
  @Operation(
      summary = "returns IDs of objects who have a FK pointing to the object defined by the given globally unique identifier")
  @APIResponse(description = "List of incoming links")
  public List<Origin> incoming(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Primary key of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws Exception {
    return incoming(sc, database, table, Arrays.asList(objectId1), offset, limit);
  }

  @GET
  @Path("/incoming/{database}/{table}/{objectId1}/{objectId2}")
  @Operation(
      summary = "returns IDs of objects who have a FK pointing to the object defined by the given globally unique identifier")
  @APIResponse(description = "List of incoming links")
  public List<Origin> incoming(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws Exception {
    return incoming(sc, database, table, Arrays.asList(objectId1, objectId2), offset, limit);
  }

  @GET
  @Path("/incoming/{database}/{table}/{objectId1}/{objectId2}/{objectId3}")
  @Operation(
      summary = "returns IDs of objects who have a FK pointing to the object defined by the given globally unique identifier")
  @APIResponse(description = "List of incoming links")
  public List<Origin> incoming(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws Exception {
    return incoming(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3), offset,
        limit);
  }

  @GET
  @Path("/incoming/{database}/{table}/{objectId1}/{objectId2}/{objectId3}/{objectId4}")
  @Operation(
      summary = "returns IDs of objects who have a FK pointing to the object defined by the given globally unique identifier")
  @APIResponse(description = "List of incoming links")
  public List<Origin> incoming(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      @Parameter(description = "Composite primary key 4 of the record to operate on",
          example = "1") @PathParam("objectId4") String objectId4,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws Exception {
    return incoming(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3, objectId4),
        offset, limit);
  }

  List<Origin> incoming(@Context SecurityContext sc, String database, String table,
      List<String> objectIds, Integer offset, Integer limit) throws Exception {

    long start = System.currentTimeMillis();
    Integer timeout = services.getConfig().getAllTimeoutMs();

    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);
    ACLContainerRequestFilter.check(sc, db, m);
    String objectId = objectIds.get(0);

    String pk = null;
    for (Property p : m.properties.values())
      if (p.pkpos != null) {
        if (pk != null)
          // incoming for composite key not yet supported
          // https://github.com/dashjoin/query-editor/issues/62
          return Arrays.asList();
        pk = p.ID;
      }

    if (pk == null)
      return Arrays.asList();

    List<Origin> res = new ArrayList<>();
    for (AbstractDatabase d : services.getConfig().getDatabases())
      res.addAll(d.incoming(sc, database, table, objectId, offset, limit, start, timeout, pk));
    return res;
  }

  /**
   * like read, but returns all keys posted
   */
  @POST
  @Path("/list/{database}/{table}")
  @Operation(summary = "like read, but returns all keys posted")
  @APIResponse(description = "Map of objectId to JSON object representing the record")
  public Map<String, Map<String, Object>> list(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      List<String> objectIds) throws Exception {
    Map<String, Map<String, Object>> res = new HashMap<>();
    for (String objectId : objectIds) {
      res.put(objectId, read(sc, database, table, objectId));
    }
    return res;
  }

  /**
   * updates the object defined by the globally unique identifier with the values provided in object
   * 
   * @param object object a map of keys that are set to new values. Columns / keys that are omitted
   *        are not touched. If key=null, the key is deleted. All other keys are replaced (no
   *        merging takes place
   */
  @POST
  @Path("/crud/{database}/{table}/{objectId1}")
  @Operation(
      summary = "updates the object defined by the globally unique identifier with the values provided in object")
  public void update(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Primary key of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      Map<String, Object> object) throws Exception {
    update(sc, database, table, Arrays.asList(objectId1), object);
  }

  @POST
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}")
  @Operation(
      summary = "updates the object defined by the globally unique identifier with the values provided in object")
  public void update(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      Map<String, Object> object) throws Exception {
    update(sc, database, table, Arrays.asList(objectId1, objectId2), object);
  }

  @POST
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}")
  @Operation(
      summary = "updates the object defined by the globally unique identifier with the values provided in object")
  public void update(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      Map<String, Object> object) throws Exception {
    update(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3), object);
  }

  @POST
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}/{objectId4}")
  @Operation(
      summary = "updates the object defined by the globally unique identifier with the values provided in object")
  public void update(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      @Parameter(description = "Composite primary key 4 of the record to operate on",
          example = "1") @PathParam("objectId4") String objectId4,
      Map<String, Object> object) throws Exception {
    update(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3, objectId4), object);
  }

  void update(SecurityContext sc, String database, String table, List<String> objectId,
      Map<String, Object> object) throws Exception {
    MapUtil.clean(object);
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);

    if (m.tenantColumn != null)
      read(sc, database, table, objectId);

    ACLContainerRequestFilter.check(sc, db, m, UPDATE_ROW);
    Map<String, Object> search = key(m, objectId);

    // make sure we do not change a PK
    for (Entry<String, Object> e : search.entrySet()) {
      Object pk = object.get(e.getKey());
      if (pk != null)
        if (!pk.toString().equals("" + e.getValue()))
          throw new Exception("Cannot change key: " + e.getKey());
    }

    db.cast(m, search);
    db.cast(m, object);

    if (!dbTriggers(sc, "update", database, table, search, object, m.beforeUpdate))
      return;
    if (!db.update(m, search, object))
      throw new NotFoundException();
    dbTriggers(sc, "update", database, table, search, object, m.afterUpdate);
  }

  /**
   * deletes the object defined by the globally unique identifier
   */
  @DELETE
  @Path("/crud/{database}/{table}/{objectId1}")
  @Operation(summary = "deletes the object defined by the globally unique identifier")
  public void delete(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Primary key of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1)
      throws Exception {
    delete(sc, database, table, Arrays.asList(objectId1));
  }

  @DELETE
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}")
  @Operation(summary = "deletes the object defined by the globally unique identifier")
  public void delete(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2)
      throws Exception {
    delete(sc, database, table, Arrays.asList(objectId1, objectId2));
  }

  @DELETE
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}")
  @Operation(summary = "deletes the object defined by the globally unique identifier")
  public void delete(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3)
      throws Exception {
    delete(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3));
  }

  @DELETE
  @Path("/crud/{database}/{table}/{objectId1}/{objectId2}/{objectId3}/{objectId4}")
  @Operation(summary = "deletes the object defined by the globally unique identifier")
  public void delete(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database,
      @Parameter(description = "table name to run the operation on",
          example = "EMPLOYEES") @PathParam("table") String table,
      @Parameter(description = "Composite primary key 1 of the record to operate on",
          example = "1") @PathParam("objectId1") String objectId1,
      @Parameter(description = "Composite primary key 2 of the record to operate on",
          example = "1") @PathParam("objectId2") String objectId2,
      @Parameter(description = "Composite primary key 3 of the record to operate on",
          example = "1") @PathParam("objectId3") String objectId3,
      @Parameter(description = "Composite primary key 4 of the record to operate on",
          example = "1") @PathParam("objectId4") String objectId4)
      throws Exception {
    delete(sc, database, table, Arrays.asList(objectId1, objectId2, objectId3, objectId4));
  }

  void delete(SecurityContext sc, String database, String table, List<String> objectId)
      throws Exception {
    AbstractDatabase db = services.getConfig().getDatabase(dj(database));
    Table m = db.tables.get(table);

    if (m.tenantColumn != null)
      read(sc, database, table, objectId);

    ACLContainerRequestFilter.check(sc, db, m, DELETE_ROW);
    Map<String, Object> search = key(m, objectId);
    db.cast(m, search);
    if (!dbTriggers(sc, "delete", database, table, search, null, m.beforeDelete))
      return;
    if (!db.delete(m, search))
      throw new NotFoundException();
    dbTriggers(sc, "delete", database, table, search, null, m.afterDelete);
  }

  /**
   * converts the URL encoded single primary key into a map where the key is the tables's primary
   * key column name
   */
  Map<String, Object> key(Table type, List<String> id) throws Exception {
    if (type == null)
      throw new IllegalArgumentException("Unknown table");
    Map<String, Object> res = new HashMap<>();
    if (type.properties != null) {
      for (Property p : type.properties.values())
        if (p.pkpos != null) {
          if (p.pkpos >= id.size())
            throw new IllegalArgumentException("Missing composite key: " + p.name);
          String s = id.get(p.pkpos);
          res.put(p.name, s);
        }
    }
    if (res.isEmpty())
      throw new Exception("Operation requires table with a primary key");
    return res;
  }

  String dj(String database) {
    return services.getDashjoinID() + "/" + database;
  }
}
