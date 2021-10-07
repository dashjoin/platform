package org.dashjoin.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.common.net.UrlEscapers;

/**
 * escaping helper functions
 */
public class Escape {

  /**
   * escape regular URLs path segments such as "/function/" + e(name), where name comes from the DB
   * and might contain special characters
   */
  public static String e(String s) {
    return UrlEscapers.urlPathSegmentEscaper().escape(s);
  }

  /**
   * application/x-www-form-urlencoded, e.g. "query?parameter=" + fe(name), where name comes from
   * the DB and might contain special characters
   */
  public static String form(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  /**
   * encode file and pathnames (specifically, it is important to encode : which is not encoded using
   * e())
   */
  public static String filename(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
