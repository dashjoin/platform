package org.dashjoin.function;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileSystem;

/**
 * simply load a URL to a string
 */
public class Load extends AbstractFunction<String, String> {

  @Override
  public String getID() {
    return "load";
  }

  @Override
  public String getType() {
    return "read";
  }

  @Override
  public String run(String arg) throws Exception {
    URL url = FileSystem.getUploadURL(arg);
    try (InputStream in = url.openStream()) {
      return IOUtils.toString(in, Charset.defaultCharset());
    }
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }
}
