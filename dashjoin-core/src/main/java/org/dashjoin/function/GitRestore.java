package org.dashjoin.function;

import java.io.File;
import org.dashjoin.util.Home;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitRestore extends AbstractFunction<String, String> {

  @Override
  public String run(String arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform git operations");
    if (arg == null)
      throw new IllegalArgumentException("Arguments required: $gitRestore(path)");
    try (Git git = new Git(new FileRepository(Home.get().getHome() + "/.git"))) {
      Status status = git.status().addPath(arg).call();
      if (status.getUntracked().contains(arg))
        new File(Home.get().getHome() + "/" + arg).delete();
      else
        git.checkout().addPath(arg).call();
      return "Ok";
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
