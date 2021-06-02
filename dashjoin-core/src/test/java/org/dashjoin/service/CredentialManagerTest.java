package org.dashjoin.service;

import org.junit.Assert;
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
    Assert.assertEquals(d, pass);
  }

  @Test
  public void hashTest() {
    String hash = CredentialManager.getInstance().hash(pass);
    System.err.println(hash);
    boolean valid = (CredentialManager.getInstance().checkPassword(pass, hash));
    Assert.assertTrue("Hashed password is valid", valid);
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
    Assert.assertEquals("Password must match", pass, d);
    Assert.assertEquals("Password(2) must match", pass, d2);
  }

  @Test
  public void credentialCloseableTest() {
    String e = CredentialManager.encryptCredential(pass);
    try (CredentialManager.Credential c = new CredentialManager.Credential(e)) {
      char[] secret = c.getSecret();
      Assert.assertEquals("Password must match", pass, new String(secret));
    }
  }
}
