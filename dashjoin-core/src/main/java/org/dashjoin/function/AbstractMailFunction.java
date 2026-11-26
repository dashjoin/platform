package org.dashjoin.function;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Session;

/**
 * common configuration of the Email and EmailInbox functions: server presets, login via password
 * or OAuth2 (XOAUTH2) and additional Jakarta Mail properties
 */
public abstract class AbstractMailFunction<ARG, RET> extends AbstractConfigurableFunction<ARG, RET> {

  /**
   * default socket connect timeout (ms), can be overridden via properties
   */
  static final int CONNECT_TIMEOUT = 30000;

  /**
   * default socket read timeout (ms), can be overridden via properties
   */
  static final int READ_TIMEOUT = 60000;

  /**
   * gmail / office365 fill in host, port and TLS settings. custom: specify them via properties
   */
  public String server;

  /**
   * username to log into the email service (usually the mailbox address)
   */
  public String username;

  /**
   * password to log into the email service. Not required when oauth is set
   */
  public String password;

  /**
   * optional ID of an OAuth2Credentials function. If set, logs in via XOAUTH2 with an access token
   * instead of the password
   */
  public String oauth;

  /**
   * jakarta.mail properties passed to the session. These override the server preset
   */
  public Map<String, Object> properties;

  /**
   * true if neither a server preset nor properties are configured
   */
  boolean unconfigured() {
    return (server == null || "custom".equals(server))
        && (properties == null || properties.isEmpty());
  }

  /**
   * builds the session properties for smtp / imap: preset, OAuth mechanism, user properties
   */
  Properties properties(boolean smtp) {
    Properties prop = new Properties();
    if ("gmail".equals(server) || "office365".equals(server)) {
      boolean gmail = "gmail".equals(server);
      if (smtp) {
        prop.put("mail.transport.protocol", "smtp");
        prop.put("mail.smtp.host", gmail ? "smtp.gmail.com" : "smtp.office365.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.starttls.required", "true");
      } else {
        prop.put("mail.store.protocol", "imaps");
        prop.put("mail.imaps.host", gmail ? "imap.gmail.com" : "outlook.office365.com");
        prop.put("mail.imaps.port", "993");
      }
    }
    // fail instead of waiting forever if the server is unreachable or does not answer
    for (String protocol : new String[] {"smtp", "smtps", "imap", "imaps"}) {
      prop.put("mail." + protocol + ".connectiontimeout", "" + CONNECT_TIMEOUT);
      prop.put("mail." + protocol + ".timeout", "" + READ_TIMEOUT);
    }
    if (oauth != null) {
      prop.put("mail.smtp.auth.mechanisms", "XOAUTH2");
      prop.put("mail.imap.auth.mechanisms", "XOAUTH2");
      prop.put("mail.imaps.auth.mechanisms", "XOAUTH2");
    }
    if (properties != null)
      for (Entry<String, Object> e : properties.entrySet())
        prop.put(e.getKey(), "" + e.getValue());
    return prop;
  }

  /**
   * the session (no authenticator, credentials are passed on connect)
   */
  Session session(boolean smtp) {
    return Session.getInstance(properties(smtp));
  }

  /**
   * resolves the configured OAuth2Credentials function
   */
  OAuth2Credentials oauthCredentials() throws Exception {
    AbstractConfigurableFunction<?, ?> f = services.getConfig().getFunction(oauth);
    if (!(f instanceof OAuth2Credentials))
      throw new IllegalArgumentException(oauth + " is not an OAuth2Credentials function");
    f.init(sc, services, expressionService, readOnly);
    return (OAuth2Credentials) f;
  }

  /**
   * the secret used for login: access token or password
   */
  String secret() throws Exception {
    if (oauth != null)
      return oauthCredentials().accessToken(username);
    if (username == null)
      return null;
    return password();
  }

  public interface Login<T> {
    T run(String user, String secret) throws Exception;
  }

  /**
   * runs the login action. With OAuth, a rejected token is dropped from the cache and the action is
   * retried once with a fresh token (e.g. if the token was revoked before it expired)
   */
  <T> T withLogin(Login<T> action) throws Exception {
    try {
      return action.run(username, secret());
    } catch (AuthenticationFailedException e) {
      if (oauth == null)
        throw e;
      oauthCredentials().invalidate(username);
      return action.run(username, secret());
    }
  }
}
