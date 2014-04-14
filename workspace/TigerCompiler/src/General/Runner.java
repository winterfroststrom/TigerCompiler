package General;

import java.io.IOException;
import java.util.List;

import CodeGeneration.MipsGenerator;
import CodeGeneration.MipsGenerator.EGENERATOR;
import General.TigerFileHandler.EOUTPUT;
import IRGeneration.Generator;
import Lexer.Lexer;
import Parser.ParseTreeNode;
import Parser.Parser;
import SemanticChecking.Checker;
import SemanticChecking.SymbolTable;

public class Runner {
	public static void main(String[] args) {
		if(Configuration.FORCED_LOAD_FILE != null){
			args = new String[]{Configuration.FORCED_LOAD_FILE};
		}
		Lexer lexer = new Lexer();
		Parser parser = new Parser();
		Checker checker = new Checker();
		Generator gen = new Generator();
		MipsGenerator mips = new MipsGenerator();
		try(TigerFileHandler tfh = new TigerFileHandler(args)){
			tfh.redirectConsole();
			List<Token> tokens = lexer.lex(tfh.getInput());
			if(Configuration.PRINT_TOKENS){
				for(Token token : tokens){
					tfh.print(EOUTPUT.TOKENS, token.token + " ");	
				}
				tfh.println(EOUTPUT.TOKENS);
			}
			if(lexer.errors().isEmpty()){
				ParseTreeNode tree = parser.parse(tokens);
				if(Configuration.PRINT_TREE){
					if(parser.errors().isEmpty()){
						tfh.println(EOUTPUT.TREE, "successful parse");
						tfh.println(EOUTPUT.TREE, tree.toString());
					}  else {
						tfh.println(EOUTPUT.TREE, "unsuccessful parse");				
					}
				}
				if(parser.errors().isEmpty()){
					SymbolTable table = checker.check(tree);
					if(Configuration.PRINT_TABLE){
						tfh.println(EOUTPUT.TABLE, table.toString());
					}
					if(checker.errors().isEmpty()){
						List<IRInstruction> ir = gen.generate(tree, table);
						if(Configuration.PRINT_IR){
							for(IRInstruction instruction : ir){
								tfh.println(EOUTPUT.IR, instruction.toString());
							}
						}
						if(Configuration.PRINT_MIPS_NAIVE){
							List<String> code = mips.generate(EGENERATOR.NAIVE, ir, table);
							for(String instruction : code){
								tfh.println(EOUTPUT.MIPS_NAIVE, instruction.toString());
							}
						}
						if(Configuration.PRINT_MIPS_BB){
							List<String> code = mips.generate(EGENERATOR.BB, ir, table);
							for(String instruction : code){
								tfh.println(EOUTPUT.MIPS_BB, instruction.toString());
							}
						}
						if(Configuration.PRINT_MIPS_EBB){
							List<String> code = mips.generate(EGENERATOR.EBB, ir, table);
							for(String instruction : code){
								tfh.println(EOUTPUT.MIPS_EBB, instruction.toString());
							}
						}
					} else {
						tfh.println(EOUTPUT.TABLE, "semantic error");
					}
				}
			}
			if(printErrors(tfh, "Lexer", lexer.errors())){
				if(printErrors(tfh, "Parser", parser.errors())){
					printErrors(tfh, "Semantic", checker.errors());		
				}
			}
		} catch(IOException e){
			System.out.println(e.getMessage());
		}
	}

	private static boolean printErrors(TigerFileHandler tfh, String type, List<? extends Object> errors){
		tfh.println(EOUTPUT.ERROR, type + " Errors:");
		if(errors.isEmpty()){
			tfh.println(EOUTPUT.ERROR, "No errors");
			return true;
		} else {
			for(Object error : errors){
				tfh.println(EOUTPUT.ERROR, error.toString());
			}
			return false;
		}
	}
}
