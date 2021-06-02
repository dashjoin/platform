package org.dashjoin.model;

import java.util.List;
import java.util.Map;

/**
 * metadata about the queries stored in the system query catalog
 */
public class QueryMeta {

  /**
   * unique query ID
   */
  public String ID;

  /**
   * roles that are allowed to run the query
   */
  public List<String> roles;

  /**
   * the query which may include names arguments such as {$par}
   */
  public String query;

  /**
   * default database to use when editing
   */
  public String database;

  /**
   * the query type. This is a hint for the database implementation as to which native API to use.
   * An example are the values read, write, proc telling a SQL DB to use query, update or stored
   * procedures
   */
  public String type;

  /**
   * argument names to simple type. we do not use Property here, since the replacement algorithm
   * only supports ${var} and not things like ${var[0].x}. If we introduce the latter, we need to be
   * able to specify a full json schema for the argument structure
   */
  public Map<String, Object> arguments;

  public static QueryMeta ofQuery(String query) {
    QueryMeta t = new QueryMeta();
    t.query = query;
    return t;
  }
}
