package General;

import java.io.IOException;
import java.util.List;

import Lexer.Lexer;
import Parser.ParseTreeNode;
import Parser.Parser;
import SemanticChecking.Checker;

public class Runner {
	public static void main(String[] args) {
		if(Configuration.FORCED_LOAD_FILE != null){
			args = new String[]{Configuration.FORCED_LOAD_FILE};
		}
		Lexer lexer = new Lexer();
		Parser parser = new Parser();
		Checker checker = new Checker();
		try(TigerFileHandler tfh = new TigerFileHandler(args)){
			tfh.redirectConsole();
			List<Token> tokens = lexer.lex(tfh.getInput());
			if(Configuration.PRINT_TOKENS){
				for(Token token : tokens){
					System.out.print(token.token + " ");	
				}
				System.out.println();
			}
			if(lexer.errors().isEmpty()){
				ParseTreeNode tree = parser.parse(tokens);
				if(Configuration.PRINT_TREE){
					if(parser.errors().isEmpty()){
						System.out.println("successful parse");
					}  else {
						System.out.println("unsuccessful parse");				
					}
					System.out.println(tree);
				}
				
				if(parser.errors().isEmpty()){
					if(checker.check(tree)){
						System.out.println("semantic error");
					}
				}
			}
			if(printErrors("Lexer", lexer.errors())){
				if(printErrors("Parser", parser.errors())){
					printErrors("Semantic", checker.errors());		
				}
			}
		} catch(IOException e){
			System.err.println(e.getMessage());
		}
	}

	private static boolean printErrors(String type, List<? extends Object> errors){
		System.err.println(type + " Errors:");
		if(errors.isEmpty()){
			System.err.println("No errors");
			return true;
		} else {
			for(Object error : errors){
				System.err.println(error);
			}
			return false;
		}
	}
}
