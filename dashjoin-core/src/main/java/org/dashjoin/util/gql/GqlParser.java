package org.dashjoin.util.gql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dashjoin.util.gql.GqlQuery.Compare;
import org.dashjoin.util.gql.GqlQuery.Direction;
import org.dashjoin.util.gql.GqlQuery.EdgePattern;
import org.dashjoin.util.gql.GqlQuery.Expr;
import org.dashjoin.util.gql.GqlQuery.IsNull;
import org.dashjoin.util.gql.GqlQuery.Literal;
import org.dashjoin.util.gql.GqlQuery.Logical;
import org.dashjoin.util.gql.GqlQuery.Mode;
import org.dashjoin.util.gql.GqlQuery.NodePattern;
import org.dashjoin.util.gql.GqlQuery.Not;
import org.dashjoin.util.gql.GqlQuery.PathLength;
import org.dashjoin.util.gql.GqlQuery.Ref;
import org.dashjoin.util.gql.GqlQuery.ReturnItem;
import org.dashjoin.util.gql.GqlQuery.Selector;

/**
 * tokenizer and recursive descent parser for the GQL subset described in {@link GqlQuery}
 */
class GqlParser {

  /** reserved words: not usable as variables or labels unless `quoted` */
  static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("MATCH", "WHERE", "RETURN",
      "AS", "DISTINCT", "LIMIT", "ANY", "ALL", "SHORTEST", "PATH", "PATHS", "WALK", "TRAIL",
      "ACYCLIC", "SIMPLE", "AND", "OR", "NOT", "IS", "NULL", "TRUE", "FALSE", "IN"));

  enum Type {
    /** identifier or keyword */
    NAME,
    /** `quoted name` */
    QUOTED,
    STRING, NUMBER, PARAM, SYMBOL, END
  }

  static class Token {
    Type type;
    String text;
    int pos;

    Token(Type type, String text, int pos) {
      this.type = type;
      this.text = text;
      this.pos = pos;
    }

    boolean keyword(String k) {
      return type == Type.NAME && text.equalsIgnoreCase(k);
    }

    boolean symbol(String s) {
      return type == Type.SYMBOL && text.equals(s);
    }
  }

  final String text;
  final Map<String, Object> arguments;
  final List<Token> tokens = new ArrayList<>();
  int i;

  GqlParser(String text, Map<String, Object> arguments) {
    this.text = text;
    this.arguments = arguments;
    tokenize();
  }

  /* =============================================================================== tokens */

  void tokenize() {
    int p = 0;
    while (p < text.length()) {
      char c = text.charAt(p);
      if (Character.isWhitespace(c)) {
        p++;
        continue;
      }
      int start = p;
      if (Character.isLetter(c) || c == '_') {
        while (p < text.length()
            && (Character.isLetterOrDigit(text.charAt(p)) || text.charAt(p) == '_'))
          p++;
        tokens.add(new Token(Type.NAME, text.substring(start, p), start));
      } else if (Character.isDigit(c)) {
        while (p < text.length() && Character.isDigit(text.charAt(p)))
          p++;
        // a fraction, but not a range like 1..2
        if (p + 1 < text.length() && text.charAt(p) == '.'
            && Character.isDigit(text.charAt(p + 1))) {
          p++;
          while (p < text.length() && Character.isDigit(text.charAt(p)))
            p++;
        }
        tokens.add(new Token(Type.NUMBER, text.substring(start, p), start));
      } else if (c == '`') {
        StringBuilder s = new StringBuilder();
        p++;
        while (true) {
          if (p >= text.length())
            throw error(start, "unterminated `name`");
          if (text.charAt(p) == '`') {
            if (p + 1 < text.length() && text.charAt(p + 1) == '`') {
              s.append('`');
              p += 2;
              continue;
            }
            p++;
            break;
          }
          s.append(text.charAt(p++));
        }
        tokens.add(new Token(Type.QUOTED, s.toString(), start));
      } else if (c == '\'' || c == '"') {
        StringBuilder s = new StringBuilder();
        p++;
        while (true) {
          if (p >= text.length())
            throw error(start, "unterminated string");
          char x = text.charAt(p);
          if (x == '\\' && p + 1 < text.length()) {
            s.append(text.charAt(p + 1));
            p += 2;
            continue;
          }
          if (x == c) {
            // SQL style doubled quote
            if (p + 1 < text.length() && text.charAt(p + 1) == c) {
              s.append(c);
              p += 2;
              continue;
            }
            p++;
            break;
          }
          s.append(x);
          p++;
        }
        tokens.add(new Token(Type.STRING, s.toString(), start));
      } else if (c == '$') {
        p++;
        while (p < text.length()
            && (Character.isLetterOrDigit(text.charAt(p)) || text.charAt(p) == '_'))
          p++;
        if (p == start + 1)
          throw error(start, "a parameter needs a name: $name");
        tokens.add(new Token(Type.PARAM, text.substring(start + 1, p), start));
      } else {
        String two = p + 1 < text.length() ? text.substring(p, p + 2) : "";
        if (two.equals("<>") || two.equals("<=") || two.equals(">=") || two.equals("!=")) {
          tokens.add(new Token(Type.SYMBOL, two.equals("!=") ? "<>" : two, start));
          p += 2;
        } else if ("()[]{},:.|=<>-*+".indexOf(c) >= 0) {
          tokens.add(new Token(Type.SYMBOL, "" + c, start));
          p++;
        } else
          throw error(start, "unexpected character '" + c + "'");
      }
    }
    tokens.add(new Token(Type.END, "", text.length()));
  }

  Token peek() {
    return tokens.get(i);
  }

  Token peek(int ahead) {
    return tokens.get(Math.min(i + ahead, tokens.size() - 1));
  }

  Token next() {
    return tokens.get(i++);
  }

  boolean acceptSymbol(String s) {
    if (peek().symbol(s)) {
      i++;
      return true;
    }
    return false;
  }

  boolean acceptKeyword(String k) {
    if (peek().keyword(k)) {
      i++;
      return true;
    }
    return false;
  }

  void expectSymbol(String s) {
    if (!acceptSymbol(s))
      throw error(peek().pos, "expected '" + s + "'" + found());
  }

  void expectKeyword(String k) {
    if (!acceptKeyword(k))
      throw error(peek().pos, "expected " + k + found());
  }

  String found() {
    Token t = peek();
    return t.type == Type.END ? " at the end" : ", found '" + t.text + "'";
  }

  IllegalArgumentException error(int pos, String message) {
    int line = 1, col = 1;
    for (int p = 0; p < pos && p < text.length(); p++)
      if (text.charAt(p) == '\n') {
        line++;
        col = 1;
      } else
        col++;
    return new IllegalArgumentException("GQL " + line + ":" + col + ": " + message);
  }

  /** a variable or label: identifier (not a keyword) or `quoted` */
  String name(String what) {
    Token t = peek();
    if (t.type == Type.QUOTED || (t.type == Type.NAME && !KEYWORDS.contains(t.text.toUpperCase()))) {
      i++;
      return t.text;
    }
    throw error(t.pos, "expected " + what + found());
  }

  boolean atName() {
    Token t = peek();
    return t.type == Type.QUOTED
        || (t.type == Type.NAME && !KEYWORDS.contains(t.text.toUpperCase()));
  }

  int integer(String what) {
    Token t = peek();
    if (t.type != Type.NUMBER || t.text.contains("."))
      throw error(t.pos, "expected " + what + found());
    i++;
    return Integer.parseInt(t.text);
  }

  /* =============================================================================== query */

  GqlQuery query() {
    GqlQuery q = new GqlQuery();
    expectKeyword("MATCH");
    if (atName() && peek(1).symbol("=")) {
      q.pathVariable = name("path variable");
      expectSymbol("=");
    }
    selector(q);
    mode(q);
    q.nodes.add(node());
    while (peek().symbol("-") || peek().symbol("<")) {
      q.edges.add(edge());
      q.nodes.add(node());
    }
    if (peek().symbol(","))
      throw error(peek().pos, "only one path pattern is supported");
    if (acceptKeyword("WHERE"))
      q.where = expr();
    expectKeyword("RETURN");
    q.distinct = acceptKeyword("DISTINCT");
    do {
      ReturnItem item = new ReturnItem();
      int start = peek().pos;
      item.expr = expr();
      if (acceptKeyword("AS"))
        item.name = name("a name after AS");
      else
        // like the platform's other queries: the column is named as written (p.NAME)
        item.name = item.expr instanceof Ref ? item.expr.toString().replace("`", "")
            : text.substring(start, peek().pos).trim();
      q.ret.add(item);
    } while (acceptSymbol(","));
    if (acceptKeyword("LIMIT"))
      q.limit = integer("a number after LIMIT");
    if (peek().type != Type.END)
      throw error(peek().pos, "unexpected '" + peek().text + "'");
    return q;
  }

  void selector(GqlQuery q) {
    if (acceptKeyword("ANY")) {
      if (acceptKeyword("SHORTEST"))
        q.selector = Selector.ANY_SHORTEST;
      else {
        q.selector = Selector.ANY;
        if (peek().type == Type.NUMBER)
          q.k = integer("a number");
      }
    } else if (acceptKeyword("ALL")) {
      q.selector = acceptKeyword("SHORTEST") ? Selector.ALL_SHORTEST : Selector.ALL;
    } else if (acceptKeyword("SHORTEST")) {
      q.selector = Selector.SHORTEST;
      q.k = integer("a number after SHORTEST");
    } else
      return;
    if (q.k < 1)
      throw error(peek().pos, "the number of paths must be at least 1");
    if (!acceptKeyword("PATH"))
      acceptKeyword("PATHS");
  }

  void mode(GqlQuery q) {
    for (Mode m : Mode.values())
      if (acceptKeyword(m.name())) {
        q.mode = m;
        if (!acceptKeyword("PATH"))
          acceptKeyword("PATHS");
        return;
      }
  }

  NodePattern node() {
    NodePattern n = new NodePattern();
    expectSymbol("(");
    if (atName())
      n.variable = name("a variable");
    if (acceptSymbol(":") || acceptKeyword("IS"))
      n.labels = labels();
    if (acceptSymbol("{")) {
      if (!peek().symbol("}"))
        do {
          String key = name("a property name");
          expectSymbol(":");
          n.properties.put(key, value());
        } while (acceptSymbol(","));
      expectSymbol("}");
    }
    if (acceptKeyword("WHERE"))
      n.where = expr();
    expectSymbol(")");
    return n;
  }

  List<String> labels() {
    List<String> res = new ArrayList<>();
    do
      res.add(name("a label"));
    while (acceptSymbol("|"));
    return res;
  }

  /**
   * full edges -[...]-&gt;, &lt;-[...]-, -[...]-, &lt;-[...]-&gt; (either direction), the GQL
   * abbreviations -&gt;, &lt;-, -, &lt;-&gt; and the Cypher style --&gt;, &lt;--, --; then an optional
   * quantifier
   */
  EdgePattern edge() {
    EdgePattern e = new EdgePattern();
    boolean left = acceptSymbol("<");
    expectSymbol("-");
    boolean right;
    if (acceptSymbol("[")) {
      if (atName())
        e.variable = name("a variable");
      if (acceptSymbol(":") || acceptKeyword("IS"))
        e.labels = labels();
      if (peek().symbol("{") || peek().keyword("WHERE"))
        throw error(peek().pos, "relationships are foreign keys and have no properties");
      if (peek().symbol("*"))
        throw error(peek().pos,
            "write the number of hops after the edge: -[:REL]->{1,5} (GQL), not -[:REL*1..5]-> (Cypher)");
      expectSymbol("]");
      expectSymbol("-");
      right = acceptSymbol(">");
    } else if (acceptSymbol("-"))
      // Cypher style --> <-- --
      right = acceptSymbol(">");
    else
      right = acceptSymbol(">");
    e.direction = left == right ? Direction.ANY : left ? Direction.LEFT : Direction.RIGHT;
    quantifier(e);
    return e;
  }

  void quantifier(EdgePattern e) {
    if (acceptSymbol("*")) {
      e.min = 0;
      e.max = null;
    } else if (acceptSymbol("+")) {
      e.min = 1;
      e.max = null;
    } else if (acceptSymbol("{")) {
      if (acceptSymbol(",")) {
        e.min = 0;
        e.max = integer("the maximum number of hops");
      } else {
        e.min = integer("the number of hops");
        if (acceptSymbol(","))
          e.max = peek().symbol("}") ? null : integer("the maximum number of hops");
        else
          e.max = e.min;
      }
      expectSymbol("}");
    }
  }

  /** a literal or parameter value (node properties) */
  Object value() {
    Token t = peek();
    Expr x = primary();
    if (x instanceof Literal)
      return ((Literal) x).value;
    throw error(t.pos, "expected a value");
  }

  /* ========================================================================== expressions */

  Expr expr() {
    Expr left = and();
    while (acceptKeyword("OR"))
      left = new Logical(false, left, and());
    return left;
  }

  Expr and() {
    Expr left = not();
    while (acceptKeyword("AND"))
      left = new Logical(true, left, not());
    return left;
  }

  Expr not() {
    if (acceptKeyword("NOT"))
      return new Not(not());
    return comparison();
  }

  Expr comparison() {
    Expr left = primary();
    if (acceptKeyword("IS")) {
      boolean not = acceptKeyword("NOT");
      expectKeyword("NULL");
      return new IsNull(left, not);
    }
    if (acceptKeyword("IN"))
      return new Compare("IN", left, primary());
    for (String op : new String[] {"=", "<>", "<=", ">=", "<", ">"})
      if (acceptSymbol(op))
        return new Compare(op, left, primary());
    return left;
  }

  Expr primary() {
    Token t = peek();
    if (acceptSymbol("(")) {
      Expr e = expr();
      expectSymbol(")");
      return e;
    }
    if (acceptSymbol("-")) {
      Token n = peek();
      if (n.type != Type.NUMBER)
        throw error(n.pos, "expected a number after -");
      i++;
      return new Literal(number("-" + n.text));
    }
    if (acceptSymbol("[")) {
      List<Object> list = new ArrayList<>();
      if (!peek().symbol("]"))
        do
          list.add(value());
        while (acceptSymbol(","));
      expectSymbol("]");
      return new Literal(list);
    }
    switch (t.type) {
      case NUMBER:
        i++;
        return new Literal(number(t.text));
      case STRING:
        i++;
        return new Literal(t.text);
      case PARAM:
        i++;
        if (arguments == null || !arguments.containsKey(t.text))
          throw error(t.pos, "no value for parameter $" + t.text);
        return new Literal(arguments.get(t.text));
      default:
    }
    if (acceptKeyword("TRUE"))
      return new Literal(true);
    if (acceptKeyword("FALSE"))
      return new Literal(false);
    if (acceptKeyword("NULL"))
      return new Literal(null);
    if (t.type == Type.NAME && t.text.equalsIgnoreCase("PATH_LENGTH") && peek(1).symbol("(")) {
      i += 2;
      Expr p = expr();
      expectSymbol(")");
      return new PathLength(p);
    }
    if (atName()) {
      String variable = name("a variable");
      List<String> fields = new ArrayList<>();
      while (acceptSymbol("."))
        fields.add(fieldName());
      return new Ref(variable, fields);
    }
    throw error(t.pos, "expected a value or variable" + found());
  }

  /** after a dot any name is a field, also keywords: a.NAME, p.start */
  String fieldName() {
    Token t = peek();
    if (t.type == Type.NAME || t.type == Type.QUOTED) {
      i++;
      return t.text;
    }
    throw error(t.pos, "expected a field name" + found());
  }

  static Object number(String s) {
    if (s.contains("."))
      return Double.parseDouble(s);
    long l = Long.parseLong(s);
    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE)
      return (int) l;
    return l;
  }
}
