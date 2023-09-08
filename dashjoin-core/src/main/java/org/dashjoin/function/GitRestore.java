package org.dashjoin.function;

import java.io.File;
import org.dashjoin.util.Home;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitRestore extends AbstractFunction<String, Void> {

  @Override
  public Void run(String arg) throws Exception {
    if (arg == null)
      throw new IllegalArgumentException("Arguments required: $gitRestore(path)");
    try (Git git = new Git(new FileRepository(Home.get().getHome() + "/.git"))) {
      Status status = git.status().addPath(arg).call();
      if (status.getUntracked().contains(arg))
        new File(Home.get().getHome() + "/" + arg).delete();
      else
        git.checkout().addPath(arg).call();
      return null;
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
