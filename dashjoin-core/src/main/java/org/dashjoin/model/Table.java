package org.dashjoin.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * top level JSON schema description of a database table. Contains namespace and key info
 */
public class Table {

  /**
   * pk
   */
  public String ID;

  /**
   * parent pk
   */
  public String parent;

  /**
   * table name
   */
  public String name;

  /**
   * type (e.g. number, string, object, array)
   */
  public String type;

  /**
   * for databases that do not preserve the column order
   */
  public List<String> columnOrder;

  /**
   * child properties (if type=object)
   */
  public Map<String, Property> properties;

  /**
   * layout information for this table's instances
   */
  public Map<String, Object> instanceLayout;

  /**
   * layout information for this table itself
   */
  public Map<String, Object> tableLayout;

  /**
   * table display name
   */
  public String title;

  /**
   * roles that are allowed to read the table
   */
  public List<String> readRoles;

  /**
   * roles that are allowed to write the table
   */
  public List<String> writeRoles;

  /**
   * json schema form switch
   */
  @JsonProperty("switch")
  public String _switch;

  /**
   * ref definitions
   */
  public Map<String, Property> definitions;

  /**
   * label template
   */
  @JsonProperty("dj-label")
  public String djLabel;

  /**
   * json schema required
   */
  public List<String> required;

  public String widget;

  public String widgetType;

  public List<Object> order;

  public String layout;

  public Object computed;

  @JsonProperty("before-create")
  public String beforeCreate;

  @JsonProperty("after-create")
  public String afterCreate;

  @JsonProperty("before-update")
  public String beforeUpdate;

  @JsonProperty("after-update")
  public String afterUpdate;

  @JsonProperty("before-delete")
  public String beforeDelete;

  @JsonProperty("after-delete")
  public String afterDelete;

  public static Table ofName(String name) {
    Table t = new Table();
    t.name = name;
    return t;
  }
}
