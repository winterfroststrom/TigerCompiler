package IRGeneration;

import java.util.List;

import Parser.ParseTreeNode;
import SemanticChecking.SymbolTable;

public class Generator {
	public List<String> generate(ParseTreeNode tree, SymbolTable table){
		return (new IRGenerator()).generate(tree, table);
	}
}
