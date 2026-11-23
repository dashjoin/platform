package org.dashjoin.function;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.service.JSONDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * OAuth2 credentials for server to server access (no user interaction). Obtains access tokens from
 * an OAuth2 token endpoint, caches them until shortly before they expire, and contributes them as
 * "Authorization: Bearer ..." header wherever Credentials are accepted ($curl, RestJson
 * insertCredentials). The Email and EmailInbox functions use them for XOAUTH2 login to Gmail and
 * Office 365.
 *
 * Microsoft (Entra ID app registration, client credentials grant): username is the application
 * (client) ID, password the client secret, tenant the directory (tenant) ID or domain.
 *
 * Google (service account with domain-wide delegation, JWT bearer grant): password is the service
 * account JSON key file (username is then optional) or the PEM private key (username is then the
 * service account email). The token is issued for the mailbox being accessed or for subject.
 */
@JsonSchema(order = {"provider", "tenant", "username", "password", "scope", "subject", "tokenUrl"})
public class OAuth2Credentials extends Credentials {

  static final ObjectMapper objectMapper = new ObjectMapper();

  static final String JWT_BEARER = "urn:ietf:params:oauth:grant-type:jwt-bearer";

  /**
   * renew tokens this many seconds before they expire
   */
  static final long EXPIRY_MARGIN = 60;

  /**
   * token cache: cache key to token
   */
  static final Map<String, Token> cache = new ConcurrentHashMap<>();

  static class Token {
    String accessToken;
    long expires;
  }

  /**
   * microsoft: Entra ID client credentials, google: service account (JWT bearer), custom: client
   * credentials grant against tokenUrl
   */
  @JsonSchema(enums = {"microsoft", "google", "custom"})
  public String provider;

  /**
   * Microsoft directory (tenant) ID or domain
   */
  @JsonSchema(style = {"width", "400px"})
  public String tenant;

  /**
   * OAuth2 scope. Defaults: microsoft https://outlook.office365.com/.default (IMAP / SMTP), google
   * https://mail.google.com/. Use https://graph.microsoft.com/.default for Microsoft Graph calls
   */
  @JsonSchema(style = {"width", "400px"})
  public String scope;

  /**
   * google: default user to impersonate if the caller does not name a mailbox
   */
  @JsonSchema(style = {"width", "400px"})
  public String subject;

  /**
   * optional token endpoint, overrides the provider default
   */
  @JsonSchema(style = {"width", "400px"})
  public String tokenUrl;

  /**
   * always sends a bearer token, apiKey is ignored
   */
  @Override
  public String authorization() throws Exception {
    return "Bearer " + accessToken(null);
  }

  @Override
  public Map<String, String> httpHeaders() throws Exception {
    return Map.of("Authorization", authorization());
  }

  /**
   * returns a valid access token, fetching a new one if none is cached or the cached one is about
   * to expire. Requires init to have been called.
   *
   * @param mailbox the mailbox to access. For google, the token is issued for this user (domain-wide
   *        delegation). If null, subject is used. Ignored for other providers.
   */
  public String accessToken(String mailbox) throws Exception {
    String secret = password();
    if (secret == null || secret.isEmpty())
      throw new IllegalArgumentException("OAuth2 credentials " + ID + " have no secret");
    String key = cacheKey(mailbox, secret);
    Token token = cache.get(key);
    if (token != null && token.expires > now())
      return token.accessToken;
    token = fetch(mailbox, secret);
    cache.put(key, token);
    return token.accessToken;
  }

  /**
   * drops the cached token, e.g. after the server rejected it
   */
  public void invalidate(String mailbox) throws Exception {
    String secret = password();
    if (secret != null)
      cache.remove(cacheKey(mailbox, secret));
  }

  String cacheKey(String mailbox, String secret) throws Exception {
    String tenantPrefix = services != null ? services.getTenantManager().getHomePrefix() : "";
    String s = String.join("\n", "" + tenantPrefix, "" + ID, "" + provider, "" + tenant,
        "" + username, "" + scope(), "" + tokenUrl, "" + subject(mailbox), secret);
    return HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
  }

  long now() {
    return System.currentTimeMillis() / 1000;
  }

  boolean isGoogle() {
    return "google".equals(provider);
  }

  String subject(String mailbox) {
    if (!isGoogle())
      return null;
    return mailbox != null ? mailbox : subject;
  }

  String scope() {
    if (scope != null && !scope.isBlank())
      return scope;
    if ("microsoft".equals(provider))
      return "https://outlook.office365.com/.default";
    if (isGoogle())
      return "https://mail.google.com/";
    return null;
  }

