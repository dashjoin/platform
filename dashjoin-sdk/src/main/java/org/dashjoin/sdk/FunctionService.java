package org.dashjoin.sdk;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.dashjoin.function.Function;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * function REST skeleton
 */
@Path("/function")
// @RolesAllowed("admin")
public class FunctionService {

  /**
   * map of function name to implementation
   */
  Map<String, Function<Object, Object>> fn;

  /**
   * load config and init fn map
   */
  @SuppressWarnings("unchecked")
  synchronized Map<String, Function<Object, Object>> fn() throws Exception {
    if (fn == null) {
      fn = new HashMap<>();

      // switch to djClassName and support config parameters too

      for (String key : ConfigProvider.getConfig().getPropertyNames())
        if (key.startsWith("dashjoin.function."))
          fn.put(key.substring("dashjoin.function.".length()),
              (Function<Object, Object>) Class
                  .forName(ConfigProvider.getConfig().getValue(key, String.class))
                  .getDeclaredConstructor().newInstance());
    }
    return fn;
  }

  /**
   * run function
   */
  @POST
  @Path("/run/{function}")
  public Object run(@PathParam("function") String function, Object arg) throws Exception {
    if (fn().get(function) == null)
      throw new Exception("Function does not exist: " + function);
    return fn().get(function).run(arg);
  }
}
