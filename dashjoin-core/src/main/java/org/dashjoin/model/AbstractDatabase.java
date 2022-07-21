package org.dashjoin.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.ACLContainerRequestFilter;
import org.dashjoin.service.CredentialManager;
import org.dashjoin.service.Data.Choice;
import org.dashjoin.service.Data.Origin;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Data.SearchResult;
import org.dashjoin.service.Database;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.util.Template;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * abstract base class for all database implementations
 */
@JsonSchema(required = {"name"}, layout = "vertical",
    order = {"djClassName", "name", "comment", "title", "readRoles", "writeRoles"},
    computed = "{ \"ID\": \"\\\"dj/\\\" & name\" }")
public abstract class AbstractDatabase implements Database {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * pk
   */
  public String ID;

  /**
   * db name
   */
  @JsonSchema(createOnly = true)
  public String name;

  public String comment;

  public String title;

  /**
   * parent pk
   */
  public String parent;

  /**
   * tables of this db
   */
  public Map<String, Table> tables = new HashMap<>();

  /**
   * implementation class
   */
  @JsonSchema(title = "Database Type", widget = "select", choicesVerb = "GET",
      choicesUrl = "/rest/manage/getDatabases", jsonata = "name", displayWith = "localName")
  public String djClassName;

  /**
   * DB connection status
   */
  public String status;

  /**
   * roles that are allowed to read the database
   */
  @JsonSchema(layout = "select", choicesUrl = "/rest/database/query/config/dj-roles-without-admin",
      jsonata = "ID")
  public List<String> readRoles;

  /**
   * roles that are allowed to write the database
   */
  @JsonSchema(layout = "select", choicesUrl = "/rest/database/query/config/dj-roles-without-admin",
      jsonata = "ID")
  public List<String> writeRoles;

  /**
   * reference to global injectable, set after jackson unmarshalling
   */
  @JsonIgnore
  protected Services services;

  public void init(Services services) {
    Preconditions.checkNotNull(services);
    this.services = services;
  }

  @JsonIgnore
  public String displayUrl() {
    return getClass().getSimpleName();
  }

  @Override
  public List<SearchResult> search(SecurityContext sc, String search, Integer limit)
      throws Exception {
    return search(sc, null, search, limit);
  }

  @Override
  public List<SearchResult> search(SecurityContext sc, Table filter, String search, Integer limit)
      throws Exception {
    // TODO: only brute force search for now
    List<SearchResult> ret = new ArrayList<>();
    for (Table t : services.getConfig().searchTables(this)) {

      if (filter != null && !filter.name.equals(t.name))
        continue;

      try {
        ACLContainerRequestFilter.check(sc, this, t);
      } catch (NotAuthorizedException ex) {
        continue;
      }

      // do not search performance traces
      if ("dj-query-performance".equals(t.name))
        continue;

      for (Map<String, Object> res : all(t, null, null, null, false, null)) {
        for (Entry<String, Object> e : res.entrySet()) {
          if (e.getValue() != null
              && e.getValue().toString().toLowerCase().contains(search.toLowerCase())) {
            String[] key = new String[] {null, null, null, null};
            for (Property p : t.properties.values())
              if (p.pkpos != null)
                key[p.pkpos] = "" + res.get(p.name);

            ret.add(SearchResult.of(Resource.of(this, t, res), e.getKey(), e.getValue()));
            break;
          }
        }
        if (limit != null && ret.size() >= limit)
          return ret;
      }
    }
    return ret;
  }

  @Override
  public List<Choice> keys(Table s, String prefix, Integer limit) throws Exception {
    List<Choice> ret = new ArrayList<>();

    // special case for config/Table
    Property id = new Property();
    id.name = "ID";
    id.pkpos = 0;
    Map<String, Property> props = s.properties == null ? ImmutableMap.of("ID", id) : s.properties;

    for (Property p : props.values())
      if (p.pkpos != null)
        for (Map<String, Object> res : all(s, null, null, null, false, null))
          if (res.get(p.name) != null) {
            String match = s.djLabel == null ? res.get(p.name).toString()
                : "" + Template.replace(s.djLabel, res);
            if (match.toLowerCase().contains(prefix.toLowerCase())) {
              if (s.djLabel == null) {
                Choice choice = new Choice();
                choice.value = res.get(p.name);
                choice.name = match;
                ret.add(choice);
              } else {
                Choice choice = new Choice();
                choice.value = res.get(p.name);
                choice.name = match;
                ret.add(choice);
              }
              if (limit != null && ret.size() == limit)
                break;
            }
          }
    return ret;
  }

