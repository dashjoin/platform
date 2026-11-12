package org.dashjoin.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
 * file implementation of the config DB.
 *
 * Thread safety: files are written to a temp file and then renamed over the target, so readers and
 * a crash mid-write never see a partial file. Each record (its .json plus externalized string
 * files) has a read/write lock; writes and read-modify-write sequences (see locked()) hold the
 * write lock. The locks are JVM-wide but do not protect against other processes writing the same
 * model folder.
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
   * prefix of temp files written before the atomic rename. URLEncoder (Escape.filename) always
   * encodes ~, so no record ID or externalized file can start with this
   */
  static final String TMP_PREFIX = "~tmp-";

  /**
   * one lock per record file, shared by all instances (there can be more than one per JVM). Keys
   * are lower case since macOS and Windows file systems are case insensitive
   */
  private static final ConcurrentHashMap<String, ReentrantReadWriteLock> locks =
      new ConcurrentHashMap<>();

  static ReentrantReadWriteLock lock(File jsonFile) {
    return locks.computeIfAbsent(jsonFile.getAbsolutePath().toLowerCase(Locale.ROOT),
        k -> new ReentrantReadWriteLock());
  }

  @Override
  public <T> T locked(Table s, Object id, Callable<T> body) throws Exception {
    ReentrantReadWriteLock rw = lock(file(s, Collections.singletonMap("ID", id)));
    if (!rw.isWriteLockedByCurrentThread() && services != null)
      // create() calls getConfig(), which is synchronized and reads config files while setting up
      // a tenant. Make sure that is done before we hold the lock, otherwise the two can deadlock
      services.getConfig();
    Lock l = rw.writeLock();
    l.lock();
    try {
      return body.call();
    } finally {
      l.unlock();
    }
  }

  interface StreamWriter {
    void write(OutputStream out) throws IOException;
  }

  /**
   * write to a temp file in the same folder, then rename it over the target. Readers see either the
   * old or the new content, never a mix or a truncated file
   */
  static void atomicWrite(File target, StreamWriter writer) throws IOException {
    Path dir = target.toPath().toAbsolutePath().getParent();
    // unlike mkdirs(), does not fail when another thread creates the folder at the same time
    Files.createDirectories(dir);
    // not Files.createTempFile, which would create the file with mode 600 on POSIX systems
    Path tmp = dir.resolve(TMP_PREFIX + UUID.randomUUID() + ".part");
    try {
      try (OutputStream out =
          Files.newOutputStream(tmp, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
        writer.write(out);
      }
      move(tmp, target.toPath());
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  static void move(Path tmp, Path target) throws IOException {
    for (int attempt = 0;; attempt++) {
      try {
        try {
          Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
          Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return;
      } catch (AccessDeniedException e) {
        // on Windows, the rename fails while another process (editor, virus scanner) has the
        // target open. Retry for a short while
        if (attempt >= 20)
          throw e;
        try {
          Thread.sleep(25);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
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
        if (f.getName().endsWith(".json") || f.getName().startsWith(TMP_PREFIX))
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

    // If ID is given as filter, append it to parts so the object is read directly
    if (parts.length == 1 && arguments != null && arguments.get("ID") != null)
      parts = new String[] { parts[0], "" + arguments.get("ID") };

    if (parts.length == 1) {
      String path = "model/" + parts[0];
      File[] files = getFile(path).listFiles();
      if (files != null) {
        // Use the same order independent of file system or OS
        Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

        for (File f : files) {
          if (!f.getName().endsWith(".json"))
            continue;
          Lock l = lock(f).readLock();
          l.lock();
          try {
            if (!f.exists())
              // deleted after we listed the folder
              continue;
            Map<String, Object> object = objectMapper.readValue(f, tr);
            String id = "" + object.get("ID");
            readExternalizedStrings(f.getParentFile(), id, object);
            res.put(id, object);
          } finally {
            l.unlock();
          }
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

  @Override
  public void create(Table s, Map<String, Object> object) throws Exception {
    locked(s, object.get("ID"), () -> {
      createLocked(s, object);
      return null;
    });
  }

  @SuppressWarnings("unchecked")
  void createLocked(Table s, Map<String, Object> object) throws Exception {

    File file = file(s, object);
    // unlike mkdirs(), does not fail when another thread creates the folder at the same time
    Files.createDirectories(file.getParentFile().toPath());

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

    // reject bad pointers before any file is written
    checkPointers(file.getParentFile(), Escape.filename("" + object.get("ID")), object);
    writeExternalizedStrings(s, object.get("ID"), file, object, externalizeFields);

    atomicWrite(file, out -> objectMapper.writeValue(out, object));
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    File file = file(s, search);
    Lock l = lock(file).readLock();
    l.lock();
    try {
      if (file.exists()) {
        Map<String, Object> res = objectMapper.readValue(file, tr);
        if (res.get("ID") == null)
          throw new IllegalArgumentException("Object must contain ID field: " + file);
        readExternalizedStrings(file.getParentFile(), "" + res.get("ID"), res);
        return res;
      }
      return null;
    } finally {
      l.unlock();
    }
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    return locked(s, search.get("ID"), () -> {
      File file = file(s, search);
      if (file.exists()) {
        // only delete the files named by the record's pointers. A prefix match on the ID would
        // also hit other records (deleting "a" would delete the files of "a-b")
        Map<String, Object> res = null;
        try {
          res = objectMapper.readValue(file, tr);
        } catch (IOException e) {
          // unreadable record: delete the JSON file only
        }
        Set<File> pointerFiles = new HashSet<>();
        forEachPointerFile(file.getParentFile(), Escape.filename("" + search.get("ID")), res,
            (map, key, f) -> pointerFiles.add(f));
        for (File d : pointerFiles)
          d.delete();
      }
      return file.delete();
    });
  }

  /**
   * traverse the JSON tree and search for keys called X-pointer, add key X with the contents of
   * file ID.pointer in folder dir. The pointers name the files, so the folder is not listed (which
   * is slow for tables with thousands of records).
   *
   * A file may be referenced more than once if we use copy / paste or the duplicate plugin UI
   * feature. The second time, the pointer field is removed to make sure the content gets its own ID.
   */
  void readExternalizedStrings(File dir, String id, Object res) throws IOException {
    Set<String> used = new HashSet<>();
    forEachPointerFile(dir, Escape.filename(id), res, (map, key, f) -> {
      String field = key.substring(0, key.length() - "-pointer".length());
      map.put(field, FileUtils.readFileToString(f, Charset.defaultCharset()));
      if (!used.add(f.getName()))
        map.remove(key);
    });
  }

  interface PointerVisitor {
    /**
     * called for a key X-pointer in map whose file f exists. May change map
     */
    void visit(Map<String, Object> map, String key, File f) throws IOException;
  }

  /**
   * traverse the JSON tree depth first and call visitor for every X-pointer key whose file ID.pointer
   * exists in dir. Used by read (load the contents) and delete (remove the files)
   */
  @SuppressWarnings("unchecked")
  static void forEachPointerFile(File dir, String escapedId, Object res, PointerVisitor visitor)
      throws IOException {
    if (res instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) res;
      // iterate over a copy, the visitor may change the map
      for (Entry<String, Object> e : new ArrayList<>(map.entrySet())) {
        forEachPointerFile(dir, escapedId, e.getValue(), visitor);
        if (e.getKey().endsWith("-pointer")) {
          File f = secondaryFile(dir, escapedId, e.getValue());
          if (f != null && f.isFile())
            visitor.visit(map, e.getKey(), f);
        }
      }
    }
    if (res instanceof List)
      for (Object i : (List<Object>) res)
        forEachPointerFile(dir, escapedId, i, visitor);
  }

  /**
   * file ID.pointer in dir, or null if the pointer would leave dir (e.g. "../../etc/passwd").
   * Escape.filename encodes path separators in the ID, so only the pointer needs checking
   */
  static File secondaryFile(File dir, String escapedId, Object pointer) {
    String name = escapedId + "." + pointer;
    if (name.contains("/") || name.contains("\\") || name.contains(File.separator)
        || name.indexOf('\0') >= 0)
      return null;
    return new File(dir, name);
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
        if (externalizeFields.contains(e.getKey()) || (autoExternalize
            && e.getValue() instanceof String && ((String) e.getValue()).contains("\n")))
          writeString(id, e.getKey(), file, map, generatePointer(s, id, e));
      }
    }
    if (res instanceof List) {
      for (Object i : (List<Object>) res)
        writeExternalizedStrings(s, id, file, i, externalizeFields);
    }
  }

  /**
   * file for the pointer next to the record's JSON file. Throws if the pointer is not a string or
   * would leave the table folder (e.g. "../../x")
   */
  static File pointerFile(Object id, File file, Object pointer) {
    File res = pointer instanceof String
        ? secondaryFile(file.getParentFile(), Escape.filename("" + id), pointer)
        : null;
    if (res == null)
      throw new IllegalArgumentException("Invalid pointer: " + pointer);
    return res;
  }

  /**
   * check all X-pointer values in the JSON tree, see pointerFile
   */
  @SuppressWarnings("unchecked")
  static void checkPointers(File dir, String escapedId, Object res) {
    if (res instanceof Map)
      for (Entry<String, Object> e : ((Map<String, Object>) res).entrySet()) {
        checkPointers(dir, escapedId, e.getValue());
        if (e.getKey().endsWith("-pointer") && (!(e.getValue() instanceof String)
            || secondaryFile(dir, escapedId, e.getValue()) == null))
          throw new IllegalArgumentException("Invalid pointer: " + e.getValue());
      }
    if (res instanceof List)
      for (Object i : (List<Object>) res)
        checkPointers(dir, escapedId, i);
  }

  /**
   * remove field from map and write its value to the pointer file
   */
  void writeString(Object id, String field, File file, Map<String, Object> map, String pointer)
      throws IOException {
    File newFile = pointerFile(id, file, pointer);

    if (!(map.get(field) instanceof String) && (map.get(field) != null))
      throw new RuntimeException("Error externalizing data. The value of '" + field
          + "' must be a string. Please change the config.");

    map.put(field + "-pointer", pointer);
    String value = (String) map.remove(field);
    atomicWrite(newFile,
        out -> out.write(value == null ? new byte[0] : value.getBytes(Charset.defaultCharset())));
  }

  /**
   * remove field from map and write its value to the pointer file
   */
  void deleteString(Object id, String field, File file, Map<String, Object> map, String pointer) {
    File newFile = pointerFile(id, file, pointer);
    newFile.delete();
    map.remove(field + "-pointer");
  }

  /**
   * pick good defaults for file extensions
   */
  String ext(Entry<String, Object> e) {
    String field = e.getKey();

    String val = ((String) e.getValue()).toLowerCase();
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

      // datagrid
      case "onchange":
        return "jsonata";
      case "ondelete":
        return "jsonata";
      case "oncreate":
        return "jsonata";

      // aichat
      case "onDelete":
        return "jsonata";
      case "onUpload":
        return "jsonata";
      case "onChat":
        return "jsonata";

      // diagram
      case "nodes":
        return "jsonata";
      case "edges":
        return "jsonata";
      case "moveNode":
        return "jsonata";
      case "addNode":
        return "jsonata";
      case "removeNode":
        return "jsonata";
      case "addEdge":
        return "jsonata";
      case "removeEdge":
        return "jsonata";

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
  String generatePointer(Table s, Object id, Entry<String, Object> e)
      throws UnsupportedEncodingException {
    String field = e.getKey();
    List<String> names = new ArrayList<>();
    for (File f : secondaryFiles(s, id))
      names.add(f.getName());

    for (int counter = 0; counter < 1000; counter++) {
      String pointer = field + (counter > 0 ? "-" + counter : "") + "." + ext(e);
      if (!names.contains(Escape.filename("" + id) + "." + pointer))
        return pointer;
    }
    throw new IllegalArgumentException();
  }
}
