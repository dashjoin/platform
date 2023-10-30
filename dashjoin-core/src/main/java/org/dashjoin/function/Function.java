package org.dashjoin.function;

import java.util.List;

/**
 * basic platform function interface
 */
public interface Function<ARG, RET> {

  /**
   * performs the action: RET = run(ARG). The action can have real world side effects.
   */
  public RET run(ARG arg) throws Exception;

  /**
   * returns the argument class
   */
  public Class<ARG> getArgumentClass();

  /**
   * get function name / ID
   * 
   * defaults to ClassName => className
   */
  default public String getID() {
    String str = getClass().getSimpleName();
    return Character.toLowerCase(str.charAt(0)) + str.substring(1);
  }

  /**
   * indicates whether the function has side effects
   * 
   * read: no side effects, function is executed in preview mode also (unless handled by implementation, e.g. query)
   * 
   * write: side effects, function is not executed in preview mode, null returned instead
   * 
   * defaults to "read"
   */
  default public String getType() {
    return "read";
  }

  /**
   * get roles that can run this (null means admin only)
   */
  default public List<String> getRoles() {
    return null;
  }

  /**
   * function signature (see https://docs.jsonata.org/embedding-extending#function-signature-syntax)
   * if provided, the framework performs signature checks
   * 
   * default null means: no signature check performed, the function implementation must perform it
   */
  default public String getSignature() {
    return null;
  }

  /**
   * if a signature is provided and the signature is violated, the help text is displayed
   */
  default public String getHelp() {
    return null;
  }
}
