package org.dashjoin.util.gql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Services;
import org.dashjoin.util.Escape;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.gql.GqlQuery.Direction;
import org.dashjoin.util.gql.GqlQuery.EdgePattern;
import org.dashjoin.util.gql.GqlQuery.Mode;
import org.dashjoin.util.gql.GqlQuery.NodePattern;
import org.dashjoin.util.gql.GqlQuery.ReturnItem;
import org.dashjoin.util.gql.GqlQuery.Selector;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Runs a {@link GqlQuery} over the records of all databases: tables are node labels, foreign key
 * columns are edge labels. Records are read through {@link Data}, so access rights and tenant
 * filters apply as for any other read.
 *
 * <ul>
 * <li>ALL: depth first enumeration from the records of the first (or, if only that one is
 * constrained, the last) node</li>
 * <li>ANY / SHORTEST selectors: breadth first, per start record; each end record gets its shortest
 * path(s)</li>
 * <li>a single edge pattern between two given records (the "connection" search, e.g. MATCH p = ANY
 * SHORTEST (a:PERSON {ID: 1})-[:A|B]-{1,6}(b:EVENT {ID: 7})): breadth first from both ends until
 * they meet, which visits about 2 b^(d/2) instead of b^d records</li>
 * </ul>
 *
 * Searches stop with an error (not a partial result) when they exceed {@link #maxRecords}, take
 * longer than {@link #timeoutMs}, or, in path searches, when a record has more than
 * {@link #maxFanOut} links via one relationship (a hub like a country connects almost everything:
 * leave its relationship out).
 */
public class GqlEngine {

  /** records a query may reach */
  public int maxRecords = 100_000;

  /** path searches: links of one record via one relationship */
  public int maxFanOut = 1000;

  /** hops of an unbounded quantifier (*, +, {m,}) */
  public int maxHops = 50;

  public long timeoutMs = 60_000;

  final Data data;
  final SecurityContext sc;
  final Map<String, AbstractDatabase> dbs = new LinkedHashMap<>();

  /** all foreign keys */
  final List<Rel> rels = new ArrayList<>();
  final Map<String, List<Rel>> relsFrom = new HashMap<>();
  final Map<String, List<Rel>> relsTo = new HashMap<>();

  /** records read during this query, by key */
  final Map<String, Rec> cache = new HashMap<>();

  long started;
  int reached;

  public GqlEngine(Services services, Data data, SecurityContext sc) throws Exception {
    this.data = data;
    this.sc = sc;
    for (AbstractDatabase db : services.getConfig().getDatabases()) {
      dbs.put(db.name, db);
      for (Table t : db.tables.values())
        if (t.properties != null && t.name != null)
          for (Property p : t.properties.values()) {
            String ref = p.ref != null ? p.ref : p.items != null ? p.items.ref : null;
            if (ref == null || ref.split("/").length != 4)
              continue;
            Rel r = new Rel();
            r.sourceDb = db.name;
            r.sourceTable = t.name;
            r.column = p.name;
            r.id = p.ID != null ? p.ID : p.items != null ? p.items.ID : null;
            if (r.id == null)
              r.id = "dj/" + db.name + "/" + Escape.encodeTableOrColumnName(t.name) + "/"
                  + Escape.encodeTableOrColumnName(p.name);
            String[] target = Escape.parseColumnID(ref);
            r.targetDb = target[1];
            r.targetTable = target[2];
            r.targetColumn = target[3];
            rels.add(r);
            relsFrom.computeIfAbsent(tableKey(r.sourceDb, r.sourceTable), x -> new ArrayList<>())
                .add(r);
            relsTo.computeIfAbsent(tableKey(r.targetDb, r.targetTable), x -> new ArrayList<>())
                .add(r);
          }
    }
  }

  /* ============================================================================== model */

  /** a foreign key column: an edge from the record holding it to the record it references */
  static class Rel {
    String sourceDb, sourceTable, column, id;
    String targetDb, targetTable, targetColumn;
  }

  /** a record (node) */
  static class Rec {
    String key;
    String db;
    String table;
    Map<String, Object> value;
  }

  /** a step from a record to a neighbour */
  static class Hop {
    final Rec to;
    final Rel rel;
    /** followed as declared: from the record holding the key to the one it references */
    final boolean outbound;

    Hop(Rec to, Rel rel, boolean outbound) {
      this.to = to;
      this.rel = rel;
      this.outbound = outbound;
    }
  }

  /**
   * a (partial) match as a persistent list: the start (node 0), hops (along edge pattern "edge")
   * and the nodes matched on the way ("node")
   */
  static class Trace {
    final Trace prev;
    final Rec rec;
    final Hop hop;
    final int edge;
    final int node;
    final int length;

    Trace(Trace prev, Rec rec, Hop hop, int edge, int node) {
      this.prev = prev;
      this.rec = rec;
      this.hop = hop;
      this.edge = edge;
      this.node = node;
      this.length = prev == null ? 0 : prev.length + (hop == null ? 0 : 1);
    }

    static Trace start(Rec rec) {
      return new Trace(null, rec, null, -1, 0);
    }

    Trace hop(Hop h, int edge) {
      return new Trace(this, h.to, h, edge, -1);
    }

    Trace node(int node) {
      return new Trace(this, rec, null, -1, node);
    }

    List<Trace> list() {
      List<Trace> res = new ArrayList<>();
      for (Trace t = this; t != null; t = t.prev)
        res.add(t);
      Collections.reverse(res);
      return res;
    }
  }

  static String tableKey(String db, String table) {
    return db + "\u0000" + table;
  }

  /* ============================================================================== run */

  public List<Map<String, Object>> run(GqlQuery q) throws Exception {
    started = System.currentTimeMillis();
    Map<NodePattern, Set<String>> tables = new HashMap<>();
    Map<EdgePattern, Set<Rel>> edgeRels = new HashMap<>();
    for (NodePattern n : q.nodes)
      tables.put(n, n.labels == null ? null : tables(n.labels));
    for (EdgePattern e : q.edges)
      edgeRels.put(e, e.labels == null ? null : rels(e.labels));

    Sink sink = new Sink(q);
    NodePattern first = q.nodes.get(0);
    NodePattern last = q.nodes.get(q.nodes.size() - 1);
    if (first.labels == null && last.labels == null)
      throw new IllegalArgumentException(
          "The first or the last node needs a label (a table) where the search starts: (a:TABLE)");

    boolean selective = q.selector != Selector.ALL;
    EdgePattern single = q.edges.size() == 1 ? q.edges.get(0) : null;
    boolean bidirectional = selective && single != null && single.min <= 1
        && (q.selector == Selector.ANY_SHORTEST || q.selector == Selector.ALL_SHORTEST
            || (q.selector == Selector.ANY && q.k == 1))
        && first.labels != null && !first.properties.isEmpty() && last.labels != null
        && !last.properties.isEmpty();

    Search s = new Search(q, tables, edgeRels, sink);
    if (bidirectional) {
      List<Rec> ends = s.enumerate(last, new HashMap<>());
      for (Rec start : s.enumerate(first, new HashMap<>())) {
        s.bidirectional(start, ends);
        if (sink.full())
          break;
      }
      return sink.rows;
    }

    // start where the pattern is most constrained
    boolean reversed = score(last) > score(first);
    Search r = reversed ? new Search(q.reversed(), tables, edgeRels, sink) : s;
    r.reversedOf = reversed ? q : null;
    for (Rec start : r.enumerate(r.q.nodes.get(0), new HashMap<>())) {
      if (selective)
        r.bfs(start);
      else
        r.dfs(Trace.start(start), 0, 0, bind(new HashMap<>(), r.q.nodes.get(0), start));
      if (sink.full())
        break;
    }
    return sink.rows;
  }

  static int score(NodePattern n) {
    return n.labels == null ? 0 : n.properties.isEmpty() && n.where == null ? 1 : 2;
  }

  static Map<String, Object> bind(Map<String, Object> bindings, NodePattern n, Rec r) {
    if (n.variable != null)
      bindings.put(n.variable, r.value);
    return bindings;
  }

  /** node labels: table names (in any database) or IDs dj/db/table */
  Set<String> tables(List<String> labels) {
    Set<String> res = new LinkedHashSet<>();
    for (String label : labels) {
      boolean found = false;
      if (label.startsWith("dj/") && label.split("/").length == 3) {
        String[] id = Escape.parseTableID(label);
        AbstractDatabase db = dbs.get(id[1]);
        if (db != null && db.tables.containsKey(id[2])) {
          res.add(tableKey(id[1], id[2]));
          found = true;
        }
      } else
        for (AbstractDatabase db : dbs.values())
          if (db.tables.containsKey(label)) {
            res.add(tableKey(db.name, label));
            found = true;
          }
      if (!found)
        throw new IllegalArgumentException("Unknown table: " + label);
    }
    return res;
  }

  /** edge labels: foreign key column names (in any table) or IDs dj/db/table/column */
  Set<Rel> rels(List<String> labels) {
    Set<Rel> res = new LinkedHashSet<>();
    for (String label : labels) {
      boolean found = false;
      for (Rel r : rels)
        if (label.contains("/") ? r.id.equals(label) : r.column.equals(label)) {
          res.add(r);
          found = true;
        }
      if (!found)
        throw new IllegalArgumentException("Unknown relationship (foreign key column): " + label);
    }
    return res;
  }

  /* ============================================================================== records */

  Rec rec(String db, String table, Map<String, Object> value) {
    List<Object> pk = new ArrayList<>();
    Table t = dbs.get(db).tables.get(table);
    if (t != null && t.properties != null)
      for (Property p : t.properties.values())
        if (p.pkpos != null) {
          while (pk.size() <= p.pkpos)
            pk.add(null);
          pk.set(p.pkpos, value.get(p.name));
        }
    String key = tableKey(db, table) + "\u0000"
        + (pk.isEmpty() ? "" + System.identityHashCode(value) : keyOf(pk));
    Rec cached = cache.get(key);
    if (cached != null)
      return cached;
    Rec r = new Rec();
    r.key = key;
    r.db = db;
    r.table = table;
    r.value = value;
    value.put("_dj_resource", Resource.of(db, table, pk));
    cache.put(key, r);
    return r;
  }

  static String keyOf(List<Object> pk) {
    List<String> s = new ArrayList<>();
    for (Object o : pk)
      s.add(String.valueOf(o));
    return String.join("\u0000", s);
  }

  /** records matching column equalities; access errors read as no records */
  List<Map<String, Object>> all(String db, String table, Map<String, Object> where, Integer limit)
      throws Exception {
    try {
      return data.all(sc, db, table, null, limit, null, false,
          where == null || where.isEmpty() ? null : new LinkedHashMap<>(where));
    } catch (WebApplicationException e) {
      return Arrays.asList();
    }
  }

  /** the record referenced by a foreign key value */
  Rec lookup(Rel rel, Object value) throws Exception {
    Table t = dbs.containsKey(rel.targetDb) ? dbs.get(rel.targetDb).tables.get(rel.targetTable)
        : null;
    if (t == null)
      return null;
    List<String> pks = new ArrayList<>();
    for (Property p : t.properties.values())
      if (p.pkpos != null)
        pks.add(p.name);
    if (pks.size() == 1 && pks.get(0).equals(rel.targetColumn)) {
      Rec cached = cache.get(tableKey(rel.targetDb, rel.targetTable) + "\u0000" + value);
      if (cached != null)
        return cached;
      try {
        return rec(rel.targetDb, rel.targetTable,
            data.read(sc, rel.targetDb, rel.targetTable, Arrays.asList("" + value)));
      } catch (WebApplicationException e) {
        return null;
      }
    }
    List<Map<String, Object>> rows =
        all(rel.targetDb, rel.targetTable, MapUtil.of(rel.targetColumn, value), 1);
    return rows.isEmpty() ? null : rec(rel.targetDb, rel.targetTable, rows.get(0));
  }

  /** the neighbours of a record along an edge pattern */
  List<Hop> expand(Rec r, EdgePattern e, Set<Rel> allowed, boolean limitFanOut) throws Exception {
    List<Hop> res = new ArrayList<>();
    String tk = tableKey(r.db, r.table);
    if (e.direction != Direction.LEFT)
      for (Rel rel : relsFrom.getOrDefault(tk, Arrays.asList())) {
        if (allowed != null && !allowed.contains(rel))
          continue;
        Object v = r.value.get(rel.column);
        for (Object x : v instanceof List ? (List<?>) v : Arrays.asList(v))
          if (x != null) {
            Rec to = lookup(rel, x);
            if (to != null)
              res.add(new Hop(to, rel, true));
          }
      }
    if (e.direction != Direction.RIGHT)
      for (Rel rel : relsTo.getOrDefault(tk, Arrays.asList())) {
        if (allowed != null && !allowed.contains(rel))
          continue;
        Object v = r.value.get(rel.targetColumn);
        if (v == null)
          continue;
        List<Map<String, Object>> rows = all(rel.sourceDb, rel.sourceTable,
            MapUtil.of(rel.column, v), limitFanOut ? maxFanOut + 1 : null);
        if (limitFanOut && rows.size() > maxFanOut)
          throw new IllegalStateException("The record " + r.table + " "
              + r.value.get("_dj_resource") + " has more than " + maxFanOut + " links via " + rel.id
              + ". Leave this relationship out of the search: -[:REL1|REL2]-");
        for (Map<String, Object> row : rows)
          res.add(new Hop(rec(rel.sourceDb, rel.sourceTable, row), rel, false));
      }
    reached += res.size();
    if (reached > maxRecords)
      throw new IllegalStateException("The query reached more than " + maxRecords
          + " records. Narrow it down: fewer hops, or only some relationships -[:REL1|REL2]-");
    if (System.currentTimeMillis() - started > timeoutMs)
      throw new IllegalStateException("The query took longer than " + timeoutMs / 1000
          + "s. Narrow it down: fewer hops, or only some relationships -[:REL1|REL2]-");
    return res;
  }

  /* ============================================================================== search */

  class Search {
    final GqlQuery q;
    final Map<NodePattern, Set<String>> tables;
    final Map<EdgePattern, Set<Rel>> edgeRels;
    final Sink sink;
    /** set when q is the reversed query: results are turned around */
    GqlQuery reversedOf;

    Search(GqlQuery q, Map<NodePattern, Set<String>> tables, Map<EdgePattern, Set<Rel>> edgeRels,
        Sink sink) {
      this.q = q;
      this.tables = tables;
      this.edgeRels = edgeRels;
      this.sink = sink;
    }

    /** the relationships an edge pattern allows (null: all) */
    Set<Rel> rels(EdgePattern e) {
      return edgeRels.get(e.original());
    }

    /** the records of a node with a label, filtered by its properties and condition */
    List<Rec> enumerate(NodePattern n, Map<String, Object> bindings) throws Exception {
      List<Rec> res = new ArrayList<>();
      for (String tk : tables.get(n)) {
        String[] t = tk.split("\u0000", 2);
        for (Map<String, Object> row : all(t[0], t[1], n.properties, null)) {
          Rec r = rec(t[0], t[1], row);
          if (matches(r, n, bindings))
            res.add(r);
        }
      }
      return res;
    }

    boolean matches(Rec r, NodePattern n, Map<String, Object> bindings) {
      Set<String> allowed = tables.get(n);
      if (allowed != null && !allowed.contains(tableKey(r.db, r.table)))
        return false;
      for (Map.Entry<String, Object> p : n.properties.entrySet())
        if (!Values.equal(p.getValue(), r.value.get(p.getKey())))
          return false;
      if (n.where != null) {
        Map<String, Object> vars = new HashMap<>(bindings);
        if (n.variable != null)
          vars.put(n.variable, r.value);
        return Boolean.TRUE.equals(n.where.eval(vars::get));
      }
      return true;
    }

    /** the path mode allows the hop */
    boolean allowed(Trace t, Hop h) {
      if (q.mode == Mode.WALK)
        return true;
      if (q.mode == Mode.TRAIL) {
        String edge = edgeKey(t.rec, h);
        for (Trace x = t; x.prev != null; x = x.prev)
          if (x.hop != null && edgeKey(x.prev.rec, x.hop).equals(edge))
            return false;
        return true;
      }
      Rec first = null;
      for (Trace x = t; x != null; x = x.prev) {
        if (x.prev == null)
          first = x.rec;
        else if (x.hop != null && x.rec == h.to)
          return false;
      }
      if (h.to == first)
        // SIMPLE: a path may come back to its start (and then has to end there)
        return q.mode == Mode.SIMPLE && t.length > 0;
      if (q.mode == Mode.SIMPLE && t.length > 0 && t.rec == first)
        return false;
      return true;
    }

    String edgeKey(Rec from, Hop h) {
      return h.rel.id + "\u0000" + (h.outbound ? from.key + "\u0000" + h.to.key
          : h.to.key + "\u0000" + from.key);
    }

    Map<String, Object> bindings(Trace t) {
      Map<String, Object> res = new HashMap<>();
      for (Trace x = t; x != null; x = x.prev)
        if (x.node >= 0 && q.nodes.get(x.node).variable != null)
          res.putIfAbsent(q.nodes.get(x.node).variable, x.rec.value);
      return res;
    }

    int max(EdgePattern e) {
      return e.max == null ? maxHops : e.max;
    }

    /** ALL: every match, depth first; j: current edge pattern, c: hops taken along it */
    void dfs(Trace t, int j, int c, Map<String, Object> bindings) throws Exception {
      if (sink.full())
        return;
      if (j == q.edges.size()) {
        sink.add(this, t);
        return;
      }
      EdgePattern e = q.edges.get(j);
      if (c >= e.min) {
        NodePattern n = q.nodes.get(j + 1);
        if (matches(t.rec, n, bindings)) {
          Object before = n.variable == null ? null : bindings.get(n.variable);
          bind(bindings, n, t.rec);
          dfs(t.node(j + 1), j + 1, 0, bindings);
          if (n.variable != null)
            bindings.put(n.variable, before);
        }
      }
      if (c < max(e))
        for (Hop h : expand(t.rec, e, rels(e), false))
          if (allowed(t, h))
            dfs(t.hop(h, j), j, c + 1, bindings);
    }

    /** a partial match in a breadth first search */
    class Partial {
      final Trace t;
      final int j, c;

      Partial(Trace t, int j, int c) {
        this.t = t;
        this.j = j;
        this.c = c;
      }

      String state() {
        if (j == q.edges.size())
          return t.rec.key + "|end";
        EdgePattern e = q.edges.get(j);
        // beyond the minimum, an unbounded edge pattern is in the same state at every count
        return t.rec.key + "|" + j + "|" + (e.max == null ? Math.min(c, e.min) : c);
      }
    }

    /** the partial and the matches that close edge patterns without another hop */
    List<Partial> closure(Partial p) {
      List<Partial> res = new ArrayList<>();
      res.add(p);
      while (p.j < q.edges.size() && p.c >= q.edges.get(p.j).min) {
        NodePattern n = q.nodes.get(p.j + 1);
        if (!matches(p.t.rec, n, bindings(p.t)))
          break;
        p = new Partial(p.t.node(p.j + 1), p.j + 1, 0);
        res.add(p);
      }
      return res;
    }

    /**
     * selective search from one record, breadth first: the shortest paths to each end record (ANY
     * SHORTEST: one, ALL SHORTEST: all of that length, SHORTEST k / ANY k: k)
     */
    void bfs(Rec start) throws Exception {
      Map<String, Integer> first = new HashMap<>();
      Map<String, Integer> count = new HashMap<>();
      List<Partial> layer = closure(new Partial(Trace.start(start), 0, 0));
      for (int depth = 0; !layer.isEmpty() && !sink.full(); depth++) {
        List<Partial> next = new ArrayList<>();
        for (Partial p : layer) {
          String state = p.state();
          Integer f = first.putIfAbsent(state, depth);
          int n = count.merge(state, 1, Integer::sum);
          boolean admit = q.selector == Selector.ALL_SHORTEST ? f == null || f == depth
              : n <= (q.selector == Selector.ANY_SHORTEST ? 1 : q.k);
          if (!admit)
            continue;
          if (p.j == q.edges.size()) {
            sink.add(this, p.t);
            continue;
          }
          EdgePattern e = q.edges.get(p.j);
          if (p.c < max(e))
            for (Hop h : expand(p.t.rec, e, rels(e), true))
              if (allowed(p.t, h))
                next.addAll(closure(new Partial(p.t.hop(h, p.j), p.j, p.c + 1)));
        }
        layer = next;
      }
    }

    /** a link in a breadth first search tree */
    class Link {
      final Rec from;
      final Hop hop;

      Link(Rec from, Hop hop) {
        this.from = from;
        this.hop = hop;
      }
    }

    /**
     * shortest paths between two records along a single edge pattern, breadth first from both
     * ends: each round grows the smaller side by one hop until a record is reached from both
     */
    void bidirectional(Rec start, List<Rec> ends) throws Exception {
      Map<String, Object> bindings = bind(new HashMap<>(), q.nodes.get(0), start);
      EdgePattern e = q.edges.get(0);
      for (Rec end : ends) {
        if (sink.full())
          return;
        if (!matches(end, q.nodes.get(1), bindings))
          continue;
        if (end == start) {
          if (e.min == 0)
            sink.add(this, Trace.start(start).node(1));
          continue;
        }
        boolean all = q.selector == Selector.ALL_SHORTEST;
        Map<String, Integer> df = new HashMap<>(), db = new HashMap<>();
        Map<String, List<Link>> pf = new HashMap<>(), pb = new HashMap<>();
        Map<String, Rec> recs = new HashMap<>();
        df.put(start.key, 0);
        db.put(end.key, 0);
        recs.put(start.key, start);
        recs.put(end.key, end);
        List<Rec> ff = new ArrayList<>(Arrays.asList(start));
        List<Rec> fb = new ArrayList<>(Arrays.asList(end));
        int depthF = 0, depthB = 0;
        List<String> meet = new ArrayList<>();
        while (!ff.isEmpty() && !fb.isEmpty() && depthF + depthB < max(e) && meet.isEmpty()) {
          boolean forward = ff.size() <= fb.size();
          Map<String, Integer> dist = forward ? df : db, other = forward ? db : df;
          Map<String, List<Link>> parents = forward ? pf : pb;
          int depth = forward ? depthF : depthB;
          List<Rec> next = new ArrayList<>();
          for (Rec x : forward ? ff : fb)
            for (Hop h : expand(x, forward ? e : e.reversed(), rels(e), true)) {
              String y = h.to.key;
              Integer d = dist.get(y);
              if (d == null) {
                dist.put(y, depth + 1);
                parents.computeIfAbsent(y, k -> new ArrayList<>()).add(new Link(x, h));
                recs.put(y, h.to);
                next.add(h.to);
              } else if (all && d == depth + 1)
                parents.get(y).add(new Link(x, h));
            }
          if (forward) {
            depthF++;
            ff = next;
          } else {
            depthB++;
            fb = next;
          }
          // records reached from both sides: the shortest connections go through them
          Integer best = null;
          for (Rec x : next)
            if (other.containsKey(x.key)) {
              int total = dist.get(x.key) + other.get(x.key);
              if (best == null || total < best) {
                best = total;
                meet.clear();
              }
              if (total == best && !meet.contains(x.key))
                meet.add(x.key);
            }
        }
        if (!all && meet.size() > 1)
          meet = meet.subList(0, 1);
        for (String m : meet) {
          for (List<Hop> head : paths(pf, start.key, m, all))
            for (List<Hop> tail : paths(pb, end.key, m, all)) {
              Trace t = Trace.start(start);
              for (Hop h : head)
                t = t.hop(h, 0);
              // the backward side was searched from the end: turn its hops around
              for (int i = tail.size() - 1; i >= 0; i--) {
                Hop h = tail.get(i);
                Rec to = i == 0 ? end : tail.get(i - 1).to;
                t = t.hop(new Hop(to, h.rel, !h.outbound), 0);
              }
              sink.add(this, t.node(1));
              if (sink.full() || !all)
                return;
            }
        }
      }
    }

    /** the hops from root to node along the search tree (all of them, or the first) */
    List<List<Hop>> paths(Map<String, List<Link>> parents, String root, String node, boolean all) {
      List<List<Hop>> res = new ArrayList<>();
      if (node.equals(root)) {
        res.add(new ArrayList<>());
        return res;
      }
      for (Link l : parents.get(node)) {
        for (List<Hop> p : paths(parents, root, l.from.key, all)) {
          List<Hop> x = new ArrayList<>(p);
          x.add(l.hop);
          res.add(x);
          if (!all)
            return res;
          if (res.size() > maxRecords)
            throw new IllegalStateException("More than " + maxRecords + " shortest paths");
        }
      }
      return res;
    }
  }

  /* ============================================================================== results */

  /** collects result rows: WHERE, RETURN, DISTINCT, LIMIT */
  class Sink {
    final GqlQuery q;
    final List<Map<String, Object>> rows = new ArrayList<>();
    final Set<String> seen = new HashSet<>();

    Sink(GqlQuery q) {
      this.q = q;
    }

    boolean full() {
      return q.limit != null && rows.size() >= q.limit;
    }

    void add(Search s, Trace t) {
      if (full())
        return;
      List<Trace> events = t.list();
      // the query's own orientation
      int nodeCount = q.nodes.size(), edgeCount = q.edges.size();
      List<Rec> recs = new ArrayList<>();
      List<Hop> hops = new ArrayList<>();
      List<Integer> hopEdge = new ArrayList<>();
      Map<Integer, Integer> nodeAt = new HashMap<>();
      for (Trace x : events) {
        if (x.prev == null)
          recs.add(x.rec);
        if (x.hop != null) {
          hops.add(x.hop);
          hopEdge.add(x.edge);
          recs.add(x.rec);
        }
        if (x.node >= 0)
          nodeAt.putIfAbsent(x.node, recs.size() - 1);
      }
      if (s.reversedOf != null) {
        int last = recs.size() - 1;
        Collections.reverse(recs);
        List<Hop> turned = new ArrayList<>();
        List<Integer> edges = new ArrayList<>();
        for (int i = hops.size() - 1; i >= 0; i--) {
          Hop h = hops.get(i);
          // hop i went from recs[i] to recs[i + 1] (old order); now it ends at the old recs[i]
          turned.add(new Hop(recs.get(hops.size() - 1 - i + 1), h.rel, !h.outbound));
          edges.add(edgeCount - 1 - hopEdge.get(i));
        }
        hops = turned;
        hopEdge = edges;
        Map<Integer, Integer> at = new HashMap<>();
        for (Map.Entry<Integer, Integer> n : nodeAt.entrySet())
          at.put(nodeCount - 1 - n.getKey(), last - n.getValue());
        nodeAt = at;
      }

      Map<String, Object> vars = new HashMap<>();
      for (int i = 0; i < nodeCount; i++) {
        NodePattern n = q.nodes.get(i);
        if (n.variable != null && nodeAt.containsKey(i))
          vars.put(n.variable, recs.get(nodeAt.get(i)).value);
      }
      List<Map<String, Object>> steps = new ArrayList<>();
      Map<Integer, List<Map<String, Object>>> edgeValues = new HashMap<>();
      for (int i = 0; i < hops.size(); i++) {
        Map<String, Object> edge = edge(hops.get(i));
        edgeValues.computeIfAbsent(hopEdge.get(i), x -> new ArrayList<>()).add(edge);
        steps.add(MapUtil.of("edge", edge, "end", recs.get(i + 1).value));
      }
      for (int i = 0; i < edgeCount; i++) {
        EdgePattern e = q.edges.get(i);
        if (e.variable != null) {
          List<Map<String, Object>> list = edgeValues.getOrDefault(i, new ArrayList<>());
          // a quantified edge pattern binds the list of its edges (GQL group variable)
          vars.put(e.variable, e.quantified() ? list : list.isEmpty() ? null : list.get(0));
        }
      }
      if (q.pathVariable != null)
        vars.put(q.pathVariable, MapUtil.of("start", recs.get(0).value, "steps", steps));

      Function<String, Object> lookup = vars::get;
      if (q.where != null && !Boolean.TRUE.equals(q.where.eval(lookup)))
        return;
      Map<String, Object> row = new LinkedHashMap<>();
      for (ReturnItem item : q.ret)
        row.put(item.name, item.expr.eval(lookup));
      if (q.distinct && !seen.add(String.valueOf(row)))
        return;
      rows.add(row);
    }

    Map<String, Object> edge(Hop h) {
      Map<String, Object> res = new LinkedHashMap<>();
      res.put("_dj_edge", h.rel.id);
      res.put("_dj_outbound", h.outbound);
      return res;
    }
  }
}
