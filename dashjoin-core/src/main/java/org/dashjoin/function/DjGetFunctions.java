package org.dashjoin.function;

import java.util.List;
import org.dashjoin.service.Manage.FunctionVersion;

/**
 * function to list the available jsonata functions
 */
public class DjGetFunctions extends AbstractEveryoneFunction<Void, List<FunctionVersion>> {

  @Override
  public List<FunctionVersion> run(Void arg) throws Exception {
    return expressionService.getManage().getFunctions();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djGetFunctions";
  }
}
