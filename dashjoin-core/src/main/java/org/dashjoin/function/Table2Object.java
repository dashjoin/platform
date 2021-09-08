package org.dashjoin.function;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets a 2 column table as by object by using the first column as keys and the second column
 * as values
 */
public class Table2Object extends AbstractFunction<List<Map<String, Object>>, Map<String, Object>> {

  @Override
  public Map<String, Object> run(List<Map<String, Object>> arg) throws Exception {
    Map<String, Object> res = new LinkedHashMap<>();
    for (Map<String, Object> row : arg)
      if (row != null) {
        Iterator<Object> i = row.values().iterator();
        String key = "" + i.next();
        Object value = i.next();
        res.put(key, value);
      }
    return res;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<List<Map<String, Object>>> getArgumentClass() {
    Object res = List.class;
    return (Class<List<Map<String, Object>>>) res;
  }

  @Override
  public String getID() {
    return "table2object";
  }

  @Override
  public String getType() {
    return "read";
  }
}
