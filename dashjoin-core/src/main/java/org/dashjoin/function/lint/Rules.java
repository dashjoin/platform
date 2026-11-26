package org.dashjoin.function.lint;

import static org.dashjoin.function.lint.Kind.APP;
import static org.dashjoin.function.lint.Kind.EXPRESSION;
import static org.dashjoin.function.lint.Kind.FORM;
import static org.dashjoin.function.lint.Kind.LAYOUT;
import static org.dashjoin.function.lint.Kind.METADATA;
import static org.dashjoin.function.lint.Kind.RECORD;
import static org.dashjoin.function.lint.Kind.REFERENCE;
import static org.dashjoin.function.lint.Severity.ERROR;
import static org.dashjoin.function.lint.Severity.HINT;
import static org.dashjoin.function.lint.Severity.WARNING;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.dashjoin.function.DjLint;
import org.dashjoin.function.Function;
import org.dashjoin.service.ConfigQuery;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.SafeServiceLoader;
import org.dashjoin.util.Escape;
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * the lint rules. Widget and form properties come from widgets.json (WidgetCatalog); references
 * from the tables below (ROLE_FIELDS, FUNCTION_REFERENCES) and the query / database properties of
 * widgets.json
 */
public class Rules {

  /** the inputs of a form field ("widget"), from widgets.json (WidgetCatalog.inputs) */
  public static final List<String> INPUTS = WidgetCatalog.get().inputs();

  public static final List<String> OPTION_INPUTS =
      Arrays.asList("select", "multi select", "auto complete");

  public static final List<String> FILE_INPUTS = Arrays.asList("file", "binary file",
      "file with metadata", "binary file with metadata");

  /** form field settings for file inputs only */
  static final List<String> FILE_SETTINGS = Arrays.asList("multiple", "capture", "accept", "limit");

  /** widgets whose form is the table's schema, which carries the column metadata */
  static final List<String> TABLE_SCHEMA_WIDGETS = Arrays.asList("edit", "create");

  /**
   * column metadata (see org.dashjoin.model.Property) that is present in table schemas used by the
   * edit and create widgets
   */
  static final List<String> TABLE_SCHEMA_METADATA =
      Arrays.asList("ID", "parent", "name", "pkpos", "ref", "dbType", "comment", "displayWith");

  /** record fields that list roles (dj-role IDs) */
  static final List<String> ROLE_FIELDS = Arrays.asList("roles", "readRoles", "writeRoles");

  /** functions whose first argument is a database name */
  static final List<String> DATABASE_FUNCTIONS = Arrays.asList("all", "read", "create", "update",
      "delete", "upsert", "setIfAbsent", "traverse", "incoming", "query", "queryGraph",
      "adHocQuery", "adHocQueryGraph");

  /**
   * function arguments that reference something: function name to argument position to the kind
   * (database, query, function, library)
   */
  static final Map<String, Map<Integer, String>> FUNCTION_REFERENCES = new HashMap<>();
  static {
    for (String f : DATABASE_FUNCTIONS)
      FUNCTION_REFERENCES.put(f, new LinkedHashMap<>(Map.of(0, "database")));
    FUNCTION_REFERENCES.get("query").put(1, "query");
    FUNCTION_REFERENCES.get("queryGraph").put(1, "query");
    FUNCTION_REFERENCES.put("call", Map.of(0, "function"));
    FUNCTION_REFERENCES.put("use", Map.of(0, "library"));
  }

  /**
   * optional functions called from built-in pages the user cannot edit (e.g. DataCatalog). They are
   * not reported when missing
   */
  static final List<String> OPTIONAL_FUNCTIONS = Arrays.asList("dj-copilot");

  /**
   * built-in queries that do not need a query catalog entry: PojoDatabase methods marked with
   * ConfigQuery and dj-query-performance (see PojoDatabase.getQueryMeta and query)
   */
  public static final Set<String> BUILT_IN_QUERIES = new HashSet<>();
  static {
    BUILT_IN_QUERIES.add("dj-query-performance");
    for (Method m : PojoDatabase.class.getMethods()) {
      ConfigQuery cq = m.getAnnotation(ConfigQuery.class);
      if (cq != null)
        BUILT_IN_QUERIES.add(cq.query());
    }
  }

  static String stripDj(String database) {
    return database.startsWith("dj/") ? database.substring("dj/".length()) : database;
  }

