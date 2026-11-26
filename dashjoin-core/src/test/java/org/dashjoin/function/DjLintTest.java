package org.dashjoin.function;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.dashjoin.function.lint.Finding;
import org.dashjoin.function.lint.Linter;
import org.dashjoin.function.lint.Rules;
import org.dashjoin.function.lint.Severity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DjLintTest {

  static final ObjectMapper om = new ObjectMapper();

  static final String PAGE = "model/page/test.json";

  static JsonNode json(String s) throws Exception {
    return om.readTree(s);
  }

  @SuppressWarnings("unchecked")
  static Map<String, Object> map(String s) throws Exception {
    return om.readValue(s, Map.class);
  }

  static void layout(Linter lint, String s) throws Exception {
    lint.layout(PAGE, "/layout", json(s));
  }

  static void expressions(Linter lint, String s, boolean backendOnly) throws Exception {
    lint.expressions(PAGE, "", json(s), backendOnly);
  }

  static String all(Linter lint) {
    return "" + lint.findings;
  }

  @Test
  public void emptyForeach() throws Exception {
    Linter lint = new Linter();
    expressions(lint, "{\"type\": \"ETL\", \"expression\": \"1\", \"foreach\": \"\"}", true);
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertEquals("empty-expression", lint.findings.get(0).rule());
    Assertions.assertTrue(all(lint).contains("Empty expression foreach"), all(lint));
  }

  @Test
  public void validForeach() throws Exception {
    Linter lint = new Linter();
    expressions(lint, "{\"type\": \"ETL\", \"expression\": \"1\", \"foreach\": \"[1..3]\"}", true);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void layoutQuery() throws Exception {
    Linter lint = new Linter();
    lint.queries = new HashSet<>(Arrays.asList("list", "insert"));
    lint.queryTypes.put("list", "read");
    lint.queryTypes.put("insert", "write");

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    layout(lint, "{\"widget\": \"table\", \"query\": \"nope\"}");
    layout(lint, "{\"widget\": \"table\", \"query\": \"insert\"}");
    Assertions.assertEquals(2, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("unknown query in widget table: nope"), all(lint));
    Assertions.assertTrue(all(lint).contains("write query must not be used in widget table"),
        all(lint));
  }

  @Test
  public void queryArguments() throws Exception {
    Linter lint = new Linter();
    lint.queryTypes.put("list", "read");
    lint.queryTypes.put("all", "read");
    lint.queriesWithArguments.add("list");

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\", \"arguments\": \"{'limit': 5}\"}");
    layout(lint, "{\"widget\": \"table\", \"query\": \"all\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\"}");
    layout(lint, "{\"widget\": \"table\", \"query\": \"list\", \"arguments\": \"\"}");
    // the empty arguments expression is reported too
    String all = all(lint);
    Assertions.assertEquals(2,
        lint.findings.stream().filter(f -> f.rule().equals("missing-query-arguments")).count(),
        all);
    Assertions.assertTrue(all.contains("no arguments set for query: list"), all);
  }

  @Test
  public void selectWithoutOptions() throws Exception {
    Linter lint = new Linter();
    lint.form(PAGE, "", json("{\"widget\": \"select\", \"options\": \"['a', 'b']\"}"), false);
    lint.form(PAGE, "", json("{\"widget\": \"string\"}"), false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    lint.form(PAGE, "", json("{\"widget\": \"multi select\"}"), false);
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("no options set for input: multi select"), all(lint));
  }

  @Test
  public void queryAndExpression() throws Exception {
    Linter lint = new Linter();
    lint.queryTypes.put("list", "read");

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\"}");
    layout(lint, "{\"widget\": \"table\", \"expression\": \"1\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\", \"expression\": \"1\"}");
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("query is ignored because expression"), all(lint));
  }

  @Test
  public void layoutDatabase() throws Exception {
    Linter lint = new Linter();
    lint.databases = new HashSet<>(Arrays.asList("config", "northwind"));

    layout(lint, "{\"widget\": \"table\", \"database\": \"northwind\"}");
    layout(lint, "{\"widget\": \"table\", \"database\": \"dj/northwind\"}");
    layout(lint, "{\"widget\": \"analytics\", \"database\": \"config\"}");
    layout(lint, "{\"widget\": \"table\", \"database\": \"${db}\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    layout(lint, "{\"widget\": \"analytics\", \"database\": \"nope\"}");
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("unknown database in widget analytics: nope"),
        all(lint));
  }

  Linter referenceLint() {
    Linter lint = new Linter();
    lint.queries = new HashSet<>(Arrays.asList("list"));
    lint.databases = new HashSet<>(Arrays.asList("config", "northwind"));
    lint.functions = new HashSet<>(Arrays.asList("sendMail"));
    lint.libraries = new HashSet<>(Arrays.asList("unit-test"));
    return lint;
  }

  @Test
  public void validReferences() throws Exception {
    Linter lint = referenceLint();
    expressions(lint, "{\"expression\": "
        + "\"x ? $call('sendMail') : [$query('northwind', 'list'), $all('dj/northwind', 'T'), "
        + "$eval($use('unit-test')), $call(fn), function(){$read('config', 'page', 'p')}]\"}",
        false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void builtInQueries() throws Exception {
    Linter lint = referenceLint();
    layout(lint, "{\"widget\": \"table\", \"database\": \"config\", \"query\": \"dj-navigation\"}");
    expressions(lint,
        "{\"expression\": \"[$query('config', 'dj-functions'), $query('config', 'dj-query-performance')]\"}",
        false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
    // built-in queries are not hinted as unused
    lint.finish();
    for (String q : Rules.BUILT_IN_QUERIES)
      Assertions.assertFalse(all(lint).contains("/" + q + ".json"), all(lint));
  }

  @Test
  public void missingCopilot() throws Exception {
    Linter lint = referenceLint();
    // dj-copilot is optional and called from built-in pages
    expressions(lint, "{\"expression\": \"$call('dj-copilot', {})\"}", false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void danglingReferences() throws Exception {
    Linter lint = referenceLint();
    // unknown references in branches and lambdas that are never evaluated
    expressions(lint, "{\"expression\": "
        + "\"x ? $call('nope') : [$query('northwind', 'nope'), $all('nope', 'T'), "
        + "function(){$use('nope')}]\"}", false);
    String all = all(lint);
    Assertions.assertEquals(4, lint.findings.size(), all);
    Assertions.assertTrue(all.contains("unknown function in $call: nope"), all);
    Assertions.assertTrue(all.contains("unknown query in $query: nope"), all);
    Assertions.assertTrue(all.contains("unknown database in $all: nope"), all);
    Assertions.assertTrue(all.contains("unknown library in $use: nope"), all);
  }

  @Test
  public void unused() throws Exception {
    Linter lint = referenceLint();
    lint.queries = new HashSet<>(Arrays.asList("list", "orphanQuery"));
    lint.queryTypes.put("list", "read");
    lint.functions = new HashSet<>(Arrays.asList("sendMail", "orphanFunction", "etl"));
    // etl is not a $call type and must not be reported
    lint.calledFunctions.addAll(Arrays.asList("sendMail", "orphanFunction"));
    lint.libraries = new HashSet<>(Arrays.asList("unit-test", "lib", "orphanLibrary"));

    layout(lint, "{\"widget\": \"table\", \"query\": \"list\"}");
    expressions(lint, "{\"expression\": \"x ? $call('sendMail') : $eval($use('lib'))\"}", false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    lint.finish();
    String all = all(lint);
    Assertions.assertEquals(3, lint.findings.size(), all);
    Assertions.assertTrue(all.contains("dj-query-catalog/orphanQuery.json: hint: query"), all);
    Assertions.assertTrue(all.contains("dj-function/orphanFunction.json: hint: function"), all);
    Assertions.assertTrue(all.contains("dj-library/orphanLibrary.json: hint: library"), all);
    Assertions.assertTrue(lint.findings.stream().allMatch(f -> f.severity() == Severity.HINT));
  }

  @Test
  public void roles() throws Exception {
    Linter lint = new Linter();
    lint.roles = new HashSet<>(Arrays.asList("admin", "authenticated"));
    // icons.roles is a string, not a role list, and must be ignored
    lint.record("dj-database", map("{\"ID\": \"dj/northwind\", \"roles\": [\"admin\"], \"icons\": "
        + "{\"roles\": \"person_search\"}, \"tables\": {\"T\": {\"ID\": \"dj/northwind/T\", "
        + "\"name\": \"T\", \"parent\": \"dj/northwind\", \"readRoles\": [\"admni\"], "
        + "\"writeRoles\": [\"authenticated\"]}}}"));
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("unknown role in readRoles: admni"), all(lint));
    Assertions.assertEquals("/tables/T/readRoles", lint.findings.get(0).path());
  }

  @Test
  public void encodedColumnId() throws Exception {
    Linter lint = new Linter();
    // "/" and "%" in a column name are encoded in the ID
    lint.record("dj-database",
        map("{\"ID\": \"dj/db\", \"tables\": {\"T\": {\"ID\": \"dj/db/T\", \"name\": \"T\", "
            + "\"parent\": \"dj/db\", \"properties\": {\"a/b%c\": {\"ID\": \"dj/db/T/a%2Fb%25c\", "
            + "\"parent\": \"dj/db/T\", \"name\": \"a/b%c\", \"type\": \"string\"}, \"plain\": "
            + "{\"ID\": \"dj/db/T/plain\", \"parent\": \"dj/db/T\", \"name\": \"plain\", "
            + "\"type\": \"string\"}}}}}"));
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    lint.record("dj-database",
        map("{\"ID\": \"dj/db\", \"tables\": {\"T\": {\"ID\": \"dj/db/T\", \"name\": \"T\", "
            + "\"parent\": \"dj/db\", \"properties\": {\"a/b\": {\"ID\": \"dj/db/T/a/b\", "
            + "\"parent\": \"dj/db/T\", \"name\": \"a/b\", \"type\": \"string\"}}}}}"));
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("ID must be parent + name"), all(lint));
    Assertions.assertEquals("/tables/T/properties/a~1b", lint.findings.get(0).path());
  }

  @Test
  public void editSchema() throws Exception {
    Linter lint = new Linter();
    // editRedirect is a Studio setting ("studio": true in widgets.json): still allowed
    // edit / create schemas are table schemas and carry the column metadata
    layout(lint, "{\"widget\": \"page\", \"children\": [{\"title\": \"Edit Record\","
        + "\"widget\": \"edit\", \"editRedirect\": \"record\", \"schema\": {\"type\": \"object\","
        + "\"properties\": {\"ID\": {\"ID\": \"dj/result/Ergebnis/ID\", \"parent\": "
        + "\"dj/result/Ergebnis\", \"name\": \"ID\", \"readOnly\": true, \"dbType\": \"serial\","
        + "\"type\": \"number\", \"pkpos\": 0, \"createOnly\": true}}}}]}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));

    // button schemas are written by hand and must not contain column metadata
    layout(lint, "{\"widget\": \"button\", \"schema\": {\"type\": \"object\","
        + "\"properties\": {\"x\": {\"type\": \"string\", \"dbType\": \"serial\"}}}}");
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("unknown form property: dbType"), all(lint));
    Assertions.assertEquals("/layout/schema/properties/x", lint.findings.get(0).path());
  }

  @Test
  public void deprecatedFormProperty() throws Exception {
    Linter lint = new Linter();
    // required: a list on the form (JSON schema) is fine, a flag on a field is deprecated
    layout(lint, "{\"widget\": \"button\", \"schema\": {\"type\": \"object\", \"required\": [\"x\"],"
        + "\"properties\": {\"x\": {\"type\": \"string\", \"required\": true}}}}");
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertEquals("deprecated-form-property", lint.findings.get(0).rule());
    Assertions.assertEquals("/layout/schema/properties/x", lint.findings.get(0).path());
    Assertions.assertTrue(all(lint).contains("form property required is deprecated: Classic UI"), all(lint));
  }

  @Test
  public void newUiClientFunctions() throws Exception {
    Linter lint = new Linter();
    expressions(lint, "{\"options\": \"$rest('/rest/ai/ais')\"}", false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
    // client functions are not available in the backend
    expressions(lint, "{\"expression\": \"$rest('/rest/ai/ais')\"}", true);
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
  }

  @Test
  public void inputs() throws Exception {
    // from widgets.json: the default input of each type plus the form's "widget" values
    Assertions.assertTrue(Rules.INPUTS.containsAll(List.of("string", "number", "boolean", "select",
        "json", "voice", "qrcode", "binary file with metadata")), "" + Rules.INPUTS);
    Linter lint = new Linter();
    lint.form(PAGE, "", json("{\"widget\": \"json\"}"), false);
    lint.form(PAGE, "", json("{\"widget\": \"jsn\"}"), false);
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("unknown widget type: jsn"), all(lint));
  }

  @Test
  public void testTry() throws Exception {
    Linter lint = new Linter();
    expressions(lint, "{\"expression\": \"$try(function(){$echo('test')})\"}", false);
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void orphanedFiles(@TempDir Path model) throws Exception {
    Path q = Files.createDirectories(model.resolve("dj-query-catalog"));
    Path p = Files.createDirectories(model.resolve("page"));
    Files.writeString(q.resolve("list.json"), "{\"ID\": \"list\", \"query-pointer\": \"0.sql\"}");
    Files.writeString(q.resolve("list.0.sql"), "select 1");
    // pointer was removed / renamed
    Files.writeString(q.resolve("list.1.sql"), "select 2");
    // JSON was deleted
    Files.writeString(q.resolve("gone.0.sql"), "select 3");
    // nested pointer and encoded ID
    Files.writeString(p.resolve("a%2Fb.json"), "{\"ID\": \"a/b\", \"layout\": {\"widget\": "
        + "\"text\", \"markdown-pointer\": \"0.md\"}}");
    Files.writeString(p.resolve("a%2Fb.0.md"), "# hi");
    Files.writeString(p.resolve(".DS_Store"), "");

    Linter lint = new Linter();
    lint.modelDir = model.toFile();
    lint.finish();
    String all = all(lint);
    Assertions.assertEquals(2, lint.findings.size(), all);
    Assertions.assertTrue(all.contains("model/dj-query-catalog/gone.0.sql: warning: externalized"),
        all);
    Assertions.assertTrue(all.contains("model/dj-query-catalog/list.1.sql: warning: externalized"),
        all);

    // missing model folder
    lint = new Linter();
    lint.modelDir = new File(model.toFile(), "nope");
    lint.finish();
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  // rules from widgets.json

  @Test
  public void unknownWidgetAndProperty() throws Exception {
    Linter lint = new Linter();
    layout(lint, "{\"widget\": \"page\", \"children\": [{\"widget\": \"tabel\"}, "
        + "{\"widget\": \"text\", \"titel\": \"x\"}]}");
    Assertions.assertEquals(2, lint.findings.size(), all(lint));
    Finding widget = lint.findings.get(0);
    Assertions.assertEquals("unknown-widget", widget.rule());
    Assertions.assertEquals("/layout/children/0", widget.path());
    Finding prop = lint.findings.get(1);
    Assertions.assertEquals("unknown-property", prop.rule());
    Assertions.assertEquals(Severity.WARNING, prop.severity());
    Assertions.assertEquals(
        "model/page/test.json: warning: unknown property for widget text: titel (at /layout/children/1)",
        prop.text());
  }

  @Test
  public void newUiWidgets() throws Exception {
    Linter lint = new Linter();
    // widgets and properties of the new UI (unknown to the classic UI's property lists)
    layout(lint, "{\"widget\": \"page\", \"frame\": {\"toolbar\": false}, \"children\": ["
        + "{\"widget\": \"heading\", \"text\": \"Hi\"}, {\"widget\": \"container\", \"class\": "
        + "\"md:col-span-6\", \"foreach\": \"[1, 2]\", \"children\": [{\"widget\": \"text\"}]}, "
        + "{\"widget\": \"button\", \"icon\": \"add\", \"onClick\": \"1\"}]}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void deprecatedProperty() throws Exception {
    Linter lint = new Linter();
    layout(lint, "{\"widget\": \"button\", \"navigate\": \"'/page/x'\", \"createSchema\": {}}");
    layout(lint, "{\"widget\": \"aichat\", \"url\": \"x\"}");
    // deprecated without a message: reported with the description, common properties included
    layout(lint, "{\"widget\": \"text\", \"size\": 6}");
    layout(lint, "{\"widget\": \"table\", \"onchange\": \"1\"}");
    String all = all(lint);
    Assertions.assertEquals(5, lint.findings.size(), all);
    Assertions.assertTrue(all.contains("property size is deprecated for widget text: Legacy: width"), all);
    Assertions.assertTrue(all.contains("property onchange is deprecated for widget table: Legacy name of onChange"), all);
    Assertions.assertTrue(all.contains("property navigate is deprecated for widget button: use $navigate"), all);
    Assertions.assertTrue(all.contains("property createSchema is deprecated for widget button: use schema"), all);
    Assertions.assertTrue(all.contains("property url is deprecated for widget aichat: only supported by the legacy chat"), all);
  }

  @Test
  public void deprecatedWidget() throws Exception {
    Linter lint = new Linter();
    layout(lint, "{\"widget\": \"card\", \"text\": \"x\"}");
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertEquals("deprecated-widget", lint.findings.get(0).rule());
    Assertions.assertTrue(all(lint).contains("widget card is deprecated: Legacy: read as a container"), all(lint));
  }

  @Test
  public void appWidgets() throws Exception {
    Linter lint = new Linter();
    lint.define("widget", Map.of("ID", "my-kpi"));
    layout(lint, "{\"widget\": \"my-kpi\", \"metric\": \"sales\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void ignoreRules() throws Exception {
    Linter lint = new Linter().ignore(List.of("unknown-property"));
    layout(lint, "{\"widget\": \"text\", \"titel\": \"x\"}");
    Assertions.assertEquals(0, lint.findings.size(), all(lint));
  }

  @Test
  public void danglingPointer() throws Exception {
    Linter lint = new Linter();
    lint.record("page", map("{\"ID\": \"p\", \"layout\": {\"widget\": \"text\", "
        + "\"markdown-pointer\": \"0.md\"}}"));
    Assertions.assertEquals(1, lint.findings.size(), all(lint));
    Assertions.assertTrue(all(lint).contains("There must be a file called p.0.md"), all(lint));
    Assertions.assertEquals("/layout/markdown-pointer", lint.findings.get(0).path());
  }

  /**
   * the platform's built-in pages, widgets and table / record layouts (dj/config.json) match
   * widgets.json
   */
  @Test
  public void builtInModel() throws Exception {
    List<String> shape = List.of("missing-widget", "unknown-widget", "unknown-property",
        "deprecated-widget", "deprecated-property", "deprecated-form-property", "url-not-string", "form-not-object", "unknown-input",
        "options-not-select", "file-setting", "unknown-form-property");
    File model = new File("src/main/resources/model");
    Assertions.assertTrue(model.isDirectory(), model.getAbsolutePath());
    Linter lint = new Linter();
    File[] folders = model.listFiles(File::isDirectory);
    for (boolean walk : new boolean[] {false, true})
      for (File folder : folders)
        for (File f : folder.listFiles((dir, name) -> name.endsWith(".json")))
          if (walk)
            lint.record(folder.getName(), map(Files.readString(f.toPath())));
          else
            lint.define(folder.getName(), map(Files.readString(f.toPath())));
    // the knowledge base's default table / record layouts (PojoDatabase.template)
    for (File folder : new File("src/main/resources/template").listFiles(File::isDirectory))
      for (File f : folder.listFiles((dir, name) -> name.endsWith(".json"))) {
        String file = "template/" + folder.getName() + "/" + f.getName();
        lint.layout(file, "", json(Files.readString(f.toPath())));
        lint.expressions(file, "", json(Files.readString(f.toPath())), false);
      }
    // templates: no findings at all (e.g. an empty expression)
    List<Finding> found = lint.findings.stream().filter(f -> shape.contains(f.rule())
        || f.file().startsWith("template/") && f.severity() != Severity.HINT).toList();
    Assertions.assertEquals(List.of(), found, "" + found);
  }

  @Test
  public void json() throws Exception {
    Linter lint = new Linter();
    Assertions.assertEquals("no warnings", lint.text());
    layout(lint, "{\"widget\": \"tabel\"}");
    Assertions.assertEquals(List.of(Map.of("file", PAGE, "path", "/layout", "rule",
        "unknown-widget", "severity", "error", "message", "unknown widget: tabel")), lint.json());
  }
}
