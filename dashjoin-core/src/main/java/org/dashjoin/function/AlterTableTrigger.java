package org.dashjoin.function;

import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.Table;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.util.Escape;

/**
 * wraps AlterTable in order to make schema changes appear like a CRUD on the metadata
 */
public class AlterTableTrigger extends AbstractDatabaseTrigger {

  @SuppressWarnings("unchecked")
  @Override
  public Boolean run(Config arg) throws Exception {

    AlterTable at = new AlterTable();
    at.services = services;
    AlterTable.Config x = new AlterTable.Config();
    switch (arg.command) {
      case "create": {
        // form does not set table id
        arg.object.put("ID", arg.object.get("parent") + "/" + arg.object.get("name"));
        x.command = "create";
        x.database = (String) arg.object.get("parent");
        x.table = (String) arg.object.get("name");
        if (arg.object.containsKey("properties")) {
          Map<String, Object> properties = (Map<String, Object>) arg.object.get("properties");
          if (properties.size() == 1) {
            Entry<String, Object> e = properties.entrySet().iterator().next();
            x.newName = e.getKey();
            x.newType = (String) ((Map<String, Object>) e.getValue()).get("type");
          }
        }
        at.run(x);
        ((PojoDatabase) services.getConfig()).metadataCollection(x.database);
        return false;
      }
      case "update": {
        x.command = "rename";
        String[] parts = ((String) arg.search.get("ID")).split("/");
        x.database = parts[0] + "/" + parts[1];
        x.table = Escape.decodeTableOrColumnName(parts[2]);
        x.newName = (String) arg.object.get("name");

        // only call if name changes and is set
        Table old = services.getConfig().getSchema((String) arg.search.get("ID"));
        if (x.newName == null)
          return true;
        if (old.name.equals(x.newName))
          return true;

        at.run(x);
        ((PojoDatabase) services.getConfig()).metadataCollection(x.database);

        // delete data under old name but copy dj-label
        services.getConfig().getConfigDatabase().delete(Table.ofName(arg.table), arg.search);
        if (old.djLabel != null && !arg.object.containsKey("dj-label"))
          arg.object.put("dj-label", old.djLabel);
        if (old.beforeCreate != null && !arg.object.containsKey("before-create"))
          arg.object.put("before-create", old.beforeCreate);
        if (old.afterCreate != null && !arg.object.containsKey("after-create"))
          arg.object.put("after-create", old.afterCreate);
        if (old.beforeUpdate != null && !arg.object.containsKey("before-update"))
          arg.object.put("before-update", old.beforeUpdate);
        if (old.afterUpdate != null && !arg.object.containsKey("after-update"))
          arg.object.put("after-update", old.afterUpdate);
        if (old.beforeDelete != null && !arg.object.containsKey("before-delete"))
          arg.object.put("before-delete", old.beforeDelete);
        if (old.afterDelete != null && !arg.object.containsKey("after-delete"))
          arg.object.put("after-delete", old.afterDelete);

        // before we continue, we need to change the update ID
        arg.search.put("ID", x.database + "/" + x.newName);

        // continue with an update since other props might be set
        return true;
      }
      case "delete": {
        x.command = "delete";
        String[] parts = ((String) arg.search.get("ID")).split("/");
        x.database = parts[0] + "/" + parts[1];
        x.table = Escape.decodeTableOrColumnName(parts[2]);
        at.run(x);
        ((PojoDatabase) services.getConfig()).metadataCollection(x.database);
        // continue with delete since the config DB things need to be deleted also
        return true;
      }
      default:
        throw new Exception("illegal command: " + arg.command);
    }
  }

  @Override
  public String getID() {
    return "alterTableTrigger";
  }
}
