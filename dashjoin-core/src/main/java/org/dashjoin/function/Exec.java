package org.dashjoin.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.Home;
import org.dashjoin.util.MapUtil;
import org.w3c.dom.Document;
import org.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * runs a script in the upload folder
 */
@SuppressWarnings("rawtypes")
public class Exec extends AbstractVarArgFunction<Exec.Result> {

  private static ObjectMapper om = new ObjectMapper();

  public class Result {
    public Object out;
    public String err;
    public int code;
  }

  @Override
  public Result run(List arg) throws Exception {
    String script = (String) arg.get(0);
    String args = (String) arg.get(1);
    String format = (String) arg.get(2);

    if (script == null)
      throw new IllegalArgumentException("Syntax: $exec(executable, arguments, [json, xml, csv])");

    // get executable location relative to home dir and make sure it is in app/bin
    File sc = Home.get().getFile(script);
    FileSystem.checkFileAccess(sc, "bin");

    // optionally concat the bin/script with the arguments
    String line = sc.getAbsolutePath() + (args == null ? "" : " " + args);

    // prepare exec
    CommandLine cmdLine = CommandLine.parse(line);
    DefaultExecutor executor = new DefaultExecutor();
    OutputStream out = new ByteArrayOutputStream();
    OutputStream err = new ByteArrayOutputStream();
    executor.setStreamHandler(new PumpStreamHandler(out, err));

    // run and copy results into Result object
    Result res = new Result();
    res.code = executor.execute(cmdLine);
    res.out = out.toString();
    res.err = err.toString();
    if ("json".equals(format))
      res.out = om.readValue((String) res.out, Object.class);
    if ("xml".equals(format)) {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new ByteArrayInputStream(((String) res.out).getBytes()));
      res.out = MapUtil.of(doc.getDocumentElement().getNodeName(),
          Doc2data.xml(doc.getDocumentElement()));
    }
    if ("csv".equals(format)) {
      List<Object> l = new ArrayList<>();
      Iterator<CSVRecord> records = CSVFormat.RFC4180
          .parse(new InputStreamReader(new ByteArrayInputStream(((String) res.out).getBytes())))
          .iterator();
      CSVRecord headers = records.next();
      while (records.hasNext()) {
        Map<String, Object> object = new LinkedHashMap<>();
        int col = 0;
        for (String r : records.next()) {
          if (col < headers.size())
            object.put(headers.get(col), r);
          col++;
        }
        l.add(object);
      }
      res.out = l;
    }
    if ("yaml".equals(format))
      res.out = new Yaml().load(new ByteArrayInputStream(((String) res.out).getBytes()));

    return res;
  }

  @Override
  public String getID() {
    return "exec";
  }

  @Override
  public String getType() {
    return "write";
  }

  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, String.class, String.class);
  }
}
