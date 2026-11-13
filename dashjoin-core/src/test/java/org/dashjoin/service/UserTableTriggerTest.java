package org.dashjoin.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.dashjoin.jsonata.Jsonata;

/**
 * the before-update trigger of config/user keeps non-admins from changing fields they must not
 * set themselves
 */
public class UserTableTriggerTest {

  @SuppressWarnings("unchecked")
  Object run(List<String> roles, Map<String, Object> object) throws Exception {
    Map<String, Object> db;
    try (InputStream in = getClass().getResourceAsStream("/model/dj-database/dj%2Fconfig.json")) {
      db = JSONDatabase.objectMapper.readValue(in, JSONDatabase.tr);
    }
    Map<String, Object> user =
        (Map<String, Object>) ((Map<String, Object>) db.get("tables")).get("user");
    Jsonata expr = Jsonata.jsonata((String) user.get("before-update"));
    expr.registerFunction("djRoles", () -> roles);
    Map<String, Object> context = new HashMap<>();
    context.put("command", "update");
    context.put("table", "user");
    context.put("object", object);
    return expr.evaluate(context);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void userCannotWriteProtectedFields() throws Exception {
    Map<String, Object> res = (Map<String, Object>) run(List.of("authenticated"),
        Map.of("comment", "hi", "name", "x", "firstlogin", "2020-01-01", "sessions", List.of()));
    Map<String, Object> set = (Map<String, Object>) res.get("setObject");
    Assertions.assertEquals("hi", set.get("comment"));
    Assertions.assertNotNull(set.get("lastlogin"));
    Assertions.assertFalse(set.containsKey("name"));
    Assertions.assertFalse(set.containsKey("firstlogin"));
    Assertions.assertFalse(set.containsKey("sessions"));
  }

  @Test
  public void adminIsNotRestricted() throws Exception {
    Assertions.assertEquals(false,
        run(List.of("authenticated", "admin"), Map.of("sessions", List.of())));
  }
}
