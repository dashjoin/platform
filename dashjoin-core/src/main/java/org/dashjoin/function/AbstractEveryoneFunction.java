package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;

/**
 * base class for functions that can be called by everyone
 */
public abstract class AbstractEveryoneFunction<ARG, RET> extends AbstractFunction<ARG, RET> {

  @Override
  public String getType() {
    return "read";
  }

  @Override
  public List<String> getRoles() {
    return Arrays.asList("authenticated");
  }
}
