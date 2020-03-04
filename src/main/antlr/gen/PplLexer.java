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
		INDEX=9, CASE=10, IN=11, ABS=12, POW=13, AVG=14, COUNT=15, NOT=16, OR=17, 
		AND=18, TRUE=19, FALSE=20, DATAMODEL=21, LOOKUP=22, SAVEDSEARCH=23, PIPE=24, 
		COMMA=25, DOT=26, EQUAL_SYMBOL=27, GREATER_SYMBOL=28, LESS_SYMBOL=29, 
		NOT_GREATER=30, NOT_LESS=31, NOT_EQUAL=32, PLUS=33, MINUS=34, STAR=35, 
		DIVIDE=36, MODULE=37, EXCLAMATION_SYMBOL=38, COLON=39, LT_PRTHS=40, RT_PRTHS=41, 
		STRING_LITERAL=42, ID=43, DOT_ID=44, DECIMAL_LITERAL=45, ERROR_RECOGNITION=46;
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
			"INDEX", "CASE", "IN", "ABS", "POW", "AVG", "COUNT", "NOT", "OR", "AND", 
			"TRUE", "FALSE", "DATAMODEL", "LOOKUP", "SAVEDSEARCH", "PIPE", "COMMA", 
			"DOT", "EQUAL_SYMBOL", "GREATER_SYMBOL", "LESS_SYMBOL", "NOT_GREATER", 
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
			"'SOURCE'", "'INDEX'", "'CASE'", "'IN'", "'ABS'", "'POW'", "'AVG'", "'COUNT'", 
			"'NOT'", "'OR'", "'AND'", "'TRUE'", "'FALSE'", "'DATAMODEL'", "'LOOKUP'", 
			"'SAVEDSEARCH'", "'|'", "','", "'.'", "'='", "'>'", "'<'", null, null, 
			null, "'+'", "'-'", "'*'", "'/'", "'%'", "'!'", "':'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEARCH", "TOP", "FROM", "WHERE", "LIMIT", "BY", "COUNTFIELD", 
			"SOURCE", "INDEX", "CASE", "IN", "ABS", "POW", "AVG", "COUNT", "NOT", 
			"OR", "AND", "TRUE", "FALSE", "DATAMODEL", "LOOKUP", "SAVEDSEARCH", "PIPE", 
			"COMMA", "DOT", "EQUAL_SYMBOL", "GREATER_SYMBOL", "LESS_SYMBOL", "NOT_GREATER", 
			"NOT_LESS", "NOT_EQUAL", "PLUS", "MINUS", "STAR", "DIVIDE", "MODULE", 
			"EXCLAMATION_SYMBOL", "COLON", "LT_PRTHS", "RT_PRTHS", "STRING_LITERAL", 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\60\u0162\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\r"+
		"\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\3\24"+
		"\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\32\3\32\3\33"+
		"\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3!\3!\3!\3"+
		"\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3+\3+\5"+
		"+\u0119\n+\3,\3,\3-\3-\3-\3.\6.\u0121\n.\r.\16.\u0122\3/\7/\u0126\n/\f"+
		"/\16/\u0129\13/\3/\6/\u012c\n/\r/\16/\u012d\3/\7/\u0131\n/\f/\16/\u0134"+
		"\13/\3\60\3\60\3\60\3\60\3\60\3\60\7\60\u013c\n\60\f\60\16\60\u013f\13"+
		"\60\3\60\3\60\3\61\3\61\3\61\3\61\3\61\3\61\7\61\u0149\n\61\f\61\16\61"+
		"\u014c\13\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\7\62\u0156\n\62\f"+
		"\62\16\62\u0159\13\62\3\62\3\62\3\63\3\63\3\64\3\64\3\64\3\64\4\u0127"+
		"\u012d\2\65\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33"+
		"\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67"+
		"\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\2_\2a\2c\2e\2g\60\3\2\t"+
		"\6\2&&\62;B\\aa\6\2&&//C\\aa\7\2&&//\62;C\\aa\4\2$$^^\4\2))^^\4\2^^bb"+
		"\3\2\62;\2\u016c\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13"+
		"\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2"+
		"\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2"+
		"!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3"+
		"\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2"+
		"\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E"+
		"\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2"+
		"\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2g\3\2\2\2"+
		"\3i\3\2\2\2\5p\3\2\2\2\7t\3\2\2\2\ty\3\2\2\2\13\177\3\2\2\2\r\u0085\3"+
		"\2\2\2\17\u0088\3\2\2\2\21\u0093\3\2\2\2\23\u009a\3\2\2\2\25\u00a0\3\2"+
		"\2\2\27\u00a5\3\2\2\2\31\u00a8\3\2\2\2\33\u00ac\3\2\2\2\35\u00b0\3\2\2"+
		"\2\37\u00b4\3\2\2\2!\u00ba\3\2\2\2#\u00be\3\2\2\2%\u00c1\3\2\2\2\'\u00c5"+
		"\3\2\2\2)\u00ca\3\2\2\2+\u00d0\3\2\2\2-\u00da\3\2\2\2/\u00e1\3\2\2\2\61"+
		"\u00ed\3\2\2\2\63\u00ef\3\2\2\2\65\u00f1\3\2\2\2\67\u00f3\3\2\2\29\u00f5"+
		"\3\2\2\2;\u00f7\3\2\2\2=\u00f9\3\2\2\2?\u00fc\3\2\2\2A\u00ff\3\2\2\2C"+
		"\u0102\3\2\2\2E\u0104\3\2\2\2G\u0106\3\2\2\2I\u0108\3\2\2\2K\u010a\3\2"+
		"\2\2M\u010c\3\2\2\2O\u010e\3\2\2\2Q\u0110\3\2\2\2S\u0112\3\2\2\2U\u0118"+
		"\3\2\2\2W\u011a\3\2\2\2Y\u011c\3\2\2\2[\u0120\3\2\2\2]\u0127\3\2\2\2_"+
		"\u0135\3\2\2\2a\u0142\3\2\2\2c\u014f\3\2\2\2e\u015c\3\2\2\2g\u015e\3\2"+
		"\2\2ij\7U\2\2jk\7G\2\2kl\7C\2\2lm\7T\2\2mn\7E\2\2no\7J\2\2o\4\3\2\2\2"+
		"pq\7V\2\2qr\7Q\2\2rs\7R\2\2s\6\3\2\2\2tu\7H\2\2uv\7T\2\2vw\7Q\2\2wx\7"+
		"O\2\2x\b\3\2\2\2yz\7Y\2\2z{\7J\2\2{|\7G\2\2|}\7T\2\2}~\7G\2\2~\n\3\2\2"+
		"\2\177\u0080\7N\2\2\u0080\u0081\7K\2\2\u0081\u0082\7O\2\2\u0082\u0083"+
		"\7K\2\2\u0083\u0084\7V\2\2\u0084\f\3\2\2\2\u0085\u0086\7D\2\2\u0086\u0087"+
		"\7[\2\2\u0087\16\3\2\2\2\u0088\u0089\7E\2\2\u0089\u008a\7Q\2\2\u008a\u008b"+
		"\7W\2\2\u008b\u008c\7P\2\2\u008c\u008d\7V\2\2\u008d\u008e\7H\2\2\u008e"+
		"\u008f\7K\2\2\u008f\u0090\7G\2\2\u0090\u0091\7N\2\2\u0091\u0092\7F\2\2"+
		"\u0092\20\3\2\2\2\u0093\u0094\7U\2\2\u0094\u0095\7Q\2\2\u0095\u0096\7"+
		"W\2\2\u0096\u0097\7T\2\2\u0097\u0098\7E\2\2\u0098\u0099\7G\2\2\u0099\22"+
		"\3\2\2\2\u009a\u009b\7K\2\2\u009b\u009c\7P\2\2\u009c\u009d\7F\2\2\u009d"+
		"\u009e\7G\2\2\u009e\u009f\7Z\2\2\u009f\24\3\2\2\2\u00a0\u00a1\7E\2\2\u00a1"+
		"\u00a2\7C\2\2\u00a2\u00a3\7U\2\2\u00a3\u00a4\7G\2\2\u00a4\26\3\2\2\2\u00a5"+
		"\u00a6\7K\2\2\u00a6\u00a7\7P\2\2\u00a7\30\3\2\2\2\u00a8\u00a9\7C\2\2\u00a9"+
		"\u00aa\7D\2\2\u00aa\u00ab\7U\2\2\u00ab\32\3\2\2\2\u00ac\u00ad\7R\2\2\u00ad"+
		"\u00ae\7Q\2\2\u00ae\u00af\7Y\2\2\u00af\34\3\2\2\2\u00b0\u00b1\7C\2\2\u00b1"+
		"\u00b2\7X\2\2\u00b2\u00b3\7I\2\2\u00b3\36\3\2\2\2\u00b4\u00b5\7E\2\2\u00b5"+
		"\u00b6\7Q\2\2\u00b6\u00b7\7W\2\2\u00b7\u00b8\7P\2\2\u00b8\u00b9\7V\2\2"+
		"\u00b9 \3\2\2\2\u00ba\u00bb\7P\2\2\u00bb\u00bc\7Q\2\2\u00bc\u00bd\7V\2"+
		"\2\u00bd\"\3\2\2\2\u00be\u00bf\7Q\2\2\u00bf\u00c0\7T\2\2\u00c0$\3\2\2"+
		"\2\u00c1\u00c2\7C\2\2\u00c2\u00c3\7P\2\2\u00c3\u00c4\7F\2\2\u00c4&\3\2"+
		"\2\2\u00c5\u00c6\7V\2\2\u00c6\u00c7\7T\2\2\u00c7\u00c8\7W\2\2\u00c8\u00c9"+
		"\7G\2\2\u00c9(\3\2\2\2\u00ca\u00cb\7H\2\2\u00cb\u00cc\7C\2\2\u00cc\u00cd"+
		"\7N\2\2\u00cd\u00ce\7U\2\2\u00ce\u00cf\7G\2\2\u00cf*\3\2\2\2\u00d0\u00d1"+
		"\7F\2\2\u00d1\u00d2\7C\2\2\u00d2\u00d3\7V\2\2\u00d3\u00d4\7C\2\2\u00d4"+
		"\u00d5\7O\2\2\u00d5\u00d6\7Q\2\2\u00d6\u00d7\7F\2\2\u00d7\u00d8\7G\2\2"+
		"\u00d8\u00d9\7N\2\2\u00d9,\3\2\2\2\u00da\u00db\7N\2\2\u00db\u00dc\7Q\2"+
		"\2\u00dc\u00dd\7Q\2\2\u00dd\u00de\7M\2\2\u00de\u00df\7W\2\2\u00df\u00e0"+
		"\7R\2\2\u00e0.\3\2\2\2\u00e1\u00e2\7U\2\2\u00e2\u00e3\7C\2\2\u00e3\u00e4"+
		"\7X\2\2\u00e4\u00e5\7G\2\2\u00e5\u00e6\7F\2\2\u00e6\u00e7\7U\2\2\u00e7"+
		"\u00e8\7G\2\2\u00e8\u00e9\7C\2\2\u00e9\u00ea\7T\2\2\u00ea\u00eb\7E\2\2"+
		"\u00eb\u00ec\7J\2\2\u00ec\60\3\2\2\2\u00ed\u00ee\7~\2\2\u00ee\62\3\2\2"+
		"\2\u00ef\u00f0\7.\2\2\u00f0\64\3\2\2\2\u00f1\u00f2\7\60\2\2\u00f2\66\3"+
		"\2\2\2\u00f3\u00f4\7?\2\2\u00f48\3\2\2\2\u00f5\u00f6\7@\2\2\u00f6:\3\2"+
		"\2\2\u00f7\u00f8\7>\2\2\u00f8<\3\2\2\2\u00f9\u00fa\7>\2\2\u00fa\u00fb"+
		"\7?\2\2\u00fb>\3\2\2\2\u00fc\u00fd\7@\2\2\u00fd\u00fe\7?\2\2\u00fe@\3"+
		"\2\2\2\u00ff\u0100\7#\2\2\u0100\u0101\7?\2\2\u0101B\3\2\2\2\u0102\u0103"+
		"\7-\2\2\u0103D\3\2\2\2\u0104\u0105\7/\2\2\u0105F\3\2\2\2\u0106\u0107\7"+
		",\2\2\u0107H\3\2\2\2\u0108\u0109\7\61\2\2\u0109J\3\2\2\2\u010a\u010b\7"+
		"\'\2\2\u010bL\3\2\2\2\u010c\u010d\7#\2\2\u010dN\3\2\2\2\u010e\u010f\7"+
		"<\2\2\u010fP\3\2\2\2\u0110\u0111\7*\2\2\u0111R\3\2\2\2\u0112\u0113\7+"+
		"\2\2\u0113T\3\2\2\2\u0114\u0119\5]/\2\u0115\u0119\5_\60\2\u0116\u0119"+
		"\5a\61\2\u0117\u0119\5c\62\2\u0118\u0114\3\2\2\2\u0118\u0115\3\2\2\2\u0118"+
		"\u0116\3\2\2\2\u0118\u0117\3\2\2\2\u0119V\3\2\2\2\u011a\u011b\5]/\2\u011b"+
		"X\3\2\2\2\u011c\u011d\7\60\2\2\u011d\u011e\5W,\2\u011eZ\3\2\2\2\u011f"+
		"\u0121\5e\63\2\u0120\u011f\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0120\3\2"+
		"\2\2\u0122\u0123\3\2\2\2\u0123\\\3\2\2\2\u0124\u0126\t\2\2\2\u0125\u0124"+
		"\3\2\2\2\u0126\u0129\3\2\2\2\u0127\u0128\3\2\2\2\u0127\u0125\3\2\2\2\u0128"+
		"\u012b\3\2\2\2\u0129\u0127\3\2\2\2\u012a\u012c\t\3\2\2\u012b\u012a\3\2"+
		"\2\2\u012c\u012d\3\2\2\2\u012d\u012e\3\2\2\2\u012d\u012b\3\2\2\2\u012e"+
		"\u0132\3\2\2\2\u012f\u0131\t\4\2\2\u0130\u012f\3\2\2\2\u0131\u0134\3\2"+
		"\2\2\u0132\u0130\3\2\2\2\u0132\u0133\3\2\2\2\u0133^\3\2\2\2\u0134\u0132"+
		"\3\2\2\2\u0135\u013d\7$\2\2\u0136\u0137\7^\2\2\u0137\u013c\13\2\2\2\u0138"+
		"\u0139\7$\2\2\u0139\u013c\7$\2\2\u013a\u013c\n\5\2\2\u013b\u0136\3\2\2"+
		"\2\u013b\u0138\3\2\2\2\u013b\u013a\3\2\2\2\u013c\u013f\3\2\2\2\u013d\u013b"+
		"\3\2\2\2\u013d\u013e\3\2\2\2\u013e\u0140\3\2\2\2\u013f\u013d\3\2\2\2\u0140"+
		"\u0141\7$\2\2\u0141`\3\2\2\2\u0142\u014a\7)\2\2\u0143\u0144\7^\2\2\u0144"+
		"\u0149\13\2\2\2\u0145\u0146\7)\2\2\u0146\u0149\7)\2\2\u0147\u0149\n\6"+
		"\2\2\u0148\u0143\3\2\2\2\u0148\u0145\3\2\2\2\u0148\u0147\3\2\2\2\u0149"+
		"\u014c\3\2\2\2\u014a\u0148\3\2\2\2\u014a\u014b\3\2\2\2\u014b\u014d\3\2"+
		"\2\2\u014c\u014a\3\2\2\2\u014d\u014e\7)\2\2\u014eb\3\2\2\2\u014f\u0157"+
		"\7b\2\2\u0150\u0151\7^\2\2\u0151\u0156\13\2\2\2\u0152\u0153\7b\2\2\u0153"+
		"\u0156\7b\2\2\u0154\u0156\n\7\2\2\u0155\u0150\3\2\2\2\u0155\u0152\3\2"+
		"\2\2\u0155\u0154\3\2\2\2\u0156\u0159\3\2\2\2\u0157\u0155\3\2\2\2\u0157"+
		"\u0158\3\2\2\2\u0158\u015a\3\2\2\2\u0159\u0157\3\2\2\2\u015a\u015b\7b"+
		"\2\2\u015bd\3\2\2\2\u015c\u015d\t\b\2\2\u015df\3\2\2\2\u015e\u015f\13"+
		"\2\2\2\u015f\u0160\3\2\2\2\u0160\u0161\b\64\2\2\u0161h\3\2\2\2\16\2\u0118"+
		"\u0122\u0127\u012d\u0132\u013b\u013d\u0148\u014a\u0155\u0157\3\2\5\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}