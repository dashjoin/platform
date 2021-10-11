package org.dashjoin.function;

import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.ddl.SchemaChange;

/**
 * exposes the SchemaChange interface as a function so it can be scheduled via the API, etc.
 */
public class AlterColumn extends AbstractFunction<AlterColumn.Config, Void> {

  /**
   * config class also add the column name
   */
  public static class Config extends AlterTable.Config {
    public String column;
  }

  @Override
  public Void run(Config arg) throws Exception {
    if (arg == null)
      throw new IllegalArgumentException("No argument provided");
    if (arg.command == null)
      throw new IllegalArgumentException("No command provided: create, rename, alter or delete");
    if (arg.table == null)
      throw new IllegalArgumentException("No table provided");
    if (arg.column == null)
      throw new IllegalArgumentException("No column provided");

    AbstractDatabase db = services.getConfig().getDatabase(arg.database);
    SchemaChange s = db.getSchemaChange();
    switch (arg.command) {
      case "create":
        s.createColumn(arg.table, arg.column, arg.newType);
        return null;
      case "rename":
        s.renameColumn(arg.table, arg.column, arg.newName);
        return null;
      case "alter":
        s.alterColumn(arg.table, arg.column, arg.newType);
        return null;
      case "delete":
        s.dropColumn(arg.table, arg.column);
        return null;
      default:
        throw new Exception(
            "Unknown command " + arg.command + ". Must create, rename, alter or delete.");
    }
  }

  @Override
  public Class<Config> getArgumentClass() {
    return Config.class;
  }

  @Override
  public String getID() {
    return "alterColumn";
  }

  @Override
  public String getType() {
    return "write";
  }
}
