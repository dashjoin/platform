package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import org.dashjoin.service.Data.SearchResult;

public class Search extends AbstractVarArgFunction<List<SearchResult>> {

  @SuppressWarnings("rawtypes")
  @Override
  public List<SearchResult> run(List arg) throws Exception {
    try {
      String term = (String) arg.get(0);
      Integer limit = (Integer) arg.get(1);
      String database = (String) arg.get(2);
      String table = (String) arg.get(3);
      if (term == null)
        throw new Exception("$search(term, limit?, database?, table?)");
      if (database != null && table != null)
        return this.expressionService.getData().search(this.sc, database, table, term, limit);
      if (database != null)
        return this.expressionService.getData().search(this.sc, database, term, limit);
      return this.expressionService.getData().search(this.sc, term, limit);
    } catch (ClassCastException e) {
      throw new Exception("$search(term, limit?, database?, table?)");
    }
  }

  @Override
  public String getID() {
    return "search";
  }

  @Override
  public String getType() {
    return "read";
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, Integer.class, String.class, String.class);
  }
}
