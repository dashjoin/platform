package org.dashjoin.function;

import java.util.HashMap;
import java.util.Map;

/**
 * Function that allows to reference the row number in a table that is being mapped using JSONata
 * 
 * If no job ID is set, a thread local counter is used. If the job ID is set, a counter per job is
 * used (each worker thread of the job needs to call setJobID)
 */
public class Index extends AbstractFunction<Void, Long> {
  // Per-thread index counter
  private static ThreadLocal<Long> counter = new ThreadLocal<>();

  // Per-job index counters. If thread-local job ID is set, this counter is used
  private static Map<String, Long> counterMap = new HashMap<>();
  private static ThreadLocal<String> jobId = new ThreadLocal<>();

  public static String getJobID() {
    return jobId.get();
  }

  public static void setJobID(String id) {
    jobId.set(id);
  }

  /**
   * Resets the counter for the job.
   */
  public static void resetJob() {
    set(0);
  }

  /**
   * Resets the counter in a streaming thread. Will not reset if jobID is set.
   */
  public static void reset() {
    if (getJobID() == null)
      set(0);
  }

  public static synchronized void set(long value) {
    String id = getJobID();
    if (id != null)
      counterMap.put(id, value);
    else
      counter.set(value);
  }

  public static synchronized long get() {
    String id = getJobID();
    if (id != null)
      return counterMap.get(id);

    Long val = counter.get();
    return val != null ? val : 0L;
  }

  /**
   * Returns the current counter and increments it atomically
   * 
   * @return
   */
  public synchronized static long increment() {
    long ix = get();
    set(ix + 1);
    return ix;
  }

  private static ThreadLocal<Long> returnValue = new ThreadLocal<>();

  /**
   * Sets the return value (on the current thread).
   * 
   * This ensures that while evaluating an expression the value stays constant.
   * 
   * @param val
   */
  public static void setReturnValue(long val) {
    returnValue.set(val);
  }

  @Override
  public Long run(Void arg) throws Exception {
    if (returnValue.get() != null)
      return returnValue.get().longValue();

    return get();
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
