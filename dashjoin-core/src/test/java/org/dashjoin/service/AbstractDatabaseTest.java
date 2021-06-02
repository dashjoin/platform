package org.dashjoin.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableMap;

public abstract class AbstractDatabaseTest {

  @Test
  public void read() throws Exception {
    Database db = db();
    Table s = new Table();
    s.name = "EMP";
    Map<String, Object> res = db.read(s, Collections.singletonMap("ID", 1));
    Assert.assertEquals("mike", res.get("NAME"));
    Assert.assertEquals(1000, res.get("WORKSON"));
  }

  @Test
  public void create() throws Exception {
    Database db = db();
    Table s = new Table();
    s.name = "EMP";
    s.properties = ImmutableMap.of("ID", new Property(), "NAME", new Property());
    s.properties.get("ID").pkpos = 0;
    db().create(s, Collections.singletonMap("ID", 3));
    Map<String, Object> res = db.read(s, Collections.singletonMap("ID", 3));
    Assert.assertEquals(3, res.get("ID"));

    Assert.assertTrue(
        db().update(s, Collections.singletonMap("ID", 3), ImmutableMap.of("NAME", "newname")));
    Map<String, Object> res2 = db.read(s, Collections.singletonMap("ID", 3));
    Assert.assertEquals("newname", res2.get("NAME"));

    Assert.assertTrue(db.delete(s, Collections.singletonMap("ID", 3)));
    Assert.assertNotNull(db.read(s, Collections.singletonMap("ID", 1)));
    Assert.assertNull(db.read(s, Collections.singletonMap("ID", 3)));
  }

  @Test
  public void query() throws Exception {
    Database db = db();
    QueryMeta info = new QueryMeta();
    info.query = getQuery();
    QueryMeta info2 = new QueryMeta();
    info2.query = getQuery2();
    for (List<Map<String, Object>> res : Arrays.asList(db.query(info, null),
        db().query(info2, null))) {
      int idx = res.size() - 1;
      if (res.get(idx).get("EMP.WORKSON") != null)
        Assert.assertEquals(1000, res.get(idx).get("EMP.WORKSON"));
      else
        Assert.assertEquals(1000, res.get(idx).get("WORKSON"));
    }
  }

  abstract String getQuery();

  abstract String getQuery2();

  abstract Database db() throws Exception;
}
