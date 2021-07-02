package org.dashjoin.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FileResourceTest {

  @Test
  public void fileResourceTest() {

    Assert.assertEquals("FileResource must be equal", "file:simple.txt",
        "" + FileResource.of("simple.txt"));
    Assert.assertEquals("FileResource must be equal", "file:path/to/file.txt",
        "" + FileResource.of("file:path/to/file.txt"));
    Assert.assertEquals("FileResource must be equal", "file:/path/to/file.txt",
        "" + FileResource.of("file:/path/to/file.txt"));
    Assert.assertEquals("FileResource must be equal", "file:/path/to/file.txt?start=123&size=456",
        "" + FileResource.of("file:/path/to/file.txt?start=123&size=456"));
    Assert.assertEquals("FileResource must be equal",
        "file:/path/to/file.txt?start=1000000000000&size=2000000000000",
        "" + FileResource.of("file:/path/to/file.txt?start=1000000000000&size=2000000000000"));

    System.err.println(FileResource.of("http://example.org/path/mydata.csv"));
  }

}