  // records

  static final LintRule DANGLING_POINTER = LintRule.of("dangling-pointer", ERROR, RECORD,
      (n, lint) -> danglingPointers(n, lint, n.value, ""));

  static void danglingPointers(Node n, Linter lint, JsonNode tree, String path) {
    for (Entry<String, JsonNode> e : tree.properties()) {
      String field = e.getKey();
      if (field.endsWith("-pointer")) {
        String org = field.substring(0, field.length() - "-pointer".length());
        if (tree.get(org) == null)
          lint.report(n.file, path + "/" + Linter.escape(field), "The field value for " + org
              + " cannot be found via the reference " + field + ". There must be a file called "
              + n.record.get("ID") + '.' + e.getValue().asText());
      }
    }
    if (tree.isArray())
      for (int i = 0; i < tree.size(); i++)
        danglingPointers(n, lint, tree.get(i), path + "/" + i);
    else
      for (Entry<String, JsonNode> e : tree.properties())
        danglingPointers(n, lint, e.getValue(), path + "/" + Linter.escape(e.getKey()));
  }

  static final LintRule UNIT_TEST = LintRule.of("unit-test", ERROR, RECORD, (n, lint) -> {
    if (n.folder.equals("dj-library") && lint.unitTests != null) {
      String error = lint.unitTests.apply(n.record);
      if (error != null)
        lint.report(n, error);
    }
  });

  // database metadata

  static final LintRule NAME_MISMATCH = LintRule.of("name-mismatch", ERROR, METADATA, (n, lint) -> {
    if (n.key != null && !n.key.equals(n.map.get("name")))
      lint.report(n, "name property must match key, " + n.map.get("name") + " != " + n.key);
  });

  static final LintRule MISSING_TYPE = LintRule.of("missing-type", ERROR, METADATA, (n, lint) -> {
    String id = "" + n.map.get("ID");
    if (id.split("/").length > 2 && id.contains("dj-database") && n.map.get("type") == null)
      lint.report(n, "no type: " + n.map);
  });

  static final LintRule PARENT_MISMATCH =
      LintRule.of("parent-mismatch", ERROR, METADATA, (n, lint) -> {
        Object parent = n.map.get("parent");
        if (n.parentId != null && parent != null && !n.parentId.equals(parent))
          lint.report(n, "parent property must match parent ID, " + parent + " != " + n.parentId);
      });

  static final LintRule ID_NOT_PARENT_NAME =
      LintRule.of("id-not-parent-name", ERROR, METADATA, (n, lint) -> {
        Object parent = n.map.get("parent");
        if (parent == null)
          return;
        // the name is encoded in the ID (e.g. "/" becomes "%2F")
        String expected = parent + "/" + Escape.encodeTableOrColumnName((String) n.map.get("name"));
        if (!expected.equals(n.map.get("ID")))
          lint.report(n, "ID must be parent + name, " + n.map.get("ID") + " != " + expected);
      });

  static final LintRule ID_PREFIX = LintRule.of("id-prefix", ERROR, METADATA, (n, lint) -> {
    if (!("" + n.map.get("ID")).startsWith("dj/"))
      lint.report(n, "ID must start with dj/: " + n.map.get("ID"));
  });

  static final LintRule REF_NOT_PK = LintRule.of("ref-not-pk", ERROR, METADATA, (n, lint) -> {
    // check ref within the same DB
    if (n.map.get("ref") instanceof String ref && ref.startsWith("" + n.root.get("ID")))
      if (!pk(n, lint, n.root, ref))
        lint.report(n, "ref does not match: " + ref);
  });

  @SuppressWarnings("unchecked")
  static boolean pk(Node n, Linter lint, Map<String, Object> map, String ref) {
    if (ref.equals(map.get("ID"))) {
      if (!Integer.valueOf(0).equals(map.get("pkpos")))
        lint.report(n, ref + " must ref a primary key (pkpos=0), " + map.get("pkpos") + " != 0");
      return true;
    }
    for (Object e : map.values())
      if (e instanceof Map && pk(n, lint, (Map<String, Object>) e, ref))
        return true;
    return false;
  }

  // layouts

  static final LintRule MISSING_WIDGET = LintRule.of("missing-widget", ERROR, LAYOUT, (n, lint) -> {
    if (n.widget == null)
      lint.report(n, "Missing widget: " + n.value);
  });

