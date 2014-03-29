package IRGeneration;

import java.util.LinkedList;
import java.util.List;

import General.Configuration;
import General.EVARIABLE;
import Parser.ParseTreeNode;
import SemanticChecking.SymbolTable;

public class IRGenerator {
	private List<String> code;
	
	public IRGenerator(){
		code = new LinkedList<>();
	}
	
	public List<String> generate(ParseTreeNode tree, SymbolTable table){
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree, table);
		return code;
	}

	private void traverse(String scope, ParseTreeNode tree, SymbolTable table) {
		if(tree.getSymbol().equals(EVARIABLE.FUNCT_DECLARATION)){
			//System.out.println(tree);
		} else {
			for(ParseTreeNode child : tree.getChildren()){
				traverse(scope, child, table);
			}
		}
	}
}
