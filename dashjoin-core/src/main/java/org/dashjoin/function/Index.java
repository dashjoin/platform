package org.dashjoin.function;

/**
 * function that allows to reference the row number in a table that is being mapped using JSONata
 */
public class Index extends AbstractFunction<Void, Long> {

  private static ThreadLocal<Long> counter = new ThreadLocal<>();

  public static void reset() {
    set(0);
  }

  public static void set(long value) {
    counter.set(value);
  }

  public static long get() {
    Long val = counter.get();
    return val != null ? val : 0L;
  }

  public synchronized static void increment() {
    set(get() + 1);
  }

  @Override
  public Long run(Void arg) throws Exception {
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
