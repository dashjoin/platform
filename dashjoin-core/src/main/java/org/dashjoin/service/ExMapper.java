package org.dashjoin.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * REST exception mapper. makes sure the exception text is written to the HTTP entity data. We
 * currently translate all errors to HTTP 500 (internal server error)
 */
@Provider
public class ExMapper implements ExceptionMapper<Throwable> {

  private final static Logger logger = Logger.getLogger(ExMapper.class.getName());

  /**
   * convert exception to response code
   */
  @Override
  public Response toResponse(Throwable throwable) {

    if (throwable instanceof WebApplicationException)
      return ((WebApplicationException) throwable).getResponse();

    logger.log(Level.INFO, "REST exception", throwable);

    String msg = throwable.getMessage();
    if (msg == null || msg.isEmpty())
      msg = throwable.getClass().getSimpleName();
    return toResponse(msg);
  }

  /**
   * can be overridden in order not to require the default glassfish classes
   */
  protected Response toResponse(String msg) {
    return Response.serverError().entity(msg).build();
  }
}
