package org.dashjoin.function;

import org.dashjoin.model.JsonSchema;

/**
 * allows saving an expression on the server. When run, we evaluate / apply the expression with the
 * data context passed as an argument
 */
@JsonSchema(required = {"expression"}, order = {"expression"})
public class Invoke extends AbstractConfigurableFunction<Object, Object> {

  @JsonSchema(widget = "custom", widgetType = "expression")
  public String expression;

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
