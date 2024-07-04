package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Choice;
import org.dashjoin.service.Data.Origin;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Data.SearchResult;
import org.dashjoin.util.Escape;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.SecurityContext;

/**
 * tests the REST endpoint Data that serves "/database". This endpoint delegates requests to the
 * underlying implementation of AbstractDatabase defined as dj/junit.
 * 
 * Apps that deliver an implementation of an AbstractDatabase should define a test class that
 * extends from this class. There are some methods that can be overridden to return queries in the
 * respective language
 */
@QuarkusTest
public class DBTest {

  @Inject
  protected Services services;

  @Inject
  public Data db;

  @Test
  public void testMetadata() throws Exception {
    Table t = services.getConfig().getDatabase("dj/junit").tables.get("EMP");
    Assertions.assertEquals(idRead(), t.properties.get(idRead()).name);
    Assertions.assertEquals(0, t.properties.get(idRead()).pkpos.intValue());
    Assertions.assertNull(t.properties.get(idRead()).ref);
    Assertions.assertEquals("dj/junit/EMP/" + idRead(), t.properties.get(idRead()).ID);

    Assertions.assertEquals("WORKSON", t.properties.get("WORKSON").name);
    Assertions.assertNull(t.properties.get("WORKSON").pkpos);
    Assertions.assertEquals("dj/junit/PRJ/" + idRead(), t.properties.get("WORKSON").ref);
    Assertions.assertEquals("dj/junit/EMP/WORKSON", t.properties.get("WORKSON").ID);
  }

  @Test
  public void testGetTables() throws Exception {
    Assertions.assertTrue(db.tables().contains("dj/junit/EMP"));
    Assertions.assertTrue(db.tables().contains("dj/junit/PRJ"));
    Assertions.assertTrue(db.tables().contains("dj/junit/NOKEY"));
    Assertions.assertTrue(db.tables().contains("dj/junit/T"));
    Assertions.assertTrue(db.tables().contains("dj/junit/U"));
  }

