package org.dashjoin.expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Map.Entry;
import org.dashjoin.expression.jsonatajs.JsonataJS;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.function.AbstractVarArgFunction;
import org.dashjoin.function.FunctionService;
import org.dashjoin.function.JobStatus;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.ACLContainerRequestFilter;
import org.dashjoin.service.Data;
import org.dashjoin.service.Manage;
import org.dashjoin.service.QueryEditor.QueryDatabase;
import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.jboss.logmanager.Level;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ExpressionsVisitor;
import com.api.jsonata4java.expressions.ParseException;
import com.api.jsonata4java.expressions.functions.FunctionBase;
import com.api.jsonata4java.expressions.generated.MappingExpressionParser.Function_callContext;
import com.dashjoin.jsonata.Functions;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.Fn7;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.dashjoin.jsonata.Jsonata.JLambda;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.java.Log;

/**
 * REST API for expression evaluation
 */
@Path(Services.REST_PREFIX + "expression")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@SuppressWarnings("serial")
@Log
public class ExpressionService {

  private static final ObjectMapper om =
      new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  @Inject
  Services services;

  @Inject
  Data data;

  @Inject
  FunctionService function;

  @Inject
  Manage manage;

  public Manage getManage() {
    return manage;
  }

  public Data getData() {
    return data;
  }

  /**
   * combine expression and data into a single post parameter
   */
  public static class ExpressionAndData {
    @Schema(title = "jsonata expression", example = "$.x")
    public String expression;
    @Schema(example = "{\"x\":1, \"y\":2}")
    public Object data;
  }

  @POST
  @Path("/")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public JsonNode resolve(@Context SecurityContext sc, ExpressionAndData e) throws Exception {
    try {
      return jsonata(sc, e.expression, o2j(e.data), false);
    } catch (PolyglotException ex) {
      throw convert(ex);
    }
  }

  @GET
  @Path("/{expression}")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public JsonNode resolveCached(@Context SecurityContext sc,
      @PathParam("expression") String expression) throws Exception {
    ExpressionAndData e = om.readValue(expression, ExpressionAndData.class);
    try {
      return jsonata(sc, e.expression, o2j(e.data), false);
    } catch (PolyglotException ex) {
      throw convert(ex);
    }
    // return jsonata(sc, expression.expression, o2j(expression.data), false);
  }

  /**
   * when logged by JAX-RS, PolyglotException does not show exceptions in JSONata functions
   */
  Exception convert(PolyglotException e) {
    if (e.isHostException()) {
      Throwable host = e.asHostException();
      if (host != null) {
        Throwable wrapped = host.getCause();
        if (wrapped != null) {
          Throwable real = wrapped.getCause();
          if (real instanceof PolyglotException && real != e)
            return convert((PolyglotException) real);
          if (real instanceof Exception)
            return (Exception) real;
        }
      }
    }
    return e;
  }

  public Object resolve(SecurityContext sc, String expression, Object data) throws Exception {
    return j2o(jsonata(sc, expression, o2j(data), false));
  }

  public Object resolve(SecurityContext sc, String expression, Object data, boolean readOnly)
      throws Exception {
    return j2o(jsonata(sc, expression, o2j(data), readOnly));
  }

  public ParsedExpression prepare(SecurityContext sc, String expression)
      throws ParseException, IOException {
    return parse(sc, expression, false);
  }

