package org.dashjoin.mapping;

import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.SecurityContext;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.model.JsonSchema;

public abstract class AbstractMapping<ARG> extends AbstractConfigurableFunction<ARG, Void> {

  /**
   * the target ETL database
   */
  @JsonSchema(choicesUrl = "/rest/database/all/config/dj-database",
      jsonata = "$filter($.name, function($x){$x != \"config\"})", title = "Target database")
  public String database;

  /**
   * mappings to be applied after data is gathered
   */
  @JsonSchema(widget = "custom", widgetType = "mapping")
  public Map<String, Mapping> mappings;

  /**
   * if true, this class creates the required tables / columns in the database
   */
  @JsonSchema(title = "Create Schema")
  public Boolean createSchema;

  public abstract Map<String, List<Map<String, Object>>> gather(SecurityContext sc)
      throws Exception;
}
