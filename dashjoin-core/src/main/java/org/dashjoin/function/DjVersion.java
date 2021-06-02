package org.dashjoin.function;

import org.dashjoin.service.Manage.Version;

public class DjVersion extends AbstractFunction<Void, Version> {

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

  @Override
  public String getType() {
    return "read";
  }
}
