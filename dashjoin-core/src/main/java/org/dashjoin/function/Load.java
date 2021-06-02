package org.dashjoin.function;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.MapUtil;

/**
 * simply load a URL to a string
 */
public class Load extends AbstractFunction<Object, Object> {

  @SuppressWarnings("unchecked")
  @Override
  public Object run(Object arg) throws Exception {
    if (arg instanceof String)
      return string((String) arg);
    List<Object> res = new ArrayList<>();
    if (arg != null)
      for (String a : (List<String>) arg)
        res.add(string(a));
    return res;
  }

  protected Object string(String arg) throws Exception {
    URL url = new URL(arg);
    FileSystem.checkFileAccess(url);
    try (InputStream in = url.openStream()) {
      String s = IOUtils.toString(in, Charset.defaultCharset());
      return MapUtil.of("url", arg, "content", s);
    }
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }

  @Override
  public String getID() {
    return "load";
  }

  @Override
  public String getType() {
    return "read";
  }
}
