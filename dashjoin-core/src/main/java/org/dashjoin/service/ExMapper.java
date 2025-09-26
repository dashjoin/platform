package org.dashjoin.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dashjoin.model.Table;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper. makes sure the exception text is written to the HTTP entity data. We
 * currently translate all errors to HTTP 500 (internal server error)
 */
@Provider
public class ExMapper implements ExceptionMapper<Throwable> {

  final static Logger logger = Logger.getLogger(ExMapper.class.getName());

  @Inject
  Services services;

  /**
   * convert exception to response code
   */
  @SuppressWarnings("unchecked")
  @Override
  public Response toResponse(Throwable throwable) {

    if (throwable instanceof WebApplicationException)
      return ((WebApplicationException) throwable).getResponse();

    logger.log(Level.INFO, "REST exception", throwable);

    String msg = throwable.getMessage();
    if (msg == null || msg.isEmpty())
      msg = throwable.getClass().getSimpleName();

    try {
      Map<String, Object> r = services.getConfig().getConfigDatabase()
          .read(Table.ofName("dj-config"), Map.of("ID", "exception-mapper"));
      if (r != null) {
        r = (Map<String, Object>) r.get("map");
        for (Entry<String, Object> e : r.entrySet()) {
          if (msg.contains(e.getKey())) {
            msg = "" + e.getValue();
            break;
          }
        }
      }
    } catch (Exception ignore) {
    }

    return toResponse(msg);
  }

  /**
   * can be overridden in order not to require the default glassfish classes
   */
  protected Response toResponse(String msg) {
    return Response.serverError().entity(msg).build();
  }

  /**
   * process an exception in order to extract a string from it that can be used on the UI
   */
  public static String getMessage(Throwable t) {
    if (t.getMessage() != null)
      return t.getMessage().replace('\n', ' ');
    if (t instanceof EncryptionOperationNotPossibleException)
      return "Error decrypting. Please re-enter the credentials.";
    if (t.getCause() != null)
      if (t.getCause() != t)
        return getMessage(t.getCause());
    return t.getClass().getSimpleName();
  }
}
