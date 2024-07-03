package org.dashjoin.service;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * In memory store for query performance data. Note that the info is local to a single cluster node,
 * so results may vary between calls depending on which node computes the result. This DB serves
 * queries to config/dj-query-performance
 */
public class PerformanceDatabase extends JSONDatabase {

  /**
   * query performance entry (one row in the dj-query-performance table)
   */
  public static class QueryPerformance {
    public QueryPerformance(String query, QueryMeta meta) {
      this.query = query;
      this.catalog = meta == null ? null : meta.ID;
    }

    /**
     * the query string / also the PK
     */
    public String query;

    /**
     * optional FK to the query catalog
     */
    public String catalog;

    /**
     * query use case / type
     */
    public String type;

    /**
     * last run timestamp
     */
    public Date lastRun;

    /**
     * called how often
     */
    public int count;

    /**
     * error how often
     */
    public int errorCount;

    /**
     * last error
     */
    public String lastError;

    /**
     * total aggregated runtime of all runs
     */
    public long totalTimeMs;

    /**
     * runtime of the last run
     */
    public long lastTimeMs;

    /**
     * last timeout
     */
    public Integer lastTimeoutMs;

    /**
     * last limit
     */
    public Integer lastLimit;

    /**
     * computed average runtime
     */
    public long getAverageTimeMs() {
      return totalTimeMs / count;
    }

    public void add(long runtime, Integer limit, Integer queryTimeout, String error) {

      for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
        if (el.getMethodName().equals("keys")) {
          this.type = "keys";
          break;
        }
        if (el.getMethodName().equals("search")) {
          this.type = "search";
          break;
        }
        if (el.getMethodName().equals("query")) {
          this.type = "query";
          break;
        }
        if (el.getMethodName().equals("all")) {
          this.type = "all";
          break;
        }
        if (el.getMethodName().equals("read")) {
          this.type = "read";
          break;
        }
        if (el.getMethodName().equals("update")) {
          this.type = "update";
          break;
        }
        if (el.getMethodName().equals("create")) {
          this.type = "create";
          break;
        }
        if (el.getMethodName().equals("delete")) {
          this.type = "delete";
          break;
        }
        if (el.getMethodName().equals("evaluate")) {
          this.type = "jsonata";
          break;
        }
      }

      if (error != null) {
        this.lastError = error;
        this.errorCount++;
      }
      this.count++;
      this.lastTimeMs = runtime;
      this.totalTimeMs += runtime;
      this.lastRun = new Date();
      this.lastLimit = limit;
      this.lastTimeoutMs = queryTimeout == null ? null : queryTimeout * 1000;
    }
  }

  protected static final ObjectMapper objectMapper =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  /**
   * Concurrent cache. contains max 1000 entries. Queries with low total runtimes are evicted first.
   */
  static Map<String, QueryPerformance> queries = new ConcurrentHashMap<>();

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    return objectMapper.convertValue(queries.get(search.get("ID")), JSONDatabase.tr);
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return objectMapper.convertValue(queries, JSONDatabase.trr);
  }

  synchronized public static void add(String query, long runtime, Integer limit,
      Integer queryTimeout, String error) {
    add(query, null, runtime, limit, queryTimeout, error);
  }

  /**
   * update performance table
   * 
   * @param query the query that was run
   * @param runtime query runtime in millisecs
   * @param limit optional limit given to the connection
   */
  synchronized public static void add(String query, QueryMeta meta, long runtime, Integer limit,
      Integer queryTimeout, String error) {

    QueryPerformance q = queries.get(query);
    if (q == null) {
      q = new QueryPerformance(query, meta);
      if (queries.size() == 1000) {
        // evict the entry with the smallest total runtime
        long min = Long.MAX_VALUE;
        String id = null;
        for (Entry<String, QueryPerformance> e : queries.entrySet())
          if (e.getValue().totalTimeMs < min) {
            min = e.getValue().totalTimeMs;
            id = e.getKey();
          }
        queries.remove(id);
      }
      queries.put(query, q);
    }

    q.add(runtime, limit, queryTimeout, error);
  }
}
