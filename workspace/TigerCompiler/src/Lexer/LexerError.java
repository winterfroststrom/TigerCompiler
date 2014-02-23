package Lexer;

import Lexer.GeneralDFA.ESTATE;

public class LexerError {
	private char character;
	private int position;
	private ESTATE state;
	
	public LexerError(char character, int position, ESTATE state){
		this.character = character;
		this.position = position;
		this.state = state;
	}
	
	@Override
	public String toString(){
		return "Unexpected character at position " + position + ": " 
				+ character
				+ " at state " + state;
	}
}
