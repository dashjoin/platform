package org.dashjoin.function;

import java.io.File;
import org.dashjoin.service.JSONFileCache;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitRestore extends AbstractFunction<String, Void> {

  @Override
  public Void run(String arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");
    if (arg == null)
      throw new IllegalArgumentException("Arguments required: $gitRestore(path)");
    try (Git git = new Git(new FileRepository(services.getTenantHome() + "/.git"))) {
      Status status = git.status().addPath(arg).call();
      if (status.getUntracked().contains(arg))
        new File(services.getTenantHome() + "/" + arg).delete();
      else
        git.checkout().addPath(arg).call();
      return null;
    } finally {
      // arg may be a record or one of its externalized string files
      JSONFileCache.clear(new File(services.getTenantHome(), "model"));
    }
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }

  @Override
  public String getID() {
    return "gitRestore";
  }

  @Override
  public String getType() {
    return "write";
  }
}
