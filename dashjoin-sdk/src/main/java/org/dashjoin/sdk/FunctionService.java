package org.dashjoin.sdk;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.Function;
import org.eclipse.microprofile.config.ConfigProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * function REST skeleton
 */
@Path("/function")
// @RolesAllowed("admin")
public class FunctionService {

  private static final ObjectMapper om = new ObjectMapper();

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

      Map<String, Object> functions = new LinkedHashMap<>();

      for (Function<Object, Object> i : ServiceLoader.load(Function.class))
        fn.put(i.getID(), i);

      for (String key : ConfigProvider.getConfig().getPropertyNames())
        if (key.startsWith("dashjoin.function.")) {
          String[] parts = key.split("\\.");
          List<String> path = new ArrayList<>(Arrays.asList(parts));
          path.remove(0);
          path.remove(0);
          add(functions, path, ConfigProvider.getConfig().getValue(key, String.class));
        }

      for (Entry<String, Object> e : functions.entrySet()) {
        Map<String, Object> map = (Map<String, Object>) e.getValue();
        Function<Object, Object> f = (Function<Object, Object>) Class
            .forName((String) map.get("djClassName")).getDeclaredConstructor().newInstance();
        fn.put(e.getKey(), f);

        if (f instanceof AbstractConfigurableFunction)
          for (Entry<String, Object> kv : map.entrySet()) {
            Field field = f.getClass().getField(kv.getKey());
            if (field.getType().equals(Integer.class))
              field.set(f, Integer.parseInt((String) kv.getValue()));
            else if (field.getType().equals(Long.class))
              field.set(f, Long.parseLong((String) kv.getValue()));
            else if (field.getType().equals(Boolean.class))
              field.set(f, Boolean.parseBoolean((String) kv.getValue()));
            else
              field.set(f, kv.getValue());
          }
      }
    }
    return fn;
  }

  @SuppressWarnings("unchecked")
  void add(Map<String, Object> map, List<String> path, Object value) {
    String key = path.remove(0);

    if (path.size() == 0)
      map.put(key, value);
    else {
      if (!map.containsKey(key))
        map.put(key, new LinkedHashMap<>());
      map = (Map<String, Object>) map.get(key);
      add(map, path, value);
    }
  }

  /**
   * run function
   */
  @POST
  @Path("/run/{function}")
  public Object run(@PathParam("function") String function, Object arg) throws Exception {
    if (fn().get(function) == null)
      throw new Exception("Function does not exist: " + function);

    arg = om.convertValue(arg, fn().get(function).getArgumentClass());
    return fn().get(function).run(arg);
  }
}
