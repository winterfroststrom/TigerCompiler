package Lexer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class CommentDFA {
	private static final int[][] STATE_TABLE = new int[6][Character.MAX_VALUE]; 
	private static final int[][] ACTION_TABLE = new int[6][Character.MAX_VALUE]; 
	private static final int START = 0;
	private static final int STRING = 1;
	private static final int STRING_SLASH = 2;
	private static final int SLASH = 3;
	private static final int COMMENT = 4;
	private static final int COMMENT_END = 5;
	private static final int NOOP = 0;
	private static final int TOKEN = 1;
	private static final int CLEAR = 2;
	
	
	
	private List<String> tokens = new LinkedList<>();
	private List<Character> charBuffer = new ArrayList<>();
	
	public void initializeTable(){
		Arrays.fill(STATE_TABLE[START], START);
		Arrays.fill(ACTION_TABLE[START], TOKEN);
		STATE_TABLE[START]['"'] = STRING;
		ACTION_TABLE[START]['"'] = NOOP;
		STATE_TABLE[START]['/'] = SLASH;
		ACTION_TABLE[START]['/'] = NOOP;
		Arrays.fill(STATE_TABLE[STRING], STRING);
		STATE_TABLE[STRING]['\\'] = STRING_SLASH;
		STATE_TABLE[STRING]['"'] = START;
		ACTION_TABLE[STRING]['"'] = TOKEN;
		Arrays.fill(STATE_TABLE[STRING_SLASH], STRING);
		Arrays.fill(STATE_TABLE[SLASH], START);
		Arrays.fill(ACTION_TABLE[SLASH], TOKEN);
		STATE_TABLE[SLASH]['*'] = COMMENT;
		ACTION_TABLE[SLASH]['*'] = NOOP;
		Arrays.fill(STATE_TABLE[COMMENT], COMMENT);
		STATE_TABLE[COMMENT]['*'] = COMMENT_END;
		Arrays.fill(STATE_TABLE[COMMENT_END], COMMENT);
		STATE_TABLE[COMMENT_END]['*'] = COMMENT_END;
		STATE_TABLE[COMMENT_END]['/'] = START;
		ACTION_TABLE[COMMENT_END]['/'] = CLEAR;
	}
	
	public String lex(String input){
		initializeTable();
		char[] chars = input.toCharArray();
		int state = START;
		int position = 0;
		while(position < chars.length){
			char next = chars[position++];
			int nextState = STATE_TABLE[state][next];
			int action = ACTION_TABLE[state][next];
			state = nextState;
			charBuffer.add(next);
			doAction(action);
		}
		String result = "";
		for(String token : tokens){
			result += token;
		}
		return result;
	}
	
	private String charBufferToString(){
		String token = "";
		for(char c : charBuffer){
			token += c;
		}
		return token;
	}
	
	private void doAction(int action){
		switch(action){
		case TOKEN:
			tokens.add(charBufferToString());
			charBuffer.clear();
			break;
		case CLEAR:
			charBuffer.clear();
			break;
		default:
			break;
		}
	}
}
