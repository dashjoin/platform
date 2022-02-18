package org.dashjoin.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileResource;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.MapUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.java.Log;

/**
 * given a URL, parses the contents from JSON, XML, CSV to a json object
 */
@Log
public class Doc2data extends AbstractFunction<String, Object> {

  private static final ObjectMapper om = new ObjectMapper();

  long MAX_SIZE = 100 * 1024 * 1024;

  @Override
  public Object run(String arg) throws Exception {

    try {
      try {
        Object res = parseJsonDoc(arg);
        return res;
      } catch (Throwable e) {
        // ignore
        log.info("Error parsing JSON: " + e + " - fallback to regular parser");
      }

      URL url = FileSystem.getUploadURL(arg);

      if (new java.io.File(url.getPath()).length() > MAX_SIZE)
        throw new RuntimeException("Data file too large: " + url);

      try (InputStream in = url.openStream()) {
        String s = IOUtils.toString(in, Charset.defaultCharset());
        return parse(s);
      }
    } catch (MalformedURLException textNotUrl) {
      return parse(arg);
    }
  }

  Object parse(String s) throws Exception {
    try {
      return om.readValue(s, Object.class);
    } catch (Exception notJson) {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(s.getBytes()));
        return MapUtil.of(doc.getDocumentElement().getNodeName(), xml(doc.getDocumentElement()));
      } catch (Exception notXML) {
        List<Object> res = new ArrayList<>();
        Iterator<CSVRecord> records = CSVFormat.RFC4180.parse(new StringReader(s)).iterator();
        CSVRecord headers = records.next();
        while (records.hasNext()) {
          Map<String, Object> object = new HashMap<>();
          int col = 0;
          for (String r : records.next()) {
            object.put(headers.get(col), r);
            col++;
          }
          res.add(object);
        }
        return res;
      }
    }
  }

  @SuppressWarnings("unchecked")
  Object xml(Element node) {
    Map<String, Object> res = new LinkedHashMap<>();
    NamedNodeMap att = node.getAttributes();
    for (int i = 0; i < att.getLength(); i++)
      res.put(att.item(i).getNodeName(), att.item(i).getNodeValue());
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++)
      if (list.item(i) instanceof Element) {
        Element kid = (Element) list.item(i);
        Object old = res.get(kid.getNodeName());
        if (old == null)
          res.put(kid.getNodeName(), xml(kid));
        else if (old instanceof List)
          ((List<Object>) old).add(xml(kid));
        else {
          List<Object> tmp = new ArrayList<>();
          tmp.add(old);
          tmp.add(xml(kid));
          res.put(kid.getNodeName(), tmp);
        }
      }
    if (!res.isEmpty())
      return cleanArrays(res);

    for (int i = 0; i < list.getLength(); i++)
      if (list.item(i) instanceof Text) {
        Text kid = (Text) list.item(i);
        return kid.getNodeValue();
      }

    return null;
  }

  /**
   * in array of object fields, make sure all "types" are consistent (a single value might have to
   * be converted to an array)
   */
  @SuppressWarnings("unchecked")
  Object cleanArrays(Map<String, Object> res) {
    for (String field : arrayFields(res)) {
      Set<String> arrayFields = new HashSet<>();

      // sweep 1: collect all array fields
      for (Object i : (List<Object>) res.get(field)) {
        if (i instanceof Map<?, ?>)
          arrayFields.addAll(arrayFields((Map<String, Object>) i));
      }

      // sweep 2: add array if necessary
      for (Object i : (List<Object>) res.get(field)) {
        if (i instanceof Map<?, ?>) {
          for (Entry<String, Object> e : ((Map<String, Object>) i).entrySet()) {
            if (arrayFields.contains(e.getKey()))
              if (!(e.getValue() instanceof List))
                e.setValue(Arrays.asList(e.getValue()));
          }
        }
      }
    }
    return res;
  }

  Set<String> arrayFields(Map<String, Object> res) {
    Set<String> arrayFields = new HashSet<>();
    for (Entry<String, Object> e : res.entrySet())
      if (e.getValue() instanceof List<?>)
        arrayFields.add(e.getKey());
    return arrayFields;
  }

  Object parseJsonDoc(String file) throws Throwable {

    FileResource fr = FileResource.of(file);
    if (fr.size != null) {
      if (fr.size > MAX_SIZE)
        throw new Exception("Data file too large: " + fr);
    } else if (FileSystem.getUploadFile(fr.file).length() > MAX_SIZE)
      throw new Exception("Data file too large: " + fr);

    try (InputStream in = FileResource.of(file).getInputStream()) {
      return parseJson(in, true);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  Object parseJson(InputStream in, boolean flattenArrays)
      throws IOException, JsonParseException, JsonMappingException {
    JsonFactory jf =
        JsonFactory.builder().configure(StreamReadFeature.AUTO_CLOSE_SOURCE, false).build();
    ObjectMapper _om = new ObjectMapper(jf);

    JsonParser jp = _om.getFactory().createParser(in);

    try (jp) {
      List<Object> res = new ArrayList<>();
      Object obj;
      try {
        while ((obj = _om.readValue(jp, Object.class)) != null) {

          if (flattenArrays && obj instanceof List)
            res.addAll((List) obj);
          else
            res.add(obj);

          if (readOnly)
            break;
        }
      } catch (MismatchedInputException | JsonEOFException ex) {
        // EOF
      }
      // System.err.println("JSON stream " + file + " : " + res.size());
      if (res.isEmpty())
        return null;
      if (res.size() == 1)
        return res.get(0);
      return res;
    }
  }

  @Override
  public String getID() {
    return "doc2data";
  }

  @Override
  public String getType() {
    return "read";
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }
}
