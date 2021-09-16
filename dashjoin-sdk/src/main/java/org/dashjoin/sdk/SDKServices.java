package org.dashjoin.sdk;

import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.Config;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.Services;

/**
 * Implementation of the central service manager that can be injected into remote functions and
 * databases
 */
public class SDKServices extends Services {

  /**
   * link back to the database
   */
  AbstractDatabase db;

  /**
   * password to use
   */
  String password;

  public SDKServices(AbstractDatabase db, String password) {
    this.db = db;
    this.password = password;
  }

  /**
   * return a fake config DB that returns the DB reference and password without a real config DB
   * (which is not present in the VM)
   */
  @Override
  public Config getConfig() {
    return new PojoDatabase() {

      @SuppressWarnings("unchecked")
      @Override
      public <T extends AbstractDatabase> T getCachedForce(String id, Class<T> cls)
          throws Exception {
        return (T) db;
      }

      @Override
      public String password(String table, String id) throws Exception {
        return password;
      }
    };
  }
}
