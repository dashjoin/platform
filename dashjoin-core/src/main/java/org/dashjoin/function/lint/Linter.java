package org.dashjoin.function.lint;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.service.PojoDatabase;
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Parser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Lints an app: walks its config records and hands every node to the rules registered for its kind
 * (see Kind and Rules). Rules report findings with report(...).
 *
 * <pre>
 * Linter lint = new Linter();
 * lint.define(folder, record)   // for all records: what the app defines (queries, roles, ...)
 * lint.record(folder, record)   // for all records: walk and check
 * lint.finish()                 // app rules: unused items, orphaned files
 * lint.findings
 * </pre>
 *
 * The sets of defined items (roles, databases, queries, functions, libraries) are null until
 * defineAll() is called; references of an unknown kind are not checked (e.g. in unit tests).
 */
public class Linter {

  static final ObjectMapper om =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public final List<Finding> findings = new ArrayList<>();

  final List<LintRule> rules;

  /** rule IDs not to run */
  final Set<String> ignore = new HashSet<>();

  WidgetCatalog catalog;

  /** what the app defines; null: unknown, references are not checked */
  public Set<String> roles;
  public Set<String> databases;
  public Set<String> queries;
  public Set<String> functions;
  public Set<String> libraries;

  /** query ID to its type (read, write) */
  public final Map<String, String> queryTypes = new HashMap<>();

  /** IDs of catalog queries that declare arguments */
  public final Set<String> queriesWithArguments = new HashSet<>();

  /** IDs of dj-functions of a CALLED_FUNCTIONS type that are not scheduled */
  public final Set<String> calledFunctions = new HashSet<>();

  /** references found while walking: kind to IDs */
  final Map<String, Set<String>> used = new HashMap<>();

  /** the app's model folder, for the orphaned-file rule; null: not checked */
  public File modelDir;

  /** runs a dj-library record's unit tests, returning the error or null; null: not run */
  public Function<Map<String, Object>, String> unitTests;

  public Linter() {
    this(WidgetCatalog.get(), Rules.ALL);
  }

  public Linter(WidgetCatalog catalog, List<LintRule> rules) {
    this.catalog = catalog;
    this.rules = rules;
    use("library", "unit-test");
  }

  /** do not run these rules */
  public Linter ignore(Collection<String> ruleIds) {
    ignore.addAll(ruleIds);
    return this;
  }

  /**
   * from now on, references to roles, databases, queries, functions and libraries are checked
   * against what define(...) adds
   */
  public void defineAll() {
    roles = new HashSet<>();
    databases = new HashSet<>(Arrays.asList("config"));
    queries = new HashSet<>();
    functions = new HashSet<>();
    libraries = new HashSet<>();
  }

  /**
   * function types that are normally called via $call from the App
   */
  static final List<String> CALLED_FUNCTIONS = Arrays.asList("org.dashjoin.function.Invoke",
      "org.dashjoin.function.RestJson", "org.dashjoin.function.Email");

  /**
   * collects what a record defines, before the records are walked
   */
  public void define(String folder, Map<String, Object> item) {
    String id = "" + item.get("ID");
    switch (folder) {
      case "dj-role":
        add(roles, id);
        break;
      case "dj-database":
        add(databases, Rules.stripDj(id));
        break;
      case "dj-query-catalog":
        add(queries, id);
        QueryMeta qm = om.convertValue(item, QueryMeta.class);
        queryTypes.put(qm.ID, qm.type);
        if (qm.arguments != null && !qm.arguments.isEmpty())
          queriesWithArguments.add(qm.ID);
        break;
      case "dj-function":
        add(functions, id);
        // only hint at functions that are normally called via $call from the App. Others like
        // ETL, Receive or MCPServer are run manually, via schedule, webhook or API
        if (CALLED_FUNCTIONS.contains(item.get("djClassName")) && item.get("cron") == null
            && item.get("every") == null)
          calledFunctions.add(id);
        break;
      case "dj-library":
        add(libraries, id);
        break;
      case "widget":
        // config/widget layouts and runtime components accept any properties
        catalog = catalog.withAppWidgets(List.of(id));
        break;
      default:
    }
  }

