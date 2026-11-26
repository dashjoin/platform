package org.dashjoin.function.lint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
   * the deprecation messages of properties to report, the properties that name a query or a
   * database and the properties that hold a form
   */
  record Widget(Set<String> properties, Map<String, String> deprecations,
      Map<String, String> references, Set<String> forms) {
  }

  static final Widget ANY = new Widget(null, Map.of(), Map.of(), Set.of());

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

  WidgetCatalog(Map<String, Widget> widgets, Set<String> formKeys) {
    this.widgets = widgets;
    this.formKeys = formKeys;
  }

  public WidgetCatalog(JsonNode source) {
    Set<String> common = new HashSet<>();
    source.get("common").fieldNames().forEachRemaining(common::add);

    widgets = new HashMap<>();
    for (Entry<String, JsonNode> w : source.get("widgets").properties()) {
      Set<String> props = new HashSet<>(common);
      Map<String, String> deprecations = new HashMap<>();
      Map<String, String> references = new HashMap<>();
      Set<String> forms = new HashSet<>();
      JsonNode properties = w.getValue().get("properties");
      if (properties != null)
        for (Entry<String, JsonNode> p : properties.properties()) {
          props.add(p.getKey());
          JsonNode message = p.getValue().get("deprecationMessage");
          if (message != null)
            deprecations.put(p.getKey(), message.asText());
          String ref = p.getValue().has("$ref") ? p.getValue().get("$ref").asText() : "";
          if (ref.equals("#/$defs/query"))
            references.put(p.getKey(), "query");
          if (ref.equals("#/$defs/database"))
            references.put(p.getKey(), "database");
          if (ref.equals("#/$defs/form"))
            forms.add(p.getKey());
        }
      widgets.put(w.getKey(), new Widget(props, deprecations, references, forms));
    }

    formKeys = new HashSet<>();
    JsonNode form = source.get("form");
    form.get("properties").fieldNames().forEachRemaining(formKeys::add);
    form.at("/properties/properties/additionalProperties/properties").fieldNames()
        .forEachRemaining(formKeys::add);
  }

  /**
   * this catalog plus the app's config/widget records, which accept any property
   */
  public WidgetCatalog withAppWidgets(Collection<String> ids) {
    Map<String, Widget> all = new HashMap<>(widgets);
    for (String id : ids)
      all.putIfAbsent(id, ANY);
    return new WidgetCatalog(all, formKeys);
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

  /** the message to report for a property no longer supported, null if none */
  public String deprecation(String widget, String property) {
    Widget w = widgets.get(widget);
    return w == null ? null : w.deprecations().get(property);
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

  public boolean isFormKey(String key) {
    return formKeys.contains(key);
  }
}
