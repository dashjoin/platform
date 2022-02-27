package org.dashjoin.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileSystem;
import lombok.extern.java.Log;

/**
 * function to download a url (or list of urls) to the local upload folder
 */
@Log
public class Download extends Load {

  @Override
  public String run(String arg) throws Exception {
    URL url = FileSystem.getUploadURL(arg);
    String filename = url.getPath();
    if (filename.isEmpty())
      filename = filename + "/";
    if (filename.endsWith("/"))
      filename = filename + "_";
    File file = FileSystem.getUploadFile("upload/" + url.getHost() + "/" + filename);
    if (file.exists())
      try (InputStream in = new FileInputStream(file)) {
        return IOUtils.toString(in, Charset.defaultCharset());
      }
    else {
      log.info("downloading: " + url);
      String s = super.run(arg);
      FileUtils.writeStringToFile(file, s, Charset.defaultCharset());
      return s;
    }
  }

  @Override
  public String getID() {
    return "download";
  }
}
