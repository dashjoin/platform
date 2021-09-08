package org.dashjoin.function;

import org.dashjoin.service.Data;

/**
 * function to check if we are being called from a DB trigger (can be used to avoid trigger
 * recursion)
 */
public class IsRecursiveTrigger extends AbstractFunction<Void, Boolean> {

  @Override
  public Boolean run(Void arg) throws Exception {
    int counter = 0;
    for (StackTraceElement s : Thread.currentThread().getStackTrace())
      if ("dbTriggers".equals(s.getMethodName()))
        if (Data.class.getName().equals(s.getClassName()))
          counter++;
    return counter > 1;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "isRecursiveTrigger";
  }

  @Override
  public String getType() {
    return "read";
  }
}