  static void add(Set<String> set, String id) {
    if (set != null)
      set.add(id);
  }

  /**
   * walks a record: its layouts, forms, expressions, references and database metadata
   */
  public void record(String folder, Map<String, Object> item) {
    JsonNode tree = om.valueToTree(item);
    String file = "model/" + folder + "/" + item.get("ID") + ".json";

    Node record = new Node(Kind.RECORD, file, "", tree);
    record.folder = folder;
    record.record = item;
    visit(record);

    if (folder.equals("page") || folder.equals("widget"))
      if (tree.get("layout") != null)
        layout(file, "/layout", tree.get("layout"));

    if (folder.equals("dj-database")) {
      databaseLayouts(file, "", tree);
      metadata(file, item);
    }

    if (folder.equals("dj-query-catalog") && tree.get("database") != null)
      reference(file, "/database", tree, "database", tree.get("database").asText(),
          "query catalog");

    roles(file, "", tree);
    expressions(file, "", tree, folder.equals("dj-function"));
  }

  /**
   * runs the app rules (unused items, orphaned files)
   */
  public void finish() {
    visit(new Node(Kind.APP, "", "", null));
  }

  /** the table and record layouts of a database */
  void databaseLayouts(String file, String path, JsonNode tree) {
    for (String layout : new String[] {"instanceLayout", "tableLayout"})
      if (tree.get(layout) != null)
        layout(file, path + "/" + layout, tree.get(layout));
    for (Entry<String, JsonNode> e : tree.properties())
      databaseLayouts(file, path + "/" + escape(e.getKey()), e.getValue());
  }

  /** a widget and its children */
  public void layout(String file, String path, JsonNode tree) {
    if (tree.get("children") != null && tree.get("children").isArray())
      for (int i = 0; i < tree.get("children").size(); i++)
        layout(file, path + "/children/" + i, tree.get("children").get(i));

    Node node = new Node(Kind.LAYOUT, file, path, tree);
    node.widget = node.text("widget");
    // a template (e.g. "${pk[0]}" in the config/widget preview) names the widget at runtime
    if (node.widget != null && node.widget.contains("${"))
      return;
    visit(node);

    if (node.widget == null)
      return;
    for (Entry<String, JsonNode> e : tree.properties()) {
      String kind = catalog.reference(node.widget, e.getKey());
      if (kind != null && e.getValue().isTextual())
        reference(file, path + "/" + escape(e.getKey()), tree, kind, e.getValue().asText(),
            "widget " + node.widget, node.widget);
      if (catalog.isForm(node.widget, e.getKey()))
        form(file, path + "/" + escape(e.getKey()), e.getValue(),
            Rules.TABLE_SCHEMA_WIDGETS.contains(node.widget));
    }
  }

  /** a form and its fields */
  public void form(String file, String path, JsonNode schema, boolean tableSchema) {
    Node node = new Node(Kind.FORM, file, path, schema);
    node.tableSchema = tableSchema;
    visit(node);
    if (!schema.isObject())
      return;
    if (schema.get("properties") != null)
      for (Entry<String, JsonNode> p : schema.get("properties").properties())
        form(file, path + "/properties/" + escape(p.getKey()), p.getValue(), tableSchema);
    if (schema.get("items") != null)
      form(file, path + "/items", schema.get("items"), tableSchema);
  }

