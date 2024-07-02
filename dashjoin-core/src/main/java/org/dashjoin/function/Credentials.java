package org.dashjoin.function;

import org.dashjoin.model.JsonSchema;

/**
 * allows storing credentials for OpenJson etc.
 */
public class Credentials extends AbstractConfigurableFunction<Void, Void> {

  /**
   * optional HTTP basic authentication user name
   */
  @JsonSchema(style = {"width", "400px"})
  public String username;

  /**
   * optional HTTP basic authentication password
   */
  @JsonSchema(widget = "password", style = {"width", "400px"})
  public String password;

  @JsonSchema(title = "Use credentials as HTTP API key")
  public Boolean apiKey;

  @Override
  public Void run(Void arg) throws Exception {
    return null;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }
}
