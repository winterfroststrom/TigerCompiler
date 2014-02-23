package Lexer;
import java.util.LinkedList;
import java.util.List;

import General.Token;

public class Lexer {
	private CommentDFA cdfa;
	private GeneralDFA gdfa;
	public List<Token> lex(String input) {
		cdfa = new CommentDFA();
		gdfa = new GeneralDFA();
		return gdfa.lex(cdfa.lex(input));
	}
	
	public List<LexerError> errors(){
		if(gdfa != null){
			return gdfa.errors();
		} else{
			return new LinkedList<>();
		}
	}
}
