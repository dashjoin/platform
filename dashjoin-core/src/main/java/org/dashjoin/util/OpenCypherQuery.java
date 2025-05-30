package org.dashjoin.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
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

    /**
     * the table name inferred from the query context
     */
    public String ref;
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
      s = s.trim();
      this.isEdge = isEdge;
      if (!s.isEmpty())
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
        int st = s.lastIndexOf('*');
        s = s.substring(0, st);
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
    public Boolean left2right;

    @Override
    public String toString() {
      if (left2right == null)
        return "-" + edge + "-" + table;
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

  public boolean newEngine = true;

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
        } else if (rel.startsWith("<-")) {
          c.left2right = false;
          rel = rel.substring(2, rel.length() - 1);
        } else
          rel = rel.substring(1, rel.length() - 1);
        c.edge = new Table(rel, true);
        int index = ctx.getChild(1) instanceof TerminalNode ? 2 : 1;
        c.table = new Table(ctx.getChild(index).getText(), false);
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

    if (!context.name.contains("/"))
      if (guessTable(context.name) != null)
        context.name = guessTable(context.name);
    for (Chain link : links)
      if (link.table.name != null && !link.table.name.contains("/"))
        link.table.name = guessTable(link.table.name);

    // compute starting context nodes
    String[] table = Escape.parseTableID(context.name);
    for (Map<String, Object> row : data.all(sc, table[1], table[2], null, null, null, false,
        context.key == null ? null
            : MapUtil.of(context.key,
                context.value.startsWith("'") && context.value.endsWith("'")
                    ? context.value.substring(1, context.value.length() - 1)
                    : context.value))) {

      if (this.newEngine) {
        OpenCypher oc = new OpenCypher(this);
        res.addAll(oc.run(service, data, sc, row));
      } else {
        // initialize variable bindings
        Map<String, Object> vars = new LinkedHashMap<>();

        // initialize path variable
        Map<String, Object> path = MapUtil.of("start", row, "steps", new ArrayList<>());

        step(service, data, sc, context, new LinkedHashMap<>(vars), path, row, 0);
      }
    }
    return res;
  }

  /**
   * holds potential recursion data
   */
  static class Struct {
    Struct(Map<String, Object> i, String linkEdgeName, VariableName ctx, boolean left2right) {
      this.list = Arrays.asList(i);
      this.linkEdgeName = linkEdgeName;
      this.ctxName = ctx.ref;
      this.left2right = left2right;
    }

    Struct(List<Map<String, Object>> list, String linkEdgeName, VariableName ctx,
        boolean left2right) {
      this.list = list;
      this.linkEdgeName = linkEdgeName;
      this.ctxName = ctx.ref;
      this.left2right = left2right;
    }

    List<Map<String, Object>> list;
    String linkEdgeName;
    String ctxName;
    boolean left2right;
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
      res.add(project(vars, path));
    else {
      Chain link = links.get(linkIndex);
      ctx = link.table.getVariableName();
      if (row == null)
        return;
      List<Struct> incRes = new ArrayList<>();
      if (link.left2right == null || link.left2right) {
        if (link.edge.name == null) {
          // outgoing link, no rel type specified, check all properties for "ref"
          for (Property p : dbs.get(table[1]).tables.get(table[2]).properties.values())
            if (p.ref != null) {
              if (!checkContext(ctx, p.ref.substring(0, p.ref.lastIndexOf('/'))))
                continue;
              List<String> pks = pks(dbs.get(table[1]), table[2]);
              if (pks.size() == 1)
                if (link.edge.star)
                  for (List<Map<String, Object>> x : traverse(data, sc, table[1], table[2],
                      "" + row.get(pks.get(0)), p.name, link.edge.from, link.edge.to)) {
                    for (Map<String, Object> xx : x)
                      addResource(table[1], table[2], xx);
                    incRes.add(new Struct(x, p.name, ctx, true));
                  }
                else
                  incRes.add(new Struct((Map<String, Object>) data.traverse(sc, table[1], table[2],
                      "" + row.get(pks.get(0)), p.name), p.name, ctx, true));
              else
                incRes.add(new Struct(
                    (Map<String, Object>) data.traverse(sc, table[1], table[2],
                        "" + row.get(pks.get(0)), "" + row.get(pks.get(1)), p.name),
                    p.name, ctx, true));
            }
        } else {
          // outgoing link with prop, do a simple traverse
          String ref = dbs.get(table[1]).tables.get(table[2]).properties.get(link.edge.name).ref;
          ref = ref == null ? null : ref.substring(0, ref.lastIndexOf('/'));
          if (!checkContext(ctx, ref))
            ;
          else
            incRes.add(new Struct(
                (Map<String, Object>) data.traverse(sc, table[1], table[2],
                    "" + row.get(pk(dbs.get(table[1]), table[2])), link.edge.name),
                link.edge.name, ctx, true));
        }
      }
      if (link.left2right == null || !link.left2right) {

        if (link.edge.star || link.edge.from != null || link.edge.to != null)
          throw new Exception("Not supported: " + link.edge);

        if (link.edge.name == null) {

          // incoming, no prop specified, call data.incoming
          for (Origin o : data.incoming(sc, table[1], table[2],
              "" + row.get(pk(dbs.get(table[1]), table[2])), 0, 100)) {
            Map<String, Object> lookup =
                o.id.pk.size() == 1 ? data.read(sc, o.id.database, o.id.table, "" + o.id.pk.get(0))
                    : data.read(sc, o.id.database, o.id.table, "" + o.id.pk.get(0),
                        "" + o.id.pk.get(1));
            if (!checkContext(ctx, "dj/" + o.id.database + "/" + o.id.table))
              ;
            else
              incRes.add(new Struct(lookup, o.fk, ctx, false));
          }
        } else {
          // incoming, prop specified, do a traverse (which might yield several results)
          if (!checkContext(ctx, link.edge.name.substring(0, link.edge.name.lastIndexOf('/'))))
            ;
          else
            for (Map<String, Object> x : (List<Map<String, Object>>) data.traverse(sc, table[1],
                table[2], "" + row.get(pk(dbs.get(table[1]), table[2])), link.edge.name))
              incRes.add(new Struct(x, link.edge.name, ctx, false));
        }
      }
      for (Struct i : incRes) {

        // last element of solution path
        Map<String, Object> last = i.list.get(i.list.size() - 1);

        // omit nulls
        if (last == null)
          continue;

        // check condition
        if (link.table.key != null) {
          String val = link.table.value;
          if (val.startsWith("'") && val.endsWith("'"))
            val = val.substring(1, val.length() - 1);
          if (!("" + last.get(link.table.key)).equals(val))
            continue;
        }

        row = last;
        vars.put(link.table.variable, row);

        Map<String, Object> edge =
            MapUtil.of("_dj_edge", i.linkEdgeName, "_dj_outbound", i.left2right);
        vars.put(link.edge.variable, edge);

        // ((List<Object>) path.get("steps")).add(MapUtil.of("edge", edge, "end", row));
        ctx.name = i.ctxName;
        step(service, data, sc, ctx, new LinkedHashMap<>(vars), addStep(path, edge, i.list), row,
            linkIndex + 1);
      }
    }
  }

  /**
   * clone the path variable and add the step
   */
  Map<String, Object> addStep(Map<String, Object> path, Map<String, Object> edge,
      List<Map<String, Object>> list) {
    path = new LinkedHashMap<>(path);
    @SuppressWarnings("unchecked")
    List<Object> steps = new ArrayList<>((List<Object>) path.get("steps"));
    for (Object step : list)
      steps.add(MapUtil.of("edge", edge, "end", step));
    path.put("steps", steps);
    return path;
  }

  /**
   * traverse from...to steps
   */
  List<List<Map<String, Object>>> traverse(Data data, SecurityContext sc, String database,
      String table, String id, String fk, Integer from, Integer to) throws Exception {

    if (from == null)
      from = 1;
    if (to == null)
      to = 10;

    List<List<Map<String, Object>>> res = new ArrayList<>();
    for (int i = from; i <= to; i++)
      res.addAll(traverse(data, sc, database, table, id, fk, i));
    return res;
  }

  /**
   * traverse n steps
   */
  List<List<Map<String, Object>>> traverse(Data data, SecurityContext sc, String database,
      String table, String id, String fk, int steps) throws Exception {
    String pk = pk(dbs.get(database), table);
    if (steps == 1)
      return traverse(data, sc, database, table, id, fk).stream().map(o -> Arrays.asList(o))
          .collect(Collectors.toList());
    else {
      List<List<Map<String, Object>>> res = new ArrayList<>();
      List<List<Map<String, Object>>> rec = traverse(data, sc, database, table, id, fk, steps - 1);
      for (List<Map<String, Object>> row : rec)
        for (Map<String, Object> o : traverse(data, sc, database, table,
            "" + row.get(row.size() - 1).get(pk), fk))
          res.add(concat(row, o));
      return res;
    }
  }

  List<Map<String, Object>> concat(List<Map<String, Object>> row, Map<String, Object> o) {
    List<Map<String, Object>> res = new ArrayList<>(row);
    res.add(o);
    return res;
  }

  /**
   * traverse results is list of objects
   */
  @SuppressWarnings("unchecked")
  List<Map<String, Object>> traverse(Data data, SecurityContext sc, String database, String table,
      String id, String fk) throws Exception {
    Object res = data.traverse(sc, database, table, id, fk);
    if (res instanceof List)
      return (List<Map<String, Object>>) res;
    if (res instanceof Map)
      return Arrays.asList((Map<String, Object>) res);
    return Arrays.asList();
  }

  boolean checkContext(VariableName ctx, String ref) {
    ctx.ref = ref;
    if (ctx.name == null)
      ;
    else if (!ctx.name.equals(ref))
      // ref type and table type do not match
      return false;
    return true;
  }

  /**
   * given a variable binding map, evaluates the "ret" projection
   */
  @SuppressWarnings("unchecked")
  Map<String, Object> project(Map<String, Object> vars, Map<String, Object> path) {
    Map<String, Object> projected = new LinkedHashMap<>();
    for (List<String> var : ret) {
      Object current = vars;
      for (String p : var) {
        if (current instanceof Map)
          if (p.equals(pathVariable))
            current = path;
          else
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
  Resource getResource(String database, String table, Map<String, Object> object) {
    List<Object> keys = new ArrayList<>();
    for (Property prop : dbs.get(database).tables.get(table).properties.values()) {
      if (prop.pkpos != null) {
        while (keys.size() <= prop.pkpos)
          keys.add(null);
        keys.set(prop.pkpos, object.get(prop.name));
      }
    }
    return Resource.of(database, table, keys);
  }

  void addResource(String database, String table, Map<String, Object> object) {
    object.put("_dj_resource", getResource(database, table, object));
  }

  /**
   * get the property name with pkpos=0
   */
  List<String> pks(AbstractDatabase db, String table) {
    List<String> res = new ArrayList<>();
    for (Property p : db.tables.get(table).properties.values())
      if (p.pkpos != null)
        res.add(p.name);
    return res;
  }

  String pk(AbstractDatabase db, String table) {
    return pks(db, table).get(0);
  }

  String guessTable(String table) {
    for (AbstractDatabase db : dbs.values())
      for (org.dashjoin.model.Table t : db.tables.values())
        if (t.name.equals(table))
          return t.ID;
    return null;
  }
}
