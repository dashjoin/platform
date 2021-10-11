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

  /**
   * in the config DB, table and column IDs are concatenated: dj/database/table/column. Therefore,
   * this method URLEncodes / and %
   */
  public static String encodeTableOrColumnName(String s) {
    return s.replaceAll("%", "%25").replaceAll("/", "%2F");
  }

  /**
   * decode version of above
   */
  public static String decodeTableOrColumnName(String s) {
    return s.replaceAll("%2F", "/").replaceAll("%25", "%");
  }

  /**
   * Table IDs in the config database look like dj/northwind/EMP replaces id.split('/') since the
   * table name might have / in it
   */
  public static String[] parseTableID(String id) {
    if (id.split("/").length == 3) {
      String[] res = id.split("/");
      res[2] = decodeTableOrColumnName(res[2]);
      return res;
    }
    throw new IllegalArgumentException("illegal table id: " + id);
  }

  /**
   * Column IDs in the config database look like dj/northwind/EMP/LAST_NAME replaces id.split('/')
   * since the table name might have / in it
   */
  public static String[] parseColumnID(String id) {
    if (id.split("/").length == 4) {
      String[] res = id.split("/");
      res[2] = decodeTableOrColumnName(res[2]);
      res[3] = decodeTableOrColumnName(res[3]);
      return res;
    }
    throw new IllegalArgumentException("illegal column id: " + id);
  }
}
