package org.dashjoin.service;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IteratorUtils;
import org.dashjoin.model.QueryMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * checks the structure of all JSON files in apps (query catalog, widgets, database definitions,
 * etc.)
 * 
 * Apps should have a test case that extends from this test case
 */
public class ModelTest {

  private static ObjectMapper om = new ObjectMapper();

  /**
   * check config resources on the classpath if they can be parsed and have the correct fields
   */
  @Test
  public void parseJson() throws Exception {
    for (String folder : new String[] {"Dashjoin", "dj-query-catalog", "widget", "page",
        "dj-database", "dj-function", "dj-config"})
      for (String s : new Reflections("model." + folder, new ResourcesScanner())
          .getResources(Pattern.compile(".*\\.json"))) {
        // System.out.println(getClass() + " " + s);
        JsonNode tree = om.readTree(getClass().getResourceAsStream("/" + s));
        if (folder.equals("dj-query-catalog"))
          om.readValue(getClass().getResourceAsStream("/" + s), QueryMeta.class);
        if (folder.equals("widget"))
          checkLayout(tree.get("layout"));
        if (folder.equals("page"))
          checkLayout(tree.get("layout"));
        if (folder.equals("dj-database"))
          checkDatabaseLayout(tree);
        checkExpressions(tree);
      }
  }

  /**
   * special integrity check for dj-database (ID = parent/name)
   */
  @Test
  public void djDatabase() throws Exception {
    for (String s : new Reflections("model.dj-database", new ResourcesScanner())
        .getResources(Pattern.compile(".*\\.json"))) {
      // System.out.println(getClass() + " " + s);
      if (!s.contains("northwind"))
        check(s);
    }
  }

  @SuppressWarnings("unchecked")
  void check(String s) throws Exception {
    Map<String, Object> map =
        om.readValue(getClass().getResourceAsStream("/" + s), JSONDatabase.tr);

    check(map, null, map);
    if (map.containsKey("tables"))
      for (Entry<String, Map<String, Object>> e : ((Map<String, Map<String, Object>>) map
          .get("tables")).entrySet()) {

        if (e.getKey().equals("ARR"))
          continue;
        if (e.getKey().equals("PRJ"))
          continue;
        if (e.getKey().equals("EMP"))
          continue;
        if (e.getKey().equals("http://ex.org/PRJ"))
          continue;
        if (e.getKey().equals("http://ex.org/EMP"))
          continue;

        Assertions.assertEquals(e.getKey(), e.getValue().get("name"));
        check(map, map.get("ID"), e.getValue());
        if (e.getValue().containsKey("properties"))
          for (Entry<String, Map<String, Object>> p : ((Map<String, Map<String, Object>>) e
              .getValue().get("properties")).entrySet()) {
            Assertions.assertEquals(p.getKey(), p.getValue().get("name"));
            check(map, e.getValue().get("ID"), p.getValue());
          }
      }
  }

  void check(Map<String, Object> root, Object parentId, Map<String, Object> map) {
    if (map == null)
      return;
    if (((String) map.get("ID")).split("/").length > 2)
      if (((String) map.get("ID")).contains("dj-database"))
        if (map.get("type") == null)
          Assertions.fail("no type: " + map);
    if (parentId != null)
      Assertions.assertEquals(parentId, map.get("parent"));
    Assertions.assertEquals(map.get("ID"), map.get("parent") + "/" + map.get("name"));
    Assertions.assertTrue(((String) map.get("ID")).startsWith("dj/"));
    if (map.get("ref") != null)
      Assertions.assertTrue(pk(root, (String) map.get("ref")));
  }

  @SuppressWarnings("unchecked")
  boolean pk(Map<String, Object> root, String ref) {
    if (ref.equals(root.get("ID"))) {
      Assertions.assertEquals(0, root.get("pkpos"));
      return true;
    }
    for (Object e : root.values())
      if (e instanceof Map) {
        if (pk((Map<String, Object>) e, ref))
          return true;
      }
    return false;
  }

  void checkDatabaseLayout(JsonNode tree) {
    for (String layout : new String[] {"instanceLayout", "tableLayout"})
      if (tree.get(layout) != null)
        checkLayout(tree.get(layout));
    for (JsonNode i : tree)
      checkDatabaseLayout(i);
  }

