package org.dashjoin.function;

/**
 * expose manage.saveApi as a function
 */
public class SaveApi extends AbstractFunction<Void, Void> {

  @Override
  public Void run(Void arg) throws Exception {
    expressionService.getManage().saveapi(sc);
    return null;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "saveApi";
  }

  @Override
  public String getType() {
    return "write";
  }
}
