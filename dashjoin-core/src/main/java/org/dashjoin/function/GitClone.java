package org.dashjoin.function;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import lombok.extern.java.Log;

@Log
public class GitClone extends AbstractFunction<String, Void> {

  @Override
  public Void run(String url) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");

    if (services.getTenantManager().getHomePrefix().isBlank())
      throw new Exception(
          "Ad hoc gitClone is only allowed in playground mode. To install an App, use the DASHJOIN_APPURL environment variable.");

    log.info("deleting home folder");
    FileUtils.deleteDirectory(new File(services.getTenantHome()));

    log.info("cloning repository");
    CloneCommand cloneCommand = Git.cloneRepository();
    cloneCommand.setDirectory(new File(services.getTenantHome()));
    cloneCommand.setURI(url);
    cloneCommand.call();

    log.info("collecting metadata");
    services.getConfig().metadataCollection();

    log.info("done");
    return null;
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
