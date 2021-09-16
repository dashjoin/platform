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

  public static class Query {
    public String query;
    public Map<String, Object> arguments;
  }

  @POST
  @Path("/query")
  public List<Map<String, Object>> query(Query q) throws Exception {
    QueryMeta qi = QueryMeta.ofQuery(q.query);
    qi.database = db().ID;
    return db().query(qi, q.arguments);
  }

  @POST
  @Path("/queryMeta")
  public Map<String, Property> queryMeta(Query q) throws Exception {
    QueryMeta qi = QueryMeta.ofQuery(q.query);
    qi.type = "read";
    qi.database = db().ID;
    return db().queryMeta(qi, q.arguments);
  }

  @POST
  @Path("/connectAndCollectMetadata/{id}")
  public Map<String, Object> connectAndCollectMetadata(@PathParam("id") String id,
      Map<String, Table> tables) throws Exception {
    db().tables = tables;
    db().ID = id;
    return db().connectAndCollectMetadata();
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
    return db().all(Table.ofName(table), offset, limit, sort, descending, arguments);
  }
}
