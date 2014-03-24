package General;

import java.io.IOException;
import java.util.List;

import Lexer.Lexer;
import Parser.Parser;
import SemanticChecking.Checker;

public class Runner {
	public static void main(String[] args) {
		args = new String[]{"resources/tictactoe.tiger"};
		
		Lexer lexer = new Lexer();
		Parser parser = new Parser();
		Checker checker = new Checker();
		try(TigerFileHandler tfh = new TigerFileHandler(args)){
			tfh.redirectConsole();
			List<Token> tokens = lexer.lex(tfh.getInput());
			for(Token token : tokens){
				System.out.print(token.token + " ");	
			}
			System.out.println();
			
			boolean hasSemanticError = checker.check(parser.parse(tokens));
			if(parser.errors().isEmpty()){
				System.out.println("successful parse");
			}  else {
				System.out.println("unsuccessful parse");				
			}
			if(hasSemanticError){
				System.out.println("semantic error");
			}
			printErrors("Lexer", lexer.errors());
			printErrors("Parser", parser.errors());
			printErrors("Semantic", checker.errors());
		} catch(IOException e){
			System.err.println(e.getMessage());
		}
	}

	private static void printErrors(String type, List<? extends Object> errors){
		System.err.println(type + " Errors:");
		if(errors.isEmpty()){
			System.err.println("No errors");
		} else {
			for(Object error : errors){
				System.err.println(error);
			}
		}
	}
}
