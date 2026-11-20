package org.dashjoin.function;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.service.SqlConfigStore;

/**
 * Updates the app in the SQL config database: git pull, then the model folder of the new HEAD
 * replaces the app layer. Changes made at runtime (local layer) stay. $appSync(true) only reports
 * what would change.
 */
public class AppSync extends AbstractFunction<Boolean, Map<String, Object>> {

  private final static Logger logger = Logger.getLogger(AppSync.class.getName());

  @Override
  public Map<String, Object> run(Boolean dryRun) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to sync the app");
    return sync(services, Boolean.TRUE.equals(dryRun));
  }

  /**
   * pull and sync the app; reconnects the databases whose definition changed
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> sync(Services services, boolean dryRun) throws Exception {
    SqlConfigStore store = SqlConfigStore.get();
    if (store == null)
      throw new Exception(
          "The config database is not stored in SQL (dashjoin.configdb.url is not set)");
    Map<String, Object> report = store.sync(services.getTenantHome(), true, dryRun);
    if (!dryRun)
      for (String id : (List<String>) report.get("changedDatabases"))
        try {
          ((PojoDatabase) services.getConfig()).metadataCollection(id);
        } catch (Exception e) {
          // e.g. the database was removed
          logger.info("Not reconnecting database " + id + ": " + e);
        }
    return report;
  }

  @Override
  public Class<Boolean> getArgumentClass() {
    return Boolean.class;
  }

  @Override
  public String getID() {
    return "appSync";
  }

  @Override
  public String getType() {
    return "write";
  }
}
