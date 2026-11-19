package org.dashjoin.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;
import com.google.common.base.CharMatcher;

/**
 * $erDiagram(database?, format?) generates an entity relationship diagram of the given database
 * (all databases if omitted). format is dbml (default, for https://dbdiagram.io/d) or mermaid
 * (https://mermaid.js.org/syntax/entityRelationshipDiagram.html)
 */
@SuppressWarnings("rawtypes")
public class ERDiagram extends AbstractVarArgFunction<String> {

  @Override
  public String run(List args) throws Exception {
    String database = arg(args, 0);
    String format = arg(args, 1);
    List<AbstractDatabase> dbs = databases(database);
    if (format == null || format.equals("dbml"))
      return dbml(dbs);
    if (format.equals("mermaid"))
      return mermaid(dbs);
    throw new IllegalArgumentException(
        "$erDiagram: unknown format " + format + ". Supported formats: dbml, mermaid");
  }

  static String arg(List args, int index) {
    Object value = args != null && index < args.size() ? args.get(index) : null;
    if (value == null)
      return null;
    if (!(value instanceof String))
      throw new IllegalArgumentException(
          "Syntax: $erDiagram(database?, format?). Argument " + (index + 1) + " must be a string");
    return (String) value;
  }

  List<AbstractDatabase> databases(String database) throws Exception {
    Collection<AbstractDatabase> all = database == null ? this.services.getConfig().getDatabases()
        : Arrays.asList(this.services.getConfig().getDatabase("dj/" + database));
    return all.stream().filter(db -> !db.name.equals("config")).collect(Collectors.toList());
  }

  /**
   * Table users { id integer [primary key] username varchar }
   */
  String dbml(List<AbstractDatabase> dbs) {
    StringBuffer res = new StringBuffer();
    for (AbstractDatabase db : dbs) {
      for (Table table : db.tables.values()) {
        String comp = comp(table);
        res.append("Table " + q(table.name) + "{\n");
        for (Property prop : table.properties.values()) {
          res.append("  " + q(prop.name) + " " + t(prop.dbType != null ? prop.dbType : prop.type));
          if (prop.pkpos != null && prop.pkpos == 0 && comp == null)
            res.append(" [primary key]");
          if (prop.items != null)
            if (prop.items.ref != null)
              res.append(" [ref: > " + q(Escape.parseColumnID(prop.items.ref)[2]) + '.'
                  + Escape.parseColumnID(prop.items.ref)[3] + "]");
          if (prop.ref != null)
            res.append(" [ref: > " + q(Escape.parseColumnID(prop.ref)[2]) + '.'
                + Escape.parseColumnID(prop.ref)[3] + "]");
          if (prop.title != null || prop.comment != null)
            res.append(" [note: " + note(prop.title, prop.comment) + "]");
          res.append("\n");
        }
        if (table.title != null || table.comment != null)
          res.append("  Note: " + note(table.title, table.comment) + "\n");
        if (comp != null)
          res.append("  " + comp + "\n");
        res.append("}\n\n");
      }
    }
    return res.toString().replace("] [", ", ");
  }

