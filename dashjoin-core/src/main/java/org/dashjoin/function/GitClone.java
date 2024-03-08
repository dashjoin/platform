package org.dashjoin.function;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import lombok.extern.java.Log;

@Log
public class GitClone extends AbstractFunction<String, String> {

  @Override
  public String run(String url) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");

    if (services.getTenantManager().getHomePrefix().isBlank())
      throw new Exception(
          "Ad hoc gitClone is only allowed in playground mode. To install an App, use the DASHJOIN_APPURL environment variable.");

    File model = new File(services.getTenantHome(), "model");
    if (hasFiles(new File(model, "page")))
      throw new Exception("Please delete all custom dashboard pages before installing a new App");
    if (hasFiles(new File(model, "dj-function")))
      throw new Exception("Please delete all functions before installing a new App");
    if (hasFiles(new File(model, "dj-query-catalog")))
      throw new Exception("Please delete all queries before installing a new App");
    if (hasFiles(new File(model, "dj-database")))
      throw new Exception("Please delete all custom databases before installing a new App");

    log.info("deleting home folder");
    FileUtils.deleteDirectory(new File(services.getTenantHome()));

    log.info("cloning repository");
    CloneCommand cloneCommand = Git.cloneRepository();
    cloneCommand.setDirectory(new File(services.getTenantHome()));
    cloneCommand.setURI(url);
    cloneCommand.call();

    log.info("collecting metadata");
    services.getConfig().getConfigDatabase().connectAndCollectMetadata();

    log.info("done");
    return "Installation successful - please refresh your browser";
  }

  boolean hasFiles(File file) {
    String[] files = file.list();
    if (files == null)
      return false;
    else
      return files.length > 0;
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }

  @Override
  public String getType() {
    return "write";
  }
}
