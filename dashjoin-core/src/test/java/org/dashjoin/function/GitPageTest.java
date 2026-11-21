package org.dashjoin.function;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import jakarta.ws.rs.core.SecurityContext;

/**
 * What the App / Git page shows and does must match what the config API reads and writes: edits
 * made through the API show up in $gitStatus, and after $gitRestore, $gitPull or $gitCheckout the
 * API returns the version on disk. See AppRepoTest for the setup and how to run this with another
 * storage.
 *
 * Not covered: $gitCommit (needs dashjoin.appurl, credentials and a push target).
 */
public class GitPageTest extends AppRepoTest {

  final Table page = Table.ofName("page");

  static final String A = "model/page/GitTestA.json";
  static final String B = "model/page/GitTestB.json";

  @Override
  protected void createApp() throws Exception {
    write(remote, A, "{\"ID\": \"GitTestA\", \"title\": \"v1\"}");
  }

  Map<String, Object> read(String id) throws Exception {
    return config.read(page, Map.of("ID", id));
  }

  boolean listed(String id) throws Exception {
    QueryMeta q = new QueryMeta();
    q.query = "page";
    for (Map<String, Object> r : config.query(q, null))
      if (id.equals(r.get("ID")))
        return true;
    return false;
  }

  /** read A by ID and through a query, so a cache (if any) holds it before the git operation */
  void warmUp() throws Exception {
    Assertions.assertNotNull(read("GitTestA"));
    listed("GitTestA");
  }

  @Test
  public void cleanAfterClone() throws Exception {
    Assertions.assertEquals(Map.of(), status());
    Assertions.assertEquals("v1", read("GitTestA").get("title"));
  }

  @Test
  public void updateShowsAsModified() throws Exception {
    config.update(page, Map.of("ID", "GitTestA"), new HashMap<>(Map.of("title", "edited")));
    Assertions.assertEquals(Map.of(A, "modified"), status());
    Assertions.assertTrue(Files.readString(appHome.resolve(A)).contains("edited"));
  }

  @Test
  public void createShowsAsUntracked() throws Exception {
    config.create(page, new HashMap<>(Map.of("ID", "GitTestB", "title", "new")));
    Assertions.assertEquals(Map.of(B, "untracked"), status());
  }

  @Test
  public void deleteShowsAsMissing() throws Exception {
    config.delete(page, Map.of("ID", "GitTestA"));
    Assertions.assertEquals(Map.of(A, "missing"), status());
  }

  @Test
  public void restoreRevertsUpdate() throws Exception {
    config.update(page, Map.of("ID", "GitTestA"), new HashMap<>(Map.of("title", "edited")));
    warmUp();
    fn(new GitRestore()).run(A);
    Assertions.assertEquals("v1", read("GitTestA").get("title"));
    Assertions.assertEquals(Map.of(), status());
  }

  @Test
  public void restoreRemovesCreatedRecord() throws Exception {
    config.create(page, new HashMap<>(Map.of("ID", "GitTestB", "title", "new")));
    Assertions.assertNotNull(read("GitTestB"));
    Assertions.assertTrue(listed("GitTestB"));
    fn(new GitRestore()).run(B);
    Assertions.assertNull(read("GitTestB"));
    Assertions.assertFalse(listed("GitTestB"));
    Assertions.assertEquals(Map.of(), status());
  }

  @Test
  public void restoreBringsBackDeletedRecord() throws Exception {
    warmUp();
    config.delete(page, Map.of("ID", "GitTestA"));
    Assertions.assertNull(read("GitTestA"));
    fn(new GitRestore()).run(A);
    Assertions.assertEquals("v1", read("GitTestA").get("title"));
    Assertions.assertTrue(listed("GitTestA"));
    Assertions.assertEquals(Map.of(), status());
  }

  @Test
  public void pullShowsNewVersion() throws Exception {
    warmUp();
    write(remote, A, "{\"ID\": \"GitTestA\", \"title\": \"v2\"}");
    write(remote, B, "{\"ID\": \"GitTestB\", \"title\": \"added in git\"}");
    commit(origin, "v2");

    fn(new GitPull()).run(null);
    Assertions.assertEquals("v2", read("GitTestA").get("title"));
    Assertions.assertEquals("added in git", read("GitTestB").get("title"));
    Assertions.assertTrue(listed("GitTestB"));
    Assertions.assertEquals(Map.of(), status());
  }

  @Test
  public void pullRemovesRecordDeletedInGit() throws Exception {
    warmUp();
    origin.rm().addFilepattern(A).call();
    origin.commit().setMessage("remove A").call();

    fn(new GitPull()).run(null);
    Assertions.assertNull(read("GitTestA"));
    Assertions.assertFalse(listed("GitTestA"));
  }

  @Test
  public void checkoutSwitchesBranch() throws Exception {
    origin.branchCreate().setName("feature").call();
    origin.checkout().setName("feature").call();
    write(remote, A, "{\"ID\": \"GitTestA\", \"title\": \"feature\"}");
    write(remote, B, "{\"ID\": \"GitTestB\", \"title\": \"only on feature\"}");
    commit(origin, "feature");
    origin.checkout().setName("master").call();

    warmUp();
    Assertions.assertNull(fn(new GitCheckout()).run("feature"));
    Assertions.assertEquals("feature", fn(new GitCheckout()).run(null));
    Assertions.assertEquals("feature", read("GitTestA").get("title"));
    Assertions.assertTrue(listed("GitTestB"));

    fn(new GitCheckout()).run("master");
    Assertions.assertEquals("v1", read("GitTestA").get("title"));
    Assertions.assertNull(read("GitTestB"));
    Assertions.assertFalse(listed("GitTestB"));
  }

  @Test
  public void gitFunctionsRequireAdmin() throws Exception {
    SecurityContext user = Mockito.mock(SecurityContext.class);
    for (AbstractFunction<?, ?> f : Arrays.asList(new GitStatus(), new GitPull(),
        new GitRestore(), new GitCheckout())) {
      f.init(user, services, expressionService, false);
      Exception e = Assertions.assertThrows(Exception.class, () -> {
        if (f instanceof GitRestore)
          ((GitRestore) f).run(A);
        else if (f instanceof GitCheckout)
          ((GitCheckout) f).run(null);
        else if (f instanceof GitStatus)
          ((GitStatus) f).run(null);
        else
          ((GitPull) f).run(null);
      });
      Assertions.assertTrue(e.getMessage().contains("admin"), f + ": " + e.getMessage());
    }
  }
}
