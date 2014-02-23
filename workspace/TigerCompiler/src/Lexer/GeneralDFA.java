package Lexer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import General.ETERMINAL;
import General.Token;
import static Lexer.GeneralDFA.EACTION_TYPE.*;
import static Lexer.GeneralDFA.ESTATE.*;

class GeneralDFA {
	private static final ESTATE[][] STATE_TABLE = new ESTATE[ESTATE.length][Character.MAX_VALUE]; 
	private static final Action[][] ACTION_TABLE = new Action[ESTATE.length][Character.MAX_VALUE];
	private static boolean initialized;
	private List<LexerError> errors = new LinkedList<>();
	private List<Token> tokens = new LinkedList<>();
	private List<Character> charBuffer = new ArrayList<>();
	private int position = 0;
	private ESTATE state = START;
	
	private void initializeTable(){
		if(!initialized){
			defaultTransitionAction(START, START, new Action(DROP));
			action(START, ' ', new Action(IGNO));
			action(START, '\n', new Action(IGNO));
			action(START, '\t', new Action(IGNO));
			action(START, '\f', new Action(IGNO));
			action(START, '\r', new Action(IGNO));
			action(START, '+', new Action(TOKE, ETERMINAL.PLUS));
			action(START, '-', new Action(TOKE, ETERMINAL.MINUS));
			action(START, '*', new Action(TOKE, ETERMINAL.MULT));
			action(START, '/', new Action(TOKE, ETERMINAL.DIV));
			action(START, '=', new Action(TOKE, ETERMINAL.EQ));
			action(START, '(', new Action(TOKE, ETERMINAL.LPAREN));
			action(START, ')', new Action(TOKE, ETERMINAL.RPAREN));
			action(START, ',', new Action(TOKE, ETERMINAL.COMMA));
			action(START, '&', new Action(TOKE, ETERMINAL.AND));
			action(START, '|', new Action(TOKE, ETERMINAL.OR));
			action(START, '[', new Action(TOKE, ETERMINAL.LBRACK));
			action(START, ']', new Action(TOKE, ETERMINAL.RBRACK));
			action(START, ';', new Action(TOKE, ETERMINAL.SEMI));
			
			transitionAction(START, ':', COLON, new Action(NOOP));
			defaultTransitionAction(COLON, START, new Action(BACK, ETERMINAL.COLON));
			transitionAction(COLON, '=', START, new Action(TOKE, ETERMINAL.ASSIGN));
	
			transitionAction(START, '<', LANGLE, new Action(NOOP));
			defaultTransitionAction(LANGLE, START, new Action(BACK, ETERMINAL.LESS));
			transitionAction(LANGLE, '>', START, new Action(TOKE, ETERMINAL.NEQ));
			transitionAction(LANGLE, '=', START, new Action(TOKE, ETERMINAL.LESSEREQ));
			
			transitionAction(START, '>', RANGLE, new Action(NOOP));
			defaultTransitionAction(RANGLE, START, new Action(BACK, ETERMINAL.GREATER));
			transitionAction(RANGLE, '=', START, new Action(TOKE, ETERMINAL.GREATEREQ));
			
			initializeTableForSTRLIT();		
			initializeTableForINTLIT();
			initializeTableForID();
			initialized = true;
		}
	}

	private void initializeTableForSTRLIT() {
		transitionAction(START, '"', STRLIT, new Action(NOOP));
		defaultTransitionAction(STRLIT, STRLIT, new Action(NOOP));
		transitionAction(STRLIT, '"', START, new Action(TOKE, ETERMINAL.STRLIT));
		
		transition(STRLIT, '\\', STRLIT_SLASH);
		defaultTransitionAction(STRLIT_SLASH, STRLIT_SLASH, new Action(DROP));
		transitionAction(STRLIT_SLASH, 'n', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_SLASH, 't', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_SLASH, '"', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_SLASH, '\\', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_SLASH, '^', STRLIT_CTL, new Action(NOOP));
		multiTransitionAction(STRLIT_SLASH, new char[]{'0', '1', '2', '3', 
				'4', '5', '6', '7', '8', '9'}, STRLIT_CODE1, new Action(NOOP));
		transitionAction(STRLIT_SLASH, ' ', STRLIT_SPACE, new Action(IGN2));
		transitionAction(STRLIT_SLASH, '\n', STRLIT_SPACE, new Action(IGN2));
		transitionAction(STRLIT_SLASH, '\t', STRLIT_SPACE, new Action(IGN2));
		transitionAction(STRLIT_SLASH, '\f', STRLIT_SPACE, new Action(IGN2));
		transitionAction(STRLIT_SLASH, '\r', STRLIT_SPACE, new Action(IGN2));
		
		defaultTransitionAction(STRLIT_CTL, STRLIT_CTL, new Action(DROP));
		transitionAction(STRLIT_CTL, '@', STRLIT, new Action(NOOP));
		for(char i = 'A'; i < 'Z' + 1; i++){
			transitionAction(STRLIT_CTL, i, STRLIT, new Action(NOOP));
		}
		transitionAction(STRLIT_CTL, '[', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_CTL, ']', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_CTL, '\\', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_CTL, '^', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_CTL, '_', STRLIT, new Action(NOOP));
		transitionAction(STRLIT_CTL, '.', STRLIT, new Action(NOOP));
		
		defaultTransitionAction(STRLIT_CODE1, STRLIT_CODE1, new Action(DROP));
		multiTransitionAction(STRLIT_CODE1, new char[]{'0', '1', '2', '3', 
				'4', '5', '6', '7', '8', '9'}, STRLIT_CODE2, new Action(NOOP));
		
		defaultTransitionAction(STRLIT_CODE2, STRLIT_CODE2, new Action(DROP));
		multiTransitionAction(STRLIT_CODE2, new char[]{'0', '1', '2', '3', 
				'4', '5', '6', '7', '8', '9'}, STRLIT, new Action(NOOP));
		
		defaultTransitionAction(STRLIT_SPACE, STRLIT_SPACE, new Action(DROP));
		transitionAction(STRLIT_SPACE, ' ', STRLIT_SPACE, new Action(IGNO));
		transitionAction(STRLIT_SPACE, '\n', STRLIT_SPACE, new Action(IGNO));
		transitionAction(STRLIT_SPACE, '\t', STRLIT_SPACE, new Action(IGNO));
		transitionAction(STRLIT_SPACE, '\f', STRLIT_SPACE, new Action(IGNO));
		transitionAction(STRLIT_SPACE, '\r', STRLIT_SPACE, new Action(IGNO));
		transitionAction(STRLIT_SPACE, '\\', STRLIT, new Action(IGNO));
	}

