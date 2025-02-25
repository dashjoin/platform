package org.dashjoin.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.DJRuntime;
import org.dashjoin.util.Escape;
import org.dashjoin.util.Home;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.RuntimeDefinitions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 * file implementation of the config DB
 */
@ApplicationScoped
@DJRuntime(RuntimeDefinitions.ONPREMISE)
@Default
public class JSONFileDatabase extends JSONDatabase {

  @Inject
  Services services;

  @Inject
  protected Home home;

  protected File getFile(String path) {
    return home.getFile(path);
  }

  /**
   * get json file handle from table and id
   */
  File file(Table s, Map<String, Object> search) throws UnsupportedEncodingException {
    return file(s, search, "json");
  }

  /**
   * get json file handle from table, id and extension
   */
  File file(Table s, Map<String, Object> search, String ext) throws UnsupportedEncodingException {
    String path = "model/" + s.name + "/" + Escape.filename("" + search.get("ID")) + "." + ext;
    return getFile(path);
  }

  /**
   * get list of secondary file handles from table and id
   */
  List<File> secondaryFiles(Table s, Object id) throws UnsupportedEncodingException {
    File[] files = getFile("model/" + s.name).listFiles();
    return secondaryFiles(files, id);
  }

  /**
   * like above but leverages the file list
   */
  List<File> secondaryFiles(File[] files, Object id) throws UnsupportedEncodingException {
    List<File> res = new ArrayList<>();
    if (files != null) {
      for (File f : files) {
        if (f.getName().endsWith(".json"))
          continue;
        if (f.getName().startsWith(Escape.filename("" + id)))
          res.add(f);
      }
    }
    return res;
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = new LinkedHashMap<>();
    String[] parts = info.query.split("/");
    if (parts.length == 1) {
      String path = "model/" + parts[0];
      File[] files = getFile(path).listFiles();
      if (files != null) {
        // Use the same order independent of file system or OS
        Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

        for (File f : files) {
          if (!f.getName().endsWith(".json"))
            continue;
          Map<String, Object> object = objectMapper.readValue(f, tr);
          String id = "" + object.get("ID");
          readExternalizedStrings(id, secondaryFiles(files, id), object);
          res.put(id, object);
        }
      }
    } else {
      Table s = new Table();
      s.name = parts[0];
      Map<String, Object> i = read(s, Collections.singletonMap("ID", parts[1]));
      if (i != null)
        res.put("" + i.get("ID"), i);
    }
    return res;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void create(Table s, Map<String, Object> object) throws Exception {

    File file = file(s, object);
    if (!file.getParentFile().exists())
      if (!file.getParentFile().mkdirs())
        throw new IOException("Error creating model folder");

    // field names to externalize
    List<String> externalizeFields = new ArrayList<>();
    Map<String, Object> ecs = services == null ? MapUtil.of("list", null)
        : services.getConfig().getConfigDatabase().read(Table.ofName("dj-config"),
            MapUtil.of("ID", "externalize-config-strings"));
    if (ecs.get("list") instanceof List)
      for (Object i : (List<Object>) ecs.get("list"))
        if (i instanceof String) {
          String[] parts = ((String) i).split(":");
          if (parts.length == 2) {
            String table = parts[0].trim();
            String field = parts[1].trim();
            if (table.equals(s.name) || table.equals("*"))
              externalizeFields.add(field);
          }
        }

    writeExternalizedStrings(s, object.get("ID"), file, object, externalizeFields);

    objectMapper.writeValue(file, object);
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    File file = file(s, search);
    if (file.exists()) {
      Map<String, Object> res = objectMapper.readValue(file, tr);
      if (res.get("ID") == null)
        throw new IllegalArgumentException("Object must contain ID field: " + file);
      readExternalizedStrings("" + res.get("ID"), secondaryFiles(s, search.get("ID")), res);
      return res;
    }
    return null;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    for (File d : secondaryFiles(s, search.get("ID")))
      d.delete();
    return file(s, search).delete();
  }

  /**
   * traverse the JSON tree and search for keys called X-pointer, add key X with the file contents
   */
  void readExternalizedStrings(String id, List<File> secondaryFiles, Object res)
      throws IOException {
    _readExternalizedStrings(id,
        secondaryFiles.stream().map(f -> new FileUsed(f)).collect(Collectors.toList()), res);
  }

  @SuppressWarnings("unchecked")
  void _readExternalizedStrings(String id, List<FileUsed> secondaryFiles, Object res)
      throws IOException {
    if (res instanceof Map) {
      List<String> removeKeys = new ArrayList<>();
      for (Entry<String, Object> e : new ArrayList<>(((Map<String, Object>) res).entrySet())) {
        _readExternalizedStrings(id, secondaryFiles, e.getValue());
        if (e.getKey().endsWith("-pointer")) {
          String field = e.getKey().substring(0, e.getKey().length() - "-pointer".length());
          for (FileUsed f : secondaryFiles)
            if (f.file.getName().equals(Escape.filename(id) + "." + e.getValue())) {
              ((Map<String, Object>) res).put(field,
                  FileUtils.readFileToString(f.file, Charset.defaultCharset()));
              if (f.used)
                removeKeys.add(e.getKey());
              else
                f.used = true;
            }
        }
        for (String key : removeKeys)
          ((Map<String, Object>) res).remove(key);
      }
    }
    if (res instanceof List) {
      for (Object i : (List<Object>) res)
        _readExternalizedStrings(id, secondaryFiles, i);
    }
  }

  /**
   * make sure a file is only referenced once. Might be referenced multiple times if we use copy /
   * paste or the duplicate plugin UI feature. The second time, the pointer field is removed to make
   * sure the content gets its own ID.
   */
  static class FileUsed {
    FileUsed(File file) {
      this.file = file;
    }

    boolean used;
    File file;
  }

  /**
   * traverse JSON tree and externalize fields that are in externalizeFields list or end with
   * -pointer
   */
  @SuppressWarnings("unchecked")
  void writeExternalizedStrings(Table s, Object id, File file, Object res,
      List<String> externalizeFields) throws IOException {
    if (res instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) res;

      // first, handle all field-pointer entries
      for (Entry<String, Object> e : new ArrayList<>(map.entrySet())) {
        writeExternalizedStrings(s, id, file, e.getValue(), externalizeFields);
        if (e.getKey().endsWith("-pointer")) {
          String field = e.getKey().substring(0, e.getKey().length() - "-pointer".length());
          if (map.get(field) == null)
            deleteString(id, field, file, map, (String) e.getValue());
          else
            writeString(id, field, file, map, (String) e.getValue());
        }
      }

      // second, in case we're creating for the first time, check the fields that should be ext.
      final boolean autoExternalize = externalizeFields.contains("*");
      for (Entry<String, Object> e : new ArrayList<>(map.entrySet())) {
        if (externalizeFields.contains(e.getKey())
          || (autoExternalize && e.getValue() instanceof String
          && ((String)e.getValue()).contains("\n")))
          writeString(id, e.getKey(), file, map, generatePointer(s, id, e));
      }
    }
    if (res instanceof List) {
      for (Object i : (List<Object>) res)
        writeExternalizedStrings(s, id, file, i, externalizeFields);
    }
  }

