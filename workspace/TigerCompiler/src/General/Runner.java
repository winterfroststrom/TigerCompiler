package General;

import java.io.IOException;
import java.util.List;

import Lexer.Lexer;
import Lexer.LexerError;
import Parser.Parser;
import Parser.ParserError;

public class Runner {
	public static void main(String[] args) {
		args[0] = "resources/tictactoe.tiger";
		Lexer lexer = new Lexer();
		Parser parser = new Parser();
		try(TigerFileHandler tfh = new TigerFileHandler(args)){
			tfh.redirectConsole();
			List<Token> tokens = lexer.lex(tfh.getInput());
			for(Token token : tokens){
				System.out.print(token.token + " ");	
			}
			System.out.println();
			
			if(parser.parse(tokens)){
				System.out.println("successful parse");
			}  else {
				System.out.println("unsuccessful parse");				
			}
			printLexerErrors(lexer.errors());
			printParserErrors(parser.errors());
		} catch(IOException e){
			System.err.println(e.getMessage());
		}
	}

	private static void printParserErrors(List<ParserError> errors ) {
		System.err.println("Parser Errors:");
		if(errors.isEmpty()){
			System.err.println("No errors");
		} else {
			for(ParserError error : errors){
				System.err.println(error);
			}
		}
	}

	private static void printLexerErrors(List<LexerError> errors) {
		System.err.println("Lexer Errors:");
		if(errors.isEmpty()){
			System.err.println("No errors");
		} else {
			for(LexerError error : errors){
				System.err.println(error);
			}
		}
	}
}
