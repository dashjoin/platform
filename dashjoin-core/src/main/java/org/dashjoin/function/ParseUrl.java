package org.dashjoin.function;

import java.net.MalformedURLException;
import java.net.URL;

public class ParseUrl extends AbstractFunction<String, ParseUrl.Result> {

  public static class Result {
    public String authority;
    public int defaultPort;
    public String file;
    public String host;
    public String path;
    public int port;
    public String protocol;
    public String query;
    public String userInfo;
  }

  @Override
  public Result run(String arg) {
    if (arg == null)
      throw new IllegalArgumentException("Syntax: $parseURL(url)");
    try {
      URL url = new URL(arg.replace("/#/", "/"));
      Result res = new Result();
      res.authority = url.getAuthority();
      res.defaultPort = url.getDefaultPort();
      res.file = url.getFile();
      res.host = url.getHost();
      res.path = url.getPath();
      res.port = url.getPort();
      res.protocol = url.getProtocol();
      res.query = url.getQuery();
      res.userInfo = url.getUserInfo();
      return res;
    } catch (MalformedURLException e) {
      return null;
    }
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }
}
