package org.dashjoin.function;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.dashjoin.util.FileSystem;
import lombok.extern.java.Log;

@Log
public class Download extends AbstractMultiInputFunction {

  @Override
  public Object single(Object arg) throws Exception {
    URL url = new URL((String) arg);
    FileSystem.checkFileAccess(url);
    log.info("downloading: " + url);
    try (InputStream in = url.openStream()) {
      String filename = url.getPath().isEmpty() ? url.getHost() : new File(url.getPath()).getName();
      FileUtils.copyInputStreamToFile(in, new File("upload/" + filename));
      return null;
    }
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }

  @Override
  public String getID() {
    return "download";
  }

  @Override
  public String getType() {
    return "write";
  }

  @Override
  public String inputField() {
    return "url";
  }

  @Override
  public String outputField() {
    return null;
  }
}