  static final LintRule UNKNOWN_WIDGET = LintRule.of("unknown-widget", ERROR, LAYOUT, (n, lint) -> {
    if (n.widget != null && !lint.catalog.known(n.widget))
      lint.report(n, "unknown widget: " + n.widget);
  });

  static final LintRule UNKNOWN_PROPERTY =
      LintRule.of("unknown-property", WARNING, LAYOUT, (n, lint) -> {
        Set<String> props = n.widget == null ? null : lint.catalog.properties(n.widget);
        if (props == null)
          return;
        for (String field : (Iterable<String>) n.value::fieldNames)
          if (!field.endsWith("-pointer") && !props.contains(field))
            lint.report(n, "unknown property for widget " + n.widget + ": " + field);
      });

  static final LintRule DEPRECATED_WIDGET =
      LintRule.of("deprecated-widget", WARNING, LAYOUT, (n, lint) -> {
        String message = n.widget == null ? null : lint.catalog.deprecation(n.widget);
        if (message != null)
          lint.report(n, "widget " + n.widget + " is deprecated: " + message);
      });

  static final LintRule DEPRECATED_PROPERTY =
      LintRule.of("deprecated-property", WARNING, LAYOUT, (n, lint) -> {
        if (n.widget == null)
          return;
        for (String field : (Iterable<String>) n.value::fieldNames) {
          String message = lint.catalog.deprecation(n.widget, field);
          if (message != null)
            lint.report(n,
                "property " + field + " is deprecated for widget " + n.widget + ": " + message);
        }
      });

  static final LintRule QUERY_IGNORED = LintRule.of("query-ignored", WARNING, LAYOUT, (n, lint) -> {
    // the frontend (DJBaseComponent.getData) evaluates expression first and ignores query
    if (n.has("query") && n.has("expression"))
      lint.report(n, "query is ignored because expression is set in widget " + n.widget);
  });

  static final LintRule URL_NOT_STRING =
      LintRule.of("url-not-string", ERROR, LAYOUT, (n, lint) -> {
        if (n.has("url") && !n.value.get("url").isTextual())
          lint.report(n, "url property must be a string: " + n.value.get("url"));
      });

  // forms

  static final LintRule FORM_NOT_OBJECT =
      LintRule.of("form-not-object", ERROR, FORM, (n, lint) -> {
        if (!n.value.isObject())
          lint.report(n, "schema must be an object: " + n.value);
      });

  static String input(Node n) {
    return n.has("widget") ? n.text("widget") : "string";
  }

  static String type(Node n) {
    return n.has("type") ? n.text("type") : "string";
  }

  static final LintRule UNKNOWN_INPUT = LintRule.of("unknown-input", ERROR, FORM, (n, lint) -> {
    if (n.value.isObject() && !lint.catalog.inputs().contains(input(n)))
      lint.report(n, "unknown widget type: " + input(n));
  });

  static final LintRule MISSING_OPTIONS =
      LintRule.of("missing-options", WARNING, FORM, (n, lint) -> {
        // the frontend (useChoices) reads options, choices, enum or choicesUrl
        if (n.value.isObject() && OPTION_INPUTS.contains(input(n))
            && Stream.of("options", "choices", "enum", "choicesUrl").noneMatch(n::has))
          lint.report(n, "no options set for input: " + input(n));
      });

  static final LintRule OPTIONS_NOT_SELECT =
      LintRule.of("options-not-select", WARNING, FORM, (n, lint) -> {
        if (n.value.isObject() && n.has("options") && !OPTION_INPUTS.contains(input(n)))
          lint.report(n, "options only allowed for select inputs: " + input(n));
      });

  static final LintRule FILE_SETTING = LintRule.of("file-setting", WARNING, FORM, (n, lint) -> {
    if (n.value.isObject() && !FILE_INPUTS.contains(input(n)))
      for (String key : FILE_SETTINGS)
        if (n.has(key))
          lint.report(n, key + " only allowed for file inputs: " + input(n));
  });

  static final LintRule DEPRECATED_FORM_PROPERTY =
      LintRule.of("deprecated-form-property", WARNING, FORM, (n, lint) -> {
        if (!n.value.isObject())
          return;
        for (String key : (Iterable<String>) n.value::fieldNames) {
          String message = lint.catalog.formDeprecation(key);
          // required: a list on the form or an object field (JSON schema), a flag on a field
          if (message != null && !(key.equals("required") && n.value.get(key).isArray()))
            lint.report(n, "form property " + key + " is deprecated: " + message);
        }
      });

