package SemanticChecking;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import General.ETERMINAL;
import General.EVARIABLE;
import Parser.ParseTreeNode;

class SymbolTable {
	HashMap<String, Type> types;
	HashMap<String, ScopedName> names;
	HashMap<String, List<Type>> functionParams;
	List<SemanticError> errors;
	
	public SymbolTable(List<SemanticError> errors){
		types = new HashMap<>();
		names = new HashMap<>();
		functionParams = new HashMap<>();
		this.errors = errors;
	}
	
	public void build(ParseTreeNode root){
		ParseTreeNode declarationSegment = root.getChildren().get(1);
		ParseTreeNode typeDeclarationList = declarationSegment.getChildren().get(0);
		ParseTreeNode varDeclarationList = declarationSegment.getChildren().get(1);
		ParseTreeNode functDeclarationList = declarationSegment.getChildren().get(2);
		buildTypes(typeDeclarationList);
		buildVariables(varDeclarationList);
		buildFunctions(functDeclarationList);
	}
		
	private void buildFunctions(ParseTreeNode functDeclarationList) {
		if(!functDeclarationList.getChildren().isEmpty()){
			ParseTreeNode functDeclaration = functDeclarationList.getChild(0);
			String functName = functDeclaration.getChild(1).getSymbol().getText();
			ParseTreeNode retType = functDeclaration.getChild(5);
			Type type = null;
			if(!retType.getChildren().isEmpty()){
				type = getTypeId(retType.getChild(1));
			}
			List<Type> paramTypes = new LinkedList<>();
			if(!functDeclaration.getChild(3).getChildren().isEmpty()){
				ParseTreeNode param = functDeclaration.getChild(3).getChild(0);
				handleParam(functName, paramTypes, param);
				buildParams(functName, paramTypes, functDeclaration.getChild(3).getChild(1));
			}
			if(names.containsKey(functName)){
				errors.add(new SemanticError("Redeclared name " + functName 
						+ " in global scope at position " 
						+ functDeclaration.getChild(0).getSymbol().getPosition()));
			} else {
				names.put(functName, new ScopedName(true, functName, type));
				functionParams.put(functName, paramTypes);
			}
			buildFunctions(functDeclarationList.getChild(1));
		}
	}

	private void buildParams(String functName, List<Type> paramTypes, ParseTreeNode paramListTail) {
		if(!paramListTail.getChildren().isEmpty()){
			ParseTreeNode param = paramListTail.getChild(1);
			handleParam(functName, paramTypes, param);
			buildParams(functName, paramTypes, paramListTail.getChild(2));
		}
	}

	private void handleParam(String functName, List<Type> paramTypes,
			ParseTreeNode param) {
		String paramName = ScopedName.createScopedName(functName, param.getChild(0).getSymbol().getText()); 
		Type paramType = getTypeId(param.getChild(2));
		if(names.containsKey(paramName)){
			errors.add(new SemanticError("Redeclared name " + paramName + " at position " 
					+ param.getChild(0).getSymbol().getPosition()));
		} else {
			names.put(paramName, new ScopedName(false, paramName, paramType));
			paramTypes.add(paramType);
		}
	}

	private void buildVariables(ParseTreeNode varDeclarationList) {
		if(!varDeclarationList.getChildren().isEmpty()){
			ParseTreeNode varDeclaration = varDeclarationList.getChild(0);
			List<String> ids = new LinkedList<>(); 
			ids.add(varDeclaration.getChild(1).getChild(0).getSymbol().getText());
			buildIdList(ids, varDeclaration.getChild(1).getChild(1));
			Type type = getTypeId(varDeclaration.getChild(3));
			for(String id : ids){
				if(names.containsKey(id)){
					errors.add(new SemanticError("Redeclared name " + id  + " in global scope at position " 
							+ varDeclaration.getChild(0).getSymbol().getPosition()));
				} else {
					names.put(id, new ScopedName(false, id, type));
				}
			}
			if(!varDeclaration.getChild(4).getChildren().isEmpty()){
				int position = varDeclaration.getChild(4).getChild(1).getChild(0).getSymbol().getPosition();
				if(varDeclaration.getChild(4).getChild(1).getChild(0).getSymbol().equals(ETERMINAL.STRLIT)){
					if(!type.equals(Type.STRING)){
						errors.add(new SemanticError("Type mismatch : expected " 
								+ type + " but was " + Type.STRING + " at position " + position));
					}
				} else {
					if(!type.equals(Type.INT)){
						errors.add(new SemanticError("Type mismatch : expected "
								+ type + " but was " + Type.INT + " at position " + position));
					}
				}
			}
			buildVariables(varDeclarationList.getChild(1));
		}
	}

	private void buildIdList(List<String> ids, ParseTreeNode idListTail) {
		if(!idListTail.getChildren().isEmpty()){
			ids.add(idListTail.getChild(1).getSymbol().getText());
			buildIdList(ids, idListTail.getChild(2));
		}
	}

	public void buildTypes(ParseTreeNode tree){
		if(!tree.getChildren().isEmpty()){
			ParseTreeNode typeDeclaration = tree.getChildren().get(0);
			String typeName = typeDeclaration.getChildren().get(1).getSymbol().getText();
			int position = typeDeclaration.getChildren().get(0).getSymbol().getPosition();
			if(types.containsKey(typeName)){
				errors.add(new SemanticError("Redeclared type " + typeName + " near position " 
				+ position));
				
			} else {
				ParseTreeNode typeLit = typeDeclaration.getChildren().get(3);
				if(typeLit.getChildren().get(0).getSymbol().equals(EVARIABLE.TYPE_ID)){
					types.put(typeName, new Type(typeName, getTypeId(typeLit.getChildren().get(0))));
				} else {
					List<Integer> dimensions = new LinkedList<Integer>();
					dimensions.add(Integer.parseInt(typeLit.getChildren().get(2).getSymbol().getText()));
					buildDimensions(dimensions, typeLit.getChildren().get(4));
					Type type = getTypeId(typeLit.getChildren().get(6));
					types.put(typeName, new Type(typeName, type, dimensions));
				}
			}
			buildTypes(tree.getChildren().get(1));
		}
	}
	
	private void buildDimensions(List<Integer> dimensions, ParseTreeNode typeDim) {
		if(!typeDim.getChildren().isEmpty()){
			dimensions.add(Integer.parseInt(typeDim.getChild(1).getSymbol().getText()));
			buildDimensions(dimensions, typeDim.getChild(3));
		}
	}

	private Type getTypeId(ParseTreeNode typeId){
		String typeDef = typeId.getChild(0).getSymbol().getText();
		if(typeDef.equals("int")){
			return Type.INT;
		} else if(typeDef.equals("string")) {
			return Type.STRING;
		} else if(types.containsKey(typeDef)){
			return types.get(typeDef).type;
		} else {
			errors.add(new SemanticError("Undefined type " + typeDef 
					+ " near position " + typeId.getChild(0).getSymbol().getPosition()));
			return null;
		}
	}
}
