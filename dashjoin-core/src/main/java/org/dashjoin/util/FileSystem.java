package org.dashjoin.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * makes sure no file access is allowed except for the upload folder
 */
public class FileSystem {

  /**
   * checks file URLs, ignores the other protocols
   */
  public static void checkFileAccess(URL url) throws IOException {
    if (url.getProtocol().equals("file")) {
      checkFileAccess(new File(url.getPath()));
    }
  }

  /**
   * checks file objects, . and .. are normalized
   */
  public static void checkFileAccess(File file) throws IOException {

    String upload = new File("").getCanonicalPath() + File.separator + "upload";
    String test = file.getCanonicalPath();

    if (!test.startsWith(upload))
      throw new RuntimeException("You do not have access to the folder '" + file
          + "'. Choose a file in the upload folder.");
  }
}
