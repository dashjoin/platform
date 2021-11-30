package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.ArangoDBQuery.Expression;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.Col;
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

public class ArangoDBEditor implements QueryEditorInternal {

  ArangoDB db;

  Services services;

  /**
   * construct with ref to DB
   */
  public ArangoDBEditor(Services services, ArangoDB db) {
    this.db = db;
    this.services = services;
  }

  @Override
  public QueryResponse rename(RenameRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse distinct(DistinctRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse sort(SortRequest ac) throws Exception {
    ArangoDBQuery q = new ArangoDBQuery(ac.query);
    if (ac.order == null || ac.order.isEmpty())
      q.sort = null;
    else {
      q.sort = q.variable + "." + ac.col.column + (ac.order.equals("desc") ? " DESC" : "");
    }
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse addColumn(AddColumnRequest ac) throws Exception {
    ArangoDBQuery q = new ArangoDBQuery(ac.query);
    if (ac.add.table.equals(ac.col.table))
      q.project.vars.put("\"" + ac.add.column + "\"", q.variable + "." + ac.add.column);
    else {
      throw new NotImplementedException("join not implemented");
    }
    ac.query = q.toString();
    return noop(ac);
  }

  @Override
  public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse setWhere(SetWhereRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public QueryResponse noop(QueryDatabase query) throws Exception {
    QueryResponse res = new QueryResponse();
    res.query = query.query;

    // call "new" query method to get the data (create a tmp catalog entry)
    QueryMeta info = new QueryMeta();
    info.query = res.query;
    info.type = "read";

    ArangoDBQuery parse = new ArangoDBQuery(query.query);
    Map<String, Property> x = db.queryMeta(info, null);

    // TODO: handle compatibilityError, e.g. for find() query
    res.compatibilityError = null;
    res.data = db.query(info, null);
    res.database = query.database;
    res.distinct = false;
    res.fieldNames = new ArrayList<>(x.keySet());

    Table table = db.tables.get(parse.collection);
    res.joinOptions = new ArrayList<>();

    // get missing columns of the current collection
    // TODO: use org.dashjoin.service.QueryEditorInternal.tableColumns()
    Map<String, Object> sample = res.data.size() > 0 && res.data.get(0).get("_key") != null
        ? db.read(table, of("_key", res.data.get(0).get("_key")))
        : of();
    for (Property p : table.properties.values())
      if (!res.fieldNames.contains(p.name)) {
        AddColumnRequest ac = new AddColumnRequest();
        ac.col = Col.col(parse.collection, p.name);
        ac.add = Col.col(parse.collection, p.name);
        ac.preview = sample == null ? "" : sample.get(p.name);
        res.joinOptions.add(ac);
      }

    res.limit = query.limit;
    res.metadata = new ArrayList<>();
    for (String f : res.fieldNames) {
      QueryColumn qc = new QueryColumn();
      qc.database = this.db.ID;
      qc.col = Col.col(parse.collection, f);
      if (x != null) {
        Property prop = x.get(f);
        qc.columnID = prop.ID;
        qc.keyTable = prop.pkpos == null ? null : parse.collection;
        if (prop.ref != null)
          qc.keyTable = prop.ref.split("/")[2];
      }
      for (Expression w : parse.filters)
        if (w.left.equals(f))
          // TODO: other types and operators / group by
          qc.where = "== \"" + w.right + "\"";
      res.metadata.add(qc);
    }

    // TODO: use org.dashjoin.service.QueryEditorInternal.joinColumns()
    for (QueryColumn qc : res.metadata) {
      if (qc.keyTable != null)
        if (!parse.collection.equals(qc.keyTable)) {
          // follow outgoing foreign keys
          AddColumnRequest ac = new AddColumnRequest();
          ac.col = qc.col;
          ac.add = new Col();
          ac.add.table = qc.keyTable;
          if (db.tables.get(qc.keyTable) != null)
            for (Property p : db.tables.get(qc.keyTable).properties.values())
              if (p.pkpos != null)
                ac.add.column = p.name;
          // TODO: get preview data
          ac.preview = "TODO";

          /*
           * if (parse.lookup.get("from") != null) if
           * (qc.keyTable.equals(parse.lookup.get("from").asString().getValue())) // column was
           * already added continue;
           */

          res.joinOptions.add(ac);
        }

        else {
          // follow incoming foreign keys
          // find a FK that points to this PK
          for (Table t : db.tables.values())
            for (Property p : t.properties.values())
              if (qc.columnID.equals(p.ref)) {
                AddColumnRequest ac = new AddColumnRequest();
                ac.col = qc.col;
                ac.add = Col.col(t.name, p.name);
                // TODO: get preview data
                ac.preview = "TODO";
                res.joinOptions.add(ac);
              }
        }
    }

    res.query = query.query;
    res.querylimit = parse.limit;
    res.distinct = false;

    return res;
  }

  @Override
  public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
    Table s = services.getConfig().getSchema(ac.table);
    QueryDatabase query = new QueryDatabase();
    query.query = "FOR i IN " + s.name + " RETURN {\"_key\": i._key}";
    query.limit = ac.limit;
    query.database = db.ID;
    return noop(query);
  }
}
