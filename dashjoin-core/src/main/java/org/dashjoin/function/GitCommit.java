package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.dashjoin.util.Home;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.microprofile.config.ConfigProvider;

public class GitCommit extends AbstractVarArgFunction<Void> {

  @SuppressWarnings("rawtypes")
  @Override
  public Void run(List args) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to perform Git operations");
    String spec = ConfigProvider.getConfig().getConfigValue("dashjoin.appurl").getValue();
    if (spec == null)
      throw new Exception("Cannot commit: No repository configured.");
    CredentialsProvider credentials = Home.getCredentials();
    if (credentials == null)
      throw new IllegalArgumentException(
          "No Git credentials configured (DASHJOIN_GIT_USER / DASHJOIN_GIT_TOKEN or in the App URL)");
    String message = (String) args.get(0);
    List paths = (List) args.get(1);
    if (message == null || paths == null || paths.isEmpty())
      throw new IllegalArgumentException("Arguments required: $gitCommit(message, [paths])");
    try (Git git = new Git(new FileRepository(services.getTenantHome() + "/.git"))) {
      Status status = git.status().call();
      Set<String> missing = status.getMissing();
      for (Object s : paths)
        if (missing.contains(s))
          git.rm().addFilepattern("" + s).call();
        else
          git.add().addFilepattern("" + s).call();
      git.commit().setMessage(message).call();
      git.push().setCredentialsProvider(credentials).call();
      return null;
    }
  }

  @Override
  public String getID() {
    return "gitCommit";
  }

  @Override
  public String getType() {
    return "write";
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, List.class);
  }
}
