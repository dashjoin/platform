package org.dashjoin.expression;

import static org.dashjoin.util.MapUtil.of;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.service.Data;
import org.dashjoin.service.Data.Origin;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Services;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import com.dashjoin.jsonata.JException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

@QuarkusTest
public class ExpressionServiceTest {
  @Inject
  Services services;

  @Inject
  Data data;

  @Inject
  ExpressionService s;

  private static final ObjectMapper om =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  @Test
  public void testJsonataTypes() throws Exception {

    // make sure om.convertValue() does not change legal types
    Object o;
    o = "string";
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = 1;
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = Arrays.asList(1, 2, 3);
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = of("x", Arrays.asList(1, 2, 3));
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = true;
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = (short) 12;
    Assertions.assertEquals(o, om.convertValue(o, Object.class));
    o = null;
    Assertions.assertEquals(o, om.convertValue(o, Object.class));

    // date / uuid etc., om.convertValue() performs a toString() / ISO 8601 for date / time
    o = UUID.randomUUID();
    Assertions.assertEquals(o + "", om.convertValue(o, Object.class));
    o = new java.util.Date();
    om.convertValue(o, Object.class);
    o = UUID.randomUUID();
    Assertions.assertEquals(of("x", o + ""), om.convertValue(of("x", o), Object.class));

    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    for (int i = 0; i < 7; i++)
      s.resolve(sc, "$sqlDate(x) & '  '", of("x", i));
  }

  public static class SqlDate extends AbstractFunction<Integer, Object> {

    @Override
    public Object run(Integer arg) throws Exception {
      if (arg == 0)
        return new Date(0);
      if (arg == 1)
        return Calendar.getInstance();
      if (arg == 2)
        return UUID.randomUUID();
      if (arg == 2)
        return LocalDate.of(2020, 1, 8);
      if (arg == 3)
        return null;
      // requires additional jackson lib
      // return LocalDateTime.of(2014, Month.JANUARY, 1, 10, 10, 30);
      if (arg == 3)
        return Instant.now();
      if (arg == 4)
        return Arrays.asList(run(0));
      if (arg == 5)
        return of("x", run(0));
      return null;
    }

    @Override
    public Class<Integer> getArgumentClass() {
      return Integer.class;
    }
  }

  @Test
  public void read() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    org.junit.jupiter.api.Assertions.assertEquals("{ID=1, NAME=mike, WORKSON=1000}",
        "" + s.resolve(sc, "$read(\"junit\", \"EMP\", 1)", null, false));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createUpdateDelete() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Resource res = new ObjectMapper().convertValue(
        s.resolve(sc, "$create(\"junit\", \"EMP\", {\"ID\": 8})", null, false), Resource.class);
    Assertions.assertEquals("junit", res.database);
    Assertions.assertEquals("EMP", res.table);
    Assertions.assertEquals(8, res.pk.get(0));

    Assertions.assertEquals(8,
        ((Map<String, Object>) s.resolve(sc, "$read(\"junit\", \"EMP\", 8)", null, false))
            .get("ID"));

    s.resolve(sc, "$update(\"junit\", \"EMP\", 8, {\"NAME\": \"jsonata\"})", null, false);

    Assertions.assertEquals("jsonata",
        ((Map<String, Object>) s.resolve(sc, "$read(\"junit\", \"EMP\", 8)", null, false))
            .get("NAME"));

    s.resolve(sc, "$delete(\"junit\", \"EMP\", 8)", null, false);

