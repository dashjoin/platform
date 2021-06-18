package org.dashjoin.function;

import java.util.List;

/**
 * basic platform function interface
 */
public interface Function<ARG, RET> extends java.util.function.Function<ARG, RET> {

  /**
   * performs the action: RET = run(ARG). The action can have real world side effects.
   */
  public RET run(ARG arg) throws Exception;

  /**
   * runtime exception used to wrap exceptions. used in order to make the dashjoin function
   * interface compatible with jdk functions
   */
  public class FunctionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public FunctionException() {
      super();
    }

    public FunctionException(String message) {
      super(message);
    }

    public FunctionException(String message, Throwable cause) {
      super(message, cause);
    }

    public FunctionException(Throwable cause) {
      super(cause);
    }
  }

  @Override
  default RET apply(ARG arg) {
    try {
      return run(arg);
    } catch (Exception e) {
      throw new FunctionException(e);
    }
  }

  /**
   * returns the argument class
   */
  public Class<ARG> getArgumentClass();

  /**
   * get function name / ID
   */
  public String getID();

  /**
   * either read or write
   */
  public String getType();

  /**
   * get roles that can run this (null means admin only)
   */
  default public List<String> getRoles() {
    return null;
  }
}
