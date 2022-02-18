package org.dashjoin.function;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.dashjoin.util.FileSystem;
import lombok.extern.java.Log;

/**
 * function to download a url (or list of urls) to the local upload folder
 */
@Log
public class Download extends AbstractFunction<String, Void> {

  @Override
  public Void run(String arg) throws Exception {
    URL url = FileSystem.getUploadURL(arg);
    log.info("downloading: " + url);
    try (InputStream in = url.openStream()) {
      String filename = url.getPath().isEmpty() ? url.getHost() : new File(url.getPath()).getName();
      FileUtils.copyInputStreamToFile(in, new File("upload/" + filename));
      return null;
    }
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }

  @Override
  public String getID() {
    return "download";
  }

  @Override
  public String getType() {
    return "write";
  }
}
