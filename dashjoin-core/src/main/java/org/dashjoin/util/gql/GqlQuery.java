package org.dashjoin.util.gql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.dashjoin.util.Template;

/**
 * A graph query in a subset of ISO GQL: one path pattern, an optional WHERE and a RETURN.
 *
 * <pre>
 * MATCH [p =] [selector] [mode] (a:LABEL {KEY: value} WHERE cond) -[e:REL|REL2]-&gt;{1,5} (b) ...
 * [WHERE cond]
 * RETURN [DISTINCT] a, a.NAME AS name, e, p [LIMIT n]
 * </pre>
 *
 * <ul>
 * <li>selector: ALL (default), ANY, ANY k, ANY SHORTEST, ALL SHORTEST, SHORTEST k</li>
 * <li>mode: WALK (default), TRAIL (no repeated edge), ACYCLIC (no repeated node), SIMPLE (like
 * ACYCLIC, but the path may end where it started)</li>
 * <li>node labels are tables: a table name or its ID (`dj/db/table`); several with |</li>
 * <li>edge labels are foreign key columns: a column name or its ID (`dj/db/table/column`). An edge
 * points from the record holding the key to the record it references (-&gt;), &lt;- follows it
 * backwards, - in either direction</li>
 * <li>quantifiers after an edge: {m,n}, {m,}, {,n}, {n}, * and +</li>
 * <li>parameters: $name (from the arguments), also the platform's ${name} templates</li>
 * </ul>
 */
public class GqlQuery {

  public enum Selector {
    ALL, ANY, ANY_SHORTEST, ALL_SHORTEST, SHORTEST
  }

  public enum Mode {
    WALK, TRAIL, ACYCLIC, SIMPLE
  }

  public enum Direction {
    /** -[]-> */
    RIGHT,
    /** &lt;-[]- */
    LEFT,
    /** -[]- */
    ANY
  }

  /** (var:LABEL|LABEL2 {KEY: value} WHERE cond) */
  public static class NodePattern {
    public String variable;
    /** null: any table */
    public List<String> labels;
    public Map<String, Object> properties = new LinkedHashMap<>();
    public Expr where;

    @Override
    public String toString() {
      StringBuilder s = new StringBuilder("(");
      if (variable != null)
        s.append(name(variable));
      if (labels != null)
        s.append(":").append(labels(labels));
      if (!properties.isEmpty()) {
        List<String> kv = new ArrayList<>();
        for (Entry<String, Object> e : properties.entrySet())
          kv.add(name(e.getKey()) + ": " + Literal.format(e.getValue()));
        s.append(s.length() > 1 ? " " : "").append("{").append(String.join(", ", kv)).append("}");
      }
      if (where != null)
        s.append(s.length() > 1 ? " " : "").append("WHERE ").append(where);
      return s.append(")").toString();
    }
  }

  /** -[var:REL|REL2]-&gt;{min,max} */
  public static class EdgePattern {
    public String variable;
    /** null: any foreign key */
    public List<String> labels;
    public Direction direction = Direction.ANY;
    public int min = 1;
    /** null: unbounded */
    public Integer max = 1;
    /** the edge pattern this one is the reverse of */
    EdgePattern source;

    /** the edge pattern as written in the query */
    public EdgePattern original() {
      return source == null ? this : source;
    }

    public boolean quantified() {
      return !(min == 1 && max != null && max == 1);
    }

    public EdgePattern reversed() {
      EdgePattern r = new EdgePattern();
      r.variable = variable;
      r.labels = labels;
      r.direction = direction == Direction.RIGHT ? Direction.LEFT
          : direction == Direction.LEFT ? Direction.RIGHT : Direction.ANY;
      r.min = min;
      r.max = max;
      r.source = original();
      return r;
    }

    @Override
    public String toString() {
      String inner = (variable == null ? "" : name(variable))
          + (labels == null ? "" : ":" + labels(labels));
      String s;
      if (inner.isEmpty())
        s = direction == Direction.RIGHT ? "->" : direction == Direction.LEFT ? "<-" : "-";
      else
        s = (direction == Direction.LEFT ? "<-[" : "-[") + inner
            + (direction == Direction.RIGHT ? "]->" : "]-");
      if (quantified())
        s += max == null && min == 0 ? "*"
            : max == null && min == 1 ? "+"
                : max == null ? "{" + min + ",}"
                    : min == max ? "{" + min + "}" : "{" + min + "," + max + "}";
      return s;
    }
  }

  /** RETURN expression [AS name] */
  public static class ReturnItem {
    public Expr expr;
    public String name;

