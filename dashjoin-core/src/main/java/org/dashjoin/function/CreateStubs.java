package org.dashjoin.function;

/**
 * expose manage.createStubs as a function
 */
public class CreateStubs extends AbstractFunction<Void, Void> {

  @Override
  public Void run(Void arg) throws Exception {
    expressionService.getManage().createStubs(sc);
    return null;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "createStubs";
  }

  @Override
  public String getType() {
    return "write";
  }
}
