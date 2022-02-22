package org.dashjoin.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@Disabled("Requires a local ArangoDB instance running")
@QuarkusTest
public class ArangoDBTest extends DBTest {

  @Override
  protected Object toID(Object o) {
    if (o.equals(1))
      return "EMP/" + o;
    if (o.equals("1"))
      return "EMP/" + o;
    if (o.equals("2"))
      return "EMP/" + o;
    if (o.equals("1000"))
      return "PRJ/" + o;
    if (o instanceof Integer)
      return "PRJ/" + o;
    return o;
  }

  @Override
  protected Object thousand() {
    return 1000l;
  }

  @Override
  protected String idRead() {
    return "_id";
  }

  @Override
  protected String idQuery() {
    return "_id";
  }

  @Override
  @Test
  public void testPath() throws Exception {
    // TODO
  }
}
