package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
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

/**
 * internal query editor API (equivalent of Database (internal) vs. Data (REST))
 */
public interface QueryEditorInternal {

  public QueryResponse rename(RenameRequest ac) throws Exception;

  public QueryResponse distinct(DistinctRequest ac) throws Exception;

  public QueryResponse sort(SortRequest ac) throws Exception;

  public QueryResponse addColumn(AddColumnRequest ac) throws Exception;

  public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception;

  public QueryResponse setWhere(SetWhereRequest ac) throws Exception;

  public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception;

  public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception;

  public QueryResponse noop(QueryDatabase query) throws Exception;

  public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception;

  /**
   * given the current result data, adds the missing table projections to the join options
   */
  static void tableColumns(AbstractDatabase db, QueryResponse res, Table table, String collection)
      throws Exception {
    Map<String, Object> sample = res.data.size() > 0 && res.data.get(0).get("ID") != null
        ? db.read(table, of("ID", res.data.get(0).get("ID")))
        : of();
    for (Property p : table.properties.values())
      if (!res.fieldNames.contains(p.name)) {
        AddColumnRequest ac = new AddColumnRequest();
        ac.col = Col.col(collection, p.name);
        ac.add = Col.col(collection, p.name);
        ac.preview = sample == null ? "" : sample.get(p.name);
        res.joinOptions.add(ac);
      }
  }

  /**
   * given the database schema, adds joinable columns from other tables
   */
  static void joinColumns(AbstractDatabase db, QueryResponse res, String collection,
      List<String> from) {
    for (QueryColumn qc : res.metadata) {
      if (qc.keyTable != null)
        if (!collection.equals(qc.keyTable)) {
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

          if (from != null)
            if (from.contains(qc.keyTable))
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
  }
}
