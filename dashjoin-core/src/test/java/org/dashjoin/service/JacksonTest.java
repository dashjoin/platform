package org.dashjoin.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;


/**
 * tests the Exception mapper and the jackson serialization config
 */
public class JacksonTest {

  void check(Response res, int status, String entity, String reason) {
    Assertions.assertEquals(status, res.getStatus());
    Assertions.assertEquals(entity, res.getEntity());
    Assertions.assertEquals(reason, res.getStatusInfo().getReasonPhrase());
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
    ExMapper.logger.setLevel(Level.OFF);
    Response res = m.toResponse(new NotFoundException());
    check(res, 404, null, "Not Found");
  }

  @Test
  public void testNPE() {
    ExMapper m = new ExMapper();
    ExMapper.logger.setLevel(Level.OFF);
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
    Assertions.assertEquals("1970-01-01T00:00:00.000+00:00",
        c.getContext(null).valueToTree(new Date(0)).asText());
  }

  @Test
  public void testGetMessage() {
    Assertions.assertEquals("NullPointerException",
        ExMapper.getMessage(new NullPointerException()));
    Assertions.assertEquals("NullPointerException",
        ExMapper.getMessage(new Exception(null, new NullPointerException())));
    Assertions.assertEquals("test", ExMapper.getMessage(new Exception("test")));
    Assertions.assertEquals("inner",
        ExMapper.getMessage(new RuntimeException(null, new NullPointerException("inner"))));
    Assertions.assertEquals("Error decrypting. Please re-enter the credentials.",
        ExMapper.getMessage(new EncryptionOperationNotPossibleException()));
  }
}
