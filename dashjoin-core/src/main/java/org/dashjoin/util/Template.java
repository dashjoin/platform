package org.dashjoin.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * simple version of
 * https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringSubstitutor.html
 * supports parsing ${var} out of template and retains the type for single var templates
 */
public class Template {

  /**
   * regex for ${var}
   */
  static final Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");

  /**
   * given a template (e.g. "Hi ${name}", returns a list of all variables)
   */
  public static List<String> getVariables(String template) {
    return getVariables(template, false);
  }

  /**
   * given a template (e.g. "Hi ${name}", returns a list of all variables)
   */
  public static List<String> getVariables(String template, boolean unique) {
    List<String> res = new ArrayList<String>();
    if (template != null) {
      Matcher m = pattern.matcher(template);
      while (m.find()) {
        String var = m.group();
        var = var.substring(2, var.length() - 1);
        if (!res.contains(var) || !unique)
          res.add(var);
      }
    }
    return res;
  }

  /**
   * replaces the template string's variables with the map values (optionally urlencode the values)
   */
  public static Object replace(String template, Map<String, Object> values) {
    return replace(template, values, false);
  }

  public static Object replace(String template, Map<String, Object> values, boolean urlEncode) {
    List<String> vars = getVariables(template);

    // special case where template is a single var, retain type
    if (vars.size() == 1 && ("${" + vars.get(0) + "}").equals(template))
      return values.get(vars.get(0));

    for (String var : vars) {
      if (values.get(var) != null)
        template = template.replace("${" + var + "}",
            urlEncode ? URLEncoder.encode("" + values.get(var), StandardCharsets.UTF_8)
                : "" + values.get(var));
    }

    return template;
  }

  /**
   * translates the template to a SQL expression
   */
  public static String sql(String key, String template) {

    if (template == null)
      return "cast(" + key + " as VARCHAR(255))";

    List<String> vars = getVariables(template);

    // special case where template is a single var
    if (vars.size() == 1 && ("${" + vars.get(0) + "}").equals(template))
      return "cast(" + vars.get(0) + " as VARCHAR(255))";

    Map<String, Object> values = new HashMap<>();
    for (String var : vars)
      values.put(var, "', " + var + ", '");
    return "concat('" + replace(template, values) + "')";
  }
}
