package org.dashjoin.service;

import static org.mockito.Mockito.when;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class OdataTest {

  @Inject
  OdataApi api;

  @Test
  public void testRow() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("{value={ID=1, NAME=mike, WORKSON=1000}}",
        api.getTable(uriInfo, sc, "junit", "EMP('1')", null, null, null, null, null).toString());
  }

  @Test
  public void testField() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("{value=mike}",
        api.getField(uriInfo, sc, "junit", "EMP('1')", "NAME", null, null, null, null, null)
            .toString());
  }

  @Test
  public void testValue() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("mike",
        api.getValue(uriInfo, sc, "junit", "EMP('1')", "NAME", null, null, null, null, null)
            .toString());
  }

  @Test
  public void testTable() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "{value=[{ID=1, NAME=mike, WORKSON=1000}, {ID=2, NAME=joe, WORKSON=1000}]}",
        api.getTable(uriInfo, sc, "junit", "EMP", null, null, null, null, null).toString());
  }

  @Test
  public void testFilter() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("{value=[{ID=2, NAME=joe, WORKSON=1000}]}", api
        .getTable(uriInfo, sc, "junit", "EMP", "NAME eq 'joe'", null, null, null, null).toString());
    Assertions.assertEquals("{value=[{ID=2, NAME=joe, WORKSON=1000}]}",
        api.getTable(uriInfo, sc, "junit", "EMP", "ID eq 2", null, null, null, null).toString());
  }

  @Test
  public void testSort() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals(
        "{value=[{ID=2, NAME=joe, WORKSON=1000}, {ID=1, NAME=mike, WORKSON=1000}]}",
        api.getTable(uriInfo, sc, "junit", "EMP", null, null, "ID desc", null, null).toString());
  }

  @Test
  public void testSelect() throws Exception {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Assertions.assertEquals("{value=[{NAME=joe, WORKSON=1000}, {NAME=mike, WORKSON=1000}]}",
        api.getTable(uriInfo, sc, "junit", "EMP", null, "NAME, WORKSON", "ID desc", null, null)
            .toString());
  }
}
