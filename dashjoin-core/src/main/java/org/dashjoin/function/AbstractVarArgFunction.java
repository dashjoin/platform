package org.dashjoin.function;

import java.util.List;

/**
 * like abstractfunction but allows multiple args which are passed to run([arg0, arg1, ...])
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractVarArgFunction<RET> extends AbstractFunction<List, RET> {

  /**
   * returns the argument class
   */
  @Override
  public Class<List> getArgumentClass() {
    return List.class;
  }

  public abstract List<Class> getArgumentClassList();
}
