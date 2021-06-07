package org.dashjoin.util;

import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
