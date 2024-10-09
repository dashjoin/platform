package org.dashjoin.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.dashjoin.util.Home;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;
import io.quarkus.arc.Unremovable;

/**
 * Manages credentials (secrets, passwords, etc.) either in hashed or in encrypted format.
 * 
 * @author uli
 */
@ApplicationScoped
@Default
@Unremovable
public class CredentialManager {

  private final static Logger logger = Logger.getLogger(CredentialManager.class.getName());

  private static CredentialManager instance = null;

  private final String SECRETS_ID_FILE;

  private final String SID;

  public synchronized static CredentialManager getInstance() {
    if (instance == null) {
      instance = jakarta.enterprise.inject.spi.CDI.current().select(CredentialManager.class).get();
    }
    return instance;
  }

  @Inject
  CredentialManager(Home home) {
    SID = System.getenv("DJ_SID");
    // Use DJ_SID if provided
    if (SID != null) {
      logger.info("Using provided DJ_SID from environment");
      SECRETS_ID_FILE = null;
      return;
    }

    SECRETS_ID_FILE = System.getenv("DJ_SID_FILE") != null ? System.getenv("DJ_SID_FILE")
        : home.getFile("model/.secrets.id").getAbsolutePath();
    logger.info("Using SID file: " + SECRETS_ID_FILE);
    generateSecretsId();
  }

  /**
   * Generates the system unique SECRETS_ID_FILE.
   */
  private void generateSecretsId() {
    // Use DJ_SID if provided
    if (SID != null)
      return;

    File secretsFile = new File(SECRETS_ID_FILE);
    if (!secretsFile.exists()) {
      logger.info("New system - creating " + SECRETS_ID_FILE);

      if (!secretsFile.getParentFile().exists())
        if (!secretsFile.getParentFile().mkdirs())
          throw new RuntimeException("Error creating model folder");

      byte[] secretBytes = new byte[32];
      try {
        SecureRandom.getInstanceStrong().nextBytes(secretBytes);
      } catch (NoSuchAlgorithmException e) {
        logger.log(Level.SEVERE, "Crypo config exception", e);
      }
      String secret = Base64.getEncoder().encodeToString(secretBytes);

      try (FileWriter w = new FileWriter(secretsFile, StandardCharsets.UTF_8)) {
        w.write(secret);
      } catch (IOException e) {
        logger.log(Level.SEVERE,
            "Could not write " + SECRETS_ID_FILE + " - maybe missing file access rights?", e);
      }
    }
  }

  /**
   * Gets the system unique secret. This is stored in SECRETS_ID_FILE which should be protected.
   * 
   * @return
   */
  private char[] getSecret() {
    // Use DJ_SID if provided
    if (SID != null)
      return SID.toCharArray();

    try (FileReader r = new FileReader(SECRETS_ID_FILE, StandardCharsets.UTF_8)) {
      char[] buf = new char[1024];
      int sz;
      sz = r.read(buf);
      char[] res = new char[sz];
      System.arraycopy(buf, 0, res, 0, sz);
      destroySecret(buf);
      return res;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot read " + SECRETS_ID_FILE, e);
      throw new RuntimeException("Cannot read " + SECRETS_ID_FILE, e);
    }
  }

  /**
   * Cleans the secret from memory. This makes memory read attacks harder (timing)
   * 
   * @param secret
   */
  private void destroySecret(char[] secret) {
    Arrays.fill(secret, ' ');
  }

  final static String PREFIX_HASH = "DJ0#\b";
  final static String PREFIX_ENCRYPTED = "DJ1#\b";

  /**
   * Encrypts the given text using the system unique key.
   * 
   * @param text
   * @return
   */
  public String encrypt(String text) {
    char[] s = getSecret();
    String res = encrypt(text, s);
    destroySecret(s);
    return res;
  }

  /**
   * Encrypts the given credential using the system's unique secret. Idempotent.
   */
  public static String encryptCredential(String cred) {
    // If it's already encrypted
    if (cred.startsWith(PREFIX_HASH) || cred.startsWith(PREFIX_ENCRYPTED)) {
      return cred;
    }
    return PREFIX_ENCRYPTED + CredentialManager.getInstance().encrypt(cred);
  }

  /**
   * Decrypts the given encrypted credential for using it in a login etc.
   */
  public static char[] decryptCredential(String crypt) {
    if (!crypt.startsWith(PREFIX_ENCRYPTED)) {
      return crypt.toCharArray();
    }
    String e = crypt.substring(PREFIX_ENCRYPTED.length());
    return CredentialManager.getInstance().decrypt(e).toCharArray();
  }

  /**
   * Decrypts the given text using the system unique key.
   * 
   * @param text
   * @return
   */
  public String decrypt(String text) {
    char[] s = getSecret();
    String res = decrypt(text, s);
    destroySecret(s);
    return res;
  }

  public String encrypt(String text, char[] key) {
    StrongTextEncryptor ste = new StrongTextEncryptor();
    ste.setPasswordCharArray(key);
    return ste.encrypt(text);
  }

  public String decrypt(String text, char[] key) {
    StrongTextEncryptor ste = new StrongTextEncryptor();
    ste.setPasswordCharArray(key);
    return ste.decrypt(text);
  }

  /**
   * Generates password hash from the given password.
   * 
   * @param plainPassword
   * @return
   */
  public String hash(String plainPassword) {
    StrongPasswordEncryptor spe = new StrongPasswordEncryptor();
    return spe.encryptPassword(plainPassword);
  }

  /**
   * Checks whether the provided password is valid for the hash.
   * 
   * @param plainPassword
   * @param hash
   * @return
   */
  public boolean checkPassword(String plainPassword, String hash) {
    StrongPasswordEncryptor spe = new StrongPasswordEncryptor();
    return spe.checkPassword(plainPassword, hash);
  }

  /**
   * Credential class which takes care to keep the secret as short in memory as required. Usage:
   * 
   * try (Credential c = new Credential(encryptedCredential)) {
   * 
   * ... login( c.getSecret() ); ...
   * 
   * }
   * 
   * @author uli
   *
   */
  public static class Credential implements java.lang.AutoCloseable {

    private String crypt;
    private char[] plain = null;

    public Credential(String crypt) {
      this.crypt = crypt;
    }

    @Override
    public synchronized void close() {
      if (plain != null) {
        CredentialManager.getInstance().destroySecret(plain);
        plain = null;
      }
    }

    public synchronized char[] getSecret() {
      if (crypt == null)
        return null;

      if (plain == null) {
        plain = CredentialManager.decryptCredential(crypt);
      }
      return plain;
    }
  }
}
