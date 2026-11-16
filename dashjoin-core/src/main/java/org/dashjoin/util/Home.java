package org.dashjoin.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.logging.Level;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Dashjoin Home folder encapsulation.
 */
@ApplicationScoped
public class Home {
  private static final Logger log = Logger.getLogger(Home.class.getName());

  String home;
  String fileHome;

  static Home instance;

  public synchronized static Home get() {
    if (instance == null) {
      Instance<Home> inst = jakarta.enterprise.inject.spi.CDI.current().select(Home.class);
      if (inst.isUnsatisfied()) {
        log.warning("Dashjoin Home is unresolvable - using default");
        instance = new Home(Optional.empty(), Optional.empty());
      } else
        instance = inst.get();
    }
    return instance;
  }

  @Inject
  public Home(@ConfigProperty(name = "dashjoin.home", defaultValue = "") Optional<String> home,
      @ConfigProperty(name = "dashjoin.appurl") Optional<String> appurl) {
    this.home = home.orElse("");
    this.fileHome = this.home.isBlank() ? new File("").getAbsolutePath()
        : this.home.replace("$HOME", System.getProperty("user.home"));

    log.info("DASHJOIN_HOME = " + fileHome);

    ensureHomeExists();

    // do a git checkout
    if (appurl.isPresent()) {
      try {
        String[] files = new File(fileHome).list();
        if (files != null && files.length > 0) {

          if (!new File(fileHome + "/.git").exists())
            throw new Exception(
                "Directory " + fileHome + " must be empty or contain a .git folder");

          boolean pull = ConfigProvider.getConfig()
              .getOptionalValue("dashjoin.git.pull", Boolean.class).orElse(true);
          if (pull)
            try (Git git = new Git(new FileRepository(fileHome + "/.git"))) {
              log.info("git pull ...");
              git.pull().setCredentialsProvider(getCredentials()).call();
            }
          else
            log.info("git pull disabled (DASHJOIN_GIT_PULL=false)");
        } else {
          log.info("git clone ...");
          CloneCommand cloneCommand = Git.cloneRepository();
          cloneCommand.setDirectory(new File(fileHome));
          UrlBranch ub = getUrlBranch(appurl.get());
          cloneCommand.setURI(ub.url);
          cloneCommand.setCredentialsProvider(getCredentials());
          if (ub.branch != null)
            cloneCommand.setBranch(ub.branch);
          cloneCommand.call();
        }
      } catch (Exception e) {
        log.log(Level.SEVERE, "git error", e);
      }
    }

    Home.instance = this;
  }

  void ensureHomeExists() {
    File f = new File(fileHome);
    if (!f.exists() && !f.mkdirs())
      log.warning("Cannot create home: " + f);
  }

  public String getHome() {
    return fileHome;
  }

  public File getFile(String path) {
    return new File(fileHome, path);
  }

  /**
   * call this instead of getHome(url.getPath()) in order to decode # etc
   */
  public File getFile(URL url) {
    return new File(fileHome, URLDecoder.decode(url.getPath(), Charset.defaultCharset()));
  }

  static class UrlBranch {
    String url;
    String branch;
  }

  /**
   * Git credentials from dashjoin.git.user / dashjoin.git.token (DASHJOIN_GIT_USER /
   * DASHJOIN_GIT_TOKEN). Falls back to user:token@ in dashjoin.appurl. Returns null if neither is
   * configured.
   */
  public static CredentialsProvider getCredentials() {
    Config c = ConfigProvider.getConfig();
    Optional<String> user = c.getOptionalValue("dashjoin.git.user", String.class);
    Optional<String> token = c.getOptionalValue("dashjoin.git.token", String.class);
    if (user.isPresent() && token.isPresent())
      return new UsernamePasswordCredentialsProvider(user.get(), token.get());

    Optional<String> spec = c.getOptionalValue("dashjoin.appurl", String.class);
    if (spec.isPresent())
      try {
        String ui = new URL(getUrlBranch(spec.get()).url).getUserInfo();
        if (ui != null && ui.contains(":")) {
          String[] a = ui.split(":", 2);
          return new UsernamePasswordCredentialsProvider(a[0], a[1]);
        }
      } catch (Exception ignore) {
      }
    return null;
  }

  static UrlBranch getUrlBranch(String s) {
    UrlBranch res = new UrlBranch();
    String[] a = s.split("#");
    if (a.length == 2) {
      res.url = a[0];
      res.branch = a[1];
    } else
      res.url = s;
    return res;
  }
}
