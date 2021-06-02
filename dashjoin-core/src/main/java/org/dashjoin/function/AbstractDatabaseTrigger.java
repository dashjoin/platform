package org.dashjoin.function;

import java.util.Map;

/**
 * subclasses of this abstract class allow triggers to be injected into the regular database crud
 * operations. Other than regular triggers, AbstractDatabaseTriggers can
 * 
 * 1) make changes to the payload before it is passed to the database (makes sense for any kind of
 * before hook)
 * 
 * 2) notify the system to NOT perform the update. First, all before triggers are run, if any of
 * these triggers returns FALSE from the run call, the main CrUD operation is not performed.
 * 
 * the CRUD and subsequent triggers are aborted if a trigger throws an exception
 */
public abstract class AbstractDatabaseTrigger
    extends AbstractFunction<AbstractDatabaseTrigger.Config, Boolean> {

  /**
   * trigger config
   */
  public static class Config {
    /**
     * create, update or delete
     */
    public String command;

    /**
     * CRUD on this DB
     */
    public String database;

    /**
     * CRUD on this table
     */
    public String table;

    /**
     * set for delete and update
     */
    public Map<String, Object> search;

    /**
     * set for update and create
     */
    public Map<String, Object> object;
  }

  @Override
  public Class<Config> getArgumentClass() {
    return Config.class;
  }

  @Override
  public String getType() {
    return "write";
  }
}
