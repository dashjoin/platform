package org.dashjoin.mapping;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.service.Services;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class InferenceTest {

  @Inject
  ExpressionService expressionService;

  @Inject
  Services services;

  SecurityContext sc = Mockito.mock(SecurityContext.class);

  @Test
  public void testGather() throws Exception {
    ETL i = new ETL();
    i.init(sc, services, expressionService, false);

    // empty
    i.expression = "";
    Assert.assertEquals("{table=[{column=null}]}", i.gather(sc).toString());

    // single value
    i.expression = "123";
    Assert.assertEquals("{table=[{column=123}]}", i.gather(sc).toString());

    // empty array
    i.expression = "[]";
    Assert.assertEquals("{table=[]}", i.gather(sc).toString());

    // array of single value
    i.expression = "[4, 5, 6]";
    Assert.assertEquals("{table=[{column=4}, {column=5}, {column=6}]}", i.gather(sc).toString());

    // single object
    i.expression = "{\"x\":1}";
    Assert.assertEquals("{table=[{x=1}]}", i.gather(sc).toString());

    // single table
    i.expression = "[{\"column\":4}, {\"column\":5}, {\"column\":6}]";
    Assert.assertEquals("{table=[{column=4}, {column=5}, {column=6}]}", i.gather(sc).toString());

    // map of tables
    i.expression = "{\"a\":[{\"x\":1}]}";
    Assert.assertEquals("{a=[{x=1}]}", i.gather(sc).toString());

    // mixed is treated like a normal object
    i.expression = "{\"a\":[{\"x\":1}], \"b\":[1,2,3]}";
    Assert.assertEquals("{table=[{a=[{x=1}], b=[1, 2, 3]}]}", i.gather(sc).toString());
  }
}
