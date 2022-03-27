package org.dashjoin.expression;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.service.Data;
import org.dashjoin.service.Services;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ExpressionServiceTest {
  @Inject
  Services services;

  @Inject
  Data data;

  @Inject
  ExpressionService s;

  @Test
  public void read() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    org.junit.jupiter.api.Assertions.assertEquals("{\"ID\":1,\"NAME\":\"mike\",\"WORKSON\":1000}",
        "" + s.jsonata(sc, "$read(\"junit\", \"EMP\", 1)", null, false));
  }

  @Test
  public void createUpdateDelete() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Assertions.assertEquals("{\"database\":\"junit\",\"table\":\"EMP\",\"pk\":[8]}",
        "" + s.jsonata(sc, "$create(\"junit\", \"EMP\", {\"ID\": 8})", null, false));

    Assertions.assertEquals(8,
        s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false).get("ID").asInt());

    s.jsonata(sc, "$update(\"junit\", \"EMP\", 8, {\"NAME\": \"jsonata\"})", null, false);

    Assertions.assertEquals("jsonata",
        s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false).get("NAME").asText());

    s.jsonata(sc, "$delete(\"junit\", \"EMP\", 8)", null, false);

    try {
      s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false);
      Assertions.fail();
    } catch (RuntimeException wrapped404) {
      // Note: exception loses cause from Javascript to Java world...
      Assertions.assertTrue(wrapped404.toString().indexOf("NotFoundException") > 0);
    }
  }

  @Test
  public void incoming() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "{\"id\":{\"database\":\"junit\",\"table\":\"EMP\",\"pk\":[2]},\"pk\":\"dj/junit/PRJ/ID\",\"fk\":\"dj/junit/EMP/WORKSON\"}",
        "" + s.jsonata(sc, "$incoming(\"junit\", \"PRJ\", 1000)", null, false).get(1));
  }

  @Test
  public void query() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "[{\"EMP.ID\":1,\"EMP.NAME\":\"mike\"},{\"EMP.ID\":2,\"EMP.NAME\":\"joe\"}]",
        "" + s.jsonata(sc, "$query(\"junit\", \"list\")", null, false));
  }

  @Test
  public void echo() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Assertions.assertEquals("result", s.jsonata(sc, "$echo(\"result\")", null, false).asText());
  }

  @Test
  public void call() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    // see echo.json
    Assertions.assertEquals(123, s.jsonata(sc, "$call(\"echo\")", null, false).asInt());
  }

  @Test
  public void traverse() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Assertions.assertEquals("{\"ID\":1000,\"NAME\":\"dev-project\"}",
        s.jsonata(sc, "$traverse(\"junit\", \"EMP\", 1, \"WORKSON\")", null, false).toString());

    Assertions.assertEquals(
        "[{\"ID\":1,\"NAME\":\"mike\",\"WORKSON\":1000},{\"ID\":2,\"NAME\":\"joe\",\"WORKSON\":1000}]",
        s.jsonata(sc, "$traverse(\"junit\", \"PRJ\", 1000, \"dj/junit/EMP/WORKSON\")", null, false)
            .toString());
  }

  @Test
  public void map() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[\"mike\",\"joe\"]",
        "" + s.jsonata(sc, "$query(\"junit\", \"list\").$echo($.\"EMP.NAME\")", null, false));
  }

  @Test
  public void vararg() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(0, ExpressionService.j2o(s.jsonata(sc, "$add()", null, false)));
    Assertions.assertEquals(1, ExpressionService.j2o(s.jsonata(sc, "$add(1)", null, false)));
    Assertions.assertEquals(2, ExpressionService.j2o(s.jsonata(sc, "$add(null, 2)", null, false)));
    Assertions.assertEquals(3, ExpressionService.j2o(s.jsonata(sc, "$add(1,2)", null, false)));
    Assertions.assertEquals(3, ExpressionService.j2o(s.jsonata(sc, "$add(1,2,3)", null, false)));
  }

  @Test
  public void testPojoArg() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    s.jsonata(sc, "$coord({\"x\":1})", null, false);
  }

  public static class Coord {
    public int x;
    public int y;
  }

  public static class EchoCoord extends AbstractFunction<Coord, Coord> {

    @Override
    public Coord run(Coord arg) throws Exception {
      return arg;
    }

    @Override
    public Class<Coord> getArgumentClass() {
      return Coord.class;
    }

    @Override
    public String getID() {
      return "coord";
    }

    @Override
    public String getType() {
      return "read";
    }
  }
}
