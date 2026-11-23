package org.dashjoin.function;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
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

  /**
   * the value this credential contributes to an HTTP request: the API key if apiKey is set,
   * otherwise the value of the Authorization header. Requires init to have been called.
   */
  public String authorization() throws Exception {
    if (apiKey != null && apiKey)
      return password();
    return "Basic " + Base64.getEncoder()
        .encodeToString((username + ":" + password()).getBytes(StandardCharsets.UTF_8));
  }

  /**
   * the HTTP headers this credential contributes to a request. Requires init to have been called.
   */
  public Map<String, String> httpHeaders() throws Exception {
    if (apiKey != null && apiKey)
      return Map.of(username, password());
    return Map.of("Authorization", authorization());
  }

  @Override
  public Void run(Void arg) throws Exception {
    return null;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }
}
