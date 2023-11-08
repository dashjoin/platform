package org.dashjoin.mapping;

import static org.dashjoin.util.MapUtil.of;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.dashjoin.function.Index;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.AbstractDatabase.DeleteBatch;
import org.dashjoin.model.AbstractDatabase.MergeBatch;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.service.ExMapper;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.ddl.SchemaChange;
import org.dashjoin.util.PerfTimer;
import lombok.extern.java.Log;

/**
 * base class for datasource / provider functions
 */
@Log
@JsonSchema(required = {"database", "oldData"},
    order = {"database", "oldData", "createSchema", "mappings"})
public abstract class AbstractSource extends AbstractMapping<Void> {

  /**
   * indicates how to deal with data from the previous run:
   * 
   * Ignore: simply add the data (update in case the record already is in the DB, insert if not). We
   * follow the "normal" update semantics meaning that key=null actually deletes the value in the
   * DB, whereas missing keys remain untouched
   * 
   * Refresh: all records from the target tables that have the _dj_source column matching the ID of
   * this function
   * 
   * Delete All: all records from the target tables are deleted, if createSchema is true, the tables
   * are also dropped in case columns are no longer needed or previously had another datatype
   * 
   * default is Ignore
   */
  @JsonSchema(enums = {"Ignore", "Refresh", "Delete All"}, title = "Handle existing data",
      description = "Ignore: simply add the data. Refresh: data that was created by this function and is no longer present in this run is deleted. Delete All: all records from the target tables are deleted.")
  public String oldData;

  public Date start;

  public String status;

  public Date end;

  /**
   * no longer needed since we can externalize multi line strings
   */
  public Boolean logStatusOnly;

  public static ThreadLocal<Context> context = new ThreadLocal<>();

  public static class Context {
    // default queue size 1000 - producers have to assure that object size is reasonable
    public BlockingQueue<Object> queue = new LinkedBlockingQueue<>(1000);
    boolean producerDone;

    public void producerDone() {
      producerDone = true;
    }

    public boolean hasNext() throws InterruptedException {
      while (queue.isEmpty()) {
        if (producerDone)
          return false;
        Thread.sleep(100);
      }
      return true;
    }
  }

  /**
   * contains the main merge logic
   */
  @Override
  public Void run(Void arg) throws Exception {
    try {
      set(of("start", new Date(), "end", null, "status", "starting..."));
      Void res = runInternal(arg);
      set(of("end", new Date(), "status", "success"));
      return res;
    } catch (Exception e) {
      set(of("end", new Date(), "status", "Error: " + ExMapper.getMessage(e)));
      throw e;
    }
  }

  protected void info(String s) throws Exception {
    set(of("status", s));
  }

  // Global lock for ConfigDB workaround
  protected static Object lock = new Object();

  protected void set(Map<String, Object> update) throws Exception {
    if (ID == null)
      return;
    log.info("" + update);
    if (Boolean.TRUE.equals(logStatusOnly))
      return;

    // FIXME: workaround for ConfigDB multi threading issue
    // Remove this once fixed
    synchronized (lock) {
      services.getConfig().getDatabase(services.getDashjoinID() + "/config")
          .update(Table.ofName("dj-function"), of("ID", ID), update);
    }
  }

