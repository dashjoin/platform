package org.dashjoin.function;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileSystem;

/**
 * given a URL, return a list of URLs "contained" in the URL by following all hyperlinks / files in
 * the file / ftp folder
 */
public class Crawl extends AbstractFunction<Object, List<String>> {

  @SuppressWarnings("unchecked")
  @Override
  public List<String> run(Object _arg) throws Exception {

    if (_arg instanceof List<?>) {
      List<String> res = new ArrayList<>();
      for (String s : (List<String>) _arg)
        res.addAll(run(s));
      return res;
    }

    String arg = (String) _arg;

    List<String> res = new ArrayList<>();
    URL url = FileSystem.getUploadURL(arg);

    try (InputStream in = url.openStream()) {
      String s = IOUtils.toString(in, Charset.defaultCharset());
      if (arg.startsWith("file:") || arg.startsWith("ftp:")) {
        if (!arg.endsWith("/"))
          url = new URL(arg + "/");
        for (String line : s.split("\n")) {
          res.add(new URL(url, line).toString());
          if (readOnly && res.size() == 10)
            break;
        }
      } else {
        Matcher matcher = Pattern.compile("href=\\\".*?\\\"").matcher(s);
        while (matcher.find()) {
          String href = s.substring(matcher.start(), matcher.end());
          href = href.substring(href.indexOf('"') + 1, href.lastIndexOf('"'));
          res.add(new URL(url, href).toString());
          if (readOnly && res.size() == 10)
            break;
        }
      }
    }
    return res;
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }

  @Override
  public String getID() {
    return "crawl";
  }

  @Override
  public String getType() {
    return "read";
  }
}
