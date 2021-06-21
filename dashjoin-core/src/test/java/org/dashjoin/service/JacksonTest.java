package org.dashjoin.service;

import java.sql.SQLException;
import java.util.Date;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * tests the Exception mapper and the jackson serialization config
 */
public class JacksonTest {

  void check(Response res, int status, String entity, String reason) {
    Assert.assertEquals(status, res.getStatus());
    Assert.assertEquals(entity, res.getEntity());
    Assert.assertEquals(reason, res.getStatusInfo().getReasonPhrase());
  }

  @Test
  public void testMapper() {
    ExMapper m = new ExMapper();
    Response res = m.toResponse("test");
    check(res, 500, "test", "Internal Server Error");
  }

  @Test
  public void test404() {
    ExMapper m = new ExMapper();
    Response res = m.toResponse(new NotFoundException());
    check(res, 404, null, "Not Found");
  }

  @Test
  public void testNPE() {
    ExMapper m = new ExMapper();
    Response res = m.toResponse(new NullPointerException());
    check(res, 500, "NullPointerException", "Internal Server Error");
  }

  @Test
  public void testSQL() {
    ExMapper m = new ExMapper();
    Response res = m.toResponse(new SQLException("col X not found"));
    check(res, 500, "col X not found", "Internal Server Error");
  }

  @Test
  public void testSerializer() {
    JacksonConfig c = new JacksonConfig();
    Assert.assertEquals("1970-01-01T00:00:00.000+00:00",
        c.getContext(null).valueToTree(new Date(0)).asText());
  }

  @Test
  public void testGetMessage() {
    Assert.assertEquals("NullPointerException", ExMapper.getMessage(new NullPointerException()));
    Assert.assertEquals("NullPointerException",
        ExMapper.getMessage(new Exception(null, new NullPointerException())));
    Assert.assertEquals("test", ExMapper.getMessage(new Exception("test")));
    Assert.assertEquals("inner",
        ExMapper.getMessage(new RuntimeException(null, new NullPointerException("inner"))));
    Assert.assertEquals("Error decrypting. Please re-enter the credentials.",
        ExMapper.getMessage(new EncryptionOperationNotPossibleException()));
  }
}
