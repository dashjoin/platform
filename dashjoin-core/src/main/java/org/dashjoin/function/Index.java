package org.dashjoin.function;

public class Index extends AbstractFunction<Void, Integer> {

  public static ThreadLocal<Integer> counter = new ThreadLocal<>();

  @Override
  public Integer run(Void arg) throws Exception {
    return counter.get();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "index";
  }

  @Override
  public String getType() {
    return "read";
  }
}
