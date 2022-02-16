package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.AbstractDatabase.CreateBatch;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.sqlite.JDBC;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.jcc.DB2Driver;

/**
 * REST API for management tasks
 */
@Path(Services.REST_PREFIX + "manage")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class Manage {

  @Inject
  Services services;

  @Inject
  Data data;

  /**
   * describes a column when client calls detect
   */
  public static class TypeSample {
    /**
     * column name
     */
    public String name;

    /**
     * column type (string, integer, number, date, string)
     */
    public String type;

    /**
     * first 10 values
     */
    public List<Object> sample;

    /**
     * suggested as a PK
     */
    public boolean pk;
  }

  /**
   * describes a set of tables to be uploaded
   */
  public static class DetectResult {

    /**
     * cannot be null, true: tables will be created, false: tables can be appended / replaced
     */
    public Boolean createMode;

    /**
     * map: tablename - tableinfo which is List of column infos
     */
    public Map<String, List<TypeSample>> schema = new LinkedHashMap<>();;
  }

  /**
   * allow iterable and index access to a csv record
   */
  static class CSVRecordWrapper extends AbstractList<String> {

    CSVRecord record;

    CSVRecordWrapper(CSVRecord record) {
      this.record = record;
    }

    @Override
    public String get(int index) {
      return record.get(index);
    }

    @Override
    public int size() {
      return record.size();
    }
  }

  /**
   * allow iterable and index access to a excel row
   */
  static class RowWrapper extends AbstractList<String> {

    List<String> record = new ArrayList<>();

    RowWrapper(FormulaEvaluator evaluator, Row record) {
      int colindex = -1;
      for (Cell c : record) {
        colindex++;
        while (colindex < c.getColumnIndex()) {
          colindex++;
          this.record.add("");
        }
        CellValue cellValue = evaluator.evaluate(c);
        if (cellValue.getCellType() == CellType.BOOLEAN)
          this.record.add("" + cellValue.getBooleanValue());
        else if (cellValue.getCellType() == CellType.NUMERIC)
          this.record.add("" + cellValue.getNumberValue());
        else if (cellValue.getCellType() == CellType.STRING)
          this.record.add("" + cellValue.getStringValue());
        else if (cellValue.getCellType() == CellType.ERROR)
          this.record.add("");
        else if (cellValue.getCellType() == CellType.BLANK)
          this.record.add("");
        else
          throw new RuntimeException("Illegal cell type");
      }
    }

    @Override
    public String get(int index) {
      if (record.size() > index)
        return record.get(index);
      else
        return "";
    }

    @Override
    public int size() {
      return record.size();
    }
  }

  protected static final ObjectMapper objectMapper = new ObjectMapper()
      .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
      .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true)
      .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true);;

  @Inject
  UserProfileManager userProfileManager;

  /**
   * creates the tables contained in input in the database
   */
  @SuppressWarnings("unchecked")
  @POST
  @Path("/create")
  @Consumes("multipart/form-data")
  @Operation(summary = "Create tables and insert data")
  public void create(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @QueryParam("database") String database,
      MultipartFormDataInput input) throws Exception {

    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    InputPart inputPart = uploadForm.get("__dj_schema").get(0);
    List<InputPart> inputParts = uploadForm.get("file");

    InputStream inputStream = inputPart.getBody(InputStream.class, null);
    Map<String, Object> schema = objectMapper.readValue(inputStream, JSONDatabase.tr);

    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    ACLContainerRequestFilter.check(sc, db, null);

    String dbId = services.getDashjoinID() + "/" + database;

    // for every table
    for (Entry<String, Object> entry : schema.entrySet()) {

      // lookup pk / type from metadata
      String pk = null;
      String type = null;
      for (Map<String, Object> prop : (List<Map<String, Object>>) entry.getValue())
        if ((boolean) prop.get("pk")) {
          pk = (String) prop.get("name");
          type = (String) prop.get("type");
        }

      if (pk == null)
        throw new Exception("No primary key defined in table: " + entry.getKey());

      if (type == null)
        type = "string";

      // create table(pk)
      data.create(sc, "config", "Table", new HashMap<>(of("name", entry.getKey(), "parent", dbId,
          "properties", new HashMap<>(of(pk, new HashMap<>(of("type", type)))))));

      // create columns
      for (Map<String, Object> prop : (List<Map<String, Object>>) entry.getValue()) {
        if (prop.get("name").equals(pk))
          continue;
        String ctype = (String) prop.get("type");
        if (ctype == null)
          ctype = "string";
        data.create(sc, "config", "Property", new HashMap<>(
            of("name", prop.get("name"), "type", ctype, "parent", dbId + "/" + entry.getKey())));
      }
    }

    // lookup DB again (new metadata)
    db = services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    insert(db, inputParts, false);
  }

  /**
   * append data to existing tables in database
   */
  @POST
  @Path("/append")
  @Consumes("multipart/form-data")
  @Operation(summary = "Append data into existing tables")
  public void append(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @QueryParam("database") String database,
      MultipartFormDataInput input) throws Exception {

    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    List<InputPart> inputParts = uploadForm.get("file");

    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    ACLContainerRequestFilter.check(sc, db, null);

    insert(db, inputParts, false);
  }

  void insert(AbstractDatabase db, List<InputPart> inputParts, boolean clearTable)
      throws Exception {
    // for each table
    for (InputPart inputPart : inputParts) {
      MultivaluedMap<String, String> header = inputPart.getHeaders();

      if (getFileName(header).contains("/")) {
        // special case: model folder upload
        JSONClassloaderDatabase cl = null;
        if (db.name.equals("config"))
          cl = new JSONClassloaderDatabase();

        Map<String, List<Map<String, Object>>> tables = handleModel(inputParts);
        for (Entry<String, List<Map<String, Object>>> t : tables.entrySet()) {
          Table m = db.tables.get(t.getKey());

          if (clearTable)
            db.delete(m);

          // for each row
          CreateBatch batch = db.openCreateBatch(m);
          for (Map<String, Object> object : t.getValue()) {
            db.cast(m, object);
            if (clearTable && cl != null && cl.read(m, object) != null)
              // special case: update read only object (instead of create) defined on the
              // classloader
              db.update(m, object, object);
            else
              batch.create(object);
          }
          batch.complete();
        }
        return;
      }

      if (getFileExt(header).toLowerCase().equals("csv")) {
        // parse CSV
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Iterator<CSVRecord> records = CSVFormat.RFC4180.parse(in).iterator();
        CSVRecord headers = records.next();

        // lookup table object
        Table m = db.tables.get(getFileName(header));

        if (clearTable)
          db.delete(m);

        // for each row
        CreateBatch batch = db.openCreateBatch(m);
        while (records.hasNext()) {
          Map<String, Object> object = new HashMap<>();
          int col = 0;
          for (String s : records.next()) {
            object.put(cleanColumnName(headers.get(col)), s);
            col++;
          }
          db.cast(m, object);
          batch.create(object);
        }
        batch.complete();
      } else if (getFileExt(header).toLowerCase().equals("xlsx")) {
        Workbook wb = WorkbookFactory.create(inputPart.getBody(InputStream.class, null));
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : wb) {
          Table m = db.tables.get(sheet.getSheetName());
          if (clearTable)
            db.delete(m);

          Iterator<Row> records = sheet.iterator();
          RowWrapper headers = new RowWrapper(evaluator, records.next());

          // for each row
          CreateBatch batch = db.openCreateBatch(m);
          while (records.hasNext()) {
            Map<String, Object> object = new HashMap<>();
            int col = 0;
            for (String s : new RowWrapper(evaluator, records.next())) {
              object.put(cleanColumnName(headers.get(col)), s);
              col++;
            }
            db.cast(m, object);
            batch.create(object);
          }
          batch.complete();
        }
      } else if (getFileExt(header).toLowerCase().equals("sqlite")) {
        File tmp = File.createTempFile(getFileName(header), "." + getFileExt(header));
        IOUtils.copy(inputPart.getBody(InputStream.class, null), new FileOutputStream(tmp));
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + tmp.getAbsolutePath())) {
          try (ResultSet rs = con.getMetaData().getTables(null, null, null, null)) {
            while (rs.next()) {
              String tablename = rs.getString("TABLE_NAME");
              Table m = db.tables.get(tablename);

              if (clearTable)
                db.delete(m);

              CreateBatch batch = db.openCreateBatch(m);
              try (Statement stmt = con.createStatement()) {
                try (ResultSet rows = stmt.executeQuery("select * from " + tablename)) {
                  ResultSetMetaData md = rows.getMetaData();
                  while (rows.next()) {
                    Map<String, Object> object = new HashMap<>();
                    for (int c = 1; c <= md.getColumnCount(); c++)
                      object.put(md.getColumnName(c), rows.getObject(c));
                    db.cast(m, object);
                    batch.create(object);
                  }
                }
              }
              batch.complete();
            }
          }
        }
        tmp.delete();
      } else if (getFileExt(header).toLowerCase().equals("json")) {
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        List<Map<String, Object>> parsed =
            objectMapper.readValue(inputStream, JSONDatabase.trTable);
        Table m = db.tables.get(getFileName(header));

        if (clearTable)
          db.delete(m);

        // for each row
        CreateBatch batch = db.openCreateBatch(m);
        for (Map<String, Object> object : parsed) {
          db.cast(m, object);
          batch.create(object);
        }
        batch.complete();
      } else
        throw new Exception("Unsupported file type: " + getFileExt(header)
            + ". Must be json, csv, xlsx or sqlite.");
    }
  }

  @POST
  @Path("/replace")
  @Consumes("multipart/form-data")
  @Operation(summary = "Delete data in tables and insert new data")
  public void replace(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @QueryParam("database") String database,
      MultipartFormDataInput input) throws Exception {

    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    List<InputPart> inputParts = uploadForm.get("file");

    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    ACLContainerRequestFilter.check(sc, db, null);

    insert(db, inputParts, true);
  }

  @POST
  @Path("/detect")
  @Consumes("multipart/form-data")
  @Operation(summary = "Detect tables, columns and datatypes before uploading", hidden = true)
  public DetectResult detect(@Context SecurityContext sc,
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @QueryParam("database") String database,
      MultipartFormDataInput input) throws Exception {

    DetectResult res = new DetectResult();

    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    List<InputPart> inputParts = uploadForm.get("file");

    Database db = services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    ACLContainerRequestFilter.check(sc, db, null);

    for (InputPart inputPart : inputParts) {
      MultivaluedMap<String, String> header = inputPart.getHeaders();

      if (getFileNameInternal(header).contains("/")) {
        // special case: model folder upload
        res.createMode = false;
        Map<String, List<Map<String, Object>>> tables = handleModel(inputParts);
        for (Entry<String, List<Map<String, Object>>> t : tables.entrySet())
          handleJson(res, database, database, ((AbstractDatabase) db).tables.get(t.getKey()),
              t.getKey(), t.getValue());
        return res;
      }

      if (getFileExt(header).toLowerCase().equals("csv")) {
        Table m = ((AbstractDatabase) db).tables.get(getFileName(header));
        createMode(res, database, getFileName(header), m);

        // convert the uploaded file to input stream
        InputStream inputStream = inputPart.getBody(InputStream.class, null);

        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        List<CSVRecord> records = CSVFormat.RFC4180.parse(in).getRecords();

        Iterator<CSVRecord> iter = records.iterator();
        CSVRecord first = iter.next();
        List<List<String>> _second = new ArrayList<>();
        for (int i = 0; i < 10; i++)
          _second.add(iter.hasNext() ? new CSVRecordWrapper(iter.next()) : null);

        handleStringTable(res, database, getFileName(header), m, new CSVRecordWrapper(first),
            _second);
      } else if (getFileExt(header).toLowerCase().equals("xlsx")) {
        Workbook wb = WorkbookFactory.create(inputPart.getBody(InputStream.class, null));
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : wb) {
          Table m = ((AbstractDatabase) db).tables.get(sheet.getSheetName());
          createMode(res, database, getFileName(header), m);

          Iterator<Row> iter = sheet.iterator();
          Row first = iter.next();
          List<List<String>> _second = new ArrayList<>();
          for (int i = 0; i < 10; i++)
            _second.add(iter.hasNext() ? new RowWrapper(evaluator, iter.next()) : null);

          handleStringTable(res, database, sheet.getSheetName(), m,
              new RowWrapper(evaluator, first), _second);
        }
      } else if (getFileExt(header).toLowerCase().equals("sqlite")) {
        File tmp = File.createTempFile(getFileName(header), "." + getFileExt(header));
        IOUtils.copy(inputPart.getBody(InputStream.class, null), new FileOutputStream(tmp));
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:" + tmp.getAbsolutePath())) {
          try (ResultSet rs = con.getMetaData().getTables(null, null, null, null)) {
            while (rs.next()) {
              String tablename = rs.getString("TABLE_NAME");
              Table m = ((AbstractDatabase) db).tables.get(tablename);
              createMode(res, database, getFileName(header), m);

              List<String> headers = new ArrayList<>();
              List<List<String>> data = new ArrayList<>();

              try (Statement stmt = con.createStatement()) {
                try (ResultSet rows =
                    stmt.executeQuery("select * from " + tablename + " limit 10")) {
                  ResultSetMetaData md = rows.getMetaData();
                  for (int c = 1; c <= md.getColumnCount(); c++)
                    headers.add(md.getColumnName(c));
                  while (rows.next()) {
                    List<String> row = new ArrayList<>();
                    for (int c = 1; c <= md.getColumnCount(); c++)
                      row.add(rows.getString(c));
                    data.add(row);
                  }
                }
              }

              while (data.size() < 10)
                data.add(null);

              handleStringTable(res, database, tablename, m, headers, data);
            }
          }
        }
      } else if (getFileExt(header).toLowerCase().equals("json")) {
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        try {
          List<Map<String, Object>> parsed =
              objectMapper.readValue(inputStream, JSONDatabase.trTable);
          Table m = ((AbstractDatabase) db).tables.get(getFileName(header));
          createMode(res, database, getFileName(header), m);

          handleJson(res, database, database, m, getFileName(header), parsed);
        } catch (JsonMappingException m) {
          throw new Exception(
              "Please provide a json file that contains a table (array of objects)");
        }
      } else
        throw new Exception("Unsupported file type: " + getFileExt(header)
            + ". Must be json, csv, xlsx or sqlite.");
    }

    return res;
  }

  /**
   * if the upload is done using the model folder, parses this into a map of tables
   */
  Map<String, List<Map<String, Object>>> handleModel(List<InputPart> inputParts) throws Exception {
    Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
    for (InputPart inputPartM : inputParts) {
      MultivaluedMap<String, String> headerM = inputPartM.getHeaders();
      InputStream inputStream = inputPartM.getBody(InputStream.class, null);
      Map<String, Object> parsed = objectMapper.readValue(inputStream, JSONDatabase.tr);
      String[] parts = getFileName(headerM).split("/");
      String tableName = parts[parts.length - 2];
      List<Map<String, Object>> table = tables.get(tableName);
      if (table == null) {
        table = new ArrayList<>();
        tables.put(tableName, table);
      }
      table.add(parsed);
    }
    return tables;
  }

  /**
   * like handleStringTable, but does pre-processing for JSON by extracting column names
   */
  void handleJson(DetectResult res, String database, String tablename, Table m, String fileName,
      List<Map<String, Object>> parsed) throws Exception {
    List<String> headers = new ArrayList<>();
    for (Map<String, Object> x : parsed) {
      for (Entry<String, Object> cell : x.entrySet()) {
        if (!headers.contains(cell.getKey()))
          headers.add(cell.getKey());
      }
    }

    List<List<String>> data = new ArrayList<>();
    for (Map<String, Object> x : parsed) {
      List<String> row = new ArrayList<>();
      for (String header : headers) {
        row.add("" + x.get(header));
      }
      data.add(row);
    }

    while (data.size() < 10)
      data.add(null);

    handleStringTable(res, database, fileName, m, headers, data);
  }

  /**
   * translates a table of strings (e.g. from a CSV) into the detect result structure by guessing
   * datatypes and the primary key
   */
  void handleStringTable(DetectResult res, String database, String tablename, Table m,
      List<String> first, List<List<String>> _second) throws Exception {
    Map<String, TypeSample> table = new LinkedHashMap<>();
    int col = 0;
    if (res.createMode) {
      boolean pkFound = false;
      for (Object cell : first) {
        TypeSample ts = new TypeSample();
        ts.sample = new ArrayList<>();
        for (List<String> second : _second)
          if (second == null)
            ts.sample.add(null);
          else {
            String value;
            try {
              value = second.get(col);
              if (value != null)
                try {
                  ts.sample.add(Integer.parseInt(value));
                  ts.type = "integer";
                } catch (NumberFormatException e) {
                  try {
                    ts.sample.add(Double.parseDouble(value));
                    ts.type = "number";
                  } catch (NumberFormatException e2) {
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                      ts.sample.add(Boolean.parseBoolean(value));
                      ts.type = "boolean";
                    } else {
                      ts.sample.add(value);
                      ts.type = "string";
                    }
                  }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
              value = null;
            }
          }

        if (!pkFound) {
          Set<Object> index = new HashSet<>();
          boolean unique = true;
          for (List<String> record : _second)
            if (record != null && !index.add(record.get(col))) {
              unique = false;
              break;
            }
          if (unique) {
            ts.pk = true;
            pkFound = true;
          }
        }

        table.put(cleanColumnName(cell), ts);
        col++;
      }
    } else {
      Set<String> names = new HashSet<>();
      for (Object cell : first)
        names.add(cleanColumnName(cell));

      if (!database.equals("config") && !names.equals(m.properties.keySet())) {
        Set<String> old = new HashSet<>(m.properties.keySet());
        old.removeAll(names);
        names.removeAll(m.properties.keySet());
        throw new Exception("Column names do not match on table " + tablename + ". Remove: " + names
            + ", add: " + old);
      }

      for (Object cell : first) {
        TypeSample ts = new TypeSample();

        Property p = m.properties.get(cleanColumnName(cell));
        ts.pk = p.pkpos == null ? false : p.pkpos == 0;
        ts.type = p.type;

        ts.sample = new ArrayList<>();
        for (List<String> second : _second)
          if (second == null)
            ts.sample.add(null);
          else
            try {
              ts.sample.add(second.get(col));
            } catch (IndexOutOfBoundsException e) {
              ts.sample.add(null);
            }
        table.put(cleanColumnName(cell), ts);
        col++;
      }

    }
    res.schema.put(tablename, table.entrySet().stream().map(x -> {
      TypeSample v = x.getValue();
      v.name = x.getKey();
      return v;
    }).collect(Collectors.toList()));
  }

  void createMode(DetectResult res, String database, String tablename, Table m) throws Exception {
    if (res.createMode == null)
      if (m == null)
        res.createMode = true;
      else
        res.createMode = false;
    else if (res.createMode)
      if (m == null)
        ;
      else
        throw new Exception("Table " + m.name + " already exists in database " + database);
    else if (m == null)
      throw new Exception("Table " + tablename + " does not exists in database " + database);
    else
      ;
  }

  String cleanColumnName(Object cell) {
    return cell.toString().replace('-', '_');
  }

  String getFileExt(MultivaluedMap<String, String> header) {
    return FilenameUtils.getExtension(getFileNameInternal(header));
  }

  String getFileName(MultivaluedMap<String, String> header) {
    String res = FilenameUtils.removeExtension(getFileNameInternal(header));
    if (!res.contains("/"))
      if (res.contains("%3A") || res.contains("%2F"))
        // allow RDF uploads where filename must be a URI
        res = URLDecoder.decode(res, StandardCharsets.UTF_8);

    return res;
  }

  String getFileNameInternal(MultivaluedMap<String, String> header) {
    String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
    for (String filename : contentDisposition) {
      if ((filename.trim().startsWith("filename"))) {
        String[] name = filename.split("=");
        return URLDecoder.decode(name[1].trim().replaceAll("\"", ""), StandardCharsets.UTF_8);
      }
    }
    throw new RuntimeException("No filename / tablename provided");
  }

  /**
   * returns all JDBC driver classes found on the backend's service loader classpath
   */
  @GET
  @Path("/getDrivers")
  @Operation(
      summary = "returns all JDBC driver classes found on the backend's service loader classpath")
  @APIResponse(description = "List of version objects describing each driver")
  public List<Version> getDrivers() {
    List<Version> res = new ArrayList<>();
    for (Object inst : SafeServiceLoader.load(Driver.class)) {
      Version v = metaInf(inst.getClass(), null, new Version());
      v.name = inst.getClass().getName();
      res.add(v);
    }
    return res;
  }

  /**
   * returns all DB implementation classes found on the backend's service loader classpath
   */
  @GET
  @Path("/getDatabases")
  @Operation(
      summary = "returns all DB implementation classes found on the backend's service loader classpath")
  @APIResponse(description = "List of class names")
  public List<Version> getDatabases() {
    List<Version> res = new ArrayList<>();
    for (Object inst : SafeServiceLoader.load(Database.class)) {
      if (!(inst instanceof PojoDatabase)) {
        Version v = metaInf(inst.getClass(), null, new Version());
        v.name = inst.getClass().getName();
        res.add(v);
      }
    }
    return res;
  }

  /**
   * returns all function implementation classes found on the backend's service loader classpath
   */
  @GET
  @Path("/getFunctions")
  @Operation(
      summary = "returns all action implementation classes found on the backend's service loader classpath")
  @APIResponse(description = "List of class names")
  public List<FunctionVersion> getFunctions() {
    List<FunctionVersion> res = new ArrayList<>();
    for (Function<?, ?> inst : SafeServiceLoader.load(Function.class)) {
      FunctionVersion v = (FunctionVersion) metaInf(inst.getClass(), null, new FunctionVersion());
      v.name = inst.getClass().getName();
      if (inst instanceof AbstractConfigurableFunction)
        v.function = "$call(...)";
      else
        v.function = "$" + inst.getID();
      v.type = inst.getType();
      res.add(v);
    }
    for (String f : new String[] {"$read", "$create", "$update", "$traverse", "$delete", "$query",
        "$call", "$incoming"}) {
      FunctionVersion v = (FunctionVersion) metaInf(getClass(), null, new FunctionVersion());
      v.function = f;
      if (f.equals("$create") || f.equals("$update") || f.equals("$delete"))
        v.type = "write";
      else
        v.type = "read";
      res.add(v);
    }
    return res;
  }

  /**
   * returns all configurable function implementation classes found on the backend's service loader
   * classpath
   */
  @GET
  @Path("/getConfigurableFunctions")
  @Operation(
      summary = "returns all configurable function implementation classes found on the backend's service loader classpath")
  @APIResponse(description = "List of class names")
  public List<FunctionVersion> getConfigurableFunctions() {
    List<FunctionVersion> res = new ArrayList<>();
    for (Function<?, ?> inst : SafeServiceLoader.load(Function.class)) {
      FunctionVersion v = (FunctionVersion) metaInf(inst.getClass(), null, new FunctionVersion());
      v.name = inst.getClass().getName();
      v.type = inst.getType();
      if (inst instanceof AbstractConfigurableFunction) {
        v.function = "$call";
        res.add(v);
      }
    }
    return res;
  }

  /**
   * returns the version of the Dashjoin platform
   */
  @GET
  @Path("/version")
  @Operation(summary = "returns the version of the Dashjoin platform")
  @APIResponse(description = "Version object describing the platform")
  public Version version() {
    return getVersion();
  }

  public static Version getVersion() {
    Version v = metaInf(Manage.class, "dev", new Version());
    v.name = "Dashjoin Low Code Development and Integration Platform";
    v.buildTime = getGitBuildInfo().getProperty("git.build.time", "unknown");
    v.runtime = System.getProperty("java.version");
    return v;
  }

  /**
   * returns the roles of the current user
   * 
   * @throws Exception
   */
  @GET
  @Path("/roles")
  @Operation(summary = "returns the roles of the current user")
  @APIResponse(description = "list of roles")
  public List<String> roles(@Context SecurityContext sc) throws Exception {
    List<String> res = new ArrayList<>();
    for (Map<String, Object> i : services.getConfig().getConfigDatabase()
        .all(Table.ofName("dj-role"), null, null, null, false, null))
      if (sc.isUserInRole((String) i.get("ID")))
        res.add((String) i.get("ID"));
    return res;
  }

  /**
   * exports the database
   */
  @GET
  @Path("/export/{database}")
  @Operation(summary = "exports the contents of the database")
  @APIResponse(description = "map of db tables")
  public Map<String, List<Map<String, Object>>> export(
      @Parameter(description = "database name to run the operation on",
          example = "northwind") @PathParam("database") String database)
      throws Exception {

    int limit = 10000;
    int count = 0;

    Map<String, List<Map<String, Object>>> res = new LinkedHashMap<>();
    AbstractDatabase db =
        services.getConfig().getDatabase(services.getDashjoinID() + "/" + database);

    Database data = db instanceof PojoDatabase ? ((PojoDatabase) db).user() : db;

    for (Table table : db.tables.values()) {
      List<Map<String, Object>> values = data.all(table, null, limit, null, false, null);
      if (values.isEmpty())
        continue;
      res.put(table.name, values);
      count = count + values.size();
      if (count > limit)
        throw new Exception("Export limit (" + limit + " records) exceeded");
    }
    return res;
  }

  /**
   * holds the version and vendor info from the jar's manifest
   */
  @Schema(title = "Version: holds the version and vendor info from the jar's manifest")
  public static class Version {

    @Schema(title = "Semantic version string")
    public String version;

    @Schema(title = "Software / library name")
    public String title;

    @Schema(title = "Vendor company name")
    public String vendor;

    @Schema(title = "Software / library package name")
    public String name;

    @Schema(title = "Build time")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String buildTime;

    @Schema(title = "Runtime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String runtime;
  }

  /**
   * adds function name and type to version
   */
  public static class FunctionVersion extends Version {

    @Schema(title = "Function name")
    public String function;

    @Schema(title = "Read only or with side effects")
    public String type;
  }

  public static Version metaInf(Class<?> c, String def, Version v) {
    v.version = c.getPackage().getImplementationVersion();
    v.title = c.getPackage().getImplementationTitle();
    v.vendor = c.getPackage().getImplementationVendor();

    String jar = null;
    if (c.equals(org.mariadb.jdbc.Driver.class))
      jar = "mariadb";
    if (c.equals(DB2Driver.class))
      jar = "db2";
    if (c.equals(JDBC.class))
      jar = "sqlite";

    if (jar != null)
      try {
        Enumeration<URL> resources =
            Manage.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
          URL next = resources.nextElement();
          if (next.toString().contains(jar)) {

            Properties p = new Properties();
            p.load(next.openStream());
            if (v.version == null)
              v.version = p.getProperty("Bundle-Version");
            if (v.title == null)
              v.title = p.getProperty("Bundle-Name");
          }
        }
      } catch (IOException ignore) {
      }

    if (v.version == null)
      v.version = def;
    if (v.title == null)
      v.title = def;
    if (v.vendor == null)
      v.vendor = def;
    return v;
  }

  /**
   * Tries to read the git.properties which is written by the release build
   * 
   * @return Git build properties
   */
  public static Properties getGitBuildInfo() {
    String name = "git.properties";
    Properties props = new Properties();
    try {
      props.load(Manage.class.getClassLoader().getResourceAsStream(name));
    } catch (IOException e) {
      // intentionally ignored
    }
    return props;
  }

}