  void checkLayout(JsonNode tree) {
    if (tree.get("children") != null)
      for (JsonNode i : tree.get("children"))
        checkLayout(i);

    if (tree.get("widget") != null) {
      // System.out.println(tree.get("widget"));
      Assertions.assertTrue(Arrays
          .asList("aichat", "button", "links", "dj-toolbar", "dj-table-metadata", "chart", "card",
              "expansion", "edit", "all", "create", "container", "grid", "display", "${pk1}",
              "text", "activity-status", "upload", "icon", "spacer", "layout-edit-switch", "search",
              "search-result", "toolbar", "table", "queryeditor", "editRelated", "markdown", "page",
              "tree", "variable", "sidenav-switch", "html", "notebook", "actionTable")
          .contains(tree.get("widget").asText()));
    }

    for (Entry<String, JsonNode> e : IteratorUtils.toList(tree.fields())) {
      // System.out.println(e);
      Assertions.assertTrue(Arrays
          .asList("ID", "widget", "text", "title", "pageLayout", "readOnly", "tooltip", "icon",
              "icons", "href", "children", "database", "table", "schema", "chart", "query",
              "arguments", "fxHide", "display", "prop", "createSchema", "columns", "roles", "if",
              "context", "properties", "deleteConfirmation", "print", "navigate", "markdown",
              "layout", "style", "class", /* schema info in config.json */ "name", "parent",
              /* gridster stuff */ "x", "y", "rows", "cols",
              /* redraw container */ "redrawInterval", "expression", "html", "script", "hideframe",
              "clearCache", "card", "size", "cached", "onChat", "uploadEnabled")
          .contains(e.getKey()));
      if (e.getKey().equals("url"))
        Assertions.assertTrue(e.getValue() instanceof TextNode);
    }
  }

  void checkExpressions(JsonNode tree) throws Exception {
    Iterator<JsonNode> items = tree.iterator();
    while (items.hasNext())
      checkExpressions(items.next());
    Iterator<Entry<String, JsonNode>> i = tree.fields();
    while (i.hasNext()) {
      Entry<String, JsonNode> kid = i.next();
      if (kid.getKey().equals("object"))
        Assertions.assertTrue(kid.getValue() instanceof ArrayNode);
      checkExpressions(kid.getValue());
      for (String field : PojoDatabase.EXPRESSION_FIELDS)
        if (kid.getKey().equals(field)) {
          String expr = kid.getValue().asText();

          if (!(kid.getValue() instanceof ObjectNode)) {
            Jsonata jsonata = Jsonata.jsonata(expr);
            JFunction x = new JFunction(new JFunctionCallable() {
              @Override
              public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                  throws Throwable {
                return null;
              }
            }, null);
            jsonata.registerFunction("gitPull", x);
            jsonata.registerFunction("gitStatus", x);
            jsonata.registerFunction("djRoles", new JFunction(new JFunctionCallable() {
              @Override
              public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                  throws Throwable {
                return "admin";
              }
            }, null));
            jsonata.registerFunction("djVersion", x);
            jsonata.registerFunction("djGetDatabases", x);
            jsonata.registerFunction("djGetDrivers", x);
            jsonata.registerFunction("djGetFunctions", x);
            jsonata.registerFunction("read", x);
            jsonata.registerFunction("update", x);
            jsonata.registerFunction("openJson", x);
            jsonata.registerFunction("refresh", x);
            jsonata.registerFunction("createTable", x);
            jsonata.registerFunction("createStubs", x);
            jsonata.registerFunction("saveApi", x);
            jsonata.registerFunction("erDiagram", x);
            jsonata.registerFunction("echo", x);
            jsonata.registerFunction("alterColumnTrigger", x);
            jsonata.registerFunction("alterTableTrigger", x);
            jsonata.registerFunction("query", x);
            jsonata.registerFunction("call", x);
            jsonata.registerFunction("all", x);
            jsonata.registerFunction("djSubscription", x);
            jsonata.registerFunction("clearCache", x);
            try {
              jsonata.evaluate(null);
            } catch (Exception e) {
              throw e;
            }
          }
        }
    }
  }
}
