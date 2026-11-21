package org.dashjoin.function;

import java.io.File;
import org.dashjoin.service.JSONFileCache;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitPull extends AbstractFunction<Void, Void> {

  @Override
  public Void run(Void arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");
    try (Git git = new Git(new FileRepository(services.getTenantHome() + "/.git"))) {
      git.pull().call();
      return null;
    } finally {
      // the pull changes model files behind the config DB's back
      JSONFileCache.clear(new File(services.getTenantHome(), "model"));
    }
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "gitPull";
  }

  @Override
  public String getType() {
    return "write";
  }
}
