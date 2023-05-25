package org.dashjoin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.dashjoin.function.EveryoneFunction;
import org.dashjoin.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.tenant.TenantManager;
import org.dashjoin.util.MapUtil;

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

    if (path.startsWith("/_ah/") || path.startsWith("/rest/info/")
        || path.equals("/rest/manage/openapi"))
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
    if (function instanceof EveryoneFunction)
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
    CREATE, READ, UPDATE, DELETE, UPDATE_ROW, DELETE_ROW
  }

  /**
   * like check(sc, db) but also disallow if there's row level restriction
   */
  public static void allowQueryEditor(SecurityContext sc, AbstractDatabase db) {
    check(sc, db);
    for (Table m : db.tables.values())
      if (hasTenantFilter(sc, m))
        throwNotAuthorizedException("User does not have the role required to query table "
            + (m.name) + " in database " + db.name);
  }

  /**
   * checks whether read is allowed on db and all of its tables
   */
  static void check(SecurityContext sc, AbstractDatabase db) {
    check(sc, db, null, Operation.READ);
  }

  /**
   * checks whether read is allowed on db and table
   */
  public static void check(SecurityContext sc, AbstractDatabase db, Table m) {
    check(sc, db, m, Operation.READ);
  }

  /**
   * checks whether create, read, delete, update is allowed on db and table. If table is null, we
   * check all tables
   */
  static void check(SecurityContext sc, AbstractDatabase db, Table m, Operation operation) {
    if (sc.isUserInRole("admin"))
      return;

    if (m == null) {
      for (Table t : db.tables.values())
        check(sc, db, t, operation);
      return;
    }

    if (operation.equals(Operation.READ)) {

      if (hasTenantFilter(sc, m))
        tenantValue(sc, m);

      List<String> readRoles = m.readRoles != null ? m.readRoles : db.readRoles;
      if (readRoles != null)
        for (String role : readRoles) {
          if (sc.isUserInRole(role))
            return;
        }
      throwNotAuthorizedException("User does not have the role required to "
          + operation.toString().toLowerCase() + " table " + (m.name) + " in database " + db.name);
    } else {

      if (operation.equals(Operation.UPDATE) || operation.equals(Operation.DELETE))
        if (hasTenantFilter(sc, m))
          throwNotAuthorizedException(
              "User does not have the role required to " + operation.toString().toLowerCase()
                  + " table " + (m.name) + " in database " + db.name);

      List<String> writeRoles = m.writeRoles != null ? m.writeRoles : db.writeRoles;
      if (writeRoles != null)
        for (String role : writeRoles) {
          if (sc.isUserInRole(role))
            return;
        }
      throwNotAuthorizedException("User does not have the role required to "
          + operation.toString().toLowerCase() + " table " + (m.name) + " in database " + db.name);
    }
  }

  public static void throwNotAuthorizedException(String msg) {
    throw new NotAuthorizedException(
        Response.status(Response.Status.UNAUTHORIZED).entity(msg).build());
  }

  /**
   * is a tenant row level security filter defined on this table / user
   */
  static boolean hasTenantFilter(SecurityContext sc, Table table) {
    if (table.tenantColumn == null)
      return false;
    if (sc.isUserInRole("admin"))
      return false;
    return true;
  }

  /**
   * checks the row level acl
   */
  static void checkRow(SecurityContext sc, Table table, Map<String, Object> row) {
    if (hasTenantFilter(sc, table))
      if (!tenantValue(sc, table).equals(row.get(table.tenantColumn)))
        ACLContainerRequestFilter
            .throwNotAuthorizedException("User does not have access to this record");
  }

  /**
   * row level security: given the table, get the value role mapping value / username to compare to
   */
  static Object tenantValue(SecurityContext sc, Table table) {
    Object v = null;
    if (table.roleMappings != null && table.roleMappings.size() > 0) {
      for (Entry<String, String> r : table.roleMappings.entrySet()) {
        if (sc.isUserInRole(r.getKey())) {
          v = r.getValue();
          break;
        }
      }
    } else {
      v = sc.getUserPrincipal().getName();
    }

    if (v == null)
      ACLContainerRequestFilter.throwNotAuthorizedException(
          "User role does not map to a tenant filter on table " + table.name);
    return v;
  }

  /**
   * adds the tenant filter condition to arguments
   */
  public static Map<String, Object> tenantFilter(SecurityContext sc, Table table,
      Map<String, Object> arguments) {
    if (!hasTenantFilter(sc, table))
      return arguments;
    if (arguments != null && arguments.containsKey(table.tenantColumn))
      throw new RuntimeException("Row level ACL is defined on " + table.tenantColumn
          + ". Cannot define an additional filter on this column.");

    if (arguments == null)
      arguments = MapUtil.of(table.tenantColumn, tenantValue(sc, table));
    else
      arguments.put(table.tenantColumn, tenantValue(sc, table));

    return arguments;
  }
}
