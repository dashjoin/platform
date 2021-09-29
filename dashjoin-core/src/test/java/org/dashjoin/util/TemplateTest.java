package org.dashjoin.util;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableMap;

public class TemplateTest {

  @Test
  public void getVariables() {
    Assert.assertEquals(Arrays.asList(), Template.getVariables(null));
    Assert.assertEquals(Arrays.asList(), Template.getVariables(""));
    Assert.assertEquals(Arrays.asList(), Template.getVariables("efef"));
    Assert.assertEquals(Arrays.asList("x"), Template.getVariables("${x}"));
    Assert.assertEquals(Arrays.asList("x", "y"), Template.getVariables("${x} ${y}"));
    Assert.assertEquals(Arrays.asList("x"), Template.getVariables("${x}${x}", true));
    Assert.assertEquals(Arrays.asList("x", "x"), Template.getVariables("${x}${x}"));
  }

  @Test
  public void replace() {
    Assert.assertEquals(null, Template.replace(null, ImmutableMap.of("x", 1)));
    Assert.assertEquals("", Template.replace("", ImmutableMap.of("x", 1)));
    Assert.assertEquals("efef", Template.replace("efef", ImmutableMap.of("x", 1)));
    Assert.assertEquals(1, Template.replace("${x}", ImmutableMap.of("x", 1, "y", "yyy")));
    Assert.assertEquals("1 yyy",
        Template.replace("${x} ${y}", ImmutableMap.of("x", 1, "y", "yyy")));
    Assert.assertEquals("11", Template.replace("${x}${x}", ImmutableMap.of("x", 1)));

    // urlencode
    Assert.assertEquals("http://ex.org/a+b",
        Template.replace("http://ex.org/${x}", ImmutableMap.of("x", "a b"), true));
  }

  @Test
  public void sql() {
    Assert.assertEquals("cast(x as VARCHAR(255))", Template.sql("x", null));
    Assert.assertEquals("cast(col as VARCHAR(255))", Template.sql("x", "${col}"));
    Assert.assertEquals("concat('Hi ', col, '')", Template.sql("x", "Hi ${col}"));
  }
}
