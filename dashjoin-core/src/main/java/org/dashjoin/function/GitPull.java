package org.dashjoin.function;

import org.dashjoin.util.Home;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitPull extends AbstractFunction<Void, String> {

  @Override
  public String run(Void arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform git operations");
    try (Git git = new Git(new FileRepository(Home.get().getHome() + "/.git"))) {
      git.pull().call();
      return "Ok";
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
