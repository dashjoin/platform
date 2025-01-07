package org.dashjoin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dashjoin.service.Data;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Services;
import org.dashjoin.util.OpenCypherQuery.Chain;
import jakarta.ws.rs.core.SecurityContext;

public class OpenCypher {

  /**
   * table in the query
   */
  static class Table {
    String db;
    String table;
    String variable;
  }

  /**
   * - prop -> in the query
   */
  static class Relation {
    String name;
    String variable;
    Boolean left2right;
  }

  /**
   * cypher pattern of table - rel - table
   */
  static class Pattern {
    Table left;
    Relation relation;
    Table right;
    boolean isLast;
    public Pattern next;
  }

  /**
   * query consists of a list of patterns
   */
  List<Pattern> patterns;

  /**
   * delegate so we can use util methods there
   */
  OpenCypherQuery query;

  /**
   * stuff we need to run the queries
   */
  Services service;
  Data data;
  SecurityContext sc;

  /**
   * convert old table structure to new (parsing made explicit)
   */
  Table parseTable(OpenCypherQuery.Table t) {
    Table res = new Table();
    res.variable = t.variable;
    if (t.name != null) {
      res.db = Escape.parseTableID(t.name)[1];
      res.table = Escape.parseTableID(t.name)[2];
    }
    return res;
  }

  /**
   * query constructor - parsing is done in old code
   */
  public OpenCypher(OpenCypherQuery query) {
    System.out.println(query);
    this.query = query;

    patterns = new ArrayList<>();

    Pattern start = new Pattern();
    start.right = parseTable(query.context);
    patterns.add(start);

    Table left = start.right;
    for (Chain i : query.links) {
      Pattern p = new Pattern();
      patterns.get(patterns.size() - 1).next = p;
      patterns.add(p);
      p.left = left;
      p.relation = new Relation();
      p.relation.left2right = i.left2right;
      p.relation.name = i.edge.name;
      p.relation.variable = i.edge.variable;
      p.right = parseTable(i.table);
      left = p.right;
    }
    patterns.get(patterns.size() - 1).isLast = true;
  }

  /**
   * query entry point (row is the starting node)
   */
  public List<Map<String, Object>> run(Services service, Data data, SecurityContext sc,
      Map<String, Object> row) throws Exception {

    Path path = new Path();
    path.bindings = new ArrayList<>();
    Binding start = new Binding();
    path.bindings.add(start);
    start.value = row;
    start.pattern = patterns.get(0);
    start.node =
        OpenCypher.this.query.getResource(start.pattern.right.db, start.pattern.right.table, row);

    this.data = data;
    this.sc = sc;
    this.service = service;

    List<Map<String, Object>> res = new ArrayList<>();
    path.search(res);
    return res;
  }

  /**
   * binding links solution values to the query element
   */
  static class Binding {
    Pattern pattern;
    Resource node;
    Map<String, Object> value;
    Map<String, Object> link;
  }

  /**
   * a path is a (partial) solution to the query
   */
  class Path {
    List<Binding> bindings;

    boolean isSolution() {
      return bindings.get(bindings.size() - 1).pattern.isLast;
    }

    /**
     * given the current partial result, return the list of possible next patterns to traverse. This
     * can be a list, because there are constructs like "traverse this link 1 or 2 times"
     */
    List<Pattern> candidatePatterns() {
      // TODO: only take next pattern for now
      Pattern next = bindings.get(bindings.size() - 1).pattern.next;
      if (next == null)
        return Arrays.asList();
      else
        return Arrays.asList(next);
    }

    /**
     * recursive search
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void search(List<Map<String, Object>> res) throws Exception {
      if (isSolution()) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (Binding b : bindings) {
          b.value.put("_dj_resource", b.node);
          r.put(b.pattern.right.variable, b.value);
          if (b.pattern.relation != null)
            r.put(b.pattern.relation.variable, b.link);
        }
        res.add(OpenCypher.this.query.project(r, null));
      }
      for (Pattern pattern : candidatePatterns()) {
        Binding b = bindings.get(bindings.size() - 1);
        Object traverse = data.traverse(sc, b.node.database, b.node.table,
            b.node.pk.stream().map(i -> i.toString()).collect(Collectors.toList()),
            pattern.relation.name);
        List<Map<String, Object>> list = traverse instanceof List ? (List) traverse
            : Arrays.asList((Map<String, Object>) traverse);
        for (Map<String, Object> item : list) {
          Binding nb = newBinding(b, pattern, item);

          Path np = new Path();
          np.bindings = new ArrayList<>(bindings);
          np.bindings.add(nb);
          np.search(res);
        }
      }
    }

    /**
     * given the current binding b, the next pattern and the match (item), make sure the item type
     * matches the pattern and return a new binding
     */
    Binding newBinding(Binding b, Pattern pattern, Map<String, Object> item) {
      Binding nb = new Binding();
      nb.pattern = pattern;
      nb.value = item;

      nb.link = MapUtil.of("_dj_edge", pattern.relation.name, "_dj_outbound",
          pattern.relation.left2right);

      Resource targetType = targetType(b.node, pattern.relation.name);
      nb.node = OpenCypher.this.query.getResource(targetType.database, targetType.table, item);

      if (pattern.right.table != null)
        if (!pattern.right.table.equals(targetType.table))
          return null;
      if (pattern.right.db != null)
        if (!pattern.right.table.equals(targetType.database))
          return null;

      return nb;
    }

    /**
     * given a FK, return db / table it points to
     */
    Resource targetType(Resource from, String prop) {
      String ref = OpenCypher.this.query.dbs.get(from.database).tables.get(from.table).properties
          .get(prop).ref;
      String[] arr = Escape.parseColumnID(ref);
      Resource res = new Resource();
      res.database = arr[1];
      res.table = arr[2];
      return res;
    }
  }
}
