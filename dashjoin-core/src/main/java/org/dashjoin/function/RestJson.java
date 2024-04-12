package org.dashjoin.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.service.JSONDatabase;
import org.dashjoin.util.MapUtil;
import org.dashjoin.util.Template;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import okhttp3.Call;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * calls a JSON REST service
 */
@SuppressWarnings("rawtypes")
@JsonSchema(required = {"url"}, order = {"url", "username", "password", "method", "contentType",
    "headers", "returnText", "timeoutSeconds"})
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

  /**
   * one of application/x-www-form-urlencoded, application/json
   */
  @JsonSchema(enums = {"application/x-www-form-urlencoded", "application/json"})
  public String contentType;

  @JsonSchema(enums = {"GET", "POST"})
  public String method;

  /**
   * if true, return the result as a string
   */
  @JsonSchema(title = "Return the result as raw text")
  public Boolean returnText;

  @JsonSchema(title = "Optional HTTP timeout (s)")
  public Integer timeoutSeconds;

  transient boolean stream;

  @SuppressWarnings("unchecked")
  protected Call getCall(Object obj) throws Exception {
    Map map = obj instanceof Map ? (Map) obj : MapUtil.of();
    OkHttpClient client = Doc2data.getHttpClient();
    if (this.timeoutSeconds != null)
      client = client.newBuilder().callTimeout(this.timeoutSeconds, TimeUnit.SECONDS).build();
    String sv = map == null ? url : (String) Template.replace(url, map, true);
    Request.Builder request = new Request.Builder().url(sv);
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
        request = request.post(RequestBody.create(null,
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)));
      else {
        Builder fb = new Builder();
        for (Object key : map.keySet())
          fb.add((String) key, (String) map.get(key));
        request = request.post(fb.build());
      }
    else
      request = request.get();
    Call call = client.newCall(request.build());
    return call;
  }

  /**
   * returns the result of the REST call with JSON mapped to a Map / List. If arg is specified,
   * POSTs the arg serialized as JSON. If arg is null, GETs the result.
   */
  @Override
  public Object run(Object obj) throws Exception {
    try (okhttp3.Response response = getCall(obj).execute()) {
      return process(response);
    }
  }

  protected Object process(okhttp3.Response response) throws IOException {
    if (response.code() >= 400) {

      // UI logout out automatically if a 401 is encountered (use 500 instead)
      if (response.code() == 401)
        throw new WebApplicationException(
            Response.status(500).entity("HTTP 401: Unauthorized").build());

      String error = "" + response.body().string();
      try {
        Map<String, Object> s =
            objectMapper.readValue(new ByteArrayInputStream(error.getBytes()), JSONDatabase.tr);
        error = (String) s.get("details");
      } catch (Exception e) {
        // ignore and keep the body
      }
      throw new WebApplicationException(Response.status(response.code()).entity(error).build());
    }

    if (this.stream)
      return response.body().charStream();

    String body = response.body().string();
    return objectMapper.readValue(body, Object.class);
  }

  @Override
  public Class<Object> getArgumentClass() {
    return Object.class;
  }
}
