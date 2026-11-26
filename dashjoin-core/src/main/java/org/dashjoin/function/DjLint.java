package org.dashjoin.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.function.lint.Linter;
import org.dashjoin.function.lint.Rules;
import org.dashjoin.util.Home;

/**
 * $djLint(): lints the App. The checks are rules (org.dashjoin.function.lint.Rules) run by the
 * walker org.dashjoin.function.lint.Linter; widget and form properties come from widgets.json
 * (/assets/schema/widgets.json), the same declarations the new UI and VS Code use.
 *
 * Arguments (optional): {"format": "json"} returns the findings as a list of {file, path, rule,
 * severity, message} instead of text; {"ignore": ["rule-id", ...]} switches rules off.
 */
public class DjLint extends AbstractFunction<Map<String, Object>, Object> {

  public static List<String> CLIENT_FUNCTIONS =
      Arrays.asList("confirm", "setVariable", "getVariable", "prompt", "alert", "notify", "refresh",
          "reload", "log", "navigate", "clearCache", "log", "translate", "stopSpeech", "speak",
          "sleep", "dialog", "progress", "setActiveStep", "stepForward", "stepBack", "sse");

  /**
   * client functions only the new UI has (dashjoin-newui src/sdk/expression.ts clientActions)
   */
  public static List<String> NEWUI_CLIENT_FUNCTIONS = Arrays.asList("rest", "login", "loginWith",
      "register", "resetPassword", "acceptLegal");

  /**
   * the client functions of both UIs
   */
  public static List<String> allClientFunctions() {
    List<String> res = new ArrayList<>(CLIENT_FUNCTIONS);
    res.addAll(NEWUI_CLIENT_FUNCTIONS);
    return res;
  }

  public static List<String> INPUTS = Rules.INPUTS;

  public static List<String> OPTION_INPUTS = Rules.OPTION_INPUTS;

  public static List<String> FILE_INPUTS = Rules.FILE_INPUTS;

  /**
   * the folders that define what the App references (dj-role) or are linted, in this order
   */
  static final List<String> FOLDERS = Arrays.asList("dj-role", "Dashjoin", "dj-query-catalog",
      "widget", "page", "dj-database", "dj-function", "dj-config", "dj-library");

  @Override
  public Object run(Map<String, Object> arg) throws Exception {
    if (!sc.isUserInRole("admin"))
      throw new Exception("must be admin to lint the App");

    Linter lint = new Linter();
    if (arg != null && arg.get("ignore") instanceof Collection<?> ignore)
      lint.ignore(ignore.stream().map(String::valueOf).toList());
    lint.modelDir = Home.get().getFile("model");
    lint.unitTests = test -> {
      try {
        expressionService.resolve(sc, "$eval($use('unit-test'))($)", test);
        return null;
      } catch (Exception e) {
        return e.getMessage();
      }
    };

    Map<String, List<Map<String, Object>>> records = new LinkedHashMap<>();
    for (String folder : FOLDERS)
      records.put(folder, new ArrayList<>(
          expressionService.getData().all(sc, "config", folder, null, null, null, false, null)));

    lint.defineAll();
    for (Entry<String, List<Map<String, Object>>> e : records.entrySet())
      for (Map<String, Object> item : e.getValue())
        lint.define(e.getKey(), item);

    for (Entry<String, List<Map<String, Object>>> e : records.entrySet())
      if (!e.getKey().equals("dj-role"))
        for (Map<String, Object> item : e.getValue())
          lint.record(e.getKey(), item);

    lint.finish();

    if (arg != null && "json".equals(arg.get("format")))
      return lint.json();
    return lint.text();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<Map<String, Object>> getArgumentClass() {
    return (Class<Map<String, Object>>) (Class<?>) Map.class;
  }
}
