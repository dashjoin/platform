package org.dashjoin.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;

/**
 * The config database as code outside the config database API sees it in SQL mode, e.g. the
 * session and tenant managers that use the injected JSONDatabase: reads return the merged records
 * of all layers, writes go to the local layer. In file mode, the injected database is the model
 * folder, which holds all of these records as well.
 */
public class SqlConfigView extends JSONDatabase {

  final SqlConfigStore store;

  SqlConfigView(SqlConfigStore store) {
    this.store = store;
  }

  /** the merged records once the layers are attached, the local layer before */
  JSONSqlDatabase reads() {
    return store.base == null ? store.local() : store.effective();
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    return reads().read(s, search);
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return reads().queryMap(info, arguments);
  }

  @Override
  public void create(Table s, Map<String, Object> object) throws Exception {
    store.local().create(s, object);
  }

  /**
   * a record that only a lower layer has (e.g. @EVERYONE from the app) gets a local record with the
   * given fields, like an update through the config database API
   */
  @Override
  public boolean update(Table s, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    return locked(s, search.get("ID"), () -> {
      if (store.local().read(s, search) != null)
        return store.local().update(s, search, object);
      if (read(s, search) == null)
        return false;
      Map<String, Object> local = new LinkedHashMap<>();
      local.put("ID", search.get("ID"));
      for (Entry<String, Object> e : object.entrySet())
        if (e.getValue() != null && !e.getKey().equals("ID"))
          local.put(e.getKey(), e.getValue());
      store.local().create(s, local);
      return true;
    });
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    return store.local().delete(s, search);
  }

  @Override
  public <T> T locked(Table s, Object id, Callable<T> body) throws Exception {
    return store.local().locked(s, id, body);
  }

  @Override
  public String toString() {
    return "SqlConfigView";
  }
}
