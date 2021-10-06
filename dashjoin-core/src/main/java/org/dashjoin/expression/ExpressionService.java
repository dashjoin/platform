package org.dashjoin.expression;

import static com.api.jsonata4java.expressions.functions.FunctionBase.getArgumentCount;
import static com.api.jsonata4java.expressions.utils.FunctionUtils.getValuesListExpression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.AbstractFunction;
import org.dashjoin.function.FunctionService;
import org.dashjoin.service.Data;
import org.dashjoin.service.Manage;
import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.ExpressionsVisitor;
import com.api.jsonata4java.expressions.ParseException;
import com.api.jsonata4java.expressions.functions.Function;
import com.api.jsonata4java.expressions.generated.MappingExpressionParser.Function_callContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST API for expression evaluation
 */
@Path(Services.REST_PREFIX + "expression")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@SuppressWarnings("serial")
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
  public JsonNode resolve(@Context SecurityContext sc,
      @Parameter(description = "expression and context to evaluate") ExpressionAndData e)
      throws Exception {
    return jsonata(sc, e.expression, o2j(e.data), false);
  }

  @GET
  @Path("/{expression}")
  @Operation(summary = "evaluates the expression with the data context")
  @APIResponse(description = "evaluation result")
  public JsonNode resolveCached(@Context SecurityContext sc,
      @PathParam("expression") String expression) throws Exception {
    ExpressionAndData e = om.readValue(expression, ExpressionAndData.class);
    return jsonata(sc, e.expression, o2j(e.data), false);
    // return jsonata(sc, expression.expression, o2j(expression.data), false);
  }


  public Object resolve(SecurityContext sc, String expression, Object data) throws Exception {
    return j2o(jsonata(sc, expression, o2j(data), false));
  }

  public Object resolve(SecurityContext sc, String expression, Object data, boolean readOnly)
      throws Exception {
    return j2o(jsonata(sc, expression, o2j(data), readOnly));
  }

  public Expressions prepare(SecurityContext sc, String expression)
      throws ParseException, IOException {
    return parse(sc, expression, false);
  }

  public Object resolve(Expressions expr, Object data) throws Exception {
    try {
      return j2o(expr.evaluate(o2j(data)));
    } catch (WrappedException e) {
      throw (Exception) e.getCause();
    }
  }

  Expressions parse(SecurityContext sc, String expression, boolean readOnly)
      throws ParseException, IOException {
    Expressions expr = Expressions.parse(expression);
    expr.getEnvironment().setJsonataFunction("$read", new Read(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$create", new Create(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$update", new Update(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$traverse", new Traverse(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$delete", new Delete(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$query", new Query(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$call", new Call(sc, readOnly));
    expr.getEnvironment().setJsonataFunction("$incoming", new Incoming(sc));

    for (org.dashjoin.function.Function<?, ?> f : ServiceLoader
        .load(org.dashjoin.function.Function.class)) {
      if (!(f instanceof AbstractConfigurableFunction<?, ?>)) {
        ((AbstractFunction<?, ?>) f).init(sc, services, this, readOnly);
        expr.getEnvironment().setJsonataFunction("$" + ((AbstractFunction<?, ?>) f).getID(),
            new Function() {

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

              @SuppressWarnings("unchecked")
              @Override
              public JsonNode invoke(ExpressionsVisitor v, Function_callContext ctx) {
                try {
                  return o2j(function.callInternal(sc, (AbstractFunction<Object, Object>) f,
                      getArgumentCount(ctx) == 0 ? null : j2o(getValuesListExpression(v, ctx, 0)),
                      readOnly));
                } catch (Exception e) {
                  throw new WrappedException(e);
                }
              }
            });
      }
    }
    return expr;
  }

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
          ((ObjectNode) data).set("value",
              o2j(this.data.read(sc, get(data, "database"), get(data, "table"), objectIds)));
        } catch (Exception e) {
          throw new IOException(e);
        }
    }

    Expressions expr = parse(sc, expression, readOnly);

    try {
      return expr.evaluate(data);
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
   * data.read(database, table, pk1)
   */
  public class Read implements Function {
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
      if (getArgumentCount(ctx) < 3)
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
      if (getArgumentCount(ctx) < 3)
        throw new RuntimeException("Arguments required: $create(database, table, pk1)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 1) == null)
        throw new RuntimeException("Table name cannot be null");
      if (getValuesListExpression(v, ctx, 2) == null)
        throw new RuntimeException("pk1 cannot be null");
      try {
        if (readOnly)
          return null;
        return o2j(data.create(sc, getValuesListExpression(v, ctx, 0).asText(),
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
      if (getArgumentCount(ctx) < 4)
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
      if (getArgumentCount(ctx) < 4)
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
      if (getArgumentCount(ctx) < 3)
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
  public class Query implements Function {

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
      if (getArgumentCount(ctx) < 2)
        throw new RuntimeException("Arguments required: $query(database, queryId, arguments?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Database name cannot be null");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Query name cannot be null");
      try {
        return o2j(data.queryInternal(sc, getValuesListExpression(v, ctx, 0).asText(),
            getValuesListExpression(v, ctx, 1).asText(), getArgumentCount(ctx) == 2 ? null
                : (Map<String, Object>) j2o(getValuesListExpression(v, ctx, 2)),
            readOnly));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * call(sc, function, argument)
   */
  public class Call implements Function {

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
      if (getArgumentCount(ctx) < 1)
        throw new RuntimeException("Arguments required: $call(function, argument?)");
      if (getValuesListExpression(v, ctx, 0) == null)
        throw new RuntimeException("Function name cannot be null");
      try {
        String f = getValuesListExpression(v, ctx, 0).asText();
        return o2j(function.callInternal(sc, f,
            getArgumentCount(ctx) == 1 ? null : j2o(getValuesListExpression(v, ctx, 1)), readOnly));
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

  /**
   * data.incoming(database, table, pk1) - limit and offset are null
   */
  public class Incoming implements Function {
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
      if (getArgumentCount(ctx) < 3)
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
