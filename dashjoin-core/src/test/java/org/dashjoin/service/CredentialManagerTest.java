package org.dashjoin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

/**
 * checks the credentials manager
 */
@QuarkusTest
public class CredentialManagerTest {

  String pass = "This.is.my.Password.Dumbass";
  String key = "!MyDumbKey!";

  @Test
  public void encryptDecryptTest() {

    String e = CredentialManager.getInstance().encrypt(pass);
    System.err.println(e);
    String d = CredentialManager.getInstance().decrypt(e);
    System.err.println(d);
    Assertions.assertEquals(d, pass);
  }

  @Test
  public void hashTest() {
    String hash = CredentialManager.getInstance().hash(pass);
    System.err.println(hash);
    boolean valid = (CredentialManager.getInstance().checkPassword(pass, hash));
    Assertions.assertTrue(valid, "Hashed password is valid");
  }

  @Test
  public void encryptPrefixTest() {
    String e = CredentialManager.encryptCredential(pass);
    // Validate that double encoding does not break (idempotent operation)
    String e2 = CredentialManager.encryptCredential(e);
    String d = new String(CredentialManager.decryptCredential(e));
    String d2 = new String(CredentialManager.decryptCredential(e2));
    System.err.println(e);
    System.err.println(d);
    System.err.println(e2);
    System.err.println(d2);
    Assertions.assertEquals(pass, d, "Password must match");
    Assertions.assertEquals(pass, d2, "Password(2) must match");
  }

  @Test
  public void credentialCloseableTest() {
    String e = CredentialManager.encryptCredential(pass);
    try (CredentialManager.Credential c = new CredentialManager.Credential(e)) {
      char[] secret = c.getSecret();
      Assertions.assertEquals(pass, new String(secret), "Password must match");
    }
  }
}
