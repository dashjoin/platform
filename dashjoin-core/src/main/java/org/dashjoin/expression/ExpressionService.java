package org.dashjoin.expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dashjoin.expression.jsonatajs.JsonataJS;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.function.AbstractVarArgFunction;
import org.dashjoin.function.FunctionService;
import org.dashjoin.function.JobStatus;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.service.ACLContainerRequestFilter;
import org.dashjoin.service.Data;
import org.dashjoin.service.Data.Origin;
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
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.dashjoin.jsonata.Utils;
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
  public Object resolve(@Context SecurityContext sc, ExpressionAndData e) throws Exception {
    return jsonata(sc, e.expression, (e.data), false);
  }

  @GET
  @Path("/{expression}")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public Object resolveCached(@Context SecurityContext sc,
      @PathParam("expression") String expression) throws Exception {
    ExpressionAndData e = om.readValue(expression, ExpressionAndData.class);
    return jsonata(sc, e.expression, (e.data), false);
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
    return (jsonata(sc, expression, (data), false));
  }

  public Object resolve(SecurityContext sc, String expression, Object data, boolean readOnly)
      throws Exception {
    return (jsonata(sc, expression, (data), readOnly));
  }

  public ParsedExpression prepare(SecurityContext sc, String expression)
      throws ParseException, IOException {
    return parse(sc, expression, false);
  }

  public Object resolve(ParsedExpression _expr, Object data) throws Exception {

    if (_expr instanceof DJsonataParsedExpression) {
      DJsonataParsedExpression expr = (DJsonataParsedExpression) _expr;
      Object res = expr.expr.evaluate(data, expr.bindings);
      return res; // j2o(res);
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
        return new DJsonataParsedExpression(expression, parsed,
            getDjsonataFunctions(parsed, sc, readOnly));
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
  Jsonata.Frame getDjsonataFunctions(Jsonata expr, SecurityContext sc, boolean readOnly)
      throws Exception {
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

      bindings.bind(name, new Jsonata.JFunction(fc, null));
    }
    return bindings;
  }

  Map<String, FunctionBase> getJsonataFunctions(SecurityContext sc, boolean readOnly)
      throws ParseException, IOException {
    // TODO: combine these with the service loader functions
    // keep in sync with org.dashjoin.service.Manage.getFunctions()
    HashMap<String, FunctionBase> res = new HashMap<>();

    /*
     * res.put("$all", new All(sc, readOnly)); res.put("$read", new Read(sc, readOnly));
     * res.put("$create", new Create(sc, readOnly)); res.put("$update", new Update(sc, readOnly));
     * res.put("$traverse", new Traverse(sc, readOnly)); res.put("$delete", new Delete(sc,
     * readOnly)); res.put("$query", new Query(sc, readOnly)); res.put("$queryGraph", new
     * QueryGraph(sc, readOnly)); res.put("$adHocQuery", new AdHocQuery(sc, readOnly));
     * res.put("$call", new Call(sc, readOnly)); res.put("$incoming", new Incoming(sc));
     */

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
    return false; // JsonataJS.getInstance() != null;
  }

  /**
   * True if reference impl via JsonataJS is active
   */
  boolean jsonataJs = canUseJsonataReference();

  /**
   * True if Dashjoin Jsonata reference is active
   */
  boolean djsonata = true;

  @SuppressWarnings({"unchecked", "rawtypes"})
  Object jsonata(SecurityContext sc, String expression, Object data, boolean readOnly)
      throws Exception {

    if (expression == null || expression.isBlank())
      return null;

    Jsonata expr = Jsonata.jsonata(expression);

    for (org.dashjoin.function.Function f : ServiceLoader
        .load(org.dashjoin.function.Function.class)) {

      // configurable function are run via $call(...)
      if (!(f instanceof AbstractConfigurableFunction)) {
        ((AbstractFunction) f).init(sc, services, this, false);

        // special case for $call
        if (f instanceof Call)
          ((Call) f).function = this.function;

        expr.registerFunction(f.getID(), new JFunction(new JFunctionCallable() {
          @Override
          public Object call(Object input, List args) throws Throwable {

            // check readOnly
            if (readOnly && !"read".equals(f.getType()))
              return null;

            // we do not allow custom functions to differentiate between null / undefined
            args = (List) Utils.convertNulls(args);

            if (f instanceof AbstractVarArgFunction) {
              AbstractVarArgFunction vaf = (AbstractVarArgFunction) f;
              if (vaf.getArgumentClassList() == null)
                // can pass 1:1
                return f.run(args);
              else {
                // legacy mode - implementations expect optional arguments to be null (i.e. [null]
                // and not [])
                AbstractVarArgFunction<Object> vf = (AbstractVarArgFunction<Object>) f;
                List<Object> vargs = new ArrayList<>();
                int index = 0;
                for (Class<?> c : vf.getArgumentClassList()) {
                  if (index < args.size())
                    vargs.add(args.get(index));
                  else
                    vargs.add(null);
                  index++;
                }
                return f.run(vargs);
              }
            } else {
              // single parameter, get optional parameter and handle NULL
              return f.run(
                  om.convertValue(args.size() == 0 ? null : args.get(0), f.getArgumentClass()));
            }
          }
        }, f.getSignature()));
      }
    }
    try {
      Object res = expr.evaluate(data);
      return res;
    } catch (JException e) {

      // intercept the exception
      // TODO: add getter to jsonata
      if ("T0410".equals(FieldUtils.readDeclaredField(e, "error", true)))
        if (expression.startsWith("$") && expression.contains("(")) {
          // TODO: add function name to exception
          String name = expression.substring(1, expression.indexOf('('));
          for (org.dashjoin.function.Function f : ServiceLoader
              .load(org.dashjoin.function.Function.class))
            if (!(f instanceof AbstractConfigurableFunction))
              if (name.equals(f.getID()))
                if (f.getHelp() != null)
                  throw new Exception(f.getHelp());
        }
      throw e;
    }
  }

  /**
   * wrapper around new evaluate(expression, data) that defines our custom function
   */
  JsonNode __jsonata(SecurityContext sc, String expression, JsonNode data, boolean readOnly)
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
        String str = data != null ? data.toString() : null;
        // System.err.println("Data "+str);

        Object res = expr
            .evaluate(str != null ? com.dashjoin.jsonata.json.Json.parseJson(str) : null, bindings);
        // System.err.println("Expr "+expression+" Result "+res);
        return o2j(res); // Functions.string(res, false);
      } catch (Exception ex) {
        log.warning("Error in expression " + expression);
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

  public static abstract class Base<T> extends AbstractVarArgFunction<T> {

    /**
     * get optional parameter (or null) from arguments
     */
    @SuppressWarnings("unchecked")
    <C> C optional(List<Object> arg, int index, Class<C> type) throws Exception {
      if (arg.size() <= index)
        return null;
      Object o = arg.get(index);
      if (o == null)
        return null;
      if (type.isAssignableFrom(o.getClass()))
        return (C) o;
      throw new Exception(getHelp());
    }
  }

  /**
   * data.all(database, table)
   */
  public static class All extends Base<List<Map<String, Object>>> {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List<Map<String, Object>> run(List arg) throws Exception {
      return this.expressionService.getData().all(sc, (String) arg.get(0), (String) arg.get(1),
          optional(arg, 2, Integer.class), optional(arg, 3, Integer.class),
          optional(arg, 4, String.class), (optional(arg, 5, Boolean.class) == null ? false
              : (boolean) optional(arg, 5, Boolean.class)),
          optional(arg, 6, Map.class));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $all(database, table, offset?, limit?, sort?, desc?, arguments?)";
    }

    @Override
    public String getSignature() {
      return "<ssn?n?s?b?o?:a>";
    }
  }

  /**
   * data.read(database, table, pk1)
   */
  public static class Read extends AbstractVarArgFunction<Object> {

    @SuppressWarnings("rawtypes")
    @Override
    public Object run(List arg) throws Exception {
      return this.expressionService.getData().read(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $read(database, table, pk1)";
    }

    @Override
    public String getSignature() {
      return "<ss(bns):o?>";
    }
  }

  /**
   * data.create(database, table, pk1)
   */
  public static class Create extends AbstractVarArgFunction<Object> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      return this.expressionService.getData().createInternal(sc, (String) arg.get(0),
          (String) arg.get(1), (Map<String, Object>) arg.get(2));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $create(database, table, object)";
    }

    @Override
    public String getType() {
      return "write";
    }

    @Override
    public String getSignature() {
      return "<sso:o?>";
    }
  }

  /**
   * data.update(database, table, pk1, object)
   */
  public static class Update extends AbstractVarArgFunction<Object> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      this.expressionService.getData().update(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2), (Map<String, Object>) arg.get(3));
      return null;
    }

    @Override
    public String getHelp() {
      return "Arguments required: $update(database, table, pk1, object)";
    }

    @Override
    public String getType() {
      return "write";
    }

    @Override
    public String getSignature() {
      return "<ss(bns)o:l>";
    }
  }

  /**
   * data.traverse(database, table, pk1, fk)
   */
  public static class Traverse extends AbstractVarArgFunction<Object> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      return this.expressionService.getData().traverse(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2), (String) arg.get(3));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $traverse(database, table, pk1, fk)";
    }
  }

  /**
   * data.delete(database, table, pk1)
   */
  public static class Delete extends AbstractVarArgFunction<Object> {

    @SuppressWarnings({"rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      this.expressionService.getData().delete(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2));
      return null;
    }

    @Override
    public String getHelp() {
      return "Arguments required: $delete(database, table, pk1)";
    }

    @Override
    public String getType() {
      return "write";
    }

    @Override
    public String getSignature() {
      return "<ss(bns):o?>";
    }
  }

  /**
   * data.query(sc, database, queryId, arguments)
   */
  public static class Query extends Base<List<Map<String, Object>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Map<String, Object>> run(List arg) throws Exception {
      return this.expressionService.getData().queryInternal(sc, (String) arg.get(0),
          (String) arg.get(1), optional(arg, 2, Map.class), readOnly);
    }

    @Override
    public String getHelp() {
      return "Arguments required: $query(database, queryId, arguments?)";
    }

    @Override
    public String getSignature() {
      return "<sso?:a>";
    }
  }

  /**
   * data.query(sc, database, queryId, arguments)
   */
  public static class QueryGraph extends Base<List<Map<String, Object>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Map<String, Object>> run(List arg) throws Exception {
      return this.expressionService.getData().queryGraphInternal(sc, (String) arg.get(0),
          (String) arg.get(1), optional(arg, 2, Map.class), readOnly);
    }

    @Override
    public String getHelp() {
      return "Arguments required: $queryGraph(database, queryId, arguments?)";
    }

    @Override
    public String getSignature() {
      return "<sso?:a>";
    }
  }

  /**
   * org.dashjoin.service.QueryEditor.Delegate.noop(SecurityContext, QueryDatabase)
   */
  public static class AdHocQuery extends Base<List<Map<String, Object>>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Map<String, Object>> run(List arg) throws Exception {
      QueryDatabase query = new QueryDatabase();
      query.database = "dj/" + arg.get(0);
      query.query = (String) arg.get(1);
      query.limit = optional(arg, 2, Integer.class);
      AbstractDatabase db = services.getConfig().getDatabase(query.database);
      ACLContainerRequestFilter.allowQueryEditor(sc, db);
      return db.getQueryEditor().noop(query).data;
    }

    @Override
    public String getHelp() {
      return "Arguments required: $adHocQuery(database, query, limit?)";
    }

    @Override
    public String getSignature() {
      return "<ssn?:a>";
    }
  }

  /**
   * call(sc, function, argument)
   */
  public static class Call extends Base<Object> {

    FunctionService function;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      return function.call(sc, (String) arg.get(0), optional(arg, 1, Object.class));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $call(function, argument?)";
    }

    @Override
    public String getSignature() {
      return "<sx?:x>";
    }
  }

  /**
   * data.incoming(database, table, pk1) - limit and offset are null
   */
  public static class Incoming extends AbstractVarArgFunction<List<Origin>> {

    @SuppressWarnings({"rawtypes"})
    @Override
    public List<Origin> run(List arg) throws Exception {
      return this.expressionService.getData().incoming(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2), null, null);
    }

    @Override
    public String getHelp() {
      return "Arguments required: $incoming(database, table, pk1)";
    }

    @Override
    public String getSignature() {
      return "<ss(bns):a>";
    }
  }

  public static JsonNode o2j(Object object) {
    return om.convertValue(Utils.convertNulls(object), JsonNode.class);
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
}