  String tokenUrl(Map<String, Object> googleKey) {
    if (tokenUrl != null && !tokenUrl.isBlank())
      return tokenUrl;
    if ("microsoft".equals(provider)) {
      if (tenant == null || tenant.isBlank())
        throw new IllegalArgumentException("OAuth2 credentials " + ID + ": please specify tenant");
      return "https://login.microsoftonline.com/" + tenant + "/oauth2/v2.0/token";
    }
    if (isGoogle()) {
      Object uri = googleKey.get("token_uri");
      return uri instanceof String ? (String) uri : "https://oauth2.googleapis.com/token";
    }
    throw new IllegalArgumentException("OAuth2 credentials " + ID + ": please specify tokenUrl");
  }

  Token fetch(String mailbox, String secret) throws Exception {
    FormBody.Builder form = new FormBody.Builder();
    String url;
    if (isGoogle()) {
      Map<String, Object> googleKey = googleKey(secret);
      url = tokenUrl(googleKey);
      form.add("grant_type", JWT_BEARER);
      form.add("assertion", jwt(googleKey, url, subject(mailbox)));
    } else {
      url = tokenUrl(null);
      form.add("grant_type", "client_credentials");
      if (username == null || username.isBlank())
        throw new IllegalArgumentException(
            "OAuth2 credentials " + ID + ": please specify the client ID as username");
      form.add("client_id", username);
      form.add("client_secret", secret);
      if (scope() != null)
        form.add("scope", scope());
    }

    OkHttpClient client = Doc2data.getHttpClient();
    Request request = new Request.Builder().url(url).post(form.build())
        .header("Accept", "application/json").build();
    try (okhttp3.Response response = client.newCall(request).execute()) {
      String body = response.body() == null ? "" : response.body().string();
      Map<String, Object> json;
      try {
        json = objectMapper.readValue(body, JSONDatabase.tr);
      } catch (Exception e) {
        json = Map.of();
      }
      if (response.code() >= 400 || !(json.get("access_token") instanceof String)) {
        Object error = json.get("error_description");
        if (error == null)
          error = json.get("error");
        throw new Exception("OAuth2 credentials " + ID + ": token request failed (HTTP "
            + response.code() + ")" + (error != null ? ": " + error : ""));
      }
      Token token = new Token();
      token.accessToken = (String) json.get("access_token");
      long expiresIn = json.get("expires_in") instanceof Number
          ? ((Number) json.get("expires_in")).longValue()
          : json.get("expires_in") instanceof String
              ? Long.parseLong((String) json.get("expires_in"))
              : 3600;
      token.expires = now() + Math.max(0, expiresIn - EXPIRY_MARGIN);
      return token;
    }
  }

  /**
   * parses the google secret: either the service account JSON key file or a PEM private key (then
   * username must hold the service account email)
   */
  Map<String, Object> googleKey(String secret) throws Exception {
    Map<String, Object> res = new LinkedHashMap<>();
    if (secret.trim().startsWith("{"))
      res.putAll(objectMapper.readValue(secret, JSONDatabase.tr));
    else
      res.put("private_key", secret);
    if (username != null && !username.isBlank())
      res.put("client_email", username);
    if (!(res.get("client_email") instanceof String))
      throw new IllegalArgumentException("OAuth2 credentials " + ID
          + ": please specify the service account email as username or use the JSON key file");
    if (!(res.get("private_key") instanceof String))
      throw new IllegalArgumentException(
          "OAuth2 credentials " + ID + ": the JSON key file has no private_key");
    return res;
  }

  /**
   * creates the signed JWT assertion for the google service account flow
   */
  String jwt(Map<String, Object> googleKey, String audience, String sub) throws Exception {
    long now = now();
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("iss", googleKey.get("client_email"));
    if (scope() != null)
      claims.put("scope", scope());
    claims.put("aud", audience);
    claims.put("iat", now);
    claims.put("exp", now + 3600);
    if (sub != null)
      claims.put("sub", sub);

    Base64.Encoder b64 = Base64.getUrlEncoder().withoutPadding();
    String header = b64.encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes());
    String payload = b64.encodeToString(objectMapper.writeValueAsBytes(claims));
    String content = header + "." + payload;

    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey((String) googleKey.get("private_key")));
    signature.update(content.getBytes(StandardCharsets.US_ASCII));
    return content + "." + b64.encodeToString(signature.sign());
  }

  /**
   * parses a PKCS#8 PEM private key. Tolerates missing line breaks (e.g. from pasting into a single
   * line input) and literal \n sequences
   */
  static PrivateKey privateKey(String pem) throws Exception {
    String base64 = pem.replace("\\n", "").replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "")
        .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(base64);
    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
  }

}
