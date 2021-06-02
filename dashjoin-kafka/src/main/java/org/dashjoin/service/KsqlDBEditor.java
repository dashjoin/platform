package org.dashjoin.service;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.Col;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.QueryColumn;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * kafka / ksql DB query editor implementation
 */
public class KsqlDBEditor extends SQLEditor {

  public KsqlDBEditor(Services services, KsqlDB ksqlDB) {
    super(services, ksqlDB);
  }

  @Override
  public QueryResponse noop(QueryDatabase query) throws Exception {
    QueryResponse res = super.noop(query);
    res.query = KsqlDB.prepare(res.query);
    return res;
  }

  @Override
  void samplesAndMetadata(QueryResponse res, Map<Table, Col> tables) throws SQLException {
    for (Entry<Table, Col> t : tables.entrySet())
      for (Property p : t.getKey().properties.values()) {
        boolean present = false;
        for (QueryColumn md : res.metadata)
          if (md.col.column.equals(p.name))
            present = true;
        if (!present) {
          AddColumnRequest e = new AddColumnRequest();
          e.preview = "unknown";
          e.add = Col.col(t.getKey().name, p.name);
          e.col = t.getValue();
          res.joinOptions.add(e);
        }
      }
  }

  @Override
  public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
    Table s = services.getConfig().getSchema(ac.table);
    String clazz = s.name;
    Table t = db.tables.get(clazz);
    Iterator<String> i = t.properties.keySet().iterator();
    String query = "select " + clazz + "." + i.next() + "";
    if (i.hasNext())
      query = query + ", " + clazz + "." + i.next();
    query = query + " from " + clazz + "";
    // TODO:
    query = query + " where AGG.PROFILEID='4a7c7b41' ";

    Select stmt = (Select) CCJSqlParserUtil.parse(query);
    PlainSelect body = (PlainSelect) stmt.getSelectBody();
    return prettyPrint(db.ID, body, ac.limit);
  }

  @Override
  public QueryResponse sort(SortRequest ac) throws Exception {
    throw new Exception("Order by is not supported");
  }

  @Override
  public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
    throw new Exception("Group by is not supported");
  }
}
