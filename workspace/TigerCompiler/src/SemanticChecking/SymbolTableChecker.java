package SemanticChecking;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import General.Configuration;
import General.ETERMINAL;
import General.EVARIABLE;
import General.Type;
import Parser.ParseTreeNode;

class SymbolTableChecker {
	List<SemanticError> errors;
	private SymbolTable table;
	
	public boolean check(ParseTreeNode tree) {
		errors = new LinkedList<>();
		table = new SymbolTable(errors);
		table.build(tree);
		if(Configuration.PRINT_TABLE){
			System.out.println(table);
		}
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree);
		
		return !errors.isEmpty();
	}
	
	private void traverse(String scope, ParseTreeNode tree){
		if(tree.getSymbol().equals(EVARIABLE.FUNCT_DECLARATION)){
			scope = ScopedName.addScopeToName(scope, tree.getChild(1).getSymbol().getText());
		} else if(tree.getSymbol().equals(EVARIABLE.STAT)){
			if(tree.getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
				handleStatFunction(scope, tree);
			} else if(tree.getSymbol().equals(ETERMINAL.IF)){
				
			} else if(tree.getSymbol().equals(ETERMINAL.WHILE)){
				
			} else if(tree.getSymbol().equals(ETERMINAL.FOR)){
				
			} else if(tree.getSymbol().equals(ETERMINAL.RETURN)){
				
			} else if(tree.getSymbol().equals(ETERMINAL.ID)){
				
			} else if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
				
			} else {
				System.err.println("This should never occur. "
						+ "All cases for stat expressions should be coverd.");
			}
		} else {
			for(ParseTreeNode child : tree.getChildren()){
				traverse(scope, child);
			}	
		}
		
	}

	private void handleStatFunction(String scope, ParseTreeNode tree) {
		int position = tree.getChild(0).getSymbol().getPosition();
		List<Type> typesOfFunctionParams = table.getTypesOfParams(tree.getChild(0).getSymbol().getText(),
				position);
		List<Type> exprListTypes = new LinkedList<>();
		if(!tree.getChild(2).getChildren().isEmpty()){
			exprListTypes.add(typeOfExpr(scope, tree.getChild(2).getChild(0)));
			typesOfExprListTail(scope, exprListTypes, tree.getChild(2).getChild(1));
		}
		if(exprListTypes.size() != typesOfFunctionParams.size()){
			errors.add(new SemanticError("Invalid number of arguments, expected " 
					+ typesOfFunctionParams.size() + " but was " + exprListTypes.size()
					+ " at position " + position));
		} else {
			for(int i = 0; i < typesOfFunctionParams.size();i++){
				if(!typesOfFunctionParams.get(i).equals(exprListTypes.get(i))){
					errors.add(new SemanticError("Type mismatch : expected parameter to be of type "
							+ typesOfFunctionParams.get(i) + " but was " + exprListTypes.get(i)));
				}
			}
		}
	}

	private void typesOfExprListTail(String scope, List<Type> exprListTypes, ParseTreeNode exprListTail) {
		if(!exprListTail.getChildren().isEmpty()){
			exprListTypes.add(typeOfExpr(scope, exprListTail.getChild(1)));
			typesOfExprListTail(scope, exprListTypes, exprListTail.getChild(2));
		}
	}

	private Type typeOfExpr(String scope, ParseTreeNode expr) {
		return typeOfTree(scope, expr.getChild(0));	
	}

	private Type typeOfTree(String scope, ParseTreeNode tree) {
		if(tree.getSymbol().isValue()){
			return typeOfValue(scope, tree);
		} else {
			List<Type> childTypes = new ArrayList<>();
			for(ParseTreeNode child : tree.getChildren()){
				System.out.println(child);
				childTypes.add(typeOfTree(scope, child));
			}
			for(int i = 1; i < childTypes.size();i++){
				if(!childTypes.get(i-1).equals(childTypes.get(i))){
					errors.add(new SemanticError("Type mismatch : operator at position " 
							+ tree.getSymbol().getPosition() 
							+ " only accept the same type for operands"));
				}
			}
			Type type = childTypes.get(0);
			if(!tree.getSymbol().getTerminal().stringAllowed() && type.baseType().equals(Type.STRING)){
				errors.add(new SemanticError("Type mismatch : operator at position " 
						+ tree.getSymbol().getPosition() 
						+ " not allowed for string type"));
			}
			return type;
		}
	}

	private Type typeOfValue(String scope, ParseTreeNode tree) {
		if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
			ParseTreeNode id = tree.getChild(0);
			return table.getTypeOfId(scope, id.getSymbol().getText(), id.getSymbol().getPosition());
		} else if(tree.getSymbol().equals(ETERMINAL.STRLIT)){
			return Type.STRING;
		} else if(tree.getSymbol().equals(ETERMINAL.INTLIT)){
			return Type.INT;
		} else {
			ParseTreeNode id = tree;
			return table.getTypeOfId(scope, id.getSymbol().getText(), id.getSymbol().getPosition());
		}
	}

	
	
	private void traverseStat(String scope, ParseTreeNode tree) {
		if(tree.getSymbol().equals(ETERMINAL.ID)){
			Type typeOfId = table.getTypeOfId(scope, tree.getSymbol().getText(), 
					tree.getSymbol().getPosition());
		}
		// function calls only occur on stat -> id ( expr_list ) ; or  stat -> lvalue := id ( expr_list ) ;
		
		// TODO: need to validate params of function calls
		
		// TODO: validate types of expressions and stat assigns
		// TODO: validate return types to function returns
		
		for(ParseTreeNode child : tree.getChildren()){
			traverseStat(scope, child);
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
