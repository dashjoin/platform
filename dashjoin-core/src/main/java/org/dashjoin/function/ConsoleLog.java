package org.dashjoin.function;

/**
 * test / demo function
 */
public class ConsoleLog extends AbstractFunction<Object, Object> {

  @Override
  public Object run(Object arg) throws Exception {
    System.out.println(arg);
    return arg;
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }

  @Override
  public String getID() {
    return "echo";
  }
}
