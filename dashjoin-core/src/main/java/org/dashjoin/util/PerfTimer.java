package org.dashjoin.util;

public class PerfTimer {

  long start;

  public PerfTimer() {
    start = System.currentTimeMillis();
  }

  public long seconds() {
    long now = System.currentTimeMillis();
    long res = (now - start) / 1000;
    start = now;
    return res;
  }
}
