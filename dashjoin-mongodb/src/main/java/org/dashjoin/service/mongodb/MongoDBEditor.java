package org.dashjoin.service.mongodb;

import static com.google.common.collect.ImmutableMap.of;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * mongo DB query editor implementation
 */
public class MongoDBEditor implements QueryEditorInternal {

  /**
   * ref to the DB
   */
  MongoDB db;

  Services services;

  /**
   * construct with ref to DB
   */
  public MongoDBEditor(Services services, MongoDB db) {
    this.db = db;
    this.services = services;
  }

  /**
   * rename column
   */
  @Override
  public QueryResponse rename(RenameRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    BsonValue name = q.project.remove(ac.col.column);
    if (ac.name.isEmpty())
      q.project.put(name.asString().getValue().substring(1), name);
    else
      q.project.put(ac.name, name);
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * sort column
   */
  @Override
  public QueryResponse sort(SortRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    q.orderBy(ac.col.column, ac.order);
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * add a projection or join (lookup)
   */
  @Override
  public QueryResponse addColumn(AddColumnRequest ac) throws Exception {

    MongoDBQuery q = new MongoDBQuery(ac.query);
    if (ac.add.table.equals(ac.col.table))
      q.project.append(ac.add.column, new BsonString("$" + ac.add.column));
    else {
      // need a join
      // $lookup:{from:\"emp\", localField:\"manager\", foreignField:\"_id\", as:\"join\"}
      String newname = "" + ac.add;
      q.lookup = new BsonDocument().append("from", new BsonString(ac.add.table))
          .append("localField", new BsonString(ac.col.column))
          .append("foreignField", new BsonString(ac.add.column))
          .append("as", new BsonString(newname));
      q.project.append(newname, new BsonString("$" + newname));
    }
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * rename projection
   */
  @Override
  public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    q.project.remove(ac.col.column);
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * change projection order
   */
  @Override
  public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    int idx = 0;
    List<Entry<String, BsonValue>> list = new ArrayList<>();
    for (Entry<String, BsonValue> e : q.project.entrySet()) {
      list.add(e);
    }

    for (Entry<String, BsonValue> p : list) {
      if (p.getKey().equals(ac.col.column)) {
        Entry<String, BsonValue> tmp = list.get(ac.position);
        list.set(idx, tmp);
        list.set(ac.position, p);
        break;
      }
      idx++;
    }

    q.project = new BsonDocument();
    for (Entry<String, BsonValue> e : list)
      q.project.put(e.getKey(), e.getValue());

    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * set where condition
   */
  @Override
  public QueryResponse setWhere(SetWhereRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    q.match = new BsonDocument();
    for (ColCondition w : ac.cols)
      if (w.condition != null) {
        if (w.condition.startsWith("="))
          q.match.append(w.col.column, parse(w.condition.substring(1)));
        // TODO: other types and operators (just copied and pasted from RDF...)
        else if (w.condition.startsWith("<="))
          q.match.append(w.col.column, new BsonDocument("<=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith(">="))
          q.match.append(w.col.column, new BsonDocument(">=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith("!=") || w.condition.startsWith("<>"))
          q.match.append(w.col.column, new BsonDocument("!=", parse(w.condition.substring(2))));
        else if (w.condition.startsWith("LIKE"))
          q.match.append(w.col.column, new BsonDocument("regex", parse(w.condition.substring(4))));
        else if (w.condition.toLowerCase().startsWith("regex"))
          q.match.append(w.col.column, new BsonDocument("regex", parse(w.condition.substring(5))));
        else if (w.condition.toLowerCase().startsWith("is null")
            || w.condition.toLowerCase().startsWith("!bound"))
          q.match.append(w.col.column, new BsonDocument("!bound", null));
        else if (w.condition.toLowerCase().startsWith("is not null")
            || w.condition.toLowerCase().startsWith("bound"))
          q.match.append(w.col.column, new BsonDocument("bound", null));
        else
          throw new Exception("Operator not supported: " + w.condition);
      }
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * parsing utility
   */
  BsonValue parse(String s) {
    s = s.trim();
    if (s.startsWith("'") && s.endsWith("'"))
      return new BsonString(s.substring(1, s.length() - 1));
    return new BsonInt32(Integer.parseInt(s));
  }

  /**
   * set group by
   */
  @Override
  public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
    MongoDBQuery q = new MongoDBQuery(ac.query);
    q.project = new BsonDocument();
    q.group = new BsonDocument();
    for (ColCondition c : ac.cols) {
      if (c.condition.equals("GROUP BY"))
        q.group.append("_id", new BsonString("$" + c.col.column));
      if (c.condition.equals("COUNT"))
        q.group.append("count" + c.col.column, new BsonDocument("$sum", new BsonInt32(1)));
      // TODO: other aggregations
    }
    ac.query = q.toString();
    return noop(ac);
  }

  /**
   * main query and metadata API
   */
  @Override
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"},
      justification = "false positive")
  public QueryResponse noop(QueryDatabase query) throws Exception {
    QueryResponse res = new QueryResponse();
    res.query = query.query;

    // call "new" query method to get the data (create a tmp catalog entry)
    QueryMeta info = new QueryMeta();
    info.query = res.query;
    info.type = "read";

    MongoDBQuery parse = new MongoDBQuery(query.query);
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
    Map<String, Object> sample = res.data.size() > 0 && res.data.get(0).get("_id") != null
        ? db.read(table, of("_id", res.data.get(0).get("_id")))
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
          // only include ref if it is inside the same DB (cannot join to other DBs)
          if (prop.ref.split("/")[1].equals(db.name))
            qc.keyTable = prop.ref.split("/")[2];
      }
      for (Entry<String, BsonValue> w : parse.match.entrySet())
        if (w.getKey().equals(f))
          // TODO: other types and operators / group by
          qc.where = "= '" + w.getValue().asString().getValue() + "'";
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

          if (parse.lookup.get("from") != null)
            if (qc.keyTable.equals(parse.lookup.get("from").asString().getValue()))
              // column was already added
              continue;

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
    res.querylimit = parse.limit == null ? null : parse.limit.asInt32().getValue();
    res.distinct = false;

    return res;
  }

  /**
   * initial query on collection
   */
  @Override
  public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
    Table s = services.getConfig().getSchema(ac.table);
    QueryDatabase query = new QueryDatabase();
    query.query = "db.['" + s.name + "'].aggregate([{$project:{_id: \"$_id\"}}])";
    query.limit = ac.limit;
    query.database = db.ID;
    return noop(query);
  }

  /**
   * set distinct
   */
  @Override
  public QueryResponse distinct(DistinctRequest ac) throws Exception {
    if (Boolean.TRUE.equals(ac.distinct))
      throw new RuntimeException("Distinct is not supported on MongoDB");
    MongoDBQuery q = new MongoDBQuery(ac.query);
    q.limit = ac.querylimit == null ? null : new BsonInt32(ac.querylimit);
    ac.query = q.toString();
    return noop(ac);
  }
}
