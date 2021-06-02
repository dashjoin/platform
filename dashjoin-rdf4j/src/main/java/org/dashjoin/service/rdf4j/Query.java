package org.dashjoin.service.rdf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.algebra.AbstractAggregateOperator;
import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.Bound;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.Distinct;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.Group;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.Not;
import org.eclipse.rdf4j.query.algebra.Order;
import org.eclipse.rdf4j.query.algebra.OrderElem;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.Regex;
import org.eclipse.rdf4j.query.algebra.Slice;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UnaryValueOperator;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

/**
 * represents an object structure for sparql queries
 */
public class Query {

  /**
   * base class for const / var
   */
  public abstract class Res {
    public String name;
  }

  /**
   * sparql variable
   */
  public class Variable extends Res {
    @Override
    public String toString() {
      return "?" + name;
    }
  }

  /**
   * represents an order by clause
   */
  public static class Ord {
    public Variable var;
    public boolean asc;

    @Override
    public String toString() {
      return (asc ? "?" : "desc(?") + var.name + (asc ? "" : ")");
    }
  }

  /**
   * variable that is assigned to an aggr fn
   */
  public class GroupByVariable extends Variable {

    public Res child;
    public String op;
    public boolean distinct;

    public GroupByVariable(String name, AbstractAggregateOperator extensionElem) {
      this.name = name;
      this.op = extensionElem.getClass().getSimpleName().toLowerCase();
      this.distinct = extensionElem.isDistinct();
      this.child = res(extensionElem.getArg());
    }

    public GroupByVariable(String name, String op, String child) {
      this.name = name;
      this.op = op;
      this.child = var(child);
    }

    @Override
    public String toString() {
      String inner = (distinct ? "distinct " : "") + child;
      return "(" + op + "(" + inner + ") as " + super.toString() + ")";
    }
  }

  /**
   * sparql quer constant
   */
  public class Const extends Res {
    public boolean literal;

    public Const(Value value) {
      literal = value instanceof org.eclipse.rdf4j.model.Literal;
      name = value.toString();
    }

    @Override
    public String toString() {
      return (literal ? "" : "<") + name + (literal ? "" : ">");
    }
  }

  /**
   * filter condition
   */
  public class Cond {
    public Cond(Compare e) {
      op = e.getOperator().getSymbol();
      left = res(e.getLeftArg());
      right = res(e.getRightArg());
    }

    public Cond(Res left, String op, Res right) {
      this.op = op;
      this.left = left;
      this.right = right;
    }

    public Res left;
    public Res right;
    public String op;

    @Override
    public String toString() {
      if (op.equals("regex"))
        return "regex(" + left + ", " + right + ")";
      if (op.equals("bound"))
        return "bound(" + left + ")";
      if (op.equals("!bound"))
        return "(!bound(" + left + "))";
      return "( " + left + " " + op + " " + right + " )";
    }
  }

  public Cond cond(String left, String op, Value right) {
    return new Cond(var(left), op, right == null ? null : new Const(right));
  }

  /**
   * triple statement
   */
  public class Stmt {
    Stmt(Res subject, Res predicate, Res object) {
      this.subject = subject;
      this.predicate = predicate;
      this.object = object;
    }

    public Stmt(StatementPattern pattern) {
      subject = res(pattern.getSubjectVar());
      predicate = res(pattern.getPredicateVar());
      object = res(pattern.getObjectVar());
    }

    public Res subject;
    public Res predicate;
    public Res object;

    @Override
    public String toString() {
      return subject + " " + predicate + " " + object;
    }
  }

  public List<Variable> projection = new ArrayList<>();
  public List<Variable> groupBy = new ArrayList<>();
  public List<Stmt> where = new ArrayList<>();
  public List<Cond> filter = new ArrayList<>();
  public Map<String, Variable> vars = new HashMap<>();
  public List<Ord> order = new ArrayList<>();
  public boolean distinct;
  public Long limit;

  public Query(String query) {
    TupleExpr pt =
        QueryParserUtil.parseTupleQuery(QueryLanguage.SPARQL, query, null).getTupleExpr();
    if (pt instanceof Slice) {
      limit = ((Slice) pt).getLimit();
      pt = ((Slice) pt).getArg();
    }
    if (pt instanceof Distinct) {
      distinct = true;
      pt = ((Distinct) pt).getArg();
    }
    Projection q = (Projection) pt;
    for (ProjectionElem x : q.getProjectionElemList().getElements())
      if (x.getSourceExpression() != null
          && x.getSourceExpression().getExpr() instanceof UnaryValueOperator)
        projection.add(newGroupByVariable(x.getSourceName(),
            (AbstractAggregateOperator) x.getSourceExpression().getExpr()));
      else
        projection.add(var(x.getSourceName()));
    parse(q.getArg());
  }

  void parse(Filter filter) {
    parse(filter.getArg());
    parse(filter.getCondition());
  }

