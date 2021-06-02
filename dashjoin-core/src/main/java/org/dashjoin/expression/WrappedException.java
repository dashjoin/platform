package org.dashjoin.expression;

/**
 * Runtime exception that is used to wrap regular exceptions from JSONata evaluations
 */
public class WrappedException extends RuntimeException {

  private static final long serialVersionUID = -1178029084052277282L;

  public WrappedException(Exception e) {
    super(e);
  }
}