  /**
   * in the system we have two kinds of datatypes:
   * 
   * 1) the JSON / UI / form types (also see https://dashjoin.github.io/): these consist of string,
   * boolean, number, integer (added from JSON schema), array, object. This type is reflected in the
   * field Property.type
   * 
   * 2) the native database type like VARCHAR for SQL. This type is reflected in the field
   * Property.dbType
   * 
   * The conversion from database to JSON is implicitly built into the API by using Map(String,
   * Object) in the signatures. This requires implementations to convert the data accordingly.
   * 
   * The conversion from JSON to database must be performed by the driver in this method. It is
   * called from all CRUD methods in order to cast all search, create or update map fields.
   * 
   * @param m metadata of the table to perform the crud operation on
   * @param object pk search, create or update map
   */
  public void cast(Table m, Map<String, Object> object) {
    if (m == null)
      throw new IllegalArgumentException("Unknown table");
    if (m.properties != null && object != null)
      for (Entry<String, Property> p : m.properties.entrySet()) {
        Object obj = object.get(p.getKey());
        Object cast = cast(p.getValue(), obj);
        if (obj != cast)
          object.put(p.getKey(), cast);
      }
  }

  /**
   * single type cast method called from cast(map).
   * 
   * @param p metadata of the property to cast to
   * @param object JSON value
   * @return database value
   */
  public Object cast(Property p, Object object) {

    if (object instanceof String) {
      String s = (String) object;

      if ("integer".equals(p.type)) {
        if (s.endsWith(".0"))
          s = s.substring(0, s.length() - 2);
        try {
          return Integer.parseInt(s);
        } catch (Exception e) {
          return Long.parseLong(s);
        }
      }

      if ("number".equals(p.type)) {
        try {
          return Integer.parseInt(s);
        } catch (Exception e) {
          try {
            return Long.parseLong(s);
          } catch (Exception e2) {
            try {
              return Double.parseDouble(s);
            } catch (Exception e3) {
              throw new NumberFormatException("Expecting a numerical value: " + s);
            }
          }
        }
      }

      if ("boolean".equals(p.type))
        return "true".equals(s);

      if ("array".equals(p.type) || "object".equals(p.type)) {
        try {
          Object tmp = objectMapper.readValue(s, Object.class);
          // make sure tmp really is an array or object
          if (!(tmp instanceof List<?> || tmp instanceof Map<?, ?>))
            throw new Exception();
          return tmp;
        } catch (Exception fallback) {
          if ("array".equals(p.type)) {
            List<Object> res = new ArrayList<>();
            for (String part : s.split(","))
              res.add(cast(p.items, part));
            return res;
          }
        }
      }
    }

    return object;
  }

  /**
   * default implementation for bulk inserts
   */
  public void create(Table m, List<Map<String, Object>> objects) throws Exception {
    for (Map<String, Object> object : objects)
      create(m, object);
  }

  /**
   * default implementation for bulk merge
   */
  public void merge(Table m, List<Map<String, Object>> objects) throws Exception {
    for (Map<String, Object> object : objects)
      merge(m, object);
  }

  /**
   * default implementation for bulk deletes
   */
  public void delete(Table s, List<Map<String, Object>> searches) throws Exception {
    for (Map<String, Object> search : searches)
      delete(s, search);
  }

  /**
   * default batch size
   */
  public int getBatchSize() {
    return 500;
  }

  /**
   * default implementation for delete all table entries
   */
  public void delete(Table m) throws Exception {
    for (;;) {
      List<Map<String, Object>> batch =
          deleteTableDb().all(m, 0, getBatchSize(), null, false, null);
      if (batch.isEmpty())
        return;
      delete(m, batch);
    }
  }

  protected Database deleteTableDb() {
    return this;
  }

