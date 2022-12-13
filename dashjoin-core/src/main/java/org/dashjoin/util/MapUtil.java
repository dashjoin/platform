package org.dashjoin.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * like ImmutableMap.of() but mutable and allows nulls
 */
public class MapUtil {

  public static void keyWhitelist(Map<String, Object> map, List<String> keys) {
    List<String> delete = new ArrayList<>();
    for (String key : map.keySet())
      if (!keys.contains(key))
        delete.add(key);
    for (String key : delete)
      map.remove(key);
  }

  /**
   * treat {} and "" like null
   */
  public static void clean(Map<String, Object> object) {
    for (Entry<String, Object> e : object.entrySet()) {
      if ("".equals(e.getValue()))
        e.setValue(null);
      if (e.getValue() instanceof Map<?, ?>)
        if (((Map<?, ?>) e.getValue()).size() == 0)
          e.setValue(null);
      if (e.getValue() instanceof List<?>)
        if (((List<?>) e.getValue()).size() == 0)
          e.setValue(null);
    }
  }

  /**
   * like map.get() but casts result to Map(string, object)
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> getMap(Map<String, Object> map, String key) {
    return (Map<String, Object>) map.get(key);
  }

  public static Map<String, Object> of() {
    return new LinkedHashMap<>();
  }

  public static <V> Map<String, V> of(String key, V value) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    return m;
  }

  public static <V> Map<String, V> of(String key, V value, String key2, V value2) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    m.put(key2, value2);
    return m;
  }

  public static <V> Map<String, V> of(String key, V value, String key2, V value2, String key3,
      V value3) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    m.put(key2, value2);
    m.put(key3, value3);
    return m;
  }

  public static <V> Map<String, V> of(String key, V value, String key2, V value2, String key3,
      V value3, String key4, V value4) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    m.put(key2, value2);
    m.put(key3, value3);
    m.put(key4, value4);
    return m;
  }

  public static <V> Map<String, V> of(String key, V value, String key2, V value2, String key3,
      V value3, String key4, V value4, String key5, V value5) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    m.put(key2, value2);
    m.put(key3, value3);
    m.put(key4, value4);
    m.put(key5, value5);
    return m;
  }

  public static <V> Map<String, V> of(String key, V value, String key2, V value2, String key3,
      V value3, String key4, V value4, String key5, V value5, String key6, V value6) {
    Map<String, V> m = new LinkedHashMap<>();
    m.put(key, value);
    m.put(key2, value2);
    m.put(key3, value3);
    m.put(key4, value4);
    m.put(key5, value5);
    m.put(key6, value6);
    return m;
  }
}