  public Object resolve(ParsedExpression _expr, Object data) throws Exception {

    if (_expr instanceof DJsonataParsedExpression) {
      DJsonataParsedExpression expr = (DJsonataParsedExpression)_expr;
      Object res = expr.expr.evaluate(data, expr.bindings);
      return res; //j2o(res);
    }

    if (_expr instanceof JsonataJSParsedExpression) {
      Value expr = ((JsonataJSParsedExpression) _expr).getExpression();
      Value bindings = ((JsonataJSParsedExpression) _expr).getBindings();
      JsonataJS jsonataJs = JsonataJS.getInstance(); // .evaluate(expr, bindings);
      Value dataVal = data != null ? jsonataJs.jsonParse(o2j(data).toString()) : null;
      Value res = jsonataJs.evaluate(expr, dataVal, bindings);
      res = jsonataJs.jsonStringify(res);

      if (log.isLoggable(Level.DEBUG)) {
        log.info("jsonata res = " + res);
      }

      String json = res.toString();
      // return j2o(om.readValue(json, JsonNode.class));
      return om.readValue(json, Object.class);
    }

    Expressions expr = ((Jsonata4JavaParsedExpression) _expr).getExpression();
    try {
      return j2o(expr.evaluate(o2j(data)));
    } catch (WrappedException e) {
      throw (Exception) e.getCause();
    }
  }

  /**
   * Parsed Jsonata expression
   */
  public interface ParsedExpression {
  }

  /**
   * Jsonata4Java parsed expression
   * 
   * @author uli
   */
  public static class Jsonata4JavaParsedExpression implements ParsedExpression {
    String sexpr;
    Expressions expr;

    Jsonata4JavaParsedExpression(String sexpr, Expressions expr) {
      this.sexpr = sexpr;
      this.expr = expr;
    }

    public Expressions getExpression() {
      return this.expr;
    }

    @Override
    public String toString() {
      return sexpr;
    }
  }

  /**
   * JsonataJS parsed expression
   * 
   * @author uli
   */
  public static class JsonataJSParsedExpression implements ParsedExpression {
    String sexpr;
    Value expr, bindings;

    JsonataJSParsedExpression(String sexpr, Value expr, Value bindings) {
      this.sexpr = sexpr;
      this.expr = expr;
      this.bindings = bindings;
    }

    public Value getExpression() {
      return this.expr;
    }

    public Value getBindings() {
      return this.bindings;
    }

    @Override
    public String toString() {
      return sexpr;
    }
  }

  /**
   * Parsed Dashjoin JSonata expression
   */
  public static class DJsonataParsedExpression implements ParsedExpression {
    String sexpr;
    Jsonata expr;
    Jsonata.Frame bindings;

    DJsonataParsedExpression(String sexpr, Jsonata expr, Jsonata.Frame bindings) {
      this.sexpr = sexpr;
      this.expr = expr;
      this.bindings = bindings;
    }

    public Jsonata getExpression() {
      return this.expr;
    }

    public Jsonata.Frame getBindings() {
      return this.bindings;
    }

    @Override
    public String toString() {
      return sexpr;
    }
  }

  ParsedExpression parse(SecurityContext sc, String expression, boolean readOnly)
      throws ParseException, IOException {

    Map<String, FunctionBase> fns = getJsonataFunctions(sc, readOnly);

    if (djsonata) {
      Jsonata parsed = Jsonata.jsonata(expression);
      try {
        return new DJsonataParsedExpression(expression, parsed, getDjsonataFunctions(parsed, sc, readOnly));
      } catch (Exception e) {
       throw new RuntimeException(e);
      }
    }

    if (jsonataJs) {
      // Execute jsonata-js reference implementation
      JsonataJS jsonataJs = JsonataJS.getInstance();
      jsonataJs.initBindings(getJsonataFunctions(sc, readOnly));
      Value expr = jsonataJs.jsonata(expression);
      return new JsonataJSParsedExpression(expression, expr, jsonataJs.getBindings());
    }

    Expressions expr = Expressions.parse(expression);
    for (Map.Entry<String, FunctionBase> e : fns.entrySet()) {
      expr.getEnvironment().setJsonataFunction(e.getKey(), e.getValue());
    }
    return new Jsonata4JavaParsedExpression(expression, expr);
  }

