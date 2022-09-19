package org.dashjoin.service;

import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@SuppressWarnings("unchecked")
public class JsonApiTest {

  @Inject
  JsonApi api;

  @Test
  public void testDatabase() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost/"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertTrue(api.discover(uriInfo, sc, "junit").toString()
        .contains("http://localhost/rest/jsonapi/junit/EMP"));
  }

  @Test
  public void testRow() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost/"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "{links={self=http://localhost/rest/jsonapi/junit/EMP/1}, data={id=1, type=EMP, attributes={NAME=mike, WORKSON=1000}, relationships={WORKSON={links={related=http://localhost/rest/jsonapi/junit/PRJ/1000}}}}}",
        api.getRecord(uriInfo, sc, "junit", "EMP", "1").toString());
  }

  Object p(Map<String, Object> o) {
    List<Map<String, Object>> l = (List<Map<String, Object>>) o.get("data");
    return l.stream().map(e -> e.get("attributes")).collect(Collectors.toList());
  }

  @Test
  public void testTable() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost/"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{NAME=mike, WORKSON=1000}, {NAME=joe, WORKSON=1000}]",
        p(api.getTable(uriInfo, sc, "junit", "EMP", null, null, null, null)).toString());
  }

  @Test
  public void testFilter() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost/"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{NAME=joe, WORKSON=1000}]",
        p(api.getTable(uriInfo, sc, "junit", "EMP", null, "{\"NAME\": \"joe\"}", null, null))
            .toString());
  }

  @Test
  public void testSort() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri()).thenReturn(new URI("http://localhost/"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{NAME=joe, WORKSON=1000}, {NAME=mike, WORKSON=1000}]",
        p(api.getTable(uriInfo, sc, "junit", "EMP", "-ID", null, null, null)).toString());
  }

  @Test
  public void testSelect() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    Mockito.when(uriInfo.getBaseUri())
        .thenReturn(new URI("http://localhost/?fields[EMP]=NAME,WORKSON"));
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("[{NAME=joe, WORKSON=1000}, {NAME=mike, WORKSON=1000}]",
        p(api.getTable(uriInfo, sc, "junit", "EMP", "-ID", null, null, null)).toString());
  }
}
