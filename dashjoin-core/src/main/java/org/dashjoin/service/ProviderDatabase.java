package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;

/**
 * part of the config DB that allows computing some data and providing it to the union DB from RAM
 */
public class ProviderDatabase extends JSONDatabase {

  public ProviderDatabase(String ID, Map<String, Map<String, Map<String, Object>>> data) {
    this.ID = ID;
    this.data = data;
  }

  /**
   * provider ID - required to be able to delete it upon a change on the DB connection data / DB
   * deletion
   */
  String ID;

  /**
   * the computed payload (e.g. database metadata). The three nested maps represent:
   * 
   * 1) tablename to table data
   * 
   * 2) instanceid to instance data
   * 
   * 3) fieldname to field value
   */
  Map<String, Map<String, Map<String, Object>>> data;

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {

    String[] parts = info.query.split("/");
    Map<String, Map<String, Object>> res = dataget(parts[0]);
    if (res == null)
      return of();
    if (parts.length == 1) {
      return res;
    } else {
      Map<String, Object> res2 = res.get(parts[1]);
      if (res2 == null)
        return of();
      return of(parts[1], res2);
    }
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    Map<String, Map<String, Object>> table = dataget(s.name);
    if (table == null)
      return null;
    return table.get(search.get("ID"));
  }

  @SuppressWarnings("unchecked")
  Map<String, Map<String, Object>> dataget(String key) {
    return (Map<String, Map<String, Object>>) clone(data.get(key));
  }

  @SuppressWarnings("unchecked")
  Object clone(Object object) {
    if (object instanceof Map<?, ?>) {
      Map<String, Object> res = new LinkedHashMap<>();
      for (Entry<String, ?> e : ((Map<String, Object>) object).entrySet())
        if (e.getValue() instanceof Map<?, ?>)
          res.put(e.getKey(), clone(e.getValue()));
        else
          res.put(e.getKey(), e.getValue());
      return res;
    } else
      return object;
  }
}
