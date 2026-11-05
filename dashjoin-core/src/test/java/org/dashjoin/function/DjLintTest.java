package org.dashjoin.function;

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
}
