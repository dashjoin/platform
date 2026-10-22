package org.dashjoin.expression;

/**
 * ExpressionService intercepts some JException in order to provide better error messages. We use
 * this class to retain the error location
 */
public class JsonataException extends Exception {

  private static final long serialVersionUID = 1901661167799426117L;

  int loc;

  public JsonataException(String msg, int loc) {
    super(msg);
    this.loc = loc;
  }

  public int getLocation() {
    return loc;
  }
}
