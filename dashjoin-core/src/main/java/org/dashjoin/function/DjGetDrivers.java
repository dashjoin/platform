package org.dashjoin.function;

import java.util.List;
import org.dashjoin.service.Manage.Version;

/**
 * function to list the JDBC SQL database drivers
 */
public class DjGetDrivers extends DjGetDatabases {

  @Override
  public List<Version> run(Void arg) throws Exception {
    return expressionService.getManage().getDrivers();
  }

  @Override
  public String getID() {
    return "djGetDrivers";
  }
}
