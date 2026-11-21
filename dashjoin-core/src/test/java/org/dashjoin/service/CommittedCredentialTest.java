package org.dashjoin.service;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.dashjoin.function.AppRepoTest;
import org.dashjoin.function.GitPull;
import org.dashjoin.function.GitRestore;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Passwords of a credential that is committed to git: password() finds the stored (encrypted)
 * value, API readers only see ********, and the mask the UI sends back does not overwrite the
 * password. Also after $gitRestore and $gitPull. See AppRepoTest for the setup and how to run this
 * with another storage.
 */
public class CommittedCredentialTest extends AppRepoTest {

  final Table function = Table.ofName("dj-function");

  static final String CRED = "model/dj-function/GitTestCred.json";

  /**
   * encrypted passwords (CredentialManager.PREFIX_ENCRYPTED). Writing an already encrypted value
   * stores it as is, so the tests need no secret key
   */
  static final String ENC = CredentialManager.PREFIX_ENCRYPTED + "committed-secret";
  static final String ENC2 = CredentialManager.PREFIX_ENCRYPTED + "new-secret";

  static String credential(String encrypted) {
    return "{\"ID\": \"GitTestCred\", \"djClassName\": \"org.dashjoin.function.Credentials\", "
        + "\"username\": \"u\", \"password\": \"" + encrypted.replace("\b", "\\b") + "\"}";
  }

  @Override
  protected void createApp() throws Exception {
    write(remote, CRED, credential(ENC));
  }

  /** the stored (encrypted) password, as connections and $curl get it */
  String password() throws Exception {
    return ((Config) config).password("dj-function", "GitTestCred");
  }

  Map<String, Object> read() throws Exception {
    return config.read(function, Map.of("ID", "GitTestCred"));
  }

  Map<String, Object> listed() throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "dj-function";
    for (Map<String, Object> r : config.query(q, null))
      if ("GitTestCred".equals(r.get("ID")))
        return r;
    return null;
  }

  @Test
  public void committedPasswordIsFound() throws Exception {
    Assertions.assertEquals(ENC, password());
  }

  @Test
  public void committedPasswordIsMasked() throws Exception {
    Assertions.assertEquals("********", read().get("password"));
    Assertions.assertEquals("********", listed().get("password"));
    Assertions.assertEquals("u", read().get("username"));
  }

  @Test
  public void updateKeepsCommittedPassword() throws Exception {
    // the UI sends the masked password back with the other fields
    config.update(function, Map.of("ID", "GitTestCred"),
        new HashMap<>(Map.of("username", "u2", "password", "********")));
    Assertions.assertEquals(ENC, password());
    Assertions.assertEquals("u2", read().get("username"));
    Assertions.assertEquals("********", read().get("password"));
    Assertions.assertEquals(Map.of(CRED, "modified"), status());
    Assertions.assertFalse(Files.readString(appHome.resolve(CRED)).contains("********"));
  }

  @Test
  public void newPasswordReplacesCommittedOne() throws Exception {
    config.update(function, Map.of("ID", "GitTestCred"), new HashMap<>(Map.of("password", ENC2)));
    Assertions.assertEquals(ENC2, password());
    Assertions.assertEquals("********", read().get("password"));
    Assertions.assertEquals("********", listed().get("password"));
    Assertions.assertEquals(Map.of(CRED, "modified"), status());
  }

  @Test
  public void restoreBringsBackCommittedPassword() throws Exception {
    config.update(function, Map.of("ID", "GitTestCred"), new HashMap<>(Map.of("password", ENC2)));
    fn(new GitRestore()).run(CRED);
    Assertions.assertEquals(ENC, password());
    Assertions.assertEquals("********", read().get("password"));
    Assertions.assertEquals(Map.of(), status());
  }

  @Test
  public void pullUpdatesPassword() throws Exception {
    Assertions.assertEquals(ENC, password());
    write(remote, CRED, credential(ENC2));
    commit(origin, "rotate password");
    fn(new GitPull()).run(null);
    Assertions.assertEquals(ENC2, password());
    Assertions.assertEquals("********", read().get("password"));
    Assertions.assertEquals("********", listed().get("password"));
  }
}
