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
	
	public SymbolTable check(ParseTreeNode tree) {
		errors = new LinkedList<>();
		table = new SymbolTable(errors);
		table.build(tree);
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree);
		return table;
	}
	
	private void traverse(String scope, ParseTreeNode tree){
		if(tree.getSymbol().equals(EVARIABLE.FUNCT_DECLARATION)){
			String functionName = tree.getChild(1).getSymbol().getText();
			Type functionType = table.getTypeOfId(scope, functionName, 
					tree.getChild(1).getSymbol().getPosition());
			scope = ScopedName.addScopeToName(scope, tree.getChild(1).getSymbol().getText());
			if(functionType != null){
				Type returnType = returnType(scope, tree.getChild(7));
				if(returnType == null || !functionType.equals(returnType)){
					errors.add(new SemanticError("Type mismatch : function " + functionName 
							+ " expects a return of "
							+ functionType + " but was " + returnType +
							" at position " + tree.getChild(0).getSymbol().getPosition()));
				}
			}
		} else if(tree.getSymbol().equals(EVARIABLE.STAT)){
			handleStat(scope, tree);
		} else if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
			int index = 2;
			while(index < tree.getChildren().size()){
				Type exprType = typeOfExpr(scope, tree.getChild(index));
				if(!Type.INT.equals(exprType)){
						errors.add(new SemanticError("Type mismatch, expected int but was "
							+ exprType + " for index of lvalue at position " 
							+ tree.getChild(0).getSymbol().getPosition()));
				}
				index += 3;
			}
		}
		for(ParseTreeNode child : tree.getChildren()){
			traverse(scope, child);
		}	
	}

	private Type returnType(String scope, ParseTreeNode tree) {
		List<Type> types = new LinkedList<>();	
		if(!tree.getChildren().isEmpty() && tree.getChild(0).getSymbol().equals(ETERMINAL.RETURN)){
			return typeOfExpr(scope, tree.getChild(1));
		}
		for(ParseTreeNode child : tree.getChildren()){
			types.add(returnType(scope, child));
		}
		List<Type> ret = new ArrayList<>();
		for(Type t : types){
			if(t != null){
				ret.add(t);
			}
		}
		for(int i = 1; i < ret.size();i++){
			if(!ret.get(0).equals(ret.get(1))){
				errors.add(new SemanticError("Type mismatch : function at position "
						+ tree.allTerminals().get(0).getSymbol().getPosition()
						+ " has multiple return types"));
			}
		}
		if(ret.size() > 0){
			return ret.get(0);
		} else {
			return null;
		}
	}

	private void handleStat(String scope, ParseTreeNode tree) {
		if(tree.getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			handleStatFunction(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.IF)){
			handleIf(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.WHILE)){
			handleWhile(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.FOR)){
			handleForLoop(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.RETURN)){
			// Handled in returnType
		} else if(tree.getChild(0).getSymbol().equals(EVARIABLE.LVALUE)){
			handleStatLvalue(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.BREAK)){
			// no type checking necessary
		} else {
			System.err.println("This should never occur. "
					+ "All cases for stat expressions should be coverd.");
		}
	}

	private void handleWhile(String scope, ParseTreeNode tree) {
		Type childType = typeOfExpr(scope, tree.getChild(1));
		int position = tree.getChild(0).getSymbol().getPosition();
		if(!Type.INT.equals(typeOfExpr(scope, tree.getChild(1)))){
			errors.add(new SemanticError(
					"Type mismatch : expected int in while statement condition but was "
					+ childType + " at position " + position));
		}
	}

	private void handleIf(String scope, ParseTreeNode tree) {
		Type childType = typeOfExpr(scope, tree.getChild(1));
		int position = tree.getChild(0).getSymbol().getPosition();
		if(!Type.INT.equals(typeOfExpr(scope, tree.getChild(1)))){
			errors.add(new SemanticError(
					"Type mismatch : expected int in if statement condition but was "
					+ childType + " at position " + position));
		}
	}

	private void handleStatLvalue(String scope, ParseTreeNode tree) {
		ParseTreeNode id = tree.getChild(0).getChild(0);
		Type lvalueType = typeOfValue(scope, tree.getChild(0));
		if(tree.getChild(2).getChild(0).getChildren().size() > 1 
				&& tree.getChild(2).getChild(0).getChild(0).getSymbol().equals(ETERMINAL.ID)
				&& tree.getChild(2).getChild(0).getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			Type functionType = handleStatFunction(scope, tree.getChild(2).getChild(0));
			if(!lvalueType.equals(functionType)){
				errors.add(new SemanticError("Type mismatch : assignment from function expected "
						+ lvalueType + " but was " + functionType + " at position "
						+ id.getSymbol().getPosition()));
			}	
		} else {
			Type exprType = typeOfExpr(scope, tree.getChild(2).getChild(0));
			if(!lvalueType.equals(exprType)){
				errors.add(new SemanticError("Type mismatch : assignment from expression expected "
						+ lvalueType + " but was " + exprType + " at position "
						+ id.getSymbol().getPosition()));
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
	}

	private Type handleStatFunction(String scope, ParseTreeNode tree) {
		String functName = tree.getChild(0).getSymbol().getText();
		int position = tree.getChild(0).getSymbol().getPosition();
		List<Type> exprListTypes = new LinkedList<>();
		if(!tree.getChild(2).getChildren().isEmpty()){
			if(!tree.getChild(2).getChild(0).getChildren().isEmpty()){
				exprListTypes.add(typeOfExpr(scope, tree.getChild(2).getChild(0)));
				typesOfExprListTail(scope, exprListTypes, tree.getChild(2).getChild(1));
			}
		}
		if(!table.containsFunctionAndParams(scope, tree.getChild(0).getSymbol().getText(), exprListTypes)){
			errors.add(new SemanticError("Type mismatch : expected parameters to be ones of types "
					+ table.getParams(scope, functName, position) + " but was " + exprListTypes
					+ " for function " +  functName 
					+ " at position " + position));
			
		}
		return table.getTypeOfId(scope, functName, position);
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
			for(Type t : childTypes){
				if(t != null && !t.isConstant()){
					type = t;
				}
			}
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
			Type arrayType = table.getTypeOfId(scope, id.getSymbol().getText(), 
					id.getSymbol().getPosition());
			Type dereferencedType = arrayType.dereference(numIndices);
			if(dereferencedType == null){
				errors.add(new SemanticError("Indices must be used in full or not at all at position "
						+ id.getSymbol().getPosition()));
				return arrayType;
			} else {
				return dereferencedType;
			}
		} else if(tree.getSymbol().equals(ETERMINAL.STRLIT)){
			return Type.CSTRING;
		} else if(tree.getSymbol().equals(ETERMINAL.INTLIT)){
			return Type.CINT;
		} else {
			ParseTreeNode id = tree;
			return table.getTypeOfId(scope, id.getSymbol().getText(), id.getSymbol().getPosition());
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
