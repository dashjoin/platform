package org.dashjoin.function;

import java.io.File;
import org.dashjoin.service.JSONFileCache;

/**
 * clears the config DB record cache (dashjoin.configdb.cache) of the current tenant. Needed after
 * model files were changed without going through Dashjoin, e.g. edited by hand on the volume
 */
public class DjClearConfigCache extends AbstractFunction<Void, Void> {

  @Override
  public Void run(Void arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to clear the config cache");
    JSONFileCache.clear(new File(services.getTenantHome(), "model"));
    return null;
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djClearConfigCache";
  }

  @Override
  public String getType() {
    return "write";
  }
}
