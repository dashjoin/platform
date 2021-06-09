package org.dashjoin.util;

import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class FileSystemTest {

  @Test
  public void testCheckFileAccess() throws Exception {
    // only allow access to the upload folder
    Assertions.assertThrows(RuntimeException.class, () -> {
      FileSystem.checkFileAccess(new URL("file:."));
    });

    FileSystem.checkFileAccess(new File("upload"));
    FileSystem.checkFileAccess(new URL("file:upload"));
  }

  @Test
  public void testUploadUrl() throws Exception {
    System.err.println(FileSystem.getUploadURL("file:upload/test.txt"));

    // Other proto than file is OK
    System.err.println(FileSystem.getUploadURL("http://example.org/download/test.txt"));

    Assertions.assertThrows(RuntimeException.class, () -> {
      System.err.println(FileSystem.getUploadURL("file:test.txt"));
    });

    Assertions.assertThrows(RuntimeException.class, () -> {
      System.err.println(FileSystem.getUploadURL("file:../upload/test.txt"));
    });

    Assertions.assertThrows(RuntimeException.class, () -> {
      System.err.println(FileSystem.getUploadURL("file:/../upload/test.txt"));
    });

  }
}
