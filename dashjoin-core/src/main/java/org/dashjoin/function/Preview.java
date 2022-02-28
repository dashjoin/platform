package org.dashjoin.function;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Preview extends AbstractFunction<List, List> {

  @Override
  public List run(List arg) throws Exception {
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

  @Override
  public Class<List> getArgumentClass() {
    return List.class;
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
