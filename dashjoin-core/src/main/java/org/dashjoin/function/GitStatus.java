package org.dashjoin.function;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dashjoin.util.Home;
import org.dashjoin.util.MapUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class GitStatus extends AbstractFunction<Void, Object> {

  @Override
  public Object run(Void arg) throws Exception {
    try (Git git = new Git(new FileRepository(Home.get().getHome() + "/.git"))) {
      List<Map<String, Object>> res = new ArrayList<>();
      Status status = git.status().call();

      for (String s : status.getAdded())
        res.add(MapUtil.of("path", s, "type", "added", "diff", diff(git, s)));
      for (String s : status.getChanged())
        res.add(MapUtil.of("path", s, "type", "changed", "diff", diff(git, s)));
      for (String s : status.getRemoved())
        res.add(MapUtil.of("path", s, "type", "removed", "diff", diff(git, s)));
      for (String s : status.getMissing())
        res.add(MapUtil.of("path", s, "type", "missing", "diff", diff(git, s)));
      for (String s : status.getModified())
        res.add(MapUtil.of("path", s, "type", "modified", "diff", diff(git, s)));
      for (String s : status.getConflicting())
        res.add(MapUtil.of("path", s, "type", "conflicting", "diff", diff(git, s)));
      for (String s : status.getUntracked())
        res.add(MapUtil.of("path", s, "type", "untracked", "diff", diff(git, s)));

      return res;
    }
  }

  String diff(Git git, String path) throws GitAPIException {
    if (new File(Home.get().getHome() + '/' + path).length() > 100l * 1024l)
      return null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    git.diff().setOutputStream(out).setPathFilter(PathFilter.create(path)).call();
    return out.toString();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "gitStatus";
  }

  @Override
  public String getType() {
    return "read";
  }
}
