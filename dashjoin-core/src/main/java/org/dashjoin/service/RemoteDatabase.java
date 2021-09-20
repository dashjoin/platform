package org.dashjoin.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import org.dashjoin.function.RestJson;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.QueryEditor.AddColumnRequest;
import org.dashjoin.service.QueryEditor.DistinctRequest;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.MoveColumnRequest;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.service.QueryEditor.RemoveColumnRequest;
import org.dashjoin.service.QueryEditor.RenameRequest;
import org.dashjoin.service.QueryEditor.SetWhereRequest;
import org.dashjoin.service.QueryEditor.SortRequest;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.MapUtil;

/**
 * Client for remote databases
 */
@JsonSchema(required = {"url"}, order = {"url", "username", "password"})
@SuppressWarnings("unchecked")
public class RemoteDatabase extends AbstractDatabase {

  /**
   * service URL
   */
  @JsonSchema(style = {"width", "400px"})
  public String url;

  /**
   * optional HTTP basic authentication user name
   */
  @JsonSchema(style = {"width", "400px"})
  public String username;

  /**
   * optional HTTP basic authentication password
   */
  @JsonSchema(widget = "password", style = {"width", "400px"})
  public String password;

  /**
   * uses RestJson to communicate with the server. In case a "Schema not set" error is detected,
   * setSchema is called and the call is repeated
   */
  Object call(String method, Object arguments) throws Exception {
    RestJson client = new RestJson();
    client.url = url + "/" + method;
    client.username = username;
    client.password = password;
    client.method = "POST";
    client.contentType = "application/json";
    client.headers = MapUtil.of("Accept", "application/json");
    try {
      return client.run(arguments);
    } catch (WebApplicationException e) {
      if (e.getResponse().getStatus() == 572) {
        call("setSchema/" + e(ID), services.getConfig().getDatabase(ID).tables);
        return client.run(arguments);
      } else
        throw e;
    }
  }

  String e(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return (List<Map<String, Object>>) call("query",
        MapUtil.of("query", info.query, "arguments", arguments));
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return (Map<String, Property>) call("queryMeta",
        MapUtil.of("query", info.query, "arguments", arguments));
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    String u = "all/" + e(s.name) + "?";
    if (offset != null)
      u = u + "offset=" + offset + "&";
    if (limit != null)
      u = u + "limit=" + limit + "&";
    if (sort != null)
      u = u + "sort=" + sort + "&";
    u = u + "descending=" + descending;
    return (List<Map<String, Object>>) call(u, arguments);
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    object.putAll((Map<String, Object>) call("create/" + e(m.name), object));
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    return (Map<String, Object>) call("read/" + e(s.name), search);
  }

  @Override
  public boolean update(Table s, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    return (boolean) call("update/" + e(s.name), MapUtil.of("search", search, "object", object));
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    return (boolean) call("delete/" + e(s.name), search);
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    return (Map<String, Object>) call("connectAndCollectMetadata/" + e(ID), null);
  }

  @Override
  public void close() throws Exception {
    call("close", null);
  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new RemoteDatabaseEditor();
  }

  @Override
  public SchemaChange getSchemaChange() {
    return new RemoteSchemaChange();
  }

  public class RemoteDatabaseEditor implements QueryEditorInternal {

    @Override
    public QueryResponse rename(RenameRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse distinct(DistinctRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse sort(SortRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse addColumn(AddColumnRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse removeColumn(RemoveColumnRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse setWhere(SetWhereRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse setGroupBy(SetWhereRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse moveColumn(MoveColumnRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse noop(QueryDatabase query) throws Exception {
      throw new UnsupportedOperationException();
    }

    @Override
    public QueryResponse getInitialQuery(InitialQueryRequest ac) throws Exception {
      throw new UnsupportedOperationException();
    }
  }

  public class RemoteSchemaChange implements SchemaChange {

    @Override
    public void createTable(String table, String keyName, String keyType) throws Exception {
      if (keyName == null)
        call("createTable/" + e(table), null);
      else
        call("createTable/" + e(table) + "/" + e(keyName) + "/" + e(keyType), null);
    }

    @Override
    public void dropTable(String table) throws Exception {
      call("dropTable/" + e(table), null);
    }

    @Override
    public void renameTable(String table, String newName) throws Exception {
      call("renameTable/" + e(table) + "/" + e(newName), null);
    }

    @Override
    public void createColumn(String table, String columnName, String columnType) throws Exception {
      call("createColumn/" + e(table) + "/" + e(columnName) + "/" + e(columnType), null);
    }

    @Override
    public void renameColumn(String table, String column, String newName) throws Exception {
      call("renameColumn/" + e(table) + "/" + e(column) + "/" + e(newName), null);
    }

    @Override
    public void alterColumn(String table, String column, String newType) throws Exception {
      call("alterColumn/" + e(table) + "/" + e(column) + "/" + e(newType), null);
    }

    @Override
    public void dropColumn(String table, String column) throws Exception {
      call("dropColumn/" + e(table) + "/" + e(column), null);
    }
  }
}
