package org.dashjoin.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.util.MapUtil;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Uses an expression to extract data which is then transformed and loaded
 */
@JsonSchema(required = {"expression"}, order = {"expression"})
public class ETL extends AbstractSource {

  // cannot use "title" since the widget uses the label to determine whether it is required
  // https://github.com/dashjoin/json-schema-form/issues/106
  @JsonSchema(widget = "custom", widgetType = "expression",
      description = "Expression computing the source data")
  public String expression;

  @Override
  public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {

    if (expression == null)
      throw new Exception("Please provide an expression");

    Object res = expressionService.resolve(sc, expression, null, this.readOnly);
    return convertToMapOfTables(res);
  }

  @SuppressWarnings("unchecked")
  public Map<String, List<Map<String, Object>>> convertToMapOfTables(Object res) {
    if (res == null)
      return Map.of();
    if (res instanceof List<?>)
      if (isTable((List<?>) res))
        return MapUtil.of("table", (List<Map<String, Object>>) res);
      else {
        List<Map<String, Object>> table = new ArrayList<>();
        for (Object item : (List<?>) res)
          table.add(MapUtil.of("column", item));
        return MapUtil.of("table", table);
      }
    else if (res instanceof Map<?, ?>)
      if (isMapOfTables((Map<?, ?>) res))
        return (Map<String, List<Map<String, Object>>>) res;
      else
        return MapUtil.of("table", new ArrayList<>(Arrays.asList((Map<String, Object>) res)));
    else
      return MapUtil.of("table", new ArrayList<>(Arrays.asList(MapUtil.of("column", res))));
  }

  /**
   * is value a table or just an array of values?
   */
  boolean isTable(List<?> value) {
    for (Object row : (List<?>) value)
      if (!(row instanceof Map<?, ?>))
        return false;
    return true;
  }

  /**
   * is value a map of tables or just an object?
   */
  boolean isMapOfTables(Map<?, ?> value) {
    for (Entry<?, ?> e : value.entrySet()) {
      if (!(e.getKey() instanceof String))
        return false;
      if (e.getValue() instanceof List<?>) {
        if (!isTable((List<?>) e.getValue()))
          return false;
      } else
        return false;
    }
    return true;
  }
}