  /**
   * merge / upsert (create or update). Tries to first create the object. If that fails, tries to
   * find the record identified by the keys contained in the object and updates the fields
   * accordingly. If an update is performed, the field _dj_source is removed. This field may
   * indicate the ID of the source that created this record. In case of an update, this field should
   * not be changed
   * 
   * @param t table to merge into
   * @param object the record to merge
   * @throws Exception in case both create and update fail
   */
  public void merge(Table t, Map<String, Object> object) throws Exception {
    try {
      create(t, object);
    } catch (Exception assumePkViolation) {
      // clone the map so changes to not affect the caller
      object = new LinkedHashMap<>(object);

      object.remove("_dj_source");

      // move PKs to row to search
      Map<String, Object> search = new HashMap<>();
      for (Property p : t.properties.values())
        if (p.pkpos != null)
          search.put(p.name, object.remove(p.name));

      // make sure there is something to update
      if (!object.isEmpty())
        if (!update(t, search, object))
          // update did not happen, so the assumption of a PK violation was false
          throw new Exception("merge failed: " + assumePkViolation);
    }
  }

  /**
   * create a batch operation
   */
  public CreateBatch openCreateBatch(Table table) {
    return new CreateBatch(this, table);
  }

  public DeleteBatch openDeleteBatch(Table table) {
    return new DeleteBatch(this, table);
  }

  public MergeBatch openMergeBatch(Table table) {
    return new MergeBatch(this, table);
  }

  /**
   * batch object for creation
   */
  public static class CreateBatch {
    AbstractDatabase db;
    Table m;
    List<Map<String, Object>> objects = new ArrayList<>();

    CreateBatch(AbstractDatabase db, Table m) {
      this.db = db;
      this.m = m;
    }

    public void create(Map<String, Object> object) throws Exception {
      add(object);
    }

    public void complete() throws Exception {
      if (objects.size() > 0)
        go();
    }

    void add(Map<String, Object> object) throws Exception {
      objects.add(object);
      if (objects.size() == db.getBatchSize()) {
        go();
        objects.clear();
      }
    }

    void go() throws Exception {
      db.create(m, objects);
    }
  }

  /**
   * batch object for deletion
   */
  public static class DeleteBatch extends CreateBatch {
    DeleteBatch(AbstractDatabase db, Table m) {
      super(db, m);
    }

    public void delete(Map<String, Object> object) throws Exception {
      add(object);
    }

    @Override
    void go() throws Exception {
      db.delete(m, objects);
    }
  }

  /**
   * batch object for merge
   */
  public static class MergeBatch extends CreateBatch {
    MergeBatch(AbstractDatabase db, Table m) {
      super(db, m);
    }

    public void merge(Map<String, Object> object) throws Exception {
      add(object);
    }

    @Override
    void go() throws Exception {
      db.merge(m, objects);
    }
  }

  public String password() throws Exception {
    PojoDatabase db = (PojoDatabase) this.services.getConfig();
    String password = db.password("dj-database", ID);
    try (CredentialManager.Credential c = new CredentialManager.Credential(password)) {
      return password != null ? new String(c.getSecret()) : null;
    }
  }

  public List<Origin> incoming(@Context SecurityContext sc, String database, String table,
      String objectId, Integer offset, Integer limit, long start, Integer timeout, String pk)
      throws Exception {
    List<Origin> res = new ArrayList<>();
    AbstractDatabase d = this;
    for (Table s : d.tables.values()) {

      if (s.name == null)
        continue;

      try {
        ACLContainerRequestFilter.check(sc, d, s);
        if (s.properties != null)
          for (Property p : s.properties.values())
            if (pk.equals(p.items == null ? p.ref : p.items.ref)) {
              Map<String, Object> search = new HashMap<>();
              search.put(p.name, objectId);
              d.cast(s, search);
              for (Map<String, Object> match : d.all(s, offset, limit, null, false, search)) {
                Origin o = new Origin();
                o.id = Resource.of(d, s, match);
                o.fk = p.ID;
                o.pk = pk;
                res.add(o);
              }
              if (timeout != null)
                if (System.currentTimeMillis() - start > timeout)
                  return res;
            }
      } catch (NotAuthorizedException ignore) {
      }
    }
    return res;
  }
}
