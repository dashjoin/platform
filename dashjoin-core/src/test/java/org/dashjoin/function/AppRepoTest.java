package org.dashjoin.function;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.service.JSONClassloaderDatabase;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.service.JSONFileDatabase;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.Services;
import org.dashjoin.service.UnionDatabase;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Base for tests of an instance whose app comes from git: a temporary app repository (origin), a
 * clone of it as the instance's home folder, the config database on top of that home folder, and
 * the git functions running as admin.
 *
 * The tests only use the config API and the git functions, so they hold for every way the config
 * database is stored. On master this runs in file mode. A branch that changes the storage (cache,
 * SQL) overrides config() (and setup / teardown) in a subclass of each test to run the same tests in
 * its mode.
 */
public abstract class AppRepoTest {

  @TempDir
  protected Path tmp;

  /** the app repository the instance was cloned from (origin) */
  protected Path remote;

  /** the instance's home folder (clone of remote) */
  protected Path appHome;

  protected Git origin;
  protected Services services;
  protected ExpressionService expressionService;
  protected SecurityContext admin;

  /** the config database the UI reads and writes */
  protected UnionDatabase config;

  /**
   * write the files of the app's first commit into remote
   */
  protected abstract void createApp() throws Exception;

  @BeforeEach
  public void setup() throws Exception {
    remote = tmp.resolve("remote");
    appHome = tmp.resolve("home");

    origin = Git.init().setDirectory(remote.toFile()).setInitialBranch("master").call();
    createApp();
    commit(origin, "initial");
    Git.cloneRepository().setURI(remote.toUri().toString()).setDirectory(appHome.toFile()).call()
        .close();

    services = new Services() {
      @Override
      public String getTenantHome() {
        return appHome.toString();
      }
    };
    expressionService = Mockito.mock(ExpressionService.class);
    admin = Mockito.mock(SecurityContext.class);
    Mockito.when(admin.isUserInRole("admin")).thenReturn(true);

    config = config();
  }

  @AfterEach
  public void teardown() {
    origin.close();
  }

  /**
   * the config database the UI reads and writes, on top of the home folder. Override to run the
   * tests with another storage
   */
  protected UnionDatabase config() throws Exception {
    JSONFileDatabase file = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return appHome.resolve(path).toFile();
      }
    };
    PojoDatabase c = new PojoDatabase(services);
    setField(c, "_cache", new ConcurrentHashMap<>());
    setField(c, "_user", file);
    List<JSONDatabase> dbs = new CopyOnWriteArrayList<>();
    dbs.add(platform());
    setField(c, "_dbs", dbs);
    c.name = "config";
    c.ID = "dj/config";
    return c;
  }

  /**
   * the read-only defaults the platform ships on the classpath. Override to simulate a platform
   * version
   */
  protected JSONDatabase platform() {
    return new JSONClassloaderDatabase();
  }

  protected static void setField(Object o, String name, Object value) throws Exception {
    Class<?> c = o.getClass();
    while (c != null) {
      try {
        java.lang.reflect.Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        f.set(o, value);
        return;
      } catch (NoSuchFieldException e) {
        c = c.getSuperclass();
      }
    }
    throw new NoSuchFieldException(name);
  }

  protected static void write(Path repo, String path, String content) throws Exception {
    Path f = repo.resolve(path);
    Files.createDirectories(f.getParent());
    Files.writeString(f, content);
  }

  protected static void commit(Git git, String message) throws Exception {
    git.add().addFilepattern(".").call();
    git.commit().setMessage(message).call();
  }

  /**
   * the function, initialized to run as admin
   */
  protected <T extends AbstractFunction<?, ?>> T fn(T f) {
    f.init(admin, services, expressionService, false);
    return f;
  }

  /**
   * $gitStatus() as path to type (modified, untracked, missing, ...)
   */
  @SuppressWarnings("unchecked")
  protected Map<String, String> status() throws Exception {
    Map<String, String> res = new HashMap<>();
    for (Map<String, Object> s : (List<Map<String, Object>>) fn(new GitStatus()).run(null))
      res.put((String) s.get("path"), (String) s.get("type"));
    return res;
  }
}
