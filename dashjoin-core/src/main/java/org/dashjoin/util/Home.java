package org.dashjoin.util;

import java.io.File;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import lombok.extern.java.Log;

/**
 * Dashjoin Home folder encapsulation.
 */
@ApplicationScoped
@Log
public class Home {

  String home;
  String fileHome;

  @Inject
  Home(@ConfigProperty(name = "dashjoin.home", defaultValue = "") Optional<String> home) {
    this.home = home.orElse("");
    this.fileHome = this.home.isBlank() ? new File("").getAbsolutePath()
        : this.home.replace("$HOME", System.getProperty("user.home"));

    log.info("DASHJOIN_HOME = " + fileHome);

    ensureHomeExists();
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
}
