package org.dashjoin.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.SafeServiceLoader;
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * function to lint the App taken mostly from org.dashjoin.service.ModelTest but modified since it
 * needs to run during runtime
 */
public class DjLint extends AbstractFunction<Map<String, Object>, String> {

  private static ObjectMapper om =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static List<String> CLIENT_FUNCTIONS =
      Arrays.asList("confirm", "setVariable", "prompt", "alert", "notify", "refresh", "reload",
          "log", "navigate", "clearCache", "log", "translate", "stopSpeech", "speak", "sleep",
          "dialog", "progress", "setActiveStep", "stepForward", "stepBack");

  private static Map<String, Object> deprecated;
  private static Map<String, Object> studio;
  private static Map<String, Object> _widgets;
  static {
    try {
      // to generate widgets.json, add
      // x[config.id] = config.controls?.schema?.properties ?
      // Object.keys(config.controls.schema.properties) : []
      // to function registerWidget and console.log(x) in function initWidgetRegistry
      _widgets =
          om.readValue(DjLint.class.getResourceAsStream("/lint/widgets.json"), JSONDatabase.tr);
      studio = om.readValue(DjLint.class.getResourceAsStream("/lint/studio.json"), JSONDatabase.tr);
      deprecated =
          om.readValue(DjLint.class.getResourceAsStream("/lint/deprecated.json"), JSONDatabase.tr);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  List<String> messages = new ArrayList<String>();
  String current;
  String currentID;
  Map<String, Object> widgets;

  @Override
  public String run(Map<String, Object> arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to lint the App");

    widgets = new HashMap<String, Object>(_widgets);
    if (arg != null)
      widgets.putAll(arg);

    for (String folder : new String[] {"Dashjoin", "dj-query-catalog", "widget", "page",
        "dj-database", "dj-function", "dj-config"}) {
      var all =
          expressionService.getData().all(sc, "config", folder, null, null, null, false, null);

      for (var item : all) {
        JsonNode tree = om.valueToTree(item);

        current = "model/" + folder + "/" + item.get("ID") + ".json";
        currentID = "" + item.get("ID");

        if (folder.equals("dj-query-catalog")) {
          checkDanglingPointers(tree);
          om.convertValue(tree, QueryMeta.class);
        }
        if (folder.equals("dj-function"))
          checkDanglingPointers(tree);
        if (folder.equals("widget"))
          checkLayout(tree.get("layout"));
        if (folder.equals("page"))
          checkLayout(tree.get("layout"));
        if (folder.equals("dj-database")) {
          checkDatabaseLayout(tree);
          check(item);
        }
        checkExpressions(tree);
      }
    }

    return String.join("\n", messages);
  }

  @SuppressWarnings("unchecked")
  void check(Map<String, Object> map) throws Exception {
    check(map, null, map);
    if (map.containsKey("tables"))
      for (Entry<String, Map<String, Object>> e : ((Map<String, Map<String, Object>>) map
          .get("tables")).entrySet()) {
        assertEquals(e.getKey(), e.getValue().get("name"), "name property must match key");
        check(map, map.get("ID"), e.getValue());
        if (e.getValue().containsKey("properties"))
          for (Entry<String, Map<String, Object>> p : ((Map<String, Map<String, Object>>) e
              .getValue().get("properties")).entrySet()) {
            assertEquals(p.getKey(), p.getValue().get("name"), "name property must match key");
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
          fail("no type: " + map);
    if (parentId != null && map.get("parent") != null)
      assertEquals(parentId, map.get("parent"), "parent property must match parent ID");
    if (map.get("parent") != null)
      assertEquals(map.get("ID"), map.get("parent") + "/" + map.get("name"),
          "ID must be parent + name");
    assertTrue(((String) map.get("ID")).startsWith("dj/"), "ID must start with dj/", map.get("ID"));
    if (map.get("ref") != null && ((String) map.get("ref")).startsWith((String) root.get("ID")))
      // check ref within the same DB
      assertTrue(pk(root, (String) map.get("ref")), "ref does not match", map.get("ref"));
  }

  @SuppressWarnings("unchecked")
  boolean pk(Map<String, Object> root, String ref) {
    if (ref.equals(root.get("ID"))) {
      assertEquals(0, root.get("pkpos"), ref + "must ref a primary key (pkpos=0)");
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

  void checkDanglingPointers(JsonNode tree) {
    for (JsonNode i : tree)
      checkDanglingPointers(i);
    for (Entry<String, JsonNode> e : tree.properties()) {
      String field = e.getKey();
      if (field.endsWith("-pointer")) {
        String org = field.substring(0, field.length() - "-pointer".length());
        if (tree.get(org) == null)
          fail("The field value for " + org + " cannot be found via the reference " + field
              + ". There must be a file called " + currentID + '.' + e.getValue().asText());
        continue;
      }
    }
  }

  @SuppressWarnings("unchecked")
  void checkLayout(JsonNode tree) {
    if (tree.get("children") != null)
      for (JsonNode i : tree.get("children"))
        checkLayout(i);

    List<String> props = Arrays.asList();

    if (tree.get("widget") == null) {
      fail("Missing widget: " + tree);
      return;
    }

    String widget = tree.get("widget").asText();
    if (!Arrays.asList("activity-status", "layout-edit-switch", "toolbar", "page", "${pk1}")
        .contains(widget)) {
      assertTrue(widgets.keySet().contains(widget), "unknown widget ", widget);
      props = (List<String>) widgets.get(widget);
      if (studio.get(widget) != null)
        props = Stream.concat(props.stream(), ((Collection<String>) studio.get(widget)).stream())
            .toList();
    }

    if (widget.equals("${pk1}"))
      return;

    for (Entry<String, JsonNode> e : tree.properties()) {
      String field = e.getKey();
      if (field.endsWith("-pointer")) {
        String org = field.substring(0, field.length() - "-pointer".length());
        if (tree.get(org) == null)
          fail("The field value for " + org + " cannot be found via the reference " + field
              + ". There must be a file called " + currentID + '.' + e.getValue().asText());
        continue;
      }
      if (field.equals("widget"))
        continue;
      if (field.equals("size"))
        continue;
      if (field.equals("roles"))
        continue;
      if (field.equals("card"))
        continue;
      if (field.equals("style"))
        continue;
      if (field.equals("if"))
        continue;
      if (field.equals("cached"))
        continue;
      if (field.equals("onRender") && widget.equals("page"))
        continue;
      if (field.equals("children")
          && Arrays.asList("container", "card", "toolbar", "expansion", "page").contains(widget))
        continue;
      if (field.equals("schema")
          && Arrays.asList("button", "edit", "create", "variable").contains(widget))
        continue;
      if (field.equals("foreach") && Arrays.asList("container", "tabs").contains(widget))
        continue;
      if (field.equals("fxHide") && current.equals("model/widget/dj-toolbar.json"))
        continue;
      if (isDeprecated(widget, field) != null)
        fail("property " + field + " is deprecated for widget " + widget + ":"
            + isDeprecated(widget, field));
      else if (props != null)
        assertTrue(props.contains(field), "unknown property for widget " + widget, field);
      if (field.equals("url"))
        assertTrue(e.getValue() instanceof TextNode, "url property must be a string", e);
    }
  }

  @SuppressWarnings("unchecked")
  String isDeprecated(String widget, String prop) {
    List<Map<String, String>> x = (List<Map<String, String>>) deprecated.get(widget);
    if (x != null)
      for (Map<String, String> i : x)
        if (i.get(prop) != null)
          return i.get(prop);
    return null;
  }

  void checkExpressions(JsonNode tree) {
    if (tree.get("widget") != null)
      if (tree.get("widget").asText().equals("actionTable"))
        if (tree.get("properties") != null)
          for (JsonNode p : tree.get("properties"))
            checkExpressions(om.createObjectNode().set("expression", p));
    Iterator<JsonNode> items = tree.iterator();
    while (items.hasNext())
      checkExpressions(items.next());
    for (Entry<String, JsonNode> kid : tree.properties()) {
      for (String field : PojoDatabase.EXPRESSION_FIELDS)
        if (kid.getKey().equals(field)) {
          String expr = kid.getValue().asText();

          if (expr.toLowerCase().startsWith("//javascript")
              || expr.toLowerCase().startsWith("// javascript"))
            continue;

          if (!(kid.getValue() instanceof ObjectNode)) {
            try {
              Jsonata jsonata = Jsonata.jsonata(expr);
              JFunction x = new JFunction(new JFunctionCallable() {
                @Override
                public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                    throws Throwable {
                  return null;
                }
              }, null);
              jsonata.registerFunction("error", x);
              for (String f : CLIENT_FUNCTIONS)
                jsonata.registerFunction(f, x);
              for (Function<?, ?> inst : SafeServiceLoader.load(Function.class))
                jsonata.registerFunction(inst.getID(), x);
              try {
                jsonata.evaluate(null);
              } catch (Exception e) {
                fail("Error running expression " + expr + ": " + e.getMessage());
              }
            } catch (JException je) {
              fail("Error parsing expression " + expr + ": " + je.getMessage());
            }
          }
        }
    }
  }

  void fail(String message) {
    messages.add(current + ": " + message);
  }

  void assertEquals(Object expected, Object actual, String message) {
    if (!objectsAreEqual(expected, actual)) {
      messages.add(current + ": " + message + ", " + actual + " != " + expected);
    }
  }

  void assertTrue(boolean condition, String message, Object value) {
    if (!condition)
      messages.add(current + ": " + message + ": " + value);
  }


  static boolean objectsAreEqual(Object obj1, Object obj2) {
    if (obj1 == null) {
      return (obj2 == null);
    }
    return obj1.equals(obj2);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<Map<String, Object>> getArgumentClass() {
    return (Class<Map<String, Object>>) (Class<?>) Map.class;
  }
}
