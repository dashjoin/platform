package org.dashjoin.util.cypher;

import java.util.List;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
// Generated from Cypher.g4 by ANTLR 4.8
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CypherParser extends Parser {
  static {
    RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
  }

  protected static final DFA[] _decisionToDFA;
  protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
  public static final int T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7,
      T__7 = 8, T__8 = 9, T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15,
      T__15 = 16, T__16 = 17, T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22,
      T__22 = 23, T__23 = 24, T__24 = 25, T__25 = 26, T__26 = 27, T__27 = 28, T__28 = 29,
      T__29 = 30, T__30 = 31, T__31 = 32, T__32 = 33, T__33 = 34, T__34 = 35, T__35 = 36,
      T__36 = 37, T__37 = 38, T__38 = 39, T__39 = 40, T__40 = 41, T__41 = 42, T__42 = 43,
      T__43 = 44, T__44 = 45, UNION = 46, ALL = 47, OPTIONAL = 48, MATCH = 49, UNWIND = 50, AS = 51,
      MERGE = 52, ON = 53, CREATE = 54, SET = 55, DETACH = 56, DELETE = 57, REMOVE = 58, CALL = 59,
      YIELD = 60, WITH = 61, RETURN = 62, DISTINCT = 63, ORDER = 64, BY = 65, L_SKIP = 66,
      LIMIT = 67, ASCENDING = 68, ASC = 69, DESCENDING = 70, DESC = 71, WHERE = 72, OR = 73,
      XOR = 74, AND = 75, NOT = 76, IN = 77, STARTS = 78, ENDS = 79, CONTAINS = 80, IS = 81,
      NULL = 82, COUNT = 83, ANY = 84, NONE = 85, SINGLE = 86, TRUE = 87, FALSE = 88, EXISTS = 89,
      CASE = 90, ELSE = 91, END = 92, WHEN = 93, THEN = 94, StringLiteral = 95, EscapedChar = 96,
      HexInteger = 97, DecimalInteger = 98, OctalInteger = 99, HexLetter = 100, HexDigit = 101,
      Digit = 102, NonZeroDigit = 103, NonZeroOctDigit = 104, OctDigit = 105, ZeroDigit = 106,
      ExponentDecimalReal = 107, RegularDecimalReal = 108, CONSTRAINT = 109, DO = 110, FOR = 111,
      REQUIRE = 112, UNIQUE = 113, MANDATORY = 114, SCALAR = 115, OF = 116, ADD = 117, DROP = 118,
      FILTER = 119, EXTRACT = 120, UnescapedSymbolicName = 121, IdentifierStart = 122,
      IdentifierPart = 123, EscapedSymbolicName = 124, SP = 125, WHITESPACE = 126, Comment = 127;
  public static final int RULE_oC_Cypher = 0, RULE_oC_Statement = 1, RULE_oC_Query = 2,
      RULE_oC_RegularQuery = 3, RULE_oC_Union = 4, RULE_oC_SingleQuery = 5,
      RULE_oC_SinglePartQuery = 6, RULE_oC_MultiPartQuery = 7, RULE_oC_UpdatingClause = 8,
      RULE_oC_ReadingClause = 9, RULE_oC_Match = 10, RULE_oC_Unwind = 11, RULE_oC_Merge = 12,
      RULE_oC_MergeAction = 13, RULE_oC_Create = 14, RULE_oC_Set = 15, RULE_oC_SetItem = 16,
      RULE_oC_Delete = 17, RULE_oC_Remove = 18, RULE_oC_RemoveItem = 19, RULE_oC_InQueryCall = 20,
      RULE_oC_StandaloneCall = 21, RULE_oC_YieldItems = 22, RULE_oC_YieldItem = 23,
      RULE_oC_With = 24, RULE_oC_Return = 25, RULE_oC_ProjectionBody = 26,
      RULE_oC_ProjectionItems = 27, RULE_oC_ProjectionItem = 28, RULE_oC_Order = 29,
      RULE_oC_Skip = 30, RULE_oC_Limit = 31, RULE_oC_SortItem = 32, RULE_oC_Where = 33,
      RULE_oC_Pattern = 34, RULE_oC_PatternPart = 35, RULE_oC_AnonymousPatternPart = 36,
      RULE_oC_PatternElement = 37, RULE_oC_NodePattern = 38, RULE_oC_PatternElementChain = 39,
      RULE_oC_RelationshipPattern = 40, RULE_oC_RelationshipDetail = 41, RULE_oC_Properties = 42,
      RULE_oC_RelationshipTypes = 43, RULE_oC_NodeLabels = 44, RULE_oC_NodeLabel = 45,
      RULE_oC_RangeLiteral = 46, RULE_oC_LabelName = 47, RULE_oC_RelTypeName = 48,
      RULE_oC_Expression = 49, RULE_oC_OrExpression = 50, RULE_oC_XorExpression = 51,
      RULE_oC_AndExpression = 52, RULE_oC_NotExpression = 53, RULE_oC_ComparisonExpression = 54,
      RULE_oC_AddOrSubtractExpression = 55, RULE_oC_MultiplyDivideModuloExpression = 56,
      RULE_oC_PowerOfExpression = 57, RULE_oC_UnaryAddOrSubtractExpression = 58,
      RULE_oC_StringListNullOperatorExpression = 59, RULE_oC_ListOperatorExpression = 60,
      RULE_oC_StringOperatorExpression = 61, RULE_oC_NullOperatorExpression = 62,
      RULE_oC_PropertyOrLabelsExpression = 63, RULE_oC_Atom = 64, RULE_oC_Literal = 65,
      RULE_oC_BooleanLiteral = 66, RULE_oC_ListLiteral = 67,
      RULE_oC_PartialComparisonExpression = 68, RULE_oC_ParenthesizedExpression = 69,
      RULE_oC_RelationshipsPattern = 70, RULE_oC_FilterExpression = 71, RULE_oC_IdInColl = 72,
      RULE_oC_FunctionInvocation = 73, RULE_oC_FunctionName = 74, RULE_oC_ExistentialSubquery = 75,
      RULE_oC_ExplicitProcedureInvocation = 76, RULE_oC_ImplicitProcedureInvocation = 77,
      RULE_oC_ProcedureResultField = 78, RULE_oC_ProcedureName = 79, RULE_oC_Namespace = 80,
      RULE_oC_ListComprehension = 81, RULE_oC_PatternComprehension = 82,
      RULE_oC_PropertyLookup = 83, RULE_oC_CaseExpression = 84, RULE_oC_CaseAlternative = 85,
      RULE_oC_Variable = 86, RULE_oC_NumberLiteral = 87, RULE_oC_MapLiteral = 88,
      RULE_oC_Parameter = 89, RULE_oC_PropertyExpression = 90, RULE_oC_PropertyKeyName = 91,
      RULE_oC_IntegerLiteral = 92, RULE_oC_DoubleLiteral = 93, RULE_oC_SchemaName = 94,
      RULE_oC_ReservedWord = 95, RULE_oC_SymbolicName = 96, RULE_oC_LeftArrowHead = 97,
      RULE_oC_RightArrowHead = 98, RULE_oC_Dash = 99;

  private static String[] makeRuleNames() {
    return new String[] {"oC_Cypher", "oC_Statement", "oC_Query", "oC_RegularQuery", "oC_Union",
        "oC_SingleQuery", "oC_SinglePartQuery", "oC_MultiPartQuery", "oC_UpdatingClause",
        "oC_ReadingClause", "oC_Match", "oC_Unwind", "oC_Merge", "oC_MergeAction", "oC_Create",
        "oC_Set", "oC_SetItem", "oC_Delete", "oC_Remove", "oC_RemoveItem", "oC_InQueryCall",
        "oC_StandaloneCall", "oC_YieldItems", "oC_YieldItem", "oC_With", "oC_Return",
        "oC_ProjectionBody", "oC_ProjectionItems", "oC_ProjectionItem", "oC_Order", "oC_Skip",
        "oC_Limit", "oC_SortItem", "oC_Where", "oC_Pattern", "oC_PatternPart",
        "oC_AnonymousPatternPart", "oC_PatternElement", "oC_NodePattern", "oC_PatternElementChain",
        "oC_RelationshipPattern", "oC_RelationshipDetail", "oC_Properties", "oC_RelationshipTypes",
        "oC_NodeLabels", "oC_NodeLabel", "oC_RangeLiteral", "oC_LabelName", "oC_RelTypeName",
        "oC_Expression", "oC_OrExpression", "oC_XorExpression", "oC_AndExpression",
        "oC_NotExpression", "oC_ComparisonExpression", "oC_AddOrSubtractExpression",
        "oC_MultiplyDivideModuloExpression", "oC_PowerOfExpression",
        "oC_UnaryAddOrSubtractExpression", "oC_StringListNullOperatorExpression",
        "oC_ListOperatorExpression", "oC_StringOperatorExpression", "oC_NullOperatorExpression",
        "oC_PropertyOrLabelsExpression", "oC_Atom", "oC_Literal", "oC_BooleanLiteral",
        "oC_ListLiteral", "oC_PartialComparisonExpression", "oC_ParenthesizedExpression",
        "oC_RelationshipsPattern", "oC_FilterExpression", "oC_IdInColl", "oC_FunctionInvocation",
        "oC_FunctionName", "oC_ExistentialSubquery", "oC_ExplicitProcedureInvocation",
        "oC_ImplicitProcedureInvocation", "oC_ProcedureResultField", "oC_ProcedureName",
        "oC_Namespace", "oC_ListComprehension", "oC_PatternComprehension", "oC_PropertyLookup",
        "oC_CaseExpression", "oC_CaseAlternative", "oC_Variable", "oC_NumberLiteral",
        "oC_MapLiteral", "oC_Parameter", "oC_PropertyExpression", "oC_PropertyKeyName",
        "oC_IntegerLiteral", "oC_DoubleLiteral", "oC_SchemaName", "oC_ReservedWord",
        "oC_SymbolicName", "oC_LeftArrowHead", "oC_RightArrowHead", "oC_Dash"};
  }

  public static final String[] ruleNames = makeRuleNames();

  private static String[] makeLiteralNames() {
    return new String[] {null, "';'", "','", "'='", "'+='", "'*'", "'('", "')'", "'['", "']'",
        "':'", "'|'", "'..'", "'+'", "'-'", "'/'", "'%'", "'^'", "'<>'", "'<'", "'>'", "'<='",
        "'>='", "'{'", "'}'", "'.'", "'$'", "'\u27E8'", "'\u3008'", "'\uFE64'", "'\uFF1C'",
        "'\u27E9'", "'\u3009'", "'\uFE65'", "'\uFF1E'", "'\u00AD'", "'\u2010'", "'\u2011'",
        "'\u2012'", "'\u2013'", "'\u2014'", "'\u2015'", "'\u2212'", "'\uFE58'", "'\uFE63'",
        "'\uFF0D'", null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, "'0'"};
  }

  private static final String[] _LITERAL_NAMES = makeLiteralNames();

  private static String[] makeSymbolicNames() {
    return new String[] {null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, "UNION", "ALL", "OPTIONAL", "MATCH", "UNWIND", "AS", "MERGE", "ON",
        "CREATE", "SET", "DETACH", "DELETE", "REMOVE", "CALL", "YIELD", "WITH", "RETURN",
        "DISTINCT", "ORDER", "BY", "L_SKIP", "LIMIT", "ASCENDING", "ASC", "DESCENDING", "DESC",
        "WHERE", "OR", "XOR", "AND", "NOT", "IN", "STARTS", "ENDS", "CONTAINS", "IS", "NULL",
        "COUNT", "ANY", "NONE", "SINGLE", "TRUE", "FALSE", "EXISTS", "CASE", "ELSE", "END", "WHEN",
        "THEN", "StringLiteral", "EscapedChar", "HexInteger", "DecimalInteger", "OctalInteger",
        "HexLetter", "HexDigit", "Digit", "NonZeroDigit", "NonZeroOctDigit", "OctDigit",
        "ZeroDigit", "ExponentDecimalReal", "RegularDecimalReal", "CONSTRAINT", "DO", "FOR",
        "REQUIRE", "UNIQUE", "MANDATORY", "SCALAR", "OF", "ADD", "DROP", "FILTER", "EXTRACT",
        "UnescapedSymbolicName", "IdentifierStart", "IdentifierPart", "EscapedSymbolicName", "SP",
        "WHITESPACE", "Comment"};
  }

  private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
  public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

  /**
   * @deprecated Use {@link #VOCABULARY} instead.
   */
  @Deprecated
  public static final String[] tokenNames;
  static {
    tokenNames = new String[_SYMBOLIC_NAMES.length];
    for (int i = 0; i < tokenNames.length; i++) {
      tokenNames[i] = VOCABULARY.getLiteralName(i);
      if (tokenNames[i] == null) {
        tokenNames[i] = VOCABULARY.getSymbolicName(i);
      }

      if (tokenNames[i] == null) {
        tokenNames[i] = "<INVALID>";
      }
    }
  }

  @Override
  @Deprecated
  public String[] getTokenNames() {
    return tokenNames;
  }

  @Override

  public Vocabulary getVocabulary() {
    return VOCABULARY;
  }

  @Override
  public String getGrammarFileName() {
    return "Cypher.g4";
  }

  @Override
  public String[] getRuleNames() {
    return ruleNames;
  }

  @Override
  public String getSerializedATN() {
    return _serializedATN;
  }

  @Override
  public ATN getATN() {
    return _ATN;
  }

  public CypherParser(TokenStream input) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
  }

  public static class OC_CypherContext extends ParserRuleContext {
    public OC_StatementContext oC_Statement() {
      return getRuleContext(OC_StatementContext.class, 0);
    }

    public TerminalNode EOF() {
      return getToken(CypherParser.EOF, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_CypherContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Cypher;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Cypher(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Cypher(this);
    }
  }

  public final OC_CypherContext oC_Cypher() throws RecognitionException {
    OC_CypherContext _localctx = new OC_CypherContext(_ctx, getState());
    enterRule(_localctx, 0, RULE_oC_Cypher);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(201);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(200);
            match(SP);
          }
        }

        setState(203);
        oC_Statement();
        setState(208);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 2, _ctx)) {
          case 1: {
            setState(205);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(204);
                match(SP);
              }
            }

            setState(207);
            match(T__0);
          }
            break;
        }
        setState(211);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(210);
            match(SP);
          }
        }

        setState(213);
        match(EOF);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_StatementContext extends ParserRuleContext {
    public OC_QueryContext oC_Query() {
      return getRuleContext(OC_QueryContext.class, 0);
    }

    public OC_StatementContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Statement;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Statement(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Statement(this);
    }
  }

  public final OC_StatementContext oC_Statement() throws RecognitionException {
    OC_StatementContext _localctx = new OC_StatementContext(_ctx, getState());
    enterRule(_localctx, 2, RULE_oC_Statement);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(215);
        oC_Query();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_QueryContext extends ParserRuleContext {
    public OC_RegularQueryContext oC_RegularQuery() {
      return getRuleContext(OC_RegularQueryContext.class, 0);
    }

    public OC_StandaloneCallContext oC_StandaloneCall() {
      return getRuleContext(OC_StandaloneCallContext.class, 0);
    }

    public OC_QueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Query;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Query(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Query(this);
    }
  }

  public final OC_QueryContext oC_Query() throws RecognitionException {
    OC_QueryContext _localctx = new OC_QueryContext(_ctx, getState());
    enterRule(_localctx, 4, RULE_oC_Query);
    try {
      setState(219);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 4, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          setState(217);
          oC_RegularQuery();
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          setState(218);
          oC_StandaloneCall();
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RegularQueryContext extends ParserRuleContext {
    public OC_SingleQueryContext oC_SingleQuery() {
      return getRuleContext(OC_SingleQueryContext.class, 0);
    }

    public List<OC_UnionContext> oC_Union() {
      return getRuleContexts(OC_UnionContext.class);
    }

    public OC_UnionContext oC_Union(int i) {
      return getRuleContext(OC_UnionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_RegularQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RegularQuery;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RegularQuery(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RegularQuery(this);
    }
  }

  public final OC_RegularQueryContext oC_RegularQuery() throws RecognitionException {
    OC_RegularQueryContext _localctx = new OC_RegularQueryContext(_ctx, getState());
    enterRule(_localctx, 6, RULE_oC_RegularQuery);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(221);
        oC_SingleQuery();
        setState(228);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 6, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(223);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(222);
                    match(SP);
                  }
                }

                setState(225);
                oC_Union();
              }
            }
          }
          setState(230);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 6, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_UnionContext extends ParserRuleContext {
    public TerminalNode UNION() {
      return getToken(CypherParser.UNION, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode ALL() {
      return getToken(CypherParser.ALL, 0);
    }

    public OC_SingleQueryContext oC_SingleQuery() {
      return getRuleContext(OC_SingleQueryContext.class, 0);
    }

    public OC_UnionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Union;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Union(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Union(this);
    }
  }

  public final OC_UnionContext oC_Union() throws RecognitionException {
    OC_UnionContext _localctx = new OC_UnionContext(_ctx, getState());
    enterRule(_localctx, 8, RULE_oC_Union);
    int _la;
    try {
      setState(243);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 9, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(231);
            match(UNION);
            setState(232);
            match(SP);
            setState(233);
            match(ALL);
            setState(235);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(234);
                match(SP);
              }
            }

            setState(237);
            oC_SingleQuery();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(238);
            match(UNION);
            setState(240);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(239);
                match(SP);
              }
            }

            setState(242);
            oC_SingleQuery();
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SingleQueryContext extends ParserRuleContext {
    public OC_SinglePartQueryContext oC_SinglePartQuery() {
      return getRuleContext(OC_SinglePartQueryContext.class, 0);
    }

    public OC_MultiPartQueryContext oC_MultiPartQuery() {
      return getRuleContext(OC_MultiPartQueryContext.class, 0);
    }

    public OC_SingleQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SingleQuery;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SingleQuery(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SingleQuery(this);
    }
  }

  public final OC_SingleQueryContext oC_SingleQuery() throws RecognitionException {
    OC_SingleQueryContext _localctx = new OC_SingleQueryContext(_ctx, getState());
    enterRule(_localctx, 10, RULE_oC_SingleQuery);
    try {
      setState(247);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 10, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          setState(245);
          oC_SinglePartQuery();
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          setState(246);
          oC_MultiPartQuery();
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SinglePartQueryContext extends ParserRuleContext {
    public OC_ReturnContext oC_Return() {
      return getRuleContext(OC_ReturnContext.class, 0);
    }

    public List<OC_ReadingClauseContext> oC_ReadingClause() {
      return getRuleContexts(OC_ReadingClauseContext.class);
    }

    public OC_ReadingClauseContext oC_ReadingClause(int i) {
      return getRuleContext(OC_ReadingClauseContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_UpdatingClauseContext> oC_UpdatingClause() {
      return getRuleContexts(OC_UpdatingClauseContext.class);
    }

    public OC_UpdatingClauseContext oC_UpdatingClause(int i) {
      return getRuleContext(OC_UpdatingClauseContext.class, i);
    }

    public OC_SinglePartQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SinglePartQuery;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SinglePartQuery(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SinglePartQuery(this);
    }
  }

  public final OC_SinglePartQueryContext oC_SinglePartQuery() throws RecognitionException {
    OC_SinglePartQueryContext _localctx = new OC_SinglePartQueryContext(_ctx, getState());
    enterRule(_localctx, 12, RULE_oC_SinglePartQuery);
    int _la;
    try {
      int _alt;
      setState(284);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 19, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(255);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while ((((_la) & ~0x3f) == 0 && ((1L << _la)
                & ((1L << OPTIONAL) | (1L << MATCH) | (1L << UNWIND) | (1L << CALL))) != 0)) {
              {
                {
                  setState(249);
                  oC_ReadingClause();
                  setState(251);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(250);
                      match(SP);
                    }
                  }

                }
              }
              setState(257);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
            setState(258);
            oC_Return();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(265);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while ((((_la) & ~0x3f) == 0 && ((1L << _la)
                & ((1L << OPTIONAL) | (1L << MATCH) | (1L << UNWIND) | (1L << CALL))) != 0)) {
              {
                {
                  setState(259);
                  oC_ReadingClause();
                  setState(261);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(260);
                      match(SP);
                    }
                  }

                }
              }
              setState(267);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
            setState(268);
            oC_UpdatingClause();
            setState(275);
            _errHandler.sync(this);
            _alt = getInterpreter().adaptivePredict(_input, 16, _ctx);
            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
              if (_alt == 1) {
                {
                  {
                    setState(270);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(269);
                        match(SP);
                      }
                    }

                    setState(272);
                    oC_UpdatingClause();
                  }
                }
              }
              setState(277);
              _errHandler.sync(this);
              _alt = getInterpreter().adaptivePredict(_input, 16, _ctx);
            }
            setState(282);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 18, _ctx)) {
              case 1: {
                setState(279);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(278);
                    match(SP);
                  }
                }

                setState(281);
                oC_Return();
              }
                break;
            }
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MultiPartQueryContext extends ParserRuleContext {
    public OC_SinglePartQueryContext oC_SinglePartQuery() {
      return getRuleContext(OC_SinglePartQueryContext.class, 0);
    }

    public List<OC_WithContext> oC_With() {
      return getRuleContexts(OC_WithContext.class);
    }

    public OC_WithContext oC_With(int i) {
      return getRuleContext(OC_WithContext.class, i);
    }

    public List<OC_ReadingClauseContext> oC_ReadingClause() {
      return getRuleContexts(OC_ReadingClauseContext.class);
    }

    public OC_ReadingClauseContext oC_ReadingClause(int i) {
      return getRuleContext(OC_ReadingClauseContext.class, i);
    }

    public List<OC_UpdatingClauseContext> oC_UpdatingClause() {
      return getRuleContexts(OC_UpdatingClauseContext.class);
    }

    public OC_UpdatingClauseContext oC_UpdatingClause(int i) {
      return getRuleContext(OC_UpdatingClauseContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_MultiPartQueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_MultiPartQuery;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_MultiPartQuery(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_MultiPartQuery(this);
    }
  }

  public final OC_MultiPartQueryContext oC_MultiPartQuery() throws RecognitionException {
    OC_MultiPartQueryContext _localctx = new OC_MultiPartQueryContext(_ctx, getState());
    enterRule(_localctx, 14, RULE_oC_MultiPartQuery);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(308);
        _errHandler.sync(this);
        _alt = 1;
        do {
          switch (_alt) {
            case 1: {
              {
                setState(292);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la)
                    & ((1L << OPTIONAL) | (1L << MATCH) | (1L << UNWIND) | (1L << CALL))) != 0)) {
                  {
                    {
                      setState(286);
                      oC_ReadingClause();
                      setState(288);
                      _errHandler.sync(this);
                      _la = _input.LA(1);
                      if (_la == SP) {
                        {
                          setState(287);
                          match(SP);
                        }
                      }

                    }
                  }
                  setState(294);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                }
                setState(301);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MERGE) | (1L << CREATE)
                    | (1L << SET) | (1L << DETACH) | (1L << DELETE) | (1L << REMOVE))) != 0)) {
                  {
                    {
                      setState(295);
                      oC_UpdatingClause();
                      setState(297);
                      _errHandler.sync(this);
                      _la = _input.LA(1);
                      if (_la == SP) {
                        {
                          setState(296);
                          match(SP);
                        }
                      }

                    }
                  }
                  setState(303);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                }
                setState(304);
                oC_With();
                setState(306);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(305);
                    match(SP);
                  }
                }

              }
            }
              break;
            default:
              throw new NoViableAltException(this);
          }
          setState(310);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 25, _ctx);
        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
        setState(312);
        oC_SinglePartQuery();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_UpdatingClauseContext extends ParserRuleContext {
    public OC_CreateContext oC_Create() {
      return getRuleContext(OC_CreateContext.class, 0);
    }

    public OC_MergeContext oC_Merge() {
      return getRuleContext(OC_MergeContext.class, 0);
    }

    public OC_DeleteContext oC_Delete() {
      return getRuleContext(OC_DeleteContext.class, 0);
    }

    public OC_SetContext oC_Set() {
      return getRuleContext(OC_SetContext.class, 0);
    }

    public OC_RemoveContext oC_Remove() {
      return getRuleContext(OC_RemoveContext.class, 0);
    }

    public OC_UpdatingClauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_UpdatingClause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_UpdatingClause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_UpdatingClause(this);
    }
  }

  public final OC_UpdatingClauseContext oC_UpdatingClause() throws RecognitionException {
    OC_UpdatingClauseContext _localctx = new OC_UpdatingClauseContext(_ctx, getState());
    enterRule(_localctx, 16, RULE_oC_UpdatingClause);
    try {
      setState(319);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case CREATE:
          enterOuterAlt(_localctx, 1); {
          setState(314);
          oC_Create();
        }
          break;
        case MERGE:
          enterOuterAlt(_localctx, 2); {
          setState(315);
          oC_Merge();
        }
          break;
        case DETACH:
        case DELETE:
          enterOuterAlt(_localctx, 3); {
          setState(316);
          oC_Delete();
        }
          break;
        case SET:
          enterOuterAlt(_localctx, 4); {
          setState(317);
          oC_Set();
        }
          break;
        case REMOVE:
          enterOuterAlt(_localctx, 5); {
          setState(318);
          oC_Remove();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ReadingClauseContext extends ParserRuleContext {
    public OC_MatchContext oC_Match() {
      return getRuleContext(OC_MatchContext.class, 0);
    }

    public OC_UnwindContext oC_Unwind() {
      return getRuleContext(OC_UnwindContext.class, 0);
    }

    public OC_InQueryCallContext oC_InQueryCall() {
      return getRuleContext(OC_InQueryCallContext.class, 0);
    }

    public OC_ReadingClauseContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ReadingClause;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ReadingClause(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ReadingClause(this);
    }
  }

  public final OC_ReadingClauseContext oC_ReadingClause() throws RecognitionException {
    OC_ReadingClauseContext _localctx = new OC_ReadingClauseContext(_ctx, getState());
    enterRule(_localctx, 18, RULE_oC_ReadingClause);
    try {
      setState(324);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case OPTIONAL:
        case MATCH:
          enterOuterAlt(_localctx, 1); {
          setState(321);
          oC_Match();
        }
          break;
        case UNWIND:
          enterOuterAlt(_localctx, 2); {
          setState(322);
          oC_Unwind();
        }
          break;
        case CALL:
          enterOuterAlt(_localctx, 3); {
          setState(323);
          oC_InQueryCall();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MatchContext extends ParserRuleContext {
    public TerminalNode MATCH() {
      return getToken(CypherParser.MATCH, 0);
    }

    public OC_PatternContext oC_Pattern() {
      return getRuleContext(OC_PatternContext.class, 0);
    }

    public TerminalNode OPTIONAL() {
      return getToken(CypherParser.OPTIONAL, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public OC_MatchContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Match;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Match(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Match(this);
    }
  }

  public final OC_MatchContext oC_Match() throws RecognitionException {
    OC_MatchContext _localctx = new OC_MatchContext(_ctx, getState());
    enterRule(_localctx, 20, RULE_oC_Match);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(328);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == OPTIONAL) {
          {
            setState(326);
            match(OPTIONAL);
            setState(327);
            match(SP);
          }
        }

        setState(330);
        match(MATCH);
        setState(332);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(331);
            match(SP);
          }
        }

        setState(334);
        oC_Pattern();
        setState(339);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 31, _ctx)) {
          case 1: {
            setState(336);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(335);
                match(SP);
              }
            }

            setState(338);
            oC_Where();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_UnwindContext extends ParserRuleContext {
    public TerminalNode UNWIND() {
      return getToken(CypherParser.UNWIND, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode AS() {
      return getToken(CypherParser.AS, 0);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_UnwindContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Unwind;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Unwind(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Unwind(this);
    }
  }

  public final OC_UnwindContext oC_Unwind() throws RecognitionException {
    OC_UnwindContext _localctx = new OC_UnwindContext(_ctx, getState());
    enterRule(_localctx, 22, RULE_oC_Unwind);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(341);
        match(UNWIND);
        setState(343);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(342);
            match(SP);
          }
        }

        setState(345);
        oC_Expression();
        setState(346);
        match(SP);
        setState(347);
        match(AS);
        setState(348);
        match(SP);
        setState(349);
        oC_Variable();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MergeContext extends ParserRuleContext {
    public TerminalNode MERGE() {
      return getToken(CypherParser.MERGE, 0);
    }

    public OC_PatternPartContext oC_PatternPart() {
      return getRuleContext(OC_PatternPartContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_MergeActionContext> oC_MergeAction() {
      return getRuleContexts(OC_MergeActionContext.class);
    }

    public OC_MergeActionContext oC_MergeAction(int i) {
      return getRuleContext(OC_MergeActionContext.class, i);
    }

    public OC_MergeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Merge;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Merge(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Merge(this);
    }
  }

  public final OC_MergeContext oC_Merge() throws RecognitionException {
    OC_MergeContext _localctx = new OC_MergeContext(_ctx, getState());
    enterRule(_localctx, 24, RULE_oC_Merge);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(351);
        match(MERGE);
        setState(353);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(352);
            match(SP);
          }
        }

        setState(355);
        oC_PatternPart();
        setState(360);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 34, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(356);
                match(SP);
                setState(357);
                oC_MergeAction();
              }
            }
          }
          setState(362);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 34, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MergeActionContext extends ParserRuleContext {
    public TerminalNode ON() {
      return getToken(CypherParser.ON, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode MATCH() {
      return getToken(CypherParser.MATCH, 0);
    }

    public OC_SetContext oC_Set() {
      return getRuleContext(OC_SetContext.class, 0);
    }

    public TerminalNode CREATE() {
      return getToken(CypherParser.CREATE, 0);
    }

    public OC_MergeActionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_MergeAction;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_MergeAction(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_MergeAction(this);
    }
  }

  public final OC_MergeActionContext oC_MergeAction() throws RecognitionException {
    OC_MergeActionContext _localctx = new OC_MergeActionContext(_ctx, getState());
    enterRule(_localctx, 26, RULE_oC_MergeAction);
    try {
      setState(373);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 35, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(363);
            match(ON);
            setState(364);
            match(SP);
            setState(365);
            match(MATCH);
            setState(366);
            match(SP);
            setState(367);
            oC_Set();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(368);
            match(ON);
            setState(369);
            match(SP);
            setState(370);
            match(CREATE);
            setState(371);
            match(SP);
            setState(372);
            oC_Set();
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_CreateContext extends ParserRuleContext {
    public TerminalNode CREATE() {
      return getToken(CypherParser.CREATE, 0);
    }

    public OC_PatternContext oC_Pattern() {
      return getRuleContext(OC_PatternContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_CreateContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Create;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Create(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Create(this);
    }
  }

  public final OC_CreateContext oC_Create() throws RecognitionException {
    OC_CreateContext _localctx = new OC_CreateContext(_ctx, getState());
    enterRule(_localctx, 28, RULE_oC_Create);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(375);
        match(CREATE);
        setState(377);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(376);
            match(SP);
          }
        }

        setState(379);
        oC_Pattern();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SetContext extends ParserRuleContext {
    public TerminalNode SET() {
      return getToken(CypherParser.SET, 0);
    }

    public List<OC_SetItemContext> oC_SetItem() {
      return getRuleContexts(OC_SetItemContext.class);
    }

    public OC_SetItemContext oC_SetItem(int i) {
      return getRuleContext(OC_SetItemContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_SetContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Set;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Set(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Set(this);
    }
  }

  public final OC_SetContext oC_Set() throws RecognitionException {
    OC_SetContext _localctx = new OC_SetContext(_ctx, getState());
    enterRule(_localctx, 30, RULE_oC_Set);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(381);
        match(SET);
        setState(383);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(382);
            match(SP);
          }
        }

        setState(385);
        oC_SetItem();
        setState(396);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 40, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(387);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(386);
                    match(SP);
                  }
                }

                setState(389);
                match(T__1);
                setState(391);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(390);
                    match(SP);
                  }
                }

                setState(393);
                oC_SetItem();
              }
            }
          }
          setState(398);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 40, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SetItemContext extends ParserRuleContext {
    public OC_PropertyExpressionContext oC_PropertyExpression() {
      return getRuleContext(OC_PropertyExpressionContext.class, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_NodeLabelsContext oC_NodeLabels() {
      return getRuleContext(OC_NodeLabelsContext.class, 0);
    }

    public OC_SetItemContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SetItem;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SetItem(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SetItem(this);
    }
  }

  public final OC_SetItemContext oC_SetItem() throws RecognitionException {
    OC_SetItemContext _localctx = new OC_SetItemContext(_ctx, getState());
    enterRule(_localctx, 32, RULE_oC_SetItem);
    int _la;
    try {
      setState(435);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 48, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(399);
            oC_PropertyExpression();
            setState(401);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(400);
                match(SP);
              }
            }

            setState(403);
            match(T__2);
            setState(405);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(404);
                match(SP);
              }
            }

            setState(407);
            oC_Expression();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(409);
            oC_Variable();
            setState(411);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(410);
                match(SP);
              }
            }

            setState(413);
            match(T__2);
            setState(415);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(414);
                match(SP);
              }
            }

            setState(417);
            oC_Expression();
          }
        }
          break;
        case 3:
          enterOuterAlt(_localctx, 3); {
          {
            setState(419);
            oC_Variable();
            setState(421);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(420);
                match(SP);
              }
            }

            setState(423);
            match(T__3);
            setState(425);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(424);
                match(SP);
              }
            }

            setState(427);
            oC_Expression();
          }
        }
          break;
        case 4:
          enterOuterAlt(_localctx, 4); {
          {
            setState(429);
            oC_Variable();
            setState(431);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(430);
                match(SP);
              }
            }

            setState(433);
            oC_NodeLabels();
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_DeleteContext extends ParserRuleContext {
    public TerminalNode DELETE() {
      return getToken(CypherParser.DELETE, 0);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public TerminalNode DETACH() {
      return getToken(CypherParser.DETACH, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_DeleteContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Delete;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Delete(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Delete(this);
    }
  }

  public final OC_DeleteContext oC_Delete() throws RecognitionException {
    OC_DeleteContext _localctx = new OC_DeleteContext(_ctx, getState());
    enterRule(_localctx, 34, RULE_oC_Delete);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(439);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == DETACH) {
          {
            setState(437);
            match(DETACH);
            setState(438);
            match(SP);
          }
        }

        setState(441);
        match(DELETE);
        setState(443);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(442);
            match(SP);
          }
        }

        setState(445);
        oC_Expression();
        setState(456);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 53, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(447);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(446);
                    match(SP);
                  }
                }

                setState(449);
                match(T__1);
                setState(451);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(450);
                    match(SP);
                  }
                }

                setState(453);
                oC_Expression();
              }
            }
          }
          setState(458);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 53, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RemoveContext extends ParserRuleContext {
    public TerminalNode REMOVE() {
      return getToken(CypherParser.REMOVE, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_RemoveItemContext> oC_RemoveItem() {
      return getRuleContexts(OC_RemoveItemContext.class);
    }

    public OC_RemoveItemContext oC_RemoveItem(int i) {
      return getRuleContext(OC_RemoveItemContext.class, i);
    }

    public OC_RemoveContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Remove;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Remove(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Remove(this);
    }
  }

  public final OC_RemoveContext oC_Remove() throws RecognitionException {
    OC_RemoveContext _localctx = new OC_RemoveContext(_ctx, getState());
    enterRule(_localctx, 36, RULE_oC_Remove);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(459);
        match(REMOVE);
        setState(460);
        match(SP);
        setState(461);
        oC_RemoveItem();
        setState(472);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 56, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(463);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(462);
                    match(SP);
                  }
                }

                setState(465);
                match(T__1);
                setState(467);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(466);
                    match(SP);
                  }
                }

                setState(469);
                oC_RemoveItem();
              }
            }
          }
          setState(474);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 56, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RemoveItemContext extends ParserRuleContext {
    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_NodeLabelsContext oC_NodeLabels() {
      return getRuleContext(OC_NodeLabelsContext.class, 0);
    }

    public OC_PropertyExpressionContext oC_PropertyExpression() {
      return getRuleContext(OC_PropertyExpressionContext.class, 0);
    }

    public OC_RemoveItemContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RemoveItem;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RemoveItem(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RemoveItem(this);
    }
  }

  public final OC_RemoveItemContext oC_RemoveItem() throws RecognitionException {
    OC_RemoveItemContext _localctx = new OC_RemoveItemContext(_ctx, getState());
    enterRule(_localctx, 38, RULE_oC_RemoveItem);
    try {
      setState(479);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 57, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(475);
            oC_Variable();
            setState(476);
            oC_NodeLabels();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          setState(478);
          oC_PropertyExpression();
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_InQueryCallContext extends ParserRuleContext {
    public TerminalNode CALL() {
      return getToken(CypherParser.CALL, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ExplicitProcedureInvocationContext oC_ExplicitProcedureInvocation() {
      return getRuleContext(OC_ExplicitProcedureInvocationContext.class, 0);
    }

    public TerminalNode YIELD() {
      return getToken(CypherParser.YIELD, 0);
    }

    public OC_YieldItemsContext oC_YieldItems() {
      return getRuleContext(OC_YieldItemsContext.class, 0);
    }

    public OC_InQueryCallContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_InQueryCall;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_InQueryCall(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_InQueryCall(this);
    }
  }

  public final OC_InQueryCallContext oC_InQueryCall() throws RecognitionException {
    OC_InQueryCallContext _localctx = new OC_InQueryCallContext(_ctx, getState());
    enterRule(_localctx, 40, RULE_oC_InQueryCall);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(481);
        match(CALL);
        setState(482);
        match(SP);
        setState(483);
        oC_ExplicitProcedureInvocation();
        setState(490);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 59, _ctx)) {
          case 1: {
            setState(485);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(484);
                match(SP);
              }
            }

            setState(487);
            match(YIELD);
            setState(488);
            match(SP);
            setState(489);
            oC_YieldItems();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_StandaloneCallContext extends ParserRuleContext {
    public TerminalNode CALL() {
      return getToken(CypherParser.CALL, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ExplicitProcedureInvocationContext oC_ExplicitProcedureInvocation() {
      return getRuleContext(OC_ExplicitProcedureInvocationContext.class, 0);
    }

    public OC_ImplicitProcedureInvocationContext oC_ImplicitProcedureInvocation() {
      return getRuleContext(OC_ImplicitProcedureInvocationContext.class, 0);
    }

    public TerminalNode YIELD() {
      return getToken(CypherParser.YIELD, 0);
    }

    public OC_YieldItemsContext oC_YieldItems() {
      return getRuleContext(OC_YieldItemsContext.class, 0);
    }

    public OC_StandaloneCallContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_StandaloneCall;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_StandaloneCall(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_StandaloneCall(this);
    }
  }

  public final OC_StandaloneCallContext oC_StandaloneCall() throws RecognitionException {
    OC_StandaloneCallContext _localctx = new OC_StandaloneCallContext(_ctx, getState());
    enterRule(_localctx, 42, RULE_oC_StandaloneCall);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(492);
        match(CALL);
        setState(493);
        match(SP);
        setState(496);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
          case 1: {
            setState(494);
            oC_ExplicitProcedureInvocation();
          }
            break;
          case 2: {
            setState(495);
            oC_ImplicitProcedureInvocation();
          }
            break;
        }
        setState(507);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 63, _ctx)) {
          case 1: {
            setState(499);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(498);
                match(SP);
              }
            }

            setState(501);
            match(YIELD);
            setState(502);
            match(SP);
            setState(505);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
              case T__4: {
                setState(503);
                match(T__4);
              }
                break;
              case COUNT:
              case ANY:
              case NONE:
              case SINGLE:
              case HexLetter:
              case FILTER:
              case EXTRACT:
              case UnescapedSymbolicName:
              case EscapedSymbolicName: {
                setState(504);
                oC_YieldItems();
              }
                break;
              default:
                throw new NoViableAltException(this);
            }
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_YieldItemsContext extends ParserRuleContext {
    public List<OC_YieldItemContext> oC_YieldItem() {
      return getRuleContexts(OC_YieldItemContext.class);
    }

    public OC_YieldItemContext oC_YieldItem(int i) {
      return getRuleContext(OC_YieldItemContext.class, i);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_YieldItemsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_YieldItems;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_YieldItems(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_YieldItems(this);
    }
  }

  public final OC_YieldItemsContext oC_YieldItems() throws RecognitionException {
    OC_YieldItemsContext _localctx = new OC_YieldItemsContext(_ctx, getState());
    enterRule(_localctx, 44, RULE_oC_YieldItems);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(509);
        oC_YieldItem();
        setState(520);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(511);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(510);
                    match(SP);
                  }
                }

                setState(513);
                match(T__1);
                setState(515);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(514);
                    match(SP);
                  }
                }

                setState(517);
                oC_YieldItem();
              }
            }
          }
          setState(522);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 66, _ctx);
        }
        setState(527);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 68, _ctx)) {
          case 1: {
            setState(524);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(523);
                match(SP);
              }
            }

            setState(526);
            oC_Where();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_YieldItemContext extends ParserRuleContext {
    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_ProcedureResultFieldContext oC_ProcedureResultField() {
      return getRuleContext(OC_ProcedureResultFieldContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode AS() {
      return getToken(CypherParser.AS, 0);
    }

    public OC_YieldItemContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_YieldItem;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_YieldItem(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_YieldItem(this);
    }
  }

  public final OC_YieldItemContext oC_YieldItem() throws RecognitionException {
    OC_YieldItemContext _localctx = new OC_YieldItemContext(_ctx, getState());
    enterRule(_localctx, 46, RULE_oC_YieldItem);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(534);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 69, _ctx)) {
          case 1: {
            setState(529);
            oC_ProcedureResultField();
            setState(530);
            match(SP);
            setState(531);
            match(AS);
            setState(532);
            match(SP);
          }
            break;
        }
        setState(536);
        oC_Variable();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_WithContext extends ParserRuleContext {
    public TerminalNode WITH() {
      return getToken(CypherParser.WITH, 0);
    }

    public OC_ProjectionBodyContext oC_ProjectionBody() {
      return getRuleContext(OC_ProjectionBodyContext.class, 0);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_WithContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_With;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_With(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_With(this);
    }
  }

  public final OC_WithContext oC_With() throws RecognitionException {
    OC_WithContext _localctx = new OC_WithContext(_ctx, getState());
    enterRule(_localctx, 48, RULE_oC_With);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(538);
        match(WITH);
        setState(539);
        oC_ProjectionBody();
        setState(544);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 71, _ctx)) {
          case 1: {
            setState(541);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(540);
                match(SP);
              }
            }

            setState(543);
            oC_Where();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ReturnContext extends ParserRuleContext {
    public TerminalNode RETURN() {
      return getToken(CypherParser.RETURN, 0);
    }

    public OC_ProjectionBodyContext oC_ProjectionBody() {
      return getRuleContext(OC_ProjectionBodyContext.class, 0);
    }

    public OC_ReturnContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Return;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Return(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Return(this);
    }
  }

  public final OC_ReturnContext oC_Return() throws RecognitionException {
    OC_ReturnContext _localctx = new OC_ReturnContext(_ctx, getState());
    enterRule(_localctx, 50, RULE_oC_Return);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(546);
        match(RETURN);
        setState(547);
        oC_ProjectionBody();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ProjectionBodyContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ProjectionItemsContext oC_ProjectionItems() {
      return getRuleContext(OC_ProjectionItemsContext.class, 0);
    }

    public TerminalNode DISTINCT() {
      return getToken(CypherParser.DISTINCT, 0);
    }

    public OC_OrderContext oC_Order() {
      return getRuleContext(OC_OrderContext.class, 0);
    }

    public OC_SkipContext oC_Skip() {
      return getRuleContext(OC_SkipContext.class, 0);
    }

    public OC_LimitContext oC_Limit() {
      return getRuleContext(OC_LimitContext.class, 0);
    }

    public OC_ProjectionBodyContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ProjectionBody;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ProjectionBody(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ProjectionBody(this);
    }
  }

  public final OC_ProjectionBodyContext oC_ProjectionBody() throws RecognitionException {
    OC_ProjectionBodyContext _localctx = new OC_ProjectionBodyContext(_ctx, getState());
    enterRule(_localctx, 52, RULE_oC_ProjectionBody);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(553);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 73, _ctx)) {
          case 1: {
            setState(550);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(549);
                match(SP);
              }
            }

            setState(552);
            match(DISTINCT);
          }
            break;
        }
        setState(555);
        match(SP);
        setState(556);
        oC_ProjectionItems();
        setState(559);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 74, _ctx)) {
          case 1: {
            setState(557);
            match(SP);
            setState(558);
            oC_Order();
          }
            break;
        }
        setState(563);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 75, _ctx)) {
          case 1: {
            setState(561);
            match(SP);
            setState(562);
            oC_Skip();
          }
            break;
        }
        setState(567);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 76, _ctx)) {
          case 1: {
            setState(565);
            match(SP);
            setState(566);
            oC_Limit();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ProjectionItemsContext extends ParserRuleContext {
    public List<OC_ProjectionItemContext> oC_ProjectionItem() {
      return getRuleContexts(OC_ProjectionItemContext.class);
    }

    public OC_ProjectionItemContext oC_ProjectionItem(int i) {
      return getRuleContext(OC_ProjectionItemContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ProjectionItemsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ProjectionItems;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ProjectionItems(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ProjectionItems(this);
    }
  }

  public final OC_ProjectionItemsContext oC_ProjectionItems() throws RecognitionException {
    OC_ProjectionItemsContext _localctx = new OC_ProjectionItemsContext(_ctx, getState());
    enterRule(_localctx, 54, RULE_oC_ProjectionItems);
    int _la;
    try {
      int _alt;
      setState(597);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case T__4:
          enterOuterAlt(_localctx, 1); {
          {
            setState(569);
            match(T__4);
            setState(580);
            _errHandler.sync(this);
            _alt = getInterpreter().adaptivePredict(_input, 79, _ctx);
            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
              if (_alt == 1) {
                {
                  {
                    setState(571);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(570);
                        match(SP);
                      }
                    }

                    setState(573);
                    match(T__1);
                    setState(575);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(574);
                        match(SP);
                      }
                    }

                    setState(577);
                    oC_ProjectionItem();
                  }
                }
              }
              setState(582);
              _errHandler.sync(this);
              _alt = getInterpreter().adaptivePredict(_input, 79, _ctx);
            }
          }
        }
          break;
        case T__5:
        case T__7:
        case T__12:
        case T__13:
        case T__22:
        case T__25:
        case ALL:
        case NOT:
        case NULL:
        case COUNT:
        case ANY:
        case NONE:
        case SINGLE:
        case TRUE:
        case FALSE:
        case EXISTS:
        case CASE:
        case StringLiteral:
        case HexInteger:
        case DecimalInteger:
        case OctalInteger:
        case HexLetter:
        case ExponentDecimalReal:
        case RegularDecimalReal:
        case FILTER:
        case EXTRACT:
        case UnescapedSymbolicName:
        case EscapedSymbolicName:
          enterOuterAlt(_localctx, 2); {
          {
            setState(583);
            oC_ProjectionItem();
            setState(594);
            _errHandler.sync(this);
            _alt = getInterpreter().adaptivePredict(_input, 82, _ctx);
            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
              if (_alt == 1) {
                {
                  {
                    setState(585);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(584);
                        match(SP);
                      }
                    }

                    setState(587);
                    match(T__1);
                    setState(589);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(588);
                        match(SP);
                      }
                    }

                    setState(591);
                    oC_ProjectionItem();
                  }
                }
              }
              setState(596);
              _errHandler.sync(this);
              _alt = getInterpreter().adaptivePredict(_input, 82, _ctx);
            }
          }
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ProjectionItemContext extends ParserRuleContext {
    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode AS() {
      return getToken(CypherParser.AS, 0);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_ProjectionItemContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ProjectionItem;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ProjectionItem(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ProjectionItem(this);
    }
  }

  public final OC_ProjectionItemContext oC_ProjectionItem() throws RecognitionException {
    OC_ProjectionItemContext _localctx = new OC_ProjectionItemContext(_ctx, getState());
    enterRule(_localctx, 56, RULE_oC_ProjectionItem);
    try {
      setState(606);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 84, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(599);
            oC_Expression();
            setState(600);
            match(SP);
            setState(601);
            match(AS);
            setState(602);
            match(SP);
            setState(603);
            oC_Variable();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          setState(605);
          oC_Expression();
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_OrderContext extends ParserRuleContext {
    public TerminalNode ORDER() {
      return getToken(CypherParser.ORDER, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode BY() {
      return getToken(CypherParser.BY, 0);
    }

    public List<OC_SortItemContext> oC_SortItem() {
      return getRuleContexts(OC_SortItemContext.class);
    }

    public OC_SortItemContext oC_SortItem(int i) {
      return getRuleContext(OC_SortItemContext.class, i);
    }

    public OC_OrderContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Order;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Order(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Order(this);
    }
  }

  public final OC_OrderContext oC_Order() throws RecognitionException {
    OC_OrderContext _localctx = new OC_OrderContext(_ctx, getState());
    enterRule(_localctx, 58, RULE_oC_Order);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(608);
        match(ORDER);
        setState(609);
        match(SP);
        setState(610);
        match(BY);
        setState(611);
        match(SP);
        setState(612);
        oC_SortItem();
        setState(620);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__1) {
          {
            {
              setState(613);
              match(T__1);
              setState(615);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == SP) {
                {
                  setState(614);
                  match(SP);
                }
              }

              setState(617);
              oC_SortItem();
            }
          }
          setState(622);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SkipContext extends ParserRuleContext {
    public TerminalNode L_SKIP() {
      return getToken(CypherParser.L_SKIP, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public OC_SkipContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Skip;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Skip(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Skip(this);
    }
  }

  public final OC_SkipContext oC_Skip() throws RecognitionException {
    OC_SkipContext _localctx = new OC_SkipContext(_ctx, getState());
    enterRule(_localctx, 60, RULE_oC_Skip);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(623);
        match(L_SKIP);
        setState(624);
        match(SP);
        setState(625);
        oC_Expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_LimitContext extends ParserRuleContext {
    public TerminalNode LIMIT() {
      return getToken(CypherParser.LIMIT, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public OC_LimitContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Limit;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Limit(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Limit(this);
    }
  }

  public final OC_LimitContext oC_Limit() throws RecognitionException {
    OC_LimitContext _localctx = new OC_LimitContext(_ctx, getState());
    enterRule(_localctx, 62, RULE_oC_Limit);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(627);
        match(LIMIT);
        setState(628);
        match(SP);
        setState(629);
        oC_Expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SortItemContext extends ParserRuleContext {
    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public TerminalNode ASCENDING() {
      return getToken(CypherParser.ASCENDING, 0);
    }

    public TerminalNode ASC() {
      return getToken(CypherParser.ASC, 0);
    }

    public TerminalNode DESCENDING() {
      return getToken(CypherParser.DESCENDING, 0);
    }

    public TerminalNode DESC() {
      return getToken(CypherParser.DESC, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_SortItemContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SortItem;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SortItem(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SortItem(this);
    }
  }

  public final OC_SortItemContext oC_SortItem() throws RecognitionException {
    OC_SortItemContext _localctx = new OC_SortItemContext(_ctx, getState());
    enterRule(_localctx, 64, RULE_oC_SortItem);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(631);
        oC_Expression();
        setState(636);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 88, _ctx)) {
          case 1: {
            setState(633);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(632);
                match(SP);
              }
            }

            setState(635);
            _la = _input.LA(1);
            if (!(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (ASCENDING - 68))
                | (1L << (ASC - 68)) | (1L << (DESCENDING - 68)) | (1L << (DESC - 68)))) != 0))) {
              _errHandler.recoverInline(this);
            } else {
              if (_input.LA(1) == Token.EOF)
                matchedEOF = true;
              _errHandler.reportMatch(this);
              consume();
            }
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_WhereContext extends ParserRuleContext {
    public TerminalNode WHERE() {
      return getToken(CypherParser.WHERE, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public OC_WhereContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Where;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Where(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Where(this);
    }
  }

  public final OC_WhereContext oC_Where() throws RecognitionException {
    OC_WhereContext _localctx = new OC_WhereContext(_ctx, getState());
    enterRule(_localctx, 66, RULE_oC_Where);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(638);
        match(WHERE);
        setState(639);
        match(SP);
        setState(640);
        oC_Expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PatternContext extends ParserRuleContext {
    public List<OC_PatternPartContext> oC_PatternPart() {
      return getRuleContexts(OC_PatternPartContext.class);
    }

    public OC_PatternPartContext oC_PatternPart(int i) {
      return getRuleContext(OC_PatternPartContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PatternContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Pattern;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Pattern(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Pattern(this);
    }
  }

  public final OC_PatternContext oC_Pattern() throws RecognitionException {
    OC_PatternContext _localctx = new OC_PatternContext(_ctx, getState());
    enterRule(_localctx, 68, RULE_oC_Pattern);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(642);
        oC_PatternPart();
        setState(653);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 91, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(644);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(643);
                    match(SP);
                  }
                }

                setState(646);
                match(T__1);
                setState(648);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(647);
                    match(SP);
                  }
                }

                setState(650);
                oC_PatternPart();
              }
            }
          }
          setState(655);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 91, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PatternPartContext extends ParserRuleContext {
    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_AnonymousPatternPartContext oC_AnonymousPatternPart() {
      return getRuleContext(OC_AnonymousPatternPartContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PatternPartContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PatternPart;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PatternPart(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PatternPart(this);
    }
  }

  public final OC_PatternPartContext oC_PatternPart() throws RecognitionException {
    OC_PatternPartContext _localctx = new OC_PatternPartContext(_ctx, getState());
    enterRule(_localctx, 70, RULE_oC_PatternPart);
    int _la;
    try {
      setState(667);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case COUNT:
        case ANY:
        case NONE:
        case SINGLE:
        case HexLetter:
        case FILTER:
        case EXTRACT:
        case UnescapedSymbolicName:
        case EscapedSymbolicName:
          enterOuterAlt(_localctx, 1); {
          {
            setState(656);
            oC_Variable();
            setState(658);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(657);
                match(SP);
              }
            }

            setState(660);
            match(T__2);
            setState(662);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(661);
                match(SP);
              }
            }

            setState(664);
            oC_AnonymousPatternPart();
          }
        }
          break;
        case T__5:
          enterOuterAlt(_localctx, 2); {
          setState(666);
          oC_AnonymousPatternPart();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_AnonymousPatternPartContext extends ParserRuleContext {
    public OC_PatternElementContext oC_PatternElement() {
      return getRuleContext(OC_PatternElementContext.class, 0);
    }

    public OC_AnonymousPatternPartContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_AnonymousPatternPart;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_AnonymousPatternPart(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_AnonymousPatternPart(this);
    }
  }

  public final OC_AnonymousPatternPartContext oC_AnonymousPatternPart()
      throws RecognitionException {
    OC_AnonymousPatternPartContext _localctx = new OC_AnonymousPatternPartContext(_ctx, getState());
    enterRule(_localctx, 72, RULE_oC_AnonymousPatternPart);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(669);
        oC_PatternElement();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PatternElementContext extends ParserRuleContext {
    public OC_NodePatternContext oC_NodePattern() {
      return getRuleContext(OC_NodePatternContext.class, 0);
    }

    public List<OC_PatternElementChainContext> oC_PatternElementChain() {
      return getRuleContexts(OC_PatternElementChainContext.class);
    }

    public OC_PatternElementChainContext oC_PatternElementChain(int i) {
      return getRuleContext(OC_PatternElementChainContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PatternElementContext oC_PatternElement() {
      return getRuleContext(OC_PatternElementContext.class, 0);
    }

    public OC_PatternElementContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PatternElement;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PatternElement(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PatternElement(this);
    }
  }

  public final OC_PatternElementContext oC_PatternElement() throws RecognitionException {
    OC_PatternElementContext _localctx = new OC_PatternElementContext(_ctx, getState());
    enterRule(_localctx, 74, RULE_oC_PatternElement);
    int _la;
    try {
      int _alt;
      setState(685);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 97, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(671);
            oC_NodePattern();
            setState(678);
            _errHandler.sync(this);
            _alt = getInterpreter().adaptivePredict(_input, 96, _ctx);
            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
              if (_alt == 1) {
                {
                  {
                    setState(673);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(672);
                        match(SP);
                      }
                    }

                    setState(675);
                    oC_PatternElementChain();
                  }
                }
              }
              setState(680);
              _errHandler.sync(this);
              _alt = getInterpreter().adaptivePredict(_input, 96, _ctx);
            }
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(681);
            match(T__5);
            setState(682);
            oC_PatternElement();
            setState(683);
            match(T__6);
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NodePatternContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_NodeLabelsContext oC_NodeLabels() {
      return getRuleContext(OC_NodeLabelsContext.class, 0);
    }

    public OC_PropertiesContext oC_Properties() {
      return getRuleContext(OC_PropertiesContext.class, 0);
    }

    public OC_NodePatternContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NodePattern;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NodePattern(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NodePattern(this);
    }
  }

  public final OC_NodePatternContext oC_NodePattern() throws RecognitionException {
    OC_NodePatternContext _localctx = new OC_NodePatternContext(_ctx, getState());
    enterRule(_localctx, 76, RULE_oC_NodePattern);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(687);
        match(T__5);
        setState(689);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(688);
            match(SP);
          }
        }

        setState(695);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & ((1L << (COUNT - 83))
            | (1L << (ANY - 83)) | (1L << (NONE - 83)) | (1L << (SINGLE - 83))
            | (1L << (HexLetter - 83)) | (1L << (FILTER - 83)) | (1L << (EXTRACT - 83))
            | (1L << (UnescapedSymbolicName - 83)) | (1L << (EscapedSymbolicName - 83)))) != 0)) {
          {
            setState(691);
            oC_Variable();
            setState(693);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(692);
                match(SP);
              }
            }

          }
        }

        setState(701);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__9) {
          {
            setState(697);
            oC_NodeLabels();
            setState(699);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(698);
                match(SP);
              }
            }

          }
        }

        setState(707);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__22 || _la == T__25) {
          {
            setState(703);
            oC_Properties();
            setState(705);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(704);
                match(SP);
              }
            }

          }
        }

        setState(709);
        match(T__6);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PatternElementChainContext extends ParserRuleContext {
    public OC_RelationshipPatternContext oC_RelationshipPattern() {
      return getRuleContext(OC_RelationshipPatternContext.class, 0);
    }

    public OC_NodePatternContext oC_NodePattern() {
      return getRuleContext(OC_NodePatternContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_PatternElementChainContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PatternElementChain;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PatternElementChain(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PatternElementChain(this);
    }
  }

  public final OC_PatternElementChainContext oC_PatternElementChain() throws RecognitionException {
    OC_PatternElementChainContext _localctx = new OC_PatternElementChainContext(_ctx, getState());
    enterRule(_localctx, 78, RULE_oC_PatternElementChain);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(711);
        oC_RelationshipPattern();
        setState(713);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(712);
            match(SP);
          }
        }

        setState(715);
        oC_NodePattern();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RelationshipPatternContext extends ParserRuleContext {
    public OC_LeftArrowHeadContext oC_LeftArrowHead() {
      return getRuleContext(OC_LeftArrowHeadContext.class, 0);
    }

    public List<OC_DashContext> oC_Dash() {
      return getRuleContexts(OC_DashContext.class);
    }

    public OC_DashContext oC_Dash(int i) {
      return getRuleContext(OC_DashContext.class, i);
    }

    public OC_RightArrowHeadContext oC_RightArrowHead() {
      return getRuleContext(OC_RightArrowHeadContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_RelationshipDetailContext oC_RelationshipDetail() {
      return getRuleContext(OC_RelationshipDetailContext.class, 0);
    }

    public OC_RelationshipPatternContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RelationshipPattern;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RelationshipPattern(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RelationshipPattern(this);
    }
  }

  public final OC_RelationshipPatternContext oC_RelationshipPattern() throws RecognitionException {
    OC_RelationshipPatternContext _localctx = new OC_RelationshipPatternContext(_ctx, getState());
    enterRule(_localctx, 80, RULE_oC_RelationshipPattern);
    int _la;
    try {
      setState(781);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 122, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(717);
            oC_LeftArrowHead();
            setState(719);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(718);
                match(SP);
              }
            }

            setState(721);
            oC_Dash();
            setState(723);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 107, _ctx)) {
              case 1: {
                setState(722);
                match(SP);
              }
                break;
            }
            setState(726);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == T__7) {
              {
                setState(725);
                oC_RelationshipDetail();
              }
            }

            setState(729);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(728);
                match(SP);
              }
            }

            setState(731);
            oC_Dash();
            setState(733);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(732);
                match(SP);
              }
            }

            setState(735);
            oC_RightArrowHead();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(737);
            oC_LeftArrowHead();
            setState(739);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(738);
                match(SP);
              }
            }

            setState(741);
            oC_Dash();
            setState(743);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 112, _ctx)) {
              case 1: {
                setState(742);
                match(SP);
              }
                break;
            }
            setState(746);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == T__7) {
              {
                setState(745);
                oC_RelationshipDetail();
              }
            }

            setState(749);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(748);
                match(SP);
              }
            }

            setState(751);
            oC_Dash();
          }
        }
          break;
        case 3:
          enterOuterAlt(_localctx, 3); {
          {
            setState(753);
            oC_Dash();
            setState(755);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 115, _ctx)) {
              case 1: {
                setState(754);
                match(SP);
              }
                break;
            }
            setState(758);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == T__7) {
              {
                setState(757);
                oC_RelationshipDetail();
              }
            }

            setState(761);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(760);
                match(SP);
              }
            }

            setState(763);
            oC_Dash();
            setState(765);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(764);
                match(SP);
              }
            }

            setState(767);
            oC_RightArrowHead();
          }
        }
          break;
        case 4:
          enterOuterAlt(_localctx, 4); {
          {
            setState(769);
            oC_Dash();
            setState(771);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 119, _ctx)) {
              case 1: {
                setState(770);
                match(SP);
              }
                break;
            }
            setState(774);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == T__7) {
              {
                setState(773);
                oC_RelationshipDetail();
              }
            }

            setState(777);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(776);
                match(SP);
              }
            }

            setState(779);
            oC_Dash();
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RelationshipDetailContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_RelationshipTypesContext oC_RelationshipTypes() {
      return getRuleContext(OC_RelationshipTypesContext.class, 0);
    }

    public OC_RangeLiteralContext oC_RangeLiteral() {
      return getRuleContext(OC_RangeLiteralContext.class, 0);
    }

    public OC_PropertiesContext oC_Properties() {
      return getRuleContext(OC_PropertiesContext.class, 0);
    }

    public OC_RelationshipDetailContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RelationshipDetail;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RelationshipDetail(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RelationshipDetail(this);
    }
  }

  public final OC_RelationshipDetailContext oC_RelationshipDetail() throws RecognitionException {
    OC_RelationshipDetailContext _localctx = new OC_RelationshipDetailContext(_ctx, getState());
    enterRule(_localctx, 82, RULE_oC_RelationshipDetail);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(783);
        match(T__7);
        setState(785);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(784);
            match(SP);
          }
        }

        setState(791);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & ((1L << (COUNT - 83))
            | (1L << (ANY - 83)) | (1L << (NONE - 83)) | (1L << (SINGLE - 83))
            | (1L << (HexLetter - 83)) | (1L << (FILTER - 83)) | (1L << (EXTRACT - 83))
            | (1L << (UnescapedSymbolicName - 83)) | (1L << (EscapedSymbolicName - 83)))) != 0)) {
          {
            setState(787);
            oC_Variable();
            setState(789);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(788);
                match(SP);
              }
            }

          }
        }

        setState(797);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__9) {
          {
            setState(793);
            oC_RelationshipTypes();
            setState(795);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(794);
                match(SP);
              }
            }

          }
        }

        setState(800);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__4) {
          {
            setState(799);
            oC_RangeLiteral();
          }
        }

        setState(806);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__22 || _la == T__25) {
          {
            setState(802);
            oC_Properties();
            setState(804);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(803);
                match(SP);
              }
            }

          }
        }

        setState(808);
        match(T__8);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PropertiesContext extends ParserRuleContext {
    public OC_MapLiteralContext oC_MapLiteral() {
      return getRuleContext(OC_MapLiteralContext.class, 0);
    }

    public OC_ParameterContext oC_Parameter() {
      return getRuleContext(OC_ParameterContext.class, 0);
    }

    public OC_PropertiesContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Properties;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Properties(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Properties(this);
    }
  }

  public final OC_PropertiesContext oC_Properties() throws RecognitionException {
    OC_PropertiesContext _localctx = new OC_PropertiesContext(_ctx, getState());
    enterRule(_localctx, 84, RULE_oC_Properties);
    try {
      setState(812);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case T__22:
          enterOuterAlt(_localctx, 1); {
          setState(810);
          oC_MapLiteral();
        }
          break;
        case T__25:
          enterOuterAlt(_localctx, 2); {
          setState(811);
          oC_Parameter();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RelationshipTypesContext extends ParserRuleContext {
    public List<OC_RelTypeNameContext> oC_RelTypeName() {
      return getRuleContexts(OC_RelTypeNameContext.class);
    }

    public OC_RelTypeNameContext oC_RelTypeName(int i) {
      return getRuleContext(OC_RelTypeNameContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_RelationshipTypesContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RelationshipTypes;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RelationshipTypes(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RelationshipTypes(this);
    }
  }

  public final OC_RelationshipTypesContext oC_RelationshipTypes() throws RecognitionException {
    OC_RelationshipTypesContext _localctx = new OC_RelationshipTypesContext(_ctx, getState());
    enterRule(_localctx, 86, RULE_oC_RelationshipTypes);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(814);
        match(T__9);
        setState(816);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(815);
            match(SP);
          }
        }

        setState(818);
        oC_RelTypeName();
        setState(832);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 136, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(820);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(819);
                    match(SP);
                  }
                }

                setState(822);
                match(T__10);
                setState(824);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__9) {
                  {
                    setState(823);
                    match(T__9);
                  }
                }

                setState(827);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(826);
                    match(SP);
                  }
                }

                setState(829);
                oC_RelTypeName();
              }
            }
          }
          setState(834);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 136, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NodeLabelsContext extends ParserRuleContext {
    public List<OC_NodeLabelContext> oC_NodeLabel() {
      return getRuleContexts(OC_NodeLabelContext.class);
    }

    public OC_NodeLabelContext oC_NodeLabel(int i) {
      return getRuleContext(OC_NodeLabelContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_NodeLabelsContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NodeLabels;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NodeLabels(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NodeLabels(this);
    }
  }

  public final OC_NodeLabelsContext oC_NodeLabels() throws RecognitionException {
    OC_NodeLabelsContext _localctx = new OC_NodeLabelsContext(_ctx, getState());
    enterRule(_localctx, 88, RULE_oC_NodeLabels);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(835);
        oC_NodeLabel();
        setState(842);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 138, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(837);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(836);
                    match(SP);
                  }
                }

                setState(839);
                oC_NodeLabel();
              }
            }
          }
          setState(844);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 138, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NodeLabelContext extends ParserRuleContext {
    public OC_LabelNameContext oC_LabelName() {
      return getRuleContext(OC_LabelNameContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_NodeLabelContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NodeLabel;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NodeLabel(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NodeLabel(this);
    }
  }

  public final OC_NodeLabelContext oC_NodeLabel() throws RecognitionException {
    OC_NodeLabelContext _localctx = new OC_NodeLabelContext(_ctx, getState());
    enterRule(_localctx, 90, RULE_oC_NodeLabel);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(845);
        match(T__9);
        setState(847);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(846);
            match(SP);
          }
        }

        setState(849);
        oC_LabelName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RangeLiteralContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_IntegerLiteralContext> oC_IntegerLiteral() {
      return getRuleContexts(OC_IntegerLiteralContext.class);
    }

    public OC_IntegerLiteralContext oC_IntegerLiteral(int i) {
      return getRuleContext(OC_IntegerLiteralContext.class, i);
    }

    public OC_RangeLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RangeLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RangeLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RangeLiteral(this);
    }
  }

  public final OC_RangeLiteralContext oC_RangeLiteral() throws RecognitionException {
    OC_RangeLiteralContext _localctx = new OC_RangeLiteralContext(_ctx, getState());
    enterRule(_localctx, 92, RULE_oC_RangeLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(851);
        match(T__4);
        setState(853);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(852);
            match(SP);
          }
        }

        setState(859);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (HexInteger - 97))
            | (1L << (DecimalInteger - 97)) | (1L << (OctalInteger - 97)))) != 0)) {
          {
            setState(855);
            oC_IntegerLiteral();
            setState(857);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(856);
                match(SP);
              }
            }

          }
        }

        setState(871);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == T__11) {
          {
            setState(861);
            match(T__11);
            setState(863);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(862);
                match(SP);
              }
            }

            setState(869);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (HexInteger - 97))
                | (1L << (DecimalInteger - 97)) | (1L << (OctalInteger - 97)))) != 0)) {
              {
                setState(865);
                oC_IntegerLiteral();
                setState(867);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(866);
                    match(SP);
                  }
                }

              }
            }

          }
        }

      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_LabelNameContext extends ParserRuleContext {
    public OC_SchemaNameContext oC_SchemaName() {
      return getRuleContext(OC_SchemaNameContext.class, 0);
    }

    public OC_LabelNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_LabelName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_LabelName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_LabelName(this);
    }
  }

  public final OC_LabelNameContext oC_LabelName() throws RecognitionException {
    OC_LabelNameContext _localctx = new OC_LabelNameContext(_ctx, getState());
    enterRule(_localctx, 94, RULE_oC_LabelName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(873);
        oC_SchemaName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RelTypeNameContext extends ParserRuleContext {
    public OC_SchemaNameContext oC_SchemaName() {
      return getRuleContext(OC_SchemaNameContext.class, 0);
    }

    public OC_RelTypeNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RelTypeName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RelTypeName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RelTypeName(this);
    }
  }

  public final OC_RelTypeNameContext oC_RelTypeName() throws RecognitionException {
    OC_RelTypeNameContext _localctx = new OC_RelTypeNameContext(_ctx, getState());
    enterRule(_localctx, 96, RULE_oC_RelTypeName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(875);
        oC_SchemaName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ExpressionContext extends ParserRuleContext {
    public OC_OrExpressionContext oC_OrExpression() {
      return getRuleContext(OC_OrExpressionContext.class, 0);
    }

    public OC_ExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Expression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Expression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Expression(this);
    }
  }

  public final OC_ExpressionContext oC_Expression() throws RecognitionException {
    OC_ExpressionContext _localctx = new OC_ExpressionContext(_ctx, getState());
    enterRule(_localctx, 98, RULE_oC_Expression);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(877);
        oC_OrExpression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_OrExpressionContext extends ParserRuleContext {
    public List<OC_XorExpressionContext> oC_XorExpression() {
      return getRuleContexts(OC_XorExpressionContext.class);
    }

    public OC_XorExpressionContext oC_XorExpression(int i) {
      return getRuleContext(OC_XorExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<TerminalNode> OR() {
      return getTokens(CypherParser.OR);
    }

    public TerminalNode OR(int i) {
      return getToken(CypherParser.OR, i);
    }

    public OC_OrExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_OrExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_OrExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_OrExpression(this);
    }
  }

  public final OC_OrExpressionContext oC_OrExpression() throws RecognitionException {
    OC_OrExpressionContext _localctx = new OC_OrExpressionContext(_ctx, getState());
    enterRule(_localctx, 100, RULE_oC_OrExpression);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(879);
        oC_XorExpression();
        setState(886);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 147, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(880);
                match(SP);
                setState(881);
                match(OR);
                setState(882);
                match(SP);
                setState(883);
                oC_XorExpression();
              }
            }
          }
          setState(888);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 147, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_XorExpressionContext extends ParserRuleContext {
    public List<OC_AndExpressionContext> oC_AndExpression() {
      return getRuleContexts(OC_AndExpressionContext.class);
    }

    public OC_AndExpressionContext oC_AndExpression(int i) {
      return getRuleContext(OC_AndExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<TerminalNode> XOR() {
      return getTokens(CypherParser.XOR);
    }

    public TerminalNode XOR(int i) {
      return getToken(CypherParser.XOR, i);
    }

    public OC_XorExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_XorExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_XorExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_XorExpression(this);
    }
  }

  public final OC_XorExpressionContext oC_XorExpression() throws RecognitionException {
    OC_XorExpressionContext _localctx = new OC_XorExpressionContext(_ctx, getState());
    enterRule(_localctx, 102, RULE_oC_XorExpression);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(889);
        oC_AndExpression();
        setState(896);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 148, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(890);
                match(SP);
                setState(891);
                match(XOR);
                setState(892);
                match(SP);
                setState(893);
                oC_AndExpression();
              }
            }
          }
          setState(898);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 148, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_AndExpressionContext extends ParserRuleContext {
    public List<OC_NotExpressionContext> oC_NotExpression() {
      return getRuleContexts(OC_NotExpressionContext.class);
    }

    public OC_NotExpressionContext oC_NotExpression(int i) {
      return getRuleContext(OC_NotExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<TerminalNode> AND() {
      return getTokens(CypherParser.AND);
    }

    public TerminalNode AND(int i) {
      return getToken(CypherParser.AND, i);
    }

    public OC_AndExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_AndExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_AndExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_AndExpression(this);
    }
  }

  public final OC_AndExpressionContext oC_AndExpression() throws RecognitionException {
    OC_AndExpressionContext _localctx = new OC_AndExpressionContext(_ctx, getState());
    enterRule(_localctx, 104, RULE_oC_AndExpression);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(899);
        oC_NotExpression();
        setState(906);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 149, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(900);
                match(SP);
                setState(901);
                match(AND);
                setState(902);
                match(SP);
                setState(903);
                oC_NotExpression();
              }
            }
          }
          setState(908);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 149, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NotExpressionContext extends ParserRuleContext {
    public OC_ComparisonExpressionContext oC_ComparisonExpression() {
      return getRuleContext(OC_ComparisonExpressionContext.class, 0);
    }

    public List<TerminalNode> NOT() {
      return getTokens(CypherParser.NOT);
    }

    public TerminalNode NOT(int i) {
      return getToken(CypherParser.NOT, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_NotExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NotExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NotExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NotExpression(this);
    }
  }

  public final OC_NotExpressionContext oC_NotExpression() throws RecognitionException {
    OC_NotExpressionContext _localctx = new OC_NotExpressionContext(_ctx, getState());
    enterRule(_localctx, 106, RULE_oC_NotExpression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(915);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == NOT) {
          {
            {
              setState(909);
              match(NOT);
              setState(911);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == SP) {
                {
                  setState(910);
                  match(SP);
                }
              }

            }
          }
          setState(917);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
        setState(918);
        oC_ComparisonExpression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ComparisonExpressionContext extends ParserRuleContext {
    public OC_AddOrSubtractExpressionContext oC_AddOrSubtractExpression() {
      return getRuleContext(OC_AddOrSubtractExpressionContext.class, 0);
    }

    public List<OC_PartialComparisonExpressionContext> oC_PartialComparisonExpression() {
      return getRuleContexts(OC_PartialComparisonExpressionContext.class);
    }

    public OC_PartialComparisonExpressionContext oC_PartialComparisonExpression(int i) {
      return getRuleContext(OC_PartialComparisonExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ComparisonExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ComparisonExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ComparisonExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ComparisonExpression(this);
    }
  }

  public final OC_ComparisonExpressionContext oC_ComparisonExpression()
      throws RecognitionException {
    OC_ComparisonExpressionContext _localctx = new OC_ComparisonExpressionContext(_ctx, getState());
    enterRule(_localctx, 108, RULE_oC_ComparisonExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(920);
        oC_AddOrSubtractExpression();
        setState(927);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 153, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(922);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(921);
                    match(SP);
                  }
                }

                setState(924);
                oC_PartialComparisonExpression();
              }
            }
          }
          setState(929);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 153, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_AddOrSubtractExpressionContext extends ParserRuleContext {
    public List<OC_MultiplyDivideModuloExpressionContext> oC_MultiplyDivideModuloExpression() {
      return getRuleContexts(OC_MultiplyDivideModuloExpressionContext.class);
    }

    public OC_MultiplyDivideModuloExpressionContext oC_MultiplyDivideModuloExpression(int i) {
      return getRuleContext(OC_MultiplyDivideModuloExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_AddOrSubtractExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_AddOrSubtractExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_AddOrSubtractExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_AddOrSubtractExpression(this);
    }
  }

  public final OC_AddOrSubtractExpressionContext oC_AddOrSubtractExpression()
      throws RecognitionException {
    OC_AddOrSubtractExpressionContext _localctx =
        new OC_AddOrSubtractExpressionContext(_ctx, getState());
    enterRule(_localctx, 110, RULE_oC_AddOrSubtractExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(930);
        oC_MultiplyDivideModuloExpression();
        setState(949);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 159, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              setState(947);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 158, _ctx)) {
                case 1: {
                  {
                    setState(932);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(931);
                        match(SP);
                      }
                    }

                    setState(934);
                    match(T__12);
                    setState(936);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(935);
                        match(SP);
                      }
                    }

                    setState(938);
                    oC_MultiplyDivideModuloExpression();
                  }
                }
                  break;
                case 2: {
                  {
                    setState(940);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(939);
                        match(SP);
                      }
                    }

                    setState(942);
                    match(T__13);
                    setState(944);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(943);
                        match(SP);
                      }
                    }

                    setState(946);
                    oC_MultiplyDivideModuloExpression();
                  }
                }
                  break;
              }
            }
          }
          setState(951);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 159, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MultiplyDivideModuloExpressionContext extends ParserRuleContext {
    public List<OC_PowerOfExpressionContext> oC_PowerOfExpression() {
      return getRuleContexts(OC_PowerOfExpressionContext.class);
    }

    public OC_PowerOfExpressionContext oC_PowerOfExpression(int i) {
      return getRuleContext(OC_PowerOfExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_MultiplyDivideModuloExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_MultiplyDivideModuloExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_MultiplyDivideModuloExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_MultiplyDivideModuloExpression(this);
    }
  }

  public final OC_MultiplyDivideModuloExpressionContext oC_MultiplyDivideModuloExpression()
      throws RecognitionException {
    OC_MultiplyDivideModuloExpressionContext _localctx =
        new OC_MultiplyDivideModuloExpressionContext(_ctx, getState());
    enterRule(_localctx, 112, RULE_oC_MultiplyDivideModuloExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(952);
        oC_PowerOfExpression();
        setState(979);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 167, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              setState(977);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 166, _ctx)) {
                case 1: {
                  {
                    setState(954);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(953);
                        match(SP);
                      }
                    }

                    setState(956);
                    match(T__4);
                    setState(958);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(957);
                        match(SP);
                      }
                    }

                    setState(960);
                    oC_PowerOfExpression();
                  }
                }
                  break;
                case 2: {
                  {
                    setState(962);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(961);
                        match(SP);
                      }
                    }

                    setState(964);
                    match(T__14);
                    setState(966);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(965);
                        match(SP);
                      }
                    }

                    setState(968);
                    oC_PowerOfExpression();
                  }
                }
                  break;
                case 3: {
                  {
                    setState(970);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(969);
                        match(SP);
                      }
                    }

                    setState(972);
                    match(T__15);
                    setState(974);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SP) {
                      {
                        setState(973);
                        match(SP);
                      }
                    }

                    setState(976);
                    oC_PowerOfExpression();
                  }
                }
                  break;
              }
            }
          }
          setState(981);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 167, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PowerOfExpressionContext extends ParserRuleContext {
    public List<OC_UnaryAddOrSubtractExpressionContext> oC_UnaryAddOrSubtractExpression() {
      return getRuleContexts(OC_UnaryAddOrSubtractExpressionContext.class);
    }

    public OC_UnaryAddOrSubtractExpressionContext oC_UnaryAddOrSubtractExpression(int i) {
      return getRuleContext(OC_UnaryAddOrSubtractExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PowerOfExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PowerOfExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PowerOfExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PowerOfExpression(this);
    }
  }

  public final OC_PowerOfExpressionContext oC_PowerOfExpression() throws RecognitionException {
    OC_PowerOfExpressionContext _localctx = new OC_PowerOfExpressionContext(_ctx, getState());
    enterRule(_localctx, 114, RULE_oC_PowerOfExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(982);
        oC_UnaryAddOrSubtractExpression();
        setState(993);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 170, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(984);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(983);
                    match(SP);
                  }
                }

                setState(986);
                match(T__16);
                setState(988);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(987);
                    match(SP);
                  }
                }

                setState(990);
                oC_UnaryAddOrSubtractExpression();
              }
            }
          }
          setState(995);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 170, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_UnaryAddOrSubtractExpressionContext extends ParserRuleContext {
    public OC_StringListNullOperatorExpressionContext oC_StringListNullOperatorExpression() {
      return getRuleContext(OC_StringListNullOperatorExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_UnaryAddOrSubtractExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_UnaryAddOrSubtractExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_UnaryAddOrSubtractExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_UnaryAddOrSubtractExpression(this);
    }
  }

  public final OC_UnaryAddOrSubtractExpressionContext oC_UnaryAddOrSubtractExpression()
      throws RecognitionException {
    OC_UnaryAddOrSubtractExpressionContext _localctx =
        new OC_UnaryAddOrSubtractExpressionContext(_ctx, getState());
    enterRule(_localctx, 116, RULE_oC_UnaryAddOrSubtractExpression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1002);
        _errHandler.sync(this);
        _la = _input.LA(1);
        while (_la == T__12 || _la == T__13) {
          {
            {
              setState(996);
              _la = _input.LA(1);
              if (!(_la == T__12 || _la == T__13)) {
                _errHandler.recoverInline(this);
              } else {
                if (_input.LA(1) == Token.EOF)
                  matchedEOF = true;
                _errHandler.reportMatch(this);
                consume();
              }
              setState(998);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == SP) {
                {
                  setState(997);
                  match(SP);
                }
              }

            }
          }
          setState(1004);
          _errHandler.sync(this);
          _la = _input.LA(1);
        }
        setState(1005);
        oC_StringListNullOperatorExpression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_StringListNullOperatorExpressionContext extends ParserRuleContext {
    public OC_PropertyOrLabelsExpressionContext oC_PropertyOrLabelsExpression() {
      return getRuleContext(OC_PropertyOrLabelsExpressionContext.class, 0);
    }

    public List<OC_StringOperatorExpressionContext> oC_StringOperatorExpression() {
      return getRuleContexts(OC_StringOperatorExpressionContext.class);
    }

    public OC_StringOperatorExpressionContext oC_StringOperatorExpression(int i) {
      return getRuleContext(OC_StringOperatorExpressionContext.class, i);
    }

    public List<OC_ListOperatorExpressionContext> oC_ListOperatorExpression() {
      return getRuleContexts(OC_ListOperatorExpressionContext.class);
    }

    public OC_ListOperatorExpressionContext oC_ListOperatorExpression(int i) {
      return getRuleContext(OC_ListOperatorExpressionContext.class, i);
    }

    public List<OC_NullOperatorExpressionContext> oC_NullOperatorExpression() {
      return getRuleContexts(OC_NullOperatorExpressionContext.class);
    }

    public OC_NullOperatorExpressionContext oC_NullOperatorExpression(int i) {
      return getRuleContext(OC_NullOperatorExpressionContext.class, i);
    }

    public OC_StringListNullOperatorExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_StringListNullOperatorExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_StringListNullOperatorExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_StringListNullOperatorExpression(this);
    }
  }

  public final OC_StringListNullOperatorExpressionContext oC_StringListNullOperatorExpression()
      throws RecognitionException {
    OC_StringListNullOperatorExpressionContext _localctx =
        new OC_StringListNullOperatorExpressionContext(_ctx, getState());
    enterRule(_localctx, 118, RULE_oC_StringListNullOperatorExpression);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1007);
        oC_PropertyOrLabelsExpression();
        setState(1013);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 174, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              setState(1011);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 173, _ctx)) {
                case 1: {
                  setState(1008);
                  oC_StringOperatorExpression();
                }
                  break;
                case 2: {
                  setState(1009);
                  oC_ListOperatorExpression();
                }
                  break;
                case 3: {
                  setState(1010);
                  oC_NullOperatorExpression();
                }
                  break;
              }
            }
          }
          setState(1015);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 174, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ListOperatorExpressionContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode IN() {
      return getToken(CypherParser.IN, 0);
    }

    public OC_PropertyOrLabelsExpressionContext oC_PropertyOrLabelsExpression() {
      return getRuleContext(OC_PropertyOrLabelsExpressionContext.class, 0);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public OC_ListOperatorExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ListOperatorExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ListOperatorExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ListOperatorExpression(this);
    }
  }

  public final OC_ListOperatorExpressionContext oC_ListOperatorExpression()
      throws RecognitionException {
    OC_ListOperatorExpressionContext _localctx =
        new OC_ListOperatorExpressionContext(_ctx, getState());
    enterRule(_localctx, 120, RULE_oC_ListOperatorExpression);
    int _la;
    try {
      setState(1041);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 180, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(1016);
            match(SP);
            setState(1017);
            match(IN);
            setState(1019);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1018);
                match(SP);
              }
            }

            setState(1021);
            oC_PropertyOrLabelsExpression();
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(1023);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1022);
                match(SP);
              }
            }

            setState(1025);
            match(T__7);
            setState(1026);
            oC_Expression();
            setState(1027);
            match(T__8);
          }
        }
          break;
        case 3:
          enterOuterAlt(_localctx, 3); {
          {
            setState(1030);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1029);
                match(SP);
              }
            }

            setState(1032);
            match(T__7);
            setState(1034);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__7) | (1L << T__12)
                | (1L << T__13) | (1L << T__22) | (1L << T__25) | (1L << ALL))) != 0)
                || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & ((1L << (NOT - 76))
                    | (1L << (NULL - 76)) | (1L << (COUNT - 76)) | (1L << (ANY - 76))
                    | (1L << (NONE - 76)) | (1L << (SINGLE - 76)) | (1L << (TRUE - 76))
                    | (1L << (FALSE - 76)) | (1L << (EXISTS - 76)) | (1L << (CASE - 76))
                    | (1L << (StringLiteral - 76)) | (1L << (HexInteger - 76))
                    | (1L << (DecimalInteger - 76)) | (1L << (OctalInteger - 76))
                    | (1L << (HexLetter - 76)) | (1L << (ExponentDecimalReal - 76))
                    | (1L << (RegularDecimalReal - 76)) | (1L << (FILTER - 76))
                    | (1L << (EXTRACT - 76)) | (1L << (UnescapedSymbolicName - 76))
                    | (1L << (EscapedSymbolicName - 76)))) != 0)) {
              {
                setState(1033);
                oC_Expression();
              }
            }

            setState(1036);
            match(T__11);
            setState(1038);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__7) | (1L << T__12)
                | (1L << T__13) | (1L << T__22) | (1L << T__25) | (1L << ALL))) != 0)
                || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & ((1L << (NOT - 76))
                    | (1L << (NULL - 76)) | (1L << (COUNT - 76)) | (1L << (ANY - 76))
                    | (1L << (NONE - 76)) | (1L << (SINGLE - 76)) | (1L << (TRUE - 76))
                    | (1L << (FALSE - 76)) | (1L << (EXISTS - 76)) | (1L << (CASE - 76))
                    | (1L << (StringLiteral - 76)) | (1L << (HexInteger - 76))
                    | (1L << (DecimalInteger - 76)) | (1L << (OctalInteger - 76))
                    | (1L << (HexLetter - 76)) | (1L << (ExponentDecimalReal - 76))
                    | (1L << (RegularDecimalReal - 76)) | (1L << (FILTER - 76))
                    | (1L << (EXTRACT - 76)) | (1L << (UnescapedSymbolicName - 76))
                    | (1L << (EscapedSymbolicName - 76)))) != 0)) {
              {
                setState(1037);
                oC_Expression();
              }
            }

            setState(1040);
            match(T__8);
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_StringOperatorExpressionContext extends ParserRuleContext {
    public OC_PropertyOrLabelsExpressionContext oC_PropertyOrLabelsExpression() {
      return getRuleContext(OC_PropertyOrLabelsExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode STARTS() {
      return getToken(CypherParser.STARTS, 0);
    }

    public TerminalNode WITH() {
      return getToken(CypherParser.WITH, 0);
    }

    public TerminalNode ENDS() {
      return getToken(CypherParser.ENDS, 0);
    }

    public TerminalNode CONTAINS() {
      return getToken(CypherParser.CONTAINS, 0);
    }

    public OC_StringOperatorExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_StringOperatorExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_StringOperatorExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_StringOperatorExpression(this);
    }
  }

  public final OC_StringOperatorExpressionContext oC_StringOperatorExpression()
      throws RecognitionException {
    OC_StringOperatorExpressionContext _localctx =
        new OC_StringOperatorExpressionContext(_ctx, getState());
    enterRule(_localctx, 122, RULE_oC_StringOperatorExpression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1053);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 181, _ctx)) {
          case 1: {
            {
              setState(1043);
              match(SP);
              setState(1044);
              match(STARTS);
              setState(1045);
              match(SP);
              setState(1046);
              match(WITH);
            }
          }
            break;
          case 2: {
            {
              setState(1047);
              match(SP);
              setState(1048);
              match(ENDS);
              setState(1049);
              match(SP);
              setState(1050);
              match(WITH);
            }
          }
            break;
          case 3: {
            {
              setState(1051);
              match(SP);
              setState(1052);
              match(CONTAINS);
            }
          }
            break;
        }
        setState(1056);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1055);
            match(SP);
          }
        }

        setState(1058);
        oC_PropertyOrLabelsExpression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NullOperatorExpressionContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode IS() {
      return getToken(CypherParser.IS, 0);
    }

    public TerminalNode NULL() {
      return getToken(CypherParser.NULL, 0);
    }

    public TerminalNode NOT() {
      return getToken(CypherParser.NOT, 0);
    }

    public OC_NullOperatorExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NullOperatorExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NullOperatorExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NullOperatorExpression(this);
    }
  }

  public final OC_NullOperatorExpressionContext oC_NullOperatorExpression()
      throws RecognitionException {
    OC_NullOperatorExpressionContext _localctx =
        new OC_NullOperatorExpressionContext(_ctx, getState());
    enterRule(_localctx, 124, RULE_oC_NullOperatorExpression);
    try {
      setState(1070);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 183, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          {
            setState(1060);
            match(SP);
            setState(1061);
            match(IS);
            setState(1062);
            match(SP);
            setState(1063);
            match(NULL);
          }
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          {
            setState(1064);
            match(SP);
            setState(1065);
            match(IS);
            setState(1066);
            match(SP);
            setState(1067);
            match(NOT);
            setState(1068);
            match(SP);
            setState(1069);
            match(NULL);
          }
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PropertyOrLabelsExpressionContext extends ParserRuleContext {
    public OC_AtomContext oC_Atom() {
      return getRuleContext(OC_AtomContext.class, 0);
    }

    public List<OC_PropertyLookupContext> oC_PropertyLookup() {
      return getRuleContexts(OC_PropertyLookupContext.class);
    }

    public OC_PropertyLookupContext oC_PropertyLookup(int i) {
      return getRuleContext(OC_PropertyLookupContext.class, i);
    }

    public OC_NodeLabelsContext oC_NodeLabels() {
      return getRuleContext(OC_NodeLabelsContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PropertyOrLabelsExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PropertyOrLabelsExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PropertyOrLabelsExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PropertyOrLabelsExpression(this);
    }
  }

  public final OC_PropertyOrLabelsExpressionContext oC_PropertyOrLabelsExpression()
      throws RecognitionException {
    OC_PropertyOrLabelsExpressionContext _localctx =
        new OC_PropertyOrLabelsExpressionContext(_ctx, getState());
    enterRule(_localctx, 126, RULE_oC_PropertyOrLabelsExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1072);
        oC_Atom();
        setState(1079);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 185, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(1074);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(1073);
                    match(SP);
                  }
                }

                setState(1076);
                oC_PropertyLookup();
              }
            }
          }
          setState(1081);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 185, _ctx);
        }
        setState(1086);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 187, _ctx)) {
          case 1: {
            setState(1083);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1082);
                match(SP);
              }
            }

            setState(1085);
            oC_NodeLabels();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_AtomContext extends ParserRuleContext {
    public OC_LiteralContext oC_Literal() {
      return getRuleContext(OC_LiteralContext.class, 0);
    }

    public OC_ParameterContext oC_Parameter() {
      return getRuleContext(OC_ParameterContext.class, 0);
    }

    public OC_CaseExpressionContext oC_CaseExpression() {
      return getRuleContext(OC_CaseExpressionContext.class, 0);
    }

    public TerminalNode COUNT() {
      return getToken(CypherParser.COUNT, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ListComprehensionContext oC_ListComprehension() {
      return getRuleContext(OC_ListComprehensionContext.class, 0);
    }

    public OC_PatternComprehensionContext oC_PatternComprehension() {
      return getRuleContext(OC_PatternComprehensionContext.class, 0);
    }

    public TerminalNode ALL() {
      return getToken(CypherParser.ALL, 0);
    }

    public OC_FilterExpressionContext oC_FilterExpression() {
      return getRuleContext(OC_FilterExpressionContext.class, 0);
    }

    public TerminalNode ANY() {
      return getToken(CypherParser.ANY, 0);
    }

    public TerminalNode NONE() {
      return getToken(CypherParser.NONE, 0);
    }

    public TerminalNode SINGLE() {
      return getToken(CypherParser.SINGLE, 0);
    }

    public OC_RelationshipsPatternContext oC_RelationshipsPattern() {
      return getRuleContext(OC_RelationshipsPatternContext.class, 0);
    }

    public OC_ParenthesizedExpressionContext oC_ParenthesizedExpression() {
      return getRuleContext(OC_ParenthesizedExpressionContext.class, 0);
    }

    public OC_FunctionInvocationContext oC_FunctionInvocation() {
      return getRuleContext(OC_FunctionInvocationContext.class, 0);
    }

    public OC_ExistentialSubqueryContext oC_ExistentialSubquery() {
      return getRuleContext(OC_ExistentialSubqueryContext.class, 0);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_AtomContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Atom;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Atom(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Atom(this);
    }
  }

  public final OC_AtomContext oC_Atom() throws RecognitionException {
    OC_AtomContext _localctx = new OC_AtomContext(_ctx, getState());
    enterRule(_localctx, 128, RULE_oC_Atom);
    int _la;
    try {
      setState(1167);
      _errHandler.sync(this);
      switch (getInterpreter().adaptivePredict(_input, 203, _ctx)) {
        case 1:
          enterOuterAlt(_localctx, 1); {
          setState(1088);
          oC_Literal();
        }
          break;
        case 2:
          enterOuterAlt(_localctx, 2); {
          setState(1089);
          oC_Parameter();
        }
          break;
        case 3:
          enterOuterAlt(_localctx, 3); {
          setState(1090);
          oC_CaseExpression();
        }
          break;
        case 4:
          enterOuterAlt(_localctx, 4); {
          {
            setState(1091);
            match(COUNT);
            setState(1093);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1092);
                match(SP);
              }
            }

            setState(1095);
            match(T__5);
            setState(1097);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1096);
                match(SP);
              }
            }

            setState(1099);
            match(T__4);
            setState(1101);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1100);
                match(SP);
              }
            }

            setState(1103);
            match(T__6);
          }
        }
          break;
        case 5:
          enterOuterAlt(_localctx, 5); {
          setState(1104);
          oC_ListComprehension();
        }
          break;
        case 6:
          enterOuterAlt(_localctx, 6); {
          setState(1105);
          oC_PatternComprehension();
        }
          break;
        case 7:
          enterOuterAlt(_localctx, 7); {
          {
            setState(1106);
            match(ALL);
            setState(1108);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1107);
                match(SP);
              }
            }

            setState(1110);
            match(T__5);
            setState(1112);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1111);
                match(SP);
              }
            }

            setState(1114);
            oC_FilterExpression();
            setState(1116);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1115);
                match(SP);
              }
            }

            setState(1118);
            match(T__6);
          }
        }
          break;
        case 8:
          enterOuterAlt(_localctx, 8); {
          {
            setState(1120);
            match(ANY);
            setState(1122);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1121);
                match(SP);
              }
            }

            setState(1124);
            match(T__5);
            setState(1126);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1125);
                match(SP);
              }
            }

            setState(1128);
            oC_FilterExpression();
            setState(1130);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1129);
                match(SP);
              }
            }

            setState(1132);
            match(T__6);
          }
        }
          break;
        case 9:
          enterOuterAlt(_localctx, 9); {
          {
            setState(1134);
            match(NONE);
            setState(1136);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1135);
                match(SP);
              }
            }

            setState(1138);
            match(T__5);
            setState(1140);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1139);
                match(SP);
              }
            }

            setState(1142);
            oC_FilterExpression();
            setState(1144);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1143);
                match(SP);
              }
            }

            setState(1146);
            match(T__6);
          }
        }
          break;
        case 10:
          enterOuterAlt(_localctx, 10); {
          {
            setState(1148);
            match(SINGLE);
            setState(1150);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1149);
                match(SP);
              }
            }

            setState(1152);
            match(T__5);
            setState(1154);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1153);
                match(SP);
              }
            }

            setState(1156);
            oC_FilterExpression();
            setState(1158);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1157);
                match(SP);
              }
            }

            setState(1160);
            match(T__6);
          }
        }
          break;
        case 11:
          enterOuterAlt(_localctx, 11); {
          setState(1162);
          oC_RelationshipsPattern();
        }
          break;
        case 12:
          enterOuterAlt(_localctx, 12); {
          setState(1163);
          oC_ParenthesizedExpression();
        }
          break;
        case 13:
          enterOuterAlt(_localctx, 13); {
          setState(1164);
          oC_FunctionInvocation();
        }
          break;
        case 14:
          enterOuterAlt(_localctx, 14); {
          setState(1165);
          oC_ExistentialSubquery();
        }
          break;
        case 15:
          enterOuterAlt(_localctx, 15); {
          setState(1166);
          oC_Variable();
        }
          break;
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_LiteralContext extends ParserRuleContext {
    public OC_NumberLiteralContext oC_NumberLiteral() {
      return getRuleContext(OC_NumberLiteralContext.class, 0);
    }

    public TerminalNode StringLiteral() {
      return getToken(CypherParser.StringLiteral, 0);
    }

    public OC_BooleanLiteralContext oC_BooleanLiteral() {
      return getRuleContext(OC_BooleanLiteralContext.class, 0);
    }

    public TerminalNode NULL() {
      return getToken(CypherParser.NULL, 0);
    }

    public OC_MapLiteralContext oC_MapLiteral() {
      return getRuleContext(OC_MapLiteralContext.class, 0);
    }

    public OC_ListLiteralContext oC_ListLiteral() {
      return getRuleContext(OC_ListLiteralContext.class, 0);
    }

    public OC_LiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Literal;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Literal(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Literal(this);
    }
  }

  public final OC_LiteralContext oC_Literal() throws RecognitionException {
    OC_LiteralContext _localctx = new OC_LiteralContext(_ctx, getState());
    enterRule(_localctx, 130, RULE_oC_Literal);
    try {
      setState(1175);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case HexInteger:
        case DecimalInteger:
        case OctalInteger:
        case ExponentDecimalReal:
        case RegularDecimalReal:
          enterOuterAlt(_localctx, 1); {
          setState(1169);
          oC_NumberLiteral();
        }
          break;
        case StringLiteral:
          enterOuterAlt(_localctx, 2); {
          setState(1170);
          match(StringLiteral);
        }
          break;
        case TRUE:
        case FALSE:
          enterOuterAlt(_localctx, 3); {
          setState(1171);
          oC_BooleanLiteral();
        }
          break;
        case NULL:
          enterOuterAlt(_localctx, 4); {
          setState(1172);
          match(NULL);
        }
          break;
        case T__22:
          enterOuterAlt(_localctx, 5); {
          setState(1173);
          oC_MapLiteral();
        }
          break;
        case T__7:
          enterOuterAlt(_localctx, 6); {
          setState(1174);
          oC_ListLiteral();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_BooleanLiteralContext extends ParserRuleContext {
    public TerminalNode TRUE() {
      return getToken(CypherParser.TRUE, 0);
    }

    public TerminalNode FALSE() {
      return getToken(CypherParser.FALSE, 0);
    }

    public OC_BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_BooleanLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_BooleanLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_BooleanLiteral(this);
    }
  }

  public final OC_BooleanLiteralContext oC_BooleanLiteral() throws RecognitionException {
    OC_BooleanLiteralContext _localctx = new OC_BooleanLiteralContext(_ctx, getState());
    enterRule(_localctx, 132, RULE_oC_BooleanLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1177);
        _la = _input.LA(1);
        if (!(_la == TRUE || _la == FALSE)) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ListLiteralContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public OC_ListLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ListLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ListLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ListLiteral(this);
    }
  }

  public final OC_ListLiteralContext oC_ListLiteral() throws RecognitionException {
    OC_ListLiteralContext _localctx = new OC_ListLiteralContext(_ctx, getState());
    enterRule(_localctx, 134, RULE_oC_ListLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1179);
        match(T__7);
        setState(1181);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1180);
            match(SP);
          }
        }

        setState(1200);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__7) | (1L << T__12)
            | (1L << T__13) | (1L << T__22) | (1L << T__25) | (1L << ALL))) != 0)
            || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76))
                & ((1L << (NOT - 76)) | (1L << (NULL - 76)) | (1L << (COUNT - 76))
                    | (1L << (ANY - 76)) | (1L << (NONE - 76)) | (1L << (SINGLE - 76))
                    | (1L << (TRUE - 76)) | (1L << (FALSE - 76)) | (1L << (EXISTS - 76))
                    | (1L << (CASE - 76)) | (1L << (StringLiteral - 76)) | (1L << (HexInteger - 76))
                    | (1L << (DecimalInteger - 76)) | (1L << (OctalInteger - 76))
                    | (1L << (HexLetter - 76)) | (1L << (ExponentDecimalReal - 76))
                    | (1L << (RegularDecimalReal - 76)) | (1L << (FILTER - 76))
                    | (1L << (EXTRACT - 76)) | (1L << (UnescapedSymbolicName - 76))
                    | (1L << (EscapedSymbolicName - 76)))) != 0)) {
          {
            setState(1183);
            oC_Expression();
            setState(1185);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1184);
                match(SP);
              }
            }

            setState(1197);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la == T__1) {
              {
                {
                  setState(1187);
                  match(T__1);
                  setState(1189);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1188);
                      match(SP);
                    }
                  }

                  setState(1191);
                  oC_Expression();
                  setState(1193);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1192);
                      match(SP);
                    }
                  }

                }
              }
              setState(1199);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
          }
        }

        setState(1202);
        match(T__8);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PartialComparisonExpressionContext extends ParserRuleContext {
    public OC_AddOrSubtractExpressionContext oC_AddOrSubtractExpression() {
      return getRuleContext(OC_AddOrSubtractExpressionContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_PartialComparisonExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PartialComparisonExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PartialComparisonExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PartialComparisonExpression(this);
    }
  }

  public final OC_PartialComparisonExpressionContext oC_PartialComparisonExpression()
      throws RecognitionException {
    OC_PartialComparisonExpressionContext _localctx =
        new OC_PartialComparisonExpressionContext(_ctx, getState());
    enterRule(_localctx, 136, RULE_oC_PartialComparisonExpression);
    int _la;
    try {
      setState(1234);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case T__2:
          enterOuterAlt(_localctx, 1); {
          {
            setState(1204);
            match(T__2);
            setState(1206);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1205);
                match(SP);
              }
            }

            setState(1208);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        case T__17:
          enterOuterAlt(_localctx, 2); {
          {
            setState(1209);
            match(T__17);
            setState(1211);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1210);
                match(SP);
              }
            }

            setState(1213);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        case T__18:
          enterOuterAlt(_localctx, 3); {
          {
            setState(1214);
            match(T__18);
            setState(1216);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1215);
                match(SP);
              }
            }

            setState(1218);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        case T__19:
          enterOuterAlt(_localctx, 4); {
          {
            setState(1219);
            match(T__19);
            setState(1221);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1220);
                match(SP);
              }
            }

            setState(1223);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        case T__20:
          enterOuterAlt(_localctx, 5); {
          {
            setState(1224);
            match(T__20);
            setState(1226);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1225);
                match(SP);
              }
            }

            setState(1228);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        case T__21:
          enterOuterAlt(_localctx, 6); {
          {
            setState(1229);
            match(T__21);
            setState(1231);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1230);
                match(SP);
              }
            }

            setState(1233);
            oC_AddOrSubtractExpression();
          }
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ParenthesizedExpressionContext extends ParserRuleContext {
    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ParenthesizedExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ParenthesizedExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ParenthesizedExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ParenthesizedExpression(this);
    }
  }

  public final OC_ParenthesizedExpressionContext oC_ParenthesizedExpression()
      throws RecognitionException {
    OC_ParenthesizedExpressionContext _localctx =
        new OC_ParenthesizedExpressionContext(_ctx, getState());
    enterRule(_localctx, 138, RULE_oC_ParenthesizedExpression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1236);
        match(T__5);
        setState(1238);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1237);
            match(SP);
          }
        }

        setState(1240);
        oC_Expression();
        setState(1242);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1241);
            match(SP);
          }
        }

        setState(1244);
        match(T__6);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RelationshipsPatternContext extends ParserRuleContext {
    public OC_NodePatternContext oC_NodePattern() {
      return getRuleContext(OC_NodePatternContext.class, 0);
    }

    public List<OC_PatternElementChainContext> oC_PatternElementChain() {
      return getRuleContexts(OC_PatternElementChainContext.class);
    }

    public OC_PatternElementChainContext oC_PatternElementChain(int i) {
      return getRuleContext(OC_PatternElementChainContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_RelationshipsPatternContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RelationshipsPattern;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RelationshipsPattern(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RelationshipsPattern(this);
    }
  }

  public final OC_RelationshipsPatternContext oC_RelationshipsPattern()
      throws RecognitionException {
    OC_RelationshipsPatternContext _localctx = new OC_RelationshipsPatternContext(_ctx, getState());
    enterRule(_localctx, 140, RULE_oC_RelationshipsPattern);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1246);
        oC_NodePattern();
        setState(1251);
        _errHandler.sync(this);
        _alt = 1;
        do {
          switch (_alt) {
            case 1: {
              {
                setState(1248);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(1247);
                    match(SP);
                  }
                }

                setState(1250);
                oC_PatternElementChain();
              }
            }
              break;
            default:
              throw new NoViableAltException(this);
          }
          setState(1253);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 221, _ctx);
        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_FilterExpressionContext extends ParserRuleContext {
    public OC_IdInCollContext oC_IdInColl() {
      return getRuleContext(OC_IdInCollContext.class, 0);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_FilterExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_FilterExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_FilterExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_FilterExpression(this);
    }
  }

  public final OC_FilterExpressionContext oC_FilterExpression() throws RecognitionException {
    OC_FilterExpressionContext _localctx = new OC_FilterExpressionContext(_ctx, getState());
    enterRule(_localctx, 142, RULE_oC_FilterExpression);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1255);
        oC_IdInColl();
        setState(1260);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 223, _ctx)) {
          case 1: {
            setState(1257);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1256);
                match(SP);
              }
            }

            setState(1259);
            oC_Where();
          }
            break;
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_IdInCollContext extends ParserRuleContext {
    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode IN() {
      return getToken(CypherParser.IN, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public OC_IdInCollContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_IdInColl;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_IdInColl(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_IdInColl(this);
    }
  }

  public final OC_IdInCollContext oC_IdInColl() throws RecognitionException {
    OC_IdInCollContext _localctx = new OC_IdInCollContext(_ctx, getState());
    enterRule(_localctx, 144, RULE_oC_IdInColl);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1262);
        oC_Variable();
        setState(1263);
        match(SP);
        setState(1264);
        match(IN);
        setState(1265);
        match(SP);
        setState(1266);
        oC_Expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_FunctionInvocationContext extends ParserRuleContext {
    public OC_FunctionNameContext oC_FunctionName() {
      return getRuleContext(OC_FunctionNameContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode DISTINCT() {
      return getToken(CypherParser.DISTINCT, 0);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public OC_FunctionInvocationContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_FunctionInvocation;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_FunctionInvocation(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_FunctionInvocation(this);
    }
  }

  public final OC_FunctionInvocationContext oC_FunctionInvocation() throws RecognitionException {
    OC_FunctionInvocationContext _localctx = new OC_FunctionInvocationContext(_ctx, getState());
    enterRule(_localctx, 146, RULE_oC_FunctionInvocation);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1268);
        oC_FunctionName();
        setState(1270);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1269);
            match(SP);
          }
        }

        setState(1272);
        match(T__5);
        setState(1274);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1273);
            match(SP);
          }
        }

        setState(1280);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == DISTINCT) {
          {
            setState(1276);
            match(DISTINCT);
            setState(1278);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1277);
                match(SP);
              }
            }

          }
        }

        setState(1299);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__7) | (1L << T__12)
            | (1L << T__13) | (1L << T__22) | (1L << T__25) | (1L << ALL))) != 0)
            || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76))
                & ((1L << (NOT - 76)) | (1L << (NULL - 76)) | (1L << (COUNT - 76))
                    | (1L << (ANY - 76)) | (1L << (NONE - 76)) | (1L << (SINGLE - 76))
                    | (1L << (TRUE - 76)) | (1L << (FALSE - 76)) | (1L << (EXISTS - 76))
                    | (1L << (CASE - 76)) | (1L << (StringLiteral - 76)) | (1L << (HexInteger - 76))
                    | (1L << (DecimalInteger - 76)) | (1L << (OctalInteger - 76))
                    | (1L << (HexLetter - 76)) | (1L << (ExponentDecimalReal - 76))
                    | (1L << (RegularDecimalReal - 76)) | (1L << (FILTER - 76))
                    | (1L << (EXTRACT - 76)) | (1L << (UnescapedSymbolicName - 76))
                    | (1L << (EscapedSymbolicName - 76)))) != 0)) {
          {
            setState(1282);
            oC_Expression();
            setState(1284);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1283);
                match(SP);
              }
            }

            setState(1296);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la == T__1) {
              {
                {
                  setState(1286);
                  match(T__1);
                  setState(1288);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1287);
                      match(SP);
                    }
                  }

                  setState(1290);
                  oC_Expression();
                  setState(1292);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1291);
                      match(SP);
                    }
                  }

                }
              }
              setState(1298);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
          }
        }

        setState(1301);
        match(T__6);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_FunctionNameContext extends ParserRuleContext {
    public OC_NamespaceContext oC_Namespace() {
      return getRuleContext(OC_NamespaceContext.class, 0);
    }

    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public OC_FunctionNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_FunctionName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_FunctionName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_FunctionName(this);
    }
  }

  public final OC_FunctionNameContext oC_FunctionName() throws RecognitionException {
    OC_FunctionNameContext _localctx = new OC_FunctionNameContext(_ctx, getState());
    enterRule(_localctx, 148, RULE_oC_FunctionName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1303);
        oC_Namespace();
        setState(1304);
        oC_SymbolicName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ExistentialSubqueryContext extends ParserRuleContext {
    public TerminalNode EXISTS() {
      return getToken(CypherParser.EXISTS, 0);
    }

    public OC_RegularQueryContext oC_RegularQuery() {
      return getRuleContext(OC_RegularQueryContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PatternContext oC_Pattern() {
      return getRuleContext(OC_PatternContext.class, 0);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public OC_ExistentialSubqueryContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ExistentialSubquery;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ExistentialSubquery(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ExistentialSubquery(this);
    }
  }

  public final OC_ExistentialSubqueryContext oC_ExistentialSubquery() throws RecognitionException {
    OC_ExistentialSubqueryContext _localctx = new OC_ExistentialSubqueryContext(_ctx, getState());
    enterRule(_localctx, 150, RULE_oC_ExistentialSubquery);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1306);
        match(EXISTS);
        setState(1308);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1307);
            match(SP);
          }
        }

        setState(1310);
        match(T__22);
        setState(1312);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1311);
            match(SP);
          }
        }

        setState(1322);
        _errHandler.sync(this);
        switch (_input.LA(1)) {
          case OPTIONAL:
          case MATCH:
          case UNWIND:
          case MERGE:
          case CREATE:
          case SET:
          case DETACH:
          case DELETE:
          case REMOVE:
          case CALL:
          case WITH:
          case RETURN: {
            setState(1314);
            oC_RegularQuery();
          }
            break;
          case T__5:
          case COUNT:
          case ANY:
          case NONE:
          case SINGLE:
          case HexLetter:
          case FILTER:
          case EXTRACT:
          case UnescapedSymbolicName:
          case EscapedSymbolicName: {
            {
              setState(1315);
              oC_Pattern();
              setState(1320);
              _errHandler.sync(this);
              switch (getInterpreter().adaptivePredict(_input, 236, _ctx)) {
                case 1: {
                  setState(1317);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1316);
                      match(SP);
                    }
                  }

                  setState(1319);
                  oC_Where();
                }
                  break;
              }
            }
          }
            break;
          default:
            throw new NoViableAltException(this);
        }
        setState(1325);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1324);
            match(SP);
          }
        }

        setState(1327);
        match(T__23);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ExplicitProcedureInvocationContext extends ParserRuleContext {
    public OC_ProcedureNameContext oC_ProcedureName() {
      return getRuleContext(OC_ProcedureNameContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public OC_ExplicitProcedureInvocationContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ExplicitProcedureInvocation;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ExplicitProcedureInvocation(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ExplicitProcedureInvocation(this);
    }
  }

  public final OC_ExplicitProcedureInvocationContext oC_ExplicitProcedureInvocation()
      throws RecognitionException {
    OC_ExplicitProcedureInvocationContext _localctx =
        new OC_ExplicitProcedureInvocationContext(_ctx, getState());
    enterRule(_localctx, 152, RULE_oC_ExplicitProcedureInvocation);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1329);
        oC_ProcedureName();
        setState(1331);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1330);
            match(SP);
          }
        }

        setState(1333);
        match(T__5);
        setState(1335);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1334);
            match(SP);
          }
        }

        setState(1354);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__7) | (1L << T__12)
            | (1L << T__13) | (1L << T__22) | (1L << T__25) | (1L << ALL))) != 0)
            || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76))
                & ((1L << (NOT - 76)) | (1L << (NULL - 76)) | (1L << (COUNT - 76))
                    | (1L << (ANY - 76)) | (1L << (NONE - 76)) | (1L << (SINGLE - 76))
                    | (1L << (TRUE - 76)) | (1L << (FALSE - 76)) | (1L << (EXISTS - 76))
                    | (1L << (CASE - 76)) | (1L << (StringLiteral - 76)) | (1L << (HexInteger - 76))
                    | (1L << (DecimalInteger - 76)) | (1L << (OctalInteger - 76))
                    | (1L << (HexLetter - 76)) | (1L << (ExponentDecimalReal - 76))
                    | (1L << (RegularDecimalReal - 76)) | (1L << (FILTER - 76))
                    | (1L << (EXTRACT - 76)) | (1L << (UnescapedSymbolicName - 76))
                    | (1L << (EscapedSymbolicName - 76)))) != 0)) {
          {
            setState(1337);
            oC_Expression();
            setState(1339);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1338);
                match(SP);
              }
            }

            setState(1351);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la == T__1) {
              {
                {
                  setState(1341);
                  match(T__1);
                  setState(1343);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1342);
                      match(SP);
                    }
                  }

                  setState(1345);
                  oC_Expression();
                  setState(1347);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1346);
                      match(SP);
                    }
                  }

                }
              }
              setState(1353);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
          }
        }

        setState(1356);
        match(T__6);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ImplicitProcedureInvocationContext extends ParserRuleContext {
    public OC_ProcedureNameContext oC_ProcedureName() {
      return getRuleContext(OC_ProcedureNameContext.class, 0);
    }

    public OC_ImplicitProcedureInvocationContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ImplicitProcedureInvocation;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ImplicitProcedureInvocation(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ImplicitProcedureInvocation(this);
    }
  }

  public final OC_ImplicitProcedureInvocationContext oC_ImplicitProcedureInvocation()
      throws RecognitionException {
    OC_ImplicitProcedureInvocationContext _localctx =
        new OC_ImplicitProcedureInvocationContext(_ctx, getState());
    enterRule(_localctx, 154, RULE_oC_ImplicitProcedureInvocation);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1358);
        oC_ProcedureName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ProcedureResultFieldContext extends ParserRuleContext {
    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public OC_ProcedureResultFieldContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ProcedureResultField;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ProcedureResultField(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ProcedureResultField(this);
    }
  }

  public final OC_ProcedureResultFieldContext oC_ProcedureResultField()
      throws RecognitionException {
    OC_ProcedureResultFieldContext _localctx = new OC_ProcedureResultFieldContext(_ctx, getState());
    enterRule(_localctx, 156, RULE_oC_ProcedureResultField);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1360);
        oC_SymbolicName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ProcedureNameContext extends ParserRuleContext {
    public OC_NamespaceContext oC_Namespace() {
      return getRuleContext(OC_NamespaceContext.class, 0);
    }

    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public OC_ProcedureNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ProcedureName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ProcedureName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ProcedureName(this);
    }
  }

  public final OC_ProcedureNameContext oC_ProcedureName() throws RecognitionException {
    OC_ProcedureNameContext _localctx = new OC_ProcedureNameContext(_ctx, getState());
    enterRule(_localctx, 158, RULE_oC_ProcedureName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1362);
        oC_Namespace();
        setState(1363);
        oC_SymbolicName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NamespaceContext extends ParserRuleContext {
    public List<OC_SymbolicNameContext> oC_SymbolicName() {
      return getRuleContexts(OC_SymbolicNameContext.class);
    }

    public OC_SymbolicNameContext oC_SymbolicName(int i) {
      return getRuleContext(OC_SymbolicNameContext.class, i);
    }

    public OC_NamespaceContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Namespace;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Namespace(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Namespace(this);
    }
  }

  public final OC_NamespaceContext oC_Namespace() throws RecognitionException {
    OC_NamespaceContext _localctx = new OC_NamespaceContext(_ctx, getState());
    enterRule(_localctx, 160, RULE_oC_Namespace);
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1370);
        _errHandler.sync(this);
        _alt = getInterpreter().adaptivePredict(_input, 246, _ctx);
        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
          if (_alt == 1) {
            {
              {
                setState(1365);
                oC_SymbolicName();
                setState(1366);
                match(T__24);
              }
            }
          }
          setState(1372);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 246, _ctx);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ListComprehensionContext extends ParserRuleContext {
    public OC_FilterExpressionContext oC_FilterExpression() {
      return getRuleContext(OC_FilterExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public OC_ListComprehensionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ListComprehension;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ListComprehension(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ListComprehension(this);
    }
  }

  public final OC_ListComprehensionContext oC_ListComprehension() throws RecognitionException {
    OC_ListComprehensionContext _localctx = new OC_ListComprehensionContext(_ctx, getState());
    enterRule(_localctx, 162, RULE_oC_ListComprehension);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1373);
        match(T__7);
        setState(1375);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1374);
            match(SP);
          }
        }

        setState(1377);
        oC_FilterExpression();
        setState(1386);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 250, _ctx)) {
          case 1: {
            setState(1379);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1378);
                match(SP);
              }
            }

            setState(1381);
            match(T__10);
            setState(1383);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1382);
                match(SP);
              }
            }

            setState(1385);
            oC_Expression();
          }
            break;
        }
        setState(1389);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1388);
            match(SP);
          }
        }

        setState(1391);
        match(T__8);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PatternComprehensionContext extends ParserRuleContext {
    public OC_RelationshipsPatternContext oC_RelationshipsPattern() {
      return getRuleContext(OC_RelationshipsPatternContext.class, 0);
    }

    public OC_ExpressionContext oC_Expression() {
      return getRuleContext(OC_ExpressionContext.class, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_VariableContext oC_Variable() {
      return getRuleContext(OC_VariableContext.class, 0);
    }

    public OC_WhereContext oC_Where() {
      return getRuleContext(OC_WhereContext.class, 0);
    }

    public OC_PatternComprehensionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PatternComprehension;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PatternComprehension(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PatternComprehension(this);
    }
  }

  public final OC_PatternComprehensionContext oC_PatternComprehension()
      throws RecognitionException {
    OC_PatternComprehensionContext _localctx = new OC_PatternComprehensionContext(_ctx, getState());
    enterRule(_localctx, 164, RULE_oC_PatternComprehension);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1393);
        match(T__7);
        setState(1395);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1394);
            match(SP);
          }
        }

        setState(1405);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & ((1L << (COUNT - 83))
            | (1L << (ANY - 83)) | (1L << (NONE - 83)) | (1L << (SINGLE - 83))
            | (1L << (HexLetter - 83)) | (1L << (FILTER - 83)) | (1L << (EXTRACT - 83))
            | (1L << (UnescapedSymbolicName - 83)) | (1L << (EscapedSymbolicName - 83)))) != 0)) {
          {
            setState(1397);
            oC_Variable();
            setState(1399);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1398);
                match(SP);
              }
            }

            setState(1401);
            match(T__2);
            setState(1403);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1402);
                match(SP);
              }
            }

          }
        }

        setState(1407);
        oC_RelationshipsPattern();
        setState(1409);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1408);
            match(SP);
          }
        }

        setState(1415);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == WHERE) {
          {
            setState(1411);
            oC_Where();
            setState(1413);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1412);
                match(SP);
              }
            }

          }
        }

        setState(1417);
        match(T__10);
        setState(1419);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1418);
            match(SP);
          }
        }

        setState(1421);
        oC_Expression();
        setState(1423);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1422);
            match(SP);
          }
        }

        setState(1425);
        match(T__8);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PropertyLookupContext extends ParserRuleContext {
    public OC_PropertyKeyNameContext oC_PropertyKeyName() {
      return getRuleContext(OC_PropertyKeyNameContext.class, 0);
    }

    public TerminalNode SP() {
      return getToken(CypherParser.SP, 0);
    }

    public OC_PropertyLookupContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PropertyLookup;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PropertyLookup(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PropertyLookup(this);
    }
  }

  public final OC_PropertyLookupContext oC_PropertyLookup() throws RecognitionException {
    OC_PropertyLookupContext _localctx = new OC_PropertyLookupContext(_ctx, getState());
    enterRule(_localctx, 166, RULE_oC_PropertyLookup);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1427);
        match(T__24);
        setState(1429);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1428);
            match(SP);
          }
        }

        {
          setState(1431);
          oC_PropertyKeyName();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_CaseExpressionContext extends ParserRuleContext {
    public TerminalNode END() {
      return getToken(CypherParser.END, 0);
    }

    public TerminalNode ELSE() {
      return getToken(CypherParser.ELSE, 0);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public TerminalNode CASE() {
      return getToken(CypherParser.CASE, 0);
    }

    public List<OC_CaseAlternativeContext> oC_CaseAlternative() {
      return getRuleContexts(OC_CaseAlternativeContext.class);
    }

    public OC_CaseAlternativeContext oC_CaseAlternative(int i) {
      return getRuleContext(OC_CaseAlternativeContext.class, i);
    }

    public OC_CaseExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_CaseExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_CaseExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_CaseExpression(this);
    }
  }

  public final OC_CaseExpressionContext oC_CaseExpression() throws RecognitionException {
    OC_CaseExpressionContext _localctx = new OC_CaseExpressionContext(_ctx, getState());
    enterRule(_localctx, 168, RULE_oC_CaseExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1455);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 267, _ctx)) {
          case 1: {
            {
              setState(1433);
              match(CASE);
              setState(1438);
              _errHandler.sync(this);
              _alt = 1;
              do {
                switch (_alt) {
                  case 1: {
                    {
                      setState(1435);
                      _errHandler.sync(this);
                      _la = _input.LA(1);
                      if (_la == SP) {
                        {
                          setState(1434);
                          match(SP);
                        }
                      }

                      setState(1437);
                      oC_CaseAlternative();
                    }
                  }
                    break;
                  default:
                    throw new NoViableAltException(this);
                }
                setState(1440);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 263, _ctx);
              } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
            }
          }
            break;
          case 2: {
            {
              setState(1442);
              match(CASE);
              setState(1444);
              _errHandler.sync(this);
              _la = _input.LA(1);
              if (_la == SP) {
                {
                  setState(1443);
                  match(SP);
                }
              }

              setState(1446);
              oC_Expression();
              setState(1451);
              _errHandler.sync(this);
              _alt = 1;
              do {
                switch (_alt) {
                  case 1: {
                    {
                      setState(1448);
                      _errHandler.sync(this);
                      _la = _input.LA(1);
                      if (_la == SP) {
                        {
                          setState(1447);
                          match(SP);
                        }
                      }

                      setState(1450);
                      oC_CaseAlternative();
                    }
                  }
                    break;
                  default:
                    throw new NoViableAltException(this);
                }
                setState(1453);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 266, _ctx);
              } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
            }
          }
            break;
        }
        setState(1465);
        _errHandler.sync(this);
        switch (getInterpreter().adaptivePredict(_input, 270, _ctx)) {
          case 1: {
            setState(1458);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1457);
                match(SP);
              }
            }

            setState(1460);
            match(ELSE);
            setState(1462);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1461);
                match(SP);
              }
            }

            setState(1464);
            oC_Expression();
          }
            break;
        }
        setState(1468);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1467);
            match(SP);
          }
        }

        setState(1470);
        match(END);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_CaseAlternativeContext extends ParserRuleContext {
    public TerminalNode WHEN() {
      return getToken(CypherParser.WHEN, 0);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public TerminalNode THEN() {
      return getToken(CypherParser.THEN, 0);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_CaseAlternativeContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_CaseAlternative;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_CaseAlternative(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_CaseAlternative(this);
    }
  }

  public final OC_CaseAlternativeContext oC_CaseAlternative() throws RecognitionException {
    OC_CaseAlternativeContext _localctx = new OC_CaseAlternativeContext(_ctx, getState());
    enterRule(_localctx, 170, RULE_oC_CaseAlternative);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1472);
        match(WHEN);
        setState(1474);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1473);
            match(SP);
          }
        }

        setState(1476);
        oC_Expression();
        setState(1478);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1477);
            match(SP);
          }
        }

        setState(1480);
        match(THEN);
        setState(1482);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1481);
            match(SP);
          }
        }

        setState(1484);
        oC_Expression();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_VariableContext extends ParserRuleContext {
    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public OC_VariableContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Variable;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Variable(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Variable(this);
    }
  }

  public final OC_VariableContext oC_Variable() throws RecognitionException {
    OC_VariableContext _localctx = new OC_VariableContext(_ctx, getState());
    enterRule(_localctx, 172, RULE_oC_Variable);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1486);
        oC_SymbolicName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_NumberLiteralContext extends ParserRuleContext {
    public OC_DoubleLiteralContext oC_DoubleLiteral() {
      return getRuleContext(OC_DoubleLiteralContext.class, 0);
    }

    public OC_IntegerLiteralContext oC_IntegerLiteral() {
      return getRuleContext(OC_IntegerLiteralContext.class, 0);
    }

    public OC_NumberLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_NumberLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_NumberLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_NumberLiteral(this);
    }
  }

  public final OC_NumberLiteralContext oC_NumberLiteral() throws RecognitionException {
    OC_NumberLiteralContext _localctx = new OC_NumberLiteralContext(_ctx, getState());
    enterRule(_localctx, 174, RULE_oC_NumberLiteral);
    try {
      setState(1490);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case ExponentDecimalReal:
        case RegularDecimalReal:
          enterOuterAlt(_localctx, 1); {
          setState(1488);
          oC_DoubleLiteral();
        }
          break;
        case HexInteger:
        case DecimalInteger:
        case OctalInteger:
          enterOuterAlt(_localctx, 2); {
          setState(1489);
          oC_IntegerLiteral();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_MapLiteralContext extends ParserRuleContext {
    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public List<OC_PropertyKeyNameContext> oC_PropertyKeyName() {
      return getRuleContexts(OC_PropertyKeyNameContext.class);
    }

    public OC_PropertyKeyNameContext oC_PropertyKeyName(int i) {
      return getRuleContext(OC_PropertyKeyNameContext.class, i);
    }

    public List<OC_ExpressionContext> oC_Expression() {
      return getRuleContexts(OC_ExpressionContext.class);
    }

    public OC_ExpressionContext oC_Expression(int i) {
      return getRuleContext(OC_ExpressionContext.class, i);
    }

    public OC_MapLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_MapLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_MapLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_MapLiteral(this);
    }
  }

  public final OC_MapLiteralContext oC_MapLiteral() throws RecognitionException {
    OC_MapLiteralContext _localctx = new OC_MapLiteralContext(_ctx, getState());
    enterRule(_localctx, 176, RULE_oC_MapLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1492);
        match(T__22);
        setState(1494);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if (_la == SP) {
          {
            setState(1493);
            match(SP);
          }
        }

        setState(1529);
        _errHandler.sync(this);
        _la = _input.LA(1);
        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNION) | (1L << ALL) | (1L << OPTIONAL)
            | (1L << MATCH) | (1L << UNWIND) | (1L << AS) | (1L << MERGE) | (1L << ON)
            | (1L << CREATE) | (1L << SET) | (1L << DETACH) | (1L << DELETE) | (1L << REMOVE)
            | (1L << WITH) | (1L << RETURN) | (1L << DISTINCT))) != 0)
            || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (ORDER - 64))
                | (1L << (BY - 64)) | (1L << (L_SKIP - 64)) | (1L << (LIMIT - 64))
                | (1L << (ASCENDING - 64)) | (1L << (ASC - 64)) | (1L << (DESCENDING - 64))
                | (1L << (DESC - 64)) | (1L << (WHERE - 64)) | (1L << (OR - 64))
                | (1L << (XOR - 64)) | (1L << (AND - 64)) | (1L << (NOT - 64)) | (1L << (IN - 64))
                | (1L << (STARTS - 64)) | (1L << (ENDS - 64)) | (1L << (CONTAINS - 64))
                | (1L << (IS - 64)) | (1L << (NULL - 64)) | (1L << (COUNT - 64))
                | (1L << (ANY - 64)) | (1L << (NONE - 64)) | (1L << (SINGLE - 64))
                | (1L << (TRUE - 64)) | (1L << (FALSE - 64)) | (1L << (EXISTS - 64))
                | (1L << (CASE - 64)) | (1L << (ELSE - 64)) | (1L << (END - 64))
                | (1L << (WHEN - 64)) | (1L << (THEN - 64)) | (1L << (HexLetter - 64))
                | (1L << (CONSTRAINT - 64)) | (1L << (DO - 64)) | (1L << (FOR - 64))
                | (1L << (REQUIRE - 64)) | (1L << (UNIQUE - 64)) | (1L << (MANDATORY - 64))
                | (1L << (SCALAR - 64)) | (1L << (OF - 64)) | (1L << (ADD - 64))
                | (1L << (DROP - 64)) | (1L << (FILTER - 64)) | (1L << (EXTRACT - 64))
                | (1L << (UnescapedSymbolicName - 64))
                | (1L << (EscapedSymbolicName - 64)))) != 0)) {
          {
            setState(1496);
            oC_PropertyKeyName();
            setState(1498);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1497);
                match(SP);
              }
            }

            setState(1500);
            match(T__9);
            setState(1502);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1501);
                match(SP);
              }
            }

            setState(1504);
            oC_Expression();
            setState(1506);
            _errHandler.sync(this);
            _la = _input.LA(1);
            if (_la == SP) {
              {
                setState(1505);
                match(SP);
              }
            }

            setState(1526);
            _errHandler.sync(this);
            _la = _input.LA(1);
            while (_la == T__1) {
              {
                {
                  setState(1508);
                  match(T__1);
                  setState(1510);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1509);
                      match(SP);
                    }
                  }

                  setState(1512);
                  oC_PropertyKeyName();
                  setState(1514);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1513);
                      match(SP);
                    }
                  }

                  setState(1516);
                  match(T__9);
                  setState(1518);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1517);
                      match(SP);
                    }
                  }

                  setState(1520);
                  oC_Expression();
                  setState(1522);
                  _errHandler.sync(this);
                  _la = _input.LA(1);
                  if (_la == SP) {
                    {
                      setState(1521);
                      match(SP);
                    }
                  }

                }
              }
              setState(1528);
              _errHandler.sync(this);
              _la = _input.LA(1);
            }
          }
        }

        setState(1531);
        match(T__23);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ParameterContext extends ParserRuleContext {
    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public TerminalNode DecimalInteger() {
      return getToken(CypherParser.DecimalInteger, 0);
    }

    public OC_ParameterContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Parameter;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Parameter(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Parameter(this);
    }
  }

  public final OC_ParameterContext oC_Parameter() throws RecognitionException {
    OC_ParameterContext _localctx = new OC_ParameterContext(_ctx, getState());
    enterRule(_localctx, 178, RULE_oC_Parameter);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1533);
        match(T__25);
        setState(1536);
        _errHandler.sync(this);
        switch (_input.LA(1)) {
          case COUNT:
          case ANY:
          case NONE:
          case SINGLE:
          case HexLetter:
          case FILTER:
          case EXTRACT:
          case UnescapedSymbolicName:
          case EscapedSymbolicName: {
            setState(1534);
            oC_SymbolicName();
          }
            break;
          case DecimalInteger: {
            setState(1535);
            match(DecimalInteger);
          }
            break;
          default:
            throw new NoViableAltException(this);
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PropertyExpressionContext extends ParserRuleContext {
    public OC_AtomContext oC_Atom() {
      return getRuleContext(OC_AtomContext.class, 0);
    }

    public List<OC_PropertyLookupContext> oC_PropertyLookup() {
      return getRuleContexts(OC_PropertyLookupContext.class);
    }

    public OC_PropertyLookupContext oC_PropertyLookup(int i) {
      return getRuleContext(OC_PropertyLookupContext.class, i);
    }

    public List<TerminalNode> SP() {
      return getTokens(CypherParser.SP);
    }

    public TerminalNode SP(int i) {
      return getToken(CypherParser.SP, i);
    }

    public OC_PropertyExpressionContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PropertyExpression;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PropertyExpression(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PropertyExpression(this);
    }
  }

  public final OC_PropertyExpressionContext oC_PropertyExpression() throws RecognitionException {
    OC_PropertyExpressionContext _localctx = new OC_PropertyExpressionContext(_ctx, getState());
    enterRule(_localctx, 180, RULE_oC_PropertyExpression);
    int _la;
    try {
      int _alt;
      enterOuterAlt(_localctx, 1);
      {
        setState(1538);
        oC_Atom();
        setState(1543);
        _errHandler.sync(this);
        _alt = 1;
        do {
          switch (_alt) {
            case 1: {
              {
                setState(1540);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == SP) {
                  {
                    setState(1539);
                    match(SP);
                  }
                }

                setState(1542);
                oC_PropertyLookup();
              }
            }
              break;
            default:
              throw new NoViableAltException(this);
          }
          setState(1545);
          _errHandler.sync(this);
          _alt = getInterpreter().adaptivePredict(_input, 288, _ctx);
        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_PropertyKeyNameContext extends ParserRuleContext {
    public OC_SchemaNameContext oC_SchemaName() {
      return getRuleContext(OC_SchemaNameContext.class, 0);
    }

    public OC_PropertyKeyNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_PropertyKeyName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_PropertyKeyName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_PropertyKeyName(this);
    }
  }

  public final OC_PropertyKeyNameContext oC_PropertyKeyName() throws RecognitionException {
    OC_PropertyKeyNameContext _localctx = new OC_PropertyKeyNameContext(_ctx, getState());
    enterRule(_localctx, 182, RULE_oC_PropertyKeyName);
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1547);
        oC_SchemaName();
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_IntegerLiteralContext extends ParserRuleContext {
    public TerminalNode HexInteger() {
      return getToken(CypherParser.HexInteger, 0);
    }

    public TerminalNode OctalInteger() {
      return getToken(CypherParser.OctalInteger, 0);
    }

    public TerminalNode DecimalInteger() {
      return getToken(CypherParser.DecimalInteger, 0);
    }

    public OC_IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_IntegerLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_IntegerLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_IntegerLiteral(this);
    }
  }

  public final OC_IntegerLiteralContext oC_IntegerLiteral() throws RecognitionException {
    OC_IntegerLiteralContext _localctx = new OC_IntegerLiteralContext(_ctx, getState());
    enterRule(_localctx, 184, RULE_oC_IntegerLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1549);
        _la = _input.LA(1);
        if (!(((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (HexInteger - 97))
            | (1L << (DecimalInteger - 97)) | (1L << (OctalInteger - 97)))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_DoubleLiteralContext extends ParserRuleContext {
    public TerminalNode ExponentDecimalReal() {
      return getToken(CypherParser.ExponentDecimalReal, 0);
    }

    public TerminalNode RegularDecimalReal() {
      return getToken(CypherParser.RegularDecimalReal, 0);
    }

    public OC_DoubleLiteralContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_DoubleLiteral;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_DoubleLiteral(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_DoubleLiteral(this);
    }
  }

  public final OC_DoubleLiteralContext oC_DoubleLiteral() throws RecognitionException {
    OC_DoubleLiteralContext _localctx = new OC_DoubleLiteralContext(_ctx, getState());
    enterRule(_localctx, 186, RULE_oC_DoubleLiteral);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1551);
        _la = _input.LA(1);
        if (!(_la == ExponentDecimalReal || _la == RegularDecimalReal)) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SchemaNameContext extends ParserRuleContext {
    public OC_SymbolicNameContext oC_SymbolicName() {
      return getRuleContext(OC_SymbolicNameContext.class, 0);
    }

    public OC_ReservedWordContext oC_ReservedWord() {
      return getRuleContext(OC_ReservedWordContext.class, 0);
    }

    public OC_SchemaNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SchemaName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SchemaName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SchemaName(this);
    }
  }

  public final OC_SchemaNameContext oC_SchemaName() throws RecognitionException {
    OC_SchemaNameContext _localctx = new OC_SchemaNameContext(_ctx, getState());
    enterRule(_localctx, 188, RULE_oC_SchemaName);
    try {
      setState(1555);
      _errHandler.sync(this);
      switch (_input.LA(1)) {
        case COUNT:
        case ANY:
        case NONE:
        case SINGLE:
        case HexLetter:
        case FILTER:
        case EXTRACT:
        case UnescapedSymbolicName:
        case EscapedSymbolicName:
          enterOuterAlt(_localctx, 1); {
          setState(1553);
          oC_SymbolicName();
        }
          break;
        case UNION:
        case ALL:
        case OPTIONAL:
        case MATCH:
        case UNWIND:
        case AS:
        case MERGE:
        case ON:
        case CREATE:
        case SET:
        case DETACH:
        case DELETE:
        case REMOVE:
        case WITH:
        case RETURN:
        case DISTINCT:
        case ORDER:
        case BY:
        case L_SKIP:
        case LIMIT:
        case ASCENDING:
        case ASC:
        case DESCENDING:
        case DESC:
        case WHERE:
        case OR:
        case XOR:
        case AND:
        case NOT:
        case IN:
        case STARTS:
        case ENDS:
        case CONTAINS:
        case IS:
        case NULL:
        case TRUE:
        case FALSE:
        case EXISTS:
        case CASE:
        case ELSE:
        case END:
        case WHEN:
        case THEN:
        case CONSTRAINT:
        case DO:
        case FOR:
        case REQUIRE:
        case UNIQUE:
        case MANDATORY:
        case SCALAR:
        case OF:
        case ADD:
        case DROP:
          enterOuterAlt(_localctx, 2); {
          setState(1554);
          oC_ReservedWord();
        }
          break;
        default:
          throw new NoViableAltException(this);
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_ReservedWordContext extends ParserRuleContext {
    public TerminalNode ALL() {
      return getToken(CypherParser.ALL, 0);
    }

    public TerminalNode ASC() {
      return getToken(CypherParser.ASC, 0);
    }

    public TerminalNode ASCENDING() {
      return getToken(CypherParser.ASCENDING, 0);
    }

    public TerminalNode BY() {
      return getToken(CypherParser.BY, 0);
    }

    public TerminalNode CREATE() {
      return getToken(CypherParser.CREATE, 0);
    }

    public TerminalNode DELETE() {
      return getToken(CypherParser.DELETE, 0);
    }

    public TerminalNode DESC() {
      return getToken(CypherParser.DESC, 0);
    }

    public TerminalNode DESCENDING() {
      return getToken(CypherParser.DESCENDING, 0);
    }

    public TerminalNode DETACH() {
      return getToken(CypherParser.DETACH, 0);
    }

    public TerminalNode EXISTS() {
      return getToken(CypherParser.EXISTS, 0);
    }

    public TerminalNode LIMIT() {
      return getToken(CypherParser.LIMIT, 0);
    }

    public TerminalNode MATCH() {
      return getToken(CypherParser.MATCH, 0);
    }

    public TerminalNode MERGE() {
      return getToken(CypherParser.MERGE, 0);
    }

    public TerminalNode ON() {
      return getToken(CypherParser.ON, 0);
    }

    public TerminalNode OPTIONAL() {
      return getToken(CypherParser.OPTIONAL, 0);
    }

    public TerminalNode ORDER() {
      return getToken(CypherParser.ORDER, 0);
    }

    public TerminalNode REMOVE() {
      return getToken(CypherParser.REMOVE, 0);
    }

    public TerminalNode RETURN() {
      return getToken(CypherParser.RETURN, 0);
    }

    public TerminalNode SET() {
      return getToken(CypherParser.SET, 0);
    }

    public TerminalNode L_SKIP() {
      return getToken(CypherParser.L_SKIP, 0);
    }

    public TerminalNode WHERE() {
      return getToken(CypherParser.WHERE, 0);
    }

    public TerminalNode WITH() {
      return getToken(CypherParser.WITH, 0);
    }

    public TerminalNode UNION() {
      return getToken(CypherParser.UNION, 0);
    }

    public TerminalNode UNWIND() {
      return getToken(CypherParser.UNWIND, 0);
    }

    public TerminalNode AND() {
      return getToken(CypherParser.AND, 0);
    }

    public TerminalNode AS() {
      return getToken(CypherParser.AS, 0);
    }

    public TerminalNode CONTAINS() {
      return getToken(CypherParser.CONTAINS, 0);
    }

    public TerminalNode DISTINCT() {
      return getToken(CypherParser.DISTINCT, 0);
    }

    public TerminalNode ENDS() {
      return getToken(CypherParser.ENDS, 0);
    }

    public TerminalNode IN() {
      return getToken(CypherParser.IN, 0);
    }

    public TerminalNode IS() {
      return getToken(CypherParser.IS, 0);
    }

    public TerminalNode NOT() {
      return getToken(CypherParser.NOT, 0);
    }

    public TerminalNode OR() {
      return getToken(CypherParser.OR, 0);
    }

    public TerminalNode STARTS() {
      return getToken(CypherParser.STARTS, 0);
    }

    public TerminalNode XOR() {
      return getToken(CypherParser.XOR, 0);
    }

    public TerminalNode FALSE() {
      return getToken(CypherParser.FALSE, 0);
    }

    public TerminalNode TRUE() {
      return getToken(CypherParser.TRUE, 0);
    }

    public TerminalNode NULL() {
      return getToken(CypherParser.NULL, 0);
    }

    public TerminalNode CONSTRAINT() {
      return getToken(CypherParser.CONSTRAINT, 0);
    }

    public TerminalNode DO() {
      return getToken(CypherParser.DO, 0);
    }

    public TerminalNode FOR() {
      return getToken(CypherParser.FOR, 0);
    }

    public TerminalNode REQUIRE() {
      return getToken(CypherParser.REQUIRE, 0);
    }

    public TerminalNode UNIQUE() {
      return getToken(CypherParser.UNIQUE, 0);
    }

    public TerminalNode CASE() {
      return getToken(CypherParser.CASE, 0);
    }

    public TerminalNode WHEN() {
      return getToken(CypherParser.WHEN, 0);
    }

    public TerminalNode THEN() {
      return getToken(CypherParser.THEN, 0);
    }

    public TerminalNode ELSE() {
      return getToken(CypherParser.ELSE, 0);
    }

    public TerminalNode END() {
      return getToken(CypherParser.END, 0);
    }

    public TerminalNode MANDATORY() {
      return getToken(CypherParser.MANDATORY, 0);
    }

    public TerminalNode SCALAR() {
      return getToken(CypherParser.SCALAR, 0);
    }

    public TerminalNode OF() {
      return getToken(CypherParser.OF, 0);
    }

    public TerminalNode ADD() {
      return getToken(CypherParser.ADD, 0);
    }

    public TerminalNode DROP() {
      return getToken(CypherParser.DROP, 0);
    }

    public OC_ReservedWordContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_ReservedWord;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_ReservedWord(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_ReservedWord(this);
    }
  }

  public final OC_ReservedWordContext oC_ReservedWord() throws RecognitionException {
    OC_ReservedWordContext _localctx = new OC_ReservedWordContext(_ctx, getState());
    enterRule(_localctx, 190, RULE_oC_ReservedWord);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1557);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << UNION) | (1L << ALL)
            | (1L << OPTIONAL) | (1L << MATCH) | (1L << UNWIND) | (1L << AS) | (1L << MERGE)
            | (1L << ON) | (1L << CREATE) | (1L << SET) | (1L << DETACH) | (1L << DELETE)
            | (1L << REMOVE) | (1L << WITH) | (1L << RETURN) | (1L << DISTINCT))) != 0)
            || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (ORDER - 64))
                | (1L << (BY - 64)) | (1L << (L_SKIP - 64)) | (1L << (LIMIT - 64))
                | (1L << (ASCENDING - 64)) | (1L << (ASC - 64)) | (1L << (DESCENDING - 64))
                | (1L << (DESC - 64)) | (1L << (WHERE - 64)) | (1L << (OR - 64))
                | (1L << (XOR - 64)) | (1L << (AND - 64)) | (1L << (NOT - 64)) | (1L << (IN - 64))
                | (1L << (STARTS - 64)) | (1L << (ENDS - 64)) | (1L << (CONTAINS - 64))
                | (1L << (IS - 64)) | (1L << (NULL - 64)) | (1L << (TRUE - 64))
                | (1L << (FALSE - 64)) | (1L << (EXISTS - 64)) | (1L << (CASE - 64))
                | (1L << (ELSE - 64)) | (1L << (END - 64)) | (1L << (WHEN - 64))
                | (1L << (THEN - 64)) | (1L << (CONSTRAINT - 64)) | (1L << (DO - 64))
                | (1L << (FOR - 64)) | (1L << (REQUIRE - 64)) | (1L << (UNIQUE - 64))
                | (1L << (MANDATORY - 64)) | (1L << (SCALAR - 64)) | (1L << (OF - 64))
                | (1L << (ADD - 64)) | (1L << (DROP - 64)))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_SymbolicNameContext extends ParserRuleContext {
    public TerminalNode UnescapedSymbolicName() {
      return getToken(CypherParser.UnescapedSymbolicName, 0);
    }

    public TerminalNode EscapedSymbolicName() {
      return getToken(CypherParser.EscapedSymbolicName, 0);
    }

    public TerminalNode HexLetter() {
      return getToken(CypherParser.HexLetter, 0);
    }

    public TerminalNode COUNT() {
      return getToken(CypherParser.COUNT, 0);
    }

    public TerminalNode FILTER() {
      return getToken(CypherParser.FILTER, 0);
    }

    public TerminalNode EXTRACT() {
      return getToken(CypherParser.EXTRACT, 0);
    }

    public TerminalNode ANY() {
      return getToken(CypherParser.ANY, 0);
    }

    public TerminalNode NONE() {
      return getToken(CypherParser.NONE, 0);
    }

    public TerminalNode SINGLE() {
      return getToken(CypherParser.SINGLE, 0);
    }

    public OC_SymbolicNameContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_SymbolicName;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_SymbolicName(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_SymbolicName(this);
    }
  }

  public final OC_SymbolicNameContext oC_SymbolicName() throws RecognitionException {
    OC_SymbolicNameContext _localctx = new OC_SymbolicNameContext(_ctx, getState());
    enterRule(_localctx, 192, RULE_oC_SymbolicName);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1559);
        _la = _input.LA(1);
        if (!(((((_la - 83)) & ~0x3f) == 0 && ((1L << (_la - 83)) & ((1L << (COUNT - 83))
            | (1L << (ANY - 83)) | (1L << (NONE - 83)) | (1L << (SINGLE - 83))
            | (1L << (HexLetter - 83)) | (1L << (FILTER - 83)) | (1L << (EXTRACT - 83))
            | (1L << (UnescapedSymbolicName - 83)) | (1L << (EscapedSymbolicName - 83)))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_LeftArrowHeadContext extends ParserRuleContext {
    public OC_LeftArrowHeadContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_LeftArrowHead;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_LeftArrowHead(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_LeftArrowHead(this);
    }
  }

  public final OC_LeftArrowHeadContext oC_LeftArrowHead() throws RecognitionException {
    OC_LeftArrowHeadContext _localctx = new OC_LeftArrowHeadContext(_ctx, getState());
    enterRule(_localctx, 194, RULE_oC_LeftArrowHead);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1561);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__18) | (1L << T__26) | (1L << T__27)
            | (1L << T__28) | (1L << T__29))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_RightArrowHeadContext extends ParserRuleContext {
    public OC_RightArrowHeadContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_RightArrowHead;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_RightArrowHead(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_RightArrowHead(this);
    }
  }

  public final OC_RightArrowHeadContext oC_RightArrowHead() throws RecognitionException {
    OC_RightArrowHeadContext _localctx = new OC_RightArrowHeadContext(_ctx, getState());
    enterRule(_localctx, 196, RULE_oC_RightArrowHead);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1563);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__19) | (1L << T__30) | (1L << T__31)
            | (1L << T__32) | (1L << T__33))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static class OC_DashContext extends ParserRuleContext {
    public OC_DashContext(ParserRuleContext parent, int invokingState) {
      super(parent, invokingState);
    }

    @Override
    public int getRuleIndex() {
      return RULE_oC_Dash;
    }

    @Override
    public void enterRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).enterOC_Dash(this);
    }

    @Override
    public void exitRule(ParseTreeListener listener) {
      if (listener instanceof CypherListener)
        ((CypherListener) listener).exitOC_Dash(this);
    }
  }

  public final OC_DashContext oC_Dash() throws RecognitionException {
    OC_DashContext _localctx = new OC_DashContext(_ctx, getState());
    enterRule(_localctx, 198, RULE_oC_Dash);
    int _la;
    try {
      enterOuterAlt(_localctx, 1);
      {
        setState(1565);
        _la = _input.LA(1);
        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__13) | (1L << T__34) | (1L << T__35)
            | (1L << T__36) | (1L << T__37) | (1L << T__38) | (1L << T__39) | (1L << T__40)
            | (1L << T__41) | (1L << T__42) | (1L << T__43) | (1L << T__44))) != 0))) {
          _errHandler.recoverInline(this);
        } else {
          if (_input.LA(1) == Token.EOF)
            matchedEOF = true;
          _errHandler.reportMatch(this);
          consume();
        }
      }
    } catch (RecognitionException re) {
      _localctx.exception = re;
      _errHandler.reportError(this, re);
      _errHandler.recover(this, re);
    } finally {
      exitRule();
    }
    return _localctx;
  }

  public static final String _serializedATN =
      "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0081\u0622\4\2\t"
          + "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"
          + "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"
          + "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"
          + "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"
          + "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"
          + ",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"
          + "\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="
          + "\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"
          + "\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"
          + "\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"
          + "`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\3\2\5\2\u00cc\n\2\3\2\3\2\5\2\u00d0"
          + "\n\2\3\2\5\2\u00d3\n\2\3\2\5\2\u00d6\n\2\3\2\3\2\3\3\3\3\3\4\3\4\5\4\u00de"
          + "\n\4\3\5\3\5\5\5\u00e2\n\5\3\5\7\5\u00e5\n\5\f\5\16\5\u00e8\13\5\3\6\3"
          + "\6\3\6\3\6\5\6\u00ee\n\6\3\6\3\6\3\6\5\6\u00f3\n\6\3\6\5\6\u00f6\n\6\3"
          + "\7\3\7\5\7\u00fa\n\7\3\b\3\b\5\b\u00fe\n\b\7\b\u0100\n\b\f\b\16\b\u0103"
          + "\13\b\3\b\3\b\3\b\5\b\u0108\n\b\7\b\u010a\n\b\f\b\16\b\u010d\13\b\3\b"
          + "\3\b\5\b\u0111\n\b\3\b\7\b\u0114\n\b\f\b\16\b\u0117\13\b\3\b\5\b\u011a"
          + "\n\b\3\b\5\b\u011d\n\b\5\b\u011f\n\b\3\t\3\t\5\t\u0123\n\t\7\t\u0125\n"
          + "\t\f\t\16\t\u0128\13\t\3\t\3\t\5\t\u012c\n\t\7\t\u012e\n\t\f\t\16\t\u0131"
          + "\13\t\3\t\3\t\5\t\u0135\n\t\6\t\u0137\n\t\r\t\16\t\u0138\3\t\3\t\3\n\3"
          + "\n\3\n\3\n\3\n\5\n\u0142\n\n\3\13\3\13\3\13\5\13\u0147\n\13\3\f\3\f\5"
          + "\f\u014b\n\f\3\f\3\f\5\f\u014f\n\f\3\f\3\f\5\f\u0153\n\f\3\f\5\f\u0156"
          + "\n\f\3\r\3\r\5\r\u015a\n\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\5\16\u0164"
          + "\n\16\3\16\3\16\3\16\7\16\u0169\n\16\f\16\16\16\u016c\13\16\3\17\3\17"
          + "\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u0178\n\17\3\20\3\20\5\20"
          + "\u017c\n\20\3\20\3\20\3\21\3\21\5\21\u0182\n\21\3\21\3\21\5\21\u0186\n"
          + "\21\3\21\3\21\5\21\u018a\n\21\3\21\7\21\u018d\n\21\f\21\16\21\u0190\13"
          + "\21\3\22\3\22\5\22\u0194\n\22\3\22\3\22\5\22\u0198\n\22\3\22\3\22\3\22"
          + "\3\22\5\22\u019e\n\22\3\22\3\22\5\22\u01a2\n\22\3\22\3\22\3\22\3\22\5"
          + "\22\u01a8\n\22\3\22\3\22\5\22\u01ac\n\22\3\22\3\22\3\22\3\22\5\22\u01b2"
          + "\n\22\3\22\3\22\5\22\u01b6\n\22\3\23\3\23\5\23\u01ba\n\23\3\23\3\23\5"
          + "\23\u01be\n\23\3\23\3\23\5\23\u01c2\n\23\3\23\3\23\5\23\u01c6\n\23\3\23"
          + "\7\23\u01c9\n\23\f\23\16\23\u01cc\13\23\3\24\3\24\3\24\3\24\5\24\u01d2"
          + "\n\24\3\24\3\24\5\24\u01d6\n\24\3\24\7\24\u01d9\n\24\f\24\16\24\u01dc"
          + "\13\24\3\25\3\25\3\25\3\25\5\25\u01e2\n\25\3\26\3\26\3\26\3\26\5\26\u01e8"
          + "\n\26\3\26\3\26\3\26\5\26\u01ed\n\26\3\27\3\27\3\27\3\27\5\27\u01f3\n"
          + "\27\3\27\5\27\u01f6\n\27\3\27\3\27\3\27\3\27\5\27\u01fc\n\27\5\27\u01fe"
          + "\n\27\3\30\3\30\5\30\u0202\n\30\3\30\3\30\5\30\u0206\n\30\3\30\7\30\u0209"
          + "\n\30\f\30\16\30\u020c\13\30\3\30\5\30\u020f\n\30\3\30\5\30\u0212\n\30"
          + "\3\31\3\31\3\31\3\31\3\31\5\31\u0219\n\31\3\31\3\31\3\32\3\32\3\32\5\32"
          + "\u0220\n\32\3\32\5\32\u0223\n\32\3\33\3\33\3\33\3\34\5\34\u0229\n\34\3"
          + "\34\5\34\u022c\n\34\3\34\3\34\3\34\3\34\5\34\u0232\n\34\3\34\3\34\5\34"
          + "\u0236\n\34\3\34\3\34\5\34\u023a\n\34\3\35\3\35\5\35\u023e\n\35\3\35\3"
          + "\35\5\35\u0242\n\35\3\35\7\35\u0245\n\35\f\35\16\35\u0248\13\35\3\35\3"
          + "\35\5\35\u024c\n\35\3\35\3\35\5\35\u0250\n\35\3\35\7\35\u0253\n\35\f\35"
          + "\16\35\u0256\13\35\5\35\u0258\n\35\3\36\3\36\3\36\3\36\3\36\3\36\3\36"
          + "\5\36\u0261\n\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\5\37\u026a\n\37\3"
          + "\37\7\37\u026d\n\37\f\37\16\37\u0270\13\37\3 \3 \3 \3 \3!\3!\3!\3!\3\""
          + "\3\"\5\"\u027c\n\"\3\"\5\"\u027f\n\"\3#\3#\3#\3#\3$\3$\5$\u0287\n$\3$"
          + "\3$\5$\u028b\n$\3$\7$\u028e\n$\f$\16$\u0291\13$\3%\3%\5%\u0295\n%\3%\3"
          + "%\5%\u0299\n%\3%\3%\3%\5%\u029e\n%\3&\3&\3\'\3\'\5\'\u02a4\n\'\3\'\7\'"
          + "\u02a7\n\'\f\'\16\'\u02aa\13\'\3\'\3\'\3\'\3\'\5\'\u02b0\n\'\3(\3(\5("
          + "\u02b4\n(\3(\3(\5(\u02b8\n(\5(\u02ba\n(\3(\3(\5(\u02be\n(\5(\u02c0\n("
          + "\3(\3(\5(\u02c4\n(\5(\u02c6\n(\3(\3(\3)\3)\5)\u02cc\n)\3)\3)\3*\3*\5*"
          + "\u02d2\n*\3*\3*\5*\u02d6\n*\3*\5*\u02d9\n*\3*\5*\u02dc\n*\3*\3*\5*\u02e0"
          + "\n*\3*\3*\3*\3*\5*\u02e6\n*\3*\3*\5*\u02ea\n*\3*\5*\u02ed\n*\3*\5*\u02f0"
          + "\n*\3*\3*\3*\3*\5*\u02f6\n*\3*\5*\u02f9\n*\3*\5*\u02fc\n*\3*\3*\5*\u0300"
          + "\n*\3*\3*\3*\3*\5*\u0306\n*\3*\5*\u0309\n*\3*\5*\u030c\n*\3*\3*\5*\u0310"
          + "\n*\3+\3+\5+\u0314\n+\3+\3+\5+\u0318\n+\5+\u031a\n+\3+\3+\5+\u031e\n+"
          + "\5+\u0320\n+\3+\5+\u0323\n+\3+\3+\5+\u0327\n+\5+\u0329\n+\3+\3+\3,\3,"
          + "\5,\u032f\n,\3-\3-\5-\u0333\n-\3-\3-\5-\u0337\n-\3-\3-\5-\u033b\n-\3-"
          + "\5-\u033e\n-\3-\7-\u0341\n-\f-\16-\u0344\13-\3.\3.\5.\u0348\n.\3.\7.\u034b"
          + "\n.\f.\16.\u034e\13.\3/\3/\5/\u0352\n/\3/\3/\3\60\3\60\5\60\u0358\n\60"
          + "\3\60\3\60\5\60\u035c\n\60\5\60\u035e\n\60\3\60\3\60\5\60\u0362\n\60\3"
          + "\60\3\60\5\60\u0366\n\60\5\60\u0368\n\60\5\60\u036a\n\60\3\61\3\61\3\62"
          + "\3\62\3\63\3\63\3\64\3\64\3\64\3\64\3\64\7\64\u0377\n\64\f\64\16\64\u037a"
          + "\13\64\3\65\3\65\3\65\3\65\3\65\7\65\u0381\n\65\f\65\16\65\u0384\13\65"
          + "\3\66\3\66\3\66\3\66\3\66\7\66\u038b\n\66\f\66\16\66\u038e\13\66\3\67"
          + "\3\67\5\67\u0392\n\67\7\67\u0394\n\67\f\67\16\67\u0397\13\67\3\67\3\67"
          + "\38\38\58\u039d\n8\38\78\u03a0\n8\f8\168\u03a3\138\39\39\59\u03a7\n9\3"
          + "9\39\59\u03ab\n9\39\39\59\u03af\n9\39\39\59\u03b3\n9\39\79\u03b6\n9\f"
          + "9\169\u03b9\139\3:\3:\5:\u03bd\n:\3:\3:\5:\u03c1\n:\3:\3:\5:\u03c5\n:"
          + "\3:\3:\5:\u03c9\n:\3:\3:\5:\u03cd\n:\3:\3:\5:\u03d1\n:\3:\7:\u03d4\n:"
          + "\f:\16:\u03d7\13:\3;\3;\5;\u03db\n;\3;\3;\5;\u03df\n;\3;\7;\u03e2\n;\f"
          + ";\16;\u03e5\13;\3<\3<\5<\u03e9\n<\7<\u03eb\n<\f<\16<\u03ee\13<\3<\3<\3"
          + "=\3=\3=\3=\7=\u03f6\n=\f=\16=\u03f9\13=\3>\3>\3>\5>\u03fe\n>\3>\3>\5>"
          + "\u0402\n>\3>\3>\3>\3>\3>\5>\u0409\n>\3>\3>\5>\u040d\n>\3>\3>\5>\u0411"
          + "\n>\3>\5>\u0414\n>\3?\3?\3?\3?\3?\3?\3?\3?\3?\3?\5?\u0420\n?\3?\5?\u0423"
          + "\n?\3?\3?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\5@\u0431\n@\3A\3A\5A\u0435\nA"
          + "\3A\7A\u0438\nA\fA\16A\u043b\13A\3A\5A\u043e\nA\3A\5A\u0441\nA\3B\3B\3"
          + "B\3B\3B\5B\u0448\nB\3B\3B\5B\u044c\nB\3B\3B\5B\u0450\nB\3B\3B\3B\3B\3"
          + "B\5B\u0457\nB\3B\3B\5B\u045b\nB\3B\3B\5B\u045f\nB\3B\3B\3B\3B\5B\u0465"
          + "\nB\3B\3B\5B\u0469\nB\3B\3B\5B\u046d\nB\3B\3B\3B\3B\5B\u0473\nB\3B\3B"
          + "\5B\u0477\nB\3B\3B\5B\u047b\nB\3B\3B\3B\3B\5B\u0481\nB\3B\3B\5B\u0485"
          + "\nB\3B\3B\5B\u0489\nB\3B\3B\3B\3B\3B\3B\3B\5B\u0492\nB\3C\3C\3C\3C\3C"
          + "\3C\5C\u049a\nC\3D\3D\3E\3E\5E\u04a0\nE\3E\3E\5E\u04a4\nE\3E\3E\5E\u04a8"
          + "\nE\3E\3E\5E\u04ac\nE\7E\u04ae\nE\fE\16E\u04b1\13E\5E\u04b3\nE\3E\3E\3"
          + "F\3F\5F\u04b9\nF\3F\3F\3F\5F\u04be\nF\3F\3F\3F\5F\u04c3\nF\3F\3F\3F\5"
          + "F\u04c8\nF\3F\3F\3F\5F\u04cd\nF\3F\3F\3F\5F\u04d2\nF\3F\5F\u04d5\nF\3"
          + "G\3G\5G\u04d9\nG\3G\3G\5G\u04dd\nG\3G\3G\3H\3H\5H\u04e3\nH\3H\6H\u04e6"
          + "\nH\rH\16H\u04e7\3I\3I\5I\u04ec\nI\3I\5I\u04ef\nI\3J\3J\3J\3J\3J\3J\3"
          + "K\3K\5K\u04f9\nK\3K\3K\5K\u04fd\nK\3K\3K\5K\u0501\nK\5K\u0503\nK\3K\3"
          + "K\5K\u0507\nK\3K\3K\5K\u050b\nK\3K\3K\5K\u050f\nK\7K\u0511\nK\fK\16K\u0514"
          + "\13K\5K\u0516\nK\3K\3K\3L\3L\3L\3M\3M\5M\u051f\nM\3M\3M\5M\u0523\nM\3"
          + "M\3M\3M\5M\u0528\nM\3M\5M\u052b\nM\5M\u052d\nM\3M\5M\u0530\nM\3M\3M\3"
          + "N\3N\5N\u0536\nN\3N\3N\5N\u053a\nN\3N\3N\5N\u053e\nN\3N\3N\5N\u0542\n"
          + "N\3N\3N\5N\u0546\nN\7N\u0548\nN\fN\16N\u054b\13N\5N\u054d\nN\3N\3N\3O"
          + "\3O\3P\3P\3Q\3Q\3Q\3R\3R\3R\7R\u055b\nR\fR\16R\u055e\13R\3S\3S\5S\u0562"
          + "\nS\3S\3S\5S\u0566\nS\3S\3S\5S\u056a\nS\3S\5S\u056d\nS\3S\5S\u0570\nS"
          + "\3S\3S\3T\3T\5T\u0576\nT\3T\3T\5T\u057a\nT\3T\3T\5T\u057e\nT\5T\u0580"
          + "\nT\3T\3T\5T\u0584\nT\3T\3T\5T\u0588\nT\5T\u058a\nT\3T\3T\5T\u058e\nT"
          + "\3T\3T\5T\u0592\nT\3T\3T\3U\3U\5U\u0598\nU\3U\3U\3V\3V\5V\u059e\nV\3V"
          + "\6V\u05a1\nV\rV\16V\u05a2\3V\3V\5V\u05a7\nV\3V\3V\5V\u05ab\nV\3V\6V\u05ae"
          + "\nV\rV\16V\u05af\5V\u05b2\nV\3V\5V\u05b5\nV\3V\3V\5V\u05b9\nV\3V\5V\u05bc"
          + "\nV\3V\5V\u05bf\nV\3V\3V\3W\3W\5W\u05c5\nW\3W\3W\5W\u05c9\nW\3W\3W\5W"
          + "\u05cd\nW\3W\3W\3X\3X\3Y\3Y\5Y\u05d5\nY\3Z\3Z\5Z\u05d9\nZ\3Z\3Z\5Z\u05dd"
          + "\nZ\3Z\3Z\5Z\u05e1\nZ\3Z\3Z\5Z\u05e5\nZ\3Z\3Z\5Z\u05e9\nZ\3Z\3Z\5Z\u05ed"
          + "\nZ\3Z\3Z\5Z\u05f1\nZ\3Z\3Z\5Z\u05f5\nZ\7Z\u05f7\nZ\fZ\16Z\u05fa\13Z\5"
          + "Z\u05fc\nZ\3Z\3Z\3[\3[\3[\5[\u0603\n[\3\\\3\\\5\\\u0607\n\\\3\\\6\\\u060a"
          + "\n\\\r\\\16\\\u060b\3]\3]\3^\3^\3_\3_\3`\3`\5`\u0616\n`\3a\3a\3b\3b\3"
          + "c\3c\3d\3d\3e\3e\3e\2\2f\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&("
          + "*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084"
          + "\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c"
          + "\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4"
          + "\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\2\f\3\2F"
          + "I\3\2\17\20\3\2YZ\3\2ce\3\2mn\6\2\60<?TY`ox\6\2UXffy{~~\4\2\25\25\35 "
          + "\4\2\26\26!$\4\2\20\20%/\2\u0700\2\u00cb\3\2\2\2\4\u00d9\3\2\2\2\6\u00dd"
          + "\3\2\2\2\b\u00df\3\2\2\2\n\u00f5\3\2\2\2\f\u00f9\3\2\2\2\16\u011e\3\2"
          + "\2\2\20\u0136\3\2\2\2\22\u0141\3\2\2\2\24\u0146\3\2\2\2\26\u014a\3\2\2"
          + "\2\30\u0157\3\2\2\2\32\u0161\3\2\2\2\34\u0177\3\2\2\2\36\u0179\3\2\2\2"
          + " \u017f\3\2\2\2\"\u01b5\3\2\2\2$\u01b9\3\2\2\2&\u01cd\3\2\2\2(\u01e1\3"
          + "\2\2\2*\u01e3\3\2\2\2,\u01ee\3\2\2\2.\u01ff\3\2\2\2\60\u0218\3\2\2\2\62"
          + "\u021c\3\2\2\2\64\u0224\3\2\2\2\66\u022b\3\2\2\28\u0257\3\2\2\2:\u0260"
          + "\3\2\2\2<\u0262\3\2\2\2>\u0271\3\2\2\2@\u0275\3\2\2\2B\u0279\3\2\2\2D"
          + "\u0280\3\2\2\2F\u0284\3\2\2\2H\u029d\3\2\2\2J\u029f\3\2\2\2L\u02af\3\2"
          + "\2\2N\u02b1\3\2\2\2P\u02c9\3\2\2\2R\u030f\3\2\2\2T\u0311\3\2\2\2V\u032e"
          + "\3\2\2\2X\u0330\3\2\2\2Z\u0345\3\2\2\2\\\u034f\3\2\2\2^\u0355\3\2\2\2"
          + "`\u036b\3\2\2\2b\u036d\3\2\2\2d\u036f\3\2\2\2f\u0371\3\2\2\2h\u037b\3"
          + "\2\2\2j\u0385\3\2\2\2l\u0395\3\2\2\2n\u039a\3\2\2\2p\u03a4\3\2\2\2r\u03ba"
          + "\3\2\2\2t\u03d8\3\2\2\2v\u03ec\3\2\2\2x\u03f1\3\2\2\2z\u0413\3\2\2\2|"
          + "\u041f\3\2\2\2~\u0430\3\2\2\2\u0080\u0432\3\2\2\2\u0082\u0491\3\2\2\2"
          + "\u0084\u0499\3\2\2\2\u0086\u049b\3\2\2\2\u0088\u049d\3\2\2\2\u008a\u04d4"
          + "\3\2\2\2\u008c\u04d6\3\2\2\2\u008e\u04e0\3\2\2\2\u0090\u04e9\3\2\2\2\u0092"
          + "\u04f0\3\2\2\2\u0094\u04f6\3\2\2\2\u0096\u0519\3\2\2\2\u0098\u051c\3\2"
          + "\2\2\u009a\u0533\3\2\2\2\u009c\u0550\3\2\2\2\u009e\u0552\3\2\2\2\u00a0"
          + "\u0554\3\2\2\2\u00a2\u055c\3\2\2\2\u00a4\u055f\3\2\2\2\u00a6\u0573\3\2"
          + "\2\2\u00a8\u0595\3\2\2\2\u00aa\u05b1\3\2\2\2\u00ac\u05c2\3\2\2\2\u00ae"
          + "\u05d0\3\2\2\2\u00b0\u05d4\3\2\2\2\u00b2\u05d6\3\2\2\2\u00b4\u05ff\3\2"
          + "\2\2\u00b6\u0604\3\2\2\2\u00b8\u060d\3\2\2\2\u00ba\u060f\3\2\2\2\u00bc"
          + "\u0611\3\2\2\2\u00be\u0615\3\2\2\2\u00c0\u0617\3\2\2\2\u00c2\u0619\3\2"
          + "\2\2\u00c4\u061b\3\2\2\2\u00c6\u061d\3\2\2\2\u00c8\u061f\3\2\2\2\u00ca"
          + "\u00cc\7\177\2\2\u00cb\u00ca\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00cd\3"
          + "\2\2\2\u00cd\u00d2\5\4\3\2\u00ce\u00d0\7\177\2\2\u00cf\u00ce\3\2\2\2\u00cf"
          + "\u00d0\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d3\7\3\2\2\u00d2\u00cf\3\2"
          + "\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d5\3\2\2\2\u00d4\u00d6\7\177\2\2\u00d5"
          + "\u00d4\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8\7\2"
          + "\2\3\u00d8\3\3\2\2\2\u00d9\u00da\5\6\4\2\u00da\5\3\2\2\2\u00db\u00de\5"
          + "\b\5\2\u00dc\u00de\5,\27\2\u00dd\u00db\3\2\2\2\u00dd\u00dc\3\2\2\2\u00de"
          + "\7\3\2\2\2\u00df\u00e6\5\f\7\2\u00e0\u00e2\7\177\2\2\u00e1\u00e0\3\2\2"
          + "\2\u00e1\u00e2\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00e5\5\n\6\2\u00e4\u00e1"
          + "\3\2\2\2\u00e5\u00e8\3\2\2\2\u00e6\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7"
          + "\t\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e9\u00ea\7\60\2\2\u00ea\u00eb\7\177"
          + "\2\2\u00eb\u00ed\7\61\2\2\u00ec\u00ee\7\177\2\2\u00ed\u00ec\3\2\2\2\u00ed"
          + "\u00ee\3\2\2\2\u00ee\u00ef\3\2\2\2\u00ef\u00f6\5\f\7\2\u00f0\u00f2\7\60"
          + "\2\2\u00f1\u00f3\7\177\2\2\u00f2\u00f1\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3"
          + "\u00f4\3\2\2\2\u00f4\u00f6\5\f\7\2\u00f5\u00e9\3\2\2\2\u00f5\u00f0\3\2"
          + "\2\2\u00f6\13\3\2\2\2\u00f7\u00fa\5\16\b\2\u00f8\u00fa\5\20\t\2\u00f9"
          + "\u00f7\3\2\2\2\u00f9\u00f8\3\2\2\2\u00fa\r\3\2\2\2\u00fb\u00fd\5\24\13"
          + "\2\u00fc\u00fe\7\177\2\2\u00fd\u00fc\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe"
          + "\u0100\3\2\2\2\u00ff\u00fb\3\2\2\2\u0100\u0103\3\2\2\2\u0101\u00ff\3\2"
          + "\2\2\u0101\u0102\3\2\2\2\u0102\u0104\3\2\2\2\u0103\u0101\3\2\2\2\u0104"
          + "\u011f\5\64\33\2\u0105\u0107\5\24\13\2\u0106\u0108\7\177\2\2\u0107\u0106"
          + "\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u010a\3\2\2\2\u0109\u0105\3\2\2\2\u010a"
          + "\u010d\3\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010c\u010e\3\2"
          + "\2\2\u010d\u010b\3\2\2\2\u010e\u0115\5\22\n\2\u010f\u0111\7\177\2\2\u0110"
          + "\u010f\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\5\22"
          + "\n\2\u0113\u0110\3\2\2\2\u0114\u0117\3\2\2\2\u0115\u0113\3\2\2\2\u0115"
          + "\u0116\3\2\2\2\u0116\u011c\3\2\2\2\u0117\u0115\3\2\2\2\u0118\u011a\7\177"
          + "\2\2\u0119\u0118\3\2\2\2\u0119\u011a\3\2\2\2\u011a\u011b\3\2\2\2\u011b"
          + "\u011d\5\64\33\2\u011c\u0119\3\2\2\2\u011c\u011d\3\2\2\2\u011d\u011f\3"
          + "\2\2\2\u011e\u0101\3\2\2\2\u011e\u010b\3\2\2\2\u011f\17\3\2\2\2\u0120"
          + "\u0122\5\24\13\2\u0121\u0123\7\177\2\2\u0122\u0121\3\2\2\2\u0122\u0123"
          + "\3\2\2\2\u0123\u0125\3\2\2\2\u0124\u0120\3\2\2\2\u0125\u0128\3\2\2\2\u0126"
          + "\u0124\3\2\2\2\u0126\u0127\3\2\2\2\u0127\u012f\3\2\2\2\u0128\u0126\3\2"
          + "\2\2\u0129\u012b\5\22\n\2\u012a\u012c\7\177\2\2\u012b\u012a\3\2\2\2\u012b"
          + "\u012c\3\2\2\2\u012c\u012e\3\2\2\2\u012d\u0129\3\2\2\2\u012e\u0131\3\2"
          + "\2\2\u012f\u012d\3\2\2\2\u012f\u0130\3\2\2\2\u0130\u0132\3\2\2\2\u0131"
          + "\u012f\3\2\2\2\u0132\u0134\5\62\32\2\u0133\u0135\7\177\2\2\u0134\u0133"
          + "\3\2\2\2\u0134\u0135\3\2\2\2\u0135\u0137\3\2\2\2\u0136\u0126\3\2\2\2\u0137"
          + "\u0138\3\2\2\2\u0138\u0136\3\2\2\2\u0138\u0139\3\2\2\2\u0139\u013a\3\2"
          + "\2\2\u013a\u013b\5\16\b\2\u013b\21\3\2\2\2\u013c\u0142\5\36\20\2\u013d"
          + "\u0142\5\32\16\2\u013e\u0142\5$\23\2\u013f\u0142\5 \21\2\u0140\u0142\5"
          + "&\24\2\u0141\u013c\3\2\2\2\u0141\u013d\3\2\2\2\u0141\u013e\3\2\2\2\u0141"
          + "\u013f\3\2\2\2\u0141\u0140\3\2\2\2\u0142\23\3\2\2\2\u0143\u0147\5\26\f"
          + "\2\u0144\u0147\5\30\r\2\u0145\u0147\5*\26\2\u0146\u0143\3\2\2\2\u0146"
          + "\u0144\3\2\2\2\u0146\u0145\3\2\2\2\u0147\25\3\2\2\2\u0148\u0149\7\62\2"
          + "\2\u0149\u014b\7\177\2\2\u014a\u0148\3\2\2\2\u014a\u014b\3\2\2\2\u014b"
          + "\u014c\3\2\2\2\u014c\u014e\7\63\2\2\u014d\u014f\7\177\2\2\u014e\u014d"
          + "\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0150\3\2\2\2\u0150\u0155\5F$\2\u0151"
          + "\u0153\7\177\2\2\u0152\u0151\3\2\2\2\u0152\u0153\3\2\2\2\u0153\u0154\3"
          + "\2\2\2\u0154\u0156\5D#\2\u0155\u0152\3\2\2\2\u0155\u0156\3\2\2\2\u0156"
          + "\27\3\2\2\2\u0157\u0159\7\64\2\2\u0158\u015a\7\177\2\2\u0159\u0158\3\2"
          + "\2\2\u0159\u015a\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u015c\5d\63\2\u015c"
          + "\u015d\7\177\2\2\u015d\u015e\7\65\2\2\u015e\u015f\7\177\2\2\u015f\u0160"
          + "\5\u00aeX\2\u0160\31\3\2\2\2\u0161\u0163\7\66\2\2\u0162\u0164\7\177\2"
          + "\2\u0163\u0162\3\2\2\2\u0163\u0164\3\2\2\2\u0164\u0165\3\2\2\2\u0165\u016a"
          + "\5H%\2\u0166\u0167\7\177\2\2\u0167\u0169\5\34\17\2\u0168\u0166\3\2\2\2"
          + "\u0169\u016c\3\2\2\2\u016a\u0168\3\2\2\2\u016a\u016b\3\2\2\2\u016b\33"
          + "\3\2\2\2\u016c\u016a\3\2\2\2\u016d\u016e\7\67\2\2\u016e\u016f\7\177\2"
          + "\2\u016f\u0170\7\63\2\2\u0170\u0171\7\177\2\2\u0171\u0178\5 \21\2\u0172"
          + "\u0173\7\67\2\2\u0173\u0174\7\177\2\2\u0174\u0175\78\2\2\u0175\u0176\7"
          + "\177\2\2\u0176\u0178\5 \21\2\u0177\u016d\3\2\2\2\u0177\u0172\3\2\2\2\u0178"
          + "\35\3\2\2\2\u0179\u017b\78\2\2\u017a\u017c\7\177\2\2\u017b\u017a\3\2\2"
          + "\2\u017b\u017c\3\2\2\2\u017c\u017d\3\2\2\2\u017d\u017e\5F$\2\u017e\37"
          + "\3\2\2\2\u017f\u0181\79\2\2\u0180\u0182\7\177\2\2\u0181\u0180\3\2\2\2"
          + "\u0181\u0182\3\2\2\2\u0182\u0183\3\2\2\2\u0183\u018e\5\"\22\2\u0184\u0186"
          + "\7\177\2\2\u0185\u0184\3\2\2\2\u0185\u0186\3\2\2\2\u0186\u0187\3\2\2\2"
          + "\u0187\u0189\7\4\2\2\u0188\u018a\7\177\2\2\u0189\u0188\3\2\2\2\u0189\u018a"
          + "\3\2\2\2\u018a\u018b\3\2\2\2\u018b\u018d\5\"\22\2\u018c\u0185\3\2\2\2"
          + "\u018d\u0190\3\2\2\2\u018e\u018c\3\2\2\2\u018e\u018f\3\2\2\2\u018f!\3"
          + "\2\2\2\u0190\u018e\3\2\2\2\u0191\u0193\5\u00b6\\\2\u0192\u0194\7\177\2"
          + "\2\u0193\u0192\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0195\3\2\2\2\u0195\u0197"
          + "\7\5\2\2\u0196\u0198\7\177\2\2\u0197\u0196\3\2\2\2\u0197\u0198\3\2\2\2"
          + "\u0198\u0199\3\2\2\2\u0199\u019a\5d\63\2\u019a\u01b6\3\2\2\2\u019b\u019d"
          + "\5\u00aeX\2\u019c\u019e\7\177\2\2\u019d\u019c\3\2\2\2\u019d\u019e\3\2"
          + "\2\2\u019e\u019f\3\2\2\2\u019f\u01a1\7\5\2\2\u01a0\u01a2\7\177\2\2\u01a1"
          + "\u01a0\3\2\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3\u01a4\5d"
          + "\63\2\u01a4\u01b6\3\2\2\2\u01a5\u01a7\5\u00aeX\2\u01a6\u01a8\7\177\2\2"
          + "\u01a7\u01a6\3\2\2\2\u01a7\u01a8\3\2\2\2\u01a8\u01a9\3\2\2\2\u01a9\u01ab"
          + "\7\6\2\2\u01aa\u01ac\7\177\2\2\u01ab\u01aa\3\2\2\2\u01ab\u01ac\3\2\2\2"
          + "\u01ac\u01ad\3\2\2\2\u01ad\u01ae\5d\63\2\u01ae\u01b6\3\2\2\2\u01af\u01b1"
          + "\5\u00aeX\2\u01b0\u01b2\7\177\2\2\u01b1\u01b0\3\2\2\2\u01b1\u01b2\3\2"
          + "\2\2\u01b2\u01b3\3\2\2\2\u01b3\u01b4\5Z.\2\u01b4\u01b6\3\2\2\2\u01b5\u0191"
          + "\3\2\2\2\u01b5\u019b\3\2\2\2\u01b5\u01a5\3\2\2\2\u01b5\u01af\3\2\2\2\u01b6"
          + "#\3\2\2\2\u01b7\u01b8\7:\2\2\u01b8\u01ba\7\177\2\2\u01b9\u01b7\3\2\2\2"
          + "\u01b9\u01ba\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb\u01bd\7;\2\2\u01bc\u01be"
          + "\7\177\2\2\u01bd\u01bc\3\2\2\2\u01bd\u01be\3\2\2\2\u01be\u01bf\3\2\2\2"
          + "\u01bf\u01ca\5d\63\2\u01c0\u01c2\7\177\2\2\u01c1\u01c0\3\2\2\2\u01c1\u01c2"
          + "\3\2\2\2\u01c2\u01c3\3\2\2\2\u01c3\u01c5\7\4\2\2\u01c4\u01c6\7\177\2\2"
          + "\u01c5\u01c4\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6\u01c7\3\2\2\2\u01c7\u01c9"
          + "\5d\63\2\u01c8\u01c1\3\2\2\2\u01c9\u01cc\3\2\2\2\u01ca\u01c8\3\2\2\2\u01ca"
          + "\u01cb\3\2\2\2\u01cb%\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cd\u01ce\7<\2\2\u01ce"
          + "\u01cf\7\177\2\2\u01cf\u01da\5(\25\2\u01d0\u01d2\7\177\2\2\u01d1\u01d0"
          + "\3\2\2\2\u01d1\u01d2\3\2\2\2\u01d2\u01d3\3\2\2\2\u01d3\u01d5\7\4\2\2\u01d4"
          + "\u01d6\7\177\2\2\u01d5\u01d4\3\2\2\2\u01d5\u01d6\3\2\2\2\u01d6\u01d7\3"
          + "\2\2\2\u01d7\u01d9\5(\25\2\u01d8\u01d1\3\2\2\2\u01d9\u01dc\3\2\2\2\u01da"
          + "\u01d8\3\2\2\2\u01da\u01db\3\2\2\2\u01db\'\3\2\2\2\u01dc\u01da\3\2\2\2"
          + "\u01dd\u01de\5\u00aeX\2\u01de\u01df\5Z.\2\u01df\u01e2\3\2\2\2\u01e0\u01e2"
          + "\5\u00b6\\\2\u01e1\u01dd\3\2\2\2\u01e1\u01e0\3\2\2\2\u01e2)\3\2\2\2\u01e3"
          + "\u01e4\7=\2\2\u01e4\u01e5\7\177\2\2\u01e5\u01ec\5\u009aN\2\u01e6\u01e8"
          + "\7\177\2\2\u01e7\u01e6\3\2\2\2\u01e7\u01e8\3\2\2\2\u01e8\u01e9\3\2\2\2"
          + "\u01e9\u01ea\7>\2\2\u01ea\u01eb\7\177\2\2\u01eb\u01ed\5.\30\2\u01ec\u01e7"
          + "\3\2\2\2\u01ec\u01ed\3\2\2\2\u01ed+\3\2\2\2\u01ee\u01ef\7=\2\2\u01ef\u01f2"
          + "\7\177\2\2\u01f0\u01f3\5\u009aN\2\u01f1\u01f3\5\u009cO\2\u01f2\u01f0\3"
          + "\2\2\2\u01f2\u01f1\3\2\2\2\u01f3\u01fd\3\2\2\2\u01f4\u01f6\7\177\2\2\u01f5"
          + "\u01f4\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6\u01f7\3\2\2\2\u01f7\u01f8\7>"
          + "\2\2\u01f8\u01fb\7\177\2\2\u01f9\u01fc\7\7\2\2\u01fa\u01fc\5.\30\2\u01fb"
          + "\u01f9\3\2\2\2\u01fb\u01fa\3\2\2\2\u01fc\u01fe\3\2\2\2\u01fd\u01f5\3\2"
          + "\2\2\u01fd\u01fe\3\2\2\2\u01fe-\3\2\2\2\u01ff\u020a\5\60\31\2\u0200\u0202"
          + "\7\177\2\2\u0201\u0200\3\2\2\2\u0201\u0202\3\2\2\2\u0202\u0203\3\2\2\2"
          + "\u0203\u0205\7\4\2\2\u0204\u0206\7\177\2\2\u0205\u0204\3\2\2\2\u0205\u0206"
          + "\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0209\5\60\31\2\u0208\u0201\3\2\2\2"
          + "\u0209\u020c\3\2\2\2\u020a\u0208\3\2\2\2\u020a\u020b\3\2\2\2\u020b\u0211"
          + "\3\2\2\2\u020c\u020a\3\2\2\2\u020d\u020f\7\177\2\2\u020e\u020d\3\2\2\2"
          + "\u020e\u020f\3\2\2\2\u020f\u0210\3\2\2\2\u0210\u0212\5D#\2\u0211\u020e"
          + "\3\2\2\2\u0211\u0212\3\2\2\2\u0212/\3\2\2\2\u0213\u0214\5\u009eP\2\u0214"
          + "\u0215\7\177\2\2\u0215\u0216\7\65\2\2\u0216\u0217\7\177\2\2\u0217\u0219"
          + "\3\2\2\2\u0218\u0213\3\2\2\2\u0218\u0219\3\2\2\2\u0219\u021a\3\2\2\2\u021a"
          + "\u021b\5\u00aeX\2\u021b\61\3\2\2\2\u021c\u021d\7?\2\2\u021d\u0222\5\66"
          + "\34\2\u021e\u0220\7\177\2\2\u021f\u021e\3\2\2\2\u021f\u0220\3\2\2\2\u0220"
          + "\u0221\3\2\2\2\u0221\u0223\5D#\2\u0222\u021f\3\2\2\2\u0222\u0223\3\2\2"
          + "\2\u0223\63\3\2\2\2\u0224\u0225\7@\2\2\u0225\u0226\5\66\34\2\u0226\65"
          + "\3\2\2\2\u0227\u0229\7\177\2\2\u0228\u0227\3\2\2\2\u0228\u0229\3\2\2\2"
          + "\u0229\u022a\3\2\2\2\u022a\u022c\7A\2\2\u022b\u0228\3\2\2\2\u022b\u022c"
          + "\3\2\2\2\u022c\u022d\3\2\2\2\u022d\u022e\7\177\2\2\u022e\u0231\58\35\2"
          + "\u022f\u0230\7\177\2\2\u0230\u0232\5<\37\2\u0231\u022f\3\2\2\2\u0231\u0232"
          + "\3\2\2\2\u0232\u0235\3\2\2\2\u0233\u0234\7\177\2\2\u0234\u0236\5> \2\u0235"
          + "\u0233\3\2\2\2\u0235\u0236\3\2\2\2\u0236\u0239\3\2\2\2\u0237\u0238\7\177"
          + "\2\2\u0238\u023a\5@!\2\u0239\u0237\3\2\2\2\u0239\u023a\3\2\2\2\u023a\67"
          + "\3\2\2\2\u023b\u0246\7\7\2\2\u023c\u023e\7\177\2\2\u023d\u023c\3\2\2\2"
          + "\u023d\u023e\3\2\2\2\u023e\u023f\3\2\2\2\u023f\u0241\7\4\2\2\u0240\u0242"
          + "\7\177\2\2\u0241\u0240\3\2\2\2\u0241\u0242\3\2\2\2\u0242\u0243\3\2\2\2"
          + "\u0243\u0245\5:\36\2\u0244\u023d\3\2\2\2\u0245\u0248\3\2\2\2\u0246\u0244"
          + "\3\2\2\2\u0246\u0247\3\2\2\2\u0247\u0258\3\2\2\2\u0248\u0246\3\2\2\2\u0249"
          + "\u0254\5:\36\2\u024a\u024c\7\177\2\2\u024b\u024a\3\2\2\2\u024b\u024c\3"
          + "\2\2\2\u024c\u024d\3\2\2\2\u024d\u024f\7\4\2\2\u024e\u0250\7\177\2\2\u024f"
          + "\u024e\3\2\2\2\u024f\u0250\3\2\2\2\u0250\u0251\3\2\2\2\u0251\u0253\5:"
          + "\36\2\u0252\u024b\3\2\2\2\u0253\u0256\3\2\2\2\u0254\u0252\3\2\2\2\u0254"
          + "\u0255\3\2\2\2\u0255\u0258\3\2\2\2\u0256\u0254\3\2\2\2\u0257\u023b\3\2"
          + "\2\2\u0257\u0249\3\2\2\2\u02589\3\2\2\2\u0259\u025a\5d\63\2\u025a\u025b"
          + "\7\177\2\2\u025b\u025c\7\65\2\2\u025c\u025d\7\177\2\2\u025d\u025e\5\u00ae"
          + "X\2\u025e\u0261\3\2\2\2\u025f\u0261\5d\63\2\u0260\u0259\3\2\2\2\u0260"
          + "\u025f\3\2\2\2\u0261;\3\2\2\2\u0262\u0263\7B\2\2\u0263\u0264\7\177\2\2"
          + "\u0264\u0265\7C\2\2\u0265\u0266\7\177\2\2\u0266\u026e\5B\"\2\u0267\u0269"
          + "\7\4\2\2\u0268\u026a\7\177\2\2\u0269\u0268\3\2\2\2\u0269\u026a\3\2\2\2"
          + "\u026a\u026b\3\2\2\2\u026b\u026d\5B\"\2\u026c\u0267\3\2\2\2\u026d\u0270"
          + "\3\2\2\2\u026e\u026c\3\2\2\2\u026e\u026f\3\2\2\2\u026f=\3\2\2\2\u0270"
          + "\u026e\3\2\2\2\u0271\u0272\7D\2\2\u0272\u0273\7\177\2\2\u0273\u0274\5"
          + "d\63\2\u0274?\3\2\2\2\u0275\u0276\7E\2\2\u0276\u0277\7\177\2\2\u0277\u0278"
          + "\5d\63\2\u0278A\3\2\2\2\u0279\u027e\5d\63\2\u027a\u027c\7\177\2\2\u027b"
          + "\u027a\3\2\2\2\u027b\u027c\3\2\2\2\u027c\u027d\3\2\2\2\u027d\u027f\t\2"
          + "\2\2\u027e\u027b\3\2\2\2\u027e\u027f\3\2\2\2\u027fC\3\2\2\2\u0280\u0281"
          + "\7J\2\2\u0281\u0282\7\177\2\2\u0282\u0283\5d\63\2\u0283E\3\2\2\2\u0284"
          + "\u028f\5H%\2\u0285\u0287\7\177\2\2\u0286\u0285\3\2\2\2\u0286\u0287\3\2"
          + "\2\2\u0287\u0288\3\2\2\2\u0288\u028a\7\4\2\2\u0289\u028b\7\177\2\2\u028a"
          + "\u0289\3\2\2\2\u028a\u028b\3\2\2\2\u028b\u028c\3\2\2\2\u028c\u028e\5H"
          + "%\2\u028d\u0286\3\2\2\2\u028e\u0291\3\2\2\2\u028f\u028d\3\2\2\2\u028f"
          + "\u0290\3\2\2\2\u0290G\3\2\2\2\u0291\u028f\3\2\2\2\u0292\u0294\5\u00ae"
          + "X\2\u0293\u0295\7\177\2\2\u0294\u0293\3\2\2\2\u0294\u0295\3\2\2\2\u0295"
          + "\u0296\3\2\2\2\u0296\u0298\7\5\2\2\u0297\u0299\7\177\2\2\u0298\u0297\3"
          + "\2\2\2\u0298\u0299\3\2\2\2\u0299\u029a\3\2\2\2\u029a\u029b\5J&\2\u029b"
          + "\u029e\3\2\2\2\u029c\u029e\5J&\2\u029d\u0292\3\2\2\2\u029d\u029c\3\2\2"
          + "\2\u029eI\3\2\2\2\u029f\u02a0\5L\'\2\u02a0K\3\2\2\2\u02a1\u02a8\5N(\2"
          + "\u02a2\u02a4\7\177\2\2\u02a3\u02a2\3\2\2\2\u02a3\u02a4\3\2\2\2\u02a4\u02a5"
          + "\3\2\2\2\u02a5\u02a7\5P)\2\u02a6\u02a3\3\2\2\2\u02a7\u02aa\3\2\2\2\u02a8"
          + "\u02a6\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9\u02b0\3\2\2\2\u02aa\u02a8\3\2"
          + "\2\2\u02ab\u02ac\7\b\2\2\u02ac\u02ad\5L\'\2\u02ad\u02ae\7\t\2\2\u02ae"
          + "\u02b0\3\2\2\2\u02af\u02a1\3\2\2\2\u02af\u02ab\3\2\2\2\u02b0M\3\2\2\2"
          + "\u02b1\u02b3\7\b\2\2\u02b2\u02b4\7\177\2\2\u02b3\u02b2\3\2\2\2\u02b3\u02b4"
          + "\3\2\2\2\u02b4\u02b9\3\2\2\2\u02b5\u02b7\5\u00aeX\2\u02b6\u02b8\7\177"
          + "\2\2\u02b7\u02b6\3\2\2\2\u02b7\u02b8\3\2\2\2\u02b8\u02ba\3\2\2\2\u02b9"
          + "\u02b5\3\2\2\2\u02b9\u02ba\3\2\2\2\u02ba\u02bf\3\2\2\2\u02bb\u02bd\5Z"
          + ".\2\u02bc\u02be\7\177\2\2\u02bd\u02bc\3\2\2\2\u02bd\u02be\3\2\2\2\u02be"
          + "\u02c0\3\2\2\2\u02bf\u02bb\3\2\2\2\u02bf\u02c0\3\2\2\2\u02c0\u02c5\3\2"
          + "\2\2\u02c1\u02c3\5V,\2\u02c2\u02c4\7\177\2\2\u02c3\u02c2\3\2\2\2\u02c3"
          + "\u02c4\3\2\2\2\u02c4\u02c6\3\2\2\2\u02c5\u02c1\3\2\2\2\u02c5\u02c6\3\2"
          + "\2\2\u02c6\u02c7\3\2\2\2\u02c7\u02c8\7\t\2\2\u02c8O\3\2\2\2\u02c9\u02cb"
          + "\5R*\2\u02ca\u02cc\7\177\2\2\u02cb\u02ca\3\2\2\2\u02cb\u02cc\3\2\2\2\u02cc"
          + "\u02cd\3\2\2\2\u02cd\u02ce\5N(\2\u02ceQ\3\2\2\2\u02cf\u02d1\5\u00c4c\2"
          + "\u02d0\u02d2\7\177\2\2\u02d1\u02d0\3\2\2\2\u02d1\u02d2\3\2\2\2\u02d2\u02d3"
          + "\3\2\2\2\u02d3\u02d5\5\u00c8e\2\u02d4\u02d6\7\177\2\2\u02d5\u02d4\3\2"
          + "\2\2\u02d5\u02d6\3\2\2\2\u02d6\u02d8\3\2\2\2\u02d7\u02d9\5T+\2\u02d8\u02d7"
          + "\3\2\2\2\u02d8\u02d9\3\2\2\2\u02d9\u02db\3\2\2\2\u02da\u02dc\7\177\2\2"
          + "\u02db\u02da\3\2\2\2\u02db\u02dc\3\2\2\2\u02dc\u02dd\3\2\2\2\u02dd\u02df"
          + "\5\u00c8e\2\u02de\u02e0\7\177\2\2\u02df\u02de\3\2\2\2\u02df\u02e0\3\2"
          + "\2\2\u02e0\u02e1\3\2\2\2\u02e1\u02e2\5\u00c6d\2\u02e2\u0310\3\2\2\2\u02e3"
          + "\u02e5\5\u00c4c\2\u02e4\u02e6\7\177\2\2\u02e5\u02e4\3\2\2\2\u02e5\u02e6"
          + "\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7\u02e9\5\u00c8e\2\u02e8\u02ea\7\177"
          + "\2\2\u02e9\u02e8\3\2\2\2\u02e9\u02ea\3\2\2\2\u02ea\u02ec\3\2\2\2\u02eb"
          + "\u02ed\5T+\2\u02ec\u02eb\3\2\2\2\u02ec\u02ed\3\2\2\2\u02ed\u02ef\3\2\2"
          + "\2\u02ee\u02f0\7\177\2\2\u02ef\u02ee\3\2\2\2\u02ef\u02f0\3\2\2\2\u02f0"
          + "\u02f1\3\2\2\2\u02f1\u02f2\5\u00c8e\2\u02f2\u0310\3\2\2\2\u02f3\u02f5"
          + "\5\u00c8e\2\u02f4\u02f6\7\177\2\2\u02f5\u02f4\3\2\2\2\u02f5\u02f6\3\2"
          + "\2\2\u02f6\u02f8\3\2\2\2\u02f7\u02f9\5T+\2\u02f8\u02f7\3\2\2\2\u02f8\u02f9"
          + "\3\2\2\2\u02f9\u02fb\3\2\2\2\u02fa\u02fc\7\177\2\2\u02fb\u02fa\3\2\2\2"
          + "\u02fb\u02fc\3\2\2\2\u02fc\u02fd\3\2\2\2\u02fd\u02ff\5\u00c8e\2\u02fe"
          + "\u0300\7\177\2\2\u02ff\u02fe\3\2\2\2\u02ff\u0300\3\2\2\2\u0300\u0301\3"
          + "\2\2\2\u0301\u0302\5\u00c6d\2\u0302\u0310\3\2\2\2\u0303\u0305\5\u00c8"
          + "e\2\u0304\u0306\7\177\2\2\u0305\u0304\3\2\2\2\u0305\u0306\3\2\2\2\u0306"
          + "\u0308\3\2\2\2\u0307\u0309\5T+\2\u0308\u0307\3\2\2\2\u0308\u0309\3\2\2"
          + "\2\u0309\u030b\3\2\2\2\u030a\u030c\7\177\2\2\u030b\u030a\3\2\2\2\u030b"
          + "\u030c\3\2\2\2\u030c\u030d\3\2\2\2\u030d\u030e\5\u00c8e\2\u030e\u0310"
          + "\3\2\2\2\u030f\u02cf\3\2\2\2\u030f\u02e3\3\2\2\2\u030f\u02f3\3\2\2\2\u030f"
          + "\u0303\3\2\2\2\u0310S\3\2\2\2\u0311\u0313\7\n\2\2\u0312\u0314\7\177\2"
          + "\2\u0313\u0312\3\2\2\2\u0313\u0314\3\2\2\2\u0314\u0319\3\2\2\2\u0315\u0317"
          + "\5\u00aeX\2\u0316\u0318\7\177\2\2\u0317\u0316\3\2\2\2\u0317\u0318\3\2"
          + "\2\2\u0318\u031a\3\2\2\2\u0319\u0315\3\2\2\2\u0319\u031a\3\2\2\2\u031a"
          + "\u031f\3\2\2\2\u031b\u031d\5X-\2\u031c\u031e\7\177\2\2\u031d\u031c\3\2"
          + "\2\2\u031d\u031e\3\2\2\2\u031e\u0320\3\2\2\2\u031f\u031b\3\2\2\2\u031f"
          + "\u0320\3\2\2\2\u0320\u0322\3\2\2\2\u0321\u0323\5^\60\2\u0322\u0321\3\2"
          + "\2\2\u0322\u0323\3\2\2\2\u0323\u0328\3\2\2\2\u0324\u0326\5V,\2\u0325\u0327"
          + "\7\177\2\2\u0326\u0325\3\2\2\2\u0326\u0327\3\2\2\2\u0327\u0329\3\2\2\2"
          + "\u0328\u0324\3\2\2\2\u0328\u0329\3\2\2\2\u0329\u032a\3\2\2\2\u032a\u032b"
          + "\7\13\2\2\u032bU\3\2\2\2\u032c\u032f\5\u00b2Z\2\u032d\u032f\5\u00b4[\2"
          + "\u032e\u032c\3\2\2\2\u032e\u032d\3\2\2\2\u032fW\3\2\2\2\u0330\u0332\7"
          + "\f\2\2\u0331\u0333\7\177\2\2\u0332\u0331\3\2\2\2\u0332\u0333\3\2\2\2\u0333"
          + "\u0334\3\2\2\2\u0334\u0342\5b\62\2\u0335\u0337\7\177\2\2\u0336\u0335\3"
          + "\2\2\2\u0336\u0337\3\2\2\2\u0337\u0338\3\2\2\2\u0338\u033a\7\r\2\2\u0339"
          + "\u033b\7\f\2\2\u033a\u0339\3\2\2\2\u033a\u033b\3\2\2\2\u033b\u033d\3\2"
          + "\2\2\u033c\u033e\7\177\2\2\u033d\u033c\3\2\2\2\u033d\u033e\3\2\2\2\u033e"
          + "\u033f\3\2\2\2\u033f\u0341\5b\62\2\u0340\u0336\3\2\2\2\u0341\u0344\3\2"
          + "\2\2\u0342\u0340\3\2\2\2\u0342\u0343\3\2\2\2\u0343Y\3\2\2\2\u0344\u0342"
          + "\3\2\2\2\u0345\u034c\5\\/\2\u0346\u0348\7\177\2\2\u0347\u0346\3\2\2\2"
          + "\u0347\u0348\3\2\2\2\u0348\u0349\3\2\2\2\u0349\u034b\5\\/\2\u034a\u0347"
          + "\3\2\2\2\u034b\u034e\3\2\2\2\u034c\u034a\3\2\2\2\u034c\u034d\3\2\2\2\u034d"
          + "[\3\2\2\2\u034e\u034c\3\2\2\2\u034f\u0351\7\f\2\2\u0350\u0352\7\177\2"
          + "\2\u0351\u0350\3\2\2\2\u0351\u0352\3\2\2\2\u0352\u0353\3\2\2\2\u0353\u0354"
          + "\5`\61\2\u0354]\3\2\2\2\u0355\u0357\7\7\2\2\u0356\u0358\7\177\2\2\u0357"
          + "\u0356\3\2\2\2\u0357\u0358\3\2\2\2\u0358\u035d\3\2\2\2\u0359\u035b\5\u00ba"
          + "^\2\u035a\u035c\7\177\2\2\u035b\u035a\3\2\2\2\u035b\u035c\3\2\2\2\u035c"
          + "\u035e\3\2\2\2\u035d\u0359\3\2\2\2\u035d\u035e\3\2\2\2\u035e\u0369\3\2"
          + "\2\2\u035f\u0361\7\16\2\2\u0360\u0362\7\177\2\2\u0361\u0360\3\2\2\2\u0361"
          + "\u0362\3\2\2\2\u0362\u0367\3\2\2\2\u0363\u0365\5\u00ba^\2\u0364\u0366"
          + "\7\177\2\2\u0365\u0364\3\2\2\2\u0365\u0366\3\2\2\2\u0366\u0368\3\2\2\2"
          + "\u0367\u0363\3\2\2\2\u0367\u0368\3\2\2\2\u0368\u036a\3\2\2\2\u0369\u035f"
          + "\3\2\2\2\u0369\u036a\3\2\2\2\u036a_\3\2\2\2\u036b\u036c\5\u00be`\2\u036c"
          + "a\3\2\2\2\u036d\u036e\5\u00be`\2\u036ec\3\2\2\2\u036f\u0370\5f\64\2\u0370"
          + "e\3\2\2\2\u0371\u0378\5h\65\2\u0372\u0373\7\177\2\2\u0373\u0374\7K\2\2"
          + "\u0374\u0375\7\177\2\2\u0375\u0377\5h\65\2\u0376\u0372\3\2\2\2\u0377\u037a"
          + "\3\2\2\2\u0378\u0376\3\2\2\2\u0378\u0379\3\2\2\2\u0379g\3\2\2\2\u037a"
          + "\u0378\3\2\2\2\u037b\u0382\5j\66\2\u037c\u037d\7\177\2\2\u037d\u037e\7"
          + "L\2\2\u037e\u037f\7\177\2\2\u037f\u0381\5j\66\2\u0380\u037c\3\2\2\2\u0381"
          + "\u0384\3\2\2\2\u0382\u0380\3\2\2\2\u0382\u0383\3\2\2\2\u0383i\3\2\2\2"
          + "\u0384\u0382\3\2\2\2\u0385\u038c\5l\67\2\u0386\u0387\7\177\2\2\u0387\u0388"
          + "\7M\2\2\u0388\u0389\7\177\2\2\u0389\u038b\5l\67\2\u038a\u0386\3\2\2\2"
          + "\u038b\u038e\3\2\2\2\u038c\u038a\3\2\2\2\u038c\u038d\3\2\2\2\u038dk\3"
          + "\2\2\2\u038e\u038c\3\2\2\2\u038f\u0391\7N\2\2\u0390\u0392\7\177\2\2\u0391"
          + "\u0390\3\2\2\2\u0391\u0392\3\2\2\2\u0392\u0394\3\2\2\2\u0393\u038f\3\2"
          + "\2\2\u0394\u0397\3\2\2\2\u0395\u0393\3\2\2\2\u0395\u0396\3\2\2\2\u0396"
          + "\u0398\3\2\2\2\u0397\u0395\3\2\2\2\u0398\u0399\5n8\2\u0399m\3\2\2\2\u039a"
          + "\u03a1\5p9\2\u039b\u039d\7\177\2\2\u039c\u039b\3\2\2\2\u039c\u039d\3\2"
          + "\2\2\u039d\u039e\3\2\2\2\u039e\u03a0\5\u008aF\2\u039f\u039c\3\2\2\2\u03a0"
          + "\u03a3\3\2\2\2\u03a1\u039f\3\2\2\2\u03a1\u03a2\3\2\2\2\u03a2o\3\2\2\2"
          + "\u03a3\u03a1\3\2\2\2\u03a4\u03b7\5r:\2\u03a5\u03a7\7\177\2\2\u03a6\u03a5"
          + "\3\2\2\2\u03a6\u03a7\3\2\2\2\u03a7\u03a8\3\2\2\2\u03a8\u03aa\7\17\2\2"
          + "\u03a9\u03ab\7\177\2\2\u03aa\u03a9\3\2\2\2\u03aa\u03ab\3\2\2\2\u03ab\u03ac"
          + "\3\2\2\2\u03ac\u03b6\5r:\2\u03ad\u03af\7\177\2\2\u03ae\u03ad\3\2\2\2\u03ae"
          + "\u03af\3\2\2\2\u03af\u03b0\3\2\2\2\u03b0\u03b2\7\20\2\2\u03b1\u03b3\7"
          + "\177\2\2\u03b2\u03b1\3\2\2\2\u03b2\u03b3\3\2\2\2\u03b3\u03b4\3\2\2\2\u03b4"
          + "\u03b6\5r:\2\u03b5\u03a6\3\2\2\2\u03b5\u03ae\3\2\2\2\u03b6\u03b9\3\2\2"
          + "\2\u03b7\u03b5\3\2\2\2\u03b7\u03b8\3\2\2\2\u03b8q\3\2\2\2\u03b9\u03b7"
          + "\3\2\2\2\u03ba\u03d5\5t;\2\u03bb\u03bd\7\177\2\2\u03bc\u03bb\3\2\2\2\u03bc"
          + "\u03bd\3\2\2\2\u03bd\u03be\3\2\2\2\u03be\u03c0\7\7\2\2\u03bf\u03c1\7\177"
          + "\2\2\u03c0\u03bf\3\2\2\2\u03c0\u03c1\3\2\2\2\u03c1\u03c2\3\2\2\2\u03c2"
          + "\u03d4\5t;\2\u03c3\u03c5\7\177\2\2\u03c4\u03c3\3\2\2\2\u03c4\u03c5\3\2"
          + "\2\2\u03c5\u03c6\3\2\2\2\u03c6\u03c8\7\21\2\2\u03c7\u03c9\7\177\2\2\u03c8"
          + "\u03c7\3\2\2\2\u03c8\u03c9\3\2\2\2\u03c9\u03ca\3\2\2\2\u03ca\u03d4\5t"
          + ";\2\u03cb\u03cd\7\177\2\2\u03cc\u03cb\3\2\2\2\u03cc\u03cd\3\2\2\2\u03cd"
          + "\u03ce\3\2\2\2\u03ce\u03d0\7\22\2\2\u03cf\u03d1\7\177\2\2\u03d0\u03cf"
          + "\3\2\2\2\u03d0\u03d1\3\2\2\2\u03d1\u03d2\3\2\2\2\u03d2\u03d4\5t;\2\u03d3"
          + "\u03bc\3\2\2\2\u03d3\u03c4\3\2\2\2\u03d3\u03cc\3\2\2\2\u03d4\u03d7\3\2"
          + "\2\2\u03d5\u03d3\3\2\2\2\u03d5\u03d6\3\2\2\2\u03d6s\3\2\2\2\u03d7\u03d5"
          + "\3\2\2\2\u03d8\u03e3\5v<\2\u03d9\u03db\7\177\2\2\u03da\u03d9\3\2\2\2\u03da"
          + "\u03db\3\2\2\2\u03db\u03dc\3\2\2\2\u03dc\u03de\7\23\2\2\u03dd\u03df\7"
          + "\177\2\2\u03de\u03dd\3\2\2\2\u03de\u03df\3\2\2\2\u03df\u03e0\3\2\2\2\u03e0"
          + "\u03e2\5v<\2\u03e1\u03da\3\2\2\2\u03e2\u03e5\3\2\2\2\u03e3\u03e1\3\2\2"
          + "\2\u03e3\u03e4\3\2\2\2\u03e4u\3\2\2\2\u03e5\u03e3\3\2\2\2\u03e6\u03e8"
          + "\t\3\2\2\u03e7\u03e9\7\177\2\2\u03e8\u03e7\3\2\2\2\u03e8\u03e9\3\2\2\2"
          + "\u03e9\u03eb\3\2\2\2\u03ea\u03e6\3\2\2\2\u03eb\u03ee\3\2\2\2\u03ec\u03ea"
          + "\3\2\2\2\u03ec\u03ed\3\2\2\2\u03ed\u03ef\3\2\2\2\u03ee\u03ec\3\2\2\2\u03ef"
          + "\u03f0\5x=\2\u03f0w\3\2\2\2\u03f1\u03f7\5\u0080A\2\u03f2\u03f6\5|?\2\u03f3"
          + "\u03f6\5z>\2\u03f4\u03f6\5~@\2\u03f5\u03f2\3\2\2\2\u03f5\u03f3\3\2\2\2"
          + "\u03f5\u03f4\3\2\2\2\u03f6\u03f9\3\2\2\2\u03f7\u03f5\3\2\2\2\u03f7\u03f8"
          + "\3\2\2\2\u03f8y\3\2\2\2\u03f9\u03f7\3\2\2\2\u03fa\u03fb\7\177\2\2\u03fb"
          + "\u03fd\7O\2\2\u03fc\u03fe\7\177\2\2\u03fd\u03fc\3\2\2\2\u03fd\u03fe\3"
          + "\2\2\2\u03fe\u03ff\3\2\2\2\u03ff\u0414\5\u0080A\2\u0400\u0402\7\177\2"
          + "\2\u0401\u0400\3\2\2\2\u0401\u0402\3\2\2\2\u0402\u0403\3\2\2\2\u0403\u0404"
          + "\7\n\2\2\u0404\u0405\5d\63\2\u0405\u0406\7\13\2\2\u0406\u0414\3\2\2\2"
          + "\u0407\u0409\7\177\2\2\u0408\u0407\3\2\2\2\u0408\u0409\3\2\2\2\u0409\u040a"
          + "\3\2\2\2\u040a\u040c\7\n\2\2\u040b\u040d\5d\63\2\u040c\u040b\3\2\2\2\u040c"
          + "\u040d\3\2\2\2\u040d\u040e\3\2\2\2\u040e\u0410\7\16\2\2\u040f\u0411\5"
          + "d\63\2\u0410\u040f\3\2\2\2\u0410\u0411\3\2\2\2\u0411\u0412\3\2\2\2\u0412"
          + "\u0414\7\13\2\2\u0413\u03fa\3\2\2\2\u0413\u0401\3\2\2\2\u0413\u0408\3"
          + "\2\2\2\u0414{\3\2\2\2\u0415\u0416\7\177\2\2\u0416\u0417\7P\2\2\u0417\u0418"
          + "\7\177\2\2\u0418\u0420\7?\2\2\u0419\u041a\7\177\2\2\u041a\u041b\7Q\2\2"
          + "\u041b\u041c\7\177\2\2\u041c\u0420\7?\2\2\u041d\u041e\7\177\2\2\u041e"
          + "\u0420\7R\2\2\u041f\u0415\3\2\2\2\u041f\u0419\3\2\2\2\u041f\u041d\3\2"
          + "\2\2\u0420\u0422\3\2\2\2\u0421\u0423\7\177\2\2\u0422\u0421\3\2\2\2\u0422"
          + "\u0423\3\2\2\2\u0423\u0424\3\2\2\2\u0424\u0425\5\u0080A\2\u0425}\3\2\2"
          + "\2\u0426\u0427\7\177\2\2\u0427\u0428\7S\2\2\u0428\u0429\7\177\2\2\u0429"
          + "\u0431\7T\2\2\u042a\u042b\7\177\2\2\u042b\u042c\7S\2\2\u042c\u042d\7\177"
          + "\2\2\u042d\u042e\7N\2\2\u042e\u042f\7\177\2\2\u042f\u0431\7T\2\2\u0430"
          + "\u0426\3\2\2\2\u0430\u042a\3\2\2\2\u0431\177\3\2\2\2\u0432\u0439\5\u0082"
          + "B\2\u0433\u0435\7\177\2\2\u0434\u0433\3\2\2\2\u0434\u0435\3\2\2\2\u0435"
          + "\u0436\3\2\2\2\u0436\u0438\5\u00a8U\2\u0437\u0434\3\2\2\2\u0438\u043b"
          + "\3\2\2\2\u0439\u0437\3\2\2\2\u0439\u043a\3\2\2\2\u043a\u0440\3\2\2\2\u043b"
          + "\u0439\3\2\2\2\u043c\u043e\7\177\2\2\u043d\u043c\3\2\2\2\u043d\u043e\3"
          + "\2\2\2\u043e\u043f\3\2\2\2\u043f\u0441\5Z.\2\u0440\u043d\3\2\2\2\u0440"
          + "\u0441\3\2\2\2\u0441\u0081\3\2\2\2\u0442\u0492\5\u0084C\2\u0443\u0492"
          + "\5\u00b4[\2\u0444\u0492\5\u00aaV\2\u0445\u0447\7U\2\2\u0446\u0448\7\177"
          + "\2\2\u0447\u0446\3\2\2\2\u0447\u0448\3\2\2\2\u0448\u0449\3\2\2\2\u0449"
          + "\u044b\7\b\2\2\u044a\u044c\7\177\2\2\u044b\u044a\3\2\2\2\u044b\u044c\3"
          + "\2\2\2\u044c\u044d\3\2\2\2\u044d\u044f\7\7\2\2\u044e\u0450\7\177\2\2\u044f"
          + "\u044e\3\2\2\2\u044f\u0450\3\2\2\2\u0450\u0451\3\2\2\2\u0451\u0492\7\t"
          + "\2\2\u0452\u0492\5\u00a4S\2\u0453\u0492\5\u00a6T\2\u0454\u0456\7\61\2"
          + "\2\u0455\u0457\7\177\2\2\u0456\u0455\3\2\2\2\u0456\u0457\3\2\2\2\u0457"
          + "\u0458\3\2\2\2\u0458\u045a\7\b\2\2\u0459\u045b\7\177\2\2\u045a\u0459\3"
          + "\2\2\2\u045a\u045b\3\2\2\2\u045b\u045c\3\2\2\2\u045c\u045e\5\u0090I\2"
          + "\u045d\u045f\7\177\2\2\u045e\u045d\3\2\2\2\u045e\u045f\3\2\2\2\u045f\u0460"
          + "\3\2\2\2\u0460\u0461\7\t\2\2\u0461\u0492\3\2\2\2\u0462\u0464\7V\2\2\u0463"
          + "\u0465\7\177\2\2\u0464\u0463\3\2\2\2\u0464\u0465\3\2\2\2\u0465\u0466\3"
          + "\2\2\2\u0466\u0468\7\b\2\2\u0467\u0469\7\177\2\2\u0468\u0467\3\2\2\2\u0468"
          + "\u0469\3\2\2\2\u0469\u046a\3\2\2\2\u046a\u046c\5\u0090I\2\u046b\u046d"
          + "\7\177\2\2\u046c\u046b\3\2\2\2\u046c\u046d\3\2\2\2\u046d\u046e\3\2\2\2"
          + "\u046e\u046f\7\t\2\2\u046f\u0492\3\2\2\2\u0470\u0472\7W\2\2\u0471\u0473"
          + "\7\177\2\2\u0472\u0471\3\2\2\2\u0472\u0473\3\2\2\2\u0473\u0474\3\2\2\2"
          + "\u0474\u0476\7\b\2\2\u0475\u0477\7\177\2\2\u0476\u0475\3\2\2\2\u0476\u0477"
          + "\3\2\2\2\u0477\u0478\3\2\2\2\u0478\u047a\5\u0090I\2\u0479\u047b\7\177"
          + "\2\2\u047a\u0479\3\2\2\2\u047a\u047b\3\2\2\2\u047b\u047c\3\2\2\2\u047c"
          + "\u047d\7\t\2\2\u047d\u0492\3\2\2\2\u047e\u0480\7X\2\2\u047f\u0481\7\177"
          + "\2\2\u0480\u047f\3\2\2\2\u0480\u0481\3\2\2\2\u0481\u0482\3\2\2\2\u0482"
          + "\u0484\7\b\2\2\u0483\u0485\7\177\2\2\u0484\u0483\3\2\2\2\u0484\u0485\3"
          + "\2\2\2\u0485\u0486\3\2\2\2\u0486\u0488\5\u0090I\2\u0487\u0489\7\177\2"
          + "\2\u0488\u0487\3\2\2\2\u0488\u0489\3\2\2\2\u0489\u048a\3\2\2\2\u048a\u048b"
          + "\7\t\2\2\u048b\u0492\3\2\2\2\u048c\u0492\5\u008eH\2\u048d\u0492\5\u008c"
          + "G\2\u048e\u0492\5\u0094K\2\u048f\u0492\5\u0098M\2\u0490\u0492\5\u00ae"
          + "X\2\u0491\u0442\3\2\2\2\u0491\u0443\3\2\2\2\u0491\u0444\3\2\2\2\u0491"
          + "\u0445\3\2\2\2\u0491\u0452\3\2\2\2\u0491\u0453\3\2\2\2\u0491\u0454\3\2"
          + "\2\2\u0491\u0462\3\2\2\2\u0491\u0470\3\2\2\2\u0491\u047e\3\2\2\2\u0491"
          + "\u048c\3\2\2\2\u0491\u048d\3\2\2\2\u0491\u048e\3\2\2\2\u0491\u048f\3\2"
          + "\2\2\u0491\u0490\3\2\2\2\u0492\u0083\3\2\2\2\u0493\u049a\5\u00b0Y\2\u0494"
          + "\u049a\7a\2\2\u0495\u049a\5\u0086D\2\u0496\u049a\7T\2\2\u0497\u049a\5"
          + "\u00b2Z\2\u0498\u049a\5\u0088E\2\u0499\u0493\3\2\2\2\u0499\u0494\3\2\2"
          + "\2\u0499\u0495\3\2\2\2\u0499\u0496\3\2\2\2\u0499\u0497\3\2\2\2\u0499\u0498"
          + "\3\2\2\2\u049a\u0085\3\2\2\2\u049b\u049c\t\4\2\2\u049c\u0087\3\2\2\2\u049d"
          + "\u049f\7\n\2\2\u049e\u04a0\7\177\2\2\u049f\u049e\3\2\2\2\u049f\u04a0\3"
          + "\2\2\2\u04a0\u04b2\3\2\2\2\u04a1\u04a3\5d\63\2\u04a2\u04a4\7\177\2\2\u04a3"
          + "\u04a2\3\2\2\2\u04a3\u04a4\3\2\2\2\u04a4\u04af\3\2\2\2\u04a5\u04a7\7\4"
          + "\2\2\u04a6\u04a8\7\177\2\2\u04a7\u04a6\3\2\2\2\u04a7\u04a8\3\2\2\2\u04a8"
          + "\u04a9\3\2\2\2\u04a9\u04ab\5d\63\2\u04aa\u04ac\7\177\2\2\u04ab\u04aa\3"
          + "\2\2\2\u04ab\u04ac\3\2\2\2\u04ac\u04ae\3\2\2\2\u04ad\u04a5\3\2\2\2\u04ae"
          + "\u04b1\3\2\2\2\u04af\u04ad\3\2\2\2\u04af\u04b0\3\2\2\2\u04b0\u04b3\3\2"
          + "\2\2\u04b1\u04af\3\2\2\2\u04b2\u04a1\3\2\2\2\u04b2\u04b3\3\2\2\2\u04b3"
          + "\u04b4\3\2\2\2\u04b4\u04b5\7\13\2\2\u04b5\u0089\3\2\2\2\u04b6\u04b8\7"
          + "\5\2\2\u04b7\u04b9\7\177\2\2\u04b8\u04b7\3\2\2\2\u04b8\u04b9\3\2\2\2\u04b9"
          + "\u04ba\3\2\2\2\u04ba\u04d5\5p9\2\u04bb\u04bd\7\24\2\2\u04bc\u04be\7\177"
          + "\2\2\u04bd\u04bc\3\2\2\2\u04bd\u04be\3\2\2\2\u04be\u04bf\3\2\2\2\u04bf"
          + "\u04d5\5p9\2\u04c0\u04c2\7\25\2\2\u04c1\u04c3\7\177\2\2\u04c2\u04c1\3"
          + "\2\2\2\u04c2\u04c3\3\2\2\2\u04c3\u04c4\3\2\2\2\u04c4\u04d5\5p9\2\u04c5"
          + "\u04c7\7\26\2\2\u04c6\u04c8\7\177\2\2\u04c7\u04c6\3\2\2\2\u04c7\u04c8"
          + "\3\2\2\2\u04c8\u04c9\3\2\2\2\u04c9\u04d5\5p9\2\u04ca\u04cc\7\27\2\2\u04cb"
          + "\u04cd\7\177\2\2\u04cc\u04cb\3\2\2\2\u04cc\u04cd\3\2\2\2\u04cd\u04ce\3"
          + "\2\2\2\u04ce\u04d5\5p9\2\u04cf\u04d1\7\30\2\2\u04d0\u04d2\7\177\2\2\u04d1"
          + "\u04d0\3\2\2\2\u04d1\u04d2\3\2\2\2\u04d2\u04d3\3\2\2\2\u04d3\u04d5\5p"
          + "9\2\u04d4\u04b6\3\2\2\2\u04d4\u04bb\3\2\2\2\u04d4\u04c0\3\2\2\2\u04d4"
          + "\u04c5\3\2\2\2\u04d4\u04ca\3\2\2\2\u04d4\u04cf\3\2\2\2\u04d5\u008b\3\2"
          + "\2\2\u04d6\u04d8\7\b\2\2\u04d7\u04d9\7\177\2\2\u04d8\u04d7\3\2\2\2\u04d8"
          + "\u04d9\3\2\2\2\u04d9\u04da\3\2\2\2\u04da\u04dc\5d\63\2\u04db\u04dd\7\177"
          + "\2\2\u04dc\u04db\3\2\2\2\u04dc\u04dd\3\2\2\2\u04dd\u04de\3\2\2\2\u04de"
          + "\u04df\7\t\2\2\u04df\u008d\3\2\2\2\u04e0\u04e5\5N(\2\u04e1\u04e3\7\177"
          + "\2\2\u04e2\u04e1\3\2\2\2\u04e2\u04e3\3\2\2\2\u04e3\u04e4\3\2\2\2\u04e4"
          + "\u04e6\5P)\2\u04e5\u04e2\3\2\2\2\u04e6\u04e7\3\2\2\2\u04e7\u04e5\3\2\2"
          + "\2\u04e7\u04e8\3\2\2\2\u04e8\u008f\3\2\2\2\u04e9\u04ee\5\u0092J\2\u04ea"
          + "\u04ec\7\177\2\2\u04eb\u04ea\3\2\2\2\u04eb\u04ec\3\2\2\2\u04ec\u04ed\3"
          + "\2\2\2\u04ed\u04ef\5D#\2\u04ee\u04eb\3\2\2\2\u04ee\u04ef\3\2\2\2\u04ef"
          + "\u0091\3\2\2\2\u04f0\u04f1\5\u00aeX\2\u04f1\u04f2\7\177\2\2\u04f2\u04f3"
          + "\7O\2\2\u04f3\u04f4\7\177\2\2\u04f4\u04f5\5d\63\2\u04f5\u0093\3\2\2\2"
          + "\u04f6\u04f8\5\u0096L\2\u04f7\u04f9\7\177\2\2\u04f8\u04f7\3\2\2\2\u04f8"
          + "\u04f9\3\2\2\2\u04f9\u04fa\3\2\2\2\u04fa\u04fc\7\b\2\2\u04fb\u04fd\7\177"
          + "\2\2\u04fc\u04fb\3\2\2\2\u04fc\u04fd\3\2\2\2\u04fd\u0502\3\2\2\2\u04fe"
          + "\u0500\7A\2\2\u04ff\u0501\7\177\2\2\u0500\u04ff\3\2\2\2\u0500\u0501\3"
          + "\2\2\2\u0501\u0503\3\2\2\2\u0502\u04fe\3\2\2\2\u0502\u0503\3\2\2\2\u0503"
          + "\u0515\3\2\2\2\u0504\u0506\5d\63\2\u0505\u0507\7\177\2\2\u0506\u0505\3"
          + "\2\2\2\u0506\u0507\3\2\2\2\u0507\u0512\3\2\2\2\u0508\u050a\7\4\2\2\u0509"
          + "\u050b\7\177\2\2\u050a\u0509\3\2\2\2\u050a\u050b\3\2\2\2\u050b\u050c\3"
          + "\2\2\2\u050c\u050e\5d\63\2\u050d\u050f\7\177\2\2\u050e\u050d\3\2\2\2\u050e"
          + "\u050f\3\2\2\2\u050f\u0511\3\2\2\2\u0510\u0508\3\2\2\2\u0511\u0514\3\2"
          + "\2\2\u0512\u0510\3\2\2\2\u0512\u0513\3\2\2\2\u0513\u0516\3\2\2\2\u0514"
          + "\u0512\3\2\2\2\u0515\u0504\3\2\2\2\u0515\u0516\3\2\2\2\u0516\u0517\3\2"
          + "\2\2\u0517\u0518\7\t\2\2\u0518\u0095\3\2\2\2\u0519\u051a\5\u00a2R\2\u051a"
          + "\u051b\5\u00c2b\2\u051b\u0097\3\2\2\2\u051c\u051e\7[\2\2\u051d\u051f\7"
          + "\177\2\2\u051e\u051d\3\2\2\2\u051e\u051f\3\2\2\2\u051f\u0520\3\2\2\2\u0520"
          + "\u0522\7\31\2\2\u0521\u0523\7\177\2\2\u0522\u0521\3\2\2\2\u0522\u0523"
          + "\3\2\2\2\u0523\u052c\3\2\2\2\u0524\u052d\5\b\5\2\u0525\u052a\5F$\2\u0526"
          + "\u0528\7\177\2\2\u0527\u0526\3\2\2\2\u0527\u0528\3\2\2\2\u0528\u0529\3"
          + "\2\2\2\u0529\u052b\5D#\2\u052a\u0527\3\2\2\2\u052a\u052b\3\2\2\2\u052b"
          + "\u052d\3\2\2\2\u052c\u0524\3\2\2\2\u052c\u0525\3\2\2\2\u052d\u052f\3\2"
          + "\2\2\u052e\u0530\7\177\2\2\u052f\u052e\3\2\2\2\u052f\u0530\3\2\2\2\u0530"
          + "\u0531\3\2\2\2\u0531\u0532\7\32\2\2\u0532\u0099\3\2\2\2\u0533\u0535\5"
          + "\u00a0Q\2\u0534\u0536\7\177\2\2\u0535\u0534\3\2\2\2\u0535\u0536\3\2\2"
          + "\2\u0536\u0537\3\2\2\2\u0537\u0539\7\b\2\2\u0538\u053a\7\177\2\2\u0539"
          + "\u0538\3\2\2\2\u0539\u053a\3\2\2\2\u053a\u054c\3\2\2\2\u053b\u053d\5d"
          + "\63\2\u053c\u053e\7\177\2\2\u053d\u053c\3\2\2\2\u053d\u053e\3\2\2\2\u053e"
          + "\u0549\3\2\2\2\u053f\u0541\7\4\2\2\u0540\u0542\7\177\2\2\u0541\u0540\3"
          + "\2\2\2\u0541\u0542\3\2\2\2\u0542\u0543\3\2\2\2\u0543\u0545\5d\63\2\u0544"
          + "\u0546\7\177\2\2\u0545\u0544\3\2\2\2\u0545\u0546\3\2\2\2\u0546\u0548\3"
          + "\2\2\2\u0547\u053f\3\2\2\2\u0548\u054b\3\2\2\2\u0549\u0547\3\2\2\2\u0549"
          + "\u054a\3\2\2\2\u054a\u054d\3\2\2\2\u054b\u0549\3\2\2\2\u054c\u053b\3\2"
          + "\2\2\u054c\u054d\3\2\2\2\u054d\u054e\3\2\2\2\u054e\u054f\7\t\2\2\u054f"
          + "\u009b\3\2\2\2\u0550\u0551\5\u00a0Q\2\u0551\u009d\3\2\2\2\u0552\u0553"
          + "\5\u00c2b\2\u0553\u009f\3\2\2\2\u0554\u0555\5\u00a2R\2\u0555\u0556\5\u00c2"
          + "b\2\u0556\u00a1\3\2\2\2\u0557\u0558\5\u00c2b\2\u0558\u0559\7\33\2\2\u0559"
          + "\u055b\3\2\2\2\u055a\u0557\3\2\2\2\u055b\u055e\3\2\2\2\u055c\u055a\3\2"
          + "\2\2\u055c\u055d\3\2\2\2\u055d\u00a3\3\2\2\2\u055e\u055c\3\2\2\2\u055f"
          + "\u0561\7\n\2\2\u0560\u0562\7\177\2\2\u0561\u0560\3\2\2\2\u0561\u0562\3"
          + "\2\2\2\u0562\u0563\3\2\2\2\u0563\u056c\5\u0090I\2\u0564\u0566\7\177\2"
          + "\2\u0565\u0564\3\2\2\2\u0565\u0566\3\2\2\2\u0566\u0567\3\2\2\2\u0567\u0569"
          + "\7\r\2\2\u0568\u056a\7\177\2\2\u0569\u0568\3\2\2\2\u0569\u056a\3\2\2\2"
          + "\u056a\u056b\3\2\2\2\u056b\u056d\5d\63\2\u056c\u0565\3\2\2\2\u056c\u056d"
          + "\3\2\2\2\u056d\u056f\3\2\2\2\u056e\u0570\7\177\2\2\u056f\u056e\3\2\2\2"
          + "\u056f\u0570\3\2\2\2\u0570\u0571\3\2\2\2\u0571\u0572\7\13\2\2\u0572\u00a5"
          + "\3\2\2\2\u0573\u0575\7\n\2\2\u0574\u0576\7\177\2\2\u0575\u0574\3\2\2\2"
          + "\u0575\u0576\3\2\2\2\u0576\u057f\3\2\2\2\u0577\u0579\5\u00aeX\2\u0578"
          + "\u057a\7\177\2\2\u0579\u0578\3\2\2\2\u0579\u057a\3\2\2\2\u057a\u057b\3"
          + "\2\2\2\u057b\u057d\7\5\2\2\u057c\u057e\7\177\2\2\u057d\u057c\3\2\2\2\u057d"
          + "\u057e\3\2\2\2\u057e\u0580\3\2\2\2\u057f\u0577\3\2\2\2\u057f\u0580\3\2"
          + "\2\2\u0580\u0581\3\2\2\2\u0581\u0583\5\u008eH\2\u0582\u0584\7\177\2\2"
          + "\u0583\u0582\3\2\2\2\u0583\u0584\3\2\2\2\u0584\u0589\3\2\2\2\u0585\u0587"
          + "\5D#\2\u0586\u0588\7\177\2\2\u0587\u0586\3\2\2\2\u0587\u0588\3\2\2\2\u0588"
          + "\u058a\3\2\2\2\u0589\u0585\3\2\2\2\u0589\u058a\3\2\2\2\u058a\u058b\3\2"
          + "\2\2\u058b\u058d\7\r\2\2\u058c\u058e\7\177\2\2\u058d\u058c\3\2\2\2\u058d"
          + "\u058e\3\2\2\2\u058e\u058f\3\2\2\2\u058f\u0591\5d\63\2\u0590\u0592\7\177"
          + "\2\2\u0591\u0590\3\2\2\2\u0591\u0592\3\2\2\2\u0592\u0593\3\2\2\2\u0593"
          + "\u0594\7\13\2\2\u0594\u00a7\3\2\2\2\u0595\u0597\7\33\2\2\u0596\u0598\7"
          + "\177\2\2\u0597\u0596\3\2\2\2\u0597\u0598\3\2\2\2\u0598\u0599\3\2\2\2\u0599"
          + "\u059a\5\u00b8]\2\u059a\u00a9\3\2\2\2\u059b\u05a0\7\\\2\2\u059c\u059e"
          + "\7\177\2\2\u059d\u059c\3\2\2\2\u059d\u059e\3\2\2\2\u059e\u059f\3\2\2\2"
          + "\u059f\u05a1\5\u00acW\2\u05a0\u059d\3\2\2\2\u05a1\u05a2\3\2\2\2\u05a2"
          + "\u05a0\3\2\2\2\u05a2\u05a3\3\2\2\2\u05a3\u05b2\3\2\2\2\u05a4\u05a6\7\\"
          + "\2\2\u05a5\u05a7\7\177\2\2\u05a6\u05a5\3\2\2\2\u05a6\u05a7\3\2\2\2\u05a7"
          + "\u05a8\3\2\2\2\u05a8\u05ad\5d\63\2\u05a9\u05ab\7\177\2\2\u05aa\u05a9\3"
          + "\2\2\2\u05aa\u05ab\3\2\2\2\u05ab\u05ac\3\2\2\2\u05ac\u05ae\5\u00acW\2"
          + "\u05ad\u05aa\3\2\2\2\u05ae\u05af\3\2\2\2\u05af\u05ad\3\2\2\2\u05af\u05b0"
          + "\3\2\2\2\u05b0\u05b2\3\2\2\2\u05b1\u059b\3\2\2\2\u05b1\u05a4\3\2\2\2\u05b2"
          + "\u05bb\3\2\2\2\u05b3\u05b5\7\177\2\2\u05b4\u05b3\3\2\2\2\u05b4\u05b5\3"
          + "\2\2\2\u05b5\u05b6\3\2\2\2\u05b6\u05b8\7]\2\2\u05b7\u05b9\7\177\2\2\u05b8"
          + "\u05b7\3\2\2\2\u05b8\u05b9\3\2\2\2\u05b9\u05ba\3\2\2\2\u05ba\u05bc\5d"
          + "\63\2\u05bb\u05b4\3\2\2\2\u05bb\u05bc\3\2\2\2\u05bc\u05be\3\2\2\2\u05bd"
          + "\u05bf\7\177\2\2\u05be\u05bd\3\2\2\2\u05be\u05bf\3\2\2\2\u05bf\u05c0\3"
          + "\2\2\2\u05c0\u05c1\7^\2\2\u05c1\u00ab\3\2\2\2\u05c2\u05c4\7_\2\2\u05c3"
          + "\u05c5\7\177\2\2\u05c4\u05c3\3\2\2\2\u05c4\u05c5\3\2\2\2\u05c5\u05c6\3"
          + "\2\2\2\u05c6\u05c8\5d\63\2\u05c7\u05c9\7\177\2\2\u05c8\u05c7\3\2\2\2\u05c8"
          + "\u05c9\3\2\2\2\u05c9\u05ca\3\2\2\2\u05ca\u05cc\7`\2\2\u05cb\u05cd\7\177"
          + "\2\2\u05cc\u05cb\3\2\2\2\u05cc\u05cd\3\2\2\2\u05cd\u05ce\3\2\2\2\u05ce"
          + "\u05cf\5d\63\2\u05cf\u00ad\3\2\2\2\u05d0\u05d1\5\u00c2b\2\u05d1\u00af"
          + "\3\2\2\2\u05d2\u05d5\5\u00bc_\2\u05d3\u05d5\5\u00ba^\2\u05d4\u05d2\3\2"
          + "\2\2\u05d4\u05d3\3\2\2\2\u05d5\u00b1\3\2\2\2\u05d6\u05d8\7\31\2\2\u05d7"
          + "\u05d9\7\177\2\2\u05d8\u05d7\3\2\2\2\u05d8\u05d9\3\2\2\2\u05d9\u05fb\3"
          + "\2\2\2\u05da\u05dc\5\u00b8]\2\u05db\u05dd\7\177\2\2\u05dc\u05db\3\2\2"
          + "\2\u05dc\u05dd\3\2\2\2\u05dd\u05de\3\2\2\2\u05de\u05e0\7\f\2\2\u05df\u05e1"
          + "\7\177\2\2\u05e0\u05df\3\2\2\2\u05e0\u05e1\3\2\2\2\u05e1\u05e2\3\2\2\2"
          + "\u05e2\u05e4\5d\63\2\u05e3\u05e5\7\177\2\2\u05e4\u05e3\3\2\2\2\u05e4\u05e5"
          + "\3\2\2\2\u05e5\u05f8\3\2\2\2\u05e6\u05e8\7\4\2\2\u05e7\u05e9\7\177\2\2"
          + "\u05e8\u05e7\3\2\2\2\u05e8\u05e9\3\2\2\2\u05e9\u05ea\3\2\2\2\u05ea\u05ec"
          + "\5\u00b8]\2\u05eb\u05ed\7\177\2\2\u05ec\u05eb\3\2\2\2\u05ec\u05ed\3\2"
          + "\2\2\u05ed\u05ee\3\2\2\2\u05ee\u05f0\7\f\2\2\u05ef\u05f1\7\177\2\2\u05f0"
          + "\u05ef\3\2\2\2\u05f0\u05f1\3\2\2\2\u05f1\u05f2\3\2\2\2\u05f2\u05f4\5d"
          + "\63\2\u05f3\u05f5\7\177\2\2\u05f4\u05f3\3\2\2\2\u05f4\u05f5\3\2\2\2\u05f5"
          + "\u05f7\3\2\2\2\u05f6\u05e6\3\2\2\2\u05f7\u05fa\3\2\2\2\u05f8\u05f6\3\2"
          + "\2\2\u05f8\u05f9\3\2\2\2\u05f9\u05fc\3\2\2\2\u05fa\u05f8\3\2\2\2\u05fb"
          + "\u05da\3\2\2\2\u05fb\u05fc\3\2\2\2\u05fc\u05fd\3\2\2\2\u05fd\u05fe\7\32"
          + "\2\2\u05fe\u00b3\3\2\2\2\u05ff\u0602\7\34\2\2\u0600\u0603\5\u00c2b\2\u0601"
          + "\u0603\7d\2\2\u0602\u0600\3\2\2\2\u0602\u0601\3\2\2\2\u0603\u00b5\3\2"
          + "\2\2\u0604\u0609\5\u0082B\2\u0605\u0607\7\177\2\2\u0606\u0605\3\2\2\2"
          + "\u0606\u0607\3\2\2\2\u0607\u0608\3\2\2\2\u0608\u060a\5\u00a8U\2\u0609"
          + "\u0606\3\2\2\2\u060a\u060b\3\2\2\2\u060b\u0609\3\2\2\2\u060b\u060c\3\2"
          + "\2\2\u060c\u00b7\3\2\2\2\u060d\u060e\5\u00be`\2\u060e\u00b9\3\2\2\2\u060f"
          + "\u0610\t\5\2\2\u0610\u00bb\3\2\2\2\u0611\u0612\t\6\2\2\u0612\u00bd\3\2"
          + "\2\2\u0613\u0616\5\u00c2b\2\u0614\u0616\5\u00c0a\2\u0615\u0613\3\2\2\2"
          + "\u0615\u0614\3\2\2\2\u0616\u00bf\3\2\2\2\u0617\u0618\t\7\2\2\u0618\u00c1"
          + "\3\2\2\2\u0619\u061a\t\b\2\2\u061a\u00c3\3\2\2\2\u061b\u061c\t\t\2\2\u061c"
          + "\u00c5\3\2\2\2\u061d\u061e\t\n\2\2\u061e\u00c7\3\2\2\2\u061f\u0620\t\13"
          + "\2\2\u0620\u00c9\3\2\2\2\u0124\u00cb\u00cf\u00d2\u00d5\u00dd\u00e1\u00e6"
          + "\u00ed\u00f2\u00f5\u00f9\u00fd\u0101\u0107\u010b\u0110\u0115\u0119\u011c"
          + "\u011e\u0122\u0126\u012b\u012f\u0134\u0138\u0141\u0146\u014a\u014e\u0152"
          + "\u0155\u0159\u0163\u016a\u0177\u017b\u0181\u0185\u0189\u018e\u0193\u0197"
          + "\u019d\u01a1\u01a7\u01ab\u01b1\u01b5\u01b9\u01bd\u01c1\u01c5\u01ca\u01d1"
          + "\u01d5\u01da\u01e1\u01e7\u01ec\u01f2\u01f5\u01fb\u01fd\u0201\u0205\u020a"
          + "\u020e\u0211\u0218\u021f\u0222\u0228\u022b\u0231\u0235\u0239\u023d\u0241"
          + "\u0246\u024b\u024f\u0254\u0257\u0260\u0269\u026e\u027b\u027e\u0286\u028a"
          + "\u028f\u0294\u0298\u029d\u02a3\u02a8\u02af\u02b3\u02b7\u02b9\u02bd\u02bf"
          + "\u02c3\u02c5\u02cb\u02d1\u02d5\u02d8\u02db\u02df\u02e5\u02e9\u02ec\u02ef"
          + "\u02f5\u02f8\u02fb\u02ff\u0305\u0308\u030b\u030f\u0313\u0317\u0319\u031d"
          + "\u031f\u0322\u0326\u0328\u032e\u0332\u0336\u033a\u033d\u0342\u0347\u034c"
          + "\u0351\u0357\u035b\u035d\u0361\u0365\u0367\u0369\u0378\u0382\u038c\u0391"
          + "\u0395\u039c\u03a1\u03a6\u03aa\u03ae\u03b2\u03b5\u03b7\u03bc\u03c0\u03c4"
          + "\u03c8\u03cc\u03d0\u03d3\u03d5\u03da\u03de\u03e3\u03e8\u03ec\u03f5\u03f7"
          + "\u03fd\u0401\u0408\u040c\u0410\u0413\u041f\u0422\u0430\u0434\u0439\u043d"
          + "\u0440\u0447\u044b\u044f\u0456\u045a\u045e\u0464\u0468\u046c\u0472\u0476"
          + "\u047a\u0480\u0484\u0488\u0491\u0499\u049f\u04a3\u04a7\u04ab\u04af\u04b2"
          + "\u04b8\u04bd\u04c2\u04c7\u04cc\u04d1\u04d4\u04d8\u04dc\u04e2\u04e7\u04eb"
          + "\u04ee\u04f8\u04fc\u0500\u0502\u0506\u050a\u050e\u0512\u0515\u051e\u0522"
          + "\u0527\u052a\u052c\u052f\u0535\u0539\u053d\u0541\u0545\u0549\u054c\u055c"
          + "\u0561\u0565\u0569\u056c\u056f\u0575\u0579\u057d\u057f\u0583\u0587\u0589"
          + "\u058d\u0591\u0597\u059d\u05a2\u05a6\u05aa\u05af\u05b1\u05b4\u05b8\u05bb"
          + "\u05be\u05c4\u05c8\u05cc\u05d4\u05d8\u05dc\u05e0\u05e4\u05e8\u05ec\u05f0"
          + "\u05f4\u05f8\u05fb\u0602\u0606\u060b\u0615";
  public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());
  static {
    _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
      _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
    }
  }
}
