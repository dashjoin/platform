package org.dashjoin.function.lint;

import java.util.Map;
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * what the walker hands to a rule: the kind, where it is (file and JSON pointer) and the JSON. The
 * other fields are set for some kinds only
 */
public class Node {

  public final Kind kind;

  /** the model file, e.g. model/page/Info.json */
  public final String file;

  /** JSON pointer inside the file */
  public final String path;

  /** the node itself (RECORD: the record, LAYOUT: the widget, FORM: the form or field) */
  public final JsonNode value;

  /** RECORD: the record's folder (page, widget, dj-database, ...) and the record */
  public String folder;
  public Map<String, Object> record;

  /** LAYOUT: the widget id; REFERENCE from a layout: the widget that holds it */
  public String widget;

  /** FORM: an edit / create form (table schema), which carries the column metadata */
  public boolean tableSchema;

  /** EXPRESSION: the field holding it, its text, the compiled expression or the parse error */
  public String field;
  public String expression;
  public Jsonata compiled;
  public JException parseError;

  /** EXPRESSION: runs in the backend only (dj-function), the client functions are not defined */
  public boolean backendOnly;

  /**
   * REFERENCE: what is referenced (query, database, function, library, role), its ID and where
   * (e.g. "widget table", "$call", "readRoles")
   */
  public String refKind;
  public String id;
  public String where;

  /** METADATA: the database record, the parent's ID and the key in the parent's map */
  public Map<String, Object> root;
  public Map<String, Object> map;
  public Object parentId;
  public String key;

  public Node(Kind kind, String file, String path, JsonNode value) {
    this.kind = kind;
    this.file = file;
    this.path = path;
    this.value = value;
  }

  /** the text of a property of value, null if missing */
  public String text(String property) {
    JsonNode v = value == null ? null : value.get(property);
    return v == null ? null : v.asText();
  }

  public boolean has(String property) {
    return value != null && value.get(property) != null;
  }
}
