package org.dashjoin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import org.dashjoin.util.Home;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CredentialKeyDerivationTest {
  @TempDir Path directory;

  @Test void derivesStablePurposeSpecificKeysWithoutChangingStoredCredentials() throws Exception {
    assumeTrue(System.getenv("DJ_SID") == null && System.getenv("DJ_SID_FILE") == null);
    Files.createDirectories(directory.resolve("model"));
    Path secret = directory.resolve("model/.secrets.id");
    String root = "0123456789abcdef0123456789abcdef";
    Files.writeString(secret, root);
    var home = new Home(Optional.of(directory.toString()), Optional.empty());
    var first = new CredentialManager(home);
    String encrypted = first.encrypt("test-credential");
    byte[] key = first.deriveKey("dashjoin/session-signing/v1");
    assertEquals(32, key.length);
    // Independently calculated HKDF-SHA256 vector using UTF-8 root text and an empty salt.
    assertEquals("b823d288e1077625959e64a4d7a844b685d021d48237bce5688e255c6dc77f39", HexFormat.of().formatHex(key));
    var second = new CredentialManager(home);
    assertArrayEquals(key, second.deriveKey("dashjoin/session-signing/v1"));
    assertFalse(Arrays.equals(key, second.deriveKey("dashjoin/session-signing/v2")));
    assertFalse(Arrays.equals(key, second.deriveKey("another-purpose")));
    assertEquals("test-credential", second.decrypt(encrypted));
    assertEquals(root, Files.readString(secret));
    assertThrows(IllegalArgumentException.class, () -> first.deriveKey(""));
    Files.writeString(secret, "abcdef0123456789abcdef0123456789");
    assertFalse(Arrays.equals(key, second.deriveKey("dashjoin/session-signing/v1")));
  }
}
