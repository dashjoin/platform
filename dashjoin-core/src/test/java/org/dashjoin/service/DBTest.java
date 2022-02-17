package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.SecurityContext;
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
  Services services;

  @Inject
  Data db;

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
    Assertions.assertEquals(2, db.read(sc, "junit", toID("PRJ"), toID("1000")).size());
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
  Map<String, Object> getMap(Map<String, Object> map, String field) {
    return (Map<String, Object>) map.get(field);
  }

  @SuppressWarnings("unchecked")
  List<Map<String, Object>> getList(Map<String, Object> map, String field) {
    return (List<Map<String, Object>>) map.get(field);
  }

  @Test
  public void testSearch() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<SearchResult> res = db.search(sc, "mike", null);
    Assertions.assertEquals(1, res.size());
    Assertions.assertEquals("junit", res.get(0).id.database);
    name("EMP", res.get(0).id.table);
    number(1, res.get(0).id.pk.get(0));
    name("NAME", res.get(0).column);
    Assertions.assertEquals("mike", res.get(0).match);
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
    db.all(sc, "junit", toID("EMP"), 0, 1, null, false, null);
    List<Map<String, Object>> x = db.all(sc, "junit", toID("EMP"), 1, 10, null, false, null);
    map("{ID=2, NAME=joe, WORKSON=1000}", x.get(0));
  }

  void map(String string, Map<String, Object> map) {
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
  public void testCRUD() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Resource id = db.create(sc, "junit", toID("PRJ"), of(idRead(), toID(2000)));
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
    Resource id = db.create(sc, "junit", toID("EMP"), of(idRead(), toID(3)));
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

  void name(String string, String id) {
    Assertions.assertEquals(string, id.replaceAll("http://ex.org/", ""));
  }

  void number(int expected, Object actual) {
    Assertions.assertTrue((actual + "").equals(expected + "") || actual.equals(expected)
        || actual.equals("http://ex.org/" + expected) || actual.equals("EMP/" + expected));
  }

  @Test
  public void testList() throws Exception {
    Map<String, Map<String, Object>> res = db.list("junit", toID("EMP"), Arrays.asList(toID("1")));
    map("{WORKSON=1000, ID=1, NAME=mike}", res.get(toID("1")));
  }

  @Test
  public void testKeys() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    List<Choice> complete = db.keys(sc, "junit", toID("EMP"), "1", 10);
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
