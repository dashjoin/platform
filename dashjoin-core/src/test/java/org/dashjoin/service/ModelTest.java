package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.dashjoin.function.lint.Finding;
import org.dashjoin.function.lint.Linter;
import org.dashjoin.function.lint.Rules;
import org.dashjoin.function.lint.Severity;
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
        "dj-database", "dj-function", "dj-config", "dj-library"})
      for (String s : new Reflections("model." + folder, new ResourcesScanner())
          .getResources(Pattern.compile(".*\\.json"))) {
        // System.out.println(getClass() + " " + s);
        JsonNode tree = om.readTree(getClass().getResourceAsStream("/" + s));
        if (folder.equals("dj-query-catalog"))
          om.readValue(getClass().getResourceAsStream("/" + s), QueryMeta.class);
        // layouts and forms: see lint()
        checkExpressions(tree);
      }
  }

  /**
   * lints layouts and forms against widgets.json (the rules in Rules.SHAPE): pages, widgets, table
   * / record layouts of databases and the knowledge base's default layouts (PojoDatabase.template)
   */
  @Test
  public void lint() throws Exception {
    Map<String, List<Map<String, Object>>> records = new LinkedHashMap<>();
    for (String folder : new String[] {"Dashjoin", "dj-query-catalog", "widget", "page",
        "dj-database", "dj-function", "dj-config", "dj-library"})
      for (String s : new Reflections("model." + folder, new ResourcesScanner())
          .getResources(Pattern.compile(".*\\.json")))
        records.computeIfAbsent(folder, k -> new ArrayList<>())
            .add(om.readValue(getClass().getResourceAsStream("/" + s), JSONDatabase.tr));

    Linter lint = new Linter();
    for (Entry<String, List<Map<String, Object>>> e : records.entrySet())
      for (Map<String, Object> record : e.getValue())
        lint.define(e.getKey(), record);
    for (Entry<String, List<Map<String, Object>>> e : records.entrySet())
      for (Map<String, Object> record : e.getValue())
        lint.record(e.getKey(), record);
    for (String s : new Reflections("template", new ResourcesScanner())
        .getResources(Pattern.compile(".*\\.json"))) {
      JsonNode template = om.readTree(getClass().getResourceAsStream("/" + s));
      lint.layout(s, "", template);
      lint.expressions(s, "", template, false);
    }

    // templates: no errors at all (e.g. an empty expression)
    List<Finding> found = lint.findings.stream().filter(f -> Rules.SHAPE.contains(f.rule())
        || f.file().startsWith("template/") && f.severity() == Severity.ERROR).toList();
    Assertions.assertEquals(List.of(), found, "" + found);
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
            // System.out.println(expr);
            JFunction x = new JFunction(new JFunctionCallable() {
              @Override
              public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                  throws Throwable {
                return null;
              }
            }, null);
            jsonata.registerFunction("use", new JFunction(new JFunctionCallable() {
              @Override
              public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                  throws Throwable {
                return "function(){1}";
              }
            }, null));
            jsonata.registerFunction("rest", x);
            jsonata.registerFunction("refresh", x);
            jsonata.registerFunction("prompt", x);
            jsonata.registerFunction("gitPull", x);
            jsonata.registerFunction("gitCheckout", x);
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
            jsonata.registerFunction("notify", x);
            jsonata.registerFunction("setIfAbsent", x);
            jsonata.registerFunction("djLint", x);
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
