package org.dashjoin.function;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JobStatus extends AbstractFunction<Void, JobStatus.Status> {

  public static class Status {
    public String status;
    public Long start;
    public Long end;
  }

  private static ThreadLocal<Status> jobStatus = new ThreadLocal<>();

  public static void reset() {
    jobStatus.set(null);
  }

  public static void set(Object status, Object start, Object end) {
    Status s = new Status();
    if (end instanceof String)
      s.end =
          Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) end))).getTime();
    if (start instanceof String)
      s.start =
          Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) start))).getTime();
    if (status instanceof String)
      s.status = (String) status;
    jobStatus.set(s);
  }

  @Override
  public Status run(Void arg) throws Exception {
    return jobStatus.get();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "jobStatus";
  }

  @Override
  public String getType() {
    return "read";
  }
}
