package SemanticChecking;

import java.util.LinkedList;
import java.util.List;

import General.Configuration;
import General.ETERMINAL;
import General.EVARIABLE;
import Parser.ParseTreeNode;

public class SymbolTableChecker {
	private List<SemanticError> errors;
	
	public boolean check(ParseTreeNode tree) {
		errors = new LinkedList<>();
		SymbolTable table = new SymbolTable(errors);
		table.build(tree);
		if(Configuration.PRINT_TABLE){
			System.out.println(table);
		}
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree, table);
		
		return !errors.isEmpty();
	}
	
	private void traverse(String scope, ParseTreeNode tree, SymbolTable table){
		if(tree.getSymbol().equals(EVARIABLE.FUNCT_DECLARATION)){
			scope = ScopedName.addScopeToName(scope, tree.getChild(1).getSymbol().getText());
		}
		if(tree.getSymbol().equals(EVARIABLE.STAT)){
			traverseStat(scope, tree, table);
		} else {
			for(ParseTreeNode child : tree.getChildren()){
				traverse(scope, child, table);
			}	
		}
		
	}

	private void traverseStat(String scope, ParseTreeNode tree,
			SymbolTable table) {
		if(tree.getSymbol().equals(ETERMINAL.ID)){
			Type typeOfId = getIdType(scope, tree, table);
		}
		// function calls only occur on stat -> id ( expr_list ) ; or  stat -> lvalue := id ( expr_list ) ;
		// TODO: need to validate params of function calls
		
		// TODO: validate types of expressions and stat assigns
		// TODO: validate return types to function returns
		
		for(ParseTreeNode child : tree.getChildren()){
			traverseStat(scope, child, table);
		}
	}

	private Type getIdType(String scope, ParseTreeNode tree, SymbolTable table) {
		String id = tree.getSymbol().getText();
		if(table.names.containsKey(ScopedName.addScopeToName(scope, id))){
			return table.names.get(ScopedName.addScopeToName(scope, id)).type;
		} else if(table.names.containsKey(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id))){
			return table.names.get(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id)).type; 
		} else {
			errors.add(new SemanticError("Missing variable " + id  
					+ " in scope of " + scope + " at position " + tree.getSymbol().getPosition()));
			return Configuration.DEFAULT_TYPE_ON_ERROR;
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
