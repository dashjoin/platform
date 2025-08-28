package org.dashjoin.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * convert the example DB in llms.json to llms.txt format
 */
public class LLMs {

  /**
   * example record
   */
  public static class Example {
    public String title;
    public String description;
    public String language;
    public String file;
    public Object code;
  }

  public static void main(String[] args) throws Exception {
    ObjectMapper om = new ObjectMapper();

    StringBuffer b = new StringBuffer();
    b.append("========================\n");
    b.append("CODE SNIPPETS\n");
    b.append("========================\n\n");

    for (File f : new File[] {new File("../dashjoin-docs/llms/widget.json"),
        new File("../dashjoin-docs/llms/input.json")}) {
      Map<String, List<Example>> list =
          om.readValue(f, new TypeReference<Map<String, List<Example>>>() {});

      for (Entry<String, List<Example>> entry : list.entrySet())
        for (Example e : entry.getValue()) {
          if (e.language == null)
            e.language = e.code instanceof String ? "jsonata" : "json";
          b.append("TITLE: " + e.title + "\n");
          b.append("DESCRIPTION: " + e.description + "\n");
          if (e.file != null)
            b.append("FILE: " + e.file + "\n");
          b.append("LANGUAGE: " + e.language + "\n");
          b.append(
              "CODE: " + om.writerWithDefaultPrettyPrinter().writeValueAsString(e.code) + "\n\n");
        }
    }
    System.out.println(b);
  }
}
