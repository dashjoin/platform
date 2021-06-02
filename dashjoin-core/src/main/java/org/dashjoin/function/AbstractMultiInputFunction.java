package org.dashjoin.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * base class for all functions that accept single values, objects and lists thereof
 */
public abstract class AbstractMultiInputFunction extends AbstractFunction<Object, Object> {

  @SuppressWarnings("unchecked")
  @Override
  public Object run(Object arg) throws Exception {
    // handle 4 cases
    if (arg instanceof List<?>) {
      List<Object> list = new ArrayList<Object>((List<Object>) arg);
      for (int i = 0; i < list.size(); i++) {
        list.set(i, run(list.get(i)));
      }
      return list;
    } else {
      if (arg instanceof Map<?, ?>) {
        Map<String, Object> map = (Map<String, Object>) arg;
        Object input = map.get(inputField());
        Object result = single(input);
        if (outputField() != null && result != null)
          if (outputField().equals("."))
            if (result instanceof Map<?, ?>) {
              ((Map<String, Object>) result).put(inputField(), input);
              return result;
            } else {
              for (Map<String, Object> i : (List<Map<String, Object>>) result)
                i.put(inputField(), input);
              return result;
            }
          else
            map.put(outputField(), result);
        return map;
      } else {
        return single(arg);
      }
    }

  }

  public abstract Object single(Object arg) throws Exception;

  public abstract String inputField();

  public abstract String outputField();

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }
}
