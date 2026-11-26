package org.dashjoin.mapping;

import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.SecurityContext;
import org.dashjoin.function.AbstractConfigurableFunction;

public abstract class AbstractMapping<ARG> extends AbstractConfigurableFunction<ARG, Void> {

  /**
   * the target ETL database
   */
  public String database;

  /**
   * mappings to be applied after data is gathered
   */
  public Map<String, Mapping> mappings;

  /**
   * if true, this class creates the required tables / columns in the database
   */
  public Boolean createSchema;

  public abstract Map<String, List<Map<String, Object>>> gather(SecurityContext sc)
      throws Exception;
}
