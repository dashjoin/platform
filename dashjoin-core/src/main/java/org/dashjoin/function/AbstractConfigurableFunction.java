package org.dashjoin.function;

import java.util.List;
import org.dashjoin.service.CredentialManager;
import org.dashjoin.service.PojoDatabase;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Base class for all configurable functions. Provides the primary key and polimorphism class name
 */
public abstract class AbstractConfigurableFunction<ARG, RET> extends AbstractFunction<ARG, RET> {

  /**
   * db name
   */
  public String ID;

  /**
   * implementation class
   */
  public String djClassName;

  /**
   * roles that are allowed to run the function
   */
  public List<String> roles;

  /**
   * does this function have side effects (write) or does it only query (read)
   */
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

    String envpwd = ConfigProvider.getConfig()
        .getConfigValue("dashjoin.function." + ID + ".password").getValue();
    if (envpwd != null)
      return envpwd;

    PojoDatabase db = (PojoDatabase) this.services.getConfig();
    String password = db.password("dj-function", ID);
    try (CredentialManager.Credential c = new CredentialManager.Credential(password)) {
      return password != null ? new String(c.getSecret()) : null;
    }
  }
}
