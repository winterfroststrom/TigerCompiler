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
		}
		if(tree.getSymbol().equals(EVARIABLE.STAT)){
			if(tree.getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
				handleStatFunction(scope, tree);
			} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.IF)){
				Type childType = typeOfExpr(scope, tree.getChild(1));
				int position = tree.getChild(0).getSymbol().getPosition();
				if(!Type.INT.equals(typeOfExpr(scope, tree.getChild(1)))){
					errors.add(new SemanticError(
							"Type mismatch : expected int in if statement condition but was "
							+ childType + " at position " + position));
				}
				for(ParseTreeNode child : tree.getChildren()){
					traverse(scope, child);
				}
			} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.WHILE)){
				Type childType = typeOfExpr(scope, tree.getChild(1));
				int position = tree.getChild(0).getSymbol().getPosition();
				if(!Type.INT.equals(typeOfExpr(scope, tree.getChild(1)))){
					errors.add(new SemanticError(
							"Type mismatch : expected int in while statement condition but was "
							+ childType + " at position " + position));
				}
				for(ParseTreeNode child : tree.getChildren()){
					traverse(scope, child);
				}	
			} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.FOR)){
				handleForLoop(scope, tree);
			} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.RETURN)){
				// TODO
			} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.ID)){
				System.out.println(tree);
			} else if(tree.getChild(0).getSymbol().equals(EVARIABLE.LVALUE)){
				ParseTreeNode id = tree.getChild(0).getChild(0);
				Type lvalueType = typeOfValue(scope, tree.getChild(0));
				if(tree.getChild(2).getChild(0).getSymbol().equals(EVARIABLE.EXPR)){
					Type exprType = typeOfExpr(scope, tree.getChild(2).getChild(0));
					if(!lvalueType.equals(exprType)){
						errors.add(new SemanticError("Type mismatch : assignment from expression expected "
								+ lvalueType + " but was " + exprType + " at position "
								+ id.getSymbol().getPosition()));
					}	
				} else {
					Type functionType = handleStatFunction(scope, tree.getChild(2));
					if(!lvalueType.equals(functionType)){
						errors.add(new SemanticError("Type mismatch : assignment from function expected "
								+ lvalueType + " but was " + functionType + " at position "
								+ id.getSymbol().getPosition()));
					}	
				}
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

	private void handleForLoop(String scope, ParseTreeNode tree) {
		int position = tree.getChild(0).getSymbol().getPosition();
		Type idType = typeOfValue(scope, tree.getChild(1));
		if(!idType.baseType().equals(Type.INT)){
			errors.add(new SemanticError("Type mismatch : need int compatible type in for loop "
					+ "at position " + position));
		}
		Type expr1Type = typeOfExpr(scope, tree.getChild(3));
		Type expr2Type = typeOfExpr(scope, tree.getChild(5));
		if(!(idType.equals(expr1Type) && expr1Type.equals(expr2Type))){
			if(!(tree.getChild(3).getChild(0).getSymbol().equals(ETERMINAL.INTLIT)
					&& tree.getChild(5).getChild(0).getSymbol().equals(ETERMINAL.INTLIT))){
				errors.add(new SemanticError(
						"Type mismatch : need int compatible type in for loop interval "
						+ "at position " + position));	
			}	
		}
		for(ParseTreeNode child : tree.getChildren()){
			traverse(scope, child);
		}
	}

	private Type handleStatFunction(String scope, ParseTreeNode tree) {
		int position = tree.getChild(0).getSymbol().getPosition();
		List<Type> typesOfFunctionParams = table.getTypesOfParams(tree.getChild(0).getSymbol().getText(),
				position);
		List<Type> exprListTypes = new LinkedList<>();
		if(!tree.getChild(2).getChildren().isEmpty()){
			if(!tree.getChild(2).getChild(0).getChildren().isEmpty()){
				exprListTypes.add(typeOfExpr(scope, tree.getChild(2).getChild(0)));
				typesOfExprListTail(scope, exprListTypes, tree.getChild(2).getChild(1));
			}
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
		return table.getTypeOfId(scope, tree.getChild(0).getSymbol().getText(), position);
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
			if(!tree.getSymbol().getTerminal().comparisionOperators() && type.baseType().equals(Type.STRING)){
				errors.add(new SemanticError("Type mismatch : operator at position " 
						+ tree.getSymbol().getPosition() 
						+ " not allowed for string type"));
			}
			if(tree.getSymbol().getTerminal().comparisionOperators()){
				return Type.INT;
			} else {
				return type;
			}
		}
	}

	private Type typeOfValue(String scope, ParseTreeNode tree) {
		if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
			ParseTreeNode id = tree.getChild(0);
			int numIndices = (tree.getChildren().size() - 1) / 3;
			Type arrayType = table.getTypeOfId(scope, id.getSymbol().getText(), id.getSymbol().getPosition());
			if(numIndices == 0){
				return arrayType;	
			} else if(numIndices == arrayType.dimensions.size()) {
				return arrayType.type;
			} else {
				errors.add(new SemanticError("Indices must be used in full or not at all at position "
						+ id.getSymbol().getPosition()));
				return arrayType;
			}
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
		
		// TODO: validate return types to function returns
//		 Function return values and types must agree
//		lvalues indexing
		
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
