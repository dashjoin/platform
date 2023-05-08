package org.dashjoin.function;

/**
 * base class for functions that can be called by everyone
 */
public abstract class AbstractEveryoneFunction<ARG, RET> extends AbstractFunction<ARG, RET>
    implements EveryoneFunction {

  @Override
  public String getType() {
    return "read";
  }
}
