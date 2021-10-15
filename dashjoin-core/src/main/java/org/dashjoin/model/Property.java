package org.dashjoin.model;

import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON schema description of a property
 */
@Schema(title = "Property: JSON schema description of a property")
public class Property {

  /**
   * pk
   */
  @Schema(title = "Primary key", example = "dj/northwind/EMPLOYEES/LAST_NAME")
  public String ID;

  /**
   * parent pk
   */
  @Schema(title = "Table the property is defined in", example = "dj/northwind/EMPLOYEE")
  public String parent;

  /**
   * property name
   */
  @Schema(title = "name of the property")
  public String name;

  /**
   * null if prop is no PK, the position in the (composite) PK otherwise (starting from 0)
   */
  @Schema(
      title = "If the property is the primary key, this is 0. If it is part of a composite key, it is 0,1,2,.. Null if this is no key.")
  public Integer pkpos;

  /**
   * null if the property is no FK, PK of the property it points to otherwise
   */
  @Schema(
      title = "ID of the property / column this foreign key is pointing to. Null if the column is no foreign key",
      example = "dj/northwind/EMPLOYEE/EMPLOYEE_ID")
  public String ref;

  /**
   * type (e.g. number, string, object, array)
   */
  @Schema(title = "JSON type of the property", example = "number")
  public String type;

  /**
   * short info about the property
   */
  @Schema(title = "JSON schema title")
  public String title;

  /**
   * long description of the property
   */
  @Schema(title = "JSON schema description")
  public String description;

  /**
   * widget to use
   */
  @Schema(title = "JSON schema form widget to use for visualization")
  public String widget;

  /**
   * custom widget type to use (widget=custom)
   */
  @Schema(title = "JSON schema form custom widget")
  public String widgetType;

  /**
   * REST URL to collect choices from
   */
  @Schema(title = "JSON schema form option choices URL")
  public String choicesUrl;

  /**
   * REST URL verb to collect choices from
   */
  @Schema(title = "JSON schema form option choices verb")
  public String choicesVerb;

  /**
   * REST URL to collect choices from
   */
  @Schema(title = "JSON schema form option choices jsonata")
  public String jsonata;

  /**
   * customize select / autocomplete
   */
  @Schema(title = "customize select / autocomplete")
  public String displayWith;

  /**
   * json schema array type
   */
  @Schema(title = "JSON schema array type")
  public Property items;

  /**
   * child properties (if type=object)
   */
  @Schema(title = "JSON schema object properties")
  public Map<String, Property> properties;

  /**
   * free key / value
   */
  @Schema(title = "JSON schema additional properties")
  public Property additionalProperties;

  /**
   * json schema form switch
   */
  @JsonProperty("switch")
  @Schema(title = "JSON schema form switch")
  public String _switch;

  /**
   * json schema form case
   */
  @JsonProperty("case")
  @Schema(title = "JSON schema form case")
  public List<String> _case;

  /**
   * json schema enum
   */
  @JsonProperty("enum")
  @Schema(title = "JSON schema enum")
  public List<String> _enum;

  /**
   * json schema form $ref
   */
  @JsonProperty("$ref")
  @Schema(title = "JSON schema ref")
  public String _ref;

  /**
   * json schema form layout
   */
  @Schema(title = "JSON schema form layout")
  public String layout;

  /**
   * The native DB type
   */
  @Schema(title = "native database type of the property")
  public String dbType;

  /**
   * json schema read only marker
   */
  @Schema(title = "JSON schema readOnly")
  public Boolean readOnly;

  @Schema(title = "JSON schema createOnly")
  public Boolean createOnly;

  /**
   * json schema required
   */
  @Schema(title = "JSON schema required")
  public Boolean required;

  /**
   * json schema examples
   */
  @Schema(title = "JSON schema examples")
  public List<String> examples;

  /**
   * json schema form style
   */
  @Schema(title = "JSON schema form style")
  public Object style;

  /**
   * json schema form choices
   */
  @Schema(title = "JSON schema form choices")
  public List<String> choices;

  /**
   * format
   */
  @Schema(title = "field string format")
  public String format;

  /**
   * errorMessage
   */
  @Schema(title = "custom validation error message")
  public String errorMessage;
}
