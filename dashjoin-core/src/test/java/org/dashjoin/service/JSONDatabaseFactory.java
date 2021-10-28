package org.dashjoin.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import org.dashjoin.util.Home;

/**
 * This is currently only used for tests!
 */
public class JSONDatabaseFactory {

  public static JSONDatabase getPersistantInstance() {
    // if (Cluster.getInstance().isClusterMode())
    // return new JSONEtcdDatabase(Cluster.getInstance().getClusterConfiguration().getEtcdClient());

    String jsonDbClz = System.getenv("JSONDB_PROVIDER");
    if (jsonDbClz != null) {
      JSONDatabase jsonDb;
      try {
        jsonDb = (JSONDatabase) Class
            .forName(jsonDbClz, false, Thread.currentThread().getContextClassLoader())
            .getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException
          | ClassNotFoundException e) {
        throw new RuntimeException("Cannot instantiate provided JSONDB_PROVIDER: " + jsonDbClz, e);
      }
      return jsonDb;
    }

    JSONFileDatabase db = new JSONFileDatabase();
    db.home = new Home(Optional.empty(), Optional.empty());
    return db;
  }

  public static JSONDatabase getReadOnlyInstance() {
    return new JSONClassloaderDatabase();
  }

}
