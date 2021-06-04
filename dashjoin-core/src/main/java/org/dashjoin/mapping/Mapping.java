package org.dashjoin.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.util.MapUtil;
import com.api.jsonata4java.expressions.EvaluateException;
import com.api.jsonata4java.expressions.Expressions;

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
  public String childTable;

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
  public static Map<String, Object> apply(ExpressionService expressionService, Expressions filter,
      Expressions rowMapping, Map<String, Object> row) throws Exception {
    if (filter != null) {
      Object include = expressionService.resolve(filter, row);
      if (include instanceof Boolean)
        if (!((Boolean) include))
          return null;
    }
    if (rowMapping != null) {
      return (Map<String, Object>) expressionService.resolve(rowMapping, row);
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

        if (mapping.getValue().childTable != null) {
          Mapping lookup = null;
          for (Entry<String, Mapping> m : mappings.entrySet()) {
            if (m.getValue() == mapping.getValue())
              continue;
            String sourceTableName = m.getKey();
            if (m.getValue() != null && m.getValue().sourceTable != null)
              sourceTableName = m.getValue().sourceTable;
            if (sourceTableName.equals(mapping.getValue().sourceTable))
              if (m.getValue() == null || m.getValue().childTable == null)
                lookup = m.getValue();
          }
          if (lookup == null || lookup.pk == null)
            throw new EvaluateException("The source table must have a primary key defined");
          List<Map<String, Object>> tmp = new ArrayList<>();
          for (Map<String, Object> row : source) {
            Object list = row.get(mapping.getValue().childTable);
            if (list instanceof List<?>) {
              for (Object item : (List<?>) list) {
                Map<String, Object> i = MapUtil.of("parent_" + lookup.pk, row.get(lookup.pk));
                if (item instanceof Map<?, ?>)
                  i.putAll((Map<String, Object>) item);
                else
                  i.put("list", item);
                tmp.add(i);
              }
            }
          }
          if (tmp.isEmpty())
            throw new EvaluateException(
                "Column " + mapping.getValue().childTable + " does not contain arrays to extract");
          source = tmp;
        }

        List<Map<String, Object>> mapped = new ArrayList<>();
        Expressions filter = mapping.getValue().rowFilter == null ? null
            : expressionService.prepare(sc, mapping.getValue().rowFilter);
        Expressions rowMapping = mapping.getValue().rowMapping() == null ? null
            : expressionService.prepare(sc, mapping.getValue().rowMapping());

        String rownumber = null;
        if (mapping.getValue().rowMapping != null)
          for (Entry<String, String> i : mapping.getValue().rowMapping.entrySet())
            if ("$index".equals(i.getValue()))
              rownumber = i.getKey();
        int counter = 0;

        for (Map<String, Object> row : source) {
          Map<String, Object> mappedRow = apply(expressionService, filter, rowMapping, row);
          if (mappedRow != null) {
            if (rownumber != null)
              mappedRow.put(rownumber, counter++);
            mapped.add(mappedRow);
          }
        }
        res.put(mapping.getKey(), mapped);
      }
    }
    return res;
  }
}
