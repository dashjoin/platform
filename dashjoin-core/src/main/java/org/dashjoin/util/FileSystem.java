package org.dashjoin.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.dashjoin.service.Services;

/**
 * makes sure no file access is allowed except for the upload folder
 */
public class FileSystem {

  /**
   * checks file URLs, ignores the other protocols
   */
  public static void checkFileAccess(URL url) throws IOException {
    if (url.getProtocol().equals("file")) {
      checkFileAccess(Home.get().getFile(url.getPath()));
    }
  }

  /**
   * checks file objects, . and .. are normalized
   */
  public static void checkFileAccess(File file) throws IOException {
    checkFileAccess(file, "upload");
  }

  /**
   * checks file objects, . and .. are normalized
   */
  public static void checkFileAccess(File file, String folder) throws IOException {

    String upload = Home.get().getFile(folder).getCanonicalPath();
    // File("").getCanonicalPath() + File.separator + "upload";
    String test = file.getCanonicalPath();

    if (!test.startsWith(upload))
      throw new RuntimeException("You do not have access to the folder '" + file
          + "'. Choose a file in the " + folder + " folder.");
  }

  /**
   * given a file path, returns the File object. Takes the DJ Home dir into account if path is
   * relative. Throws a RuntimeException if the path is not in the upload folder
   */
  public static File getUploadFile(String name) throws IOException {
    File res = Home.get().getFile(name);
    checkFileAccess(res);
    return res;
  }

  /**
   * given a URL, returns the URL object. If the URL is a file URL, works like getUploadFile
   */
  public static URL getUploadURL(String name) throws IOException {
    URL url = new URL(name);
    if (url.getProtocol().equals("file")) {
      url = new URL("file:" + Home.get().getFile(url.getPath()).getCanonicalPath()
          + (url.getQuery() != null ? "?" + url.getQuery() : ""));
      checkFileAccess(new File(url.getPath()));
    }
    return url;
  }

  static File getFile(Services services, String file) {
    return new File(services.getTenantHome(), file);
  }

  public static String getJdbcUrl(Services services, String url) throws IOException {

    if (url.toLowerCase().startsWith("jdbc:sqlite:")) {
      String file = url.substring("jdbc:sqlite:".length());
      checkSQLiteAccess(getFile(services, file));
      return "jdbc:sqlite:" + getFile(services, file);
    }
    if (url.toLowerCase().startsWith("jdbc:h2:") && !url.toLowerCase().startsWith("jdbc:h2:mem:")) {
      String file = url.substring("jdbc:h2:".length());
      checkSQLiteAccess(getFile(services, file));
      return "jdbc:h2:" + getFile(services, file);
    }
    if (url.toLowerCase().startsWith("jdbc:ucanaccess://")) {
      String file = url.substring("jdbc:ucanaccess://".length());
      checkSQLiteAccess(getFile(services, file));
      return "jdbc:ucanaccess://" + getFile(services, file);
    }
    return url;
  }

  /**
   * checks file objects, . and .. are normalized
   */
  public static void checkSQLiteAccess(File file) throws IOException {

    String tmp = new File("/tmp").getCanonicalPath();
    String root = Home.get().getFile(".").getCanonicalPath();
    String test = file.getCanonicalPath();
    if (!test.startsWith(root) && !test.startsWith(tmp))
      throw new RuntimeException("You do not have access to the folder '" + file
          + "'. Choose a file in the application folder.");
  }

}
