package org.dashjoin.function;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.service.JSONDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

public class OAuth2CredentialsTest {

  static final ObjectMapper om = new ObjectMapper();

  HttpServer server;

  /**
   * form parameters of the requests received by the token endpoint
   */
  List<Map<String, String>> requests = new ArrayList<>();

  int status = 200;
  String response = "{\"access_token\":\"tok\",\"expires_in\":3600,\"token_type\":\"Bearer\"}";

  @BeforeEach
  public void before() throws Exception {
    OAuth2Credentials.cache.clear();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/token", ex -> {
      String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
      Map<String, String> form = new LinkedHashMap<>();
      for (String kv : body.split("&")) {
        String[] p = kv.split("=", 2);
        form.put(URLDecoder.decode(p[0], StandardCharsets.UTF_8),
            URLDecoder.decode(p[1], StandardCharsets.UTF_8));
      }
      requests.add(form);
      byte[] out = response.getBytes(StandardCharsets.UTF_8);
      ex.getResponseHeaders().add("Content-Type", "application/json");
      ex.sendResponseHeaders(status, out.length);
      ex.getResponseBody().write(out);
      ex.close();
    });
    server.start();
  }

  @AfterEach
  public void after() {
    server.stop(0);
  }

  String url() {
    return "http://127.0.0.1:" + server.getAddress().getPort() + "/token";
  }

  static OAuth2Credentials creds(String secret) {
    OAuth2Credentials c = new OAuth2Credentials() {
      @Override
      public String password() {
        return secret;
      }
    };
    c.ID = "oauth";
    return c;
  }

  @Test
  public void testClientCredentials() throws Exception {
    OAuth2Credentials c = creds("s3cret");
    c.provider = "microsoft";
    c.tenant = "contoso.onmicrosoft.com";
    c.username = "client-id";
    c.tokenUrl = url();

    Assertions.assertEquals("Bearer tok", c.authorization());
    Assertions.assertEquals(Map.of("Authorization", "Bearer tok"), c.httpHeaders());
    Assertions.assertEquals(1, requests.size(), "second call is served from the cache");

    Map<String, String> form = requests.get(0);
    Assertions.assertEquals("client_credentials", form.get("grant_type"));
    Assertions.assertEquals("client-id", form.get("client_id"));
    Assertions.assertEquals("s3cret", form.get("client_secret"));
    Assertions.assertEquals("https://outlook.office365.com/.default", form.get("scope"));

    c.invalidate(null);
    c.accessToken(null);
    Assertions.assertEquals(2, requests.size());

    // changed secret means new cache key
    OAuth2Credentials c2 = creds("other");
    c2.provider = c.provider;
    c2.tenant = c.tenant;
    c2.username = c.username;
    c2.tokenUrl = c.tokenUrl;
    c2.accessToken(null);
    Assertions.assertEquals(3, requests.size());
  }

  @Test
  public void testExpiry() throws Exception {
    response = "{\"access_token\":\"tok\",\"expires_in\":30}";
    OAuth2Credentials c = creds("s3cret");
    c.provider = "custom";
    c.username = "client-id";
    c.tokenUrl = url();
    c.accessToken(null);
    c.accessToken(null);
    Assertions.assertEquals(2, requests.size(), "tokens within the expiry margin are renewed");
    Assertions.assertNull(requests.get(0).get("scope"));
  }

  @Test
  public void testError() throws Exception {
    status = 401;
    response = "{\"error\":\"invalid_client\",\"error_description\":\"AADSTS7000215: bad secret\"}";
    OAuth2Credentials c = creds("s3cret");
    c.provider = "custom";
    c.username = "client-id";
    c.tokenUrl = url();
    Exception e = Assertions.assertThrows(Exception.class, () -> c.accessToken(null));
    Assertions.assertTrue(e.getMessage().contains("AADSTS7000215"), e.getMessage());
    Assertions.assertFalse(e.getMessage().contains("s3cret"));
  }

  @Test
  public void testMicrosoftDefaults() {
    OAuth2Credentials c = creds("x");
    c.provider = "microsoft";
    Assertions.assertThrows(IllegalArgumentException.class, () -> c.tokenUrl(null));
    c.tenant = "t1";
    Assertions.assertEquals("https://login.microsoftonline.com/t1/oauth2/v2.0/token",
        c.tokenUrl(null));
    c.scope = "https://graph.microsoft.com/.default";
    Assertions.assertEquals("https://graph.microsoft.com/.default", c.scope());
  }

  @Test
  public void testGoogleServiceAccount() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    KeyPair kp = gen.generateKeyPair();
    String pem = "-----BEGIN PRIVATE KEY-----\n"
        + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(kp.getPrivate().getEncoded())
        + "\n-----END PRIVATE KEY-----\n";
    Map<String, Object> keyFile = new LinkedHashMap<>();
    keyFile.put("type", "service_account");
    keyFile.put("client_email", "robot@project.iam.gserviceaccount.com");
    keyFile.put("private_key", pem);
    keyFile.put("token_uri", url());

    // pasting into a single line input removes the line breaks
    OAuth2Credentials c = creds(om.writeValueAsString(keyFile).replace("\n", ""));
    c.provider = "google";
    c.subject = "default@example.com";

    Assertions.assertEquals("tok", c.accessToken("invoices@example.com"));
    Assertions.assertEquals("tok", c.accessToken(null));
    Assertions.assertEquals(2, requests.size(), "tokens are cached per mailbox");

    Map<String, String> form = requests.get(0);
    Assertions.assertEquals(OAuth2Credentials.JWT_BEARER, form.get("grant_type"));
    String[] jwt = form.get("assertion").split("\\.");
    Assertions.assertEquals(3, jwt.length);

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initVerify(kp.getPublic());
    sig.update((jwt[0] + "." + jwt[1]).getBytes(StandardCharsets.US_ASCII));
    Assertions.assertTrue(sig.verify(Base64.getUrlDecoder().decode(jwt[2])));

    Map<String, Object> claims = om.readValue(Base64.getUrlDecoder().decode(jwt[1]), JSONDatabase.tr);
    Assertions.assertEquals("robot@project.iam.gserviceaccount.com", claims.get("iss"));
    Assertions.assertEquals("invoices@example.com", claims.get("sub"));
    Assertions.assertEquals("https://mail.google.com/", claims.get("scope"));
    Assertions.assertEquals(url(), claims.get("aud"));

    claims = om.readValue(
        Base64.getUrlDecoder().decode(requests.get(1).get("assertion").split("\\.")[1]),
        JSONDatabase.tr);
    Assertions.assertEquals("default@example.com", claims.get("sub"));

    // raw PEM with service account email as username
    OAuth2Credentials raw = creds(pem.replace("\n", " "));
    raw.provider = "google";
    raw.username = "robot@project.iam.gserviceaccount.com";
    raw.tokenUrl = url();
    Assertions.assertEquals("tok", raw.accessToken("a@example.com"));
  }

  @Test
  public void testNoSecret() {
    OAuth2Credentials c = creds(null);
    c.provider = "custom";
    c.tokenUrl = url();
    Assertions.assertThrows(IllegalArgumentException.class, () -> c.accessToken(null));
  }

  @Test
  public void testNotForHmac() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hmac.asCredentials(new OAuth2Credentials(), "oauth"));
  }

  @Test
  public void testBasicCredentials() throws Exception {
    Credentials c = new Credentials() {
      @Override
      public String password() {
        return "djdjdj";
      }
    };
    c.username = "admin";
    Assertions.assertEquals("Basic YWRtaW46ZGpkamRq", c.authorization());
    c.apiKey = true;
    c.username = "x-api-key";
    Assertions.assertEquals("djdjdj", c.authorization());
    Assertions.assertEquals(Map.of("x-api-key", "djdjdj"), c.httpHeaders());
  }
}