  /** the database, its tables and their columns */
  @SuppressWarnings("unchecked")
  void metadata(String file, Map<String, Object> db) {
    metadata(file, "", db, null, db, null);
    if (db.get("tables") instanceof Map<?, ?> tables)
      for (Entry<String, Map<String, Object>> t : ((Map<String, Map<String, Object>>) tables)
          .entrySet()) {
        String tp = "/tables/" + escape(t.getKey());
        metadata(file, tp, db, db.get("ID"), t.getValue(), t.getKey());
        if (t.getValue() != null && t.getValue().get("properties") instanceof Map<?, ?> columns)
          for (Entry<String, Map<String, Object>> c : ((Map<String, Map<String, Object>>) columns)
              .entrySet())
            metadata(file, tp + "/properties/" + escape(c.getKey()), db, t.getValue().get("ID"),
                c.getValue(), c.getKey());
      }
  }

  void metadata(String file, String path, Map<String, Object> root, Object parentId,
      Map<String, Object> map, String key) {
    if (map == null)
      return;
    Node node = new Node(Kind.METADATA, file, path, null);
    node.root = root;
    node.parentId = parentId;
    node.map = map;
    node.key = key;
    visit(node);
  }

  /** roles, readRoles and writeRoles anywhere in the record */
  void roles(String file, String path, JsonNode tree) {
    for (Entry<String, JsonNode> e : tree.properties())
      if (Rules.ROLE_FIELDS.contains(e.getKey()) && e.getValue().isArray())
        for (JsonNode role : e.getValue())
          if (role.isTextual())
            reference(file, path + "/" + escape(e.getKey()), tree, "role", role.asText(),
                e.getKey());
    if (tree.isArray())
      for (int i = 0; i < tree.size(); i++)
        roles(file, path + "/" + i, tree.get(i));
    else
      for (Entry<String, JsonNode> e : tree.properties())
        roles(file, path + "/" + escape(e.getKey()), e.getValue());
  }

  /** widgets whose "actions" (legacy name "properties") map labels to expressions */
  static final Set<String> ACTION_WIDGETS = Set.of("table", "actionTable", "mdxeditor");

  /** the expressions in a tree (PojoDatabase.EXPRESSION_FIELDS, table / actionTable actions) */
  public void expressions(String file, String path, JsonNode tree, boolean backendOnly) {
    if (tree.get("widget") != null && ACTION_WIDGETS.contains(tree.get("widget").asText()))
      for (String key : List.of("actions", "properties"))
        if (tree.get(key) != null && tree.get(key).isObject())
          for (Entry<String, JsonNode> e : tree.get(key).properties())
            if (!e.getKey().endsWith("-pointer"))
              expression(file, path + "/" + key + "/" + escape(e.getKey()), e.getKey(),
                  e.getValue(), backendOnly);

    if (tree.isArray())
      for (int i = 0; i < tree.size(); i++)
        expressions(file, path + "/" + i, tree.get(i), backendOnly);
    else
      for (Entry<String, JsonNode> e : tree.properties())
        expressions(file, path + "/" + escape(e.getKey()), e.getValue(), backendOnly);

    for (String field : PojoDatabase.EXPRESSION_FIELDS)
      if (tree.get(field) != null)
        expression(file, path + "/" + field, field, tree.get(field), backendOnly);
  }

