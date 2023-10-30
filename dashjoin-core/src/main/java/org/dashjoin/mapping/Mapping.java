package org.dashjoin.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.expression.ExpressionService.ParsedJsonata;
import org.dashjoin.function.Index;
import org.dashjoin.util.MapUtil;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.SecurityContext;

/**
 * represents a table mapping which is part of the JSON structure for provider functions
 */
public class Mapping {

  /**
   * apply the mapping to this source table
   */
  public String sourceTable;

  /**
   * column containing the array
   */
  public String extractColumn;

  /**
   * column containing the key to include in the extract array
   */
  public String extractKey;

  /**
   * the name of the PK column (we do not support composite keys here) only required if the create
   * option is true
   */
  public String pk;

  /**
   * jsonata expression which is applied to each row - must yield a record object
   */
  public Map<String, String> rowMapping;

  /**
   * jsonata expression which is applied to each row - must yield a boolean result (true means
   * include in mapping result)
   */
  public String rowFilter;

  /**
   * assemble jsonata expression
   */
  String rowMapping() {
    if (rowMapping != null) {
      String s = "{";
      List<String> fields = new ArrayList<>();
      for (Entry<String, String> e : rowMapping.entrySet())
        fields.add('"' + e.getKey() + '"' + ": " + e.getValue());
      s = s + String.join(", ", fields);
      s = s + "}";
      return s;
    }
    return null;
  }

  /**
   * apply the row mapping expression to a source row. return null if the result is not included
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> apply(ExpressionService expressionService, SecurityContext sc,
      Map<String, Object> row) throws Exception {
    if (rowFilter != null) {
      Object include = expressionService.resolve(sc, rowFilter, row);
      if (include instanceof Boolean)
        if (!((Boolean) include))
          return null;
    }
    if (rowMapping != null) {
      return (Map<String, Object>) expressionService.resolve(sc, rowMapping(), row);
    } else
      return row;
  }

  /**
   * apply the row mapping expression to a source row. return null if the result is not included
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> apply(ExpressionService expressionService, ParsedJsonata filter,
      ParsedJsonata rowMapping, Map<String, Object> row) throws Exception {
    if (filter != null) {
      Object include = filter.evaluate(row);
      if (include instanceof Boolean)
        if (!((Boolean) include))
          return null;
    }
    if (rowMapping != null) {
      return (Map<String, Object>) rowMapping.evaluate(row);
    } else
      return row;
  }

  /**
   * apply a set of mappings to a set of tables
   */
  @SuppressWarnings("unchecked")
  public static Map<String, List<Map<String, Object>>> apply(ExpressionService expressionService,
      SecurityContext sc, Map<String, List<Map<String, Object>>> sources,
      Map<String, Mapping> mappings) throws Exception {
    if (mappings == null)
      return sources;
    Map<String, List<Map<String, Object>>> res = new LinkedHashMap<>();
    for (Entry<String, Mapping> mapping : mappings.entrySet()) {
      if (mapping.getValue() == null) {
        res.put(mapping.getKey(), sources.get(mapping.getKey()));
      } else {
        List<Map<String, Object>> source =
            sources.get(mapping.getValue().sourceTable == null ? mapping.getKey()
                : mapping.getValue().sourceTable);

        if (source == null)
          continue;

        if (mapping.getValue().extractColumn != null) {
          List<Map<String, Object>> tmp = new ArrayList<>();
          for (Map<String, Object> row : source) {
            Object list = row.get(mapping.getValue().extractColumn);
            if (list instanceof List<?>) {
              for (Object item : (List<?>) list) {
                Map<String, Object> i = MapUtil.of("parent_" + mapping.getValue().extractKey,
                    row.get(mapping.getValue().extractKey));
                if (item instanceof Map<?, ?>)
                  i.putAll((Map<String, Object>) item);
                else
                  i.put("list", item);
                tmp.add(i);
              }
            }
          }
          if (tmp.isEmpty())
            throw new Exception("Column " + mapping.getValue().extractColumn
                + " does not contain arrays to extract");
          source = tmp;
        }

        List<Map<String, Object>> mapped = new ArrayList<>();
        ParsedJsonata filter = mapping.getValue().rowFilter == null ? null
            : expressionService.jsonata(sc, mapping.getValue().rowFilter);
        ParsedJsonata rowMapping = mapping.getValue().rowMapping() == null ? null
            : expressionService.jsonata(sc, mapping.getValue().rowMapping());

        long t0 = System.currentTimeMillis();
        Log.debug("Mapping started #records=" + source.size());

        int counter = 0;
        for (Map<String, Object> row : source) {
          long ix = Index.increment();
          Index.setReturnValue(ix);

          Map<String, Object> mappedRow = apply(expressionService, filter, rowMapping, row);
          if (mappedRow != null)
            mapped.add(mappedRow);
          if (counter++ % 1000 == 0) {
            long t = System.currentTimeMillis();
            if (t - t0 >= 1000) {
              Log.info("Mapping in progress #records=" + (counter - 1));
              t0 = t;
            }
          }
        }
        Log.debug("Mapping done #records=" + counter);
        res.put(mapping.getKey(), mapped);
      }
    }
    return res;
  }
}
