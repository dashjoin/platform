package org.dashjoin.service.ddl;

import org.dashjoin.function.AbstractDatabaseTrigger.Config;

/**
 * low level schema change interface to be implemented by databases. Operation idempotency (no error
 * when update sets name=name) is handled in north bound layers.
 */
public interface SchemaChange {

  /**
   * create a table called "table". If keyname and keytype are provided, use them as the primary key
   * column. If not, default to PK ID and a string name column
   */
  public void createTable(String table, String keyName, String keyType) throws Exception;

  /**
   * delete the table
   */
  public void dropTable(String table) throws Exception;

  /**
   * rename the table
   */
  public void renameTable(String table, String newName) throws Exception;

  /**
   * create a new column with the given name/type
   */
  public void createColumn(String table, String columnName, String columnType) throws Exception;

  /**
   * rename column
   */
  public void renameColumn(String table, String column, String newName) throws Exception;

  /**
   * change column datatype
   */
  public void alterColumn(String table, String column, String newType) throws Exception;

  /**
   * drop column
   */
  public void dropColumn(String table, String column) throws Exception;

  /**
   * there is some logic in AlterTableTrigger and AlterColumnTrigger before the DB's schema change
   * interface is called. This method is called before anything happens and gives the database the
   * change to do error checks
   */
  default public void check(Config arg) throws Exception {}
}
