package org.dashjoin.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.service.Data;
import org.dashjoin.service.Data.Origin;
import org.dashjoin.service.Data.Resource;
import org.dashjoin.service.Services;
import org.dashjoin.util.cypher.CypherBaseListener;
import org.dashjoin.util.cypher.CypherLexer;
import org.dashjoin.util.cypher.CypherListener;
import org.dashjoin.util.cypher.CypherParser;
import org.dashjoin.util.cypher.CypherParser.OC_PatternElementChainContext;
import org.dashjoin.util.cypher.CypherParser.OC_PatternElementContext;
import org.dashjoin.util.cypher.CypherParser.OC_PatternPartContext;
import org.dashjoin.util.cypher.CypherParser.OC_ReturnContext;
import org.dashjoin.util.cypher.CypherParser.OC_VariableContext;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Represents an OpenCypher query, where a small subset of the language is supported.
 */
public class OpenCypherQuery {

  /**
   * volatile part of table which is cloned and written during steps
   */
  public static class VariableName {

    /**
     * variable bound to this step
     */
    public String variable;

    /**
     * table / collection / edge name
     */
    public String name;
  }

  /**
   * query template fragment like [var:collection] or (var:edge)
   */
  public static class Table extends VariableName {

    /**
     * name escaped with back ticks
     */
    boolean nameEscaped;

    /**
     * represents equality contraints on the table
     */
    public String key;

    /**
     * represents equality contraints on the table
     */
    public String value;

    /**
     * edge or collection
     */
    public boolean isEdge;

    /**
     * multi hop
     */
    public boolean star;

    /**
     * multi hop lower bound
     */
    public Integer from;

    /**
     * multi hop upper bound
     */
    public Integer to;

    public VariableName getVariableName() {
      VariableName res = new VariableName();
      res.name = name;
      res.variable = variable;
      return res;
    }

    /**
     * parses an ANTLR parse tree fragment tree.getText() which looks like [var:Collection
     * {key:value}] or (var:edge *1..2)
     */
    public Table(String s, boolean isEdge) {
      this.isEdge = isEdge;
      s = s.substring(1, s.length() - 1).trim();
      if (s.endsWith("}")) {
        int start = s.lastIndexOf('{');
        int colon = s.indexOf(':', start + 1);
        key = s.substring(start + 1, colon).trim();
        value = s.substring(colon + 1, s.length() - 1).trim();
        s = s.substring(0, start);
      }
      if (s.endsWith("*")) {
        star = true;
        s = "";
      } else if (s.contains("*")) {
        star = true;
        int st = s.lastIndexOf('*');
        if (s.substring(st).matches("[*][0-9]*[.][.][0-9]*")) {
          String pattern = s.substring(st + 1).trim();
          if (pattern.startsWith(".."))
            to = Integer.parseInt(pattern.substring(2).trim());
          else if (pattern.endsWith(".."))
            from = Integer.parseInt(pattern.substring(0, pattern.length() - 2).trim());
          else {
            from = Integer.parseInt(pattern.split("[.][.]")[0].trim());
            to = Integer.parseInt(pattern.split("[.][.]")[1].trim());
          }
          s = s.substring(0, st);
        }
      }
      if (!s.contains(":"))
        variable = s.trim();
      else {
        variable = s.split(":")[0].trim();
        name = s.substring(s.split(":")[0].length() + 1).trim();
        if (name.startsWith("`") && name.endsWith("`")) {
          name = name.substring(1, name.length() - 1);
          nameEscaped = true;
        }
      }
    }

    @Override
    public String toString() {
      String kv = key != null ? " {" + key + ": " + value + "}" : "";
      String ft = "";
      if (star)
        if (from == null && to == null)
          ft = "*";
        else if (to == null)
          ft = "*" + from + "..";
        else if (from == null)
          ft = "*.." + to;
        else
          ft = "*" + from + ".." + to;
      String _name = nameEscaped ? "`" + name + "`" : name;
      if (isEdge)
        return "[" + variable + (name == null ? "" : (":" + _name)) + kv + ft + "]";
      else
        return "(" + variable + (name == null ? "" : (":" + _name)) + kv + ft + ")";
    }
  }

  /**
   * chain element thata follows the initial context "table"
   */
  public static class Chain {

    /**
     * edge
     */
    public Table edge;

    /**
     * collection
     */
    public Table table;

    /**
     * incoming or outgoing
     */
    public boolean left2right;

    @Override
    public String toString() {
      if (left2right)
        return "-" + edge + "->" + table;
      else
        return "<-" + edge + "-" + table;
    }
  }

  /**
   * represents a path which is the result of a graph traversal
   */
  public static class Path {

    /**
     * starting record
     */
    public Map<String, Object> start;

    /**
     * list of steps leading to the destination
     */
    public List<Step> steps = new ArrayList<>();
  }

  /**
   * step in a path
   */
  public static class Step {

    /**
     * after taking the step, we arrive at this record
     */
    public Map<String, Object> end;

