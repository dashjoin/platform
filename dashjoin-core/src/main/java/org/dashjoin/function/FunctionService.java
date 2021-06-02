package org.dashjoin.function;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.ACLContainerRequestFilter;
import org.dashjoin.service.Data;
import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * REST API for function calls
 */
@Path(Services.REST_PREFIX + "function")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class FunctionService {

  private static ObjectMapper om =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  @Inject
  Services services;

  @Inject
  Data data;

  @POST
  @Path("/{function}")
  @Operation(summary = "runs a predefined function with the provided argument")
  @APIResponse(description = "Returns the function result")
  public Object call(@Context SecurityContext sc,
      @Parameter(description = "name of the function to run",
          example = "echo") @PathParam("function") String function,
      Object argument) throws Exception {
    Object res = callInternal(sc, function, argument, false);
    if (res instanceof String)
      return om.writeValueAsString(res);
    else
      return res;
  }

  public Object callInternal(SecurityContext sc, String function, Object argument, boolean readOnly)
      throws Exception {
    AbstractFunction<Object, Object> a = services.getConfig().getFunction(function);
    a.init(sc, services, data.getExpressionService(), readOnly);
    return callInternal(sc, a, argument, readOnly);
  }

  public Object callInternal(SecurityContext sc, AbstractFunction<Object, Object> a,
      Object argument, boolean readOnly) throws Exception {
    if (readOnly && "write".equals(a.getType()))
      return null;

    ACLContainerRequestFilter.check(sc, a);

    a.sc = sc;
    a.expressionService = data.getExpressionService();

    return a.run(om.readValue(om.writeValueAsBytes(argument), a.getArgumentClass()));
  }
}