  void parse(ValueExpr condition) {
    if (condition instanceof Compare)
      filter.add(new Cond((Compare) condition));
    else if (condition instanceof Bound) {
      filter.add(new Cond(res(((Bound) condition).getArg()), "bound", null));
    } else if (condition instanceof Not) {
      condition = ((Not) condition).getArg();
      filter.add(new Cond(res(((Bound) condition).getArg()), "!bound", null));
    } else if (condition instanceof Regex) {
      filter.add(new Cond(res(((Regex) condition).getLeftArg()), "regex",
          res(((Regex) condition).getRightArg())));
    } else if (condition instanceof And) {
      parse(((And) condition).getLeftArg());
      parse(((And) condition).getRightArg());
    } else
      throw new RuntimeException(condition + "");
  }

  void parse(StatementPattern pattern) {
    where.add(new Stmt(pattern));
  }

  void parse(TupleExpr o) {
    if (o instanceof StatementPattern)
      parse((StatementPattern) o);
    else if (o instanceof Filter)
      parse((Filter) o);
    else if (o instanceof Order) {
      for (OrderElem or : ((Order) o).getElements()) {
        Ord e = new Ord();
        e.var = var(((Var) or.getExpr()).getName());
        e.asc = or.isAscending();
        order.add(e);
      }
      parse(((Order) o).getArg());
    } else if (o instanceof Extension)
      parse(((Extension) o).getArg());
    else if (o instanceof Group) {
      for (String ge : ((Group) o).getGroupBindingNames())
        groupBy.add(var(ge));
      parse(((Group) o).getArg());
    } else if (o instanceof Join) {
      parse(((Join) o).getLeftArg());
      parse(((Join) o).getRightArg());
    } else
      throw new RuntimeException(o + "");
  }

  public Variable var(String name) {
    if (vars.containsKey(name))
      return vars.get(name);
    Variable var = new Variable();
    var.name = name;
    vars.put(name, var);
    return var;
  }

  public GroupByVariable var(String name, String op, String child) {
    if (vars.containsKey(name))
      return (GroupByVariable) vars.get(name);
    GroupByVariable var = new GroupByVariable(name, op, child);
    var.name = name;
    vars.put(name, var);
    return var;
  }

  GroupByVariable newGroupByVariable(String name, AbstractAggregateOperator unaryValueOperator) {
    if (vars.containsKey(name))
      return (GroupByVariable) vars.get(name);
    GroupByVariable var = new GroupByVariable(name, unaryValueOperator);
    vars.put(name, var);
    return var;
  }

  Res res(Var var) {
    return var.isConstant() ? new Const(var.getValue()) : var(var.getName());
  }

  Res res(ValueExpr var) {
    if (var instanceof Var)
      return res((Var) var);
    if (var instanceof ValueConstant)
      return new Const(((ValueConstant) var).getValue());
    throw new RuntimeException(var + "");
  }

  /**
   * convert pojo to sparql query
   */
  @Override
  public String toString() {
    StringBuffer res = new StringBuffer();
    res.append("select ");
    if (distinct)
      res.append("distinct ");
    for (Variable p : projection)
      res.append(p + " ");
    res.append("\nwhere {\n");
    for (Stmt w : where) {
      res.append("  ");
      res.append(w.toString());
      if (!w.equals(where.get(where.size() - 1)))
        res.append(" . ");
      res.append("\n");
    }

    if (!filter.isEmpty())
      res.append("  filter ");
    if (filter.size() > 1)
      res.append("(");
    for (Cond w : filter) {
      res.append(w.toString());
      if (!w.equals(filter.get(filter.size() - 1)))
        res.append(" && ");
    }
    if (filter.size() > 1)
      res.append(")");
    if (!filter.isEmpty())
      res.append("\n");
    res.append("}");

    if (!groupBy.isEmpty())
      res.append("\ngroup by ");
    res.append(
        String.join(",", groupBy.stream().map(e -> e.toString()).collect(Collectors.toList())));

    if (!order.isEmpty()) {
      res.append("\norder by ");
      res.append(
          String.join(",", order.stream().map(e -> e.toString()).collect(Collectors.toList())));
    }

    if (limit != null)
      res.append("\nlimit " + limit);
    return res.toString();
  }

  /**
   * rename var
   */
  public void rename(String column, String name) {
    Variable v = vars.get(column);
    if (v != null) {
      v.name = name;
      vars.remove(column);
      vars.put(name, v);
    }
  }

  /**
   * join new column
   */
  public void join(String column, String add) {
    if (add.startsWith("(inv) ")) {
      IRI ai = SimpleValueFactory.getInstance().createIRI(add.substring("(inv) ".length()));
      Stmt s = new Stmt(newVar(ai.getLocalName()), new Const(ai), var(column));
      where.add(s);
      projection.add((Variable) s.subject);
    } else {
      IRI ai = SimpleValueFactory.getInstance().createIRI(add);
      Stmt s = new Stmt(var(column), new Const(ai), newVar(ai.getLocalName()));
      where.add(s);
      projection.add((Variable) s.object);
    }
  }

  /**
   * set order by
   */
  public void orderBy(String column, String orderString) {
    if (orderString == null || orderString.isEmpty()) {
      order = Arrays.asList();
      return;
    }

    Ord ord = new Ord();
    ord.var = var(column);
    ord.asc = orderString.equals("asc");
    order = Arrays.asList(ord);
  }

  /**
   * lookup / create var
   */
  Variable newVar(String add) {
    if (vars.containsKey(add))
      return newVar(add + "_");
    else
      return var(add);
  }
}