  void expression(String file, String path, String field, JsonNode value, boolean backendOnly) {
    if (value instanceof ObjectNode)
      return;
    String expr = value.asText();
    if (expr.toLowerCase().startsWith("//javascript")
        || expr.toLowerCase().startsWith("// javascript"))
      return;

    Node node = new Node(Kind.EXPRESSION, file, path, value);
    node.field = field;
    node.expression = expr;
    node.backendOnly = backendOnly;
    if (!expr.isBlank())
      try {
        node.compiled = Jsonata.jsonata(expr);
      } catch (JException e) {
        node.parseError = e;
      }
    visit(node);

    if (node.compiled != null)
      try {
        calls(file, path, value, new Parser().parse(expr),
            java.util.Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>()));
      } catch (JException e) {
        // reported by expression-syntax
      }
  }

  /**
   * the JSONata library does not expose its AST, so the package private fields of Parser.Symbol
   * are read via reflection. If this fails (e.g. fields are renamed in a new version), the
   * reference checks find nothing - DjLintTest.danglingReferences guards against this
   */
  static final Map<String, Field> SYMBOL_FIELDS = new HashMap<>();
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
   * the string literal at position i of a function call, or null if the argument is missing or
   * computed
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
   * walks the JSONata AST: function calls with literal arguments that name a query, database,
   * function or library (Rules.FUNCTION_REFERENCES) become references. Unlike running the
   * expression, this also visits branches and lambdas that are not executed
   */
  void calls(String file, String path, JsonNode value, Object node, Set<Object> visited) {
    if (node == null)
      return;
    if (node instanceof Collection<?> c) {
      for (Object i : c)
        calls(file, path, value, i, visited);
      return;
    }
    if (node instanceof Object[] a) {
      for (Object i : a)
        calls(file, path, value, i, visited);
      return;
    }
    if (!(node instanceof Parser.Symbol) || !visited.add(node))
      return;

    Object type = field(node, "type");
    if ("function".equals(type) || "partial".equals(type)) {
      Object proc = field(node, "procedure");
      if ("variable".equals(field(proc, "type")) && field(proc, "value") instanceof String name) {
        Map<Integer, String> args = Rules.FUNCTION_REFERENCES.get(name);
        if (args != null)
          for (Entry<Integer, String> arg : args.entrySet()) {
            String id = literal((List<?>) field(node, "arguments"), arg.getKey());
            if (id != null)
              reference(file, path, value, arg.getValue(), id, "$" + name);
          }
      }
    }

    for (Field f : SYMBOL_FIELDS.values())
      try {
        calls(file, path, value, f.get(node), visited);
      } catch (IllegalAccessException e) {
        // ignore
      }
  }

  Node reference(String file, String path, JsonNode value, String kind, String id,
      String where) {
    return reference(file, path, value, kind, id, where, null);
  }

  /** widget: the widget holding the reference (value), null if not in a layout */
  Node reference(String file, String path, JsonNode value, String kind, String id, String where,
      String widget) {
    use(kind, id);
    Node node = new Node(Kind.REFERENCE, file, path, value);
    node.widget = widget;
    node.refKind = kind;
    node.id = id;
    node.where = where;
    visit(node);
    return node;
  }

  void use(String kind, String id) {
    used.computeIfAbsent(kind, k -> new HashSet<>()).add(id);
  }

  public boolean isUsed(String kind, String id) {
    return used.getOrDefault(kind, Set.of()).contains(id);
  }

  /** the IDs of the given kind the app defines, null if unknown */
  public Set<String> defined(String kind) {
    return switch (kind) {
      case "role" -> roles;
      case "database" -> databases;
      case "query" -> queries;
      case "function" -> functions;
      case "library" -> libraries;
      default -> null;
    };
  }

  LintRule rule;

  void visit(Node node) {
    for (LintRule r : rules)
      if (r.on().contains(node.kind) && !ignore.contains(r.id())) {
        rule = r;
        r.check(node, this);
      }
    rule = null;
  }

  /** reports a finding of the rule that is running */
  public void report(Node node, String message) {
    report(node.file, node.path, message);
  }

  public void report(String file, String path, String message) {
    findings.add(new Finding(file, path, rule.id(), rule.severity(), message));
  }

  /** the findings as text, one per line */
  public String text() {
    if (findings.isEmpty())
      return "no warnings";
    List<String> lines = new ArrayList<>();
    for (Finding f : findings)
      lines.add(f.text());
    return String.join("\n", lines);
  }

  public List<Map<String, Object>> json() {
    List<Map<String, Object>> res = new ArrayList<>();
    for (Finding f : findings)
      res.add(f.toMap());
    return res;
  }

  /** JSON pointer escaping of a key */
  static String escape(String key) {
    return key.replace("~", "~0").replace("/", "~1");
  }
}