    try {
      s.resolve(sc, "$read(\"junit\", \"EMP\", 8)", null, false);
      Assertions.fail();
    } catch (RuntimeException wrapped404) {
      System.out.println(wrapped404.toString());

      // Note: exception loses cause from Javascript to Java world...
      Assertions.assertTrue(wrapped404.toString().indexOf("NotFoundException") > 0);
    }
  }

  @Test
  public void incoming() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Origin> res = new ObjectMapper().convertValue(
        s.resolve(sc, "$incoming(\"junit\", \"PRJ\", 1000)", null, false),
        new TypeReference<List<Origin>>() {});
    Assertions.assertEquals("junit", res.get(1).id.database);
    Assertions.assertEquals("EMP", res.get(1).id.table);
    Assertions.assertEquals(2, res.get(1).id.pk.get(0));
    Assertions.assertEquals("dj/junit/PRJ/ID", res.get(1).pk);
    Assertions.assertEquals("dj/junit/EMP/WORKSON", res.get(1).fk);
  }

  @Test
  public void query() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{EMP.ID=1, EMP.NAME=mike}, {EMP.ID=2, EMP.NAME=joe}]",
        "" + s.resolve(sc, "$query(\"junit\", \"list\")", null, false));
  }

  @Test
  public void adHocQuery() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{EMP.ID=1, EMP.NAME=mike}, {EMP.ID=2, EMP.NAME=joe}]",
        "" + s.resolve(sc, "$adHocQuery(\"junit\", \"select ID,NAME from EMP\")", null, false));
  }

  @Test
  public void all() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{ID=1, NAME=mike, WORKSON=1000}, {ID=2, NAME=joe, WORKSON=1000}]",
        "" + s.resolve(sc, "$all(\"junit\", \"EMP\")", null, false));
    Assertions.assertEquals("[{ID=1, NAME=mike, WORKSON=1000}]",
        "" + s.resolve(sc, "$all(\"junit\", \"EMP\", 0 ,1)", null, false));
    Assertions.assertEquals("[{ID=1, NAME=mike, WORKSON=1000}]",
        "" + s.resolve(sc, "$all(\"junit\", \"EMP\", 1 , 2, \"ID\", true)", null, false));
    Assertions.assertEquals("[{ID=1, NAME=mike, WORKSON=1000}]", "" + s.resolve(sc,
        "$all(\"junit\", \"EMP\", null , null, null, null, {\"ID\":1})", null, false));
  }

  @Test
  public void testError() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    JException ex =
        Assertions.assertThrows(JException.class, () -> s.resolve(sc, "1+", null, false));
    Assertions.assertEquals("Unexpected end of expression", ex.getMessage());
    Exception e =
        Assertions.assertThrows(Exception.class, () -> s.resolve(sc, "$read()", null, false));
    Assertions.assertEquals("Arguments required: $read(database, table, pk1)", e.getMessage());
  }

  @Test
  public void echo() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Assertions.assertEquals("result", s.resolve(sc, "$echo(\"result\")", null, false));
  }

  @Test
  public void call() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    // see echo.json
    Assertions.assertEquals(123, s.resolve(sc, "$call(\"echo\")", null, false));
  }

  @Test
  public void traverse() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    Assertions.assertEquals("{ID=1000, NAME=dev-project, BUDGET=null}",
        s.resolve(sc, "$traverse(\"junit\", \"EMP\", 1, \"WORKSON\")", null, false).toString());

    Assertions.assertEquals("[{ID=1, NAME=mike, WORKSON=1000}, {ID=2, NAME=joe, WORKSON=1000}]",
        s.resolve(sc, "$traverse(\"junit\", \"PRJ\", 1000, \"dj/junit/EMP/WORKSON\")", null, false)
            .toString());
  }

  @Test
  public void map() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[mike, joe]",
        "" + s.resolve(sc, "$query(\"junit\", \"list\").$echo($.\"EMP.NAME\")", null, false));
  }

  @Test
  public void vararg() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(0, (s.resolve(sc, "$add()", null, false)));
    Assertions.assertEquals(1, (s.resolve(sc, "$add(1)", null, false)));
    Assertions.assertEquals(2, (s.resolve(sc, "$add(undefined, 2)", null, false)));
    Assertions.assertEquals(2, (s.resolve(sc, "$add(null, 2)", null, false)));
    Assertions.assertEquals(3, (s.resolve(sc, "$add(1,2)", null, false)));
    Assertions.assertEquals(3, (s.resolve(sc, "$add(1,2,3)", null, false)));
  }

  @Test
  public void testPojoArg() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    s.resolve(sc, "$coord({\"x\":1})", null, false);
    s.resolve(sc, "$coord()", null, false);
    s.resolve(sc, "$coord(null)", null, false);
    Assertions.assertEquals(1, s.resolve(sc, "$coord({\"x\":1}).x", null, false));
    Assertions.assertEquals(1, s.resolve(sc, "$coords([{\"x\":1}, {\"x\":1}])[0].x", null, false));
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

  @SuppressWarnings("rawtypes")
  public static class EchoCoords extends AbstractFunction<List, List> {

    @Override
    public List run(List arg) throws Exception {
      return arg;
    }

    @Override
    public String getID() {
      return "coords";
    }

    @Override
    public Class<List> getArgumentClass() {
      return List.class;
    }
  }
}
