package org.dashjoin.service.rdf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.ColCondition;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryColumn;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.dashjoin.service.QueryEditorInternal;
import org.dashjoin.service.Services;
import org.dashjoin.service.rdf4j.Query.Cond;
import org.dashjoin.service.rdf4j.Query.GroupByVariable;
import org.dashjoin.service.rdf4j.Query.Stmt;
import org.dashjoin.service.rdf4j.Query.Variable;
import org.dashjoin.util.Escape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * enables query editor support for SPARQL
 */
public class RDF4JEditor implements QueryEditorInternal {

  RDF4J db;

  Services services;

  public RDF4JEditor(Services services, RDF4J db) {
    this.db = db;
    this.services = services;
  }

  @Override
  public QueryResponse rename(RenameRequest ac) throws Exception {
    Query q = new Query(ac.query);
    q.rename(ac.col.column, ac.name);
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse sort(SortRequest ac) throws Exception {
    Query q = new Query(ac.query);
    q.orderBy(ac.col.column, ac.order);
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse addColumn(AddColumnRequest ac) throws Exception {
    Query q = new Query(ac.query);
    q.join(ac.col.column, ac.add.column);
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception {
    Query q = new Query(ac.query);
    for (Variable p : q.projection) {
      if (p.name.equals(ac.col.column)) {
        q.projection.remove(p);
        break;
      }
    }
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception {
    Query q = new Query(ac.query);
    int idx = 0;
    for (Variable p : q.projection) {
      if (p.name.equals(ac.col.column)) {
        Variable tmp = q.projection.get(ac.position);
        q.projection.set(idx, tmp);
        q.projection.set(ac.position, p);
      }
      idx++;
    }
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse setWhere(SetWhereRequest ac) throws Exception {
    Query q = new Query(ac.query);
    q.filter = new ArrayList<>();
    for (ColCondition w : ac.cols)
      if (w.condition != null) {
        if (w.condition.startsWith("="))
          q.filter.add(q.cond(w.col.column, "=", parse(w.condition.substring(1))));
        else if (w.condition.startsWith("<="))
          q.filter.add(q.cond(w.col.column, "<=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith(">="))
          q.filter.add(q.cond(w.col.column, ">=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith("!=") || w.condition.startsWith("<>"))
          q.filter.add(q.cond(w.col.column, "!=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith("LIKE"))
          q.filter.add(q.cond(w.col.column, "regex", parse(w.condition.substring(4))));
        else if (w.condition.toLowerCase().startsWith("regex"))
          q.filter.add(q.cond(w.col.column, "regex", parse(w.condition.substring(5))));
        else if (w.condition.toLowerCase().startsWith("is null")
            || w.condition.toLowerCase().startsWith("!bound"))
          q.filter.add(q.cond(w.col.column, "!bound", null));
        else if (w.condition.toLowerCase().startsWith("is not null")
            || w.condition.toLowerCase().startsWith("bound"))
          q.filter.add(q.cond(w.col.column, "bound", null));
        else
          throw new Exception("Operator not supported: " + w.condition);
      }
    ac.query = q.toString();
    return noop(ac);
  }

  Value parse(String s) {
    s = s.trim();
    s = s.substring(1, s.length() - 1);

    // TODO: need proper metadata here
    if (s.contains(":"))
      return SimpleValueFactory.getInstance().createIRI(s);
    else
      return SimpleValueFactory.getInstance().createLiteral(s);
  }

  @Override
  public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
    Query q = new Query(ac.query);
    q.groupBy.clear();
    q.order.clear();
    for (ColCondition w : ac.cols) {
      if ("GROUP BY".equals(w.condition))
        q.groupBy.add(q.var(w.col.column));
      else {
        for (Variable p : q.projection)
          if (p.name.equals(w.col.column)) {
            int idx = q.projection.indexOf(p);
            q.projection.set(idx,
                q.var(w.col.column + "_" + w.condition, w.condition, w.col.column));
          }
      }
    }
    ac.query = q.toString();
    return noop(ac);
  }

  @SuppressWarnings("deprecation")
  @Override
  public QueryResponse noop(QueryDatabase query) throws Exception {
    QueryResponse res = new QueryResponse();
    res.query = query.query;

    // call "new" query method to get the data (create a tmp catalog entry)
    QueryMeta info = new QueryMeta();
    info.query = res.query;
    info.type = "read";

    Query parse = new Query(res.query);
    Map<String, Property> x = db.queryMeta(info, null);

    // TODO: handle compatibility (parse error in new Query above)
    res.compatibilityError = null;
    res.data = db.query(info, null);
    res.database = query.database;
    res.distinct = parse.distinct;
    res.fieldNames = parse.projection.stream().map(e -> e.name).collect(Collectors.toList());

    res.joinOptions = new ArrayList<>();
    try (RepositoryConnection con = db.getConnection()) {
      if (x != null)
        for (Property p : x.values()) {
          if (p.pkpos != null) {
            Object value = res.data.get(0).get(p.name);
            IRI iri = db.iri(value);

            try (RepositoryResult<Statement> out = con.getStatements(iri, null, null)) {
              IRI type = db.getType(iri);
              Set<IRI> preds = new HashSet<>();
              while (out.hasNext()) {
                Statement stmt = out.next();

                if (!preds.add(stmt.getPredicate()))
                  continue;

                AddColumnRequest ac = new AddColumnRequest();
                ac.col = new Col();
                ac.col.column = p.name;
                ac.add = new Col();
                ac.add.table = type == null ? "out" : type.toString();
                ac.add.column = stmt.getPredicate().stringValue();
                ac.preview = db.object(stmt.getObject());

                boolean alreadyPresent = false;
                for (Stmt m : parse.where)
                  if (m.subject.name.equals(p.name))
                    if (m.predicate.name.equals(stmt.getPredicate().toString()))
                      alreadyPresent = true;

                if (!alreadyPresent)
                  res.joinOptions.add(ac);
              }
            }

            try (RepositoryResult<Statement> in = con.getStatements(null, null, iri)) {
              Set<IRI> preds = new HashSet<>();
              while (in.hasNext()) {
                Statement stmt = in.next();

                if (!preds.add(stmt.getPredicate()))
                  continue;

                IRI type = db.getType(stmt.getSubject());
                AddColumnRequest ac = new AddColumnRequest();
                ac.col = new Col();
                ac.col.column = p.name;
                ac.add = new Col();
                ac.add.table = type == null ? "in" : type.toString();
                ac.add.column = "(inv) " + stmt.getPredicate().stringValue();
                ac.preview = db.object(stmt.getSubject());

                boolean alreadyPresent = false;
                for (Stmt m : parse.where)
                  if (m.object.name.equals(p.name))
                    if (m.predicate.name.equals(stmt.getPredicate().toString()))
                      alreadyPresent = true;

                if (!alreadyPresent)
                  res.joinOptions.add(ac);
              }
            }

            String tbl = Escape.parseTableID(p.parent)[2];
            if (tbl != null) {
              Table tb = db.tables.get(tbl);
              if (tb != null && tb.properties != null)
                for (Property tp : tb.properties.values()) {
                  AddColumnRequest ac = new AddColumnRequest();
                  ac.col = new Col();
                  ac.col.column = p.name;
                  ac.add = new Col();
                  ac.add.table = tb.name;
                  ac.add.column = tp.name;

                  boolean alreadyPresent = false;
                  for (Stmt m : parse.where)
                    if (m.object.name.equals(p.name))
                      if (m.predicate.name.equals(ac.add.column))
                        alreadyPresent = true;

                  if (!alreadyPresent)
                    res.joinOptions.add(ac);
                }
            }
          }
        }
    }

    res.limit = query.limit;
    res.metadata = new ArrayList<>();
    for (String f : res.fieldNames) {
      QueryColumn qc = new QueryColumn();
      qc.database = this.db.ID;
      qc.col = new Col();
      qc.col.column = f;
      if (x != null)
        // TODO: make sure we're within the same DB
        qc.keyTable = x.get(f).pkpos == null ? null : Escape.parseTableID(x.get(f).parent)[2];
      for (Cond w : parse.filter)
        if (w.left.name.equals(f))
          qc.where = w.op + " " + (w.right == null ? "" : w.right);
      res.metadata.add(qc);
    }
    res.query = query.query;
    res.querylimit = parse.limit == null ? null : parse.limit.intValue();

    return res;
  }

  @Override
  public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
    Table s = services.getConfig().getSchema(ac.table);
    QueryDatabase query = new QueryDatabase();
    IRI iri = db.iri(s);
    query.query =
        "select ?" + iri.getLocalName() + " where { ?" + iri.getLocalName() + " a <" + iri + "> }";
    query.limit = ac.limit;
    query.database = db.ID;
    return noop(query);
  }

  @Override
  public QueryResponse distinct(DistinctRequest ac) throws Exception {
    Query q = new Query(ac.query);
    if (q.groupBy.isEmpty())
      q.distinct = ac.distinct == null ? false : ac.distinct;
    else {
      for (Variable v : q.projection)
        if (v instanceof GroupByVariable) {
          GroupByVariable g = (GroupByVariable) v;
          g.distinct = ac.distinct;
        }
    }
    q.limit = ac.querylimit == null ? null : ac.querylimit.longValue();
    ac.query = q.toString();
    return noop(ac);
  }
}