    /**
     * we take the step across this edge
     */
    public Map<String, Object> edge;
  }

  /**
   * name of the path variable: MATCH path=(...
   */
  public String pathVariable;

  /**
   * initial context to match
   */
  public Table context;

  /**
   * template links
   */
  public List<Chain> links = new ArrayList<>();

  /**
   * return / projection
   */
  public List<List<String>> ret = new ArrayList<>();

  /**
   * parse query string into instance
   */
  public OpenCypherQuery(String query, Map<String, Object> arguments) throws IOException {

    query = "" + Template.replace(query, arguments);

    BaseErrorListener errorHandler = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new RuntimeException("line " + line + ":" + charPositionInLine + " " + msg);
      }
    };

    CharStream input = CharStreams.fromStream(new ByteArrayInputStream(query.getBytes()));
    CypherLexer lexer = new CypherLexer(input);
    lexer.addErrorListener(errorHandler);
    CommonTokenStream token = new CommonTokenStream(lexer);
    CypherParser parser = new CypherParser(token);
    parser.addErrorListener(errorHandler);
    ParseTree tree = parser.oC_Cypher();

    CypherListener listener = new CypherBaseListener() {

      @Override
      public void exitOC_PatternPart(OC_PatternPartContext ctx) {
        if (ctx.getChild(0) instanceof OC_VariableContext)
          pathVariable = ctx.getChild(0).getText();
      }

      @Override
      public void exitOC_PatternElement(OC_PatternElementContext ctx) {
        String s = ctx.getChild(0).getText().trim();
        context = new Table(s, false);
      }

      @Override
      public void exitOC_PatternElementChain(OC_PatternElementChainContext ctx) {
        Chain c = new Chain();
        String rel = ctx.getChild(0).getText();
        if (rel.endsWith("->")) {
          c.left2right = true;
          rel = rel.substring(1, rel.length() - 2);
        } else
          rel = rel.substring(2, rel.length() - 1);
        c.edge = new Table(rel, true);
        c.table = new Table(ctx.getChild(1).getText(), false);
        links.add(c);
      }

      @Override
      public void exitOC_Return(OC_ReturnContext ctx) {
        for (String part : ctx.getChild(1).getText().trim().split(","))
          ret.add(Arrays.asList(part.trim().split("\\.")));
      }
    };

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(listener, tree);
  }

  /**
   * helper used to debug / visualize the parse tree
   */
  void print(ParseTree tree, String indent) {
    System.out.println(indent + tree.getClass().toString()
        .substring("class org.dashjoin.util.cypher.CypherParser$".length()) + " " + tree.getText());
    for (int i = 0; i < tree.getChildCount(); i++)
      print(tree.getChild(i), indent + "  ");
  }

  @Override
  public String toString() {
    String s = "MATCH " + (pathVariable == null ? "" : pathVariable + "=") + context;

    for (Chain c : links)
      s = s + c;

    List<String> projections = new ArrayList<>();
    for (List<String> p : ret)
      projections.add(String.join(".", p));
    return s + " RETURN " + String.join(", ", projections);
  }

  /**
   * db cache
   */
  Map<String, AbstractDatabase> dbs = new HashMap<>();

  /**
   * query result
   */
  List<Map<String, Object>> res = new ArrayList<>();

  public List<Map<String, Object>> run(Services service, Data data, SecurityContext sc)
      throws Exception {

    // fill db cache
    for (AbstractDatabase db : service.getConfig().getDatabases())
      dbs.put(db.name, db);

    if (context.name == null)
      throw new Exception("Cannot infer type of " + context
          + ". Please specify the table via :``dj/database/table``");

    // compute starting context nodes
    String[] table = Escape.parseTableID(context.name);
    for (Map<String, Object> row : data.all(sc, table[1], table[2], null, null, null, false,
        context.key == null ? null
            : MapUtil.of(context.key,
                context.value.startsWith("'") && context.value.endsWith("'")
                    ? context.value.substring(1, context.value.length() - 1)
                    : context.value))) {

      // initialize variable bindings
      Map<String, Object> vars = new LinkedHashMap<>();

      // initialize path variable
      Map<String, Object> path = MapUtil.of("start", row, "steps", new ArrayList<>());
      if (pathVariable != null)
        vars.put(pathVariable, path);

      step(service, data, sc, context, new LinkedHashMap<>(vars), path, row, 0);
    }
    return res;
  }

  /**
   * holds potential recursion data
   */
  static class Struct {
    Struct(Map<String, Object> i, String linkEdgeName, String ctxName) {
      this.i = i;
      this.linkEdgeName = linkEdgeName;
      this.ctxName = ctxName;
    }

    Map<String, Object> i;
    String linkEdgeName;
    String ctxName;
  }

  @SuppressWarnings("unchecked")
  void step(Services service, Data data, SecurityContext sc, VariableName ctx,
      Map<String, Object> vars, Map<String, Object> path, Map<String, Object> row, int linkIndex)
      throws Exception {

    // parse context metadata
    String[] table = Escape.parseTableID(ctx.name);
    addResource(table[1], table[2], row);
    vars.put(ctx.variable, row);

    if (linkIndex == links.size())
      // solution found
      res.add(project(vars));
    else {
      Chain link = links.get(linkIndex);
      ctx = link.table.getVariableName();
      if (row == null)
        return;
      List<Struct> incRes = new ArrayList<>();
      if (link.left2right) {
        if (link.edge.name == null) {
          // outgoing link, no rel type specified, check all properties for "ref"
          for (Property p : dbs.get(table[1]).tables.get(table[2]).properties.values())
            if (p.ref != null) {
              if (!checkContext(ctx, p.ref.substring(0, p.ref.lastIndexOf('/'))))
                return;
              incRes
                  .add(new Struct(
                      (Map<String, Object>) data.traverse(sc, table[1], table[2],
                          "" + row.get(pk(dbs.get(table[1]), table[2])), p.name),
                      p.name, ctx.name));
            }
        } else {
          // outgoing link with prop, do a simple traverse
          String ref = dbs.get(table[1]).tables.get(table[2]).properties.get(link.edge.name).ref;
          ref = ref == null ? null : ref.substring(0, ref.lastIndexOf('/'));
          if (!checkContext(ctx, ref))
            return;
          incRes.add(new Struct(
              (Map<String, Object>) data.traverse(sc, table[1], table[2],
                  "" + row.get(pk(dbs.get(table[1]), table[2])), link.edge.name),
              link.edge.name, ctx.name));
        }
      } else {
        if (link.edge.name == null) {
          // incoming, no prop specified, call data.incoming
          for (Origin o : data.incoming(sc, table[1], table[2],
              "" + row.get(pk(dbs.get(table[1]), table[2])), 0, 100)) {
            Map<String, Object> lookup =
                o.id.pk.size() == 1 ? data.read(sc, o.id.database, o.id.table, "" + o.id.pk.get(0))
                    : data.read(sc, o.id.database, o.id.table, "" + o.id.pk.get(0),
                        "" + o.id.pk.get(1));
            if (!checkContext(ctx, "dj/" + o.id.database + "/" + o.id.table))
              return;
            incRes.add(new Struct(lookup, o.fk, ctx.name));
          }
        } else {
          // incoming, prop specified, do a traverse (which might yield several results)
          if (!checkContext(ctx, link.edge.name.substring(0, link.edge.name.lastIndexOf('/'))))
            return;
          for (Map<String, Object> x : (List<Map<String, Object>>) data.traverse(sc, table[1],
              table[2], "" + row.get(pk(dbs.get(table[1]), table[2])), link.edge.name))
            incRes.add(new Struct(x, link.edge.name, ctx.name));
        }
      }
      for (Struct i : incRes) {

        // omit nulls
        if (i.i == null)
          continue;

        // check condition
        if (link.table.key != null) {
          String val = link.table.value;
          if (val.startsWith("'") && val.endsWith("'"))
            val = val.substring(1, val.length() - 1);
          if (!("" + i.i.get(link.table.key)).equals(val))
            continue;
        }

        row = i.i;
        vars.put(link.table.variable, row);

        Map<String, Object> edge =
            MapUtil.of("_dj_edge", i.linkEdgeName, "_dj_outbound", link.left2right);
        vars.put(link.edge.variable, edge);

        ((List<Object>) path.get("steps")).add(MapUtil.of("edge", edge, "end", row));
        ctx.name = i.ctxName;
        step(service, data, sc, ctx, new LinkedHashMap<>(vars), path, row, linkIndex + 1);
      }
    }
  }

  boolean checkContext(VariableName ctx, String ref) {
    if (ctx.name == null)
      ctx.name = ref;
    else if (!ctx.name.equals(ref))
      // ref type and table type do not match
      return false;
    return true;
  }

  /**
   * given a variable binding map, evaluates the "ret" projection
   */
  @SuppressWarnings("unchecked")
  Map<String, Object> project(Map<String, Object> vars) {
    Map<String, Object> projected = new LinkedHashMap<>();
    for (List<String> var : ret) {
      Object current = vars;
      for (String p : var) {
        if (current instanceof Map)
          current = ((Map<String, Object>) current).get(p);
        else
          current = null;
      }
      projected.put(String.join(".", var), current);
    }
    return projected;
  }

  /**
   * adds the resource object to the current object
   */
  void addResource(String database, String table, Map<String, Object> object) {
    List<Object> keys = new ArrayList<>();
    for (Property prop : dbs.get(database).tables.get(table).properties.values()) {
      if (prop.pkpos != null) {
        while (keys.size() <= prop.pkpos)
          keys.add(null);
        keys.set(prop.pkpos, object.get(prop.name));
      }
    }
    object.put("_dj_resource", Resource.of(database, table, keys));
  }

  /**
   * get the property name with pkpos=0
   */
  String pk(AbstractDatabase db, String table) {
    for (Property p : db.tables.get(table).properties.values())
      if (p.pkpos != null)
        return p.name;
    return null;
  }
}
