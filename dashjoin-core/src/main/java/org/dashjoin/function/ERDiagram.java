package org.dashjoin.function;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;

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
        res.append("Table " + table.name + "{\n");
        for (Property prop : table.properties.values()) {
          res.append("  " + prop.name + " " + t(prop.dbType));
          if (prop.pkpos != null && prop.pkpos == 0 && comp == null)
            res.append(" [primary key]");
          if (prop.ref != null)
            res.append(" [ref: > " + Escape.parseColumnID(prop.ref)[2] + '.'
                + Escape.parseColumnID(prop.ref)[3] + "]");
          if (prop.title != null || prop.description != null)
            res.append(" [note: '" + s(prop.title) + ' ' + s(prop.comment) + "']");
          res.append("\n");
        }
        if (table.title != null || table.comment != null)
          res.append("  Note: '" + s(table.title) + ' ' + s(table.comment) + "'");
        if (comp != null)
          res.append("  " + comp + "\n");
        res.append("}\n\n");
      }
    }
    return res.toString();
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

  String s(String s) {
    if (s == null)
      return "";
    else
      return s;
  }

  String t(String s) {
    return s.replaceAll(" ", "_");
  }

  String comp(Table table) {
    Set<String> res = new HashSet<>();
    for (Property prop : table.properties.values())
      if (prop.pkpos != null)
        res.add(prop.name);
    if (res.size() < 2)
      return null;
    return "indexes {(" + String.join(", ", res) + ") [pk]}";
  }
}