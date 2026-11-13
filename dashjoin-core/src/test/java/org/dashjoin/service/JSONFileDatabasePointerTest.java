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

  @Test
  public void deleteRemovesOwnPointerFiles() throws Exception {
    Files.writeString(dir.resolve("d.json"), "{\"ID\": \"d\", \"query-pointer\": \"query.sql\","
        + " \"children\": [{\"html-pointer\": \"html.html\"}]}");
    Files.writeString(dir.resolve("d.query.sql"), "select 1");
    Files.writeString(dir.resolve("d.html.html"), "<b>hi</b>");
    Assertions.assertTrue(db.delete(table, Map.of("ID", "d")));
    try (var files = Files.list(dir)) {
      Assertions.assertEquals(0, files.count());
    }
  }

  @Test
  public void deleteKeepsFilesOfRecordsWithSamePrefix() throws Exception {
    // deleting "fn" must not touch the files of "fn-all" or "fn.x"
    for (String id : new String[] {"fn", "fn-all", "fn.x"}) {
      Files.writeString(dir.resolve(id + ".json"),
          "{\"ID\": \"" + id + "\", \"expression-pointer\": \"expression.jsonata\"}");
      Files.writeString(dir.resolve(id + ".expression.jsonata"), id);
    }
    Assertions.assertTrue(db.delete(table, Map.of("ID", "fn")));
    Assertions.assertFalse(Files.exists(dir.resolve("fn.json")));
    Assertions.assertFalse(Files.exists(dir.resolve("fn.expression.jsonata")));
    Assertions.assertEquals("fn-all", read("fn-all").get("expression"));
    Assertions.assertEquals("fn.x", read("fn.x").get("expression"));
  }

  @Test
  public void deleteCannotLeaveFolder() throws Exception {
    Files.writeString(tmp.resolve("secret.txt"), "secret");
    Files.writeString(dir.resolve("p.json"),
        "{\"ID\": \"p\", \"query-pointer\": \"/../../../secret.txt\"}");
    Assertions.assertTrue(db.delete(table, Map.of("ID", "p")));
    Assertions.assertTrue(Files.exists(tmp.resolve("secret.txt")));
  }

  @Test
  public void writeRejectsPointerLeavingFolder() throws Exception {
    Map<String, Object> rec = new java.util.HashMap<>();
    rec.put("ID", "w");
    rec.put("query", "pwned");
    rec.put("query-pointer", "/../../../evil.txt");
    Assertions.assertThrows(IllegalArgumentException.class, () -> db.create(table, rec));
    Assertions.assertFalse(Files.exists(tmp.resolve("evil.txt")));
    Assertions.assertFalse(Files.exists(dir.resolve("w.json")));
  }

  @Test
  public void writeWithNullValueCannotDeleteOutsideFolder() throws Exception {
    Files.writeString(tmp.resolve("secret.txt"), "secret");
    Map<String, Object> rec = new java.util.HashMap<>();
    rec.put("ID", "w");
    rec.put("query-pointer", "/../../../secret.txt");
    Assertions.assertThrows(IllegalArgumentException.class, () -> db.create(table, rec));
    Assertions.assertTrue(Files.exists(tmp.resolve("secret.txt")));
  }

  @Test
  public void writeRejectsNonStringPointer() throws Exception {
    Map<String, Object> rec = new java.util.HashMap<>();
    rec.put("ID", "w");
    rec.put("query", "select 1");
    rec.put("query-pointer", 42);
    Assertions.assertThrows(IllegalArgumentException.class, () -> db.create(table, rec));
  }

  @Test
  public void writeValidPointer() throws Exception {
    Map<String, Object> rec = new java.util.HashMap<>();
    rec.put("ID", "w");
    rec.put("query", "select 1");
    rec.put("query-pointer", "query.sql");
    db.create(table, rec);
    Assertions.assertEquals("select 1", Files.readString(dir.resolve("w.query.sql")));
    Assertions.assertEquals("select 1", read("w").get("query"));
  }
}
