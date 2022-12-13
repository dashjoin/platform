package org.dashjoin.util;

import static org.dashjoin.util.MapUtil.getMap;
import static org.dashjoin.util.MapUtil.of;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;

/**
 * some utils for generating openapi fragments from dashjoin config metadata
 */
public class OpenAPI {

  /**
   * generate content - application/json - schema - type - object
   */
  public static Map<String, Object> content() {
    return content(null, "object");
  }

  /**
   * generate content - application/json - schema - type - object
   */
  public static Map<String, Object> content(String description, String type) {
    Map<String, Object> t;
    if ("object".equals(type))
      t = of("type", "object");
    else if ("array".equals(type))
      t = of("type", "array", "items", of("type", "object"));
    else
      t = of();
    Map<String, Object> content = of("content", of("application/json", of("schema", t)));
    if (description != null)
      content.put("description", description);
    return content;
  }

  /**
   * generate path entry from function
   */
  public static Map<String, Object> path(AbstractConfigurableFunction<?, ?> function) {
    Map<String, Object> f = of("/rest/function/" + function.ID,
        of("post", of("summary", function.comment, "operationId", function.ID, "requestBody",
            content(), "responses", of("200", content(function.ID + " response", null)))));
    clean(f);
    return f;
  }

  /**
   * generate path for query
   */
  public static Map<String, Object> path(QueryMeta query, Map<String, Property> resultMeta) {
    String database = query.database.split("/")[1];
    Map<String, Object> f = of("/rest/database/query/" + database + "/" + query.ID,
        of("post",
            of("summary", query.comment, "operationId", query.ID, "requestBody",
                parameters(query.arguments), "responses",
                of("200", resultMeta(resultMeta, query.ID + " response")))));
    clean(f);
    return f;
  }

  /**
   * generate content with schema from result metadata
   */
  public static Map<String, Object> resultMeta(Map<String, Property> resultMeta,
      String description) {
    Map<String, Object> res = content(description, "array");
    if (resultMeta != null) {
      Map<String, Object> x =
          getMap(getMap(getMap(getMap(res, "content"), "application/json"), "schema"), "items");
      Map<String, Object> properties = of();
      for (Property p : resultMeta.values())
        properties.put(p.name, property(p));
      x.put("properties", properties);
    }
    return res;
  }

  /**
   * generate content with schema from query parameters
   */
  public static Map<String, Object> parameters(Map<String, Object> arguments) {
    Map<String, Object> res = content();
    if (arguments != null) {
      Map<String, Object> x = getMap(getMap(getMap(res, "content"), "application/json"), "schema");
      Map<String, Object> properties = of();
      for (String key : arguments.keySet())
        properties.put(key, of("type", getMap(arguments, key).get("type"), "example",
            getMap(arguments, key).get("sample")));
      x.put("properties", properties);
    }
    return res;
  }

  /**
   * generate entry for properties
   */
  public static Map<String, Object> property(Property p) {
    Map<String, Object> val = of("type", p.type, "x-dbType", p.dbType);
    if (p.pkpos != null)
      val.put("x-pkPos", p.pkpos);
    if (p.ref != null)
      val.put("x-ref", p.ref);
    if (p.readOnly != null)
      val.put("readOnly", true);
    return val;
  }

  /**
   * generate schema for table
   */
  public static Map<String, Object> table(Table t) {
    Map<String, Object> properties = of();
    Map<String, Object> val = of("type", "object", "properties", properties);
    Map<String, Object> table = of(t.name, val);
    List<String> required = new ArrayList<>();
    for (Property p : t.properties.values()) {
      if (p.pkpos != null)
        required.add(p.name);
      properties.put(p.name, property(p));
    }
    if (!required.isEmpty())
      val.put("required", required);
    return table;
  }

  /**
   * clean null entries
   */
  static void clean(Map<String, Object> f) {
    for (String key : new ArrayList<>(f.keySet())) {
      if (f.get(key) instanceof Map)
        clean(getMap(f, key));
      if (f.get(key) == null)
        f.remove(key);
    }
  }
}
