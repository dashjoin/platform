package org.dashjoin.util;

import java.util.Arrays;
import org.dashjoin.service.SQLDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableMap;

public class TemplateTest {

  @Test
  public void getVariables() {
    Assertions.assertEquals(Arrays.asList(), Template.getVariables(null));
    Assertions.assertEquals(Arrays.asList(), Template.getVariables(""));
    Assertions.assertEquals(Arrays.asList(), Template.getVariables("efef"));
    Assertions.assertEquals(Arrays.asList("x"), Template.getVariables("${x}"));
    Assertions.assertEquals(Arrays.asList("x", "y"), Template.getVariables("${x} ${y}"));
    Assertions.assertEquals(Arrays.asList("x"), Template.getVariables("${x}${x}", true));
    Assertions.assertEquals(Arrays.asList("x", "x"), Template.getVariables("${x}${x}"));
  }

  @Test
  public void replace() {
    Assertions.assertEquals(null, Template.replace(null, ImmutableMap.of("x", 1)));
    Assertions.assertEquals("", Template.replace("", ImmutableMap.of("x", 1)));
    Assertions.assertEquals("efef", Template.replace("efef", ImmutableMap.of("x", 1)));
    Assertions.assertEquals(1, Template.replace("${x}", ImmutableMap.of("x", 1, "y", "yyy")));
    Assertions.assertEquals("1 yyy",
        Template.replace("${x} ${y}", ImmutableMap.of("x", 1, "y", "yyy")));
    Assertions.assertEquals("11", Template.replace("${x}${x}", ImmutableMap.of("x", 1)));

    // urlencode
    Assertions.assertEquals("http://ex.org/a+b",
        Template.replace("http://ex.org/${x}", ImmutableMap.of("x", "a b"), true));
  }

  @Test
  public void sql() {
    SQLDatabase db = new SQLDatabase();
    db.url = "jdbc:";
    Assertions.assertEquals("cast(x as VARCHAR(255))", Template.sql(db, "x", null));
    Assertions.assertEquals("cast(col as VARCHAR(255))", Template.sql(db, "x", "${col}"));
    Assertions.assertEquals("concat('Hi ', col, '')", Template.sql(db, "x", "Hi ${col}"));
    db.url = "jdbc:sqlite:";
    Assertions.assertEquals("cast(x as VARCHAR(255))", Template.sql(db, "x", null));
    Assertions.assertEquals("cast(col as VARCHAR(255))", Template.sql(db, "x", "${col}"));
    Assertions.assertEquals("'Hi ' || col || ''", Template.sql(db, "x", "Hi ${col}"));
    db.url = "jdbc:mariadb:";
    Assertions.assertEquals("cast(x as CHAR)", Template.sql(db, "x", null));
    Assertions.assertEquals("cast(col as CHAR)", Template.sql(db, "x", "${col}"));
    Assertions.assertEquals("concat('Hi ', COALESCE(col, 'null'), '')",
        Template.sql(db, "x", "Hi ${col}"));
  }
}