  /**
   * Returns the platform functions with JFunction call wrappers
   * 
   * @param expr
   * @param sc
   * @param readOnly
   * @return
   * @throws Exception
   */
  Jsonata.Frame getDjsonataFunctions(Jsonata expr, SecurityContext sc, boolean readOnly) throws Exception {
    Jsonata.Frame bindings = expr.createFrame();

    for (final Entry<String, FunctionBase> e : getJsonataFunctions(sc, readOnly).entrySet()) {
      final String name = e.getKey().substring(1); // Strip leading "$"
      final FunctionBase fn = e.getValue();

      JFunctionCallable fc = new JFunctionCallable() {

        @Override
        public Object call(Object input, List args) throws Throwable {
          JsonataJS.args.set(args.toArray());
          JsonNode jn = fn.invoke(null, null);
          return j2o(jn);
        }

      };

      bindings.bind( name, new Jsonata.JFunction(fc, null)); 
    }
    return bindings;
  }

  Map<String, FunctionBase> getJsonataFunctions(SecurityContext sc, boolean readOnly)
      throws ParseException, IOException {
    // TODO: combine these with the service loader functions
    // keep in sync with org.dashjoin.service.Manage.getFunctions()
    HashMap<String, FunctionBase> res = new HashMap<>();
    res.put("$all", new All(sc, readOnly));
    res.put("$read", new Read(sc, readOnly));
    res.put("$create", new Create(sc, readOnly));
    res.put("$update", new Update(sc, readOnly));
    res.put("$traverse", new Traverse(sc, readOnly));
    res.put("$delete", new Delete(sc, readOnly));
    res.put("$query", new Query(sc, readOnly));
    res.put("$queryGraph", new QueryGraph(sc, readOnly));
    res.put("$adHocQuery", new AdHocQuery(sc, readOnly));
    res.put("$call", new Call(sc, readOnly));
    res.put("$incoming", new Incoming(sc));

    for (org.dashjoin.function.Function<?, ?> f : ServiceLoader
        .load(org.dashjoin.function.Function.class)) {
      if (!(f instanceof AbstractConfigurableFunction<?, ?>)) {
        ((AbstractFunction<?, ?>) f).init(sc, services, this, readOnly);
        res.put("$" + ((AbstractFunction<?, ?>) f).getID(), new FunctionBase() {

          @Override
          public int getMaxArgs() {
            return 1;
          }

          @Override
          public int getMinArgs() {
            return 0;
          }

          @Override
          public String getSignature() {
            return "<j:j>";
          }

          @SuppressWarnings({"unchecked", "unused"})
          @Override
          public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
            try {
              if (f instanceof AbstractVarArgFunction) {
                AbstractVarArgFunction<Object> vf = (AbstractVarArgFunction<Object>) f;
                List<Object> args = new ArrayList<>();
                int index = 0;
                for (Class<?> c : vf.getArgumentClassList()) {
                  if (index < getArgumentCountEx(ctx))
                    args.add(j2o(getValuesListExpression(v, ctx, index)));
                  else
                    args.add(null);
                  index++;
                }
                return o2j(function.callInternal(sc, (AbstractFunction<Object, Object>) f, args,
                    readOnly));
              } else
                return o2j(function.callInternal(sc, (AbstractFunction<Object, Object>) f,
                    getArgumentCountEx(ctx) == 0 ? null : j2o(getValuesListExpression(v, ctx, 0)),
                    readOnly));
            } catch (Exception e) {
              throw new WrappedException(e);
            }
          }
        });
      }
    }
    return res;
  }

  /**
   * Checks if the JsonataJS instance can be created
   */
  static boolean canUseJsonataReference() {
    return false; //JsonataJS.getInstance() != null;
  }

  /**
   * True if reference impl via JsonataJS is active
   */
  boolean jsonataJs = canUseJsonataReference();

  /**
   * True if Dashjoin Jsonata reference is active
   */
  boolean djsonata = true;

  /**
   * wrapper around new evaluate(expression, data) that defines our custom function
   */
  JsonNode jsonata(SecurityContext sc, String expression, JsonNode data, boolean readOnly)
      throws Exception {

    if (expression == null || expression.isBlank())
      return NullNode.getInstance();

    if (data != null) {
      List<String> objectIds = new ArrayList<>();
      if (get(data, "pk1") != null)
        objectIds.add(get(data, "pk1"));
      if (get(data, "pk2") != null)
        objectIds.add(get(data, "pk2"));
      if (get(data, "pk3") != null)
        objectIds.add(get(data, "pk3"));
      if (get(data, "pk4") != null)
        objectIds.add(get(data, "pk4"));
      if (get(data, "database") != null && get(data, "table") != null && objectIds.size() > 0)
        try {
          Map<String, Object> read =
              this.data.read(sc, get(data, "database"), get(data, "table"), objectIds);

          if (read != null && "config".equals(get(data, "database"))
              && "dj-function".equals(get(data, "table")))
            JobStatus.set(read.get("status"), read.get("start"), read.get("end"));
          else
            JobStatus.reset();

          ((ObjectNode) data).set("value", o2j(read));
        } catch (Exception e) {
          throw new IOException(e);
        }
    }

    if (djsonata) {
      try {
        Jsonata expr = Jsonata.jsonata(expression);
        Jsonata.Frame bindings = getDjsonataFunctions(expr, sc, readOnly);
        String str = data!=null ? data.toString() : null;
        //System.err.println("Data "+str);

        Object res = expr.evaluate(str!=null ? com.dashjoin.jsonata.json.Json.parseJson(str) : null, bindings);
        //System.err.println("Expr "+expression+" Result "+res);
        return o2j(res); // Functions.string(res, false);
      } catch (Exception ex) {
        log.warning("Error in expression "+expression);
        throw ex;
      }
    }

    if (jsonataJs) {
      // Execute jsonata-js reference implementation
      JsonataJS jsonataJs = JsonataJS.getInstance();
      jsonataJs.initBindings(getJsonataFunctions(sc, readOnly));
      Value expr = jsonataJs.jsonata(expression);
      Object dataIn = data; // != null ? j2o(data) : null;

      if (log.isLoggable(Level.DEBUG)) {
        log.info("jsonata expr = " + expression);
        log.info("jsonata data = " + dataIn);
      }

      Value dataVal = dataIn != null ? jsonataJs.jsonParse(dataIn.toString()) : null;
      Value res = jsonataJs.evaluate(expr, dataVal);
      res = jsonataJs.jsonStringify(res);

      if (log.isLoggable(Level.DEBUG)) {
        log.info("jsonata res = " + res);
      }

      String json = res.toString();

      if ("undefined".equals(json))
        return null;

      return om.readValue(json, JsonNode.class);
    }

    Jsonata4JavaParsedExpression expr =
        (Jsonata4JavaParsedExpression) parse(sc, expression, readOnly);

    try {
      return expr.getExpression().evaluate(data);
    } catch (WrappedException e) {
      throw (Exception) e.getCause();
    }
  }

  String get(JsonNode data, String field) {
    JsonNode res = data.get(field);
    if (res == null)
      return null;
    else
      return res.textValue();
  }

  /**
   * data.all(database, table)
   */
  public class All extends FunctionBase {
    SecurityContext sc;
    boolean readOnly;

    public All(SecurityContext sc, boolean readOnly) {
      this.readOnly = readOnly;
      this.sc = sc;
    }

    @Override
    public int getMaxArgs() {
      return 7;
    }

    @Override
    public int getMinArgs() {
      return 2;
    }

    @Override
    public String getSignature() {
      return "<j:ss>";
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 2)
        throw new RuntimeException("Arguments required: $all(database, table)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");

      Integer offset =
          getArgumentCountEx(ctx) < 3 || getValuesListExpression(v, ctx, 2) == null ? null
              : getValuesListExpression(v, ctx, 2).asInt();
      Integer limit =
          getArgumentCountEx(ctx) < 4 || getValuesListExpression(v, ctx, 3) == null ? null
              : getValuesListExpression(v, ctx, 3).asInt();
      String sort = getArgumentCountEx(ctx) < 5 || getValuesListExpression(v, ctx, 4) == null ? null
          : getValuesListExpression(v, ctx, 4).asText();
      boolean descending =
          getArgumentCountEx(ctx) < 6 ? false : getValuesListExpression(v, ctx, 5).asBoolean();
      @SuppressWarnings("unchecked")
      Map<String, Object> arguments = getArgumentCountEx(ctx) < 7 ? null
          : (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 6));

      try {
        return o2j(data.all(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(), offset, limit, sort, descending,
            arguments));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.read(database, table, pk1)
   */
  public class Read extends FunctionBase {
    SecurityContext sc;
    boolean readOnly;

    public Read(SecurityContext sc, boolean readOnly) {
      this.readOnly = readOnly;
      this.sc = sc;
    }

    @Override
    public int getMaxArgs() {
      return 3;
    }

    @Override
    public int getMinArgs() {
      return 3;
    }

    @Override
    public String getSignature() {
      return "<j:sss>";
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 3)
        throw new RuntimeException("Arguments required: $read(database, table, pk1)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      try {
        return o2j(data.read(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(),
            getValuesListExpression(v, ctx, 2).asText()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.create(database, table, pk1)
   */
  public class Create extends Read {

    public Create(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 3)
        throw new RuntimeException("Arguments required: $create(database, table, object)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      try {
        if (readOnly)
          return null;
        return o2j(data.createInternal(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(),
            (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 2))));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.update(database, table, pk1, object)
   */
  public class Update extends Read {

    public Update(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @Override
    public int getMaxArgs() {
      return 4;
    }

    @Override
    public int getMinArgs() {
      return 4;
    }

    @Override
    public String getSignature() {
      return "<j:ssss>";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 4)
        throw new RuntimeException("Arguments required: $update(database, table, pk1, object)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      if (getValuesListExpression(v, ctx, 3) == null)
        throw new RuntimeException("object cannot be null");
      try {
        if (readOnly)
          return null;
        data.update(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(),
            getValuesListExpression(v, ctx, 2).asText(),
            (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 3)));
        return null;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.traverse(database, table, pk1, fk)
   */
  public class Traverse extends Update {

    public Traverse(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 4)
        throw new RuntimeException("Arguments required: $traverse(database, table, pk1, fk)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      if (getValuesListExpression(v, ctx, 3) == null)
        throw new RuntimeException("fk cannot be null");
      try {
        return o2j(data.traverse(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(),
            getValuesListExpression(v, ctx, 2).asText(),
            getValuesListExpression(v, ctx, 3).asText()));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.delete(database, table, pk1)
   */
  public class Delete extends Read {

    public Delete(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 3)
        throw new RuntimeException("Arguments required: $delete(database, table, pk1)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      try {
        if (readOnly)
          return null;
        if (getArgumentCountEx(ctx) == 4)
          data.delete(sc, getValuesListExpression(v, ctx, 0).asText(),
              getValuesListExpression(v, ctx, 1).asText(),
              getValuesListExpression(v, ctx, 2).asText(),
              getValuesListExpression(v, ctx, 3).asText());
        else
          data.delete(sc, getValuesListExpression(v, ctx, 0).asText(),
              getValuesListExpression(v, ctx, 1).asText(),
              getValuesListExpression(v, ctx, 2).asText());
        return null;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.query(sc, database, queryId, arguments)
   */
  public class Query extends FunctionBase {

    SecurityContext sc;
    boolean readOnly;

    public Query(SecurityContext sc, boolean readOnly) {
      this.readOnly = readOnly;
      this.sc = sc;
    }

    @Override
    public int getMaxArgs() {
      return 3;
    }

    @Override
    public int getMinArgs() {
      return 2;
    }

    @Override
    public String getSignature() {
      return "<j:ssj>";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 2)
        throw new RuntimeException("Arguments required: $query(database, queryId, arguments?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Query name cannot be null");
      try {
        return o2j(data.queryInternal(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(), getArgumentCountEx(ctx) == 2 ? null
                : (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 2)),
            readOnly));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * data.query(sc, database, queryId, arguments)
   */
  public class QueryGraph extends Query {

    public QueryGraph(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 2)
        throw new RuntimeException(
            "Arguments required: $queryGraph(database, queryId, arguments?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Query name cannot be null");
      try {
        return o2j(data.queryGraphInternal(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(), getArgumentCountEx(ctx) == 2 ? null
                : (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 2)),
            readOnly));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * org.dashjoin.service.QueryEditor.Delegate.noop(SecurityContext, QueryDatabase)
   */
  public class AdHocQuery extends Query {

    public AdHocQuery(SecurityContext sc, boolean readOnly) {
      super(sc, readOnly);
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 2)
        throw new RuntimeException("Arguments required: $adHocQuery(database, query, limit?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Query cannot be null");
      try {
        QueryDatabase query = new QueryDatabase();
        query.database = "dj/" + getValuesListExpression(v, ctx, 0).asText();
        query.query = getValuesListExpression(v, ctx, 1).asText();
        query.limit =
            getArgumentCountEx(ctx) < 3 ? null : getValuesListExpression(v, ctx, 2).asInt();
        AbstractDatabase db = services.getConfig().getDatabase(query.database);
        ACLContainerRequestFilter.allowQueryEditor(sc, db);
        return o2j(db.getQueryEditor().noop(query).data);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * call(sc, function, argument)
   */
  public class Call extends FunctionBase {

    SecurityContext sc;
    boolean readOnly;

    public Call(SecurityContext sc, boolean readOnly) {
      this.sc = sc;
      this.readOnly = readOnly;
    }

    @Override
    public int getMaxArgs() {
      return 2;
    }

    @Override
    public int getMinArgs() {
      return 1;
    }

    @Override
    public String getSignature() {
      return "<j:sj>";
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 1)
        throw new RuntimeException("Arguments required: $call(function, argument?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Function name cannot be null");
      try {
        String f = getValuesListExpression(v, ctx, 0).asText();
        return o2j(function.callInternal(sc, f,
            getArgumentCountEx(ctx) == 1 ? null : j2o(getValuesListExpression(v, ctx, 1)),
            readOnly));
      } catch (Exception e) {
        throw new WrappedException(e);
      }
    }
  }

  public static JsonNode o2j(Object object) {
    return om.convertValue(object, JsonNode.class);
  }

  public static Object j2o(JsonNode node) {
    return om.convertValue(node, Object.class);
  }

  public static int getArgumentCountEx(Function_callContext ctx) {
    if (ctx != null)
      return com.api.jsonata4java.expressions.functions.FunctionBase.getArgumentCount(ctx);

    // Emulate: jsonata-js mode
    Object[] args = JsonataJS.getArgs();
    return args != null ? args.length : 0;
  }

  public static JsonNode getValuesListExpression(ExpressionsVisitor exprVisitor,
      Function_callContext ctx, int index) {
    if (ctx != null)
      return com.api.jsonata4java.expressions.utils.FunctionUtils
          .getValuesListExpression(exprVisitor, ctx, index);

    // Emulate: jsonata-js mode
    Object[] args = JsonataJS.getArgs();
    return args != null && args.length > index && args[index] != null ? o2j(args[index]) : null;
  }

  /**
   * data.incoming(database, table, pk1) - limit and offset are null
   */
  public class Incoming extends FunctionBase {
    SecurityContext sc;

    public Incoming(SecurityContext sc) {
      this.sc = sc;
    }

    @Override
    public int getMaxArgs() {
      return 3;
    }

    @Override
    public int getMinArgs() {
      return 3;
    }

    @Override
    public String getSignature() {
      return "<j:sss>";
    }

    @Override
    public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
      if (getArgumentCountEx(ctx) < 3)
        throw new RuntimeException("Arguments required: $incoming(database, table, pk1)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      try {
        return o2j(data.incoming(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(),
            getValuesListExpression(v, ctx, 2).asText(), null, null));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
