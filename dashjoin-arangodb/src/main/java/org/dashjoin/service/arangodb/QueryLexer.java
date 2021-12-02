// Generated from Query.g4 by ANTLR 4.8
package org.dashjoin.service.arangodb;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, STRING=15, VAR=16, INT=17, 
		WS=18;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "STRING", "VAR", "INT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'FOR'", "'IN'", "'RETURN'", "'DISTINCT'", "'SORT'", "'.'", "'DESC'", 
			"'LIMIT'", "','", "'{'", "'}'", "':'", "'FILTER'", "'=='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, "STRING", "VAR", "INT", "WS"
		};
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


	public QueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Query.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\24~\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3"+
		"\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\7\20e\n\20"+
		"\f\20\16\20h\13\20\3\20\3\20\3\21\3\21\7\21n\n\21\f\21\16\21q\13\21\3"+
		"\22\6\22t\n\22\r\22\16\22u\3\23\6\23y\n\23\r\23\16\23z\3\23\3\23\2\2\24"+
		"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20"+
		"\37\21!\22#\23%\24\3\2\7\3\2$$\5\2C\\aac|\6\2\62;C\\aac|\3\2\62;\5\2\13"+
		"\f\17\17\"\"\2\u0081\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2"+
		"\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3"+
		"\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2"+
		"\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5+\3\2\2\2\7.\3\2\2\2"+
		"\t\65\3\2\2\2\13>\3\2\2\2\rC\3\2\2\2\17E\3\2\2\2\21J\3\2\2\2\23P\3\2\2"+
		"\2\25R\3\2\2\2\27T\3\2\2\2\31V\3\2\2\2\33X\3\2\2\2\35_\3\2\2\2\37b\3\2"+
		"\2\2!k\3\2\2\2#s\3\2\2\2%x\3\2\2\2\'(\7H\2\2()\7Q\2\2)*\7T\2\2*\4\3\2"+
		"\2\2+,\7K\2\2,-\7P\2\2-\6\3\2\2\2./\7T\2\2/\60\7G\2\2\60\61\7V\2\2\61"+
		"\62\7W\2\2\62\63\7T\2\2\63\64\7P\2\2\64\b\3\2\2\2\65\66\7F\2\2\66\67\7"+
		"K\2\2\678\7U\2\289\7V\2\29:\7K\2\2:;\7P\2\2;<\7E\2\2<=\7V\2\2=\n\3\2\2"+
		"\2>?\7U\2\2?@\7Q\2\2@A\7T\2\2AB\7V\2\2B\f\3\2\2\2CD\7\60\2\2D\16\3\2\2"+
		"\2EF\7F\2\2FG\7G\2\2GH\7U\2\2HI\7E\2\2I\20\3\2\2\2JK\7N\2\2KL\7K\2\2L"+
		"M\7O\2\2MN\7K\2\2NO\7V\2\2O\22\3\2\2\2PQ\7.\2\2Q\24\3\2\2\2RS\7}\2\2S"+
		"\26\3\2\2\2TU\7\177\2\2U\30\3\2\2\2VW\7<\2\2W\32\3\2\2\2XY\7H\2\2YZ\7"+
		"K\2\2Z[\7N\2\2[\\\7V\2\2\\]\7G\2\2]^\7T\2\2^\34\3\2\2\2_`\7?\2\2`a\7?"+
		"\2\2a\36\3\2\2\2bf\7$\2\2ce\n\2\2\2dc\3\2\2\2eh\3\2\2\2fd\3\2\2\2fg\3"+
		"\2\2\2gi\3\2\2\2hf\3\2\2\2ij\7$\2\2j \3\2\2\2ko\t\3\2\2ln\t\4\2\2ml\3"+
		"\2\2\2nq\3\2\2\2om\3\2\2\2op\3\2\2\2p\"\3\2\2\2qo\3\2\2\2rt\t\5\2\2sr"+
		"\3\2\2\2tu\3\2\2\2us\3\2\2\2uv\3\2\2\2v$\3\2\2\2wy\t\6\2\2xw\3\2\2\2y"+
		"z\3\2\2\2zx\3\2\2\2z{\3\2\2\2{|\3\2\2\2|}\b\23\2\2}&\3\2\2\2\7\2fouz\3"+
		"\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}