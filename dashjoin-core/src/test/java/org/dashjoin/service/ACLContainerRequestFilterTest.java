package org.dashjoin.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.core.interception.jaxrs.ResponseContainerRequestContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.sun.security.auth.UnixPrincipal;
import io.quarkus.test.junit.QuarkusTest;

/**
 * test the global ACL filter
 */
@QuarkusTest
public class ACLContainerRequestFilterTest {

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
