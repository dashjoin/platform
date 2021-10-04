package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.NotImplementedException;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryColumn;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.dashjoin.util.MapUtil;

/**
 * database implementation which allows merging multiple json dbs. allows user edits to go in one
 * dedicated DB
 */
public class UnionDatabase extends AbstractDatabase {

  public UnionDatabase() {}

  @Inject
  public UnionDatabase(Services services) {
    this.services = services;
  }

  /**
   * user edits go here
   */
  JSONDatabase _user;

  /**
   * list of read only DBs contributing data
   */
  List<JSONDatabase> _dbs;

  PasswordDatabase user() {
    if (_user != null)
      return new PasswordDatabase(_user);
    return new PasswordDatabase(((PojoDatabase) services.getConfig())._user);
  }

  List<JSONDatabase> dbs() {
    if (_user != null)
      return _dbs;
    return ((PojoDatabase) services.getConfig())._dbs;
  }

  /**
   * get all DBs. make sure user DB is last
   */
  List<JSONDatabase> all() {
    List<JSONDatabase> all = new ArrayList<>(dbs());
    all.add(user());
    return all;
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = new LinkedHashMap<>();
    for (JSONDatabase d : all()) {
      for (Entry<String, Map<String, Object>> e : d.queryMap(info, arguments).entrySet()) {

        // String path =
        // "model/" + info.query + "/" + UrlEscapers.urlPathSegmentEscaper().escape("" + e.getKey())
        // + ".deleted";
        // File deleted = new File(path);
        // if (deleted.exists())
        // continue;

        Map<String, Object> old = res.get(e.getKey());
        if (old == null)
          res.put(e.getKey(), e.getValue());
        else
          res.put(e.getKey(), merge(old, e.getValue()));
      }
    }

    // if files are deleted on the FS, there might be stale providers which cause
    // zombie objects without ID, remove these
    List<Map<String, Object>> list = new ArrayList<>(res.values());
    Iterator<Map<String, Object>> iter = list.iterator();
    while (iter.hasNext()) {
      Map<String, Object> item = iter.next();
      if (item.get("ID") == null)
        iter.remove();
    }
    return list;
  }

  /**
   * merge json maps
   */
  public static Map<String, Object> merge(Map<String, Object> old, Map<String, Object> value) {
    return mergeInternal(old, value, false);
  }

