package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * tests the provider database that can include dynamic config information into the config DB
 */
public class ProviderDatabaseTest {

  @Test
  public void testQueryMap() throws Exception {
    ProviderDatabase db =
        new ProviderDatabase(null, of("table", of("x", of("ID", "x", "name", "n"))));

    QueryMeta info = new QueryMeta();
    info.query = "table";
    Assert.assertEquals("{x={ID=x, name=n}}", "" + db.queryMap(info, of("ID", "x")));
    info.query = "table/x";
    Assert.assertEquals("{x={ID=x, name=n}}", "" + db.queryMap(info, of("ID", "x")));
    info.query = "table/notthere";
    Assert.assertEquals("{}", "" + db.queryMap(info, of("ID", "x")));
  }

  @Test
  public void testImmutable() throws Exception {
    ProviderDatabase db =
        new ProviderDatabase(null, of("table", of("x", of("ID", "x", "name", "n"))));
    Table s = new Table();
    s.name = "table";
    Map<String, Object> object = db.read(s, of("ID", "x"));
    object.clear();
    object = db.read(s, of("ID", "x"));
    Assert.assertEquals("n", object.get("name"));
  }
}
