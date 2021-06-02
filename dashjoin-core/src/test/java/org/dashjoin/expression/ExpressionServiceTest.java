package org.dashjoin.expression;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.Data;
import org.dashjoin.service.Services;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
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
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    Assert.assertEquals("{\"ID\":1,\"NAME\":\"mike\",\"WORKSON\":1000}",
        "" + s.jsonata(sc, "$read(\"junit\", \"EMP\", 1)", null, false));
  }

  @Test
  public void createUpdateDelete() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);

    Assert.assertEquals("\"dj/junit/EMP/8\"",
        "" + s.jsonata(sc, "$create(\"junit\", \"EMP\", {\"ID\": 8})", null, false));

    Assert.assertEquals(8,
        s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false).get("ID").asInt());

    s.jsonata(sc, "$update(\"junit\", \"EMP\", 8, {\"NAME\": \"jsonata\"})", null, false);

    Assert.assertEquals("jsonata",
        s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false).get("NAME").asText());

    s.jsonata(sc, "$delete(\"junit\", \"EMP\", 8)", null, false);

    try {
      s.jsonata(sc, "$read(\"junit\", \"EMP\", 8)", null, false);
      Assert.fail();
    } catch (RuntimeException wrapped404) {
      Assert.assertTrue(wrapped404.getCause() instanceof NotFoundException);
    }
  }

  @Test
  public void incoming() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    Assert.assertEquals(
        "{\"id\":\"dj/junit/EMP/2\",\"pk\":\"dj/junit/PRJ/ID\",\"fk\":\"dj/junit/EMP/WORKSON\"}",
        "" + s.jsonata(sc, "$incoming(\"junit\", \"PRJ\", 1000)", null, false).get(1));
  }

  @Test
  public void query() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    Assert.assertEquals(
        "[{\"EMP.ID\":1,\"EMP.NAME\":\"mike\"},{\"EMP.ID\":2,\"EMP.NAME\":\"joe\"}]",
        "" + s.jsonata(sc, "$query(\"junit\", \"list\")", null, false));
  }

  @Test
  public void call() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);

    // TODO: there should not be extra " around the result
    Assert.assertEquals("\"result\"", "" + s.jsonata(sc, "$echo(\"result\")", null, false));
  }

  @Test
  public void map() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    Assert.assertEquals("[\"mike\",\"joe\"]",
        "" + s.jsonata(sc, "$query(\"junit\", \"list\").$echo($.\"EMP.NAME\")", null, false));
  }
}
