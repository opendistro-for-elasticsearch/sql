// Generated from /Users/penghuo/opendistro/poc-ppl/src/main/antlr/PPLParser.g4 by ANTLR 4.8
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PPLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SOURCE=1, INDEX=2, SEARCH=3, TOP=4, FROM=5, WHERE=6, LIMIT=7, BY=8, COUNTFIELD=9, 
		CASE=10, IN=11, ABS=12, POW=13, AVG=14, COUNT=15, NOT=16, OR=17, AND=18, 
		TRUE=19, FALSE=20, DATAMODEL=21, LOOKUP=22, SAVEDSEARCH=23, PIPE=24, COMMA=25, 
		DOT=26, EQUAL_SYMBOL=27, GREATER_SYMBOL=28, LESS_SYMBOL=29, NOT_GREATER=30, 
		NOT_LESS=31, NOT_EQUAL=32, PLUS=33, MINUS=34, STAR=35, DIVIDE=36, MODULE=37, 
		EXCLAMATION_SYMBOL=38, COLON=39, LT_PRTHS=40, RT_PRTHS=41, ID=42, DOT_ID=43, 
		STRING_LITERAL=44, DECIMAL_LITERAL=45, ERROR_RECOGNITION=46;
	public static final int
		RULE_pplStatement = 0, RULE_searchCommands = 1, RULE_reportsCommands = 2, 
		RULE_resultsCommands = 3, RULE_filteringCommands = 4, RULE_searchCommand = 5, 
		RULE_fromClause = 6, RULE_topCommand = 7, RULE_fromCommand = 8, RULE_whereCommand = 9, 
		RULE_logicalExpression = 10, RULE_booleanExpression = 11, RULE_comparisonExpression = 12, 
		RULE_evalExpression = 13, RULE_expression = 14, RULE_evalFunctionCall = 15, 
		RULE_topOptions = 16, RULE_datasetType = 17, RULE_datasetName = 18, RULE_byClause = 19, 
		RULE_booleanLiteral = 20, RULE_comparisonOperator = 21, RULE_evalFunctionName = 22, 
		RULE_functionArgs = 23, RULE_functionArg = 24, RULE_fieldExpression = 25, 
		RULE_fieldList = 26, RULE_valueExpression = 27, RULE_valueList = 28, RULE_constant = 29, 
		RULE_literalValue = 30, RULE_stringLiteral = 31, RULE_decimalLiteral = 32, 
		RULE_fullColumnName = 33, RULE_simpleId = 34, RULE_evalFunctionNameBase = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"pplStatement", "searchCommands", "reportsCommands", "resultsCommands", 
			"filteringCommands", "searchCommand", "fromClause", "topCommand", "fromCommand", 
			"whereCommand", "logicalExpression", "booleanExpression", "comparisonExpression", 
			"evalExpression", "expression", "evalFunctionCall", "topOptions", "datasetType", 
			"datasetName", "byClause", "booleanLiteral", "comparisonOperator", "evalFunctionName", 
			"functionArgs", "functionArg", "fieldExpression", "fieldList", "valueExpression", 
			"valueList", "constant", "literalValue", "stringLiteral", "decimalLiteral", 
			"fullColumnName", "simpleId", "evalFunctionNameBase"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'SOURCE'", "'INDEX'", "'SEARCH'", "'TOP'", "'FROM'", "'WHERE'", 
			"'LIMIT'", "'BY'", "'COUNTFIELD'", "'CASE'", "'IN'", "'ABS'", "'POW'", 
			"'AVG'", "'COUNT'", "'NOT'", "'OR'", "'AND'", "'TRUE'", "'FALSE'", "'DATAMODEL'", 
			"'LOOKUP'", "'SAVEDSEARCH'", "'|'", "','", "'.'", "'='", "'>'", "'<'", 
			null, null, null, "'+'", "'-'", "'*'", "'/'", "'%'", "'!'", "':'", "'('", 
			"')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SOURCE", "INDEX", "SEARCH", "TOP", "FROM", "WHERE", "LIMIT", "BY", 
			"COUNTFIELD", "CASE", "IN", "ABS", "POW", "AVG", "COUNT", "NOT", "OR", 
			"AND", "TRUE", "FALSE", "DATAMODEL", "LOOKUP", "SAVEDSEARCH", "PIPE", 
			"COMMA", "DOT", "EQUAL_SYMBOL", "GREATER_SYMBOL", "LESS_SYMBOL", "NOT_GREATER", 
			"NOT_LESS", "NOT_EQUAL", "PLUS", "MINUS", "STAR", "DIVIDE", "MODULE", 
			"EXCLAMATION_SYMBOL", "COLON", "LT_PRTHS", "RT_PRTHS", "ID", "DOT_ID", 
			"STRING_LITERAL", "DECIMAL_LITERAL", "ERROR_RECOGNITION"
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

	@Override
	public String getGrammarFileName() { return "PPLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PPLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class PplStatementContext extends ParserRuleContext {
		public List<SearchCommandsContext> searchCommands() {
			return getRuleContexts(SearchCommandsContext.class);
		}
		public SearchCommandsContext searchCommands(int i) {
			return getRuleContext(SearchCommandsContext.class,i);
		}
		public List<ReportsCommandsContext> reportsCommands() {
			return getRuleContexts(ReportsCommandsContext.class);
		}
		public ReportsCommandsContext reportsCommands(int i) {
			return getRuleContext(ReportsCommandsContext.class,i);
		}
		public TerminalNode EOF() { return getToken(PPLParser.EOF, 0); }
		public List<TerminalNode> PIPE() { return getTokens(PPLParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(PPLParser.PIPE, i);
		}
		public ResultsCommandsContext resultsCommands() {
			return getRuleContext(ResultsCommandsContext.class,0);
		}
		public PplStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pplStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterPplStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitPplStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitPplStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PplStatementContext pplStatement() throws RecognitionException {
		PplStatementContext _localctx = new PplStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_pplStatement);
		int _la;
		try {
			setState(88);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SOURCE:
			case INDEX:
			case SEARCH:
			case ABS:
			case POW:
			case NOT:
			case TRUE:
			case FALSE:
			case LT_PRTHS:
			case ID:
			case DOT_ID:
			case STRING_LITERAL:
			case DECIMAL_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(72);
				searchCommands();
				}
				break;
			case TOP:
				enterOuterAlt(_localctx, 2);
				{
				setState(73);
				reportsCommands();
				setState(76);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(74);
					match(PIPE);
					setState(75);
					resultsCommands();
					}
					break;
				}
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==TOP || _la==PIPE) {
					{
					setState(81);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case PIPE:
						{
						setState(78);
						match(PIPE);
						setState(79);
						searchCommands();
						}
						break;
					case TOP:
						{
						setState(80);
						reportsCommands();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(85);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(86);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SearchCommandsContext extends ParserRuleContext {
		public SearchCommandContext searchCommand() {
			return getRuleContext(SearchCommandContext.class,0);
		}
		public SearchCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterSearchCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitSearchCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitSearchCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchCommandsContext searchCommands() throws RecognitionException {
		SearchCommandsContext _localctx = new SearchCommandsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_searchCommands);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			searchCommand();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReportsCommandsContext extends ParserRuleContext {
		public TopCommandContext topCommand() {
			return getRuleContext(TopCommandContext.class,0);
		}
		public ReportsCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reportsCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterReportsCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitReportsCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitReportsCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReportsCommandsContext reportsCommands() throws RecognitionException {
		ReportsCommandsContext _localctx = new ReportsCommandsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_reportsCommands);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			topCommand();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResultsCommandsContext extends ParserRuleContext {
		public FilteringCommandsContext filteringCommands() {
			return getRuleContext(FilteringCommandsContext.class,0);
		}
		public ResultsCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resultsCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterResultsCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitResultsCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitResultsCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResultsCommandsContext resultsCommands() throws RecognitionException {
		ResultsCommandsContext _localctx = new ResultsCommandsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_resultsCommands);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			filteringCommands();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilteringCommandsContext extends ParserRuleContext {
		public FromCommandContext fromCommand() {
			return getRuleContext(FromCommandContext.class,0);
		}
		public WhereCommandContext whereCommand() {
			return getRuleContext(WhereCommandContext.class,0);
		}
		public FilteringCommandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filteringCommands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFilteringCommands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFilteringCommands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFilteringCommands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilteringCommandsContext filteringCommands() throws RecognitionException {
		FilteringCommandsContext _localctx = new FilteringCommandsContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_filteringCommands);
		try {
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FROM:
				enterOuterAlt(_localctx, 1);
				{
				setState(96);
				fromCommand();
				}
				break;
			case WHERE:
				enterOuterAlt(_localctx, 2);
				{
				setState(97);
				whereCommand();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SearchCommandContext extends ParserRuleContext {
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public TerminalNode SEARCH() { return getToken(PPLParser.SEARCH, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public SearchCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterSearchCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitSearchCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitSearchCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchCommandContext searchCommand() throws RecognitionException {
		SearchCommandContext _localctx = new SearchCommandContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_searchCommand);
		int _la;
		try {
			setState(120);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEARCH) {
					{
					setState(100);
					match(SEARCH);
					}
				}

				setState(103);
				fromClause();
				setState(107);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABS) | (1L << POW) | (1L << NOT) | (1L << TRUE) | (1L << FALSE) | (1L << LT_PRTHS) | (1L << ID) | (1L << DOT_ID) | (1L << STRING_LITERAL) | (1L << DECIMAL_LITERAL))) != 0)) {
					{
					{
					setState(104);
					logicalExpression(0);
					}
					}
					setState(109);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEARCH) {
					{
					setState(110);
					match(SEARCH);
					}
				}

				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABS) | (1L << POW) | (1L << NOT) | (1L << TRUE) | (1L << FALSE) | (1L << LT_PRTHS) | (1L << ID) | (1L << DOT_ID) | (1L << STRING_LITERAL) | (1L << DECIMAL_LITERAL))) != 0)) {
					{
					{
					setState(113);
					logicalExpression(0);
					}
					}
					setState(118);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(119);
				fromClause();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode SOURCE() { return getToken(PPLParser.SOURCE, 0); }
		public TerminalNode EQUAL_SYMBOL() { return getToken(PPLParser.EQUAL_SYMBOL, 0); }
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode INDEX() { return getToken(PPLParser.INDEX, 0); }
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fromClause);
		try {
			setState(128);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SOURCE:
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				match(SOURCE);
				setState(123);
				match(EQUAL_SYMBOL);
				setState(124);
				valueExpression();
				}
				break;
			case INDEX:
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				match(INDEX);
				setState(126);
				match(EQUAL_SYMBOL);
				setState(127);
				valueExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TopCommandContext extends ParserRuleContext {
		public TerminalNode TOP() { return getToken(PPLParser.TOP, 0); }
		public DecimalLiteralContext decimalLiteral() {
			return getRuleContext(DecimalLiteralContext.class,0);
		}
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public List<TopOptionsContext> topOptions() {
			return getRuleContexts(TopOptionsContext.class);
		}
		public TopOptionsContext topOptions(int i) {
			return getRuleContext(TopOptionsContext.class,i);
		}
		public List<ByClauseContext> byClause() {
			return getRuleContexts(ByClauseContext.class);
		}
		public ByClauseContext byClause(int i) {
			return getRuleContext(ByClauseContext.class,i);
		}
		public TopCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterTopCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitTopCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitTopCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopCommandContext topCommand() throws RecognitionException {
		TopCommandContext _localctx = new TopCommandContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_topCommand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(TOP);
			setState(131);
			decimalLiteral();
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LIMIT || _la==COUNTFIELD) {
				{
				{
				setState(132);
				topOptions();
				}
				}
				setState(137);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(138);
			fieldList();
			setState(142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BY) {
				{
				{
				setState(139);
				byClause();
				}
				}
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromCommandContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(PPLParser.FROM, 0); }
		public DatasetTypeContext datasetType() {
			return getRuleContext(DatasetTypeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(PPLParser.COLON, 0); }
		public DatasetNameContext datasetName() {
			return getRuleContext(DatasetNameContext.class,0);
		}
		public FromCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFromCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFromCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFromCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromCommandContext fromCommand() throws RecognitionException {
		FromCommandContext _localctx = new FromCommandContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_fromCommand);
		try {
			setState(154);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(FROM);
				setState(146);
				datasetType();
				setState(147);
				match(COLON);
				setState(148);
				datasetName();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(150);
				match(FROM);
				setState(151);
				datasetType();
				setState(152);
				datasetName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereCommandContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(PPLParser.WHERE, 0); }
		public EvalExpressionContext evalExpression() {
			return getRuleContext(EvalExpressionContext.class,0);
		}
		public WhereCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereCommand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterWhereCommand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitWhereCommand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitWhereCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereCommandContext whereCommand() throws RecognitionException {
		WhereCommandContext _localctx = new WhereCommandContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_whereCommand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			match(WHERE);
			setState(157);
			evalExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LogicalExpressionContext extends ParserRuleContext {
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public ComparisonExpressionContext comparisonExpression() {
			return getRuleContext(ComparisonExpressionContext.class,0);
		}
		public EvalExpressionContext evalExpression() {
			return getRuleContext(EvalExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(PPLParser.NOT, 0); }
		public List<LogicalExpressionContext> logicalExpression() {
			return getRuleContexts(LogicalExpressionContext.class);
		}
		public LogicalExpressionContext logicalExpression(int i) {
			return getRuleContext(LogicalExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(PPLParser.OR, 0); }
		public TerminalNode AND() { return getToken(PPLParser.AND, 0); }
		public LogicalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterLogicalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitLogicalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitLogicalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalExpressionContext logicalExpression() throws RecognitionException {
		return logicalExpression(0);
	}

	private LogicalExpressionContext logicalExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LogicalExpressionContext _localctx = new LogicalExpressionContext(_ctx, _parentState);
		LogicalExpressionContext _prevctx = _localctx;
		int _startState = 20;
		enterRecursionRule(_localctx, 20, RULE_logicalExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(160);
				booleanExpression();
				}
				break;
			case 2:
				{
				setState(161);
				comparisonExpression();
				}
				break;
			case 3:
				{
				setState(162);
				evalExpression();
				}
				break;
			case 4:
				{
				setState(163);
				match(NOT);
				setState(164);
				logicalExpression(3);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(175);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(173);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(167);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(168);
						match(OR);
						setState(169);
						logicalExpression(3);
						}
						break;
					case 2:
						{
						_localctx = new LogicalExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_logicalExpression);
						setState(170);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(171);
						match(AND);
						setState(172);
						logicalExpression(2);
						}
						break;
					}
					} 
				}
				setState(177);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class BooleanExpressionContext extends ParserRuleContext {
		public TerminalNode LT_PRTHS() { return getToken(PPLParser.LT_PRTHS, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public TerminalNode RT_PRTHS() { return getToken(PPLParser.RT_PRTHS, 0); }
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public BooleanExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterBooleanExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitBooleanExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitBooleanExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanExpressionContext booleanExpression() throws RecognitionException {
		BooleanExpressionContext _localctx = new BooleanExpressionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_booleanExpression);
		try {
			setState(183);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LT_PRTHS:
				enterOuterAlt(_localctx, 1);
				{
				setState(178);
				match(LT_PRTHS);
				setState(179);
				booleanExpression();
				setState(180);
				match(RT_PRTHS);
				}
				break;
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
				booleanLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonExpressionContext extends ParserRuleContext {
		public FieldExpressionContext fieldExpression() {
			return getRuleContext(FieldExpressionContext.class,0);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ValueExpressionContext valueExpression() {
			return getRuleContext(ValueExpressionContext.class,0);
		}
		public TerminalNode IN() { return getToken(PPLParser.IN, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public ComparisonExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterComparisonExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitComparisonExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitComparisonExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonExpressionContext comparisonExpression() throws RecognitionException {
		ComparisonExpressionContext _localctx = new ComparisonExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_comparisonExpression);
		try {
			setState(193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(185);
				fieldExpression();
				setState(186);
				comparisonOperator();
				setState(187);
				valueExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(189);
				fieldExpression();
				setState(190);
				match(IN);
				setState(191);
				valueList();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EvalExpressionContext extends ParserRuleContext {
		public List<LiteralValueContext> literalValue() {
			return getRuleContexts(LiteralValueContext.class);
		}
		public LiteralValueContext literalValue(int i) {
			return getRuleContext(LiteralValueContext.class,i);
		}
		public TerminalNode EQUAL_SYMBOL() { return getToken(PPLParser.EQUAL_SYMBOL, 0); }
		public EvalFunctionCallContext evalFunctionCall() {
			return getRuleContext(EvalFunctionCallContext.class,0);
		}
		public EvalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_evalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterEvalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitEvalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitEvalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EvalExpressionContext evalExpression() throws RecognitionException {
		EvalExpressionContext _localctx = new EvalExpressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_evalExpression);
		try {
			setState(200);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
			case DECIMAL_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				literalValue();
				setState(196);
				match(EQUAL_SYMBOL);
				setState(197);
				literalValue();
				}
				break;
			case ABS:
			case POW:
				enterOuterAlt(_localctx, 2);
				{
				setState(199);
				evalFunctionCall();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public LogicalExpressionContext logicalExpression() {
			return getRuleContext(LogicalExpressionContext.class,0);
		}
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public ComparisonExpressionContext comparisonExpression() {
			return getRuleContext(ComparisonExpressionContext.class,0);
		}
		public EvalExpressionContext evalExpression() {
			return getRuleContext(EvalExpressionContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_expression);
		try {
			setState(206);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(202);
				logicalExpression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(203);
				booleanExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(204);
				comparisonExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(205);
				evalExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EvalFunctionCallContext extends ParserRuleContext {
		public EvalFunctionNameContext evalFunctionName() {
			return getRuleContext(EvalFunctionNameContext.class,0);
		}
		public TerminalNode LT_PRTHS() { return getToken(PPLParser.LT_PRTHS, 0); }
		public FunctionArgsContext functionArgs() {
			return getRuleContext(FunctionArgsContext.class,0);
		}
		public TerminalNode RT_PRTHS() { return getToken(PPLParser.RT_PRTHS, 0); }
		public EvalFunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_evalFunctionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterEvalFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitEvalFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitEvalFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EvalFunctionCallContext evalFunctionCall() throws RecognitionException {
		EvalFunctionCallContext _localctx = new EvalFunctionCallContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_evalFunctionCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			evalFunctionName();
			setState(209);
			match(LT_PRTHS);
			setState(210);
			functionArgs();
			setState(211);
			match(RT_PRTHS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TopOptionsContext extends ParserRuleContext {
		public TopOptionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topOptions; }
	 
		public TopOptionsContext() { }
		public void copyFrom(TopOptionsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CountfieldTopOptionContext extends TopOptionsContext {
		public TerminalNode COUNTFIELD() { return getToken(PPLParser.COUNTFIELD, 0); }
		public TerminalNode EQUAL_SYMBOL() { return getToken(PPLParser.EQUAL_SYMBOL, 0); }
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public CountfieldTopOptionContext(TopOptionsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterCountfieldTopOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitCountfieldTopOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitCountfieldTopOption(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LimitTopOptionContext extends TopOptionsContext {
		public TerminalNode LIMIT() { return getToken(PPLParser.LIMIT, 0); }
		public TerminalNode EQUAL_SYMBOL() { return getToken(PPLParser.EQUAL_SYMBOL, 0); }
		public DecimalLiteralContext decimalLiteral() {
			return getRuleContext(DecimalLiteralContext.class,0);
		}
		public LimitTopOptionContext(TopOptionsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterLimitTopOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitLimitTopOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitLimitTopOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopOptionsContext topOptions() throws RecognitionException {
		TopOptionsContext _localctx = new TopOptionsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_topOptions);
		try {
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COUNTFIELD:
				_localctx = new CountfieldTopOptionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(213);
				match(COUNTFIELD);
				setState(214);
				match(EQUAL_SYMBOL);
				setState(215);
				stringLiteral();
				}
				break;
			case LIMIT:
				_localctx = new LimitTopOptionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(216);
				match(LIMIT);
				setState(217);
				match(EQUAL_SYMBOL);
				setState(218);
				decimalLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DatasetTypeContext extends ParserRuleContext {
		public TerminalNode DATAMODEL() { return getToken(PPLParser.DATAMODEL, 0); }
		public TerminalNode LOOKUP() { return getToken(PPLParser.LOOKUP, 0); }
		public TerminalNode SAVEDSEARCH() { return getToken(PPLParser.SAVEDSEARCH, 0); }
		public DatasetTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datasetType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterDatasetType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitDatasetType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitDatasetType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatasetTypeContext datasetType() throws RecognitionException {
		DatasetTypeContext _localctx = new DatasetTypeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_datasetType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DATAMODEL) | (1L << LOOKUP) | (1L << SAVEDSEARCH))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DatasetNameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PPLParser.ID, 0); }
		public DatasetNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datasetName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterDatasetName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitDatasetName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitDatasetName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatasetNameContext datasetName() throws RecognitionException {
		DatasetNameContext _localctx = new DatasetNameContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_datasetName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ByClauseContext extends ParserRuleContext {
		public TerminalNode BY() { return getToken(PPLParser.BY, 0); }
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public ByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_byClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ByClauseContext byClause() throws RecognitionException {
		ByClauseContext _localctx = new ByClauseContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_byClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(225);
			match(BY);
			setState(226);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(PPLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(PPLParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitBooleanLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQUAL_SYMBOL() { return getToken(PPLParser.EQUAL_SYMBOL, 0); }
		public TerminalNode NOT_EQUAL() { return getToken(PPLParser.NOT_EQUAL, 0); }
		public TerminalNode LESS_SYMBOL() { return getToken(PPLParser.LESS_SYMBOL, 0); }
		public TerminalNode NOT_LESS() { return getToken(PPLParser.NOT_LESS, 0); }
		public TerminalNode GREATER_SYMBOL() { return getToken(PPLParser.GREATER_SYMBOL, 0); }
		public TerminalNode NOT_GREATER() { return getToken(PPLParser.NOT_GREATER, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL_SYMBOL) | (1L << GREATER_SYMBOL) | (1L << LESS_SYMBOL) | (1L << NOT_GREATER) | (1L << NOT_LESS) | (1L << NOT_EQUAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EvalFunctionNameContext extends ParserRuleContext {
		public EvalFunctionNameBaseContext evalFunctionNameBase() {
			return getRuleContext(EvalFunctionNameBaseContext.class,0);
		}
		public EvalFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_evalFunctionName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterEvalFunctionName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitEvalFunctionName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitEvalFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EvalFunctionNameContext evalFunctionName() throws RecognitionException {
		EvalFunctionNameContext _localctx = new EvalFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_evalFunctionName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			evalFunctionNameBase();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionArgsContext extends ParserRuleContext {
		public List<FunctionArgContext> functionArg() {
			return getRuleContexts(FunctionArgContext.class);
		}
		public FunctionArgContext functionArg(int i) {
			return getRuleContext(FunctionArgContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PPLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PPLParser.COMMA, i);
		}
		public FunctionArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFunctionArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFunctionArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFunctionArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgsContext functionArgs() throws RecognitionException {
		FunctionArgsContext _localctx = new FunctionArgsContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_functionArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			functionArg();
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(235);
				match(COMMA);
				setState(236);
				functionArg();
				}
				}
				setState(241);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionArgContext extends ParserRuleContext {
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public FullColumnNameContext fullColumnName() {
			return getRuleContext(FullColumnNameContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public EvalFunctionCallContext evalFunctionCall() {
			return getRuleContext(EvalFunctionCallContext.class,0);
		}
		public FunctionArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFunctionArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFunctionArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFunctionArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgContext functionArg() throws RecognitionException {
		FunctionArgContext _localctx = new FunctionArgContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_functionArg);
		try {
			setState(246);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(242);
				constant();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(243);
				fullColumnName();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(244);
				expression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(245);
				evalFunctionCall();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldExpressionContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public FullColumnNameContext fullColumnName() {
			return getRuleContext(FullColumnNameContext.class,0);
		}
		public FieldExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFieldExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFieldExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFieldExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldExpressionContext fieldExpression() throws RecognitionException {
		FieldExpressionContext _localctx = new FieldExpressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_fieldExpression);
		try {
			setState(250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(248);
				stringLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(249);
				fullColumnName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldListContext extends ParserRuleContext {
		public TerminalNode LT_PRTHS() { return getToken(PPLParser.LT_PRTHS, 0); }
		public List<FieldExpressionContext> fieldExpression() {
			return getRuleContexts(FieldExpressionContext.class);
		}
		public FieldExpressionContext fieldExpression(int i) {
			return getRuleContext(FieldExpressionContext.class,i);
		}
		public TerminalNode RT_PRTHS() { return getToken(PPLParser.RT_PRTHS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PPLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PPLParser.COMMA, i);
		}
		public FieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldListContext fieldList() throws RecognitionException {
		FieldListContext _localctx = new FieldListContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_fieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			match(LT_PRTHS);
			setState(253);
			fieldExpression();
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(254);
				match(COMMA);
				setState(255);
				fieldExpression();
				}
				}
				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(261);
			match(RT_PRTHS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueExpressionContext extends ParserRuleContext {
		public LiteralValueContext literalValue() {
			return getRuleContext(LiteralValueContext.class,0);
		}
		public ValueExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterValueExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitValueExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitValueExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueExpressionContext valueExpression() throws RecognitionException {
		ValueExpressionContext _localctx = new ValueExpressionContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_valueExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			literalValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueListContext extends ParserRuleContext {
		public TerminalNode LT_PRTHS() { return getToken(PPLParser.LT_PRTHS, 0); }
		public List<LiteralValueContext> literalValue() {
			return getRuleContexts(LiteralValueContext.class);
		}
		public LiteralValueContext literalValue(int i) {
			return getRuleContext(LiteralValueContext.class,i);
		}
		public TerminalNode RT_PRTHS() { return getToken(PPLParser.RT_PRTHS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PPLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PPLParser.COMMA, i);
		}
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterValueList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitValueList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitValueList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
			match(LT_PRTHS);
			setState(266);
			literalValue();
			setState(271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(267);
				match(COMMA);
				setState(268);
				literalValue();
				}
				}
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(274);
			match(RT_PRTHS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public DecimalLiteralContext decimalLiteral() {
			return getRuleContext(DecimalLiteralContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(PPLParser.MINUS, 0); }
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_constant);
		try {
			setState(281);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(276);
				stringLiteral();
				}
				break;
			case DECIMAL_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(277);
				decimalLiteral();
				}
				break;
			case MINUS:
				enterOuterAlt(_localctx, 3);
				{
				setState(278);
				match(MINUS);
				setState(279);
				decimalLiteral();
				}
				break;
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 4);
				{
				setState(280);
				booleanLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralValueContext extends ParserRuleContext {
		public StringLiteralContext stringLiteral() {
			return getRuleContext(StringLiteralContext.class,0);
		}
		public DecimalLiteralContext decimalLiteral() {
			return getRuleContext(DecimalLiteralContext.class,0);
		}
		public LiteralValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterLiteralValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitLiteralValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitLiteralValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralValueContext literalValue() throws RecognitionException {
		LiteralValueContext _localctx = new LiteralValueContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_literalValue);
		try {
			setState(285);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(283);
				stringLiteral();
				}
				break;
			case DECIMAL_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(284);
				decimalLiteral();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringLiteralContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(PPLParser.STRING_LITERAL, 0); }
		public StringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralContext stringLiteral() throws RecognitionException {
		StringLiteralContext _localctx = new StringLiteralContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_stringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(287);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecimalLiteralContext extends ParserRuleContext {
		public TerminalNode DECIMAL_LITERAL() { return getToken(PPLParser.DECIMAL_LITERAL, 0); }
		public DecimalLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decimalLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterDecimalLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitDecimalLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DecimalLiteralContext decimalLiteral() throws RecognitionException {
		DecimalLiteralContext _localctx = new DecimalLiteralContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_decimalLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(289);
			match(DECIMAL_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullColumnNameContext extends ParserRuleContext {
		public SimpleIdContext simpleId() {
			return getRuleContext(SimpleIdContext.class,0);
		}
		public List<TerminalNode> DOT_ID() { return getTokens(PPLParser.DOT_ID); }
		public TerminalNode DOT_ID(int i) {
			return getToken(PPLParser.DOT_ID, i);
		}
		public FullColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullColumnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterFullColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitFullColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitFullColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullColumnNameContext fullColumnName() throws RecognitionException {
		FullColumnNameContext _localctx = new FullColumnNameContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_fullColumnName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			simpleId();
			setState(295);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT_ID) {
				{
				{
				setState(292);
				match(DOT_ID);
				}
				}
				setState(297);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleIdContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(PPLParser.ID, 0); }
		public TerminalNode DOT_ID() { return getToken(PPLParser.DOT_ID, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(PPLParser.STRING_LITERAL, 0); }
		public SimpleIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterSimpleId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitSimpleId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitSimpleId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleIdContext simpleId() throws RecognitionException {
		SimpleIdContext _localctx = new SimpleIdContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_simpleId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ID) | (1L << DOT_ID) | (1L << STRING_LITERAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EvalFunctionNameBaseContext extends ParserRuleContext {
		public TerminalNode POW() { return getToken(PPLParser.POW, 0); }
		public TerminalNode ABS() { return getToken(PPLParser.ABS, 0); }
		public EvalFunctionNameBaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_evalFunctionNameBase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).enterEvalFunctionNameBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PPLParserListener ) ((PPLParserListener)listener).exitEvalFunctionNameBase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PPLParserVisitor ) return ((PPLParserVisitor<? extends T>)visitor).visitEvalFunctionNameBase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EvalFunctionNameBaseContext evalFunctionNameBase() throws RecognitionException {
		EvalFunctionNameBaseContext _localctx = new EvalFunctionNameBaseContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_evalFunctionNameBase);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			_la = _input.LA(1);
			if ( !(_la==ABS || _la==POW) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 10:
			return logicalExpression_sempred((LogicalExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean logicalExpression_sempred(LogicalExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\60\u0131\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\3\2\3\2\3\2\5\2O\n\2\3\2\3\2\3\2\7\2"+
		"T\n\2\f\2\16\2W\13\2\3\2\3\2\5\2[\n\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6"+
		"\5\6e\n\6\3\7\5\7h\n\7\3\7\3\7\7\7l\n\7\f\7\16\7o\13\7\3\7\5\7r\n\7\3"+
		"\7\7\7u\n\7\f\7\16\7x\13\7\3\7\5\7{\n\7\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u0083"+
		"\n\b\3\t\3\t\3\t\7\t\u0088\n\t\f\t\16\t\u008b\13\t\3\t\3\t\7\t\u008f\n"+
		"\t\f\t\16\t\u0092\13\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u009d\n"+
		"\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00a8\n\f\3\f\3\f\3\f\3"+
		"\f\3\f\3\f\7\f\u00b0\n\f\f\f\16\f\u00b3\13\f\3\r\3\r\3\r\3\r\3\r\5\r\u00ba"+
		"\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00c4\n\16\3\17\3\17"+
		"\3\17\3\17\3\17\5\17\u00cb\n\17\3\20\3\20\3\20\3\20\5\20\u00d1\n\20\3"+
		"\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u00de\n\22"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31"+
		"\3\31\3\31\7\31\u00f0\n\31\f\31\16\31\u00f3\13\31\3\32\3\32\3\32\3\32"+
		"\5\32\u00f9\n\32\3\33\3\33\5\33\u00fd\n\33\3\34\3\34\3\34\3\34\7\34\u0103"+
		"\n\34\f\34\16\34\u0106\13\34\3\34\3\34\3\35\3\35\3\36\3\36\3\36\3\36\7"+
		"\36\u0110\n\36\f\36\16\36\u0113\13\36\3\36\3\36\3\37\3\37\3\37\3\37\3"+
		"\37\5\37\u011c\n\37\3 \3 \5 \u0120\n \3!\3!\3\"\3\"\3#\3#\7#\u0128\n#"+
		"\f#\16#\u012b\13#\3$\3$\3%\3%\3%\2\3\26&\2\4\6\b\n\f\16\20\22\24\26\30"+
		"\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFH\2\7\3\2\27\31\3\2\25\26\3\2\35"+
		"\"\3\2,.\3\2\16\17\2\u0132\2Z\3\2\2\2\4\\\3\2\2\2\6^\3\2\2\2\b`\3\2\2"+
		"\2\nd\3\2\2\2\fz\3\2\2\2\16\u0082\3\2\2\2\20\u0084\3\2\2\2\22\u009c\3"+
		"\2\2\2\24\u009e\3\2\2\2\26\u00a7\3\2\2\2\30\u00b9\3\2\2\2\32\u00c3\3\2"+
		"\2\2\34\u00ca\3\2\2\2\36\u00d0\3\2\2\2 \u00d2\3\2\2\2\"\u00dd\3\2\2\2"+
		"$\u00df\3\2\2\2&\u00e1\3\2\2\2(\u00e3\3\2\2\2*\u00e6\3\2\2\2,\u00e8\3"+
		"\2\2\2.\u00ea\3\2\2\2\60\u00ec\3\2\2\2\62\u00f8\3\2\2\2\64\u00fc\3\2\2"+
		"\2\66\u00fe\3\2\2\28\u0109\3\2\2\2:\u010b\3\2\2\2<\u011b\3\2\2\2>\u011f"+
		"\3\2\2\2@\u0121\3\2\2\2B\u0123\3\2\2\2D\u0125\3\2\2\2F\u012c\3\2\2\2H"+
		"\u012e\3\2\2\2J[\5\4\3\2KN\5\6\4\2LM\7\32\2\2MO\5\b\5\2NL\3\2\2\2NO\3"+
		"\2\2\2OU\3\2\2\2PQ\7\32\2\2QT\5\4\3\2RT\5\6\4\2SP\3\2\2\2SR\3\2\2\2TW"+
		"\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WU\3\2\2\2XY\7\2\2\3Y[\3\2\2\2Z"+
		"J\3\2\2\2ZK\3\2\2\2[\3\3\2\2\2\\]\5\f\7\2]\5\3\2\2\2^_\5\20\t\2_\7\3\2"+
		"\2\2`a\5\n\6\2a\t\3\2\2\2be\5\22\n\2ce\5\24\13\2db\3\2\2\2dc\3\2\2\2e"+
		"\13\3\2\2\2fh\7\5\2\2gf\3\2\2\2gh\3\2\2\2hi\3\2\2\2im\5\16\b\2jl\5\26"+
		"\f\2kj\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2n{\3\2\2\2om\3\2\2\2pr\7\5"+
		"\2\2qp\3\2\2\2qr\3\2\2\2rv\3\2\2\2su\5\26\f\2ts\3\2\2\2ux\3\2\2\2vt\3"+
		"\2\2\2vw\3\2\2\2wy\3\2\2\2xv\3\2\2\2y{\5\16\b\2zg\3\2\2\2zq\3\2\2\2{\r"+
		"\3\2\2\2|}\7\3\2\2}~\7\35\2\2~\u0083\58\35\2\177\u0080\7\4\2\2\u0080\u0081"+
		"\7\35\2\2\u0081\u0083\58\35\2\u0082|\3\2\2\2\u0082\177\3\2\2\2\u0083\17"+
		"\3\2\2\2\u0084\u0085\7\6\2\2\u0085\u0089\5B\"\2\u0086\u0088\5\"\22\2\u0087"+
		"\u0086\3\2\2\2\u0088\u008b\3\2\2\2\u0089\u0087\3\2\2\2\u0089\u008a\3\2"+
		"\2\2\u008a\u008c\3\2\2\2\u008b\u0089\3\2\2\2\u008c\u0090\5\66\34\2\u008d"+
		"\u008f\5(\25\2\u008e\u008d\3\2\2\2\u008f\u0092\3\2\2\2\u0090\u008e\3\2"+
		"\2\2\u0090\u0091\3\2\2\2\u0091\21\3\2\2\2\u0092\u0090\3\2\2\2\u0093\u0094"+
		"\7\7\2\2\u0094\u0095\5$\23\2\u0095\u0096\7)\2\2\u0096\u0097\5&\24\2\u0097"+
		"\u009d\3\2\2\2\u0098\u0099\7\7\2\2\u0099\u009a\5$\23\2\u009a\u009b\5&"+
		"\24\2\u009b\u009d\3\2\2\2\u009c\u0093\3\2\2\2\u009c\u0098\3\2\2\2\u009d"+
		"\23\3\2\2\2\u009e\u009f\7\b\2\2\u009f\u00a0\5\34\17\2\u00a0\25\3\2\2\2"+
		"\u00a1\u00a2\b\f\1\2\u00a2\u00a8\5\30\r\2\u00a3\u00a8\5\32\16\2\u00a4"+
		"\u00a8\5\34\17\2\u00a5\u00a6\7\22\2\2\u00a6\u00a8\5\26\f\5\u00a7\u00a1"+
		"\3\2\2\2\u00a7\u00a3\3\2\2\2\u00a7\u00a4\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8"+
		"\u00b1\3\2\2\2\u00a9\u00aa\f\4\2\2\u00aa\u00ab\7\23\2\2\u00ab\u00b0\5"+
		"\26\f\5\u00ac\u00ad\f\3\2\2\u00ad\u00ae\7\24\2\2\u00ae\u00b0\5\26\f\4"+
		"\u00af\u00a9\3\2\2\2\u00af\u00ac\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af"+
		"\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\27\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4"+
		"\u00b5\7*\2\2\u00b5\u00b6\5\30\r\2\u00b6\u00b7\7+\2\2\u00b7\u00ba\3\2"+
		"\2\2\u00b8\u00ba\5*\26\2\u00b9\u00b4\3\2\2\2\u00b9\u00b8\3\2\2\2\u00ba"+
		"\31\3\2\2\2\u00bb\u00bc\5\64\33\2\u00bc\u00bd\5,\27\2\u00bd\u00be\58\35"+
		"\2\u00be\u00c4\3\2\2\2\u00bf\u00c0\5\64\33\2\u00c0\u00c1\7\r\2\2\u00c1"+
		"\u00c2\5:\36\2\u00c2\u00c4\3\2\2\2\u00c3\u00bb\3\2\2\2\u00c3\u00bf\3\2"+
		"\2\2\u00c4\33\3\2\2\2\u00c5\u00c6\5> \2\u00c6\u00c7\7\35\2\2\u00c7\u00c8"+
		"\5> \2\u00c8\u00cb\3\2\2\2\u00c9\u00cb\5 \21\2\u00ca\u00c5\3\2\2\2\u00ca"+
		"\u00c9\3\2\2\2\u00cb\35\3\2\2\2\u00cc\u00d1\5\26\f\2\u00cd\u00d1\5\30"+
		"\r\2\u00ce\u00d1\5\32\16\2\u00cf\u00d1\5\34\17\2\u00d0\u00cc\3\2\2\2\u00d0"+
		"\u00cd\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00cf\3\2\2\2\u00d1\37\3\2\2"+
		"\2\u00d2\u00d3\5.\30\2\u00d3\u00d4\7*\2\2\u00d4\u00d5\5\60\31\2\u00d5"+
		"\u00d6\7+\2\2\u00d6!\3\2\2\2\u00d7\u00d8\7\13\2\2\u00d8\u00d9\7\35\2\2"+
		"\u00d9\u00de\5@!\2\u00da\u00db\7\t\2\2\u00db\u00dc\7\35\2\2\u00dc\u00de"+
		"\5B\"\2\u00dd\u00d7\3\2\2\2\u00dd\u00da\3\2\2\2\u00de#\3\2\2\2\u00df\u00e0"+
		"\t\2\2\2\u00e0%\3\2\2\2\u00e1\u00e2\7,\2\2\u00e2\'\3\2\2\2\u00e3\u00e4"+
		"\7\n\2\2\u00e4\u00e5\5\66\34\2\u00e5)\3\2\2\2\u00e6\u00e7\t\3\2\2\u00e7"+
		"+\3\2\2\2\u00e8\u00e9\t\4\2\2\u00e9-\3\2\2\2\u00ea\u00eb\5H%\2\u00eb/"+
		"\3\2\2\2\u00ec\u00f1\5\62\32\2\u00ed\u00ee\7\33\2\2\u00ee\u00f0\5\62\32"+
		"\2\u00ef\u00ed\3\2\2\2\u00f0\u00f3\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f1\u00f2"+
		"\3\2\2\2\u00f2\61\3\2\2\2\u00f3\u00f1\3\2\2\2\u00f4\u00f9\5<\37\2\u00f5"+
		"\u00f9\5D#\2\u00f6\u00f9\5\36\20\2\u00f7\u00f9\5 \21\2\u00f8\u00f4\3\2"+
		"\2\2\u00f8\u00f5\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f7\3\2\2\2\u00f9"+
		"\63\3\2\2\2\u00fa\u00fd\5@!\2\u00fb\u00fd\5D#\2\u00fc\u00fa\3\2\2\2\u00fc"+
		"\u00fb\3\2\2\2\u00fd\65\3\2\2\2\u00fe\u00ff\7*\2\2\u00ff\u0104\5\64\33"+
		"\2\u0100\u0101\7\33\2\2\u0101\u0103\5\64\33\2\u0102\u0100\3\2\2\2\u0103"+
		"\u0106\3\2\2\2\u0104\u0102\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0107\3\2"+
		"\2\2\u0106\u0104\3\2\2\2\u0107\u0108\7+\2\2\u0108\67\3\2\2\2\u0109\u010a"+
		"\5> \2\u010a9\3\2\2\2\u010b\u010c\7*\2\2\u010c\u0111\5> \2\u010d\u010e"+
		"\7\33\2\2\u010e\u0110\5> \2\u010f\u010d\3\2\2\2\u0110\u0113\3\2\2\2\u0111"+
		"\u010f\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\3\2\2\2\u0113\u0111\3\2"+
		"\2\2\u0114\u0115\7+\2\2\u0115;\3\2\2\2\u0116\u011c\5@!\2\u0117\u011c\5"+
		"B\"\2\u0118\u0119\7$\2\2\u0119\u011c\5B\"\2\u011a\u011c\5*\26\2\u011b"+
		"\u0116\3\2\2\2\u011b\u0117\3\2\2\2\u011b\u0118\3\2\2\2\u011b\u011a\3\2"+
		"\2\2\u011c=\3\2\2\2\u011d\u0120\5@!\2\u011e\u0120\5B\"\2\u011f\u011d\3"+
		"\2\2\2\u011f\u011e\3\2\2\2\u0120?\3\2\2\2\u0121\u0122\7.\2\2\u0122A\3"+
		"\2\2\2\u0123\u0124\7/\2\2\u0124C\3\2\2\2\u0125\u0129\5F$\2\u0126\u0128"+
		"\7-\2\2\u0127\u0126\3\2\2\2\u0128\u012b\3\2\2\2\u0129\u0127\3\2\2\2\u0129"+
		"\u012a\3\2\2\2\u012aE\3\2\2\2\u012b\u0129\3\2\2\2\u012c\u012d\t\5\2\2"+
		"\u012dG\3\2\2\2\u012e\u012f\t\6\2\2\u012fI\3\2\2\2 NSUZdgmqvz\u0082\u0089"+
		"\u0090\u009c\u00a7\u00af\u00b1\u00b9\u00c3\u00ca\u00d0\u00dd\u00f1\u00f8"+
		"\u00fc\u0104\u0111\u011b\u011f\u0129";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}