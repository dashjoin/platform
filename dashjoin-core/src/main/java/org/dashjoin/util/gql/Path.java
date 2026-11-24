package org.dashjoin.util.gql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * a path in a graph query result: the start record and the steps to the end. Database drivers
 * with native graph queries (ArangoDB) return their paths in this shape too.
 */
public class Path {

  /** the first record */
  public Map<String, Object> start;

  /** the edges taken and the records reached */
  public List<Step> steps = new ArrayList<>();

  public static class Step {

    /** {_dj_edge: foreign key column ID dj/db/table/column, _dj_outbound: followed as declared} */
    public Map<String, Object> edge;

    /** the record reached */
    public Map<String, Object> end;
  }
}