  @Test
  public void testGet() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(toID(1000),
        db.read(sc, "junit", toID("PRJ"), toID("1000")).get(idRead()));
    Assertions.assertEquals("dev-project",
        db.read(sc, "junit", toID("PRJ"), toID("1000")).get(toID("NAME")));
  }

  @Test
  public void testGetNullWrongId() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertThrows(NotFoundException.class, () -> {
      db.read(sc, "junit", toID("PRJ"), toID("9999999"));
    });
  }

  @Test
  public void testGetNullWrongTable() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      db.read(sc, "junit", "DUMMY_TABLE_DOES_NOT_EXIST", "9999999");
    });
  }

  @Test
  public void testQuery() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> res = db.query(sc, "junit", "list", null);
    Assertions.assertEquals(2, res.size());
    Assertions.assertEquals(toID(1), res.get(0).get(idQuery()));
  }

  @Test
  public void testGraph() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> res = db.queryGraph(sc, "junit", "graph", null);
    Assertions.assertEquals(2, res.size());
    Assertions.assertEquals(toID(1), ((Map<?, ?>) res.get(0).get("x")).get(idRead()));
    Resource r = (Resource) ((Map<?, ?>) res.get(0).get("x")).get("_dj_resource");
    Assertions.assertEquals("junit", r.database);
    name("EMP", r.table);
    name("1", "" + r.pk.get(0));
  }

  @Test
  public void testPath() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> res = db.queryGraph(sc, "junit", "path", null);
    Assertions.assertEquals(2, res.size());
    Map<String, Object> first = getMap(res.get(0), "path");
    Assertions.assertTrue(getMap(first, "start").containsKey("_dj_resource"));
    List<Map<String, Object>> steps = getList(first, "steps");
    Assertions.assertEquals(1, steps.size());
    Assertions.assertEquals("{_dj_edge=WORKSON, _dj_outbound=true}", "" + steps.get(0).get("edge"));
    Assertions.assertTrue(getMap(steps.get(0), "end").containsKey("_dj_resource"));
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getMap(Map<String, Object> map, String field) {
    return (Map<String, Object>) map.get(field);
  }

  @SuppressWarnings("unchecked")
  protected List<Map<String, Object>> getList(Map<String, Object> map, String field) {
    return (List<Map<String, Object>>) map.get(field);
  }

  @Test
  public void testSearch() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<SearchResult> res = db.search(sc, "mike", null);
    check(res);
    res = db.search(sc, "junit", "mike", null);
    check(res);
    res = db.search(sc, "junit", "EMP", "mike", null);
    check(res);
  }

  void check(List<SearchResult> res) {
    int count = 0;
    for (SearchResult i : res)
      if (i.id.database.equals("junit"))
        count++;
    Assertions.assertEquals(1, count);
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("EMP", res.get(0).id.table);
    number(1, res.get(0).id.pk.get(0));
    name("NAME", res.get(0).column);
    Assertions.assertEquals("mike", res.get(0).match);
  }

  @Test
  public void testSearchAcl() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);

    // global
    Assertions.assertEquals(0, db.search(sc, "other", null).size());
    List<SearchResult> res = db.search(sc, "dev-project", null);
    Assertions.assertEquals(1, res.size());
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("PRJ", res.get(0).id.table);
    number(1000, res.get(0).id.pk.get(0));
    name("NAME", res.get(0).column);
    Assertions.assertEquals("dev-project", res.get(0).match);

    res = db.search(sc, "mike", null);
    Assertions.assertEquals(0, res.size());

    // db junit
    Assertions.assertEquals(0, db.search(sc, "junit", "other", null).size());
    res = db.search(sc, "junit", "dev-project", null);
    Assertions.assertEquals(1, res.size());
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("PRJ", res.get(0).id.table);
    number(1000, res.get(0).id.pk.get(0));
    name("NAME", res.get(0).column);
    Assertions.assertEquals("dev-project", res.get(0).match);

    res = db.search(sc, "junit", "mike", null);
    Assertions.assertEquals(0, res.size());

    // db junit + table
    Assertions.assertEquals(0, db.search(sc, "junit", toID("PRJ"), "other", null).size());
    res = db.search(sc, "junit", toID("PRJ"), "dev-project", null);
    Assertions.assertEquals(1, res.size());
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("PRJ", res.get(0).id.table);
    number(1000, res.get(0).id.pk.get(0));
    name("NAME", res.get(0).column);
    Assertions.assertEquals("dev-project", res.get(0).match);

    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      // emp table is restricted
      db.search(sc, "junit", "EMP", "mike", null);
    });
  }

  @Test
  public void testSearchQuery() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<SearchResult> res =
        db.searchQuery(sc, services.getConfig().getDatabase("dj/junit"), "search", "mike");
    Assertions.assertEquals(1, res.size());
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("EMP", res.get(0).id.table);
    number(1, res.get(0).id.pk.get(0));
    Assertions.assertTrue("EMP.NAME".equals(res.get(0).column) || "NAME".equals(res.get(0).column));
    Assertions.assertEquals("mike", res.get(0).match);
  }

  @Test
  public void testSearchQuery2() throws Exception {
    AbstractDatabase database = services.getConfig().getDatabase("dj/junit");
    if (database instanceof SQLDatabase) {
      SecurityContext sc = Mockito.mock(SecurityContext.class);
      Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
      List<SearchResult> res = db.searchQuery(sc, database, "search2", "mike");
      Assertions.assertEquals(1, res.size());
      Assertions.assertEquals("junit", res.get(0).id.database);
      name("EMP", res.get(0).id.table);
      number(1, res.get(0).id.pk.get(0));
      Assertions
          .assertTrue("EMP.NAME".equals(res.get(0).column) || "NAME".equals(res.get(0).column));
      Assertions.assertEquals("mike", res.get(0).match);
    }
  }

  @Test
  public void testQueryMeta() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Map<String, Property> x = db.queryMeta(sc, "junit", "list", null);
    Assertions.assertEquals((Integer) 0, x.get(idQuery()).pkpos);
  }

  @Test
  public void testAll() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> x = db.all(sc, "junit", toID("EMP"), 0, 10, null, false, null);
    map("{WORKSON=1000, ID=1, NAME=mike}", x.get(0));
    Assertions.assertEquals(2, x.size());
    x = db.getall(sc, "junit", toID("EMP"), 0, 10, null, false, null);
    map("{WORKSON=1000, ID=1, NAME=mike}", x.get(0));
    Assertions.assertEquals(2, x.size());
    x = db.all(sc, "junit", toID("EMP"), 0, 10, toID("NAME"), true, MapUtil.of(idRead(), toID(1)));
    Assertions.assertEquals(1, x.size());
  }

  @Test
  public void testAllAcl() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      db.all(sc, "junit", toID("EMP"), 0, 10, null, false, null);
    });
  }

  @Test
  public void testAllTenant() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);
    // we have access but no record matches the row level ACL
    Assertions.assertEquals(1, db.all(sc, "junit", toID("PRJ"), 0, 10, null, false, null).size());
    Assertions.assertEquals("dev-project",
        db.all(sc, "junit", toID("PRJ"), 0, 10, null, false, null).get(0).get(toID("NAME")));
  }

  @Test
  public void testCrudTenant() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);

    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      db.read(sc, "junit", toID("PRJ"), toID("1001"));
    });
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      db.update(sc, "junit", toID("PRJ"), toID("1001"), MapUtil.of("NAME", "change"));
    });
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      db.delete(sc, "junit", toID("PRJ"), toID("1001"));
    });
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      db.create(sc, "junit", toID("PRJ"), MapUtil.of("ID", 77, "NAME", "change"));
    });

    db.read(sc, "junit", toID("PRJ"), toID("1000"));
    db.update(sc, "junit", toID("PRJ"), toID("1000"), MapUtil.of(toID("BUDGET"), 100));
    db.update(sc, "junit", toID("PRJ"), toID("1000"), MapUtil.of(toID("BUDGET"), null));
    db.create(sc, "junit", toID("PRJ"),
        MapUtil.of(idRead(), toID(77), toID("NAME"), "dev-project"));
    db.delete(sc, "junit", toID("PRJ"), toID("77"));
  }

  @Test
  public void testAllWhere() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> x =
        db.all(sc, "junit", toID("EMP"), 0, 10, null, false, MapUtil.of(toID("NAME"), "mike"));
    map("{WORKSON=1000, ID=1, NAME=mike}", x.get(0));
    Assertions.assertEquals(1, x.size());
  }

  @Test
  public void testAllOffset() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Map<String, Object>> first = db.all(sc, "junit", toID("EMP"), 0, 1, null, false, null);
    map("{ID=1, NAME=mike, WORKSON=1000}", first.get(0));
    List<Map<String, Object>> x = db.all(sc, "junit", toID("EMP"), 1, 10, null, false, null);
    map("{ID=2, NAME=joe, WORKSON=1000}", x.get(0));
  }

  protected void map(String string, Map<String, Object> map) {
    string = string.substring(1, string.length() - 1);
    string = string.replaceAll("ID=", idRead() + "=");
    String[] parts = string.split(",");
    Assertions.assertEquals(parts.length, map.size());
    for (String part : parts) {
      String[] kv = part.trim().split("=");
      if (kv[0].equals("NAME"))
        Assertions.assertEquals(kv[1], map.get(toID(kv[0])));
      else
        Assertions.assertEquals(toID(kv[1]), "" + map.get(toID(kv[0])));
    }
  }

  @Test
  public void testIncoming() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Origin> links = db.incoming(sc, "junit", toID("PRJ"), toID("1000"), 0, 10);
    id("dj/junit/EMP/WORKSON", links.get(0).fk);
    Assertions.assertEquals("junit", links.get(0).id.database);
    name("EMP", links.get(0).id.table);
    number(1, links.get(0).id.pk.get(0));
    id("dj/junit/PRJ/" + idRead(), links.get(0).pk);
  }

  @Test
  public void testIncomingAcl() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);
    Mockito.when(sc.getUserPrincipal()).thenReturn(new Principal() {
      @Override
      public String getName() {
        return "admin";
      }
    });
    List<Origin> links = db.incoming(sc, "junit", toID("PRJ"), toID("1000"), 0, 10);
    Assertions.assertEquals(0, links.size());
  }

  @Test
  public void testCRUD() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Resource id = db.createInternal(sc, "junit", toID("PRJ"), of(idRead(), toID(2000)));
    id("dj/junit/PRJ/2000", "dj/" + id.database + '/' + Escape.encodeTableOrColumnName(id.table)
        + '/' + Escape.encodeTableOrColumnName("" + id.pk.get(0)));
    Assertions.assertNotNull(db.read(sc, "junit", toID("PRJ"), toID("2000")));
    db.update(sc, "junit", toID("PRJ"), toID("2000"), of(toID("NAME"), "new name"));
    Assertions.assertEquals("new name",
        db.read(sc, "junit", toID("PRJ"), toID("2000")).get(toID("NAME")));
    db.delete(sc, "junit", toID("PRJ"), toID("2000"));
    try {
      db.read(sc, "junit", toID("PRJ"), toID("2000"));
      Assertions.fail();
    } catch (NotFoundException e) {
    }
  }

  @Test
  public void testCRUD2() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Resource id = db.createInternal(sc, "junit", toID("EMP"), of(idRead(), toID(3)));
    id("dj/junit/EMP/3", "dj/" + id.database + '/' + Escape.encodeTableOrColumnName(id.table) + '/'
        + Escape.encodeTableOrColumnName("" + id.pk.get(0)));
    Assertions.assertNotNull(db.read(sc, "junit", toID("EMP"), toID("3")));
    db.update(sc, "junit", toID("EMP"), toID("3"), of(toID("NAME"), "new name"));
    db.update(sc, "junit", toID("EMP"), toID("3"), of(toID("WORKSON"), thousand()));
    Assertions.assertEquals("new name",
        db.read(sc, "junit", toID("EMP"), toID("3")).get(toID("NAME")));
    Assertions.assertEquals(thousand(),
        db.read(sc, "junit", toID("EMP"), toID("3")).get(toID("WORKSON")));
    db.delete(sc, "junit", toID("EMP"), toID("3"));
    try {
      db.read(sc, "junit", toID("EMP"), toID("3"));
      Assertions.fail();
    } catch (NotFoundException e) {
    }
  }

  @Test
  public void cannotUpdatePk() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
      SecurityContext sc = Mockito.mock(SecurityContext.class);
      Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
      db.update(sc, "junit", toID("EMP"), toID("1"), of(idRead(), 3));
    });
  }

  void id(String string, String id) {
    Assertions.assertEquals(string,
        id.replaceAll("http:%2F%2Fex.org%2F", "").replaceAll("PRJ%2F", ""));
  }

  protected void name(String string, String id) {
    Assertions.assertEquals(string, id.replaceAll("http://ex.org/", ""));
  }

  void number(int expected, Object actual) {
    Assertions.assertTrue((actual + "").equals(expected + "") || actual.equals(expected)
        || actual.equals("http://ex.org/" + expected) || actual.equals("EMP/" + expected)
        || actual.equals("PRJ/" + expected));
  }

  @Test
  public void testList() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Map<String, Map<String, Object>> res =
        db.list(sc, "junit", toID("EMP"), Arrays.asList(toID("1")), null);
    map("{WORKSON=1000, ID=1, NAME=mike}", res.get(toID("1")));
  }

  @Test
  public void testListMissing() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Map<String, Map<String, Object>> res =
        db.list(sc, "junit", toID("EMP"), Arrays.asList(toID("1"), toID("999")), true);
    map("{ID=999}", res.get(toID("999")));
  }

  @Test
  public void testKeys() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Choice> complete = db.keys(sc, "junit", toID("EMP"), "1", 10);
    Assertions.assertEquals(1, complete.size());
  }

  @Test
  public void testKeysAcl() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole("admin")).thenReturn(false);
    Mockito.when(sc.isUserInRole("authenticated")).thenReturn(true);
    List<Choice> complete = db.keys(sc, "junit", toID("PRJ"), "1", 10);
    Assertions.assertEquals(1, complete.size());
  }

  protected Object thousand() {
    return 1000;
  }

  protected String idQuery() {
    return "EMP.ID";
  }

  protected String idRead() {
    return "ID";
  }

  protected Object toID(Object o) {
    return o;
  }

  String toID(String o) {
    return (String) toID((Object) o);
  }
}
