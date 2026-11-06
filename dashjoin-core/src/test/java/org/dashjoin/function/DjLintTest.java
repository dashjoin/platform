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
}
