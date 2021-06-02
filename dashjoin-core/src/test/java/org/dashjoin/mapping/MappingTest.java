package org.dashjoin.mapping;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.api.jsonata4java.expressions.Expressions;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MappingTest {

  @Inject
  ExpressionService s;

  Mapping newMapping() {
    Mapping m = new Mapping();
    return m;
  }

  @Test
  public void testConcat() throws Exception {
    Mapping m = newMapping();

    // rename y to x
    m.rowMapping = ImmutableMap.of("z", "z", "x", "y&z");

    Expressions rm = s.prepare(Mockito.mock(SecurityContext.class), m.rowMapping());

    Map<String, Object> res = Mapping.apply(s, null, rm, MapUtil.of("y", 1, "z", "a"));
    Assert.assertEquals("{z=a, x=1a}", res.toString());
  }
}
