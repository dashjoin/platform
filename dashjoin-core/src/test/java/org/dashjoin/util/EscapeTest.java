package org.dashjoin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EscapeTest {

  @Test
  public void testConfigIds() {
    test("a", "a");
    test("#", "#");
    test("\"", "\"");
    test("A#", "A#");
    test("a%b", "a%25b");
    test("a%25b", "a%2525b");
    test("a/b", "a%2Fb");
    test("% /", "%25 %2F");
  }

  void test(String s, String expected) {
    Assertions.assertEquals(Escape.encodeTableOrColumnName(s), expected);
    Assertions.assertEquals(s, Escape.decodeTableOrColumnName(Escape.encodeTableOrColumnName(s)));
  }
}
