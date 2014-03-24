package Parser;

import java.util.LinkedList;
import java.util.List;

import General.Token;

public class Parser {
	private LL1Parser parser;
	
	public ParseTreeNode parse(List<Token> tokens){
		parser = new LL1Parser();
		return parser.parse(tokens);
	}
	
	public List<ParserError> errors(){
		if(parser != null){
			return parser.errors();
		} else {
			return new LinkedList<>();
		}
	}
}
