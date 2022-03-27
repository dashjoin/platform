package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;

public class Add extends AbstractVarArgFunction<Integer> {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Integer run(List arg) throws Exception {
    if (arg.get(0) == null)
      arg.set(0, 0);
    if (arg.get(1) == null)
      arg.set(1, 0);
    return (int) arg.get(0) + (int) arg.get(1);
  }

  @Override
  public String getID() {
    return "add";
  }

  @Override
  public String getType() {
    return "read";
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(Integer.class, Integer.class);
  }

}