  AbstractDatabase ddl(AbstractDatabase db, Map<String, List<Map<String, Object>>> tables)
      throws Exception {
    SchemaChange ddl = db.getSchemaChange();
    boolean dirty = false;

    try {
      for (Entry<String, List<Map<String, Object>>> table : tables.entrySet()) {
        if (createSchema != null && createSchema) {

          Mapping mapping = mappings == null ? null : mappings.get(table.getKey());
          String mappingpk = null;
          if (mapping == null || mapping.pk == null) {
            for (Map<String, Object> row : table.getValue()) {
              if (row.containsKey("ID")) {
                mappingpk = "ID";
                break;
              }
              if (row.containsKey("id")) {
                mappingpk = "id";
                break;
              }
              if (row.containsKey("_id")) {
                mappingpk = "_id";
                break;
              }
            }
          } else
            mappingpk = mapping.pk;

          if (mappingpk == null)
            throw new Exception("No primary key specified for table " + table.getKey());

          if ("Delete All".equals(oldData)) {
            try {
              ddl.dropTable(table.getKey());
            } catch (Exception mightNotExist) {
              info("error deleting database: " + table.getKey());
            }
            db.tables.remove(table.getKey());
          }

          if (!db.tables.containsKey(table.getKey())
              || db.tables.get(table.getKey()).name == null) {
            // table does not exist of only contains some bootstrapped metadata like dj-label
            dirty = true;
            ddl.createTable(table.getKey(), mappingpk, type(mappingpk, table.getValue()));
            for (String col : cols(table.getValue(), true))
              if (!col.equals(mappingpk)) {
                ddl.createColumn(table.getKey(), col, type(col, table.getValue()));
              }
          } else {
            for (String col : cols(table.getValue(), true))
              if (!db.tables.get(table.getKey()).properties.containsKey(col)) {
                ddl.createColumn(table.getKey(), col, type(col, table.getValue()));
                dirty = true;
              }
          }
        } else {
          if ("Delete All".equals(oldData))
            db.delete(db.tables.get(table.getKey()));
        }
      }
    } finally {
      if (dirty) {
        PojoDatabase config = (PojoDatabase) services.getConfig();
        config.metadataCollection(services.getDashjoinID() + "/" + database);
        db = services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);
      }
    }
    return db;
  }

  public Void runInternal(Void arg) throws Exception {
    return runInternal(arg, true);
  }

  protected Void runInternal(Void arg, boolean first) throws Exception {
    if (database == null)
      throw new Exception("Please provide a database");

    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    info("running " + ID);
    PerfTimer timer = new PerfTimer();
    Map<String, List<Map<String, Object>>> tables = gather(sc);
    info("gather: " + timer.seconds());
    for (Entry<String, List<Map<String, Object>>> e : tables.entrySet())
      info(e.getKey() + ": " + e.getValue().size() + " rows");

    Index.reset();
    tables = Mapping.apply(expressionService, sc, tables, mappings);
    info("apply mapping: " + timer.seconds());

    if (first) {
      db = ddl(db, tables);
      info("ddl: " + timer.seconds());
    }

    for (Entry<String, List<Map<String, Object>>> table : tables.entrySet()) {
      Table t = db.tables.get(table.getKey());

      if (t == null)
        throw new Exception("Table '" + table.getKey()
            + "' does not exist. Change the mapping or select 'create schema'.");

      // IDs of the previous run
      Set<Map<String, Object>> delete = new HashSet<>();
      if ("Refresh".equals(oldData))
        for (Map<String, Object> row : db.all(t, null, null, null, false, of("_dj_source", ID))) {
          Map<String, Object> search = new HashMap<>();
          for (Property p : t.properties.values())
            if (p.pkpos != null)
              search.put(p.name, row.remove(p.name));
          delete.add(search);
          info("check data from prev run: " + timer.seconds());
        }

      int counter = 0;
      MergeBatch mbatch = db.openMergeBatch(t);
      for (Map<String, Object> row : table.getValue()) {
        row.put("_dj_source", ID);
        db.cast(t, row);
        mbatch.merge(row);
        if (counter++ % 1000 == 0)
          info((counter - 1) + " rows processed");

        // successful update: do not delete
        // move PKs to row to search
        Map<String, Object> search = new HashMap<>();
        for (Property p : t.properties.values())
          if (p.pkpos != null)
            search.put(p.name, row.get(p.name));

        delete.remove(search);
      }
      mbatch.complete();
      info("merge: " + timer.seconds());

      DeleteBatch batch = db.openDeleteBatch(t);
      for (Map<String, Object> d : delete) {
        db.cast(t, d);
        batch.delete(d);
      }
      batch.complete();
      info("delete: " + timer.seconds());
      info("done " + ID);
    }

    return null;
  }

  /**
   * inspect the table and collect all required columns
   */
  public static List<String> cols(List<Map<String, Object>> table, boolean includeSource) {
    List<String> res = new ArrayList<>();
    if (includeSource)
      res.add("_dj_source");
    Set<String> set = new HashSet<>();
    for (Map<String, Object> row : table) {
      for (String colname : row.keySet())
        if (!set.contains(colname)) {
          set.add(colname);
          res.add(colname);
        }
    }
    return res;
  }

  /**
   * inspect the table and detect the datatype of the specified column
   */
  public static String type(String pk, List<Map<String, Object>> table) {
    if ("_dj_source".equals(pk))
      return "string";
    Set<String> types = new HashSet<>();
    for (Map<String, Object> row : table) {
      Object o = row.get(pk);
      if (o != null) {
        if (o instanceof String) {
          try {
            DateTimeFormatter.ISO_DATE_TIME.parse((String) o);
            types.add("date");
          } catch (Exception e) {
            return "string";
          }
        }
        if (o instanceof Date)
          types.add("date");
        if (o instanceof Integer)
          types.add("integer");
        if (o instanceof Boolean)
          types.add("boolean");
        if (o instanceof Number)
          types.add("number");
        if (o instanceof List)
          types.add("array");
        if (o instanceof Map)
          types.add("object");

        if (types.size() > 1)
          return "string";
      }
    }
    if (types.size() == 0)
      return "string";
    else
      return types.iterator().next();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }
}
