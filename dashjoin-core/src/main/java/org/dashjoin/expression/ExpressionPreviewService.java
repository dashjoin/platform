package org.dashjoin.expression;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.expression.ExpressionService.ExpressionAndData;
import org.dashjoin.mapping.ETL;
import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * REST API for expression evaluation in read only mode
 */
@Path(Services.REST_PREFIX + "expression-preview")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ExpressionPreviewService {

  public static class ExpressionCursorData extends ExpressionAndData {
    public boolean foreach;
  }

  @Inject
  ExpressionService expression;

  @POST
  @Path("/")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public Object resolve(@Context SecurityContext sc,
      @Parameter(description = "expression and context to evaluate") ExpressionCursorData e)
      throws Exception {
    if (e.foreach) {
      try {
        ETL.context.set(new org.dashjoin.mapping.ETL.Context());
        expression.jsonata(sc, e.expression, ExpressionService.o2j(e.data), true);
        return ETL.context.get().queue;
      } finally {
        ETL.context.set(null);
      }
    } else
      return expression.jsonata(sc, e.expression, ExpressionService.o2j(e.data), true);
  }
}
