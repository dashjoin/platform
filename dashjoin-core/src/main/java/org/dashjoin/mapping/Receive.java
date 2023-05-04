package org.dashjoin.mapping;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.model.JsonSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Receives data which is then transformed and loaded
 */
@JsonSchema(required = {"database"}, order = {"sample", "database", "createSchema", "mappings"})
public class Receive extends AbstractMapping<Object> {

  private static final ObjectMapper om = new ObjectMapper();

  public String sample;

  @Override
  public Void run(Object arg) throws Exception {

    if (arg == null)
      throw new Exception("No data received");

    // leverage the ETL code via delegate
    ETL etl = new ETL() {
      @Override
      public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {
        return convertToMapOfTables(arg);
      }

      @Override
      protected void info(String s) throws Exception {
        // noop
      }
    };
    etl.init(sc, services, expressionService, readOnly);
    etl.mappings = mappings;
    etl.createSchema = createSchema;
    etl.database = database;
    return etl.runInternal(null);
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }

  @Override
  public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {
    return new ETL().convertToMapOfTables(
        om.readValue(new ByteArrayInputStream(sample.getBytes()), Object.class));
  }
}
