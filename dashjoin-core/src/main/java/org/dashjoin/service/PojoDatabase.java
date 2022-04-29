package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;
import org.dashjoin.util.MapUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The actual config database instance. Sits on top of the union database. It maintains a cache of
 * pojos that correspond to the JSON instances. This cache allows the pojos to maintain a state
 * (e.g. for establishing connections to the database they represent)
 */
public class PojoDatabase extends UnionDatabase implements Config {

  private final static Logger logger = Logger.getLogger(PojoDatabase.class.getName());

  /**
   * singleton mapper
   */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public PojoDatabase() {}

  @Inject
  public PojoDatabase(Services services) {
    super(services);
  }

  /**
   * internal pojo cache
   * 
   * TODO: must be cluster-aware in the future (same with file system - use object store instead of
   * file system)
   */
  Map<String, AbstractDatabase> _cache;

  Map<String, AbstractDatabase> cache() {
    if (_cache != null)
      return _cache;
    return ((PojoDatabase) services.getConfig())._cache;
  }

  @Override
  public PojoDatabase getConfigDatabase() throws Exception {
    return (PojoDatabase) getDatabase(services.getDashjoinID() + "/config");
  }

  @Override
  public void addDB(ProviderDatabase db) {
    dbs().add(db);
  }

  /**
   * get database with id
   * 
   * @param id id of the database
   * 
   * @return the database with id
   * @throws IllegalArgumentException if no db with id is found
   */
  @Override
  public AbstractDatabase getDatabase(String id) throws Exception {
    AbstractDatabase db = get("dj-database", id, AbstractDatabase.class);
    if (db == null)
      throw new IllegalArgumentException("Unknown database: " + id);
    if (db instanceof PojoDatabase)
      ((PojoDatabase) db).services = this.services;
    return db;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractFunction<Object, Object> getFunction(String id) throws Exception {
    AbstractFunction<Object, Object> db = get("dj-function", id, AbstractFunction.class);
    if (db == null)
      throw new IllegalArgumentException("Unknown function: " + id);
    return db;
  }

  /**
   * get all databases
   */
  @Override
  public List<AbstractDatabase> getDatabases() throws Exception {
    return all("dj-database", AbstractDatabase.class);
  }

  /**
   * get query with id
   */
  @Override
  public QueryMeta getQueryMeta(String id) throws Exception {
    QueryMeta res = get("dj-query-catalog", id, QueryMeta.class);

    // avoid having to have a query catalog entry for pojo method config queries #209
    if (res == null)
      for (Method m : getClass().getMethods()) {
        ConfigQuery cq = m.getAnnotation(ConfigQuery.class);
        if (cq != null)
          if (cq.query().equals(id)) {
            res = new QueryMeta();
            res.ID = id;
            res.query = id;
            res.roles = Arrays.asList("authenticated");
          }
      }

    return res;
  }

  /**
   * get schema by ID
   */
  @Override
  public Table getSchema(String ID) throws Exception {
    for (Database m : getDatabases())
      if (((AbstractDatabase) m).tables != null)
        for (Table s : ((AbstractDatabase) m).tables.values()) {
          // may be called with a table ID or a column ID
          if (ID.equals(s.ID))
            return s;
          if (ID.startsWith(s.ID + "/"))
            return s;
        }
    return null;
  }

  /**
   * like query("get all") but maps to pojos
   */
  <T> List<T> all(String table, Class<T> type) throws Exception {
    List<T> res = new ArrayList<T>();
    QueryMeta info = new QueryMeta();
    info.query = table;
    for (Map<String, Object> i : query(info, null))
      try {
        res.add(convert(table, i, type));
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Object " + i.get("ID") + " cannot be loaded from JSON: " + type,
            e);
      }
    return res;
  }

  /**
   * like read(id) but maps to pojo
   */
  <T> T get(String table, String id, Class<T> type) throws Exception {
    Table s = new Table();
    s.name = table;
    Map<String, Object> res = read(s, of("ID", id));
    return convert(table, res, type);
  }