  /**
   * remove field from map and write its value to the pointer file
   */
  void writeString(Object id, String field, File file, Map<String, Object> map, String pointer)
      throws IOException {
    String newName = Escape.filename("" + id) + "." + pointer;
    File newFile = new File(file.getParentFile(), newName);

    if (!(map.get(field) instanceof String) && (map.get(field) != null))
      throw new RuntimeException("Error externalizing data. The value of '" + field
          + "' must be a string. Please change the config.");

    map.put(field + "-pointer", pointer);
    FileUtils.writeStringToFile(newFile, (String) map.remove(field), Charset.defaultCharset());
  }

  /**
   * remove field from map and write its value to the pointer file
   */
  void deleteString(Object id, String field, File file, Map<String, Object> map, String pointer) {
    String newName = Escape.filename("" + id) + "." + pointer;
    File newFile = new File(file.getParentFile(), newName);
    newFile.delete();
    map.remove(field + "-pointer");
  }

  /**
   * pick good defaults for file extensions
   */
  String ext(Entry<String,Object> e) {
    String field = e.getKey();

    String val = ((String)e.getValue()).toLowerCase();
    // Check if the value contains language hint
    // Usually first line like "// Javascript" or "/* Jsonata */"
    if (val.contains("javascript"))
      return "js";
    if (val.contains("jsonata"))
      return "jsonata";

    // Use hints from the field name to generate file type
    if (field.endsWith("js") || field.endsWith("javascript"))
      return "js";
    if (field.endsWith("jsonata") || field.endsWith("jn"))
      return "jsonata";
    
    switch (field) {
      case "query":
        return "sql";
      case "expression":
        return "jsonata";
      case "foreach":
        return "jsonata";
      case "print":
        return "jsonata";
      case "onRender":
        return "jsonata";
      case "if":
        return "jsonata";
      case "extraArgs":
        return "jsonata";
      case "arguments":
        return "jsonata";
      case "schemaExpression":
        return "jsonata";
      case "nodes":
        return "jsonata";
      case "edges":
        return "jsonata";
      case "display":
        return "jsonata";
      case "context":
        return "jsonata";
      case "markdown":
        return "md";
      case "html":
        return "html";
      default:
        return "txt";
    }
  }

  /**
   * generate an unused pointer name with a suitable file extension
   */
  String generatePointer(Table s, Object id, Entry<String,Object> e) throws UnsupportedEncodingException {
    String field = e.getKey();
    List<String> names = new ArrayList<>();
    for (File f : secondaryFiles(s, id))
      names.add(f.getName());

      for (int counter = 0; counter < 1000; counter++) {
      String pointer = field + (counter>0 ? "-" + counter : "" ) + "." + ext(e);
      if (!names.contains(Escape.filename("" + id) + "." + pointer))
        return pointer;
    }
    throw new IllegalArgumentException();
  }
}
