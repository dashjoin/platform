package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * tests some aspects of the union config DB
 */
public class UnionDatabaseTest {

  @Test
  public void testAll() throws Exception {
    UnionDatabase db = new UnionDatabase();
    JSONDatabase cp = JSONDatabaseFactory.getReadOnlyInstance();
    db._dbs = Arrays.asList();
    db._user = cp;
    List<Map<String, Object>> res =
        db.all(Table.ofName("dj-database"), 0, 99, "ID", false, MapUtil.of("parent", "dj"));
    // asc - smallest first
    Assert.assertEquals("dj/config", res.get(0).get("ID"));

    res = db.all(Table.ofName("dj-database"), 0, 99, "ID", true, MapUtil.of("parent", "dj"));
    // desc - largest first (junit or rdf4j)
    Assert.assertTrue(
        "dj/junit".equals(res.get(0).get("ID")) || "dj/rdf4j".equals(res.get(0).get("ID")));
  }

  @Test
  public void mergeArray() {
    Map<String, Object> merge = UnionDatabase.mergeArray(of(), of("a", asList(1)));
    Assert.assertEquals("{a=[1]}", merge.toString());
  }

  // @Test
  // config DB delete simply resets the bootstraped object
  public void deleteFromClasspath() throws Exception {
    UnionDatabase u = new UnionDatabase();
    JSONDatabase cp = JSONDatabaseFactory.getReadOnlyInstance();
    u._dbs = Arrays.asList(cp);
    u._user = JSONDatabaseFactory.getPersistantInstance();

    Table qc = new Table();
    qc.name = "dj-query-catalog";

    // this comes off the read only part
    Assert.assertNotNull(u.read(qc, of("ID", "list")));

    // must be able to delete it anyway
    Assert.assertTrue(u.delete(qc, of("ID", "list")));

    // now it must be gone
    Assert.assertNull(u.read(qc, of("ID", "list")));
    QueryMeta qm = new QueryMeta();
    qm.query = qc.name;
    for (Map<String, Object> q : u.query(qm, of("ID", "list")))
      if (q.get("ID").equals("list"))
        Assert.fail("list should have been deleted");

    // re-create it
    // note that recreating it immediately brings back the read only properties (e.g. the type
    // property)
    // the query property is null because the create statement explicitly sets it to null
    // overriding the read only part
    Map<String, Object> test = new HashMap<>();
    test.put("ID", "list");
    test.put("query", null);
    u.create(qc, test);
    Assert.assertEquals("{ID=list, type=read, roles=[user]}", "" + u.read(qc, of("ID", "list")));

    // clean up
    new File("model/dj-query-catalog/list.json").delete();
  }

  @Test
  public void noDeletedFileForUserData() throws Exception {
    UnionDatabase u = new UnionDatabase();
    JSONDatabase cp = JSONDatabaseFactory.getReadOnlyInstance();
    u._dbs = Arrays.asList(cp);
    u._user = JSONDatabaseFactory.getPersistantInstance();

    Table qc = new Table();
    qc.name = "dj-query-catalog";

    u.create(qc, of("ID", "myquery", "query", "bla"));
    u.delete(qc, of("ID", "myquery"));
    u.delete(qc, of("ID", "myquery"));
    Assert.assertEquals(0, new File("model/dj-query-catalog").listFiles().length);
  }
}
