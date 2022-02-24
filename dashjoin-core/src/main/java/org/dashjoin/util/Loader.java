package org.dashjoin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * data loading utility
 */
public class Loader {

  /**
   * tries opening the location from the classpath, file, and URL
   */
  public static InputStream open(String location) throws IOException {
    InputStream res = Loader.class.getResourceAsStream(location);
    if (res != null)
      return res;
    File file = FileSystem.getUploadFile(location);
    if (file.exists())
      if (!file.isDirectory())
        return new FileInputStream(file);
    return new URL(location).openStream();
  }
}
