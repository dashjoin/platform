package org.dashjoin.function;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.util.Escape;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.Template;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * calls a JSON REST service
 */
@SuppressWarnings("rawtypes")
@JsonSchema(required = {"url"},
    order = {"url", "username", "password", "method", "contentType", "headers", "cache"})
public class RestJson extends AbstractConfigurableFunction<Object, Object> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * service URL
   */
  @JsonSchema(style = {"width", "400px"})
  public String url;

  /**
   * optional HTTP basic authentication user name
   */
  @JsonSchema(style = {"width", "400px"})
  public String username;

  /**
   * optional HTTP basic authentication password
   */
  @JsonSchema(widget = "password", style = {"width", "400px"})
  public String password;

  public Map<String, String> headers;

  public Boolean cache;

  /**
   * one of application/x-www-form-urlencoded, application/json
   */
  @JsonSchema(enums = {"application/x-www-form-urlencoded", "application/json"})
  public String contentType;

  @JsonSchema(enums = {"GET", "POST"})
  public String method;

  /**
   * returns the result of the REST call with JSON mapped to a Map / List. If arg is specified,
   * POSTs the arg serialized as JSON. If arg is null, GETs the result.
   */
  @Override
  public Object run(Object obj) throws Exception {

    File file = null;
    if (Boolean.TRUE.equals(cache)) {
      String filename =
          URLEncoder.encode(objectMapper.writeValueAsString(obj), Charset.defaultCharset());
      file = FileSystem.getUploadFile("upload/" + ID + "/" + filename + ".json");
      if (file.exists())
        try (InputStream in = new FileInputStream(file)) {
          return IOUtils.toString(in, Charset.defaultCharset());
        }
      else
        file.getParentFile().mkdirs();
    }

    Object res = runInternal(obj);
    if (file != null)
      objectMapper.writeValue(file, res);

    return res;
  }

  @SuppressWarnings("unchecked")
  Object runInternal(Object obj) throws Exception {
    Map map = obj instanceof Map ? (Map) obj : MapUtil.of();
    HttpClient client = HttpClient.newBuilder().build();
    String sv = map == null ? url : (String) Template.replace(url, map, true);
    Builder request = HttpRequest.newBuilder().uri(new URI(sv));
    if (username != null)
      request = request.header("Authorization",
          "Basic " + Base64.getEncoder().encodeToString((username + ":" + password()).getBytes()));
    if (headers != null)
      for (Entry<String, String> e : headers.entrySet())
        request = request.header(e.getKey(), e.getValue());
    if (contentType != null)
      request = request.header("Content-Type", contentType);
    if ("POST".equals(method))
      if ("application/json".equals(contentType))
        request = request.POST(BodyPublishers
            .ofString(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)));
      else {
        String form = map == null ? ""
            : (String) map.keySet().stream().map(key -> key + "=" + Escape.form("" + map.get(key)))
                .collect(Collectors.joining("&"));
        request = request.POST(BodyPublishers.ofString(form));
      }

    HttpResponse<?> response = client.send(request.build(), BodyHandlers.ofString());

    if (response.statusCode() >= 400) {
      String error = "" + response.body();
      try {
        Map<String, Object> s =
            objectMapper.readValue(new ByteArrayInputStream(error.getBytes()), JSONDatabase.tr);
        error = (String) s.get("details");
      } catch (Exception e) {
        // ignore and keep the body
      }
      throw new WebApplicationException(
          Response.status(response.statusCode()).entity(error).build());
    }

    String body = response.body().toString();
    if (body.isEmpty())
      return "";

    if (body.trim().startsWith("{") || body.trim().startsWith("["))
      return objectMapper.readValue(body, Object.class);
    else
      return body;
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }
}
