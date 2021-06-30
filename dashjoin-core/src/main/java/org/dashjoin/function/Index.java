package org.dashjoin.function;

/**
 * function that allows to reference the row number in a table that is being mapped using JSONata
 */
public class Index extends AbstractFunction<Void, Integer> {

  private static ThreadLocal<Integer> counter = new ThreadLocal<>();

  public static void set(int value) {
    counter.set(value);
  }

  public static int get() {
    return counter.get();
  }

  public synchronized static void increment() {
    if (counter.get() == null)
      counter.set(0);
    else
      counter.set(counter.get() + 1);
  }

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
