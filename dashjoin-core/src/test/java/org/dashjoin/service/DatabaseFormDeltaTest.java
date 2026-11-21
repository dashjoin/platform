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
 * Baseline: documents what saving the database form (an update of the dj-database record, e.g. the
 * connection settings) does to the app's table and column settings, which live in the same
 * record, as of 7.1. PojoDatabase.update drops tables and status from what the form sends, stores
 * the changed fields, then reconnects the database.
 *
 * Uses the dj/ddl test database (TestDatabase), like DDLTest.
 */
@QuarkusTest
public class DatabaseFormDeltaTest {

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

  PojoDatabase config() {
    return (PojoDatabase) services.getConfig();
  }

  /** the database record as the form gets it */
  Map<String, Object> form() throws Exception {
    return config().read(Table.ofName("dj-database"), m("ID", "dj/ddl"));
  }

  /** what the app stores for dj/ddl */
  Map<String, Object> stored() throws Exception {
    return config().user().read(Table.ofName("dj-database"), m("ID", "dj/ddl"));
  }

  /** the stored record as it is on disk (password encrypted) */
  String storedPassword() throws Exception {
    return config().password("dj-database", "dj/ddl");
  }

  void save(Map<String, Object> form) throws Exception {
    data.update(sc(), "config", "dj-database", "dj/ddl", form);
  }

  static Map<String, Object> settings() {
    return m("T", m("instanceLayout", m("widget", "page"), "title", "Staff", "properties",
        m("AGE", m("title", "Age"))));
  }

  @BeforeEach
  public void setup() throws Exception {
    TestDatabase.init();
    new File(APP_FILE).delete();
    // the file was changed behind the config DB's back
    JSONFileCache.clear();
    data.create(sc(), "config", "Table", m("parent", "dj/ddl", "name", "T"));
    data.create(sc(), "config", "Property",
        m("parent", "dj/ddl/T", "name", "AGE", "type", "string"));
    data.update(sc(), "config", "Table", "dj/ddl/T",
        m("instanceLayout", m("widget", "page"), "title", "Staff"));
    data.update(sc(), "config", "Property", "dj/ddl/T/AGE", m("title", "Age"));
    Assertions.assertEquals(settings(), stored().get("tables"));
  }

  @AfterEach
  public void teardown() {
    new File(APP_FILE).delete();
    // the file was changed behind the config DB's back
    JSONFileCache.clear();
    TestDatabase.init();
  }

  @Test
  public void saveFormKeepsTableSettings() throws Exception {
    // the form sends the whole record as read, including tables and status, with one change
    Map<String, Object> form = form();
    Assertions.assertNotNull(form.get("tables"));
    Assertions.assertNotNull(form.get("status"));
    form.put("comment", "changed");
    save(form);

    Map<String, Object> stored = stored();
    Assertions.assertEquals("changed", stored.get("comment"));
    Assertions.assertEquals(settings(), stored.get("tables"));
    Assertions.assertNull(stored.get("status"));

    // the reconnected database still has the settings
    Assertions.assertEquals(m("widget", "page"),
        config().read(Table.ofName("Table"), m("ID", "dj/ddl/T")).get("instanceLayout"));
    Assertions.assertEquals("Age",
        config().read(Table.ofName("Property"), m("ID", "dj/ddl/T/AGE")).get("title"));
  }

  @Test
  public void saveFormStoresOnlyChangedFields() throws Exception {
    // fields equal to the platform's definition (classpath dj/ddl) are not stored
    Map<String, Object> form = form();
    form.put("comment", "changed");
    save(form);
    Assertions.assertEquals(m("ID", "dj/ddl", "comment", "changed", "tables", settings()),
        stored());
  }

  @Test
  public void passwordIsEncryptedAndMasked() throws Exception {
    Map<String, Object> form = form();
    form.put("password", "secret");
    save(form);

    Assertions.assertEquals("********", form().get("password"));
    String encrypted = storedPassword();
    Assertions.assertNotEquals("secret", encrypted);
    Assertions.assertEquals("secret", new String(CredentialManager.decryptCredential(encrypted)));
    Assertions.assertEquals(settings(), stored().get("tables"));
  }

  @Test
  public void maskedPasswordKeepsPassword() throws Exception {
    Map<String, Object> form = form();
    form.put("password", "secret");
    save(form);
    String encrypted = storedPassword();

    // the form sends the mask back with another change
    form = form();
    Assertions.assertEquals("********", form.get("password"));
    form.put("comment", "changed");
    save(form);

    Assertions.assertEquals(encrypted, storedPassword());
    Assertions.assertEquals("changed", stored().get("comment"));
    Assertions.assertEquals(settings(), stored().get("tables"));
  }
}
