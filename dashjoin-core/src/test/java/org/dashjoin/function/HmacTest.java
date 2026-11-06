package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;

public class HmacTest {

  static final String FOX = "The quick brown fox jumps over the lazy dog";

  // Well-known HMAC-SHA256("key", FOX)
  static final String FOX_HEX = "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8";

  static SecurityContext roles(String... roles) {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    for (String role : roles)
      Mockito.when(sc.isUserInRole(ArgumentMatchers.eq(role))).thenReturn(true);
    return sc;
  }

  /** $hmac with an in-memory key "key" that the given roles may use */
  static Hmac hmac(SecurityContext sc, List<String> keyRoles) {
    Hmac f = new Hmac() {
      @Override
      protected Credentials credentials(String keyId) {
        Assertions.assertEquals("cdnKey", keyId);
        Credentials c = new Credentials();
        c.ID = keyId;
        c.roles = keyRoles;
        return c;
      }

      @Override
      protected String secret(Credentials key) {
        return "key";
      }
    };
    f.sc = sc;
    return f;
  }

  @Test
  public void testEncodings() throws Exception {
    Hmac f = hmac(roles("signer"), List.of("signer"));
    Assertions.assertEquals(FOX_HEX, f.run(Arrays.asList("cdnKey", FOX, null, "hex")));
    Assertions.assertEquals("97yD9DBThCSxMpjmqm+xQ+9NWaFJRhdZl0edvC0aPNg=",
        f.run(Arrays.asList("cdnKey", FOX)));
    Assertions.assertEquals("97yD9DBThCSxMpjmqm-xQ-9NWaFJRhdZl0edvC0aPNg",
        f.run(Arrays.asList("cdnKey", FOX, "HmacSHA256", "base64url")));
    Assertions.assertEquals(128,
        ((String) f.run(Arrays.asList("cdnKey", FOX, "HmacSHA512", "hex"))).length());
  }

  @Test
  public void testKeyRoles() throws Exception {
    // authenticated users may not use a key reserved for "signer"
    Assertions.assertThrows(NotAuthorizedException.class,
        () -> hmac(roles("authenticated"), List.of("signer")).run(Arrays.asList("cdnKey", FOX)));
    // a key without roles is admin only
    Assertions.assertThrows(NotAuthorizedException.class,
        () -> hmac(roles("signer"), null).run(Arrays.asList("cdnKey", FOX)));
    Assertions.assertNotNull(hmac(roles("admin"), null).run(Arrays.asList("cdnKey", FOX)));
  }

  @Test
  public void testOnlyCredentials() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hmac.asCredentials(new Invoke(), "echo"));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hmac.asCredentials(null, "missing"));
    Credentials c = new Credentials();
    Assertions.assertSame(c, Hmac.asCredentials(c, "cdnKey"));
  }

  @Test
  public void testInvalidArguments() {
    Hmac f = hmac(roles("signer"), List.of("signer"));
    Assertions.assertThrows(IllegalArgumentException.class, () -> f.run(Arrays.asList("cdnKey")));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> f.run(Arrays.asList("cdnKey", 42)));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> f.run(Arrays.asList("cdnKey", FOX, "HmacMD5")));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> f.run(Arrays.asList("cdnKey", FOX, null, "base32")));
  }
}
