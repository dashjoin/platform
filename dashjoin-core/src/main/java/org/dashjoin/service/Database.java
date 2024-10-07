package org.dashjoin.service;

import java.util.List;
import java.util.Map;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Choice;
import org.dashjoin.service.Data.ColInfo;
import org.dashjoin.service.Data.SearchResult;
import org.dashjoin.service.ddl.SchemaChange;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Database implementation interface. Sits below the Data REST service. Database instances are
 * instantiated using configuration data located from the dj-database table in the config database.
 */
public interface Database {

  /**
   * runs a full text search. returns columns name (nice display name), url (starting with
   * /resource/), field (property where the match occurred), match (the actual string match)
   */
  default public List<SearchResult> search(@Context SecurityContext sc, String search,
      Integer limit) throws Exception {
    return null;
  };

  /**
   * like search but for a single table
   */
  default public List<SearchResult> search(@Context SecurityContext sc, Table t, String search,
      Integer limit) throws Exception {
    return null;
  };

  /**
   * runs that query defined in info using the arguments. Returns the query result
   */
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception;

  /**
   * runs that query defined in info using the arguments. Returns the query result
   */
  default public List<Map<String, Object>> queryGraph(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    return null;
  }

  /**
   * returns the column metadata for the query
   */
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception;

  /**
   * return the entire contents of the table (select * from t)
   * 
   * @param s table to operate on
   * @param offset optional offset (null means no offset)
   * @param limit optional limit (null means no limit)
   * @param arguments optional search arguments that the records must match
   * @return list of matching records
   * @throws Exception
   */
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception;

  /**
   * generates ad hoc analytics queries without having to rely on the query editor
   */
  default public String analytics(QueryMeta info, Table s, List<ColInfo> arguments) {
    return null;
  }

  /**
   * create object in schema
   */
  public void create(Table m, Map<String, Object> object) throws Exception;

  /**
   * read object with the keys defined in search
   */
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception;

  /**
   * update object with the keys defined in search and set the key value pairs defined in object
   * 
   * @param schema the table we're updating
   * @param search a map of keys required to locate the record to be updated - if the table only has
   *        a single key (ID), this map is {ID=x}
   * @param object a map of keys that are set to new values. Columns / keys that are omitted are not
   *        touched. If key=null, the key is deleted. All other keys are replaced (no merging takes
   *        place
   * @return true if the record was found, false otherwise
   * @throws Exception
   */
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception;

  /**
   * delete object with the keys defined in search
   */
  public boolean delete(Table s, Map<String, Object> search) throws Exception;

  /**
   * parses the query and returns the list of classes / tables the query operates on
   */
  public List<String> getTablesInQuery(String query) throws Exception;

  /**
   * return limit keys whose string representation begin with prefix
   */
  default public List<Choice> keys(Table schema, String prefix, Integer limit,
      Map<String, Object> arguments) throws Exception {
    return null;
  }

  /**
   * connects to the DB and collects the database. The framework will place the connected instance
   * into the cache. Therefore, the tables field does not need to be set. It is fed into the config
   * DB via the ProviderDatabase which is configured with the output of this method.
   */
  public Map<String, Object> connectAndCollectMetadata() throws Exception;

  /**
   * called by the framework when the database is deleted or edited. Allows implementations to shut
   * down connections using the previous connection data
   */
  public void close() throws Exception;

  /**
   * get the associated query editor for this DB implementation
   */
  public QueryEditorInternal getQueryEditor();

  default public SchemaChange getSchemaChange() {
    throw new RuntimeException("Schema changes are not supported on this database");
  }
}
