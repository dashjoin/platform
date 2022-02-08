package org.dashjoin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FileResourceTest {

  @Test
  public void fileResourceTest() {

    Assertions.assertEquals("file:simple.txt", "" + FileResource.of("simple.txt"),
        "FileResource must be equal");
    Assertions.assertEquals("file:path/to/file.txt", "" + FileResource.of("file:path/to/file.txt"),
        "FileResource must be equal");
    Assertions.assertEquals("file:/path/to/file.txt",
        "" + FileResource.of("file:/path/to/file.txt"), "FileResource must be equal");
    Assertions.assertEquals("file:/path/to/file.txt?start=123&size=456",
        "" + FileResource.of("file:/path/to/file.txt?start=123&size=456"),
        "FileResource must be equal");
    Assertions.assertEquals("file:/path/to/file.txt?start=1000000000000&size=2000000000000",
        "" + FileResource.of("file:/path/to/file.txt?start=1000000000000&size=2000000000000"),
        "FileResource must be equal");

    System.err.println(FileResource.of("http://example.org/path/mydata.csv"));
  }

}
