package org.dashjoin.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.service.ACLContainerRequestFilter;
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
    "headers", "apiKey", "timeoutSeconds"})
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

  @JsonSchema(title = "Use credentials as HTTP API key")
  public Boolean apiKey;

  @JsonSchema(title = "Optional HTTP timeout (s)")
  public Integer timeoutSeconds;

  public List<String> insertCredentials;

  transient boolean stream;

  @SuppressWarnings("unchecked")
  protected Call getCall(Object obj) throws Exception {
    Map map = obj instanceof Map ? (Map) obj : MapUtil.of();
    OkHttpClient client = Doc2data.getHttpClient();
    if (this.timeoutSeconds != null)
      client = client.newBuilder().connectTimeout(0, TimeUnit.SECONDS)
          .readTimeout(0, TimeUnit.SECONDS).writeTimeout(0, TimeUnit.SECONDS)
          .callTimeout(this.timeoutSeconds, TimeUnit.SECONDS).build();
    String sv = map == null ? url : (String) Template.replace(url, map, true);
    Request.Builder request = new Request.Builder().url(sv);
    if (username != null)
      if (apiKey != null && apiKey)
        request = request.header(username, password());
      else
        request = request.header("Authorization", "Basic "
            + Base64.getEncoder().encodeToString((username + ":" + password()).getBytes()));
    if (headers != null)
      for (Entry<String, String> e : headers.entrySet())
        request = request.header(e.getKey(), e.getValue());
    if (contentType != null)
      request = request.header("Content-Type", contentType);
    if ("POST".equals(method))
      if ("application/json".equals(contentType)) {
        if (insertCredentials != null)
          obj = replace(obj, insertCredentials);
        request = request.post(RequestBody.create(null,
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)));
      } else {
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

  @SuppressWarnings("unchecked")
  public Object replace(Object o, List<String> insertCredentials) throws Exception {
    if (o instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) o;
      Map<String, Object> copy = new LinkedHashMap<>();
      for (Entry<String, Object> e : map.entrySet())
        if (insertCredentials.contains(e.getKey()) && e.getValue() instanceof String) {
          copy.put(e.getKey(), cred((String) e.getValue()));
        } else
          copy.put(e.getKey(), replace(e.getValue(), insertCredentials));
      return copy;
    }
    if (o instanceof List) {
      List<Object> copy = new ArrayList<>();
      for (Object item : (List<Object>) o)
        copy.add(replace(item, insertCredentials));
      return copy;
    }
    return o;
  }

  String cred(String name) throws Exception {
    try {
      AbstractConfigurableFunction f =
          services != null ? services.getConfig().getFunction(name) : null;
      if (f instanceof Credentials) {
        ACLContainerRequestFilter.check(sc, f);
        f.init(sc, services, expressionService, readOnly);
        Credentials c = (Credentials) f;
        if (c.apiKey != null && c.apiKey)
          return f.password();
        else
          return "Basic "
              + Base64.getEncoder().encodeToString((c.username + ":" + f.password()).getBytes());
      }
    } catch (IllegalArgumentException notCredential) {
      // ignore
    }
    return name;
  }
}
