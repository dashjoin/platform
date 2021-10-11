package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import java.io.File;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dashjoin.mapping.AbstractSource;
import org.dashjoin.mapping.Mapping;
import org.dashjoin.mapping.Provider;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Table;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import com.google.common.collect.ImmutableMap;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DDLTest {

  @Inject
  Services services;

  @Before
  public void before() throws Exception {
    // services.persistantDB = new JSONFileDatabase();
    // services.readonlyDB = new JSONClassloaderDatabase();

    // initially, DB must be empty (fail if there is a remnant file in
    // dashjoin-core/model/dj-database)
    Assert.assertEquals(0, services.getConfig().getDatabase("dj/ddl").tables.size());
  }

  @Test
  public void cannotUpdate() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
      update("Table", "dj/ddl/T", newHashMap(of("name", "T2")));
    });
  }

  @Test
  public void testSlashMetadata() throws Exception {
    TestDatabase.init();
    new File("model/dj-database/dj%2Fddl.json").delete();

    AbstractDatabase db;

    // create table
    create("Table", newHashMap(of("parent", "dj/ddl", "name", "T/T")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("[T/T]", "" + db.tables.keySet());
    Assert.assertEquals("dj/ddl/T%2FT", "" + db.tables.get("T/T").ID);
    Assert.assertEquals("[name, ID]", "" + db.tables.get("T/T").properties.keySet());

    // set dj-label
    update("Table", "dj/ddl/T%2FT", newHashMap(of("dj-label", "display ${name}")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals(1, db.tables.size());
    Assert.assertEquals("display ${name}", "" + db.tables.get("T/T").djLabel);

    // create col
    create("Property", newHashMap(of("parent", "dj/ddl/T%2FT", "name", "p/k", "type", "string")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("string", db.tables.get("T/T").properties.get("p/k").type);

    // create col
    create("Property", newHashMap(of("parent", "dj/ddl/T%2FT", "name", "f/k", "type", "string")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("string", db.tables.get("T/T").properties.get("f/k").type);

    // set ref (i.e. no schema update)
    update("Property", "dj/ddl/T%2FT/f%2Fk", newHashMap(of("ref", "dj/ddl/T%2FT/p%2Fk")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("dj/ddl/T%2FT/p%2Fk", "" + db.tables.get("T/T").properties.get("f/k").ref);

    new File("model/dj-database/dj%2Fddl.json").delete();
  }

  @Test
  public void testSlash() throws Exception {
    TestDatabase.init();
    new File("model/dj-database/dj%2Fddl.json").delete();

    AbstractDatabase db;

    // create table
    create("Table", newHashMap(of("parent", "dj/ddl", "name", "T/T")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("[T/T]", "" + db.tables.keySet());
    Assert.assertEquals("dj/ddl/T%2FT", "" + db.tables.get("T/T").ID);
    Assert.assertEquals("[name, ID]", "" + db.tables.get("T/T").properties.keySet());

    // create col
    create("Property", newHashMap(of("parent", "dj/ddl/T%2FT", "name", "ag/e", "type", "date")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("date", db.tables.get("T/T").properties.get("ag/e").widget);

    // rename col
    update("Property", "dj/ddl/T%2FT/ag%2Fe", newHashMap(of("name", "ag/e2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("date", db.tables.get("T/T").properties.get("ag/e2").widget);

    // rename
    update("Table", "dj/ddl/T%2FT", newHashMap(of("name", "T/T2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("[T/T2]", "" + db.tables.keySet());

    // drop column
    delete("Property", "dj/ddl/T%2FT2/ag%2Fe2");
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertNull(db.tables.get("T/T2").properties.get("ag/e2"));

    // drop table and metadata
    delete("Table", "dj/ddl/T%2FT2");
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals(0, db.tables.size());

    new File("model/dj-database/dj%2Fddl.json").delete();
  }

  @Test
  public void test() throws Exception {

    TestDatabase.init();
    new File("model/dj-database/dj%2Fddl.json").delete();

    AbstractDatabase db;

    // simulate a user edit on URL
    update("dj-database", "dj/ddl", newHashMap(of("url", "...")));

    // create table
    create("Table", newHashMap(of("parent", "dj/ddl", "name", "T")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("[T]", "" + db.tables.keySet());
    Assert.assertEquals("[name, ID]", "" + db.tables.get("T").properties.keySet());

    // recreate fails
    try {
      create("Table", newHashMap(of("parent", "dj/ddl", "name", "T")));
      Assert.fail();
    } catch (Exception ok) {
    }

    // rename
    update("Table", "dj/ddl/T", newHashMap(of("name", "T2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("[T2]", "" + db.tables.keySet());

    // set dj-label (i.e. no schema update)
    update("Table", "dj/ddl/T2", newHashMap(of("dj-label", "${name}")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("${name}", "" + db.tables.get("T2").djLabel);

    // rename and set dj-label
    update("Table", "dj/ddl/T2", newHashMap(of("name", "T", "dj-label", "display ${name}")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals(1, db.tables.size());
    Assert.assertEquals("display ${name}", "" + db.tables.get("T").djLabel);

    // rename make sure label stays
    update("Table", "dj/ddl/T", newHashMap(of("name", "T2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("display ${name}", "" + db.tables.get("T2").djLabel);

    // name back twice, second is ignored
    update("Table", "dj/ddl/T2", newHashMap(of("name", "T")));
    update("Table", "dj/ddl/T", newHashMap(of("name", "T")));

    // TODO: rename table with custom write to prop

    // create col
    create("Property", newHashMap(of("parent", "dj/ddl/T", "name", "age", "type", "date")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("date", db.tables.get("T").properties.get("age").widget);

    // recreate fails
    try {
      create("Property", newHashMap(of("parent", "dj/ddl/T", "name", "age", "type", "date")));
      Assert.fail();
    } catch (Exception ok) {
    }

    // rename col
    update("Property", "dj/ddl/T/age", newHashMap(of("name", "age2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("date", db.tables.get("T").properties.get("age2").widget);

    // set ref (i.e. no schema update)
    update("Property", "dj/ddl/T/age2", newHashMap(of("ref", "theref")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("theref", "" + db.tables.get("T").properties.get("age2").ref);

    // rename and set ref
    update("Property", "dj/ddl/T/age2", newHashMap(of("name", "age", "ref", "refref")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("refref", "" + db.tables.get("T").properties.get("age").ref);

    // rename make sure label stays
    update("Property", "dj/ddl/T/age", newHashMap(of("name", "age2")));
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("refref", "" + db.tables.get("T").properties.get("age2").ref);

    // name back twice, second is ignored
    update("Property", "dj/ddl/T/age2", newHashMap(of("name", "age")));
    update("Property", "dj/ddl/T/age", newHashMap(of("name", "age")));

    // drop column
    delete("Property", "dj/ddl/T/age");
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertNull(db.tables.get("T").properties.get("age"));

    // drop table and metadata
    delete("Table", "dj/ddl/T");
    db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals(0, db.tables.size());

    new File("model/dj-database/dj%2Fddl.json").delete();
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
    data.create(sc, "config", table, object);
  }

  void update(String table, String search, Map<String, Object> object) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    // Data data = new Data();
    // data.services = services;
    // data.function = new FunctionService();
    // data.function.services = services;
    data.update(sc, "config", table, search, object);
  }

  void delete(String table, String search) throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(Matchers.anyString())).thenReturn(true);
    // Data data = new Data();
    // data.services = services;
    // data.function = new FunctionService();
    // data.function.services = services;
    data.delete(sc, "config", table, search);
  }

  @Test
  public void ztestMerge() throws Exception {

    AbstractSource s = new Provider();
    s.database = "ddl";
    // s.services = services;
    FieldUtils.writeField(s, "services", services, true);
    s.createSchema = true;
    s.mappings = ImmutableMap.of("t", new Mapping());
    s.mappings.get("t").pk = "ID";

    s.run(null);

    AbstractDatabase db = services.getConfig().getDatabase("dj/ddl");
    Assert.assertEquals("b",
        db.all(Table.ofName("t"), null, null, null, false, null).get(1).get("name"));
  }
}
