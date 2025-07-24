package org.dashjoin.function;

import java.util.Map;
import org.dashjoin.model.JsonSchema;

/**
 * allows saving an expression on the server. When run, we evaluate / apply the expression with the
 * data context passed as an argument
 */
@JsonSchema(required = {"expression"}, order = {"expression", "parameters"})
public class Invoke extends AbstractConfigurableFunction<Object, Object> {

  @JsonSchema(widget = "custom", widgetType = "expression", title = "Expression")
  public String expression;

  public static class Prop {
    public String description;
    @JsonSchema(enums = {"boolean", "integer", "number", "string", "date"})
    public String type;
    public Boolean required;
  }

  /**
   * parameters that provide metadata on how to call the function (to be used by AI / OpenAPI)
   */
  @JsonSchema(style = {"width", "800px"})
  public Map<String, Prop> parameters;

  @Override
  public Object run(Object data) throws Exception {
    if (expression == null)
      throw new Exception("No expression specified");
    return expressionService.resolve(sc, expression, data, this.readOnly);
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }
}
