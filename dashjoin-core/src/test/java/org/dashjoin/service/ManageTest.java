package org.dashjoin.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.service.Manage.DetectResult;
import org.dashjoin.service.Manage.TypeSample;
import org.dashjoin.util.MapUtil;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ManageTest {

  @Inject
  Services services;

  @Inject
  Manage manage;

  @Test
  public void testVersion() {
    Assertions.assertTrue(manage.getFunctions().size() > 0);
    Assertions.assertTrue(manage.getDatabases().size() > 0);
    Assertions.assertTrue(manage.getDrivers().size() > 0);
    Assertions.assertEquals("Dashjoin Low Code Development and Integration Platform",
        manage.version().name);
  }

  @Test
  public void testExport() throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "[{ID=1000, NAME=dev-project, BUDGET=null}, {ID=1001, NAME=other, BUDGET=null}]",
        manage.export(sc, "junit").get("PRJ").toString());
  }

  @Test
  public void testExportAcl() throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole("admin")).thenReturn(false);
    when(sc.isUserInRole("authenticated")).thenReturn(true);
    Assertions.assertEquals("[{ID=1000, NAME=dev-project, BUDGET=null}]",
        manage.export(sc, "junit").get("PRJ").toString());
  }

  @Test
  public void testConfigFunctions() {
    Assertions.assertTrue(manage.getConfigurableFunctions().size() > 0);
  }

  @Test
  public void testRoles() throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertTrue(manage.roles(sc).size() > 0);
  }

  @Test
  public void logger() {
    services.logConfigSettings();
  }

  @Test
  public void testGetFileName() {
    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.put("Content-Disposition", Arrays.asList("filename=\"filename.jpg\""));
    String name = new Manage().getFileName(headers);
    Assertions.assertEquals("filename", name);
  }

  @Test
  public void testGetFileNameEx() {
    Assertions.assertThrows(RuntimeException.class, () -> {
      MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
      headers.put("Content-Disposition", Arrays.asList("bla"));
      new Manage().getFileName(headers);
    });
  }

  @Test
  public void detectExisting() throws Exception {
    DetectResult dr = detect("PRJ.csv", "ID,NAME,BUDGET\n17,import,1");
    Assertions.assertEquals(false, dr.createMode);
    Assertions.assertEquals(true, dr.schema.get("PRJ").get(0).pk);
    Assertions.assertEquals("ID", dr.schema.get("PRJ").get(0).name);
    Assertions.assertEquals("17", dr.schema.get("PRJ").get(0).sample.get(0));
    Assertions.assertEquals("number", dr.schema.get("PRJ").get(0).type);
    Assertions.assertEquals(false, dr.schema.get("PRJ").get(1).pk);
    Assertions.assertEquals("NAME", dr.schema.get("PRJ").get(1).name);
    Assertions.assertEquals("import", dr.schema.get("PRJ").get(1).sample.get(0));
    Assertions.assertEquals("string", dr.schema.get("PRJ").get(1).type);
  }

  @Test
  public void detectNew() throws Exception {
    DetectResult dr = detect("NEW.csv", "B,I,D,S\ntrue,42,3.14,hello\ntrue,42,3.14,pk");
    Assertions.assertEquals(true, dr.createMode);
    Assertions.assertEquals(false, dr.schema.get("NEW").get(0).pk);
    Assertions.assertEquals("B", dr.schema.get("NEW").get(0).name);
    Assertions.assertEquals(true, dr.schema.get("NEW").get(0).sample.get(0));
    Assertions.assertEquals("boolean", dr.schema.get("NEW").get(0).type);
    Assertions.assertEquals(false, dr.schema.get("NEW").get(1).pk);
    Assertions.assertEquals("I", dr.schema.get("NEW").get(1).name);
    Assertions.assertEquals(42, dr.schema.get("NEW").get(1).sample.get(0));
    Assertions.assertEquals("integer", dr.schema.get("NEW").get(1).type);
    Assertions.assertEquals(false, dr.schema.get("NEW").get(2).pk);
    Assertions.assertEquals("D", dr.schema.get("NEW").get(2).name);
    Assertions.assertEquals(3.14, dr.schema.get("NEW").get(2).sample.get(0));
    Assertions.assertEquals("number", dr.schema.get("NEW").get(2).type);
    Assertions.assertEquals(true, dr.schema.get("NEW").get(3).pk);
    Assertions.assertEquals("S", dr.schema.get("NEW").get(3).name);
    Assertions.assertEquals("hello", dr.schema.get("NEW").get(3).sample.get(0));
    Assertions.assertEquals("string", dr.schema.get("NEW").get(3).type);
  }

  @Test
  public void detectMissingValues() throws Exception {
    detect("NEW.csv", "ID,NAME,AGE\n17.3,import,2\n,test");
  }

  @Test
  public void detectVaryingType() throws Exception {
    // float and int
    DetectResult dr = detect("NEW.csv", "number\n1\n2.2\n3\n");
    Assertions.assertEquals("number", dr.schema.get("NEW").get(0).type);

    // empty
    dr = detect("NEW.csv", "string\n");
    Assertions.assertEquals("string", dr.schema.get("NEW").get(0).type);

    // string bool
    dr = detect("NEW.csv", "string\ntrue\nstring");
    Assertions.assertEquals("string", dr.schema.get("NEW").get(0).type);

    // bool only
    dr = detect("NEW.csv", "boolean\ntrue\nfalse");
    Assertions.assertEquals("boolean", dr.schema.get("NEW").get(0).type);

    // bool and int
    dr = detect("NEW.csv", "string\ntrue\n4");
    Assertions.assertEquals("string", dr.schema.get("NEW").get(0).type);
  }

  @Test
  public void detectColMismatch() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
      detect("PRJ.csv", "NAME\nimport");
    });
  }

  @Test
  public void detectMixedTables() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
      detect("PRJ.csv", "ID,NAME\n17,import", "UNKNOWN.csv", "COL");
    });
  }

  @Test
  public void detectMixedTables2() throws Exception {
    Assertions.assertThrows(Exception.class, () -> {
      detect("UNKNOWN.csv", "COL", "PRJ.csv", "ID,NAME\n17,import");
    });
  }

  @Test
  public void testXlsx() throws Exception {
    DetectResult res =
        detect("import.xlsx", getClass().getResourceAsStream("/data/import.xlsx"), null, null);
    List<TypeSample> x = res.schema.values().iterator().next();
    Assertions.assertEquals("ID", x.get(0).name);
    Assertions.assertEquals(1.0, x.get(0).sample.get(0));
    Assertions.assertEquals(2.0, x.get(0).sample.get(1));
    Assertions.assertEquals("name", x.get(1).name);
    Assertions.assertEquals("joe", x.get(1).sample.get(0));
  }

  @Test
  public void testSQLite() throws Exception {
    DetectResult res =
        detect("import.sqlite", getClass().getResourceAsStream("/data/import.sqlite"), null, null);
    List<TypeSample> x = res.schema.values().iterator().next();
    Assertions.assertEquals("ID", x.get(0).name);
    Assertions.assertEquals(1, x.get(0).sample.get(0));
    Assertions.assertEquals(2, x.get(0).sample.get(1));
    Assertions.assertEquals("name", x.get(1).name);
    Assertions.assertEquals("test", x.get(1).sample.get(0));
  }

  @Test
  public void testHandleJson() throws Exception {
    DetectResult res = new DetectResult();
    res.createMode = true;
    new Manage().handleJson(res, null, null, null, null,
        Arrays.asList(MapUtil.of("y", 2), MapUtil.of("x", 1, "y", 2), MapUtil.of("y", 2, "x", 1)));
    Assertions.assertEquals(2, res.schema.get(null).size());
    Assertions.assertEquals("y", res.schema.get(null).get(0).name);
    Assertions.assertEquals("x", res.schema.get(null).get(1).name);
    Assertions.assertEquals(2, res.schema.get(null).get(0).sample.get(1));
    Assertions.assertEquals(1, res.schema.get(null).get(1).sample.get(1));
  }

  @Test
  public void testCreateAcl() throws Exception {
    Assertions.assertThrows(NotAuthorizedException.class, () -> {
      SecurityContext sc = mock(SecurityContext.class);
      when(sc.isUserInRole(ArgumentMatchers.eq("admin"))).thenReturn(false);
      when(sc.isUserInRole(ArgumentMatchers.eq("authenticated"))).thenReturn(true);
      MultipartFormDataInput input = mock(MultipartFormDataInput.class);
      InputPart token = mock(InputPart.class);
      when(token.getBody(InputStream.class, null))
          .thenReturn(new ByteArrayInputStream("{}".getBytes()));
      when(input.getFormDataMap()).thenReturn(MapUtil.of("__dj_schema", Arrays.asList(token)));
      manage.create(sc, "junit", input);
    });
  }

  @Test
  public void testCreate() throws Exception {
    for (String filename : new String[] {"U/card.json", "import.xlsx", "json.json", "import.csv",
        "import.sqlite"})
      for (int i = 0; i < 3; i++) {
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

        DetectResult res;
        {
          MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
          headers.put("Content-Disposition", Arrays.asList("filename=\"" + filename + "\""));
          InputPart token = mock(InputPart.class);
          when(token.getHeaders()).thenReturn(headers);
          when(token.getBody(InputStream.class, null))
              .thenReturn(getClass().getResourceAsStream("/data/" + filename));

          MultipartFormDataInput input = mock(MultipartFormDataInput.class);
          Map<String, List<InputPart>> paramsMap = new HashMap<>();
          paramsMap.put("file", Arrays.asList(token));
          when(input.getFormDataMap()).thenReturn(paramsMap);

          res = manage.detect(sc, "junit", input);
        }

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.put("Content-Disposition", Arrays.asList("filename=\"" + filename + "\""));
        InputPart token = mock(InputPart.class);
        when(token.getHeaders()).thenReturn(headers);
        when(token.getBody(InputStream.class, null))
            .thenReturn(getClass().getResourceAsStream("/data/" + filename));

        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        Map<String, List<InputPart>> paramsMap = new HashMap<>();
        paramsMap.put("file", Arrays.asList(token));
        when(input.getFormDataMap()).thenReturn(paramsMap);

        InputPart schema = mock(InputPart.class);
        when(schema.getHeaders()).thenReturn(headers);
        when(schema.getBody(InputStream.class, null)).thenReturn(
            new ByteArrayInputStream(new ObjectMapper().writeValueAsString(res.schema).getBytes()));
        paramsMap.put("__dj_schema", Arrays.asList(schema));

        if (i == 0)
          manage.create(sc, "ddl", input);
        if (i == 1)
          if (filename.equals("U/card.json"))
            manage.append(sc, "junit", input);
          else
            // table does not exist in junit
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
              manage.append(sc, "junit", input);
            });
        if (i == 2)
          manage.replace(sc, "ddl", input);
      }
  }

  DetectResult detect(String filename, String csv) throws Exception {
    return detect(filename, csv, null, null);
  }

  DetectResult detect(String filename, String csv, String filename2, String csv2) throws Exception {
    return detect(filename, new ByteArrayInputStream(csv.getBytes()), filename2, csv2);
  }

  DetectResult detect(String filename, InputStream csv, String filename2, String csv2)
      throws Exception {
    SecurityContext sc = mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);

    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    headers.put("Content-Disposition", Arrays.asList("filename=\"" + filename + "\""));
    InputPart token = mock(InputPart.class);
    when(token.getHeaders()).thenReturn(headers);
    when(token.getBody(InputStream.class, null)).thenReturn(csv);

    MultivaluedMap<String, String> headers2 = new MultivaluedHashMap<>();
    headers2.put("Content-Disposition", Arrays.asList("filename=\"" + filename2 + "\""));
    InputPart token2 = mock(InputPart.class);
    when(token2.getHeaders()).thenReturn(headers2);
    if (csv2 != null)
      when(token2.getBody(InputStream.class, null))
          .thenReturn(new ByteArrayInputStream(csv2.getBytes()));

    Map<String, List<InputPart>> paramsMap = new HashMap<>();
    if (csv2 == null)
      paramsMap.put("file", Arrays.asList(token));
    else
      paramsMap.put("file", Arrays.asList(token, token2));

    MultipartFormDataInput input = mock(MultipartFormDataInput.class);
    when(input.getFormDataMap()).thenReturn(paramsMap);

    Manage m = new Manage();
    m.services = services;
    return m.detect(sc, "junit", input);
  }
}
