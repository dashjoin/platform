package org.dashjoin.function;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.dashjoin.service.ACLContainerRequestFilter;

/**
 * $hmac(keyId, message, algorithm?, encoding?) signs message with the secret of the Credentials
 * function keyId, e.g. for signed CDN URLs or for checking webhook signatures.
 *
 * The secret is never returned. Only users with one of the roles of the Credentials function may
 * use it (admin only if it has no roles). To let other users sign a fixed kind of message, wrap the
 * call in an Invoke function with runas set to such a role.
 *
 * algorithm: HmacSHA256 (default), HmacSHA384, HmacSHA512. encoding: base64 (default), base64url
 * (without padding) or hex.
 */
@SuppressWarnings("rawtypes")
public class Hmac extends AbstractVarArgFunction<Object> {

  static final Set<String> ALGORITHMS = Set.of("HmacSHA256", "HmacSHA384", "HmacSHA512");

  @Override
  public Object run(List args) throws Exception {
    String keyId = arg(args, 0, null);
    String message = arg(args, 1, null);
    if (keyId == null || message == null)
      throw new IllegalArgumentException("Syntax: $hmac(keyId, message, algorithm?, encoding?)");
    String algorithm = arg(args, 2, "HmacSHA256");
    String encoding = arg(args, 3, "base64");

    Credentials key = credentials(keyId);
    ACLContainerRequestFilter.check(sc, key);
    String secret = secret(key);
    if (secret == null || secret.isEmpty())
      throw new IllegalArgumentException("$hmac: " + keyId + " has no secret");
    return sign(secret, message, algorithm, encoding);
  }

  /**
   * loads the key record. Only Credentials functions qualify: other functions may hold passwords
   * for other purposes.
   */
  protected Credentials credentials(String keyId) throws Exception {
    return asCredentials(services.getConfig().getFunction(keyId), keyId);
  }

  static Credentials asCredentials(Object function, String keyId) {
    if (!(function instanceof Credentials))
      throw new IllegalArgumentException("$hmac: " + keyId + " is not a Credentials function");
    return (Credentials) function;
  }

  /**
   * the secret: config value dashjoin.function.[ID].password (e.g. an environment variable) or the
   * encrypted password stored with the function
   */
  protected String secret(Credentials key) throws Exception {
    key.init(sc, services, expressionService, readOnly);
    return key.password();
  }

  static String sign(String secret, String message, String algorithm, String encoding)
      throws Exception {
    if (!ALGORITHMS.contains(algorithm))
      throw new IllegalArgumentException("$hmac: unsupported algorithm " + algorithm);
    Mac mac = Mac.getInstance(algorithm);
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
    byte[] out = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    switch (encoding) {
      case "base64":
        return Base64.getEncoder().encodeToString(out);
      case "base64url":
        return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
      case "hex":
        return HexFormat.of().formatHex(out);
      default:
        throw new IllegalArgumentException("$hmac: unsupported encoding " + encoding);
    }
  }

  static String arg(List args, int index, String fallback) {
    Object value = index < args.size() ? args.get(index) : null;
    if (value == null)
      return fallback;
    if (!(value instanceof String))
      throw new IllegalArgumentException("$hmac: argument " + (index + 1) + " must be a string");
    return (String) value;
  }

  @Override
  public String getID() {
    return "hmac";
  }

  @Override
  public String getType() {
    return "read";
  }
}