	private void initializeTableForINTLIT() {
		multiTransitionAction(START, new char[]{'0', '1', '2', '3', 
				'4', '5', '6', '7', '8', '9'}, INTLIT, new Action(NOOP));
		defaultTransitionAction(INTLIT, START, new Action(BACK, ETERMINAL.INTLIT));
		multiTransitionAction(INTLIT, new char[]{'0', '1', '2', '3', 
				'4', '5', '6', '7', '8', '9'}, INTLIT, new Action(NOOP));
	}

	private void initializeTableForID() {
		for(char i = 'a'; i < 'z' + 1; i++){
			transitionAction(START, i, ID, new Action(NOOP));
		}
		for(char i = 'A'; i < 'Z' + 1; i++){
			transitionAction(START, i, ID, new Action(NOOP));
		}
		defaultTransitionAction(ID, START, new Action(BACK, ETERMINAL.ID));
		for(char i = 'a'; i < 'z' + 1; i++){
			transitionAction(ID, i, ID, new Action(NOOP));
		}
		for(char i = 'A'; i < 'Z' + 1; i++){
			transitionAction(ID, i, ID, new Action(NOOP));
		}
		for(char i = 'a'; i < 'z' + 1; i++){
			transitionAction(ID, i, ID, new Action(NOOP));
		}
		for(char i = '0'; i < '9' + 1; i++){
			transitionAction(ID, i, ID, new Action(NOOP));
		}
		transitionAction(ID, '_', ID, new Action(NOOP));
	}
	
	private void defaultTransitionAction(ESTATE current, ESTATE next, Action action){
		defaultTransition(current, next);
		defaultAction(current, action);
	}
	
	private void multiTransitionAction(ESTATE current, char[] symbols, ESTATE next, Action action){
		for(char symbol : symbols){
			transitionAction(current, symbol, next, action);
		}
	}
	
	private void transitionAction(ESTATE current, char symbol, ESTATE next, Action action){
		transition(current, symbol, next);
		action(current, symbol, action);
	} 
	
	private void transition(ESTATE current, char symbol, ESTATE next){
		STATE_TABLE[current.ordinal()][symbol] = next;
	}
	
	private void action(ESTATE current, char symbol, Action action){
		ACTION_TABLE[current.ordinal()][symbol] = action;
	}
	
	private void defaultTransition(ESTATE current, ESTATE next){
		Arrays.fill(STATE_TABLE[current.ordinal()], next);
	}	
	
	private void defaultAction(ESTATE current, Action action){
		Arrays.fill(ACTION_TABLE[current.ordinal()], action);
	}
	
	public List<LexerError> errors(){
		return errors;
	}
	
	public List<Token> lex(String input){
		initializeTable();
		char[] chars = input.toCharArray();
		while(position < chars.length){
			char next = chars[position++];
			ESTATE nextState = STATE_TABLE[state.ordinal()][next];
			Action action = ACTION_TABLE[state.ordinal()][next];
			charBuffer.add(next);
			doAction(action);
			state = nextState;
		}
		return tokens;
	}
	
	private String charBufferToString(){
		String token = "";
		for(char c : charBuffer){
			token += c;
		}
		return token;
	}
	
	
	private void doAction(Action action) {
		switch(action.action){
		case TOKE:
			tokens.add(new Token(action.token, charBufferToString()));
			charBuffer.clear();
			break;
		case BACK:
			position--;
			charBuffer.remove(charBuffer.size() - 1);
			tokens.add(new Token(action.token, charBufferToString()));
			charBuffer.clear();
			break;
		case IGN2:
			charBuffer.remove(charBuffer.size() - 1);
			charBuffer.remove(charBuffer.size() - 1);
			break;
		case DROP:
			errors.add(generateErrorMessage());
		case IGNO:
			charBuffer.remove(charBuffer.size() - 1);
			break;
		default:
			break;
		}
	}

	private LexerError generateErrorMessage(){
		return new LexerError(charBuffer.get(charBuffer.size() - 1), position, state);
	}
	
	private class Action{
		final EACTION_TYPE action;
		final ETERMINAL token;
		
		Action(EACTION_TYPE action, ETERMINAL token){
			this.action = action;
			this.token = token;
		}
		
		Action(EACTION_TYPE action){
			this(action, null);
		}
	}
	
	protected enum EACTION_TYPE{
		TOKE, NOOP, DROP, BACK, IGNO, IGN2;
	}
	
	protected enum ESTATE{
		START, LANGLE, RANGLE, COLON, INTLIT, ID, STRLIT, STRLIT_SLASH, 
		STRLIT_CTL, STRLIT_CODE1, STRLIT_CODE2, STRLIT_SPACE;
		public static final int length = ESTATE.values().length;
	}
}
