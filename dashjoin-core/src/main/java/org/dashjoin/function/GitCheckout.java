package org.dashjoin.function;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

public class GitCheckout extends AbstractFunction<String, String> {

  @Override
  public String run(String arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");
    try (Git git = new Git(new FileRepository(services.getTenantHome() + "/.git"))) {
      if (arg == null)
        return git.getRepository().getBranch();
      switchBranch(git, arg);
      return null;
    }
  }

  public static void switchBranch(Git git, String targetBranch) throws Exception {
    git.fetch().setRemote("origin").call();
    var checkoutCmd = git.checkout().setName(targetBranch);
    if (isBranchRemoteOnly(git.getRepository(), targetBranch))
      checkoutCmd.setCreateBranch(true).setStartPoint("origin/" + targetBranch)
          .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
    checkoutCmd.call();
  }

  static boolean isBranchRemoteOnly(Repository repository, String branchName) throws Exception {
    boolean localExists = repository.findRef("refs/heads/" + branchName) != null;
    boolean remoteExists = repository.findRef("refs/remotes/origin/" + branchName) != null;
    return !localExists && remoteExists;
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
