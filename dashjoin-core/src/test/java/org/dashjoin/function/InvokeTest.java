package org.dashjoin.function;

import org.dashjoin.expression.ExpressionService;
import org.dashjoin.service.Services;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;

/**
 * test Invoke with and without runas (EMP is readable by admin only)
 */
@QuarkusTest
public class InvokeTest {

  @Inject
  Services services;

  @Inject
  ExpressionService expressionService;

  Invoke invoke(SecurityContext sc, String runas) {
    Invoke f = new Invoke();
    f.expression = "$read('junit', 'EMP', 1).NAME";
    f.runas = runas;
    f.init(sc, services, expressionService, false);
    return f;
  }

  @Test
  public void testNoRunas() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.eq("authenticated"))).thenReturn(true);
    Assertions.assertThrows(NotAuthorizedException.class, () -> invoke(sc, null).run(null));
    Assertions.assertThrows(NotAuthorizedException.class, () -> invoke(sc, " ").run(null));
  }

  @Test
  public void testRunasElevates() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.eq("authenticated"))).thenReturn(true);
    Assertions.assertEquals("mike", invoke(sc, "admin").run(null));
  }

  @Test
  public void testRunasReplacesRoles() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("mike", invoke(sc, null).run(null));
    Assertions.assertThrows(NotAuthorizedException.class,
        () -> invoke(sc, "authenticated").run(null));
  }
}
