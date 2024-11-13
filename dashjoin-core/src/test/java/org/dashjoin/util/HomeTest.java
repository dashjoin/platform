package org.dashjoin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HomeTest {

  @Test
  public void testUrlBranch() {
    // with branch
    Assertions.assertEquals("https://github.com/dashjoin/dashjoin-demo",
        Home.getUrlBranch("https://github.com/dashjoin/dashjoin-demo#5.0").url);
    Assertions.assertEquals("5.0",
        Home.getUrlBranch("https://github.com/dashjoin/dashjoin-demo#5.0").branch);

    // no branch
    Assertions.assertEquals("https://github.com/dashjoin/dashjoin-demo",
        Home.getUrlBranch("https://github.com/dashjoin/dashjoin-demo").url);
    Assertions.assertNull(Home.getUrlBranch("https://github.com/dashjoin/dashjoin-demo").branch);

    // empty string
    Assertions.assertEquals("", Home.getUrlBranch("").url);
    Assertions.assertNull(Home.getUrlBranch("").branch);

    // illegal double ## - ignore and use url 1:1
    Assertions.assertEquals("file:x##f", Home.getUrlBranch("file:x##f").url);
    Assertions.assertNull(Home.getUrlBranch("file:x##f").branch);

    // ends with # - ignore
    Assertions.assertEquals("x#", Home.getUrlBranch("x#").url);
    Assertions.assertNull(Home.getUrlBranch("x#").branch);
  }
}
