package org.dashjoin.function.lint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * the widgets and their properties, read from widgets.json - the same declarations the new UI's
 * layout editor and the JSON schemas for VS Code use (dashjoin-newui: yarn schema). Served as
 * /assets/schema/widgets.json
 */
public class WidgetCatalog {

  public static final String RESOURCE = "/META-INF/resources/assets/schema/widgets.json";

  /**
   * a widget's properties (the common ones included; null: any property, e.g. a config/widget),
   * the deprecation messages of its deprecated properties (the common ones included), the
   * properties that name a query or a database, the properties that hold a form and the
   * deprecation message of the widget itself (null if not deprecated)
   */
  record Widget(Set<String> properties, Map<String, String> deprecations,
      Map<String, String> references, Set<String> forms, String deprecated) {
  }

  static final Widget ANY = new Widget(null, Map.of(), Map.of(), Set.of(), null);

  /**
   * the message to report for a declaration marked "deprecated": true or carrying a
   * "deprecationMessage" (VS Code's keyword): that message, else the description (which says what
   * to use instead), null if the declaration is not deprecated
   */
  static String deprecation(JsonNode declaration) {
    JsonNode message = declaration.get("deprecationMessage");
    if (message != null)
      return message.asText();
    if (!declaration.path("deprecated").asBoolean())
      return null;
    JsonNode description = declaration.get("description");
    return description == null ? "no longer supported" : description.asText();
  }

  /** adds the deprecation messages of the deprecated properties to the map */
  static void deprecations(JsonNode properties, Map<String, String> map) {
    for (Entry<String, JsonNode> p : properties.properties()) {
      String message = deprecation(p.getValue());
      if (message != null)
        map.put(p.getKey(), message);
    }
  }

  private static WidgetCatalog builtIn;

  /** the built-in widgets (widgets.json from the classpath) */
  public static synchronized WidgetCatalog get() {
    if (builtIn == null)
      try (InputStream in = WidgetCatalog.class.getResourceAsStream(RESOURCE)) {
        if (in == null)
          throw new IllegalStateException(RESOURCE + " not found on the classpath");
        builtIn = new WidgetCatalog(new ObjectMapper().readTree(in));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    return builtIn;
  }

  final Map<String, Widget> widgets;

  /** the keys a form or form field may have (widgets.json form) */
  final Set<String> formKeys;

  /**
   * the inputs of a form field: the default input of each type and the "widget" values of
   * widgets.json form
   */
  final List<String> inputs;

  /** the default input of each field type, also accepted as "widget" */
  static final List<String> TYPE_INPUTS = List.of("boolean", "string", "number");

  /** the deprecation messages of the deprecated keys of a form field (widgets.json form) */
  final Map<String, String> formDeprecations;

  WidgetCatalog(Map<String, Widget> widgets, Set<String> formKeys,
      Map<String, String> formDeprecations, List<String> inputs) {
    this.widgets = widgets;
    this.formKeys = formKeys;
    this.formDeprecations = formDeprecations;
    this.inputs = inputs;
  }

  public WidgetCatalog(JsonNode source) {
    Set<String> common = new HashSet<>();
    source.get("common").fieldNames().forEachRemaining(common::add);
    Map<String, String> commonDeprecations = new HashMap<>();
    deprecations(source.get("common"), commonDeprecations);

    widgets = new HashMap<>();
    for (Entry<String, JsonNode> w : source.get("widgets").properties()) {
      Set<String> props = new HashSet<>(common);
      Map<String, String> deprecations = new HashMap<>(commonDeprecations);
      Map<String, String> references = new HashMap<>();
      Set<String> forms = new HashSet<>();
      JsonNode properties = w.getValue().get("properties");
      if (properties != null)
        for (Entry<String, JsonNode> p : properties.properties()) {
          props.add(p.getKey());
          String ref = p.getValue().has("$ref") ? p.getValue().get("$ref").asText() : "";
          if (ref.equals("#/$defs/query"))
            references.put(p.getKey(), "query");
          if (ref.equals("#/$defs/database"))
            references.put(p.getKey(), "database");
          if (ref.equals("#/$defs/form"))
            forms.add(p.getKey());
        }
      if (properties != null)
        deprecations(properties, deprecations);
      widgets.put(w.getKey(),
          new Widget(props, deprecations, references, forms, deprecation(w.getValue())));
    }

    formKeys = new HashSet<>();
    JsonNode form = source.get("form");
    form.get("properties").fieldNames().forEachRemaining(formKeys::add);
    JsonNode field = form.at("/properties/properties/additionalProperties/properties");
    field.fieldNames().forEachRemaining(formKeys::add);
    formDeprecations = new HashMap<>();
    deprecations(field, formDeprecations);

    List<String> inputs = new ArrayList<>(TYPE_INPUTS);
    for (JsonNode w : field.at("/widget/enum"))
      inputs.add(w.asText());
    this.inputs = Collections.unmodifiableList(inputs);
  }

  /**
   * this catalog plus the app's config/widget records, which accept any property
   */
  public WidgetCatalog withAppWidgets(Collection<String> ids) {
    Map<String, Widget> all = new HashMap<>(widgets);
    for (String id : ids)
      all.putIfAbsent(id, ANY);
    return new WidgetCatalog(all, formKeys, formDeprecations, inputs);
  }

  public boolean known(String widget) {
    return widgets.containsKey(widget);
  }

  public Set<String> ids() {
    return Collections.unmodifiableSet(widgets.keySet());
  }

  /** the widget's properties, null if it accepts any (or is unknown) */
  public Set<String> properties(String widget) {
    Widget w = widgets.get(widget);
    return w == null ? null : w.properties();
  }

  /** the message to report for a deprecated property, null if it is not deprecated */
  public String deprecation(String widget, String property) {
    Widget w = widgets.get(widget);
    return w == null ? null : w.deprecations().get(property);
  }

  /** the message to report for a deprecated widget, null if it is not deprecated */
  public String deprecation(String widget) {
    Widget w = widgets.get(widget);
    return w == null ? null : w.deprecated();
  }

  /** the message to report for a deprecated form field key, null if it is not deprecated */
  public String formDeprecation(String key) {
    return formDeprecations.get(key);
  }

  /** query or database if the property names one, else null */
  public String reference(String widget, String property) {
    Widget w = widgets.get(widget);
    return w == null ? null : w.references().get(property);
  }

  /** the property holds a form (button / variable / edit / create schema) */
  public boolean isForm(String widget, String property) {
    Widget w = widgets.get(widget);
    return w != null && w.forms().contains(property);
  }

  /** the inputs of a form field ("widget") */
  public List<String> inputs() {
    return inputs;
  }

  public boolean isFormKey(String key) {
    return formKeys.contains(key);
  }
}
