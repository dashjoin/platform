package org.dashjoin.function;

import java.util.ArrayList;
import java.util.List;

public class Preview extends AbstractFunction<List<Object>, List<Object>> {

  @Override
  public List<Object> run(List<Object> arg) throws Exception {
    if (!this.readOnly)
      return arg;
    List<Object> res = new ArrayList<>();
    for (Object i : arg) {
      res.add(i);
      if (res.size() == 10)
        break;
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<List<Object>> getArgumentClass() {
    return (Class<List<Object>>) new ArrayList<Object>().getClass();
  }

  @Override
  public String getID() {
    return "preview";
  }

  @Override
  public String getType() {
    return "read";
  }
}