  static final LintRule UNKNOWN_FORM_PROPERTY =
      LintRule.of("unknown-form-property", WARNING, FORM, (n, lint) -> {
        if (!n.value.isObject())
          return;
        for (String key : (Iterable<String>) n.value::fieldNames) {
          boolean ok = lint.catalog.isFormKey(key);
          if (key.equals("properties"))
            ok = type(n).equals("object");
          if (key.equals("items") || key.equals("reorder"))
            ok = type(n).equals("array");
          if (n.tableSchema && TABLE_SCHEMA_METADATA.contains(key))
            ok = true;
          if (!ok)
            lint.report(n, "unknown form property: " + key);
        }
      });

  // expressions

  static final LintRule EMPTY_EXPRESSION =
      LintRule.of("empty-expression", ERROR, EXPRESSION, (n, lint) -> {
        if (n.expression.isBlank())
          lint.report(n, "Empty expression " + n.field);
      });

  static final LintRule EXPRESSION_SYNTAX =
      LintRule.of("expression-syntax", ERROR, EXPRESSION, (n, lint) -> {
        if (n.parseError != null)
          lint.report(n, "Error parsing expression\n" + insertError(n.expression, n.parseError));
      });

  /**
   * runs the expression with all functions stubbed: finds errors the parser does not (e.g. an
   * undefined function)
   */
  static final LintRule EXPRESSION_RUN =
      LintRule.of("expression-run", ERROR, EXPRESSION, (n, lint) -> {
        if (n.compiled == null)
          return;
        Jsonata jsonata = n.compiled;
        JFunction x = new JFunction(new JFunctionCallable() {
          @Override
          public Object call(Object input, @SuppressWarnings("rawtypes") List args) {
            return null;
          }
        }, null);
        jsonata.registerFunction("error", x);
        // $try is registered directly in ExpressionService
        jsonata.registerFunction("try", x);
        if (!n.backendOnly)
          for (String f : DjLint.allClientFunctions())
            jsonata.registerFunction(f, x);
        for (Function<?, ?> inst : SafeServiceLoader.load(Function.class))
          if (inst.getID() != null)
            if (inst.getID().equals("use"))
              jsonata.registerFunction(inst.getID(), new JFunction(new JFunctionCallable() {
                @Override
                public Object call(Object input, @SuppressWarnings("rawtypes") List args) {
                  return "function(){1}";
                }
              }, null));
            else
              jsonata.registerFunction(inst.getID(), x);
        try {
          jsonata.evaluate(null);
        } catch (JException e) {
          lint.report(n, "Error running expression\n" + insertError(n.expression, e));
        } catch (Exception e) {
          lint.report(n, "Error running expression\n" + n.expression + ": " + e.getMessage());
        }
      });

  static String insertError(String expr, JException e) {
    if (e.getLocation() >= 0 && e.getLocation() <= expr.length())
      return expr.substring(0, e.getLocation()) + "<mark>" + e.getMessage() + "</mark>"
          + expr.substring(e.getLocation());
    return expr;
  }

  // references

  static final LintRule UNKNOWN_REFERENCE =
      LintRule.of("unknown-reference", ERROR, REFERENCE, (n, lint) -> {
        Set<String> known = lint.defined(n.refKind);
        String id = n.id;
        switch (n.refKind) {
          case "database":
            // computed value
            if (id.contains("$") || id.contains("{"))
              return;
            id = stripDj(id);
            break;
          case "query":
            if (BUILT_IN_QUERIES.contains(id))
              return;
            break;
          case "function":
            if (OPTIONAL_FUNCTIONS.contains(id))
              return;
            break;
          default:
        }
        if (known != null && !known.contains(id))
          lint.report(n, "unknown " + n.refKind + " in " + n.where + ": " + n.id);
      });

  static final LintRule WRITE_QUERY_IN_WIDGET =
      LintRule.of("write-query-in-widget", ERROR, REFERENCE, (n, lint) -> {
        if ("query".equals(n.refKind) && n.widget != null
            && "write".equals(lint.queryTypes.get(n.id)))
          lint.report(n, "write query must not be used in widget " + n.widget + ": " + n.id);
      });

