package org.dashjoin.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import com.google.common.net.UrlEscapers;

/**
 * loads config info from classpath (allows applications to bundle config data with code)
 */
@ApplicationScoped
@JSONReadonlyDatabase
public class JSONClassloaderDatabase extends JSONDatabase {

  static Map<String, Set<String>> cache = new HashMap<>();

  Set<String> scan(String part) {
    if (!cache.containsKey(part))
      cache.put(part, new Reflections("model." + part, new ResourcesScanner())
          .getResources(Pattern.compile(".*\\.json")));
    return cache.get(part);
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = new LinkedHashMap<>();
    String[] parts = info.query.split("/");
    if (parts.length == 1) {
      for (String s : scan(parts[0])) {
        Map<String, Object> object =
            objectMapper.readValue(getClass().getResourceAsStream("/" + s), tr);
        res.put("" + object.get("ID"), object);
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
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    String path = "model/" + s.name + "/"
        + UrlEscapers.urlPathSegmentEscaper().escape("" + search.get("ID")) + ".json";

    try (InputStream is = getClass().getResourceAsStream("/" + path)) {
      if (is != null) {
        Map<String, Object> res = objectMapper.readValue(is, tr);
        if (res.get("ID") == null)
          throw new IllegalArgumentException("Object must contain ID field: " + path);
        return res;
      }
    }
    return null;
  }

}
