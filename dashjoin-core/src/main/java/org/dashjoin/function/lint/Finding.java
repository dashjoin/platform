package org.dashjoin.function.lint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * one lint result
 *
 * @param file the model file, e.g. model/page/Info.json
 * @param path JSON pointer inside the file, e.g. /layout/children/2 ("" for the whole file)
 * @param rule the rule's id, e.g. unknown-property
 */
public record Finding(String file, String path, String rule, Severity severity, String message) {

  /**
   * one line of the text output: file, severity (errors have none), message and location
   */
  public String text() {
    return file + ": " + (severity == Severity.ERROR ? "" : severity.name().toLowerCase() + ": ")
        + message + (path == null || path.isEmpty() ? "" : " (at " + path + ")");
  }

  public Map<String, Object> toMap() {
    Map<String, Object> res = new LinkedHashMap<>();
    res.put("file", file);
    res.put("path", path);
    res.put("rule", rule);
    res.put("severity", severity.name().toLowerCase());
    res.put("message", message);
    return res;
  }

  @Override
  public String toString() {
    return text();
  }
}
