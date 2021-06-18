package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.service.Data.Choice;
import org.dashjoin.service.QueryEditor.InitialQueryRequest;
import org.dashjoin.service.QueryEditor.QueryResponse;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

/**
 * tests specific features of the pojo config database
 */
@QuarkusTest
public class PojoDatabaseTest {

  @Inject
  Services services;

  @Test
  public void configDB() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    config.getDatabase("dj/config");

    Table schema = new Table();

    // check the ID Property of the database table
    schema.name = "Property";
    Assert.assertEquals(
        "{ID=dj/config/dj-database/ID, name=ID, parent=dj/config/dj-database, pkpos=0, type=string}",
        "" + config.read(schema, of("ID", "dj/config/dj-database/ID")));

    // add a provider that specifies a widget for ID
    config.dbs().add(new JSONDatabase() {
      @Override
      public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
        return of("tables", of("dj-database",
            of("properties", of("ID", of("ID", "dj/config/dj-database/ID", "widget", "test")))));
      }

      @Override
      public Map<String, Map<String, Object>> queryMap(QueryMeta info,
          Map<String, Object> arguments) throws Exception {
        // in order to contribute to Property, we need to add
        // database/tables/properties/
        return of("dj/config", read(null, null));
      }
    });

    Assert.assertEquals("test",
        (config.read(schema, of("ID", "dj/config/dj-database/ID")).get("widget")));

    AbstractDatabase db = config.getDatabase("dj/config");
    Assert.assertEquals("test", db.tables.get("dj-database").properties.get("ID").widget);
  }

  @Test
  public void update() throws Exception {
    PojoDatabase config = services.pojoDatabase();

    Table property = new Table();
    property.name = "Property";
    config.update(property, of("ID", "dj/config/dj-database/ID"), of("ref", "test"));
    config.update(property, of("ID", "dj/config/dj-database/name"), of("ref", "test2"));

    Table table = new Table();
    table.name = "Table";
    config.update(table, of("ID", "dj/config/dj-database"), of("dj-label", "prop"));

    Assert.assertEquals("prop",
        (config.read(table, of("ID", "dj/config/dj-database")).get("dj-label")));
    Assert.assertEquals("test",
        (config.read(property, of("ID", "dj/config/dj-database/ID")).get("ref")));
    Assert.assertEquals("test2",
        (config.read(property, of("ID", "dj/config/dj-database/name")).get("ref")));

    // config.delete(property, of("ID", "dj/config/dj-database/ID"));
    config.update(property, of("ID", "dj/config/dj-database/ID"), MapUtil.of("ref", null));
    config.update(property, of("ID", "dj/config/dj-database/name"), MapUtil.of("ref", null));
    Assert.assertFalse(
        config.read(property, of("ID", "dj/config/dj-database/ID")).toString().contains("ref"));

    // config.delete(table, of("ID", "dj/config/dj-database"));
    config.update(table, of("ID", "dj/config/dj-database"), MapUtil.of("dj-label", null));
    Assert.assertFalse(
        config.read(table, of("ID", "dj/config/dj-database")).toString().contains("dj-label=prop"));
  }

  @Test
  public void testStoredProc() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    QueryMeta qi = new QueryMeta();

    qi.query = "dj-page-urls";
    List<Map<String, Object>> res = config.query(qi, null);
    qi.query = "dj-config-values";
    res = config.query(qi, null);
    qi.query = "dj-roles-without-admin";
    res = config.query(qi, null);
    qi.query = "dj-tables";
    res = config.query(qi, null);
    qi.query = "dj-layouts";
    res = config.query(qi, null);
    qi.query = "dj-navigation";
    res = config.query(qi, null);
    qi.query = "dj-databases-no-config";
    res = config.query(qi, null);
    Assert.assertEquals("dj/junit", res.get(0).get("ID"));
    Map<String, Property> meta = config.queryMeta(qi, null);
    Assert.assertEquals((Integer) 0, meta.get("ID").pkpos);

    qi.query = "Table";
    res = config.query(qi, null);
    Assert.assertEquals("dj/junit/T", res.get(0).get("ID"));
    meta = config.queryMeta(qi, null);
    Assert.assertEquals((Integer) 0, meta.get("ID").pkpos);

    qi.query = "Property";
    res = config.query(qi, null);
    Assert.assertEquals("dj/junit/T/ID", res.get(0).get("ID"));
  }

  @Test
  public void testQueryEditor() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    QueryEditorInternal qe = config.getQueryEditor();
    InitialQueryRequest r = new InitialQueryRequest();
    r.table = "dj/config/dj-database";
    QueryResponse q = qe.getInitialQuery(r);
    Assert.assertEquals("dj-database", q.query);
  }

  @Test
  public void testStoredProcMeta() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    QueryMeta meta = config.getQueryMeta("dj-page-urls");
    Assert.assertEquals("authenticated", meta.roles.get(0));
    Assert.assertEquals("dj-page-urls", meta.query);
  }

  @Test
  public void testFunctions() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    List<Map<String, Object>> f = config.queryFunctions(null, null);
    Assert.assertEquals("echo", f.get(0).get("ID"));
  }

  @Test
  public void testQueries() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    List<Map<String, Object>> f = config.queryQueries(null, null);
    Assert.assertTrue("list".equals(f.get(0).get("ID")) || "search".equals(f.get(0).get("ID")));
  }

  @Inject
  Data data;

  void create(String table, Map<String, Object> object) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    // Data data = new Data();
    // data.services = services;
    // data.function = new FunctionService();
    // data.function.services = services;
    try {
      data.create(sc, "config", table, object);
      Assert.fail();
    } catch (RuntimeException e) {
      Assert.assertEquals("Schema changes are not supported on this database: jdbc:h2:mem:test",
          e.getMessage());
    }
  }

  void update(String table, String search, Map<String, Object> object) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    // Data data = new Data();
    // data.services = services;
    // data.function = new FunctionService();
    // data.function.services = services;
    try {
      data.update(sc, "config", table, search, object);
      Assert.fail();
    } catch (RuntimeException e) {
      Assert.assertEquals("Schema changes are not supported on this database: jdbc:h2:mem:test",
          e.getMessage());
    }
  }

  void delete(String table, String search) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    // Data data = new Data();
    // data.services = services;
    // data.function = new FunctionService();
    // data.function.services = services;
    try {
      data.delete(sc, "config", table, search);
      Assert.fail();
    } catch (RuntimeException e) {
      Assert.assertEquals("Schema changes are not supported on this database: jdbc:h2:mem:test",
          e.getMessage());
    }
  }

  @Test
  public void testCreateTable() throws Exception {
    create("Table", newHashMap(of("parent", "dj/junit", "name", "X")));
  }

  @Test
  public void testRenameTable() throws Exception {
    update("Table", "dj/junit/T", newHashMap(of("name", "T2")));
  }

  @Test
  public void testDropTable() throws Exception {
    delete("Table", "dj/junit/T");
  }

  @Test
  public void testCreateCol() throws Exception {
    create("Property", newHashMap(of("parent", "dj/junit/T", "name", "C", "type", "string")));
  }

  @Test
  public void testRenameCol() throws Exception {
    update("Property", "dj/junit/T/ID", newHashMap(of("name", "ID2", "type", "string")));
  }

  @Test
  public void testDropCol() throws Exception {
    delete("Property", "dj/junit/T/ID");
  }

  @Test
  public void testKeys() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    Table table = new Table();
    table.name = "Table";
    List<Choice> keys = config.keys(table, "T", 1);
    Assert.assertEquals(1, keys.size());
    Assert.assertEquals("dj/junit/T", keys.get(0).value);
  }

  @Test
  public void testCRUD() throws Exception {
    PojoDatabase config = services.pojoDatabase();
    Table table = new Table();
    table.name = "Table";
    config.update(table, newHashMap(of("ID", "dj/junit/T")),
        of("instanceLayout", of("widget", "test")));
    config.update(table, newHashMap(of("ID", "dj/junit/T")), MapUtil.of("instanceLayout", null));
  }
}
