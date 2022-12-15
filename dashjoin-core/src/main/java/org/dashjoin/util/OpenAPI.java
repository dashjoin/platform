package org.dashjoin.util;

import static org.dashjoin.util.MapUtil.getMap;
import static org.dashjoin.util.MapUtil.of;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.service.ddl.SchemaChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * some utils for generating openapi fragments from dashjoin config metadata
 */
public class OpenAPI {

  private static ObjectMapper om = new ObjectMapper(new YAMLFactory());

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
        of("x-generated", true, "post",
            of("summary", function.comment, "operationId", function.ID, "requestBody", content(),
                "responses", of("200", content(function.ID + " response", null)))));
    clean(f);
    return f;
  }

  /**
   * generate path for query
   */
  public static Map<String, Object> path(QueryMeta query, Map<String, Property> resultMeta) {
    String database = query.database.split("/")[1];
    Map<String, Object> f = of("/rest/database/query/" + database + "/" + query.ID,
        of("x-generated", true, "post",
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
    Map<String, Object> val = of("x-generated", true, "type", "object", "properties", properties);
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

  /**
   * clean objects marked x-generated: true
   */
  public static void cleanGenerated(Map<String, Object> f) {
    for (String key : new ArrayList<>(f.keySet())) {
      if (f.get(key) instanceof Map) {
        cleanGenerated(getMap(f, key));
        Map<String, Object> kid = getMap(f, key);
        if (Boolean.TRUE.equals(kid.get("x-generated")))
          f.remove(key);
      }
    }
  }

  /**
   * get the openapi url
   */
  static URL url(Services services) throws Exception {
    Map<String, Object> config = services.getConfig().getConfigDatabase()
        .read(Table.ofName("dj-config"), of("ID", "openapi"));
    if (config.get("map") == null)
      return null;

    String u = (String) getMap(config, "map").get("url");
    if (u == null)
      return null;

    // make sure file: URLs are safe
    URL url = new URL(u);
    FileSystem.checkFileAccess(url);

    return url;
  }

  /**
   * read open api spec specified in the config db - returns null if none is configured
   */
  public static JsonNode open(Services services) throws Exception {
    URL url = url(services);

    // open URL, if file, take Home into account
    InputStream in = null;
    if ("file".equals(url.getProtocol())) {
      File file = Home.get().getFile(url.getPath());
      in = new FileInputStream(file);
    } else {
      in = url.openStream();
    }

    return om.readTree(in);
  }

  /**
   * write openapi spec
   * 
   * @param sc
   */
  public static void save(SecurityContext sc, Services services, String generate) throws Exception {
    URL url = url(services);
    if ("file".equals(url.getProtocol())) {
      if (!sc.isUserInRole("admin"))
        throw new Exception("must be admin to write file url");
      File file = Home.get().getFile(url.getPath());
      FileUtils.writeStringToFile(file, generate, Charset.defaultCharset());
    } else {
      throw new IllegalArgumentException("openapi save only supported for file urls");
    }
  }

  /**
   * matches an openapi path to a given URL
   * 
   * @return null if there is no match, a map with path parameters if there is a match
   */
  public static Map<String, Object> matchPath(String _path, String _url) {
    Map<String, Object> res = of();
    String[] p = _path.split("/");
    String[] u = _url.split("/");
    if (p.length != u.length)
      return null;
    for (int i = 0; i < p.length; i++) {
      String path = p[i];
      String url = u[i];
      if (path.equals(url))
        continue;
      String prefix = StringUtils.getCommonPrefix(path, url);
      path = path.substring(prefix.length());
      url = url.substring(prefix.length());
      String postfix = StringUtils.getCommonPrefix(new StringBuffer(path).reverse().toString(),
          new StringBuffer(url).reverse().toString());
      path = path.substring(0, path.length() - postfix.length());
      url = url.substring(0, url.length() - postfix.length());
      if (path.startsWith("{") && path.endsWith("}"))
        res.put(path.substring(1, path.length() - 1), url);
      else
        return null;
    }
    return res;
  }

  /**
   * creates a table in the db based on JSON schema spec
   * 
   * @param services main service
   * @param database name of the db to create table in
   * @param map json schema
   * 
   * @throws Exception something went wrong during DDL
   */
  public static void ddl(Services services, String database, JsonNode map, JsonNode root)
      throws Exception {
    if (map.size() == 1) {
      Entry<String, JsonNode> entry = map.fields().next();
      JsonNode properties = entry.getValue().get("properties");
      if (properties != null) {
        if (properties.size() > 0) {
          Iterator<Entry<String, JsonNode>> iter = properties.fields();
          Entry<String, JsonNode> pk = iter.next();
          JsonNode type = pk.getValue().get("type");
          if (type != null) {
            AbstractDatabase db =
                services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);
            try {
              SchemaChange ddl = db.getSchemaChange();
              ddl.createTable(entry.getKey(), pk.getKey(), type.asText());

              while (iter.hasNext()) {
                Entry<String, JsonNode> col = iter.next();

                // handle $ref
                JsonNode ref = col.getValue().get("$ref");
                if (ref != null) {
                  for (String part : ref.asText().split("/")) {
                    if (part.equals("#"))
                      continue;
                    if (root == null)
                      break;
                    root = root.get(part);
                  }
                  if (root == null)
                    continue;
                  col.setValue(root);
                }

                // ignore unknown types
                JsonNode coltype = col.getValue().get("type");
                if (coltype != null)
                  ddl.createColumn(entry.getKey(), col.getKey(), coltype.asText());
              }
            } finally {
              ((PojoDatabase) services.getConfig())
                  .metadataCollection(services.getDashjoinID() + "/" + database);
            }
            return;
          }
        }
      }
    }
    throw new IllegalArgumentException(
        "You must pass a single JSON schema with at least one property in YAML syntax");
  }
}
