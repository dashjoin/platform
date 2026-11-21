package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Table;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Baseline: documents what uploading a model folder into the config database does ("Upload to the
 * Configuration Database", Manage.replace / Manage.append with files named model/TABLE/ID.json),
 * as of 7.1.
 *
 * replace first deletes the app's records of each uploaded table. Records the platform ships are
 * then updated with UnionDatabase.update, all others are created. Unlike an update from the UI,
 * a changed platform record is stored whole (see replaceStoresWholePlatformRecordIfChanged).
 * append creates every record.
 *
 * Uses dj-config, whose records the platform ships (e.g. sidenav-width-px) and which has no app
 * records in the test working directory.
 */
@QuarkusTest
public class UploadDeltaTest {

  @Inject
  Services services;

  @Inject
  Manage manage;

  @Inject
  Data data;

  final Table config = Table.ofName("dj-config");

  /** app records this test may create */
  static final String[] IDS = {"sidenav-width-px", "sidenav-open", "upload-test", "upload-old"};

  static final String SIDENAV =
      "{\"ID\": \"sidenav-width-px\", \"description\": \"Width of the side navigation bar "
          + "(changes applied after next login)\", \"category\": \"ui\", \"type\": \"integer\", "
          + "\"integer\": %d}";

  SecurityContext sc() {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    return sc;
  }

  PojoDatabase configDb() {
    return (PojoDatabase) services.getConfig();
  }

  /** the uploaded files: file name to JSON */
  MultipartFormDataInput upload(String... nameAndJson) throws Exception {
    List<InputPart> parts = new ArrayList<>();
    for (int i = 0; i < nameAndJson.length; i += 2) {
      MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
      headers.put("Content-Disposition",
          Arrays.asList("filename=\"" + nameAndJson[i] + "\""));
      InputPart part = mock(InputPart.class);
      when(part.getHeaders()).thenReturn(headers);
      String json = nameAndJson[i + 1];
      // the body is read once per pass over the parts
      when(part.getBody(InputStream.class, null)).thenAnswer(
          inv -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
      parts.add(part);
    }
    Map<String, List<InputPart>> map = new HashMap<>();
    map.put("file", parts);
    MultipartFormDataInput input = mock(MultipartFormDataInput.class);
    when(input.getFormDataMap()).thenReturn(map);
    return input;
  }

  /** what the app stores (null: nothing) */
  Map<String, Object> stored(String id) throws Exception {
    return configDb().user().read(config, m("ID", id));
  }

  Map<String, Object> read(String id) throws Exception {
    return configDb().read(config, m("ID", id));
  }

  void clean() {
    for (String id : IDS)
      new File("model/dj-config/" + id + ".json").delete();
    File dir = new File("model/dj-config");
    String[] left = dir.list();
    if (left != null && left.length == 0)
      dir.delete();
  }

  @BeforeEach
  public void setup() {
    clean();
    Assertions.assertFalse(new File("model/dj-config").exists(),
        "the test expects no app dj-config records in the working directory");
  }

  @AfterEach
  public void teardown() {
    clean();
  }

  @Test
  public void replaceStoresWholePlatformRecordIfChanged() throws Exception {
    manage.replace(sc(), "config",
        upload("model/dj-config/sidenav-width-px.json", String.format(SIDENAV, 300),
            "model/dj-config/upload-test.json",
            "{\"ID\": \"upload-test\", \"type\": \"string\", \"string\": \"x\"}"));

    // status quo: if any field of a platform record differs, the whole uploaded record is stored,
    // including the fields equal to the platform's. Manage passes the whole record as the search
    // key to db.update(table, object, object), and UnionDatabase.update creates the missing app
    // record from the search key. A later platform change of description or category is hidden
    Assertions.assertEquals(m("ID", "sidenav-width-px", "description",
        "Width of the side navigation bar (changes applied after next login)", "category", "ui",
        "type", "integer", "integer", 300), stored("sidenav-width-px"));
    Assertions.assertEquals(300, read("sidenav-width-px").get("integer"));
    Assertions.assertEquals("ui", read("sidenav-width-px").get("category"));

    // app record: stored whole
    Assertions.assertEquals(m("ID", "upload-test", "type", "string", "string", "x"),
        stored("upload-test"));
  }

  @Test
  public void replaceWithPlatformValueStoresNothing() throws Exception {
    manage.replace(sc(), "config",
        upload("model/dj-config/sidenav-width-px.json", String.format(SIDENAV, 200)));
    Assertions.assertNull(stored("sidenav-width-px"));
    Assertions.assertEquals(200, read("sidenav-width-px").get("integer"));
  }

  @Test
  public void replaceRemovesAppRecordsNotInUpload() throws Exception {
    // the app has its own record and a change of a platform record
    data.create(sc(), "config", "dj-config",
        m("ID", "upload-old", "type", "string", "string", "old"));
    data.update(sc(), "config", "dj-config", "sidenav-open", m("boolean", false));
    Assertions.assertNotNull(stored("sidenav-open"));

    manage.replace(sc(), "config", upload("model/dj-config/upload-test.json",
        "{\"ID\": \"upload-test\", \"type\": \"string\", \"string\": \"x\"}"));

    // status quo: replace deletes all app records of the table first. The app's own record is gone
    // and the change of the platform record is reset
    Assertions.assertNull(read("upload-old"));
    Assertions.assertNull(stored("sidenav-open"));
    Assertions.assertNotNull(read("sidenav-open"));
    Assertions.assertNotNull(stored("upload-test"));
  }

  @Test
  public void appendCreatesAppRecord() throws Exception {
    manage.append(sc(), "config", upload("model/dj-config/upload-test.json",
        "{\"ID\": \"upload-test\", \"type\": \"string\", \"string\": \"x\"}"));
    Assertions.assertEquals(m("ID", "upload-test", "type", "string", "string", "x"),
        stored("upload-test"));
  }

  @Test
  public void appendFailsForPlatformRecord() throws Exception {
    // status quo: append creates, so a record the platform ships cannot be appended
    Exception e = Assertions.assertThrows(Exception.class, () -> manage.append(sc(), "config",
        upload("model/dj-config/sidenav-width-px.json", String.format(SIDENAV, 300))));
    Assertions.assertTrue(e.getMessage().contains("already exists"), e.getMessage());
    Assertions.assertNull(stored("sidenav-width-px"));
  }

  @Test
  public void appendFailsForExistingAppRecord() throws Exception {
    data.create(sc(), "config", "dj-config",
        m("ID", "upload-test", "type", "string", "string", "old"));
    Assertions.assertThrows(Exception.class, () -> manage.append(sc(), "config",
        upload("model/dj-config/upload-test.json",
            "{\"ID\": \"upload-test\", \"type\": \"string\", \"string\": \"new\"}")));
    Assertions.assertEquals("old", read("upload-test").get("string"));
  }
}
