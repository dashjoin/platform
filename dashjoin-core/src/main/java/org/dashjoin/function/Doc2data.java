package org.dashjoin.function;

import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.MapUtil;
import org.hsqldb.lib.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * given a URL, parses the contents from JSON, XML, CSV to a json object
 */
public class Doc2data extends Load {

  private static final ObjectMapper om = new ObjectMapper();

  String urlField;

  @Override
  public Object run(Object arg) throws Exception {
    if (arg instanceof Map<?, ?>) {
      urlField = (String) ((Map<?, ?>) arg).get("urlField");
      return super.run(((Map<?, ?>) arg).get("url"));
    } else
      return super.run(arg);
  }

  @Override
  protected Object string(String arg) throws Exception {
    try {
      URL url = new URL(arg);
      FileSystem.checkFileAccess(url);
      try (InputStream in = url.openStream()) {
        String s = IOUtils.toString(in, Charset.defaultCharset());
        return includeURL(parse(s), arg);
      }
    } catch (MalformedURLException textNotUrl) {
      return parse(arg);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  Object includeURL(Object res, String url) {
    if (urlField != null) {
      List list = res instanceof List ? (List) res : Arrays.asList(res);
      for (Object item : list)
        if (item instanceof Map)
          ((Map) item).put(urlField, url);
    }
    return res;
  }

  Object parse(String s) throws Exception {
    try {
      return om.readValue(s, Object.class);
    } catch (Exception notJson) {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new StringInputStream(s));
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

  Object xml(Element node) {
    Map<String, Object> res = new LinkedHashMap<>();
    NamedNodeMap att = node.getAttributes();
    for (int i = 0; i < att.getLength(); i++)
      res.put(att.item(i).getNodeName(), att.item(i).getNodeValue());
    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++)
      if (list.item(i) instanceof Element) {
        Element kid = (Element) list.item(i);
        // TODO: handle lists (child element names are repeated)
        res.put(kid.getNodeName(), xml(kid));
      }
    if (!res.isEmpty())
      return res;

    for (int i = 0; i < list.getLength(); i++)
      if (list.item(i) instanceof Text) {
        Text kid = (Text) list.item(i);
        return kid.getNodeValue();
      }

    return null;
  }

  @Override
  public String getID() {
    return "doc2data";
  }
}
