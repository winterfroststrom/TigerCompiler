package Lexer;
import java.io.IOException;
import java.util.List;

public class Lexer {

	
	public void lex(String input) {
		CommentDFA cdfa = new CommentDFA();
		GeneralDFA gdfa = new GeneralDFA();
		List<Token> tokens = gdfa.lex(cdfa.lex(input));
		for(Token token : tokens){
			System.out.print(token.token + " ");	
		}
		if(!gdfa.errored){
			System.out.println();
			System.out.println("successful parse");
		}
	}
	
	public static void main(String[] args) {
		args[0] = "resources/ex5.tiger";
		TigerFileHandler tfh = new TigerFileHandler(args);
		Lexer lexer = new Lexer();
		try{
			tfh.redirectConsole();
			lexer.lex(tfh.getInput());
		} catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
}
