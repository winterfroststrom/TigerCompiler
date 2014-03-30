package IRGeneration;

import java.util.List;

import General.IRInstruction;
import Parser.ParseTreeNode;
import SemanticChecking.SymbolTable;

public class Generator {
	public List<IRInstruction> generate(ParseTreeNode tree, SymbolTable table){
		return (new IRGenerator()).generate(tree, table);
	}
}
