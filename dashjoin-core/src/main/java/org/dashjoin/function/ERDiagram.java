package org.dashjoin.function;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;
import com.google.common.base.CharMatcher;

/**
 * generate ER diagram for https://dbdiagram.io/d
 */
public class ERDiagram extends AbstractFunction<String, String> {

  /**
   * Table users { id integer [primary key] username varchar }
   */
  @Override
  public String run(String arg) throws Exception {
    StringBuffer res = new StringBuffer();
    for (AbstractDatabase db : arg == null ? this.services.getConfig().getDatabases()
        : Arrays.asList(this.services.getConfig().getDatabase("dj/" + arg))) {
      if (db.name.equals("config"))
        continue;
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

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
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
    String s = title == null ? comment : comment == null ? title : title + ' ' + comment;
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
