package org.dashjoin.expression.jsonatajs;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.jboss.logmanager.Level;
import com.api.jsonata4java.expressions.functions.Function;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

/**
 * JSONata reference implementation wrapper.
 * 
 * Uses GraalJS to call the jsonata Javascript reference impl.
 * 
 * Expects the node_modules/jsonata folder in classloader resource folder "jsonata".
 * 
 * @author uli
 */
@Log
public class JsonataJS {

  protected Context cx;
  protected Value jsonParse;
  protected Value jsonata;
  protected Value stringify;

  static ThreadLocal<JsonataJS> instance = new ThreadLocal<>();

  static boolean logged = false;

  /**
   * Returns instance of JsonataJS
   * 
   * Creates a separate instance per thread to enable multi-threaded Javascript execution
   * 
   * Null when an error occurs (i.e. required classes not on classpath, Javascript engine to bundled
   * in JVM, etc.)
   * 
   * @return Instance, or null if not available
   */
  public static JsonataJS getInstance() {
    JsonataJS inst = instance.get();
    if (inst != null)
      return inst;

    try {
      instance.set(new JsonataJS());
    } catch (Throwable e) {
      if (!logged) {
        logged = true;
        log.log(Level.WARN, "Fallback to Jsonata4Java - cannot create Jsonata reference instance",
            e);
      }
      return null;
    }
    return getInstance();
  }

  public JsonataJS() throws Exception {
    cx = createContext();
    jsonParse = cx.eval("js", "JSON.parse");
    stringify = cx.eval("js", "JSON.stringify");

    // We have node_modules/jsonata/* in resource folder jsonata/
    // 1) get version from package.json
    String jsonataVersion = null;
    try (InputStream jin =
        JsonataJS.class.getClassLoader().getResourceAsStream("jsonata/package.json")) {
      jsonataVersion = (String) new ObjectMapper().readValue(jin, Map.class).get("version");
    }
    if (!logged) {
      logged = true;
      log.info("Using JSONata reference version=" + jsonataVersion);
    }

    // 2) parse jsonata.js and set reference to variable jsonata
    String jsonataJs = null;
    try (InputStream jin =
        JsonataJS.class.getClassLoader().getResourceAsStream("jsonata/jsonata.js")) {
      jsonataJs = IOUtils.toString(jin, StandardCharsets.UTF_8);
    }
    cx.eval("js", jsonataJs);
    jsonata = cx.getBindings("js").getMember("jsonata");
    if (jsonata == null)
      throw new RuntimeException("Jsonata reference implementation not valid (jsonata.js)");
  }

  Context createContext() {
    // Fix to load JIT compiler
    // from
    // https://github.com/alukin/polyglot-test-quarkus/blob/master/src/main/java/ua/cn/al/tst/GreetingResource.java
    // Issue:
    // https://github.com/quarkusio/quarkus/issues/8035
    ClassLoader quarkusClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());

      // Configure custom type mappings
      HostAccess access = HostAccess.newBuilder(HostAccess.EXPLICIT)
          // map to Long if possible
          .targetTypeMapping(Long.class, Object.class, null, v -> v)
          // map to List (fix for #139)
          .targetTypeMapping(Value.class, Object.class, (v) -> v.hasArrayElements(),
              (v) -> v.as(List.class))
          .build();

      return Context.newBuilder().allowHostAccess(access).build();
    } finally {
      Thread.currentThread().setContextClassLoader(quarkusClassLoader);
    }
  }

  public Value jsonata(String expression) {
    return jsonata.execute(expression);
  }

  static ThreadLocal<Value> bindings = new ThreadLocal<>();
  static ThreadLocal<Map<String, Function>> functions = new ThreadLocal<>();

  public void initBindings(Map<String, Function> functions) {
    JsonataJS.functions.set(functions);
    String s = "(function(x){ return {";
    for (Map.Entry<String, Function> e : functions.entrySet()) {
      String k = e.getKey();
      s += "'" + k.substring(1) + "':(...args)=>x.call('" + k + "',args),";
    }
    s = s.substring(0, s.length() - 1);
    s += "}})";

    if (log.isLoggable(Level.DEBUG))
      log.log(Level.DEBUG, s);
    // System.out.println("jsonata function bindings = " + s);
    // bindings =
    // cx.eval("js", "(function(x){ return {'pi':()=>x.pi()}})").execute(cx.asValue(this));
    Value bindings = cx.eval("js", s).execute(cx.asValue(this));
    JsonataJS.bindings.set(bindings);
  }

  public Value getBindings() {
    return bindings.get();
  }

  public static ThreadLocal<Object[]> args = new ThreadLocal<>();

  public static Object[] getArgs() {
    return args.get();
  }

  /**
   * Exception type we throw to Javascript
   * 
   * jsonata code expects two fields (position + token), otherwise there'll be a runtime exception
   */
  public static class JSException extends Exception {
    private static final long serialVersionUID = 1L;
    @HostAccess.Export
    public Integer position;
    @HostAccess.Export
    public String token;

    public JSException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * Callback from Javascript.
   * 
   * This will call any user-defined function. Used by the bindings callback mechanism
   * 
   * @param function
   * @param args
   * @return
   * @throws JSException
   */
  @HostAccess.Export
  public Object call(String function, Object... args) throws JSException {

    if (log.isLoggable(Level.DEBUG))
      log.info("Calling fn " + function + " args=" + Arrays.asList(args));

    Function fn = functions.get().get(function);
    JsonataJS.args.set(args);
    try {
      Object res = fn.invoke(null, null);

      if (log.isLoggable(Level.DEBUG))
        log.info("Calling fn " + function + " res=" + res);

      if (res instanceof JsonNode) {
        res = jsonParse.execute(res.toString());
      }
      return res;
    } catch (Throwable ex) {
      if (log.isLoggable(Level.DEBUG))
        log.info("Caught exception: " + ex);
      throw new JSException(ex);
    } finally {
      JsonataJS.args.remove();
    }
  }

  /**
   * Evaluates the jsonata expr, returns parsed expression
   * 
   * @param jsonata
   * @param data
   * @return
   */
  public Value evaluate(Value jsonata, Object data) {
    return evaluate(jsonata, data, bindings.get());
  }

  /**
   * Evaluates the jsonata expr, returns parsed expression
   * 
   * @param jsonata
   * @param data
   * @param bindings
   * @return
   */
  public Value evaluate(Value jsonata, Object data, Value bindings) {
    // System.out.println("jsonata evaluate");
    return jsonata.getMember("evaluate").execute(data, bindings);
  }

  /**
   * Parses the given string into JS JSON value
   * 
   * @param s
   * @return
   */
  public Value jsonParse(String s) {
    return jsonParse.execute(s);
  }

  public Value jsonStringify(Value v) {
    return stringify.execute(v);
  }

  public static void main(String[] args) throws Throwable {
    JsonataJS js = new JsonataJS();
    System.out.println(js.jsonata);
    System.out.println(js.jsonata("$"));
    System.out.println(js.evaluate(js.jsonata("$sum($)"), js.jsonParse("[1,2,3,4]")));
  }
}
