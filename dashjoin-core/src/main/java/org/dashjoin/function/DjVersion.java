package org.dashjoin.function;

import org.dashjoin.service.Manage.Version;

/**
 * function to return the platform version information
 */
public class DjVersion extends AbstractEveryoneFunction<Void, Version> {

  @Override
  public Version run(Void arg) throws Exception {
    return expressionService.getManage().version();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djVersion";
  }
}
