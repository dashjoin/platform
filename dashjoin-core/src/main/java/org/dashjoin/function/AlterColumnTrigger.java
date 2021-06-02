package org.dashjoin.function;

import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.PojoDatabase;

/**
 * wraps AlterColumn in order to make schema changes appear like a CRUD on the metadata
 */
public class AlterColumnTrigger extends AbstractDatabaseTrigger {

  @Override
  public Boolean run(Config arg) throws Exception {

    AlterColumn at = new AlterColumn();
    at.services = services;
    AlterColumn.Config x = new AlterColumn.Config();
    switch (arg.command) {
      case "create": {
        // form does not set table id
        arg.object.put("ID", arg.object.get("parent") + "/" + arg.object.get("name"));
        x.command = "create";
        String[] parts = ((String) arg.object.get("parent")).split("/");
        x.database = parts[0] + "/" + parts[1];
        x.table = parts[2];
        x.column = (String) arg.object.get("name");
        x.newType = (String) arg.object.get("type");
        at.run(x);
        ((PojoDatabase) services.getConfig()).metadataCollection(x.database);
        return false;
      }
      case "update": {
        // ref implies displayWith
        String ref = (String) arg.object.get("ref");
        if (ref != null)
          arg.object.put("displayWith", "fk");
        else
          arg.object.put("displayWith", null);

        // pkpos implies createOnly
        Object pkpos = arg.object.get("pkpos");
        if (pkpos != null)
          arg.object.put("createOnly", true);
        else
          arg.object.put("createOnly", null);

        String[] parts = ((String) arg.search.get("ID")).split("/");
        x.database = parts[0] + "/" + parts[1];

        // pre flight check
        services.getConfig().getDatabase(x.database).getSchemaChange().check(arg);

        x.table = parts[2];
        x.column = parts[3];
        x.command = "alter";
        x.newType = (String) arg.object.get("type");

        // only call if name changes and is set
        Property old =
            services.getConfig().getSchema(x.database + "/" + x.table).properties.get(x.column);

        if (x.newType != null && !x.newType.equals(old.type)) {
          at.run(x);
          ((PojoDatabase) services.getConfig()).metadataCollection(x.database);
        }
        x.command = "rename";
        x.newName = (String) arg.object.get("name");
        if (x.newName != null && !x.newName.equals(old.name)) {
          at.run(x);
          ((PojoDatabase) services.getConfig()).metadataCollection(x.database);

          // delete data under old name but copy pkpos and ref
          services.getConfig().getConfigDatabase().delete(Table.ofName(arg.table), arg.search);
          if (old.pkpos != null && !arg.object.containsKey("pkpos"))
            arg.object.put("pkpos", old.pkpos);
          if (old.ref != null && !arg.object.containsKey("ref"))
            arg.object.put("ref", old.ref);
          if (old.ref != null && !arg.object.containsKey("displayWith"))
            arg.object.put("displayWith", old.displayWith);

          // before we continue, we need to change the update ID
          arg.search.put("ID", x.database + "/" + x.table + "/" + x.newName);
        }

        // continue with an update since other props might be set
        return true;
      }
      case "delete": {
        String[] parts = ((String) arg.search.get("ID")).split("/");
        x.database = parts[0] + "/" + parts[1];
        x.table = parts[2];
        x.column = parts[3];
        x.command = "delete";
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
    return "alterColumnTrigger";
  }
}
