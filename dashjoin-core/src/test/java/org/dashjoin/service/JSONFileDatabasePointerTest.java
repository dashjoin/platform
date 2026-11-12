package org.dashjoin.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * externalized strings are found via their -pointer fields, without listing the table folder
 */
public class JSONFileDatabasePointerTest {

  @TempDir
  Path tmp;

  JSONFileDatabase db;
  Path dir;
  final Table table = Table.ofName("t");

  @BeforeEach
  void setup() throws Exception {
    db = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return tmp.resolve(path).toFile();
      }
    };
    dir = Files.createDirectories(tmp.resolve("model/t"));
  }

  Map<String, Object> read(String id) throws Exception {
    return db.read(table, Map.of("ID", id));
  }

  @Test
  public void readsPointerFile() throws Exception {
    Files.writeString(dir.resolve("a%40b.json"), "{\"ID\": \"a@b\", \"query-pointer\": \"query.sql\"}");
    Files.writeString(dir.resolve("a%40b.query.sql"), "select 1");
    Map<String, Object> res = read("a@b");
    Assertions.assertEquals("select 1", res.get("query"));
    Assertions.assertEquals("query.sql", res.get("query-pointer"));

    // same via queryMap
    org.dashjoin.model.QueryMeta q = new org.dashjoin.model.QueryMeta();
    q.query = "t";
    Assertions.assertEquals("select 1", db.queryMap(q, null).get("a@b").get("query"));
  }

  @Test
  public void nestedAndDuplicatePointers() throws Exception {
    // the second reference to the same file gets the content but loses its pointer
    Files.writeString(dir.resolve("x.json"), "{\"ID\": \"x\", \"children\": ["
        + "{\"html-pointer\": \"html.html\"}, {\"html-pointer\": \"html.html\"}]}");
    Files.writeString(dir.resolve("x.html.html"), "<b>hi</b>");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> children = (List<Map<String, Object>>) read("x").get("children");
    Assertions.assertEquals("<b>hi</b>", children.get(0).get("html"));
    Assertions.assertEquals("html.html", children.get(0).get("html-pointer"));
    Assertions.assertEquals("<b>hi</b>", children.get(1).get("html"));
    Assertions.assertFalse(children.get(1).containsKey("html-pointer"));
  }

  @Test
  public void missingPointerFileIsIgnored() throws Exception {
    Files.writeString(dir.resolve("m.json"), "{\"ID\": \"m\", \"query-pointer\": \"query.sql\"}");
    Map<String, Object> res = read("m");
    Assertions.assertFalse(res.containsKey("query"));
    Assertions.assertEquals("query.sql", res.get("query-pointer"));
  }

  @Test
  public void pointerCannotLeaveFolder() throws Exception {
    Files.writeString(tmp.resolve("secret.txt"), "secret");
    Files.writeString(dir.resolve("p.json"),
        "{\"ID\": \"p\", \"query-pointer\": \"/../../../secret.txt\"}");
    Assertions.assertFalse(read("p").containsKey("query"));
  }
}
