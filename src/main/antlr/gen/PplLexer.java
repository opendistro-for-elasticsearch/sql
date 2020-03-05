// Generated from /Users/penghuo/opendistro/poc-ppl/src/main/antlr/PPLLexer.g4 by ANTLR 4.8
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PPLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEARCH=1, TOP=2, FROM=3, WHERE=4, LIMIT=5, BY=6, COUNTFIELD=7, SOURCE=8, 
		INDEX=9, FIELDS=10, CASE=11, IN=12, ABS=13, POW=14, AVG=15, COUNT=16, 
		NOT=17, OR=18, AND=19, TRUE=20, FALSE=21, DATAMODEL=22, LOOKUP=23, SAVEDSEARCH=24, 
		PIPE=25, COMMA=26, DOT=27, EQUAL_SYMBOL=28, GREATER_SYMBOL=29, LESS_SYMBOL=30, 
		NOT_GREATER=31, NOT_LESS=32, NOT_EQUAL=33, PLUS=34, MINUS=35, STAR=36, 
		DIVIDE=37, MODULE=38, EXCLAMATION_SYMBOL=39, COLON=40, LT_PRTHS=41, RT_PRTHS=42, 
		STRING_LITERAL=43, ID=44, DOT_ID=45, DECIMAL_LITERAL=46, ERROR_RECOGNITION=47;
	public static final int
		WHITESPACE=2, ERRORCHANNEL=3;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN", "WHITESPACE", "ERRORCHANNEL"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"SEARCH", "TOP", "FROM", "WHERE", "LIMIT", "BY", "COUNTFIELD", "SOURCE", 
			"INDEX", "FIELDS", "CASE", "IN", "ABS", "POW", "AVG", "COUNT", "NOT", 
			"OR", "AND", "TRUE", "FALSE", "DATAMODEL", "LOOKUP", "SAVEDSEARCH", "PIPE", 
			"COMMA", "DOT", "EQUAL_SYMBOL", "GREATER_SYMBOL", "LESS_SYMBOL", "NOT_GREATER", 
			"NOT_LESS", "NOT_EQUAL", "PLUS", "MINUS", "STAR", "DIVIDE", "MODULE", 
			"EXCLAMATION_SYMBOL", "COLON", "LT_PRTHS", "RT_PRTHS", "STRING_LITERAL", 
			"ID", "DOT_ID", "DECIMAL_LITERAL", "ID_LITERAL", "DQUOTA_STRING", "SQUOTA_STRING", 
			"BQUOTA_STRING", "DEC_DIGIT", "ERROR_RECOGNITION"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'SEARCH'", "'TOP'", "'FROM'", "'WHERE'", "'LIMIT'", "'BY'", "'COUNTFIELD'", 
			"'SOURCE'", "'INDEX'", "'FIELDS'", "'CASE'", "'IN'", "'ABS'", "'POW'", 
			"'AVG'", "'COUNT'", "'NOT'", "'OR'", "'AND'", "'TRUE'", "'FALSE'", "'DATAMODEL'", 
			"'LOOKUP'", "'SAVEDSEARCH'", "'|'", "','", "'.'", "'='", "'>'", "'<'", 
			null, null, null, "'+'", "'-'", "'*'", "'/'", "'%'", "'!'", "':'", "'('", 
			"')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEARCH", "TOP", "FROM", "WHERE", "LIMIT", "BY", "COUNTFIELD", 
			"SOURCE", "INDEX", "FIELDS", "CASE", "IN", "ABS", "POW", "AVG", "COUNT", 
			"NOT", "OR", "AND", "TRUE", "FALSE", "DATAMODEL", "LOOKUP", "SAVEDSEARCH", 
			"PIPE", "COMMA", "DOT", "EQUAL_SYMBOL", "GREATER_SYMBOL", "LESS_SYMBOL", 
			"NOT_GREATER", "NOT_LESS", "NOT_EQUAL", "PLUS", "MINUS", "STAR", "DIVIDE", 
			"MODULE", "EXCLAMATION_SYMBOL", "COLON", "LT_PRTHS", "RT_PRTHS", "STRING_LITERAL", 
			"ID", "DOT_ID", "DECIMAL_LITERAL", "ERROR_RECOGNITION"
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


	public PPLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "PPLLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\61\u016b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3"+
		"\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f"+
		"\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\23"+
		"\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36"+
		"\3\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3"+
		"\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3,\3,\5,\u0122\n,\3-\3-\3.\3.\3.\3/\6"+
		"/\u012a\n/\r/\16/\u012b\3\60\7\60\u012f\n\60\f\60\16\60\u0132\13\60\3"+
		"\60\6\60\u0135\n\60\r\60\16\60\u0136\3\60\7\60\u013a\n\60\f\60\16\60\u013d"+
		"\13\60\3\61\3\61\3\61\3\61\3\61\3\61\7\61\u0145\n\61\f\61\16\61\u0148"+
		"\13\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\7\62\u0152\n\62\f\62\16"+
		"\62\u0155\13\62\3\62\3\62\3\63\3\63\3\63\3\63\3\63\3\63\7\63\u015f\n\63"+
		"\f\63\16\63\u0162\13\63\3\63\3\63\3\64\3\64\3\65\3\65\3\65\3\65\4\u0130"+
		"\u0136\2\66\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33"+
		"\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67"+
		"\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\2a\2c\2e\2g\2i\61\3"+
		"\2\t\6\2&&\62;B\\aa\6\2&&//C\\aa\7\2&&//\62;C\\aa\4\2$$^^\4\2))^^\4\2"+
		"^^bb\3\2\62;\2\u0175\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2"+
		"\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3"+
		"\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2"+
		"\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2"+
		"\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2"+
		"\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2"+
		"\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q"+
		"\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2"+
		"\2\2\2i\3\2\2\2\3k\3\2\2\2\5r\3\2\2\2\7v\3\2\2\2\t{\3\2\2\2\13\u0081\3"+
		"\2\2\2\r\u0087\3\2\2\2\17\u008a\3\2\2\2\21\u0095\3\2\2\2\23\u009c\3\2"+
		"\2\2\25\u00a2\3\2\2\2\27\u00a9\3\2\2\2\31\u00ae\3\2\2\2\33\u00b1\3\2\2"+
		"\2\35\u00b5\3\2\2\2\37\u00b9\3\2\2\2!\u00bd\3\2\2\2#\u00c3\3\2\2\2%\u00c7"+
		"\3\2\2\2\'\u00ca\3\2\2\2)\u00ce\3\2\2\2+\u00d3\3\2\2\2-\u00d9\3\2\2\2"+
		"/\u00e3\3\2\2\2\61\u00ea\3\2\2\2\63\u00f6\3\2\2\2\65\u00f8\3\2\2\2\67"+
		"\u00fa\3\2\2\29\u00fc\3\2\2\2;\u00fe\3\2\2\2=\u0100\3\2\2\2?\u0102\3\2"+
		"\2\2A\u0105\3\2\2\2C\u0108\3\2\2\2E\u010b\3\2\2\2G\u010d\3\2\2\2I\u010f"+
		"\3\2\2\2K\u0111\3\2\2\2M\u0113\3\2\2\2O\u0115\3\2\2\2Q\u0117\3\2\2\2S"+
		"\u0119\3\2\2\2U\u011b\3\2\2\2W\u0121\3\2\2\2Y\u0123\3\2\2\2[\u0125\3\2"+
		"\2\2]\u0129\3\2\2\2_\u0130\3\2\2\2a\u013e\3\2\2\2c\u014b\3\2\2\2e\u0158"+
		"\3\2\2\2g\u0165\3\2\2\2i\u0167\3\2\2\2kl\7U\2\2lm\7G\2\2mn\7C\2\2no\7"+
		"T\2\2op\7E\2\2pq\7J\2\2q\4\3\2\2\2rs\7V\2\2st\7Q\2\2tu\7R\2\2u\6\3\2\2"+
		"\2vw\7H\2\2wx\7T\2\2xy\7Q\2\2yz\7O\2\2z\b\3\2\2\2{|\7Y\2\2|}\7J\2\2}~"+
		"\7G\2\2~\177\7T\2\2\177\u0080\7G\2\2\u0080\n\3\2\2\2\u0081\u0082\7N\2"+
		"\2\u0082\u0083\7K\2\2\u0083\u0084\7O\2\2\u0084\u0085\7K\2\2\u0085\u0086"+
		"\7V\2\2\u0086\f\3\2\2\2\u0087\u0088\7D\2\2\u0088\u0089\7[\2\2\u0089\16"+
		"\3\2\2\2\u008a\u008b\7E\2\2\u008b\u008c\7Q\2\2\u008c\u008d\7W\2\2\u008d"+
		"\u008e\7P\2\2\u008e\u008f\7V\2\2\u008f\u0090\7H\2\2\u0090\u0091\7K\2\2"+
		"\u0091\u0092\7G\2\2\u0092\u0093\7N\2\2\u0093\u0094\7F\2\2\u0094\20\3\2"+
		"\2\2\u0095\u0096\7U\2\2\u0096\u0097\7Q\2\2\u0097\u0098\7W\2\2\u0098\u0099"+
		"\7T\2\2\u0099\u009a\7E\2\2\u009a\u009b\7G\2\2\u009b\22\3\2\2\2\u009c\u009d"+
		"\7K\2\2\u009d\u009e\7P\2\2\u009e\u009f\7F\2\2\u009f\u00a0\7G\2\2\u00a0"+
		"\u00a1\7Z\2\2\u00a1\24\3\2\2\2\u00a2\u00a3\7H\2\2\u00a3\u00a4\7K\2\2\u00a4"+
		"\u00a5\7G\2\2\u00a5\u00a6\7N\2\2\u00a6\u00a7\7F\2\2\u00a7\u00a8\7U\2\2"+
		"\u00a8\26\3\2\2\2\u00a9\u00aa\7E\2\2\u00aa\u00ab\7C\2\2\u00ab\u00ac\7"+
		"U\2\2\u00ac\u00ad\7G\2\2\u00ad\30\3\2\2\2\u00ae\u00af\7K\2\2\u00af\u00b0"+
		"\7P\2\2\u00b0\32\3\2\2\2\u00b1\u00b2\7C\2\2\u00b2\u00b3\7D\2\2\u00b3\u00b4"+
		"\7U\2\2\u00b4\34\3\2\2\2\u00b5\u00b6\7R\2\2\u00b6\u00b7\7Q\2\2\u00b7\u00b8"+
		"\7Y\2\2\u00b8\36\3\2\2\2\u00b9\u00ba\7C\2\2\u00ba\u00bb\7X\2\2\u00bb\u00bc"+
		"\7I\2\2\u00bc \3\2\2\2\u00bd\u00be\7E\2\2\u00be\u00bf\7Q\2\2\u00bf\u00c0"+
		"\7W\2\2\u00c0\u00c1\7P\2\2\u00c1\u00c2\7V\2\2\u00c2\"\3\2\2\2\u00c3\u00c4"+
		"\7P\2\2\u00c4\u00c5\7Q\2\2\u00c5\u00c6\7V\2\2\u00c6$\3\2\2\2\u00c7\u00c8"+
		"\7Q\2\2\u00c8\u00c9\7T\2\2\u00c9&\3\2\2\2\u00ca\u00cb\7C\2\2\u00cb\u00cc"+
		"\7P\2\2\u00cc\u00cd\7F\2\2\u00cd(\3\2\2\2\u00ce\u00cf\7V\2\2\u00cf\u00d0"+
		"\7T\2\2\u00d0\u00d1\7W\2\2\u00d1\u00d2\7G\2\2\u00d2*\3\2\2\2\u00d3\u00d4"+
		"\7H\2\2\u00d4\u00d5\7C\2\2\u00d5\u00d6\7N\2\2\u00d6\u00d7\7U\2\2\u00d7"+
		"\u00d8\7G\2\2\u00d8,\3\2\2\2\u00d9\u00da\7F\2\2\u00da\u00db\7C\2\2\u00db"+
		"\u00dc\7V\2\2\u00dc\u00dd\7C\2\2\u00dd\u00de\7O\2\2\u00de\u00df\7Q\2\2"+
		"\u00df\u00e0\7F\2\2\u00e0\u00e1\7G\2\2\u00e1\u00e2\7N\2\2\u00e2.\3\2\2"+
		"\2\u00e3\u00e4\7N\2\2\u00e4\u00e5\7Q\2\2\u00e5\u00e6\7Q\2\2\u00e6\u00e7"+
		"\7M\2\2\u00e7\u00e8\7W\2\2\u00e8\u00e9\7R\2\2\u00e9\60\3\2\2\2\u00ea\u00eb"+
		"\7U\2\2\u00eb\u00ec\7C\2\2\u00ec\u00ed\7X\2\2\u00ed\u00ee\7G\2\2\u00ee"+
		"\u00ef\7F\2\2\u00ef\u00f0\7U\2\2\u00f0\u00f1\7G\2\2\u00f1\u00f2\7C\2\2"+
		"\u00f2\u00f3\7T\2\2\u00f3\u00f4\7E\2\2\u00f4\u00f5\7J\2\2\u00f5\62\3\2"+
		"\2\2\u00f6\u00f7\7~\2\2\u00f7\64\3\2\2\2\u00f8\u00f9\7.\2\2\u00f9\66\3"+
		"\2\2\2\u00fa\u00fb\7\60\2\2\u00fb8\3\2\2\2\u00fc\u00fd\7?\2\2\u00fd:\3"+
		"\2\2\2\u00fe\u00ff\7@\2\2\u00ff<\3\2\2\2\u0100\u0101\7>\2\2\u0101>\3\2"+
		"\2\2\u0102\u0103\7>\2\2\u0103\u0104\7?\2\2\u0104@\3\2\2\2\u0105\u0106"+
		"\7@\2\2\u0106\u0107\7?\2\2\u0107B\3\2\2\2\u0108\u0109\7#\2\2\u0109\u010a"+
		"\7?\2\2\u010aD\3\2\2\2\u010b\u010c\7-\2\2\u010cF\3\2\2\2\u010d\u010e\7"+
		"/\2\2\u010eH\3\2\2\2\u010f\u0110\7,\2\2\u0110J\3\2\2\2\u0111\u0112\7\61"+
		"\2\2\u0112L\3\2\2\2\u0113\u0114\7\'\2\2\u0114N\3\2\2\2\u0115\u0116\7#"+
		"\2\2\u0116P\3\2\2\2\u0117\u0118\7<\2\2\u0118R\3\2\2\2\u0119\u011a\7*\2"+
		"\2\u011aT\3\2\2\2\u011b\u011c\7+\2\2\u011cV\3\2\2\2\u011d\u0122\5_\60"+
		"\2\u011e\u0122\5a\61\2\u011f\u0122\5c\62\2\u0120\u0122\5e\63\2\u0121\u011d"+
		"\3\2\2\2\u0121\u011e\3\2\2\2\u0121\u011f\3\2\2\2\u0121\u0120\3\2\2\2\u0122"+
		"X\3\2\2\2\u0123\u0124\5_\60\2\u0124Z\3\2\2\2\u0125\u0126\7\60\2\2\u0126"+
		"\u0127\5Y-\2\u0127\\\3\2\2\2\u0128\u012a\5g\64\2\u0129\u0128\3\2\2\2\u012a"+
		"\u012b\3\2\2\2\u012b\u0129\3\2\2\2\u012b\u012c\3\2\2\2\u012c^\3\2\2\2"+
		"\u012d\u012f\t\2\2\2\u012e\u012d\3\2\2\2\u012f\u0132\3\2\2\2\u0130\u0131"+
		"\3\2\2\2\u0130\u012e\3\2\2\2\u0131\u0134\3\2\2\2\u0132\u0130\3\2\2\2\u0133"+
		"\u0135\t\3\2\2\u0134\u0133\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u0137\3\2"+
		"\2\2\u0136\u0134\3\2\2\2\u0137\u013b\3\2\2\2\u0138\u013a\t\4\2\2\u0139"+
		"\u0138\3\2\2\2\u013a\u013d\3\2\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2"+
		"\2\2\u013c`\3\2\2\2\u013d\u013b\3\2\2\2\u013e\u0146\7$\2\2\u013f\u0140"+
		"\7^\2\2\u0140\u0145\13\2\2\2\u0141\u0142\7$\2\2\u0142\u0145\7$\2\2\u0143"+
		"\u0145\n\5\2\2\u0144\u013f\3\2\2\2\u0144\u0141\3\2\2\2\u0144\u0143\3\2"+
		"\2\2\u0145\u0148\3\2\2\2\u0146\u0144\3\2\2\2\u0146\u0147\3\2\2\2\u0147"+
		"\u0149\3\2\2\2\u0148\u0146\3\2\2\2\u0149\u014a\7$\2\2\u014ab\3\2\2\2\u014b"+
		"\u0153\7)\2\2\u014c\u014d\7^\2\2\u014d\u0152\13\2\2\2\u014e\u014f\7)\2"+
		"\2\u014f\u0152\7)\2\2\u0150\u0152\n\6\2\2\u0151\u014c\3\2\2\2\u0151\u014e"+
		"\3\2\2\2\u0151\u0150\3\2\2\2\u0152\u0155\3\2\2\2\u0153\u0151\3\2\2\2\u0153"+
		"\u0154\3\2\2\2\u0154\u0156\3\2\2\2\u0155\u0153\3\2\2\2\u0156\u0157\7)"+
		"\2\2\u0157d\3\2\2\2\u0158\u0160\7b\2\2\u0159\u015a\7^\2\2\u015a\u015f"+
		"\13\2\2\2\u015b\u015c\7b\2\2\u015c\u015f\7b\2\2\u015d\u015f\n\7\2\2\u015e"+
		"\u0159\3\2\2\2\u015e\u015b\3\2\2\2\u015e\u015d\3\2\2\2\u015f\u0162\3\2"+
		"\2\2\u0160\u015e\3\2\2\2\u0160\u0161\3\2\2\2\u0161\u0163\3\2\2\2\u0162"+
		"\u0160\3\2\2\2\u0163\u0164\7b\2\2\u0164f\3\2\2\2\u0165\u0166\t\b\2\2\u0166"+
		"h\3\2\2\2\u0167\u0168\13\2\2\2\u0168\u0169\3\2\2\2\u0169\u016a\b\65\2"+
		"\2\u016aj\3\2\2\2\16\2\u0121\u012b\u0130\u0136\u013b\u0144\u0146\u0151"+
		"\u0153\u015e\u0160\3\2\5\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}