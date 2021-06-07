package org.dashjoin.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.Email;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.ACLContainerRequestFilter.Operation;
import org.jboss.resteasy.core.interception.jaxrs.ResponseContainerRequestContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import com.sun.security.auth.UnixPrincipal;
import io.quarkus.test.junit.QuarkusTest;

/**
 * test the global ACL filter
 */
@QuarkusTest
public class ACLContainerRequestFilterTest {

  @Test
  public void testFunction() {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.contains("authenticated"))).thenReturn(true);
    AbstractConfigurableFunction<?, ?> function = new Email();
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      ACLContainerRequestFilter.check(sc, function);
    });
    function.roles = Arrays.asList("authenticated");
    ACLContainerRequestFilter.check(sc, function);
  }

  @Test
  public void testQueryMeta() {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.contains("authenticated"))).thenReturn(true);
    QueryMeta query = new QueryMeta();
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      ACLContainerRequestFilter.check(sc, query);
    });
    query.roles = Arrays.asList("authenticated");
    ACLContainerRequestFilter.check(sc, query);
  }

  @Test
  public void testDatabase() {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.contains("authenticated"))).thenReturn(true);
    SQLDatabase db = new SQLDatabase();
    Table table = Table.ofName("table");

    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      ACLContainerRequestFilter.check(sc, db, table);
    });

    db.readRoles = Arrays.asList("authenticated");
    ACLContainerRequestFilter.check(sc, db, table);
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      ACLContainerRequestFilter.check(sc, db, table, Operation.DELETE);
    });

    db.readRoles = Arrays.asList("authenticated");
    db.writeRoles = Arrays.asList("authenticated");
    ACLContainerRequestFilter.check(sc, db, table);
  }

  /**
   * make sure GET throws 401 unauthorized
   */
  @Test
  public void testFilter() throws Exception {
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      ACLContainerRequestFilter f = new ACLContainerRequestFilter();
      f.filter(new ResponseContainerRequestContext(MockHttpRequest.create("GET", "/")));
    });
  }


  /**
   * authenticated requests pass the filter
   */
  @Test
  public void testQuery() throws Exception {
    ACLContainerRequestFilter f = new ACLContainerRequestFilter();
    f.filter(new ResponseContainerRequestContext(null) {

      @Override
      public SecurityContext getSecurityContext() {
        return new SecurityContext() {

          @Override
          public Principal getUserPrincipal() {
            return new UnixPrincipal("root");
          }

          @Override
          public boolean isUserInRole(String role) {
            return true;
          }

          @Override
          public boolean isSecure() {
            return false;
          }

          @Override
          public String getAuthenticationScheme() {
            return null;
          }
        };
      }

      @Override
      public UriInfo getUriInfo() {
        try {
          return new ResteasyUriInfo(new URI("/database/query/list"));
        } catch (URISyntaxException e) {
          throw new RuntimeException();
        }
      }
    });
  }

}
