package org.dashjoin.function;

import java.security.Principal;
import java.util.Map;
import org.dashjoin.model.JsonSchema;
import jakarta.ws.rs.core.SecurityContext;

/**
 * allows saving an expression on the server. When run, we evaluate / apply the expression with the
 * data context passed as an argument
 */
@JsonSchema(required = {"expression"}, order = {"expression", "parameters", "runas"})
public class Invoke extends AbstractConfigurableFunction<Object, Object> {

  @JsonSchema(widget = "custom", widgetType = "expression", title = "Expression")
  public String expression;

  /**
   * if set, the expression runs with this role instead of the caller's roles
   */
  @JsonSchema(layout = "select", choicesUrl = "/rest/database/all/config/dj-role", jsonata = "ID")
  public String runas;

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
    if (runas != null && !runas.isBlank())
      return expressionService.resolve(new SecurityContext() {
        @Override
        public Principal getUserPrincipal() {
          return sc.getUserPrincipal();
        }

        @Override
        public boolean isUserInRole(String r) {
          return runas.equals(r);
        }

        @Override
        public boolean isSecure() {
          return sc.isSecure();
        }

        @Override
        public String getAuthenticationScheme() {
          return sc.getAuthenticationScheme();
        }
      }, expression, data, this.readOnly);
    else
      return expressionService.resolve(sc, expression, data, this.readOnly);
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }
}
