package org.dashjoin.service;

import static org.dashjoin.service.UnionDatabaseNestedDeltaTest.m;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.dashjoin.model.Table;
import org.dashjoin.service.UnionDatabaseNestedDeltaTest.MemoryDatabase;
import org.dashjoin.util.Escape;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Baseline: documents how externalized strings (X-pointer fields whose text lives in a file next to
 * the record) behave when a nested object that contains them is saved, as of 7.1.
 *
 * Reads put the file's text into field X and keep X-pointer. A write stores the text of X in the
 * pointer file and keeps only X-pointer in the JSON. When the client sends a nested object without
 * the pointer, JSONDatabase.copyPointers takes the pointer from the stored version, but only
 * through nested objects, not through arrays.
 *
 * Tests marked "status quo" describe the current behaviour, not necessarily the desired one.
 */
public class PointerDeltaTest {

  @TempDir
  Path root;

  Path pages;
  MemoryDatabase platform;
  PojoDatabase config;

  final Table page = Table.ofName("page");

  @BeforeEach
  public void setup() throws Exception {
    JSONFileDatabase app = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return root.resolve(path).toFile();
      }
    };
    pages = Files.createDirectories(root.resolve("model/page"));
    platform = new MemoryDatabase(true);
    config = new PojoDatabase();
    config._cache = new ConcurrentHashMap<>();
    config._user = app;
    config._dbs = Arrays.asList(platform);
  }

  String json(String id) throws Exception {
    return Files.readString(pages.resolve(Escape.filename(id) + ".json"));
  }

  /** the layout as stored in the JSON file, without resolving pointers */
  @SuppressWarnings("unchecked")
  Map<String, Object> storedLayout(String id) throws Exception {
    return (Map<String, Object>) JSONDatabase.fromJsonString(json(id)).get("layout");
  }

  Path pointerFile(Path dir, String id, String pointer) throws Exception {
    return dir.resolve(Escape.filename(id) + "." + pointer);
  }

  /** an app page whose layout has an externalized markdown text */
  void writePage(String layoutJson) throws Exception {
    Files.writeString(pages.resolve("P.json"), "{\"ID\": \"P\", \"layout\": " + layoutJson + "}");
    Files.writeString(pointerFile(pages, "P", "markdown.md"), "old");
  }

  @SuppressWarnings("unchecked")
  Map<String, Object> layout() throws Exception {
    return (Map<String, Object>) config.read(page, m("ID", "P")).get("layout");
  }

  @Test
  public void readResolvesPointer() throws Exception {
    writePage("{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}");
    Assertions.assertEquals(
        m("widget", "text", "markdown-pointer", "markdown.md", "markdown", "old"), layout());
  }

  @Test
  public void roundTripWritesPointerFile() throws Exception {
    // the client sends back what it read, with the text changed
    writePage("{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}");
    Map<String, Object> layout = layout();
    layout.put("markdown", "new");
    config.update(page, m("ID", "P"), m("layout", layout));

    Assertions.assertEquals("new", Files.readString(pointerFile(pages, "P", "markdown.md")));
    Assertions.assertTrue(json("P").contains("markdown-pointer"), json("P"));
    Assertions.assertFalse(json("P").contains("new"), json("P"));
    Assertions.assertEquals("new", layout().get("markdown"));
  }

  @Test
  public void droppedPointerInObjectIsReused() throws Exception {
    // the client sends the nested object without the pointer: it is taken from the stored version
    writePage("{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}");
    config.update(page, m("ID", "P"), m("layout", m("widget", "text", "markdown", "new")));

    Assertions.assertEquals("new", Files.readString(pointerFile(pages, "P", "markdown.md")));
    Assertions.assertTrue(json("P").contains("markdown-pointer"), json("P"));
    Assertions.assertFalse(json("P").contains("new"), json("P"));
  }

  @Test
  public void droppedPointerInArrayIsInlined() throws Exception {
    // status quo: copyPointers does not look into arrays. The text is stored in the JSON and the
    // old pointer file stays behind, unused
    writePage("{\"widget\": \"page\", \"children\": "
        + "[{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}]}");
    config.update(page, m("ID", "P"), m("layout",
        m("widget", "page", "children", Arrays.asList(m("widget", "text", "markdown", "new")))));

    Assertions.assertEquals(
        m("widget", "page", "children", Arrays.asList(m("widget", "text", "markdown", "new"))),
        storedLayout("P"));
    Assertions.assertEquals("old", Files.readString(pointerFile(pages, "P", "markdown.md")));
  }

  @Test
  public void pointerInArrayRoundTrip() throws Exception {
    // when the client sends the pointer back, arrays are fine
    writePage("{\"widget\": \"page\", \"children\": "
        + "[{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}]}");
    Map<String, Object> layout = layout();
    @SuppressWarnings("unchecked")
    Map<String, Object> child =
        ((java.util.List<Map<String, Object>>) layout.get("children")).get(0);
    Assertions.assertEquals("old", child.get("markdown"));
    child.put("markdown", "new");
    config.update(page, m("ID", "P"), m("layout", layout));

    Assertions.assertEquals("new", Files.readString(pointerFile(pages, "P", "markdown.md")));
    Assertions.assertFalse(json("P").contains("new"), json("P"));
  }

  @Test
  public void clearingTextDeletesPointerFile() throws Exception {
    // status quo: the pointer file and the pointer are removed, the null value stays in the JSON
    writePage("{\"widget\": \"text\", \"markdown-pointer\": \"markdown.md\"}");
    Map<String, Object> layout = layout();
    layout.put("markdown", null);
    config.update(page, m("ID", "P"), m("layout", layout));

    Assertions.assertFalse(Files.exists(pointerFile(pages, "P", "markdown.md")));
    Assertions.assertEquals(m("widget", "text", "markdown", null), storedLayout("P"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void tableLayoutKeepsPointer() throws Exception {
    // the platform ships the table, the app's dj-database record has a layout with an
    // externalized text. Saving the layout from the editor (whole layout, as read) keeps the
    // pointer and updates the file
    String db = "dj/northwind";
    platform.table("dj-database").put(db, m("ID", db, "name", "northwind", "tables",
        m("EMPLOYEES", m("ID", "dj/northwind/EMPLOYEES", "name", "EMPLOYEES"))));
    Path dbs = Files.createDirectories(root.resolve("model/dj-database"));
    Files.writeString(dbs.resolve(Escape.filename(db) + ".json"),
        "{\"ID\": \"dj/northwind\", \"tables\": {\"EMPLOYEES\": {\"instanceLayout\": "
            + "{\"widget\": \"markdown\", \"markdown-pointer\": \"markdown.md\"}}}}");
    Files.writeString(pointerFile(dbs, db, "markdown.md"), "old");

    Map<String, Object> layout = (Map<String, Object>) config
        .read(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES")).get("instanceLayout");
    Assertions.assertEquals("old", layout.get("markdown"));

    layout.put("markdown", "new");
    config.update(Table.ofName("Table"), m("ID", "dj/northwind/EMPLOYEES"),
        m("ID", "dj/northwind/EMPLOYEES", "instanceLayout", layout));

    String json = Files.readString(dbs.resolve(Escape.filename(db) + ".json"));
    Assertions.assertEquals("new", Files.readString(pointerFile(dbs, db, "markdown.md")));
    Assertions.assertTrue(json.contains("markdown-pointer"), json);
    Assertions.assertFalse(json.contains("new"), json);
  }
}
