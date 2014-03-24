package SemanticChecking;

import java.util.LinkedList;
import java.util.List;

import Parser.ParseTreeNode;

public class Checker {
	private SymbolTableChecker checker;

	public boolean check(ParseTreeNode tree){
		checker = new SymbolTableChecker();
		return checker.check(tree);
	}
	
	public List<SemanticError> errors(){
		if(checker != null){
			return checker.errors();
		} else {
			return new LinkedList<>();
		}
	}
}
