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

    String upload = Home.get().getFile("upload").getCanonicalPath();
    // File("").getCanonicalPath() + File.separator + "upload";
    String test = file.getCanonicalPath();

    if (!test.startsWith(upload))
      throw new RuntimeException("You do not have access to the folder '" + file
          + "'. Choose a file in the upload folder.");
  }

  public static File getUploadFile(String name) throws IOException {
    return Home.get().getFile(name);
  }

  public static URL getUploadURL(String name) throws IOException {
    URL url = new URL(name);
    if (url.getProtocol().equals("file")) {
      url = new URL("file:" + Home.get().getFile(url.getPath()).getCanonicalPath());
      System.err.println("upload url " + url);
      checkFileAccess(new File(url.getPath()));
    }
    return url;
  }
}
