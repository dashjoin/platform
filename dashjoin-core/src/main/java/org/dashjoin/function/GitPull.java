package org.dashjoin.function;

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
