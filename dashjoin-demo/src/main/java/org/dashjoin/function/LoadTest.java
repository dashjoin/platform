package org.dashjoin.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;

public class LoadTest extends AbstractFunction<LoadTest.Config, Map<String, Object>> {

  public static class Config {
    public String database;
    public String table;
    public Integer count;
  }

  @Override
  public Map<String, Object> run(Config arg) throws Exception {

    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + arg.database);
    Table m = db.tables.get(arg.table);

    long start;
    Map<String, Object> res = new HashMap<>();
    List<Map<String, Object>> ids = new ArrayList<>();
    for (int i = 0; i < arg.count; i++)
      ids.add(MapUtil.of("ID", i));

    start = System.currentTimeMillis();
    for (int i = 0; i < arg.count; i++)
      db.create(m, MapUtil.of("ID", i));
    res.put("create", System.currentTimeMillis() - start);

    start = System.currentTimeMillis();
    for (int i = 0; i < arg.count; i++)
      db.delete(m, MapUtil.of("ID", i));
    res.put("delete", System.currentTimeMillis() - start);

    // start = System.currentTimeMillis();
    // db.create(m, ids);
    // res.put("create bulk", System.currentTimeMillis() - start);
    //
    // start = System.currentTimeMillis();
    // db.delete(m, ids);
    // res.put("delete bulk", System.currentTimeMillis() - start);

    System.out.println(res);
    return res;
  }

  @Override
  public Class<Config> getArgumentClass() {
    return Config.class;
  }

  @Override
  public String getID() {
    return "loadTest";
  }

  @Override
  public String getType() {
    return "write";
  }
}