    @Override
    public String toString() {
      // RETURN a.`X` is named a.X: no AS needed when the name is the expression without quotes
      String e = expr.toString();
      return e.equals(name) || e.replace("`", "").equals(name) ? e : e + " AS " + name(name);
    }
  }

  public String pathVariable;
  public Selector selector = Selector.ALL;
  /** ANY k, SHORTEST k */
  public int k = 1;
  public Mode mode = Mode.WALK;
  public List<NodePattern> nodes = new ArrayList<>();
  /** edges.get(i) connects nodes.get(i) and nodes.get(i + 1) */
  public List<EdgePattern> edges = new ArrayList<>();
  public Expr where;
  public boolean distinct;
  public List<ReturnItem> ret = new ArrayList<>();
  public Integer limit;

  /** parses a query; arguments fill $name parameters and ${name} templates */
  public static GqlQuery parse(String query, Map<String, Object> arguments) {
    String text = arguments == null ? query : "" + Template.replace(query, arguments);
    GqlQuery q = new GqlParser(text, arguments).query();
    q.validate();
    return q;
  }

  /** rules the parser cannot check */
  void validate() {
    boolean unbounded = edges.stream().anyMatch(e -> e.max == null);
    if (unbounded && selector == Selector.ALL && mode == Mode.WALK)
      throw new IllegalArgumentException(
          "An unbounded quantifier (*, +, {m,}) needs a selector (ANY SHORTEST, ALL SHORTEST, ANY) or a path mode (TRAIL, ACYCLIC, SIMPLE)");
    for (EdgePattern e : edges)
      if (e.max != null && e.max < e.min)
        throw new IllegalArgumentException("Quantifier {" + e.min + "," + e.max + "}: the maximum is smaller than the minimum");
    List<String> vars = new ArrayList<>();
    for (NodePattern n : nodes)
      if (n.variable != null) {
        if (vars.contains(n.variable))
          throw new IllegalArgumentException("Variable " + n.variable + " is used twice");
        vars.add(n.variable);
      }
    for (EdgePattern e : edges)
      if (e.variable != null) {
        if (vars.contains(e.variable))
          throw new IllegalArgumentException("Variable " + e.variable + " is used twice");
        vars.add(e.variable);
      }
    if (pathVariable != null && vars.contains(pathVariable))
      throw new IllegalArgumentException("Variable " + pathVariable + " is used twice");
  }

  /** the pattern from its last node to its first (the variables keep their names) */
  public GqlQuery reversed() {
    GqlQuery r = new GqlQuery();
    r.pathVariable = pathVariable;
    r.selector = selector;
    r.k = k;
    r.mode = mode;
    for (int i = nodes.size() - 1; i >= 0; i--)
      r.nodes.add(nodes.get(i));
    for (int i = edges.size() - 1; i >= 0; i--)
      r.edges.add(edges.get(i).reversed());
    r.where = where;
    r.distinct = distinct;
    r.ret = ret;
    r.limit = limit;
    return r;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("MATCH ");
    if (pathVariable != null)
      s.append(name(pathVariable)).append(" = ");
    switch (selector) {
      case ALL:
        break;
      case ANY:
        s.append(k == 1 ? "ANY " : "ANY " + k + " ");
        break;
      case ANY_SHORTEST:
        s.append("ANY SHORTEST ");
        break;
      case ALL_SHORTEST:
        s.append("ALL SHORTEST ");
        break;
      case SHORTEST:
        s.append("SHORTEST " + k + " ");
        break;
    }
    if (mode != Mode.WALK)
      s.append(mode).append(" ");
    s.append(nodes.get(0));
    for (int i = 0; i < edges.size(); i++)
      s.append(edges.get(i)).append(nodes.get(i + 1));
    if (where != null)
      s.append(" WHERE ").append(where);
    s.append(" RETURN ").append(distinct ? "DISTINCT " : "");
    List<String> items = new ArrayList<>();
    for (ReturnItem i : ret)
      items.add(i.toString());
    s.append(String.join(", ", items));
    if (limit != null)
      s.append(" LIMIT ").append(limit);
    return s.toString();
  }

  static String labels(List<String> labels) {
    List<String> res = new ArrayList<>();
    for (String l : labels)
      res.add(name(l));
    return String.join("|", res);
  }

  /** names that are not plain identifiers (or are keywords) are written in backticks */
  static String name(String s) {
    if (s.matches("[A-Za-z_][A-Za-z0-9_]*") && !GqlParser.KEYWORDS.contains(s.toUpperCase()))
      return s;
    return "`" + s.replace("`", "``") + "`";
  }

  /* ========================================================================== expressions */

