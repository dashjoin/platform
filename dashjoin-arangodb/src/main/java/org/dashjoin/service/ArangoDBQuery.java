package org.dashjoin.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.dashjoin.service.arangodb.QueryBaseListener;
import org.dashjoin.service.arangodb.QueryLexer;
import org.dashjoin.service.arangodb.QueryListener;
import org.dashjoin.service.arangodb.QueryParser;

public class ArangoDBQuery {

  public static class Project {

    Map<String, String> vars = new LinkedHashMap<>();

    @Override
    public String toString() {
      String res = "{";
      List<String> list = new ArrayList<>();
      for (Entry<String, String> e : vars.entrySet()) {
        list.add(e.getKey() + ": " + e.getValue());
      }
      res += String.join(", ", list);
      res += "}";
      return res;
    }
  }

  public static class Expression {
    public String operator;
    public String left;
    public String right;

    public Expression(String left, String operator, String right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public String toString() {
      return left + operator + right;
    }
  }

  public String collection;

  public String variable;

  public Project project = new Project();

  public Integer limit;

  public Integer offset;

  public String sort;

  public boolean distinct;

  public List<Expression> filters = new ArrayList<>();

  public ArangoDBQuery(String query) throws IOException {

    BaseErrorListener errorHandler = new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new RuntimeException("line " + line + ":" + charPositionInLine + " " + msg);
      }
    };

    CharStream input = CharStreams.fromStream(new ByteArrayInputStream(query.getBytes()));
    QueryLexer lexer = new QueryLexer(input);
    lexer.addErrorListener(errorHandler);
    CommonTokenStream token = new CommonTokenStream(lexer);
    QueryParser parser = new QueryParser(token);
    parser.addErrorListener(errorHandler);
    ParseTree tree = parser.query();

    QueryListener listener = new QueryBaseListener() {
      @Override
      public void exitSort(QueryParser.SortContext ctx) {
        sort = ctx.getChild(1).getText() + "." + ctx.getChild(3).getText();
        if (ctx.getChildCount() == 5)
          sort = sort + " " + ctx.getChild(4).getText();
      }

      @Override
      public void exitLimit(QueryParser.LimitContext ctx) {
        System.out.println();
        if (ctx.getChildCount() == 4) {
          offset = Integer.parseInt(ctx.getChild(1).getText());
          limit = Integer.parseInt(ctx.getChild(3).getText());
        } else {
          limit = Integer.parseInt(ctx.getChild(1).getText());
        }
      }

      @Override
      public void exitFilter(QueryParser.FilterContext ctx) {
        filters.add(new Expression(ctx.getChild(1).getText() + "." + ctx.getChild(3).getText(),
            "==", ctx.getChild(5).getText()));
      }

      @Override
      public void exitPair(QueryParser.PairContext ctx) {
        project.vars.put(ctx.getChild(0).getText(),
            ctx.getChild(2).getText() + "." + ctx.getChild(4).getText());
      }
    };

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(listener, tree);

    variable = tree.getChild(1).getText();
    collection = tree.getChild(3).getText();

    distinct = tree.getChild(tree.getChildCount() - 2).getText().equals("DISTINCT");
  }

  @Override
  public String toString() {
    String f = "";
    for (Expression e : filters)
      f = f + "FILTER " + e + " ";
    String lim = "";
    if (limit != null || offset != null) {
      if (offset == null)
        lim = "LIMIT " + limit + " ";
      else
        lim = "LIMIT " + offset + "," + limit + " ";
    }
    return "FOR " + variable + " IN " + collection + " "
        + (sort == null ? "" : "SORT " + sort + " ") + lim + f + "RETURN "
        + (distinct ? "DISTINCT " : "") + project;
  }
}
