package org.dashjoin.service;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.dashjoin.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.tenant.TenantManager;

/**
 * REST Filter that enforces query catalog level RBAC
 */
@PreMatching
@Provider
public class ACLContainerRequestFilter implements ContainerRequestFilter {

  @Inject
  TenantManager tenantManager;

  // public ACLContainerRequestFilter() {}

  // @Inject
  // ACLContainerRequestFilter(TenantManager tenantManager) {
  // this.tenantManager = tenantManager;
  // }

  /**
   * API global filter making sure the user is authenticated
   */
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String path = requestContext.getUriInfo().getPath();
    if ("/openapi.json".equals(path))
      return;

    if (path.startsWith("/_ah/") || path.startsWith("/rest/info/"))
      return;

    SecurityContext sc = requestContext.getSecurityContext();
    if (sc == null || sc.getUserPrincipal() == null)
      throwNotAuthorizedException("Invalid username or password");
  }

  /**
   * checks the security context for the function catalog
   */
  public static void check(SecurityContext sc, Function<?, ?> function) {
    if (sc.isUserInRole("admin"))
      return;
    if (function.getRoles() != null)
      for (String role : function.getRoles()) {
        if (sc.isUserInRole(role))
          return;
      }
    throwNotAuthorizedException(
        "User does not have the role required to run function: " + function.getID());
  }

  /**
   * checks the security context for the query catalog
   * 
   * @param sc security context
   * @param info query info
   */
  static void check(SecurityContext sc, QueryMeta info) {
    if (sc.isUserInRole("admin"))
      return;
    if (info.roles != null)
      for (String role : info.roles) {
        if (sc.isUserInRole(role))
          return;
      }
    throwNotAuthorizedException("User does not have the role required to run query: " + info.ID);
  }

  static enum Operation {
    CREATE, READ, UPDATE, DELETE
  }

  /**
   * checks whether read is allowed on db and table
   */
  public static void check(SecurityContext sc, Database db, Table m) {
    check(sc, db, m, Operation.READ);
  }

  /**
   * checks whether create, read, delete, update is allowed on db and table
   */
  static void check(SecurityContext sc, Database db, Table m, Operation operation) {
    if (sc.isUserInRole("admin"))
      return;
    if (operation.equals(Operation.READ)) {
      List<String> readRoles =
          m != null && m.readRoles != null ? m.readRoles : ((AbstractDatabase) db).readRoles;
      if (readRoles != null)
        for (String role : readRoles) {
          if (sc.isUserInRole(role))
            return;
        }
      throwNotAuthorizedException("User does not have the role required to "
          + operation.toString().toLowerCase() + " table " + (m == null ? "" : m.name)
          + " in database " + ((AbstractDatabase) db).name);
    } else {
      List<String> writeRoles =
          m != null && m.writeRoles != null ? m.writeRoles : ((AbstractDatabase) db).writeRoles;
      if (writeRoles != null)
        for (String role : writeRoles) {
          if (sc.isUserInRole(role))
            return;
        }
      throwNotAuthorizedException("User does not have the role required to "
          + operation.toString().toLowerCase() + " table " + (m == null ? "" : m.name)
          + " in database " + ((AbstractDatabase) db).name);
    }
  }

  static void throwNotAuthorizedException(String msg) {
    throw new NotAuthorizedException(
        Response.status(Response.Status.UNAUTHORIZED).entity(msg).build());
  }
}
