package org.dashjoin.service;

import java.util.Collection;
import java.util.List;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;

/**
 * the main config database is an instance of PojoDatabase. It has two roles:
 * 
 * 1) provide pojo access to the databases, query catalog and schema
 * 
 * 2) it also is a "normal" database which maintains its state in json files, the classpath and via
 * providers
 * 
 * The latter is dynamic and must not be accessed via Services.getConfig().tables. Therefore, this
 * interface "protects" the PojoDatabase object and exposes only the required management methods.
 */
public interface Config {

  /**
   * get the actual config database in order to access the fields (specifically tables)
   */
  AbstractDatabase getConfigDatabase() throws Exception;

  /**
   * get database by id
   */
  AbstractDatabase getDatabase(String dj) throws Exception;

  /**
   * get all databases
   */
  List<AbstractDatabase> getDatabases() throws Exception;

  /**
   * get database by id
   */
  AbstractConfigurableFunction<Object, Object> getFunction(String id) throws Exception;

  /**
   * get query from catalog
   */
  QueryMeta getQueryMeta(String queryId) throws Exception;

  /**
   * get a table from the db schemata
   */
  Table getSchema(String clazz) throws Exception;

  /**
   * add provider DB to inject DB metadata
   */
  void addDB(ProviderDatabase db);

  /**
   * collect all DB metadata (called upon system startup)
   */
  void metadataCollection();

  /**
   * get an initialized DB from the cache, null if there is none
   */
  AbstractDatabase getCached(String id);

  /**
   * get an initialized DB from the cache. throw an exception if it's the wrong type or if it's null
   */
  <T extends AbstractDatabase> T getCachedForce(String id, Class<T> cls) throws Exception;

  /**
   * put an initialized DB in the cache
   */
  void putCache(String id, AbstractDatabase object);

  public Integer getAutocompleteTimeoutMs();

  public Integer getSearchTimeoutMs();

  public Integer getAllTimeoutMs();

  public boolean excludeFromSearch(AbstractDatabase db) throws Exception;

  public Collection<Table> searchTables(AbstractDatabase db) throws Exception;

  public String databaseSearchQuery(AbstractDatabase db) throws Exception;

  public String password(String table, String id) throws Exception;
}