  /** WHERE conditions and RETURN items */
  public interface Expr {
    /** vars: the value of a variable (a record, an edge, a path, a list of edges) */
    Object eval(Function<String, Object> vars);
  }

  public static class Literal implements Expr {
    public final Object value;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      return value;
    }

    @Override
    public String toString() {
      return format(value);
    }

    static String format(Object v) {
      if (v == null)
        return "NULL";
      if (v instanceof String)
        return "'" + ((String) v).replace("\\", "\\\\").replace("'", "\\'") + "'";
      if (v instanceof Boolean)
        return ((Boolean) v) ? "TRUE" : "FALSE";
      if (v instanceof List) {
        List<String> items = new ArrayList<>();
        for (Object o : (List<?>) v)
          items.add(format(o));
        return "[" + String.join(", ", items) + "]";
      }
      return "" + v;
    }
  }

  /** a variable or a field of it: a, a.NAME, p.start.ID */
  public static class Ref implements Expr {
    public final String variable;
    public final List<String> fields;

    public Ref(String variable, List<String> fields) {
      this.variable = variable;
      this.fields = fields;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      Object current = vars.apply(variable);
      for (String f : fields)
        current = current instanceof Map ? ((Map<?, ?>) current).get(f) : null;
      return current;
    }

    @Override
    public String toString() {
      List<String> parts = new ArrayList<>();
      parts.add(name(variable));
      for (String f : fields)
        parts.add(name(f));
      return String.join(".", parts);
    }
  }

  public static class Compare implements Expr {
    public final String op;
    public final Expr left;
    public final Expr right;

    public Compare(String op, Expr left, Expr right) {
      this.op = op;
      this.left = left;
      this.right = right;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      Object l = left.eval(vars);
      Object r = right.eval(vars);
      if (op.equals("IN")) {
        if (l == null || !(r instanceof List))
          return null;
        for (Object x : (List<?>) r)
          if (Values.equal(l, x))
            return true;
        return false;
      }
      // comparisons with null are unknown (null), as in SQL
      if (l == null || r == null)
        return null;
      if (op.equals("="))
        return Values.equal(l, r);
      if (op.equals("<>"))
        return !Values.equal(l, r);
      Integer c = Values.compare(l, r);
      if (c == null)
        return null;
      switch (op) {
        case "<":
          return c < 0;
        case "<=":
          return c <= 0;
        case ">":
          return c > 0;
        default:
          return c >= 0;
      }
    }

    @Override
    public String toString() {
      return left + " " + op + " " + right;
    }
  }

  /** IS [NOT] NULL */
  public static class IsNull implements Expr {
    public final Expr expr;
    public final boolean not;

    public IsNull(Expr expr, boolean not) {
      this.expr = expr;
      this.not = not;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      return (expr.eval(vars) == null) != not;
    }

    @Override
    public String toString() {
      return expr + (not ? " IS NOT NULL" : " IS NULL");
    }
  }

  /** AND, OR (three-valued: null is unknown) */
  public static class Logical implements Expr {
    public final boolean and;
    public final Expr left;
    public final Expr right;

    public Logical(boolean and, Expr left, Expr right) {
      this.and = and;
      this.left = left;
      this.right = right;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      Object l = left.eval(vars);
      Object r = right.eval(vars);
      if (and) {
        if (Boolean.FALSE.equals(l) || Boolean.FALSE.equals(r))
          return false;
        return Boolean.TRUE.equals(l) && Boolean.TRUE.equals(r) ? true : null;
      }
      if (Boolean.TRUE.equals(l) || Boolean.TRUE.equals(r))
        return true;
      return Boolean.FALSE.equals(l) && Boolean.FALSE.equals(r) ? false : null;
    }

    @Override
    public String toString() {
      return "(" + left + (and ? " AND " : " OR ") + right + ")";
    }
  }

  public static class Not implements Expr {
    public final Expr expr;

    public Not(Expr expr) {
      this.expr = expr;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      Object v = expr.eval(vars);
      return v == null ? null : !Boolean.TRUE.equals(v);
    }

    @Override
    public String toString() {
      return "NOT " + expr;
    }
  }

  /** PATH_LENGTH(p): the number of edges of a path */
  public static class PathLength implements Expr {
    public final Expr path;

    public PathLength(Expr path) {
      this.path = path;
    }

    @Override
    public Object eval(Function<String, Object> vars) {
      Object p = path.eval(vars);
      if (!(p instanceof Map) || !(((Map<?, ?>) p).get("steps") instanceof List))
        return null;
      return ((List<?>) ((Map<?, ?>) p).get("steps")).size();
    }

    @Override
    public String toString() {
      return "PATH_LENGTH(" + path + ")";
    }
  }
}