  /**
   * merge json maps and arrays
   */
  public static Map<String, Object> mergeArray(Map<String, Object> old, Map<String, Object> value) {
    return mergeInternal(old, value, true);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static Map<String, Object> mergeInternal(Map<String, Object> old, Map<String, Object> value,
      boolean array) {
    if (value == null)
      return old;
    if (old == null)
      return value;

    Map<String, Object> res = new LinkedHashMap<>(old);
    for (Entry<String, Object> e : value.entrySet()) {
      if (e.getValue() == null)
        // cannot happen for top level fields (field=null removes the field)
        // however, when merging nested objects, this condition can occur:
        // old: {x: {a:1, field: value}}, user: {x: {field: null}} yields: {x: {a:1}}
        res.remove(e.getKey());
      else if (array && (e.getValue() instanceof List)) {
        if (res.containsKey(e.getKey()))
          ((List) res.get(e.getKey())).addAll((List) e.getValue());
        else
          res.put(e.getKey(), e.getValue());
      } else if (e.getValue() instanceof Map)
        res.put(e.getKey(), mergeInternal((Map) res.get(e.getKey()), (Map) e.getValue(), array));
      else
        res.put(e.getKey(), e.getValue());
    }
    return res;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    if (object.get("ID") == null)
      throw new Exception("Object must contain an ID");
    if (read(m, object) != null)
      throw new Exception("Object already exists. Please choose a different ID.");

    // delegate to user db
    user().create(m, object);
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {

    // merge all DBs (user DB is last)
    Map<String, Object> res = null;
    for (JSONDatabase d : all())
      res = merge(res, d.read(s, search));

    // if files are deleted on the FS, there might be stale providers which cause
    // zombie objects without ID, remove these
    if (res != null && res.get("ID") == null)
      return null;

    return res;
  }

  /**
   * read from read only DBs only
   */
  public Map<String, Object> readDBs(Table s, Map<String, Object> search) throws Exception {
    Map<String, Object> res = null;
    for (JSONDatabase d : dbs())
      res = merge(res, d.read(s, search));
    return res;
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    Map<String, Object> readOnly = readDBs(schema, search);
    Map<String, Object> user = user().read(schema, search);

    for (Entry<String, Object> e : object.entrySet()) {
      if (e.getKey().equals("ID"))
        continue;
      if (e.getValue() == null) {
        // remove key
        user().update(schema, search, MapUtil.of(e.getKey(), e.getValue()));
      } else {
        // add / change key
        if (user != null && user.containsKey(e.getKey())) {
          // already exists in user DB
          if (!Objects.equals(e.getValue(), readOnly == null ? null : readOnly.get(e.getKey()))) {
            // different from read only (update)
            user().update(schema, search, MapUtil.of(e.getKey(), e.getValue()));
          } else {
            // value is already present in read only, delete from user
            user().update(schema, search, MapUtil.of(e.getKey(), null));
          }
        } else {
          if (readOnly != null && readOnly.containsKey(e.getKey())) {
            // already exists in read only part
            if (!Objects.equals(e.getValue(), readOnly.get(e.getKey()))) {
              // different from read only (create & update)
              if (user().read(schema, search) == null)
                user().create(schema, search);
              user().update(schema, search, MapUtil.of(e.getKey(), e.getValue()));
            } else {
              // noop since value is already present in read only
            }
          } else {
            // not present at all (create & update)
            if (user().read(schema, search) == null)
              user().create(schema, search);
            user().update(schema, search, MapUtil.of(e.getKey(), e.getValue()));
          }
        }
      }
    }

    // make sure to delete zombie user object
    Map<String, Object> onlyId = user().read(schema, search);
    if (onlyId != null && onlyId.size() == 1) {
      user().delete(schema, search);
    }

    return readOnly != null || user != null;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    Map<String, Object> old = read(s, search);
    if (old == null)
      return false;

    // the old value might be in the read only part
    // delete it from the user DB but ignore the result
    user().delete(s, search);

    return true;
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    return user().getTablesInQuery(query);
  }

  @Override
  public void close() throws Exception {
    user().close();
  }

  /**
   * editor supports noop query and get initial query only
   */
  @Override
  public QueryEditorInternal getQueryEditor() {
    return new QueryEditorInternal() {

      @Override
      public QueryResponse rename(RenameRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse distinct(DistinctRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse sort(SortRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse addColumn(AddColumnRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse setWhere(SetWhereRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception {
        throw new NotImplementedException("not implemented");
      }

      @Override
      public QueryResponse noop(QueryDatabase query) throws Exception {
        AbstractDatabase config = services.getConfig().getDatabase("dj/config");

        QueryResponse res = new QueryResponse();
        res.database = query.database;
        res.query = query.query;

        QueryMeta info = new QueryMeta();
        info.query = query.query;
        res.data = query(info, null);

        Set<String> keys = new HashSet<>();
        for (Map<String, Object> row : res.data)
          keys.addAll(row.keySet());

        res.joinOptions = Arrays.asList();
        res.fieldNames = new ArrayList<>(keys);
        if (res.fieldNames.remove("ID"))
          res.fieldNames.add(0, "ID");

        res.metadata = new ArrayList<>();
        for (String field : res.fieldNames) {
          QueryColumn qc = new QueryColumn();
          qc.database = UnionDatabase.this.ID;
          qc.col = Col.col(query.query, field);

          Table schema = config.tables.get(query.query);
          if (schema != null) {
            Property property = schema.properties.get(field);
            if (property != null) {
              qc.columnID = property.ID;
              if (property.pkpos != null)
                qc.keyTable = query.query;
              if (property.ref != null)
                qc.keyTable = property.ref.split("/")[2];
            }
          }

          res.metadata.add(qc);
        }
        return res;
      }

      @Override
      public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
        QueryDatabase q = new QueryDatabase();
        Table s = services.getConfig().getSchema(ac.table);
        q.query = s.name;
        q.database = s.parent;
        return noop(q);
      }
    };
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    QueryMeta info = new QueryMeta();
    info.query = s.name;
    List<Map<String, Object>> res = new ArrayList<>();
    for (Map<String, Object> i : query(info, arguments)) {
      boolean allTrue = true;
      if (arguments != null)
        for (Entry<String, Object> e : arguments.entrySet()) {
          String key = e.getKey();
          Object o1 = i.get(key);
          Object o2 = e.getValue();
          if (o1 == null) {
            if (o2 != null)
              allTrue = false;
          } else if (!(o1.equals(o2)))
            allTrue = false;
        }
      if (allTrue)
        res.add(i);
    }

    if (sort != null)
      Collections.sort(res, new Comparator<>() {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          Object e1 = o1 == null ? null : o1.get(sort);
          Object e2 = o2 == null ? null : o2.get(sort);
          if (e1 == null && e2 == null)
            return 0;
          if (!(e1 instanceof Comparable<?>))
            return descending ? -1 : 1;
          if (!(e2 instanceof Comparable<?>))
            return !descending ? -1 : 1;
          return !descending ? ((Comparable<Object>) e1).compareTo(e2)
              : ((Comparable<Object>) e2).compareTo(e1);
        }
      });

    if (offset != null)
      res = res.subList(offset, res.size());
    if (limit != null)
      res = res.subList(0, Math.min(limit, res.size()));

    return res;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    throw new NotImplementedException("not implemented");
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    // no operation required
    return Collections.emptyMap();
  }
}
