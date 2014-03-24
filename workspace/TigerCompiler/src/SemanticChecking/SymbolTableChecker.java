package SemanticChecking;

import java.util.LinkedList;
import java.util.List;

import General.Configuration;
import Parser.ParseTreeNode;

public class SymbolTableChecker {
	private List<SemanticError> errors;
	
	public boolean check(ParseTreeNode tree) {
		errors = new LinkedList<>();
		SymbolTable table = new SymbolTable(errors);
		table.build(tree);
		if(Configuration.PRINT_TABLE){
			System.out.println(table);
		}//System.out.println(tree);
		//traverse(tree);
		
		return !errors.isEmpty();
	}
	
	private void traverse(ParseTreeNode tree){
		//System.out.println(tree.getSymbol());
		for(ParseTreeNode child : tree.getChildren()){
			traverse(child);
		}
	}
	
	public List<SemanticError> errors() {
		if(errors != null){
			return errors;
		} else {
			return new LinkedList<>();
		}
	}

}
