package org.dashjoin.function.lint;

/**
 * the kinds of nodes the walker (Linter) hands to the rules
 */
public enum Kind {
  /** a config record (page, widget, query, database, function, setting, library) */
  RECORD,
  /** a database, table or column of a dj-database record */
  METADATA,
  /** a widget in a layout (page, config/widget, table and record layouts) */
  LAYOUT,
  /** a form (schema of button, variable, edit, create) or one of its fields */
  FORM,
  /** a JSONata expression */
  EXPRESSION,
  /** a reference to a query, database, function, library or role */
  REFERENCE,
  /** the whole app, once all records are walked (unused items, orphaned files) */
  APP
}
