package org.dashjoin.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.function.AbstractVarArgFunction;
import org.dashjoin.function.FunctionService;
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
import com.dashjoin.jsonata.JException;
import com.dashjoin.jsonata.Jsonata;
import com.dashjoin.jsonata.Jsonata.JFunction;
import com.dashjoin.jsonata.Jsonata.JFunctionCallable;
import com.dashjoin.jsonata.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

/**
 * REST API for expression evaluation
 */
@Path(Services.REST_PREFIX + "expression")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
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
    Object res = resolve(sc, e.expression, e.data);
    if (res instanceof String)
      return om.writeValueAsString(res);
    return res;
  }

  @GET
  @Path("/{expression}")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public Object resolveCached(@Context SecurityContext sc,
      @PathParam("expression") String expression) throws Exception {
    ExpressionAndData e = om.readValue(expression, ExpressionAndData.class);
    Object res = resolve(sc, e.expression, e.data);
    if (res instanceof String)
      return om.writeValueAsString(res);
    return res;
  }

  /**
   * parse and run jsonata in default mode readOnly = false
   */
  public Object resolve(SecurityContext sc, String expression, Object data) throws Exception {
    return resolve(sc, expression, data, false);
  }

  /**
   * parse and run jsonata
   */
  public Object resolve(SecurityContext sc, String expression, Object data, boolean readOnly)
      throws Exception {
    return jsonata(sc, expression, readOnly).evaluate(data);
  }

  /**
   * parse jsonata in default mode readOnly = false
   */
  public ParsedJsonata jsonata(SecurityContext sc, String expression) throws Exception {
    return jsonata(sc, expression, false);
  }

  /**
   * parse jsonata
   */
  @SuppressWarnings({"unchecked", "rawtypes", "unused"})
  public ParsedJsonata jsonata(SecurityContext sc, String expression, boolean readOnly)
      throws Exception {

    if (expression == null || expression.isBlank())
      return new ParsedJsonata();

    Jsonata expr = Jsonata.jsonata(expression);

    for (org.dashjoin.function.Function f : ServiceLoader
        .load(org.dashjoin.function.Function.class)) {

      // configurable function are run via $call(...)
      if (!(f instanceof AbstractConfigurableFunction)) {
        ((AbstractFunction) f).init(sc, services, this, readOnly);

        // special case for $call
        if (f instanceof Call)
          ((Call) f).function = this.function;

        expr.registerFunction(f.getID(), new JFunction(new JFunctionCallable() {

          /**
           * convert pojo return type to List / Map
           */
          @Override
          public Object call(Object input, List args) throws Throwable {
            Object res = _call(input, args);
            if (res == null)
              return null;
            if (res instanceof List) {
              List list = (List) res;
              if (list.size() > 0)
                if (list.get(0) != null)
                  if (!list.get(0).getClass().getName().startsWith("java"))
                    return om.convertValue(res, new TypeReference<List<Map<String, Object>>>() {});
            } else if (!res.getClass().getName().startsWith("java"))
              return om.convertValue(res, new TypeReference<Map<String, Object>>() {});
            return res;
          }

          Object _call(Object input, List args) throws Throwable {

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

    ParsedJsonata res = new ParsedJsonata();
    res.expr = expr;
    res.expression = expression;
    return res;
  }

  /**
   * wrap Jsonata such that we can perform error handling
   */
  public static class ParsedJsonata {
    Jsonata expr;
    String expression;

    @SuppressWarnings("rawtypes")
    public Object evaluate(Object data) throws Exception {

      if (expr == null)
        return null;

      try {
        Object res = expr.evaluate(data);

        // handle the case where the user types $function - the object cannot be
        if (res instanceof JFunction)
          return null;

        return res;
      } catch (JException e) {
        // intercept the exception
        // TODO: add getter to jsonata
        JException je = (JException) e;
        if ("T0410".equals(je.getError())) {
          for (org.dashjoin.function.Function f : ServiceLoader
              .load(org.dashjoin.function.Function.class))
            if (!(f instanceof AbstractConfigurableFunction))
              if (je.getExpected().equals(f.getID()))
                if (f.getHelp() != null)
                  throw new Exception(f.getHelp());
        }
        throw e;
      }
    }
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

    @SuppressWarnings({"rawtypes"})
    @Override
    public Object run(List arg) throws Exception {
      return this.expressionService.getData().traverse(sc, (String) arg.get(0), (String) arg.get(1),
          "" + arg.get(2), (String) arg.get(3));
    }

    @Override
    public String getHelp() {
      return "Arguments required: $traverse(database, table, pk1, fk)";
    }

    @Override
    public String getSignature() {
      return "<ss(bns)s:o?>";
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
      return function.callInternal(sc, (String) arg.get(0), optional(arg, 1, Object.class));
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
}
