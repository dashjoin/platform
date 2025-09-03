package org.dashjoin.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * convert the example DB in llms.json to llms.txt format
 */
public class LLMs {

  /**
   * example record
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Example {
    public String title;
    public String description;
    public String language;
    public String file;
    public Object code;
    public Object output;
  }

  static ObjectMapper om = new ObjectMapper();

  public static void main(String[] args) throws Exception {

    StringBuffer b = new StringBuffer();
    b.append("========================\n");
    b.append("CODE SNIPPETS\n");
    b.append("========================\n\n");

    for (File f : new File[] {new File("../dashjoin-docs/llms/widget.json"),
        new File("../dashjoin-docs/llms/input.json"), new File("../dashjoin-docs/llms/config.json"),
        new File("../dashjoin-docs/llms/jsonata.json")}) {
      Map<String, List<Example>> list =
          om.readValue(f, new TypeReference<Map<String, List<Example>>>() {});

      for (Entry<String, List<Example>> entry : list.entrySet())
        for (Example e : entry.getValue()) {
          if (e.language == null)
            e.language = e.code instanceof String ? "jsonata" : "json";
          b.append("TITLE: " + (e.title != null ? e.title : entry.getKey()) + "\n");
          b.append("DESCRIPTION: " + e.description + "\n");
          if (e.file != null)
            b.append("FILE: " + e.file + "\n");
          b.append("LANGUAGE: " + e.language + "\n");
          if (e.code instanceof String)
            b.append("CODE: " + e.code + "\n\n");
          else
            b.append(
                "CODE: " + om.writerWithDefaultPrettyPrinter().writeValueAsString(e.code) + "\n\n");
        }
    }

    FileUtils.write(new File("../llms.txt"), b, Charset.defaultCharset());

    writeInput();
    writeWidget();
    writeConfig();
    writeJsonata();
  }

  static void writeInput() throws Exception {
    StringBuffer b = new StringBuffer();
    b.append("# Appendix: Form Input Types\n");
    b.append("These are possible children of the create, edit, button, and variable widgets.\n");
    Map<String, List<Example>> list = om.readValue(new File("../dashjoin-docs/llms/input.json"),
        new TypeReference<Map<String, List<Example>>>() {});

    for (Entry<String, List<Example>> entry : list.entrySet())
      for (Example e : entry.getValue()) {
        if (e.language == null)
          e.language = e.code instanceof String ? "jsonata" : "json";
        b.append("## " + e.title + "\n");
        b.append(e.description + "\n");
        b.append("```json\n");
        b.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.code) + "\n```\n");
        b.append("Sample output: \n```json\n");
        b.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.output) + "\n```\n");
      }

    FileUtils.write(new File("../dashjoin-docs/docs/appendix-inputs.md"), b,
        Charset.defaultCharset());
  }

  static void writeConfig() throws Exception {
    StringBuffer b = new StringBuffer();
    b.append("# Appendix: Databases, Queries, Functions, and Configurations\n");
    Map<String, List<Example>> list = om.readValue(new File("../dashjoin-docs/llms/config.json"),
        new TypeReference<Map<String, List<Example>>>() {});

    for (Entry<String, List<Example>> entry : list.entrySet())
      for (Example e : entry.getValue()) {
        if (e.language == null)
          e.language = e.code instanceof String ? "jsonata" : "json";
        b.append("## " + e.title + "\n");
        b.append(e.description + "\n");
        b.append("```json\n");
        b.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.code) + "\n```\n");
      }

    FileUtils.write(new File("../dashjoin-docs/docs/appendix-configurations.md"), b,
        Charset.defaultCharset());
  }

  static void writeJsonata() throws Exception {
    StringBuffer b = new StringBuffer();
    b.append("# Appendix: JSONata function library\n");
    Map<String, List<Example>> list = om.readValue(new File("../dashjoin-docs/llms/jsonata.json"),
        new TypeReference<Map<String, List<Example>>>() {});

    for (Entry<String, List<Example>> entry : list.entrySet())
      for (Example e : entry.getValue()) {
        if (e.language == null)
          e.language = e.code instanceof String ? "jsonata" : "json";
        b.append("## " + (e.title != null ? e.title : entry.getKey()) + "\n");
        b.append(e.description + "\n");
        b.append("```\n");
        b.append(e.code + "\n```\n");
        b.append("Sample output: \n```json\n");
        b.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.output) + "\n```\n");
      }

    FileUtils.write(new File("../dashjoin-docs/docs/appendix-jsonata.md"), b,
        Charset.defaultCharset());
  }

  @SuppressWarnings("unchecked")
  static void writeWidget() throws Exception {
    StringBuffer b = new StringBuffer();
    b.append("# Appendix: Form Input Types\n");
    Map<String, List<Example>> list = om.readValue(new File("../dashjoin-docs/llms/widget.json"),
        new TypeReference<Map<String, List<Example>>>() {});

    for (Entry<String, List<Example>> entry : list.entrySet())
      for (Example e : entry.getValue()) {
        if (e.language == null)
          e.language = e.code instanceof String ? "jsonata" : "json";
        b.append("## " + e.title + "\n");
        b.append(e.description + "\n");
        b.append("```json\n");
        b.append(om.writerWithDefaultPrettyPrinter().writeValueAsString(e.code) + "\n```\n");
        if (e.code instanceof Map) {
          Map<String, Object> map = (Map<String, Object>) e.code;
          for (Entry<String, Object> field : map.entrySet())
            if (field.getValue() instanceof String)
              if (((String) field.getValue()).contains("\n")) {
                b.append(field.getKey() + "\n");
                b.append("```\n");
                b.append(field.getValue() + "\n");
                b.append("```\n");
              }
        }
      }

    FileUtils.write(new File("../dashjoin-docs/docs/appendix-widgets.md"), b,
        Charset.defaultCharset());
  }
}
