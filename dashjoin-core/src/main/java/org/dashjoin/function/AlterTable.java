package org.dashjoin.function;

import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.ddl.SchemaChange;

/**
 * wraps SchemaChange into a function
 */
public class AlterTable extends AbstractFunction<AlterTable.Config, Void> {

  /**
   * config class
   */
  public static class Config {
    /**
     * alter this DB
     */
    public String database;

    /**
     * create / alter this table
     */
    public String table;

    /**
     * create, rename or delete
     */
    public String command;

    /**
     * when creating a table, name of the initial column (can be omitted)
     */
    public String newName;

    /**
     * when creating a table, type of the initial column (can be omitted)
     */
    public String newType;
  }

  @Override
  public Void run(Config arg) throws Exception {
    if (arg == null)
      throw new IllegalArgumentException("No argument provided");
    if (arg.command == null)
      throw new IllegalArgumentException("No command provided: create, rename or delete");
    if (arg.table == null)
      throw new IllegalArgumentException("No table provided");
    if (arg.table.contains("/"))
      throw new IllegalArgumentException("Table names must not contain /");

    AbstractDatabase db = services.getConfig().getDatabase(arg.database);
    SchemaChange s = db.getSchemaChange();
    switch (arg.command) {
      case "create":
        if (arg.table == null)
          throw new IllegalArgumentException("No table name provided");
        s.createTable(arg.table, arg.newName, arg.newType);
        return null;
      case "rename":
        s.renameTable(arg.table, arg.newName);
        return null;
      case "delete":
        s.dropTable(arg.table);
        return null;
      default:
        throw new Exception("Unknown command " + arg.command + ". Must create, rename or delete.");
    }
  }

  @Override
  public Class<Config> getArgumentClass() {
    return Config.class;
  }

  @Override
  public String getID() {
    return "alterTable";
  }

  @Override
  public String getType() {
    return "write";
  }
}
