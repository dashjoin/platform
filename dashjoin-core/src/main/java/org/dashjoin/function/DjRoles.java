package org.dashjoin.function;

import java.util.List;

/**
 * expose getRoles as a function
 */
public class DjRoles extends AbstractFunction<Void, List<String>> {

  @Override
  public List<String> run(Void arg) throws Exception {
    return expressionService.getManage().roles(sc);
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djRoles";
  }

  @Override
  public String getType() {
    return "read";
  }
}
