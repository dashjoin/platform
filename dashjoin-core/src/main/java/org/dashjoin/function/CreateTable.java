package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;

/**
 * expose manage.createTable as a function
 */
public class CreateTable extends AbstractVarArgFunction<Void> {

  @Override
  public String getID() {
    return "createTable";
  }

  @Override
  public Void run(@SuppressWarnings("rawtypes") List list) throws Exception {
    expressionService.getManage().createTable(sc, (String) list.get(0), (String) list.get(1));
    return null;
  }

  @Override
  public String getType() {
    return "write";
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, String.class);
  }
}
