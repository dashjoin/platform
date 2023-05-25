package org.dashjoin.expression;

import jakarta.ws.rs.WebApplicationException;

/**
 * Runtime exception that is used to wrap regular exceptions from JSONata evaluations
 */
public class WrappedException extends RuntimeException {

  private static final long serialVersionUID = -1178029084052277282L;

  public WrappedException(Exception e) {
    super(e);
  }

  public String getMessage() {
    // workaround for the case where we wrap a JAX-RS error (usually a 401 access denied)
    // normally, the wrapper is removed and the cause is passed on
    // if we run in polyglot mode, this exception gets wrapped in a PolyglotException
    // which does not provide access to the underlying exception and
    // only uses ex.toString()
    // therefore, we preserve this message in getResponse().getEntity()
    if (getCause() instanceof WebApplicationException) {
      WebApplicationException ex = (WebApplicationException) getCause();
      if (ex.getResponse().getEntity() instanceof String)
        return (String) ex.getResponse().getEntity();
    }
    return super.getMessage();
  }
}