  static final LintRule MISSING_QUERY_ARGUMENTS =
      LintRule.of("missing-query-arguments", WARNING, REFERENCE, (n, lint) -> {
        if ("query".equals(n.refKind) && n.widget != null
            && lint.queriesWithArguments.contains(n.id)
            && (!n.has("arguments") || n.text("arguments").isBlank()))
          lint.report(n, "no arguments set for query: " + n.id);
      });

  // the app

  /**
   * queries, functions and libraries that are not referenced by a layout or by a literal argument
   * in an expression. These are hints only: the item may be called via the REST API or from an
   * expression with a computed argument like $call(fn)
   */
  static final LintRule UNUSED = LintRule.of("unused", HINT, APP, (n, lint) -> {
    unused(lint, "dj-query-catalog", "query", lint.queries);
    unused(lint, "dj-function", "function", lint.calledFunctions);
    unused(lint, "dj-library", "library", lint.libraries);
  });

  static void unused(Linter lint, String folder, String kind, Set<String> all) {
    if (all == null)
      return;
    for (String id : new TreeSet<>(all))
      if (!lint.isUsed(kind, id))
        lint.report("model/" + folder + "/" + id + ".json", "",
            kind + " is not referenced in the App (it might still be used via the API)");
  }

  /**
   * files in model/<folder>/ that hold an externalized field (ID.ext) but are not referenced by any
   * X-pointer field. This happens if the pointer is removed from the JSON or the JSON is renamed or
   * deleted outside of the platform. The platform does not read these files
   */
  static final LintRule ORPHANED_FILE = LintRule.of("orphaned-file", WARNING, APP, (n, lint) -> {
    File[] folders = lint.modelDir == null ? null : lint.modelDir.listFiles(File::isDirectory);
    if (folders == null)
      return;
    Arrays.sort(folders);
    for (File folder : folders) {
      File[] files = folder.listFiles(File::isFile);
      if (files == null)
        continue;
      Arrays.sort(files);

      // names of the files referenced via X-pointer
      Set<String> referenced = new HashSet<>();
      for (File f : files)
        if (f.getName().endsWith(".json"))
          try {
            JsonNode tree = Linter.om.readTree(f);
            String id = tree.get("ID") != null ? tree.get("ID").asText() : null;
            String prefix = id != null ? Escape.filename(id)
                : f.getName().substring(0, f.getName().length() - ".json".length());
            collectPointers(prefix, tree, referenced);
          } catch (IOException e) {
            // invalid JSON is reported elsewhere
          }

      for (File f : files)
        if (!f.getName().endsWith(".json") && !f.getName().startsWith(".")
            && !referenced.contains(f.getName()))
          lint.report("model/" + folder.getName() + "/" + f.getName(), "",
              "externalized field is not referenced by a -pointer field in any JSON file");
    }
  });

  static void collectPointers(String prefix, JsonNode tree, Set<String> referenced) {
    for (Entry<String, JsonNode> e : tree.properties())
      if (e.getKey().endsWith("-pointer") && e.getValue().isTextual())
        referenced.add(prefix + "." + e.getValue().asText());
    for (JsonNode i : tree)
      collectPointers(prefix, i, referenced);
  }

  /** all rules, in the order they run on a node */
  public static final List<LintRule> ALL = List.of(DANGLING_POINTER, UNIT_TEST, NAME_MISMATCH,
      MISSING_TYPE, PARENT_MISMATCH, ID_NOT_PARENT_NAME, ID_PREFIX, REF_NOT_PK, MISSING_WIDGET,
      UNKNOWN_WIDGET, DEPRECATED_WIDGET, UNKNOWN_PROPERTY, DEPRECATED_PROPERTY, QUERY_IGNORED,
      URL_NOT_STRING, FORM_NOT_OBJECT, UNKNOWN_INPUT, MISSING_OPTIONS, OPTIONS_NOT_SELECT,
      FILE_SETTING, UNKNOWN_FORM_PROPERTY, DEPRECATED_FORM_PROPERTY, EMPTY_EXPRESSION, EXPRESSION_SYNTAX, EXPRESSION_RUN,
      UNKNOWN_REFERENCE, WRITE_QUERY_IN_WIDGET, MISSING_QUERY_ARGUMENTS, UNUSED, ORPHANED_FILE);
}
