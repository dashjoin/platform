package org.dashjoin.mapping;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.dashjoin.model.JsonSchema;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonSchema(order = {"tablename1", "csv1", "tablename2", "csv2"})
public class TestSource extends AbstractSource {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public String tablename1;

  @JsonSchema(widget = "textarea", style = {"width", "400px", "height", "200px", "font-family",
      "'Courier New', Courier, monospace"})
  public String csv1;

  public String tablename2;

  @JsonSchema(widget = "textarea", style = {"width", "400px", "height", "200px", "font-family",
      "'Courier New', Courier, monospace"})
  public String csv2;

  @Override
  public Map<String, List<Map<String, Object>>> gather(SecurityContext sc) throws Exception {

    Map<String, List<Map<String, Object>>> res = new HashMap<>();

    if (csv1 == null)
      throw new Exception("please provide at least one CSV");

    if (tablename1 == null)
      throw new Exception("please provide at least one table name");

    String[] tablename =
        (tablename2 != null) ? new String[] {tablename1, tablename2} : new String[] {tablename1};

    String[] csv = (csv2 != null) ? new String[] {csv1, csv2} : new String[] {csv1};

    for (int i = 0; i < tablename.length; i++) {
      Iterator<CSVRecord> records = CSVFormat.RFC4180.parse(new StringReader(csv[i])).iterator();
      List<Map<String, Object>> table = new ArrayList<>();

      CSVRecord headers = records.next();
      while (records.hasNext()) {
        Map<String, Object> object = new LinkedHashMap<>();
        int col = 0;
        for (String s : records.next()) {
          try {
            object.put(headers.get(col), objectMapper.readValue(s, Object.class));
          } catch (Exception e) {
            object.put(headers.get(col), s);
          }
          col++;
        }
        table.add(object);
      }

      res.put(tablename[i], table);
    }

    return res;
  }
}
