package org.dashjoin.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DjLintTest {

  @Test
  public void emptyForeach() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/dj-function/etl.json";
    lint.checkExpressions(new ObjectMapper()
        .readTree("{\"type\": \"ETL\", \"expression\": \"1\", \"foreach\": \"\"}"), true);
    Assertions.assertEquals(1, lint.messages.size());
    Assertions.assertTrue(lint.messages.get(0).contains("Empty expression foreach"),
        lint.messages.get(0));
  }

  @Test
  public void validForeach() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/dj-function/etl.json";
    lint.checkExpressions(new ObjectMapper()
        .readTree("{\"type\": \"ETL\", \"expression\": \"1\", \"foreach\": \"[1..3]\"}"), true);
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);
  }

  @Test
  public void layoutQuery() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    lint.queryTypes.put("list", "read");
    lint.queryTypes.put("insert", "write");
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("database", "query")));
    ObjectMapper om = new ObjectMapper();

    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"list\"}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"nope\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"insert\"}"));
    String all = "" + lint.messages;
    Assertions.assertEquals(2, lint.messages.size(), all);
    Assertions.assertTrue(all.contains("unknown query in widget table: nope"), all);
    Assertions.assertTrue(all.contains("write query must not be used in widget table"), all);
  }

  @Test
  public void queryArguments() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    lint.queryTypes.put("list", "read");
    lint.queryTypes.put("all", "read");
    lint.queriesWithArguments.add("list");
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("query", "arguments")));
    ObjectMapper om = new ObjectMapper();

    lint.checkLayout(om.readTree(
        "{\"widget\": \"table\", \"query\": \"list\", \"arguments\": \"{'limit': 5}\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"all\"}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"list\"}"));
    lint.checkLayout(
        om.readTree("{\"widget\": \"table\", \"query\": \"list\", \"arguments\": \"\"}"));
    Assertions.assertEquals(2, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("no arguments set for query: list"),
        lint.messages.get(0));
  }

  @Test
  public void selectWithoutOptions() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    ObjectMapper om = new ObjectMapper();

    lint.checkSchema(om.readTree("{\"widget\": \"select\", \"options\": \"['a', 'b']\"}"));
    lint.checkSchema(om.readTree("{\"widget\": \"string\"}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkSchema(om.readTree("{\"widget\": \"multi select\"}"));
    Assertions.assertEquals(1, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("no options set for input: multi select"),
        lint.messages.get(0));
  }

  @Test
  public void queryAndExpression() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    lint.queryTypes.put("list", "read");
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("query", "expression")));
    ObjectMapper om = new ObjectMapper();

    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"query\": \"list\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"expression\": \"1\"}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkLayout(
        om.readTree("{\"widget\": \"table\", \"query\": \"list\", \"expression\": \"1\"}"));
    Assertions.assertEquals(1, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("query is ignored because expression"),
        lint.messages.get(0));
  }

  @Test
  public void layoutDatabase() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    lint.databases = new HashSet<>(Arrays.asList("config", "northwind"));
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("database", "query"), "analytics",
        Arrays.asList("database")));
    ObjectMapper om = new ObjectMapper();

    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"database\": \"northwind\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"database\": \"dj/northwind\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"analytics\", \"database\": \"config\"}"));
    lint.checkLayout(om.readTree("{\"widget\": \"table\", \"database\": \"${db}\"}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkLayout(om.readTree("{\"widget\": \"analytics\", \"database\": \"nope\"}"));
    Assertions.assertEquals(1, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("unknown database: nope"),
        lint.messages.get(0));
  }

  DjLint referenceLint() {
    DjLint lint = new DjLint();
    lint.current = "model/page/test.json";
    lint.queries = new HashSet<>(Arrays.asList("list"));
    lint.databases = new HashSet<>(Arrays.asList("config", "northwind"));
    lint.functions = new HashSet<>(Arrays.asList("sendMail"));
    lint.libraries = new HashSet<>(Arrays.asList("unit-test"));
    return lint;
  }

  @Test
  public void validReferences() throws Exception {
    DjLint lint = referenceLint();
    lint.checkExpressions(new ObjectMapper().readTree("{\"expression\": "
        + "\"x ? $call('sendMail') : [$query('northwind', 'list'), $all('dj/northwind', 'T'), "
        + "$eval($use('unit-test')), $call(fn), function(){$read('config', 'page', 'p')}]\"}"),
        false);
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);
  }

  @Test
  public void builtInQueries() throws Exception {
    DjLint lint = referenceLint();
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("database", "query")));
    lint.checkLayout(new ObjectMapper()
        .readTree("{\"widget\": \"table\", \"database\": \"config\", \"query\": \"dj-navigation\"}"));
    lint.checkExpressions(new ObjectMapper().readTree(
        "{\"expression\": \"[$query('config', 'dj-functions'), $query('config', 'dj-query-performance')]\"}"),
        false);
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);
    // built-in queries are not hinted as unused
    lint.checkUnused();
    for (String q : DjLint.BUILT_IN_QUERIES)
      Assertions.assertFalse(("" + lint.messages).contains("/" + q + ".json"), "" + lint.messages);
  }

  @Test
  public void missingCopilot() throws Exception {
    DjLint lint = referenceLint();
    // dj-copilot is optional and called from built-in pages
    lint.checkExpressions(
        new ObjectMapper().readTree("{\"expression\": \"$call('dj-copilot', {})\"}"), false);
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);
  }

  @Test
  public void danglingReferences() throws Exception {
    DjLint lint = referenceLint();
    // unknown references in branches and lambdas that are never evaluated
    lint.checkExpressions(new ObjectMapper().readTree("{\"expression\": "
        + "\"x ? $call('nope') : [$query('northwind', 'nope'), $all('nope', 'T'), "
        + "function(){$use('nope')}]\"}"), false);
    String all = "" + lint.messages;
    Assertions.assertEquals(4, lint.messages.size(), all);
    Assertions.assertTrue(all.contains("unknown function in $call: nope"), all);
    Assertions.assertTrue(all.contains("unknown query in $query: nope"), all);
    Assertions.assertTrue(all.contains("unknown database: nope"), all);
    Assertions.assertTrue(all.contains("unknown library in $use: nope"), all);
  }

  @Test
  public void unused() throws Exception {
    DjLint lint = referenceLint();
    lint.queries = new HashSet<>(Arrays.asList("list", "orphanQuery"));
    lint.queryTypes.put("list", "read");
    lint.functions = new HashSet<>(Arrays.asList("sendMail", "orphanFunction", "etl"));
    // etl is not a $call type and must not be reported
    lint.calledFunctions = new HashSet<>(Arrays.asList("sendMail", "orphanFunction"));
    lint.libraries = new HashSet<>(Arrays.asList("unit-test", "lib", "orphanLibrary"));
    lint.widgets = new HashMap<>(Map.of("table", Arrays.asList("query")));

    lint.checkLayout(new ObjectMapper().readTree("{\"widget\": \"table\", \"query\": \"list\"}"));
    lint.checkExpressions(new ObjectMapper().readTree(
        "{\"expression\": \"x ? $call('sendMail') : $eval($use('lib'))\"}"), false);
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    lint.checkUnused();
    String all = "" + lint.messages;
    Assertions.assertEquals(3, lint.messages.size(), all);
    Assertions.assertTrue(all.contains("dj-query-catalog/orphanQuery.json: hint: query"), all);
    Assertions.assertTrue(all.contains("dj-function/orphanFunction.json: hint: function"), all);
    Assertions.assertTrue(all.contains("dj-library/orphanLibrary.json: hint: library"), all);
  }

  @Test
  public void roles() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/dj-database/dj%2Fnorthwind.json";
    lint.roles = new HashSet<>(Arrays.asList("admin", "authenticated"));
    // icons.roles is a string, not a role list, and must be ignored
    lint.checkRoles(new ObjectMapper().readTree("{\"roles\": [\"admin\"], \"icons\": "
        + "{\"roles\": \"person_search\"}, \"tables\": {\"T\": {\"readRoles\": [\"admni\"], "
        + "\"writeRoles\": [\"authenticated\"]}}}"));
    Assertions.assertEquals(1, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("unknown role in readRoles: admni"),
        lint.messages.get(0));
  }

  @Test
  public void editSchema() throws Exception {
    DjLint lint = new DjLint();
    lint.current = "model/dj-database/dj%2Fresult.json";
    // editRedirect is a studio property (lint/studio.json)
    lint.widgets = new HashMap<>(
        Map.of("edit", Arrays.asList("title", "text", "noDelete"), "button", Arrays.asList()));
    ObjectMapper om = new ObjectMapper();

    // edit / create schemas are table schemas and carry the column metadata
    lint.checkLayout(om.readTree("{\"widget\": \"page\", \"children\": [{\"title\": \"Edit Record\","
        + "\"widget\": \"edit\", \"editRedirect\": \"record\", \"schema\": {\"type\": \"object\","
        + "\"properties\": {\"ID\": {\"ID\": \"dj/result/Ergebnis/ID\", \"parent\": "
        + "\"dj/result/Ergebnis\", \"name\": \"ID\", \"readOnly\": true, \"dbType\": \"serial\","
        + "\"type\": \"number\", \"pkpos\": 0, \"createOnly\": true}}}}]}"));
    Assertions.assertEquals(0, lint.messages.size(), "" + lint.messages);

    // button schemas are written by hand and must not contain column metadata
    lint.checkLayout(om.readTree("{\"widget\": \"button\", \"schema\": {\"type\": \"object\","
        + "\"properties\": {\"x\": {\"type\": \"string\", \"dbType\": \"serial\"}}}}"));
    Assertions.assertEquals(1, lint.messages.size(), "" + lint.messages);
    Assertions.assertTrue(lint.messages.get(0).contains("unknown property"), lint.messages.get(0));
  }
}
