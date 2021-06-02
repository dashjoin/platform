package org.dashjoin.function;

import java.util.List;
import org.dashjoin.service.Manage.Version;

public class DjGetDatabases extends AbstractFunction<Void, List<Version>> {

  @Override
  public List<Version> run(Void arg) throws Exception {
    return expressionService.getManage().getDatabases();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djGetDatabases";
  }

  @Override
  public String getType() {
    return "read";
  }
}
