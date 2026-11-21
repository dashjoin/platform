package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.io.File;
import java.util.Map;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Baseline: documents which of the app's table and column settings survive a rename or drop made
 * in Dashjoin (the AlterTableTrigger / AlterColumnTrigger schema changes on the dj/ddl test
 * database), as of 7.1.
 *
 * A rename deletes the app's settings under the old name and copies some of them to the new name:
 * for tables dj-label, the triggers, readRoles, writeRoles, title, comment, tenantColumn and
 * roleMappings; for columns pkpos, ref and displayWith. Everything else is lost.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
@QuarkusTest
public class RenameDeltaTest {

  @Inject
  Services services;

  @Inject
  Data data;

  static final String APP_FILE = "model/dj-database/dj%2Fddl.json";

  SecurityContext sc() {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    return sc;
  }

  void create(String table, Map<String, Object> object) throws Exception {
    data.create(sc(), "config", table, object);
  }

  void update(String table, String id, Map<String, Object> object) throws Exception {
    data.update(sc(), "config", table, id, object);
  }

  void delete(String table, String id) throws Exception {
    data.delete(sc(), "config", table, id);
  }

  PojoDatabase config() {
    return (PojoDatabase) services.getConfig();
  }

  Map<String, Object> table(String id) throws Exception {
    return config().read(Table.ofName("Table"), m("ID", id));
  }

  Map<String, Object> column(String id) throws Exception {
    return config().read(Table.ofName("Property"), m("ID", id));
  }

  /** the app's settings for the tables of dj/ddl (null: none) */
  @SuppressWarnings("unchecked")
  Map<String, Object> stored() throws Exception {
    Map<String, Object> db = config().user().read(Table.ofName("dj-database"), m("ID", "dj/ddl"));
    return db == null ? null : (Map<String, Object>) db.get("tables");
  }

  @BeforeEach
  public void setup() throws Exception {
    TestDatabase.init();
    new File(APP_FILE).delete();

    // table T with columns NAME and AGE
    create("Table", m("parent", "dj/ddl", "name", "T"));
    create("Property", m("parent", "dj/ddl/T", "name", "NAME", "type", "string"));
    create("Property", m("parent", "dj/ddl/T", "name", "AGE", "type", "string"));

    // the app's settings
    update("Table", "dj/ddl/T", m("instanceLayout", m("widget", "page"), "tableLayout",
        m("widget", "table"), "title", "Staff", "dj-label", "${NAME}"));
    update("Property", "dj/ddl/T/AGE",
        m("title", "Age", "comment", "in years", "ref", "dj/ddl/T/NAME"));
    update("Property", "dj/ddl/T/NAME", m("title", "Name"));
  }

  @AfterEach
  public void teardown() {
    new File(APP_FILE).delete();
    TestDatabase.init();
  }

  @Test
  public void settingsBeforeRename() throws Exception {
    Assertions.assertEquals(m("widget", "page"), table("dj/ddl/T").get("instanceLayout"));
    Assertions.assertEquals("Staff", table("dj/ddl/T").get("title"));
    Assertions.assertEquals("Age", column("dj/ddl/T/AGE").get("title"));
    Assertions.assertEquals("dj/ddl/T/NAME", column("dj/ddl/T/AGE").get("ref"));
  }

  @Test
  public void renameTable() throws Exception {
    update("Table", "dj/ddl/T", m("name", "T2"));

    // kept: dj-label and title
    Map<String, Object> t2 = table("dj/ddl/T2");
    Assertions.assertEquals("Staff", t2.get("title"));
    Assertions.assertEquals("${NAME}", t2.get("dj-label"));

    // status quo: the layouts and all column settings are lost
    Assertions.assertNull(t2.get("instanceLayout"));
    Assertions.assertNull(t2.get("tableLayout"));
    Assertions.assertNull(column("dj/ddl/T2/AGE").get("title"));
    Assertions.assertNull(column("dj/ddl/T2/AGE").get("ref"));
    Assertions.assertNull(column("dj/ddl/T2/NAME").get("title"));

    // nothing is left under the old name
    Assertions.assertNull(table("dj/ddl/T"));
    Assertions.assertEquals(m("T2", m("dj-label", "${NAME}", "title", "Staff")), stored());
  }

  @Test
  public void renameColumn() throws Exception {
    update("Property", "dj/ddl/T/AGE", m("name", "YEARS"));

    // kept: ref
    Assertions.assertEquals("dj/ddl/T/NAME", column("dj/ddl/T/YEARS").get("ref"));

    // status quo: title and comment are lost
    Assertions.assertNull(column("dj/ddl/T/YEARS").get("title"));
    Assertions.assertNull(column("dj/ddl/T/YEARS").get("comment"));

    // other columns and the table's settings are not affected
    Assertions.assertNull(column("dj/ddl/T/AGE"));
    Assertions.assertEquals("Name", column("dj/ddl/T/NAME").get("title"));
    Assertions.assertEquals(m("widget", "page"), table("dj/ddl/T").get("instanceLayout"));

    @SuppressWarnings("unchecked")
    Map<String, Object> props =
        (Map<String, Object>) ((Map<String, Object>) stored().get("T")).get("properties");
    Assertions.assertEquals(m("NAME", m("title", "Name"), "YEARS", m("ref", "dj/ddl/T/NAME")),
        props);
  }

  @Test
  public void changeColumnTypeKeepsSettings() throws Exception {
    update("Property", "dj/ddl/T/AGE", m("type", "number"));
    Assertions.assertEquals("Age", column("dj/ddl/T/AGE").get("title"));
    Assertions.assertEquals("in years", column("dj/ddl/T/AGE").get("comment"));
  }

  @Test
  public void dropColumn() throws Exception {
    delete("Property", "dj/ddl/T/AGE");
    Assertions.assertNull(column("dj/ddl/T/AGE"));

    @SuppressWarnings("unchecked")
    Map<String, Object> props =
        (Map<String, Object>) ((Map<String, Object>) stored().get("T")).get("properties");
    Assertions.assertEquals(m("NAME", m("title", "Name")), props);

    // a new column with the old name starts without settings
    create("Property", m("parent", "dj/ddl/T", "name", "AGE", "type", "string"));
    Assertions.assertNull(column("dj/ddl/T/AGE").get("title"));
  }

  @Test
  public void dropTable() throws Exception {
    delete("Table", "dj/ddl/T");
    Assertions.assertNull(table("dj/ddl/T"));
    Assertions.assertNull(stored());

    // a new table with the old name starts without settings
    create("Table", m("parent", "dj/ddl", "name", "T"));
    Assertions.assertNull(table("dj/ddl/T").get("instanceLayout"));
  }
}
