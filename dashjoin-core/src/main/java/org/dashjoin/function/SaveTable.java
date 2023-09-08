package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.dashjoin.mapping.ETL;
import org.dashjoin.mapping.Mapping;
import org.dashjoin.util.MapUtil;
import jakarta.ws.rs.core.SecurityContext;

/**
 * JSONata shortcut for ETL
 */
@SuppressWarnings("rawtypes")
public class SaveTable extends AbstractVarArgFunction<Void> {

  @Override
  public Void run(List arg) throws Exception {
    String mode = (String) arg.get(0);
    String database = (String) arg.get(1);
    String table = (String) arg.get(2);
    Object data = arg.get(3);
    String pk = arg.get(4) == null ? "ID" : (String) arg.get(4);
    if (mode == null | database == null || table == null || data == null
        || (!mode.equals("Ignore") && !mode.equals("Refresh") && !mode.equals("Delete All")))
      throw new IllegalArgumentException(
          "Syntax: $saveTable(Ignore | Refresh | Delete All, database, table, data, [pk])");

    ETL etl = new ETL() {
      @Override
      public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {
        return convertToMapOfTables(MapUtil.of(table, data));
      }

      @Override
      protected void info(String s) throws Exception {
        // noop
      }
    };
    etl.init(sc, services, expressionService, readOnly);
    etl.mappings = MapUtil.of(table, new Mapping());
    etl.mappings.get(table).pk = pk;
    etl.createSchema = true;
    etl.database = database;
    etl.oldData = mode;
    etl.runInternal(null);
    
    return null;
  }

  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, String.class, String.class, Object.class, String.class);
  }

  @Override
  public String getID() {
    return "saveTable";
  }

  @Override
  public String getType() {
    return "write";
  }
}
