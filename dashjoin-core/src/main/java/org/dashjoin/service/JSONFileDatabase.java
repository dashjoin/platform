package org.dashjoin.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.dashjoin.util.DJRuntime;
import org.dashjoin.util.Home;
import org.dashjoin.util.RuntimeDefinitions;

/**
 * file implementation of the config DB
 */
@ApplicationScoped
@DJRuntime(RuntimeDefinitions.ONPREMISE)
@Default
public class JSONFileDatabase extends JSONDatabase {

  @Inject
  Home home;

  /**
   * internal get file method
   */
  File file(Table s, Map<String, Object> search) throws UnsupportedEncodingException {
    String path =
        "model/" + s.name + "/" + URLEncoder.encode("" + search.get("ID"), "UTF-8") + ".json";
    return home.getFile(path);
  }

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    Map<String, Map<String, Object>> res = new LinkedHashMap<>();
    String[] parts = info.query.split("/");
    if (parts.length == 1) {
      String path = "model/" + parts[0];
      File[] files = home.getFile(path).listFiles();
      if (files != null)
        for (File f : files) {
          if (f.getName().endsWith(".deleted"))
            continue;
          Map<String, Object> object = objectMapper.readValue(f, tr);
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
  public void create(Table s, Map<String, Object> object) throws Exception {

    File file = file(s, object);
    if (!file.getParentFile().exists())
      if (!file.getParentFile().mkdirs())
        throw new IOException("Error creating model folder");

    objectMapper.writeValue(file, object);
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    File file = file(s, search);
    if (file.exists()) {
      Map<String, Object> res = objectMapper.readValue(file, tr);
      if (res.get("ID") == null)
        throw new IllegalArgumentException("Object must contain ID field: " + file);
      return res;
    }
    return null;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    return file(s, search).delete();
  }

}
