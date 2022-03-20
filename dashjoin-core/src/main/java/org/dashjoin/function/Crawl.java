package org.dashjoin.function;

import java.io.InputStream;
import java.net.MalformedURLException;
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
public class Crawl extends AbstractFunction<String, List<String>> {

  @Override
  public List<String> run(String arg) throws Exception {

    List<String> res = new ArrayList<>();
    URL url = FileSystem.getUploadURL(arg);

    try (InputStream in = url.openStream()) {
      String s = IOUtils.toString(in, Charset.defaultCharset());
      if (arg.startsWith("file:") || arg.startsWith("ftp:")) {
        if (!arg.endsWith("/"))
          url = new URL(arg + "/");
        for (String line : s.split("\n")) {
          try {
            res.add(new URL(url, line).toString());
          } catch (MalformedURLException ignore) {
          }
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
  public Class<String> getArgumentClass() {
    return String.class;
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
