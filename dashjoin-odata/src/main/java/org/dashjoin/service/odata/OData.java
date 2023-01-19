package org.dashjoin.service.odata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Metadata;
import org.dashjoin.service.QueryEditorInternal;
import org.dashjoin.service.Metadata.Column;
import org.dashjoin.service.Metadata.MdTable;

/**
 * Experimental OData database driver implementation
 */
@JsonSchema(required = {"url"}, order = {"url", "username", "password"})
public class OData extends AbstractDatabase {

  @JsonSchema(choices = {"https://services.odata.org/V3/Northwind/Northwind.svc/"},
      style = {"width", "600px"})
  public String url;

  ODataClient client;

  ODataClient getClient() throws Exception {
    OData x = services.getConfig().getCachedForce(ID, getClass());
    if (x.client == null)
      throw new Exception("Database not yet initialized: " + ID);
    return x.client;
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {

    ODataEntitySetRequest<ClientEntitySet> request = getClient().getRetrieveRequestFactory()
        .getEntitySetRequest(getClient().newURIBuilder(url).appendEntitySetSegment(s.name).build());
    request.setAccept("application/atom+xml,application/xml");
    ODataRetrieveResponse<ClientEntitySet> response = request.execute();
    ClientEntitySet entitySet = response.getBody();

    List<Map<String, Object>> res = new ArrayList<>();
    for (ClientEntity e : entitySet.getEntities()) {
      Map<String, Object> row = new LinkedHashMap<>();
      for (ClientProperty p : e.getProperties()) {
        row.put(p.getName(), p.getValue().asPrimitive().toValue());
      }
      res.add(row);
    }
    return res;
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {

    client = ODataClientFactory.getClient();
    Edm edm = client.getRetrieveRequestFactory().getMetadataRequest(url).execute().getBody();

    // get tables
    Metadata meta = new Metadata();
    for (EdmSchema schema : edm.getSchemas()) {
      for (EdmEntityType entityType : schema.getEntityTypes()) {
        MdTable table = new MdTable(entityType.getName() + "s");
        meta.tables.put(table.name, table);

        // get columns
        EdmEntityType etype = edm.getEntityType(entityType.getFullQualifiedName());
        for (String propertyName : etype.getPropertyNames()) {
          Column col = new Column();
          col.name = propertyName;
          col.typeName = "VARCHAR";
          table.columns.add(col);
        }
      }
    }

    return meta.getTables(ID);
  }

  @Override
  public void close() throws Exception {

  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    throw new NotImplementedException();
  }
}