  /**
   * erDiagram CUSTOMERS { VARCHAR ID PK } ORDERS { VARCHAR CUSTOMER_ID FK } ORDERS }o--||
   * CUSTOMERS : "CUSTOMER_ID"
   */
  String mermaid(List<AbstractDatabase> dbs) {
    // table ID (dj/db/table) -> unique mermaid entity name
    Map<String, String> entities = new LinkedHashMap<>();
    Set<String> used = new HashSet<>();
    for (AbstractDatabase db : dbs)
      for (Table table : db.tables.values()) {
        String base = word(table.name, "_");
        String name = base;
        for (int i = 2; used.contains(name.toLowerCase()); i++)
          name = base + "_" + i;
        used.add(name.toLowerCase());
        entities.put(tableId(db.name, table.name), name);
      }

    StringBuffer res = new StringBuffer("erDiagram\n");
    StringBuffer rels = new StringBuffer();
    for (AbstractDatabase db : dbs) {
      for (Table table : db.tables.values()) {
        String entity = entities.get(tableId(db.name, table.name));
        res.append("  " + entity);
        if (!entity.equals(table.name))
          res.append("[" + quote(table.name) + "]");
        res.append(" {\n");
        for (Property prop : table.properties.values()) {
          String type = prop.dbType != null ? prop.dbType : prop.type;
          String name = word(prop.name, "_");
          res.append("    " + word(type, "unknown") + " " + name);

          String ref = prop.ref != null ? prop.ref
              : prop.items != null && prop.items.ref != null ? prop.items.ref : null;
          if (prop.pkpos != null && ref != null)
            res.append(" PK, FK");
          else if (prop.pkpos != null)
            res.append(" PK");
          else if (ref != null)
            res.append(" FK");

          // keep the original column name if it had to be changed
          String comment = name.equals(prop.name) ? null : prop.name;
          if (prop.title != null || prop.comment != null)
            comment = (comment == null ? "" : comment + ": ") + text(prop.title, prop.comment);
          if (comment != null)
            res.append(" " + quote(comment));
          res.append("\n");

          if (ref != null) {
            String[] col = Escape.parseColumnID(ref);
            String target = entities.get(tableId(col[1], col[2]));
            if (target != null) {
              // array of foreign keys: many to many, otherwise many to one (exactly one if the
              // column is required)
              String card = prop.ref == null ? "}o--o{"
                  : Boolean.TRUE.equals(prop.required) ? "}o--||" : "}o--o|";
              rels.append("  " + entity + " " + card + " " + target + " : " + quote(prop.name)
                  + "\n");
            }
          }
        }
        res.append("  }\n");
      }
    }
    return res.append(rels).toString();
  }

  static String tableId(String db, String table) {
    return db + "/" + table;
  }

  /**
   * mermaid tokens (entity, attribute and type names) must match [A-Za-z_][A-Za-z0-9_-()[]]*
   */
  static String word(String s, String fallback) {
    if (s == null || s.isEmpty())
      return fallback;
    StringBuilder b = new StringBuilder();
    for (char c : s.toCharArray())
      b.append((c < 128 && (Character.isLetterOrDigit(c) || "_-()[]".indexOf(c) >= 0)) ? c : '_');
    if (!Character.isLetter(b.charAt(0)) && b.charAt(0) != '_')
      b.insert(0, '_');
    return b.toString();
  }

  /**
   * mermaid strings cannot contain double quotes or line breaks
   */
  static String quote(String s) {
    return '"' + s.replace('"', '\'').replaceAll("[\\r\\n]+", " ") + '"';
  }

  /**
   * title and comment, separated by a space if both are set
   */
  static String text(String title, String comment) {
    return title == null ? comment : comment == null ? title : title + ' ' + comment;
  }

  @Override
  public String getID() {
    return "erDiagram";
  }

  @Override
  public String getType() {
    return "read";
  }

  /**
   * DBML string with title and comment, separated by a space if both are set
   */
  String note(String title, String comment) {
    String s = text(title, comment);
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
  }

  String q(String s) {
    if (s == null)
      return null;
    if (StringUtils.isAlphanumeric(s.replace('_', 'a')) && CharMatcher.ascii().matchesAllOf(s))
      return s;
    else
      return '"' + s + '"';
  }

  String t(String s) {
    if (s == null)
      return null;
    return s.replaceAll(" ", "_");
  }

  String comp(Table table) {
    // composite key columns must be listed in key order (pkpos), which is the order $read /
    // $delete expect
    List<String> res = table.properties.values().stream().filter(prop -> prop.pkpos != null)
        .sorted(Comparator.comparing(prop -> prop.pkpos)).map(prop -> prop.name)
        .collect(Collectors.toList());
    if (res.size() < 2)
      return null;
    return "indexes {(" + String.join(", ", res) + ") [pk]}";
  }
}
