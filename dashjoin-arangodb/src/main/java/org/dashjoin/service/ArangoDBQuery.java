package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArangoDBQuery {

  public static class Expression {
    public String operator;
    public String left;
    public String right;

    public Expression(String s) {
      String[] parts = s.split("==");
      if (parts.length == 2) {
        this.left = parts[0];
        this.right = parts[1];
        this.operator = "==";
      } else
        throw new RuntimeException("unsupported operator: " + s);
    }

    @Override
    public String toString() {
      return left + operator + right;
    }
  }

  public String collection;

  public String variable;

  public String project;

  public Integer limit;

  public Integer offset;

  public List<Expression> filters = new ArrayList<>();

  public ArangoDBQuery(String query) {
    query = query.trim();
    String[] parts = query.split("\\s+");
    if (!parts[0].equalsIgnoreCase("for") || !parts[2].equalsIgnoreCase("in"))
      throw new RuntimeException("Only FOR ... IN queries supported");

    List<String> filt = new ArrayList<>(Arrays.asList(query.split("filter|FILTER")));
    filt.remove(0);
    if (!filt.isEmpty())
      filt.set(filt.size() - 1, filt.get(filt.size() - 1).split("return|RETURN")[0]);
    for (String f : filt) {
      f = f.trim();
      filters.add(new Expression(f));
    }

    String lmt = between(query, "limit", "return");
    if (lmt != null) {
      String[] lmts = lmt.split(",");
      if (lmts.length == 1)
        limit = Integer.parseInt(lmts[0].trim());
      else {
        offset = Integer.parseInt(lmts[0].trim());
        limit = Integer.parseInt(lmts[1].trim());
      }
    }

    variable = parts[1];
    collection = parts[3];
    project = query.split("return|RETURN")[1].trim();
  }

  static String between(String query, String from, String to) {
    String lower = query.toLowerCase();
    int l = lower.indexOf(from);
    int r = lower.indexOf(to);
    if (r >= 0 && l >= 0)
      return query.substring(l + from.length(), r);
    return null;
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
    return "FOR " + variable + " IN " + collection + " " + f + lim + "RETURN " + project;
  }
}
