package org.dashjoin.function;

import java.util.List;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.service.CredentialManager;
import org.dashjoin.service.PojoDatabase;

/**
 * Base class for all configurable functions. Provides the primary key and polimorphism class name
 */
@JsonSchema(layout = "vertical", required = {"ID"},
    order = {"djClassName", "ID", "comment", "roles", "type"})
public abstract class AbstractConfigurableFunction<ARG, RET> extends AbstractFunction<ARG, RET> {

  /**
   * db name
   */
  @JsonSchema(createOnly = true)
  public String ID;

  /**
   * implementation class
   */
  @JsonSchema(title = "Function Type", widget = "select", choicesVerb = "GET",
      choicesUrl = "/rest/manage/getConfigurableFunctions", jsonata = "name",
      displayWith = "localName")
  public String djClassName;

  /**
   * roles that are allowed to run the function
   */
  @JsonSchema(layout = "select", choicesUrl = "/rest/database/query/config/dj-roles-without-admin",
      jsonata = "ID")
  public List<String> roles;

  /**
   * does this function have side effects (write) or does it only query (read)
   */
  @JsonSchema(enums = {"read", "write"})
  public String type;

  /**
   * optional function comment
   */
  public String comment;

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getID() {
    return ID;
  }

  @Override
  public List<String> getRoles() {
    return roles;
  }

  public String password() throws Exception {
    PojoDatabase db = (PojoDatabase) this.services.getConfig();
    String password = db.password("dj-function", ID);
    try (CredentialManager.Credential c = new CredentialManager.Credential(password)) {
      return password != null ? new String(c.getSecret()) : null;
    }
  }
}