  /**
   * converts the object's Map representation into pojo and calls open() and close() and registers
   * metadata providers
   */
  @SuppressWarnings("unchecked")
  <T> T convert(String table, Map<String, Object> res, Class<T> type) throws Exception {

    if (res == null)
      return null;

    String clazz = (String) res.get("djClassName");
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    try {
      T pojo = objectMapper.convertValue(res, clazz == null ? type
          : (Class<T>) Class.forName(clazz, false, Thread.currentThread().getContextClassLoader()));

      if (pojo instanceof AbstractDatabase) {
        ((AbstractDatabase) pojo).init(services);
      }

      return pojo;
    } catch (Throwable t) {
      // Hard errors, like ClassNotFound etc.
      System.err.println("Error in convert: " + t);
      return null;
    }
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    if (s.name.equals("Table")) {
      QueryMeta info = new QueryMeta();
      info.query = "dj-database";
      for (Map<String, Object> i : query(info, null)) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> tables =
            (Map<String, Map<String, Object>>) i.get("tables");
        if (tables != null)
          for (Map<String, Object> e : tables.values())
            if (search.get("ID").equals(e.get("ID")))
              return e;
      }
    }
    if (s.name.equals("Property")) {
      QueryMeta info = new QueryMeta();
      info.query = "dj-database";
      for (Map<String, Object> i : query(info, null)) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> tables =
            (Map<String, Map<String, Object>>) i.get("tables");
        if (tables != null)
          for (Map<String, Object> e : tables.values()) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> properties =
                (Map<String, Map<String, Object>>) e.get("properties");
            if (properties != null)
              for (Map<String, Object> p : properties.values())
                if (search.get("ID").equals(p.get("ID")))
                  return p;
          }
      }
    }
    return super.read(s, search);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Map<String, Object>> query(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {

    if (qi.query.equals("dj-query-performance"))
      return new PerformanceDatabase().query(qi, arguments);

    // custom queries are possible by implementing this signature and using the ConfigQuery
    // annotation
    for (Method m : getClass().getMethods()) {
      ConfigQuery cq = m.getAnnotation(ConfigQuery.class);
      if (cq != null)
        if (cq.query().equals(qi.query))
          return (List<Map<String, Object>>) m.invoke(this, qi, arguments);
    }

    List<Map<String, Object>> res = new ArrayList<>();
    if (qi.query.equals("Table") || qi.query.startsWith("Table/")) {
      boolean all = qi.query.equals("Table");
      String name = all ? null : qi.query.split("/")[1];
      QueryMeta info = new QueryMeta();
      info.query = "dj-database";
      for (Map<String, Object> i : query(info, null)) {
        Map<String, Map<String, Object>> tables =
            (Map<String, Map<String, Object>>) i.get("tables");
        if (tables != null)
          for (Map<String, Object> e : tables.values())
            if (all || name.equals(e.get("name")))
              res.add(e);
      }
    }
    if (qi.query.equals("Property") || qi.query.startsWith("Property/")) {
      boolean all = qi.query.equals("Property");
      String name = all ? null : qi.query.split("/")[1];
      QueryMeta info = new QueryMeta();
      info.query = "dj-database";
      for (Map<String, Object> i : query(info, null)) {
        Map<String, Map<String, Object>> tables =
            (Map<String, Map<String, Object>>) i.get("tables");
        if (tables != null)
          for (Map<String, Object> e : tables.values()) {
            Map<String, Map<String, Object>> properties =
                (Map<String, Map<String, Object>>) e.get("properties");
            if (properties != null)
              for (Map<String, Object> p : properties.values())
                if (all || name.equals(p.get("name")))
                  res.add(p);
          }
      }
    }
    res.addAll(super.query(qi, arguments));
    return res;
  }

  /**
   * delegate and maintain cache
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    if (schema.name.equals("Table")) {

      // on Table, we only edit these props, ignore all others
      MapUtil.keyWhitelist(object,
          Arrays.asList("dj-label", "before-create", "after-create", "before-update",
              "after-update", "before-delete", "after-delete", "instanceLayout", "tableLayout",
              "writeRoles", "readRoles"));

      String[] parts = Escape.parseTableID((String) search.get("ID"));

      // read db to update from user()
      Map<String, Object> db =
          user().read(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));

      // create if it does not yet exist in user DB
      if (db == null) {
        db = MapUtil.of("ID", parts[0] + "/" + parts[1]);
        user().create(Table.ofName("dj-database"), db);
      }

      // create tables if not there
      if (!db.containsKey("tables")) {
        db.put("tables", MapUtil.of());
        user().update(Table.ofName("dj-database"), MapUtil.of("ID", db.get("ID")),
            MapUtil.of("tables", MapUtil.of()));
        // user().create(Table.ofName("dj-database"), db);
      }

      // create table key
      Map<String, Object> tables = (Map<String, Object>) db.get("tables");
      if (!tables.containsKey(parts[2]))
        tables.put(parts[2], MapUtil.of());

      // copy keys
      Map<String, Object> table = (Map<String, Object>) tables.get(parts[2]);
      for (Entry<String, Object> e : object.entrySet())
        if (e.getValue() == null)
          table.remove(e.getKey());
        else
          table.put(e.getKey(), e.getValue());

      // remove table if empty
      if (table.isEmpty())
        tables.remove(parts[2]);
      if (tables.isEmpty())
        db.remove("tables");

      if (db.size() == 1)
        user().delete(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));
      else
        user().update(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]),
            MapUtil.of("tables", tables.isEmpty() ? null : tables));
      return true;
    }
    if (schema.name.equals("Property")) {

      // on Table, we only edit these props, ignore all others
      MapUtil.keyWhitelist(object, Arrays.asList("pkpos", "ref", "displayWith", "createOnly"));

      String[] parts = Escape.parseColumnID((String) search.get("ID"));

      // read db to update from user()
      Map<String, Object> db =
          user().read(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));

      // create if it does not yet exist in user DB
      if (db == null) {
        db = MapUtil.of("ID", parts[0] + "/" + parts[1]);
        user().create(Table.ofName("dj-database"), db);
      }

      // create tables if not there
      if (!db.containsKey("tables"))
        db.put("tables", MapUtil.of());

      // create table key
      Map<String, Object> tables = (Map<String, Object>) db.get("tables");
      if (!tables.containsKey(parts[2]))
        tables.put(parts[2], MapUtil.of());

      // copy keys
      Map<String, Object> table = (Map<String, Object>) tables.get(parts[2]);

      // create properties if not there
      if (!table.containsKey("properties"))
        table.put("properties", MapUtil.of());

      // create property key
      Map<String, Object> properties = (Map<String, Object>) table.get("properties");
      if (!properties.containsKey(parts[3]))
        properties.put(parts[3], MapUtil.of());

      Map<String, Object> property = (Map<String, Object>) properties.get(parts[3]);
      for (Entry<String, Object> e : object.entrySet())
        if (e.getValue() == null)
          property.remove(e.getKey());
        else
          property.put(e.getKey(), e.getValue());

      // remove property if empty
      if (property.isEmpty())
        properties.remove(parts[3]);
      if (properties.isEmpty())
        table.remove("properties");

      // remove table if empty
      if (table.isEmpty())
        tables.remove(parts[2]);
      if (tables.isEmpty())
        db.remove("tables");

      if (db.size() == 1)
        user().delete(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));
      else
        user().update(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]),
            MapUtil.of("tables", tables.isEmpty() ? null : tables));
      return true;
    }

    if (schema.name.equals("dj-database")) {
      // when DB is updated, ignore tables
      object.remove("tables");
    }

    if (super.update(schema, search, object)) {
      String id = "" + search.get("ID");
      if (schema.name.equals("dj-database")) {
        removeCache(id);
        metadataCollection(getDatabase(id));
      }
      return true;
    }
    return false;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    if (m.name.equals("dj-database")) {
      String name = (String) object.get("name");
      if (name != null && !name.equals(URLEncoder.encode(name, StandardCharsets.UTF_8)))
        throw new Exception("Database name must not contain special characters");
      String djClassName = (String) object.get("djClassName");
      String url = (String) object.get("url");
      if (!(services.persistantDB instanceof JSONFileDatabase))
        if ("org.dashjoin.service.SQLDatabase".equals(djClassName))
          if (url != null)
            if (url.startsWith("jdbc:sqlite:"))
              if (!url.contains("/tmp/"))
                throw new Exception(
                    "In the cloud, only non-persistent SQLite databases in the /tmp folder are supported");
    }
    if (m.name.equals("Dashjoin")) {
      String name = (String) object.get("ID");
      if (name != null && !name.equals(URLEncoder.encode(name, StandardCharsets.UTF_8)))
        throw new Exception("Dashjoin name must not contain special characters");
    }
    super.create(m, object);
    if (m.name.equals("dj-database"))
      metadataCollection(getDatabase((String) object.get("ID")));
  }

  /**
   * delegate and maintain cache
   */
  @Override
  public boolean delete(Table schema, Map<String, Object> search) throws Exception {
    if (schema.name.equals("Table")) {
      String[] parts = ((String) search.get("ID")).split("/");
      Map<String, Object> db =
          user().read(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));
      if (db == null)
        // we're only reading the user() part, so db == null does not mean there is no data
        return true;
      @SuppressWarnings("unchecked")
      Map<String, Object> tables = (Map<String, Object>) db.get("tables");
      if (tables != null && tables.containsKey(parts[2])) {
        tables.remove(parts[2]);
        user().update(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]),
            MapUtil.of("tables", tables.isEmpty() ? null : tables));
      }
      return true;
    }
    if (schema.name.equals("Property")) {
      String[] parts = ((String) search.get("ID")).split("/");
      Map<String, Object> db =
          user().read(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]));
      if (db == null)
        return true;
      @SuppressWarnings("unchecked")
      Map<String, Object> tables = (Map<String, Object>) db.get("tables");
      if (tables != null && tables.containsKey(parts[2])) {
        // copy keys
        @SuppressWarnings("unchecked")
        Map<String, Object> table = (Map<String, Object>) tables.get(parts[2]);

        // create properties if not there
        if (!table.containsKey("properties"))
          table.put("properties", MapUtil.of());

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) table.get("properties");
        if (properties != null && properties.containsKey(parts[3])) {
          properties.remove(parts[3]);
          if (properties.isEmpty())
            table.remove("properties");

          // remove table if empty
          if (table.isEmpty())
            tables.remove(parts[2]);
          if (tables.isEmpty())
            db.remove("tables");

          user().update(Table.ofName("dj-database"), of("ID", parts[0] + "/" + parts[1]),
              MapUtil.of("tables", tables.isEmpty() ? null : tables));
        }
        return true;
      } else
        return true;
    }

    String id = "" + search.get("ID");
    if (schema.name.equals("dj-database"))
      if (read(schema, search) != null)
        removeCache(id);

    return super.delete(schema, search);
  }

  /**
   * remove pojo from cache and also remove the metadata provider
   */
  void removeCache(String id) throws Exception {
    AbstractDatabase db = cache().get(id);
    if (db != null)
      try {
        db.close();
      } catch (Exception ignore) {
        logger.warning("error closing db" + ignore);
      }
    JSONDatabase toDel = null;
    for (JSONDatabase p : dbs())
      if (p instanceof ProviderDatabase)
        if (((ProviderDatabase) p).ID.equals(id))
          toDel = p;
    if (toDel != null)
      dbs().remove(toDel);
    cache().remove(id);
  }

  /**
   * custom query. filters the config DB and projects ID, url, username
   */
  @ConfigQuery(query = "dj-databases-no-config", table = "dj-database")
  public List<Map<String, Object>> queryDatabases(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "dj-database";
    List<AbstractDatabase> objects = this.getDatabases();
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      if ("dj/config".equals(r.get("ID")))
        continue;
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      for (AbstractDatabase object : objects)
        if (object.ID.equals(r.get("ID")))
          tm.put("url", object.displayUrl());
      tm.put("username", r.get("username"));
      tm.put("status", r.get("status"));
      @SuppressWarnings("unchecked")
      Map<String, Object> tables = (Map<String, Object>) r.get("tables");
      tm.put("tables", tables == null ? null : tables.size());
      projected.add(tm);
    }
    return projected;
  }

  /**
   * get simple class name
   */
  Object simple(Object name) {
    if (name instanceof String) {
      String[] parts = ((String) name).split("\\.");
      if (parts.length == 0)
        return "";
      else
        return parts[parts.length - 1];
    } else
      return name;
  }

  /**
   * display roles list without [ ]
   */
  Object roles(Object roles) {
    if (roles instanceof List<?>) {
      String res = "";
      for (Object item : (List<?>) roles)
        res = res + item + ", ";
      if (res.isEmpty())
        return res;
      else
        return res.substring(0, res.length() - 2);
    }
    return roles;
  }

  /**
   * custom query on functions. projects ID, djClass, type, roles
   */
  @ConfigQuery(query = "dj-functions", table = "dj-function")
  public List<Map<String, Object>> queryFunctions(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "dj-function";
    return q(db, false);
  }

  /**
   * custom query for queries. projects ID, database, type, roles
   */
  @ConfigQuery(query = "dj-queries", table = "dj-query-catalog")
  public List<Map<String, Object>> queryQueries(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "dj-query-catalog";
    return q(db, true);
  }

  List<Map<String, Object>> q(QueryMeta db, boolean database) throws Exception {
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      if (database) {
        tm.put("database", r.get("database"));
        tm.put("query", r.get("query"));
      } else {
        tm.put("class", simple(r.get("djClassName")));
        tm.put("database", r.get("database"));
      }
      tm.put("type", r.get("type"));
      tm.put("roles", roles(r.get("roles")));
      if (!database) {
        tm.put("status", r.get("status"));
        Object start = r.get("start");
        Object end = r.get("end");
        tm.put("start", start);
        tm.put("end", end);
        if (start instanceof String) {
          try {
            Date s = Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) start)));
            Date e = Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) end)));
            tm.put("runtime (s)", (e.getTime() - s.getTime()) / 1000);
          } catch (Exception e) {
            // ignore
          }
        }
      }
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. generates the navigation tree
   */
  @ConfigQuery(query = "dj-navigation", table = "dj-database")
  public List<Map<String, Object>> queryNavigation(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    List<AbstractDatabase> objects = this.getDatabases();
    objects.sort(new Comparator<AbstractDatabase>() {
      @Override
      public int compare(AbstractDatabase o1, AbstractDatabase o2) {
        try {
          return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
        } catch (Exception e) {
          return 0;
        }
      }
    });
    List<Map<String, Object>> projected = new ArrayList<>();

    List<Map<String, Object>> configs = new ArrayList<>();
    configs.add(of("name", "Databases", "href", asList("/table", "config", "dj-database"),
        "children", asList()));
    configs.add(of("name", "Query Catalog", "href", asList("/table", "config", "dj-query-catalog"),
        "children", asList()));
    configs.add(of("name", "Dashboard Pages", "href", asList("/table", "config", "page"),
        "children", asList()));
    configs.add(of("name", "Functions", "href", asList("/table", "config", "dj-function"),
        "children", asList()));
    configs.add(of("name", "Info", "href", asList("/page", "Info"), "children", asList()));
    projected.add(of("ID", of("display", "Configuration", "children", configs)));

    QueryMeta db = new QueryMeta();
    db.query = "page";
    List<Map<String, Object>> pages = this.query(db, null);
    pages.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        try {
          return ((String) o1.get("ID")).toLowerCase()
              .compareTo(((String) o2.get("ID")).toLowerCase());
        } catch (Exception e) {
          return 0;
        }
      }
    });
    List<Map<String, Object>> dashboards = new ArrayList<>();
    for (Map<String, Object> page : pages) {
      if ("Home".equals(page.get("ID")))
        continue;
      if ("Info".equals(page.get("ID")))
        continue;
      if ("Account".equals(page.get("ID")))
        continue;
      if ("default".equals(page.get("ID")))
        continue;
      if ("search".equals(page.get("ID")))
        continue;
      dashboards.add(of("name", page.get("ID"), "href", asList("/page", page.get("ID")), "children",
          asList()));
    }
    projected.add(of("ID", of("display", "Dashboards", "children", dashboards)));

    for (AbstractDatabase r : objects) {
      if ("dj/config".equals(r.ID))
        continue;
      List<Map<String, Object>> tabs = new ArrayList<>();
      List<Table> sortedTables = new ArrayList<>(r.tables.values());
      sortedTables.sort(new Comparator<Table>() {
        @Override
        public int compare(Table o1, Table o2) {
          try {
            return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
          } catch (Exception e) {
            return 0;
          }
        }
      });
      for (Table i : sortedTables)
        if (i.name != null)
          tabs.add(ImmutableMap.of("name", i.name, "children", Arrays.asList(), "href",
              Arrays.asList("/table", r.name, i.name)));
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", ImmutableMap.of("name", r.name, "href",
          Arrays.asList("/resource", "config", "dj-database", r.ID), "children", tabs));
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. filters config/page and projects ID and the computed url
   */
  @ConfigQuery(query = "dj-page-urls", table = "page")
  public List<Map<String, Object>> queryPageUrls(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "page";
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("url", "/page/" + Escape.e((String) r.get("ID")));
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. get all non config tables
   */
  @ConfigQuery(query = "dj-tables", table = "Table")
  public List<Map<String, Object>> queryTables(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "Table";
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      if (r.get("parent") != null && ((String) r.get("parent")).endsWith("/config"))
        continue;
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. provides an overview of which layouts are changed
   */
  @ConfigQuery(query = "dj-layouts", table = "Table")
  public List<Map<String, Object>> queryInstanceLayout(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "Table";
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      if (r.containsKey("instanceLayout"))
        tm.put("Custom Instance Layout", "X");
      if (r.containsKey("tableLayout"))
        tm.put("Custom Table Layout", "X");
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. lists all dj-roles plus "admin"
   */
  @ConfigQuery(query = "dj-roles-without-admin", table = "dj-role")
  public List<Map<String, Object>> queryRolesWithAdmin(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "dj-role";
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      if ("admin".equals(r.get("ID")))
        continue;
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      projected.add(tm);
    }
    return projected;
  }

  /**
   * custom query. map config type (map, string) into common table column "value" in order to avoid
   * having a sparse result table
   */
  @ConfigQuery(query = "dj-config-values", table = "dj-config")
  public List<Map<String, Object>> queryConfigValue(QueryMeta qi, Map<String, Object> arguments)
      throws Exception {
    QueryMeta db = new QueryMeta();
    db.query = "dj-config";
    List<Map<String, Object>> res = this.query(db, null);
    List<Map<String, Object>> projected = new ArrayList<>();
    for (Map<String, Object> r : res) {
      Map<String, Object> tm = new LinkedHashMap<>();
      tm.put("ID", r.get("ID"));
      if (r.containsKey("type"))
        tm.put("value", r.get(r.get("type")));
      tm.put("description", r.get("description"));
      projected.add(tm);
    }
    return projected;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {

    String table = info.query;
    for (Method m : getClass().getMethods()) {
      ConfigQuery cq = m.getAnnotation(ConfigQuery.class);
      if (cq != null)
        if (cq.query().equals(info.query))
          table = cq.table();
    }
    Property prop = new Property();
    prop.parent = services.getDashjoinID() + "/config/" + table;
    prop.name = "ID";
    prop.ID = prop.parent + "/" + prop.name;
    prop.pkpos = 0;
    prop.type = "string";
    return ImmutableMap.of("ID", prop);
  }

  /**
   * make this block callable from Trigger Function
   */
  public void metadataCollection(String id) throws Exception {
    removeCache(id);
    metadataCollection(getDatabase(id));
  }

  @Override
  public void metadataCollection() {
    try {
      for (Database db : getDatabases()) {
        metadataCollection(db);
      }
    } catch (Exception e) {
      // thrown by getDatabase, something is very wrong
      throw new RuntimeException(e);
    }
  }

  void metadataCollection(Database db) {
    try {
      metadataCollectionEx(db);
    } catch (Exception e) {
      // log but ignore
      logger.warning("Ignoring metadata collection error: " + e);
      // Stacktrace only shown in fine log
      logger.log(Level.FINE, "Ignoring metadata collection error", e);
    }
  }

  @SuppressWarnings("unchecked")
  void metadataCollectionEx(Database db) throws Exception {
    // Ignore null DBs - only happens when i.e. DB class was removed/renamed
    if (db == null)
      return;

    AbstractDatabase a = (AbstractDatabase) db;

    Object cached = getCached(a.ID);
    if (cached != null)
      return;

    putCache(a.ID, a);
    Map<String, Object> database = Maps.newHashMap(of("status", "connecting...", "tables", of()));
    ProviderDatabase provider = new ProviderDatabase(a.ID, of("dj-database", of(a.ID, database)));
    this.addDB(provider);

    try {
      Map<String, Object> meta = a.connectAndCollectMetadata();

      for (String key : new HashSet<>(meta.keySet())) {
        Map<String, Object> table = (Map<String, Object>) meta.get(key);
        if (((String) table.get("ID")).split("/").length != 3) {
          meta.remove(key);
          logger.warning("ignoring unescaped table: " + key);
        } else {
          Map<String, Object> properties = (Map<String, Object>) table.get("properties");
          if (properties != null)
            for (String p : new HashSet<>(properties.keySet())) {
              Map<String, Object> property = (Map<String, Object>) properties.get(p);
              if (((String) property.get("ID")).split("/").length != 4) {
                properties.remove(p);
                logger.warning("ignoring unescaped property: " + p);
              }
            }
        }
      }

      if (meta != null)
        database.put("tables", meta);
      database.put("status", "OK");
    } catch (Exception e) {
      database.put("status", "Error: " + ExMapper.getMessage(e));
      throw e;
    }
  }

  @Override
  public AbstractDatabase getCached(String id) {
    return cache().get(id);
  }

  @Override
  public void putCache(String id, AbstractDatabase object) {
    cache().put(id, object);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends AbstractDatabase> T getCachedForce(String id, Class<T> cls) throws Exception {
    AbstractDatabase db = getCached(id);
    if (db == null)
      throw new Exception("Database not in cache: " + id);
    if (!cls.isAssignableFrom(db.getClass()))
      throw new Exception("Wrong database type cached: " + id);
    return (T) db;
  }

  @Override
  public Integer getAutocompleteTimeoutMs() {
    return getIntConfig("autocomplete-timeout-ms");
  }

  @Override
  public Integer getSearchTimeoutMs() {
    return getIntConfig("search-timeout-ms");
  }

  @Override
  public Integer getAllTimeoutMs() {
    return getIntConfig("all-timeout-ms");
  }

  Integer getIntConfig(String s) {
    Map<String, Object> res;
    try {
      res = read(Table.ofName("dj-config"), MapUtil.of("ID", s));
    } catch (Exception e) {
      return null;
    }
    Integer value = res == null ? null : (Integer) res.get("integer");
    if (value != null) {
      if (value <= 0)
        return null;
    }
    return null;
  }

  @Override
  public boolean excludeFromSearch(AbstractDatabase db) throws Exception {
    Map<String, Object> res =
        read(Table.ofName("dj-config"), MapUtil.of("ID", "exclude-database-from-search"));
    @SuppressWarnings("unchecked")
    List<String> value = res == null ? null : (List<String>) res.get("list");
    if (value == null)
      return false;
    return value.contains(db.name);
  }

  @Override
  public Collection<Table> searchTables(AbstractDatabase db) throws Exception {
    Map<String, Object> res =
        read(Table.ofName("dj-config"), MapUtil.of("ID", "exclude-table-from-search"));

    if (res == null)
      return db.tables.values();
    @SuppressWarnings("unchecked")
    List<String> excludeTable = (List<String>) res.get("list");
    if (excludeTable == null)
      return db.tables.values();

    List<Table> list = new ArrayList<>();
    for (Table t : db.tables.values())
      if (!excludeTable.contains(t.name))
        list.add(t);
    return list;
  }

  @Override
  public String databaseSearchQuery(AbstractDatabase db) throws Exception {
    Map<String, Object> res =
        read(Table.ofName("dj-config"), MapUtil.of("ID", "database-search-query"));
    @SuppressWarnings("unchecked")
    Map<String, String> value = res == null ? null : (Map<String, String>) res.get("map");
    if (value == null)
      return null;
    return value.get(db.name);
  }

  @Override
  public String password(String table, String id) throws Exception {
    return user().password(table, id);
  }
}
