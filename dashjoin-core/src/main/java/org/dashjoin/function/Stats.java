package org.dashjoin.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;

/**
 * Generate statistics for a database table (type, min, max, count, distinct values, etc.)
 */
@SuppressWarnings("rawtypes")
public class Stats extends AbstractVarArgFunction<Object> {

  @SuppressWarnings("unchecked")
  @Override
  public Object run(List arg) throws Exception {
    String database = (String) arg.get(0);
    String table = (String) arg.get(1);
    Integer limit = (Integer) arg.get(2);
    if (database == null || table == null)
      throw new IllegalArgumentException("Syntax: $stats(database, table, limit?)");

    List<Map<String, Object>> data = this.expressionService.getData().all(sc, database, table, null,
        limit == null ? 100000 : limit, null, false, null);

    AbstractDatabase db = this.services.getConfig().getDatabase("dj/" + database);
    Table t = db.tables.get(table);
    if (t == null)
      throw new IllegalArgumentException("table not found: " + table);

    List<Object> x = new ArrayList<>();

    for (Property col : t.properties.values()) {

      boolean hasInt = false;
      boolean hasBool = false;
      boolean hasNum = false;
      boolean hasString = false;
      Object min = null;
      Object max = null;
      Set<Object> distinct = new HashSet<>();
      for (Map<String, Object> row : data) {
        Object value = row.get(col.name);
        if (value != null)
          distinct.add(value);
        if (value instanceof Comparable) {
          if (max == null)
            max = value;
          else if (((Comparable) value).compareTo(max) > 0)
            max = value;
          if (min == null)
            min = value;
          else if (((Comparable) value).compareTo(min) < 0)
            min = value;
        }
        if ("string".equals(col.type) && value instanceof String) {
          try {
            Integer.parseInt((String) value);
            hasInt = true;
          } catch (NumberFormatException e) {
            try {
              Double.parseDouble((String) value);
              hasNum = true;
            } catch (NumberFormatException e2) {
              if ("true".equalsIgnoreCase((String) value)
                  || "false".equalsIgnoreCase((String) value)) {
                Boolean.parseBoolean((String) value);
                hasBool = true;
              } else {
                hasString = true;
              }
            }
          }
        }
      }

      int types = 0;
      if (hasString)
        types++;
      if (hasInt)
        types++;
      if (hasBool)
        types++;
      if (hasNum)
        types++;

      String detectedType = "string";
      if (types == 2 && hasInt && hasNum)
        // only mixed type that is not string
        detectedType = "number";
      else if (types == 1) {
        // column has no mixed type
        if (hasString)
          detectedType = "string";
        if (hasInt)
          detectedType = "integer";
        if (hasBool)
          detectedType = "boolean";
        if (hasNum)
          detectedType = "number";
      } else
        // no data or mixed type
        detectedType = "string";

      Map<String, Object> res = MapUtil.of();
      res.put("ID", col.ID);
      res.put("count", data.size());
      res.put("nulls",
          data.stream().filter(i -> i.get(col.name) == null).collect(Collectors.counting()));
      res.put("countdistinct", distinct.size());
      res.put("min", min);
      res.put("max", max);
      res.put("dbType", col.dbType);
      res.put("type", col.type);
      res.put("detectedType", detectedType);
      x.add(res);
    }
    return x;
  }

  @Override
  public String getID() {
    return "stats";
  }

  @Override
  public String getType() {
    return "read";
  }

  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, String.class, Integer.class);
  }
}
