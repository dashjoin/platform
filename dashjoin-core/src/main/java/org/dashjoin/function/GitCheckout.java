package org.dashjoin.function;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;

public class GitCheckout extends AbstractFunction<String, String> {

  @Override
  public String run(String branchName) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");
    try (Git git = new Git(new FileRepository(services.getTenantHome() + "/.git"))) {

      // if no branch name, return current branch
      if (branchName == null)
        return git.getRepository().getBranch();

      // fetch first so local repo knows about the remote branch
      git.fetch().setRemote("origin").call();

      boolean localBranchExists = git.getRepository().findRef("refs/heads/" + branchName) != null;
      if (localBranchExists)
        // Case A: Branch already exists locally. Just switch to it.
        git.checkout().setCreateBranch(false).setName(branchName).call();
      else
        // Case B: Branch does not exist locally. Create it and track origin.
        git.checkout().setCreateBranch(true).setName(branchName)
            .setStartPoint("origin/" + branchName)
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).call();
    }
    return null;
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }

  @Override
  public String getType() {
    return "write";
  }

  public String getSignature() {
    return "<s?>";
  }
}
