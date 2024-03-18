package org.dashjoin.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.dashjoin.service.ACLContainerRequestFilter;
import org.dashjoin.service.Data;
import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * REST API for streaming function calls
 */
@Path(Services.REST_PREFIX + "functionStream")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class FunctionStreamService {

  @Inject
  Services services;

  @Inject
  Data data;

  @POST
  @Path("/{function}")
  @Operation(summary = "streams a predefined function with the provided argument")
  @APIResponse(description = "Returns the function result")
  public Response call(@Context SecurityContext sc,
      @Parameter(description = "name of the function to run",
          example = "echo") @PathParam("function") String function,
      Object argument) throws Exception {

    AbstractFunction<Object, Object> a = services.getConfig().getFunction(function);
    a.init(sc, services, data.getExpressionService(), false);

    ACLContainerRequestFilter.check(sc, a);

    if (!(a instanceof RestJson))
      throw new Exception("Only RestJson can be streamed");

    ((RestJson) a).stream = true;

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        Writer writer = new OutputStreamWriter(out);
        try {
          Reader r = (Reader) a.run(argument);
          BufferedReader br = new BufferedReader(r);
          String line;
          while ((line = br.readLine()) != null) {
            writer.write(line + "\n");
            writer.flush();
          }
        } catch (Exception e) {
          throw new IOException(e);
        }
      }
    };
    return Response.ok(stream).build();
  }
}
