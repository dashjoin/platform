package org.dashjoin.service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * record cache of JSONFileDatabase, shared by all its instances (like the record locks).
 *
 * Turned on by the setting dashjoin.configdb.cache (DASHJOIN_CONFIGDB_CACHE=true). Only use it when
 * this JVM is the only writer of the model folder (e.g. one pod with a ReadWriteOnce volume) and
 * files are not edited by hand: changes made outside JSONFileDatabase are not seen until clear() is
 * called ($djClearConfigCache(), the git functions do this) or the JVM restarts.
 *
 * Consistency: every write of JSONFileDatabase ends in createLocked() or delete(), which hold the
 * record write lock and put the record as read back from disk (write-through) or remove it. Fills
 * from disk happen while holding the record read lock, so a fill cannot overwrite a newer write. A
 * failed write evicts the record, so the next read goes to disk.
 */
public class JSONFileCache {

  /**
   * setting that turns the cache on
   */
  public static final String PROPERTY = "dashjoin.configdb.cache";

  /**
   * null: not read from the config yet
   */
  static Boolean enabled;

  static boolean enabled() {
    if (enabled == null)
      try {
        enabled = ConfigProvider.getConfig().getOptionalValue(PROPERTY, Boolean.class).orElse(false);
      } catch (Exception e) {
        enabled = false;
      }
    return enabled;
  }

  /**
   * the parsed records of one table folder (externalized strings resolved), keyed by JSON file
   * name. complete: all files of the folder are in records, so a file missing from records does not
   * exist
   */
  static class TableCache {
    final ConcurrentHashMap<String, Map<String, Object>> records = new ConcurrentHashMap<>();
    volatile boolean complete;
  }

  /**
   * table folder caches, keyed by the normalized absolute path of the folder
   */
  static final ConcurrentHashMap<String, TableCache> cache = new ConcurrentHashMap<>();

  static String key(File dir) {
    return dir.toPath().toAbsolutePath().normalize().toString();
  }

  /**
   * the cache of the table folder dir, null if the cache is off
   */
  static TableCache table(File dir) {
    if (!enabled())
      return null;
    return cache.computeIfAbsent(key(dir), k -> new TableCache());
  }

  /**
   * remove all cached records of the table folders in the model folder modelDir (e.g.
   * home/model), for instance after a git operation changed files
   */
  public static void clear(File modelDir) {
    String prefix = key(modelDir) + File.separator;
    cache.keySet().removeIf(k -> k.startsWith(prefix));
  }

  /**
   * remove all cached records
   */
  public static void clear() {
    cache.clear();
  }

  /**
   * the record file was deleted
   */
  static void remove(File file) {
    TableCache tc = cache.get(key(file.getParentFile()));
    if (tc != null)
      tc.records.remove(file.getName());
  }

  /**
   * remove the record of file from the cache after a failed write. The folder is no longer known to
   * be complete, so the next read of the record goes to disk
   */
  static void evict(File file) {
    TableCache tc = cache.get(key(file.getParentFile()));
    if (tc != null) {
      tc.complete = false;
      tc.records.remove(file.getName());
    }
  }

  /**
   * deep copy of a JSON tree. Callers change the maps they get, the cached ones must stay as they
   * are
   */
  @SuppressWarnings("unchecked")
  static <T> T copy(T o) {
    if (o instanceof Map) {
      Map<String, Object> res = new LinkedHashMap<>();
      for (Entry<String, Object> e : ((Map<String, Object>) o).entrySet())
        res.put(e.getKey(), copy(e.getValue()));
      return (T) res;
    }
    if (o instanceof List) {
      List<Object> res = new ArrayList<>(((List<Object>) o).size());
      for (Object i : (List<Object>) o)
        res.add(copy(i));
      return (T) res;
    }
    // strings, numbers, booleans are immutable
    return o;
  }
}
