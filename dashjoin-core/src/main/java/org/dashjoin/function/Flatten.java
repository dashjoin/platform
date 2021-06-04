package org.dashjoin.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Flatten
    extends AbstractFunction<List<Map<String, Object>>, List<Map<String, Object>>> {

  @Override
  public List<Map<String, Object>> run(List<Map<String, Object>> table) throws Exception {
    // find array field that contains an object
    if (table != null)
      for (Map<String, Object> arg : table)
        for (Entry<String, Object> e : arg.entrySet())
          if (e.getValue() instanceof List<?>)
            for (Object li : (List<?>) e.getValue())
              if (li instanceof Map<?, ?>)
                return go(e.getKey(), table);
    return table;
  }

  List<Map<String, Object>> go(String string, List<Map<String, Object>> table) {
    List<Map<String, Object>> res = new ArrayList<>();
    for (Map<String, Object> row : table) {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> col = (List<Map<String, Object>>) row.get(string);
      if (col != null)
        for (Map<String, Object> item : col) {
          res.add(item);
          for (Entry<String, Object> i : row.entrySet())
            if (!string.equals(i.getKey()))
              if (!item.containsKey(i.getKey()))
                item.put(i.getKey(), i.getValue());
        }
    }
    return res;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public Class<List<Map<String, Object>>> getArgumentClass() {
    Object res = Class.class;
    return (Class<List<Map<String, Object>>>) res;
  }

  @Override
  public String getID() {
    return "flatten";
  }

  @Override
  public String getType() {
    return "read";
  }
}
