package org.dashjoin.function;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.service.ConfigQuery;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.SafeServiceLoader;
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.dashjoin.jsonata.Parser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
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
      Arrays.asList("confirm", "setVariable", "getVariable", "prompt", "alert", "notify", "refresh",
          "reload", "log", "navigate", "clearCache", "log", "translate", "stopSpeech", "speak",
          "sleep", "dialog", "progress", "setActiveStep", "stepForward", "stepBack", "sse");

  public static List<String> INPUTS = Arrays.asList("boolean", "string", "number", "auto complete",
      "select", "multi select", "key value", "password", "textarea", "date", "time", "datetime",
      "file", "binary file", "file with metadata", "binary file with metadata", "voice", "qrcode");

  public static List<String> OPTION_INPUTS =
      Arrays.asList("select", "multi select", "auto complete");

  public static List<String> FILE_INPUTS = Arrays.asList("file", "binary file",
      "file with metadata", "binary file with metadata");

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
  Map<String, String> queryTypes = new HashMap<String, String>();

  /**
   * IDs of catalog queries that declare arguments
   */
  Set<String> queriesWithArguments = new HashSet<String>();

  /**
   * role IDs defined in dj-role. null means unknown (e.g. in unit tests), in which case role checks
   * are skipped
   */
  Set<String> roles;

  /**
   * database names (IDs without the dj/ prefix) defined in dj-database plus config. null means
   * unknown (e.g. in unit tests), in which case database checks are skipped
   */
  Set<String> databases;

  /**
   * IDs of the query catalog, dj-function and dj-library, used to check $query, $call and $use.
   * null means unknown (e.g. in unit tests), in which case the respective check is skipped
   */
  Set<String> queries;
  Set<String> functions;
  Set<String> libraries;

  /**
   * IDs referenced from layouts and expressions, used to report unused items
   */
  Set<String> usedQueries = new HashSet<String>();
  Set<String> usedFunctions = new HashSet<String>();
  Set<String> usedLibraries = new HashSet<String>(Arrays.asList("unit-test"));

  /**
   * optional functions called from built-in pages the user cannot edit (e.g. DataCatalog). They are
   * not reported when missing
   */
  static final List<String> OPTIONAL_FUNCTIONS = Arrays.asList("dj-copilot");

  /**
   * built-in queries that do not need a query catalog entry: PojoDatabase methods marked with
   * ConfigQuery and dj-query-performance (see PojoDatabase.getQueryMeta and query)
   */
  static final Set<String> BUILT_IN_QUERIES = new HashSet<String>();
  static {
    BUILT_IN_QUERIES.add("dj-query-performance");
    for (Method m : PojoDatabase.class.getMethods()) {
      ConfigQuery cq = m.getAnnotation(ConfigQuery.class);
      if (cq != null)
        BUILT_IN_QUERIES.add(cq.query());
    }
  }

  /**
   * function types that are normally called via $call from the App
   */
  static final List<String> CALLED_FUNCTIONS = Arrays.asList("org.dashjoin.function.Invoke",
      "org.dashjoin.function.RestJson", "org.dashjoin.function.Email");

  /**
   * IDs of dj-functions of a CALLED_FUNCTIONS type that are not scheduled
   */
  Set<String> calledFunctions = new HashSet<String>();

  @Override
  public String run(Map<String, Object> arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to lint the App");

    widgets = new HashMap<String, Object>(_widgets);
    if (arg != null)
      widgets.putAll(arg);

    roles = new HashSet<String>();
    for (var role : expressionService.getData().all(sc, "config", "dj-role", null, null, null,
        false, null))
      roles.add("" + role.get("ID"));

    databases = new HashSet<String>(Arrays.asList("config"));
    for (var db : expressionService.getData().all(sc, "config", "dj-database", null, null, null,
        false, null))
      databases.add(stripDj("" + db.get("ID")));

    // expressions in the Dashjoin folder are checked before the query catalog is read below
    queries = ids("dj-query-catalog");
    functions = ids("dj-function");
    libraries = ids("dj-library");

    for (String folder : new String[] {"Dashjoin", "dj-query-catalog", "widget", "page",
        "dj-database", "dj-function", "dj-config", "dj-library"}) {
      var all =
          expressionService.getData().all(sc, "config", folder, null, null, null, false, null);

      for (var item : all) {
        JsonNode tree = om.valueToTree(item);

        current = "model/" + folder + "/" + item.get("ID") + ".json";
        currentID = "" + item.get("ID");

        if (folder.equals("dj-query-catalog")) {
          checkDanglingPointers(tree);
          QueryMeta qm = om.convertValue(tree, QueryMeta.class);
          queryTypes.put(qm.ID, qm.type);
          if (qm.arguments != null && !qm.arguments.isEmpty())
            queriesWithArguments.add(qm.ID);
          checkDatabase(qm.database);
        }
        if (folder.equals("dj-function")) {
          checkDanglingPointers(tree);
          // only hint at functions that are normally called via $call from the App. Others like
          // ETL, Receive or MCPServer are run manually, via schedule, webhook or API
          if (CALLED_FUNCTIONS.contains(item.get("djClassName")) && item.get("cron") == null
              && item.get("every") == null)
            calledFunctions.add(currentID);
        }
        if (folder.equals("widget"))
          checkLayout(tree.get("layout"));
        if (folder.equals("page"))
          checkLayout(tree.get("layout"));
        if (folder.equals("dj-database")) {
          checkDatabaseLayout(tree);
          check(item);
        }
        checkRoles(tree);
        checkExpressions(tree, folder.equals("dj-function"));
      }
    }

    checkUnused();

    // run unit tests
    for (Map<String, Object> test : expressionService.getData().all(sc, "config", "dj-library",
        null, null, null, false, null)) {
      current = "model/dj-library/" + test.get("ID") + ".json";
      currentID = "" + test.get("ID");
      try {
        expressionService.resolve(sc, "$eval($use('unit-test'))($)", test);
      } catch (Exception e) {
        fail(e.getMessage());
      }
    }

    if (messages.isEmpty())
      return "no warnings";
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

    // the frontend (DJBaseComponent.getData) evaluates expression first and ignores query
    if (tree.get("query") != null && tree.get("expression") != null)
      fail("query is ignored because expression is set in widget " + widget);

    if (Arrays.asList("table", "chart", "tree").contains(widget) && tree.get("query") != null) {
      String q = tree.get("query").asText();
      usedQueries.add(q);
      if (!queryTypes.containsKey(q) && !BUILT_IN_QUERIES.contains(q))
        fail("unknown query in widget " + widget + ": " + q);
      else
        assertTrue(!"write".equals(queryTypes.get(q)),
            "write query must not be used in widget " + widget, q);
      if (queriesWithArguments.contains(q))
        assertTrue(tree.get("arguments") != null && !tree.get("arguments").asText().isBlank(),
            "no arguments set for query", q);
    }

    if (tree.get("database") != null && tree.get("database").isTextual())
      checkDatabase(tree.get("database").asText());

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
          && Arrays.asList("container", "card", "toolbar", "expansion", "page", "stepper", "tabs")
              .contains(widget))
        continue;
      if (field.equals("schema")
          && Arrays.asList("button", "edit", "create", "variable").contains(widget)) {
        // edit and create use the table schema which carries the column metadata
        checkSchema(e.getValue(), Arrays.asList("edit", "create").contains(widget));
        continue;
      }
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

  /**
   * column metadata (see org.dashjoin.model.Property) that is present in table schemas used by the
   * edit and create widgets
   */
  static final List<String> TABLE_SCHEMA_METADATA =
      Arrays.asList("ID", "parent", "name", "pkpos", "ref", "dbType", "comment", "displayWith");

  void checkSchema(JsonNode schema) {
    checkSchema(schema, false);
  }

  void checkSchema(JsonNode schema, boolean tableSchema) {

    assertTrue(schema.getNodeType().equals(JsonNodeType.OBJECT), "schema must be an object",
        schema);

    String type = schema.get("type") == null ? "string" : schema.get("type").asText();
    String widget = schema.get("widget") == null ? "string" : schema.get("widget").asText();
    assertTrue(INPUTS.contains(widget), "unknown widget type", widget);

    // the frontend (useChoices) reads options, choices, enum or choicesUrl
    if (OPTION_INPUTS.contains(widget))
      assertTrue(Stream.of("options", "choices", "enum", "choicesUrl")
          .anyMatch(k -> schema.get(k) != null), "no options set for input", widget);
    for (Entry<String, JsonNode> e : schema.properties()) {
      String key = e.getKey();
      if (key.equals("examples") && e.getValue().getNodeType().equals(JsonNodeType.ARRAY))
        continue;
      if (key.equals("style") && e.getValue().getNodeType().equals(JsonNodeType.OBJECT))
        continue;
      if (key.equals("title") && e.getValue().getNodeType().equals(JsonNodeType.STRING))
        continue;
      if (key.equals("description") && e.getValue().getNodeType().equals(JsonNodeType.STRING))
        continue;
      if (key.equals("size") && e.getValue().getNodeType().equals(JsonNodeType.NUMBER))
        continue;
      if (key.equals("required") && e.getValue().getNodeType().equals(JsonNodeType.BOOLEAN))
        continue;
      if ((key.equals("readOnly") || key.equals("createOnly"))
          && e.getValue().getNodeType().equals(JsonNodeType.BOOLEAN))
        continue;
      if (key.equals("options")) {
        assertTrue(OPTION_INPUTS.contains(widget), "options only allowed for select inputs",
            widget);
        continue;
      }
      if (Arrays.asList("multiple", "capture", "accept", "limit").contains(key)) {
        assertTrue(FILE_INPUTS.contains(widget), key + " only allowed for file inputs", widget);
        continue;
      }
      if (key.equals("type"))
        continue;
      if (key.equals("widget"))
        continue;
      if (key.equals("properties") && type.equals("object"))
        continue;
      if (key.equals("items") && type.equals("array"))
        continue;
      if (key.equals("reorder") && type.equals("array"))
        continue;
      if (key.equals("if") && e.getValue().getNodeType().equals(JsonNodeType.STRING))
        continue;
      if (tableSchema && TABLE_SCHEMA_METADATA.contains(key))
        continue;
      fail("unknown property", e);
    }

    // check subschema
    if (schema.get("properties") != null)
      for (Entry<String, JsonNode> p : schema.get("properties").properties())
        checkSchema(p.getValue(), tableSchema);
    if (schema.get("items") != null)
      checkSchema(schema.get("items"), tableSchema);
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

  void checkExpressions(JsonNode tree, boolean backendOnly) {
    if (tree.get("widget") != null)
      if (tree.get("widget").asText().equals("actionTable"))
        if (tree.get("properties") != null)
          for (Entry<String, JsonNode> e : tree.get("properties").properties())
            if (!e.getKey().endsWith("-pointer"))
              checkExpressions(om.createObjectNode().set("expression", e.getValue()), backendOnly);
    Iterator<JsonNode> items = tree.iterator();
    while (items.hasNext())
      checkExpressions(items.next(), backendOnly);
    for (Entry<String, JsonNode> kid : tree.properties()) {
      for (String field : PojoDatabase.EXPRESSION_FIELDS)
        if (kid.getKey().equals(field)) {
          String expr = kid.getValue().asText();

          if (expr.toLowerCase().startsWith("//javascript")
              || expr.toLowerCase().startsWith("// javascript"))
            continue;

          if (!(kid.getValue() instanceof ObjectNode)) {
            if (expr.isBlank()) {
              fail("Empty expression " + kid);
              continue;
            }

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
              if (!backendOnly)
                for (String f : CLIENT_FUNCTIONS)
                  jsonata.registerFunction(f, x);
              for (Function<?, ?> inst : SafeServiceLoader.load(Function.class))
                if (inst.getID() != null)
                  if (inst.getID().equals("use"))
                    jsonata.registerFunction(inst.getID(), new JFunction(new JFunctionCallable() {
                      @Override
                      public Object call(Object input, @SuppressWarnings("rawtypes") List args)
                          throws Throwable {
                        return "function(){1}";
                      }
                    }, null));
                  else
                    jsonata.registerFunction(inst.getID(), x);
              try {
                jsonata.evaluate(null);
              } catch (JException e) {
                fail("Error running expression\n" + insertError(expr, e));
              } catch (Exception e) {
                fail("Error running expression\n" + expr + ": " + e.getMessage());
              }
              checkReferences(new Parser().parse(expr),
                  Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>()));
            } catch (JException je) {
              fail("Error parsing expression\n" + insertError(expr, je));
            }
          }
        }
    }
  }

  /**
   * roles, readRoles and writeRoles must reference a role defined in dj-role
   */
  void checkRoles(JsonNode tree) {
    if (roles == null)
      return;
    for (Entry<String, JsonNode> e : tree.properties())
      if (Arrays.asList("roles", "readRoles", "writeRoles").contains(e.getKey())
          && e.getValue().isArray())
        for (JsonNode role : e.getValue())
          if (role.isTextual())
            assertTrue(roles.contains(role.asText()), "unknown role in " + e.getKey(),
                role.asText());
    for (JsonNode i : tree)
      checkRoles(i);
  }

  Set<String> ids(String folder) throws Exception {
    Set<String> res = new HashSet<String>();
    for (var i : expressionService.getData().all(sc, "config", folder, null, null, null, false,
        null))
      res.add("" + i.get("ID"));
    return res;
  }

  /**
   * functions whose first argument is a database name
   */
  static final List<String> DATABASE_FUNCTIONS = Arrays.asList("all", "read", "create", "update",
      "delete", "upsert", "setIfAbsent", "traverse", "incoming", "query", "queryGraph",
      "adHocQuery", "adHocQueryGraph");

  /**
   * the JSONata library does not expose its AST, so the package private fields of Parser.Symbol
   * are read via reflection. If this fails (e.g. fields are renamed in a new version), the
   * reference checks find nothing - DjLintTest.danglingReferences guards against this
   */
  static final Map<String, Field> SYMBOL_FIELDS = new HashMap<String, Field>();
  static {
    for (Field f : Parser.Symbol.class.getDeclaredFields())
      if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic())
        try {
          f.setAccessible(true);
          SYMBOL_FIELDS.put(f.getName(), f);
        } catch (RuntimeException e) {
          // field not accessible - skip it
        }
  }

  static Object field(Object symbol, String name) {
    Field f = SYMBOL_FIELDS.get(name);
    if (f == null || !(symbol instanceof Parser.Symbol))
      return null;
    try {
      return f.get(symbol);
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  /**
   * returns the string literal at position i of a function call, or null if the argument is
   * missing or computed
   */
  static String literal(List<?> args, int i) {
    if (args == null || args.size() <= i)
      return null;
    Object arg = args.get(i);
    if ("string".equals(field(arg, "type")) && field(arg, "value") instanceof String s)
      return s;
    return null;
  }

  /**
   * walk the JSONata AST and check function calls with literal arguments that reference queries,
   * databases, functions or libraries. Unlike the evaluation in checkExpressions, this also visits
   * branches and lambdas that are not executed
   */
  void checkReferences(Object node, Set<Object> visited) {
    if (node == null)
      return;
    if (node instanceof Collection<?> c) {
      for (Object i : c)
        checkReferences(i, visited);
      return;
    }
    if (node instanceof Object[] a) {
      for (Object i : a)
        checkReferences(i, visited);
      return;
    }
    if (!(node instanceof Parser.Symbol) || !visited.add(node))
      return;

    Object type = field(node, "type");
    if ("function".equals(type) || "partial".equals(type)) {
      Object proc = field(node, "procedure");
      if ("variable".equals(field(proc, "type")) && field(proc, "value") instanceof String name)
        checkCall(name, (List<?>) field(node, "arguments"));
    }

    for (Field f : SYMBOL_FIELDS.values())
      try {
        checkReferences(f.get(node), visited);
      } catch (IllegalAccessException e) {
        // ignore
      }
  }

  void checkCall(String name, List<?> args) {
    if (DATABASE_FUNCTIONS.contains(name))
      checkDatabase(literal(args, 0));
    if (name.equals("query") || name.equals("queryGraph"))
      if (!BUILT_IN_QUERIES.contains(literal(args, 1)))
        checkId(queries, usedQueries, literal(args, 1), "unknown query in $" + name);
    if (name.equals("call")) {
      String f = literal(args, 0);
      if (OPTIONAL_FUNCTIONS.contains(f))
        usedFunctions.add(f);
      else
        checkId(functions, usedFunctions, f, "unknown function in $call");
    }
    if (name.equals("use"))
      checkId(libraries, usedLibraries, literal(args, 0), "unknown library in $use");
  }

  /**
   * report queries, functions and libraries that are not referenced by a layout or by a literal
   * argument in an expression. These are hints only: the item may be called via the REST API or
   * from an expression with a computed argument like $call(fn)
   */
  void checkUnused() {
    hintUnused("dj-query-catalog", "query", queries, usedQueries);
    hintUnused("dj-function", "function", calledFunctions, usedFunctions);
    hintUnused("dj-library", "library", libraries, usedLibraries);
  }

  void hintUnused(String folder, String kind, Set<String> all, Set<String> used) {
    if (all == null)
      return;
    for (String id : new TreeSet<String>(all))
      if (!used.contains(id))
        messages.add("model/" + folder + "/" + id + ".json: hint: " + kind
            + " is not referenced in the App (it might still be used via the API)");
  }

  void checkId(Set<String> known, Set<String> used, String id, String message) {
    if (id != null)
      used.add(id);
    if (known != null && id != null)
      assertTrue(known.contains(id), message, id);
  }

  static String stripDj(String database) {
    return database.startsWith("dj/") ? database.substring("dj/".length()) : database;
  }

  /**
   * databases are referenced by name ("northwind") or ID ("dj/northwind")
   */
  void checkDatabase(String database) {
    if (database == null || databases == null)
      return;
    if (database.contains("$") || database.contains("{"))
      // computed value
      return;
    assertTrue(databases.contains(stripDj(database)), "unknown database", database);
  }

  String insertError(String expr, JException e) {
    if (e.getLocation() >= 0)
      return expr.substring(0, e.getLocation()) + "<mark>" + e.getMessage() + "</mark>"
          + expr.substring(e.getLocation());
    else
      return expr;
  }

  void fail(String message) {
    messages.add(current + ": " + message);
  }

  void fail(String message, Object value) {
    assertTrue(false, message, value);
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
