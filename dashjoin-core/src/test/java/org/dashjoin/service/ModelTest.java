package org.dashjoin.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IteratorUtils;
import org.dashjoin.model.QueryMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import com.api.jsonata4java.Expression;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
          .asList("button", "links", "dj-toolbar", "dj-table-metadata", "chart", "card",
              "expansion", "edit", "all", "create", "container", "grid", "display", "${pk1}",
              "text", "activity-status", "upload", "icon", "spacer", "layout-edit-switch", "search",
              "search-result", "toolbar", "table", "queryeditor", "editRelated", "markdown", "page",
              "tree", "variable", "sidenav-switch", "html", "notebook", "actionTable")
          .contains(tree.get("widget").asText()));
    }

    for (Entry<String, JsonNode> e : IteratorUtils.toList(tree.fields())) {
      // System.out.println(e);
      Assertions.assertTrue(Arrays.asList("ID", "widget", "text", "title", "pageLayout", "readOnly",
          "tooltip", "icon", "icons", "href", "children", "database", "table", "schema", "chart",
          "query", "arguments", "fxHide", "display", "prop", "createSchema", "columns", "roles",
          "if", "context", "properties", "deleteConfirmation", "print", "navigate", "markdown",
          "layout", "style", "class", /* schema info in config.json */ "name", "parent",
          /* gridster stuff */ "x", "y", "rows", "cols", /* redraw container */ "redrawInterval",
          "expression", "html", "script", "hideframe", "clearCache", "card").contains(e.getKey()));
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
            Assertions.assertTrue(expr.startsWith("{") || expr.startsWith("$")
                || expr.startsWith("value.") || expr.startsWith("("));
            Expression jsonata = Expression.jsonata(expr);
            try {
              jsonata.evaluate(null);
            } catch (Exception e) {
              String s = e.getMessage();
              if (!s.equals("Unknown function: $call"))
                if (!s.equals("Unknown function: $trigger"))
                  if (!s.equals("Unknown function: $djRoles"))
                    if (!s.equals("Unknown function: $djVersion"))
                      if (!s.equals("Unknown function: $djGetDatabases"))
                        if (!s.equals("Unknown function: $djGetDrivers"))
                          if (!s.equals("Unknown function: $djGetFunctions"))
                            if (!s.equals("Unknown function: $echo"))
                              if (!s.equals("Unknown function: $alterColumnTrigger"))
                                if (!s.equals("Unknown function: $alterTableTrigger"))
                                  if (!s.equals("Unknown function: $traverse"))
                                    if (!s.equals("Unknown function: $query"))
                                      if (!s.equals("Unknown function: $doc2data"))
                                        if (!s.equals("Unknown function: $djSubscription"))
                                          if (!s.equals("Unknown function: $saveApi"))
                                            if (!s.equals("Unknown function: $createStubs"))
                                              if (!s.equals("Unknown function: $createTable"))
                                                if (!s.equals("Unknown function: $read"))
                                                  if (!s.equals("Unknown function: $update"))
                                                    if (!s.equals("Unknown function: $erDiagram"))
                                                      if (!s.startsWith("Unknown function: $git"))
                                                        throw e;
            }
          }
          // JsonNode expr = kid.getValue().get("dj-expr");
          // Assertions.assertEquals(1, expr.size());
          // Entry<String, JsonNode> op = expr.fields().next();
          // Assertions.assertTrue(Arrays.asList("object", "trigger",
          // "call").contains(op.getKey()));
        }
    }
  }

  public static void testDriver(String file) throws Exception {

    ObjectMapper om =
        new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
            .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);

    JsonNode map = om.readTree(new FileInputStream(new File(file)));
    file = map.get("test").get("file").asText();
    JsonNode test = om.readTree(new FileInputStream(new File(file)));
    String expr =
        Expression.jsonata(map.get("test").get("expression").asText()).evaluate(test).asText();

    Expression e = Expression.jsonata(expr);
    Iterator<String> cases = map.get("cases").fieldNames();
    while (cases.hasNext()) {
      String name = cases.next();
      JsonNode c = map.get("cases").get(name);
      ObjectNode basedata = map.get("basedata").deepCopy();

      Iterator<String> dataFields = c.get("data").fieldNames();
      while (dataFields.hasNext()) {
        String field = dataFields.next();
        basedata.set(field, c.get("data").get(field));
      }

      Assertions.assertEquals("error in case: " + name, "" + c.get("expected"),
          "" + wrap(e.evaluate(basedata)));
    }
  }

  static JsonNode wrap(JsonNode n) {
    if (n instanceof ArrayNode)
      return n;
    else
      return om.createArrayNode().add(n);
  }
}
