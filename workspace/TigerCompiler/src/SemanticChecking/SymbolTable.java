package SemanticChecking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import General.Configuration;
import General.Cons;
import General.ETERMINAL;
import General.EVARIABLE;
import General.Type;
import Parser.ParseTreeNode;

public class SymbolTable {
	private HashMap<String, Type> types;
	private HashMap<String, ScopedName> names;
	private HashMap<String, List<Cons<List<Type>, List<String>>>> functionParams;
	
	List<SemanticError> errors;
	
	public SymbolTable(List<SemanticError> errors){
		types = new HashMap<>();
		types.put("int", Type.INT);
		types.put("string", Type.STRING);
		names = new HashMap<>();
		functionParams = new HashMap<>();
		addFunction("print", null, Type.STRING);
		addFunction("printi", null, Type.INT);
		addFunction("flush", null);
		addFunction("getchar", Type.STRING);
		addFunction("ord", Type.INT, Type.STRING);
		addFunction("chr", Type.STRING, Type.INT);
		addFunction("size", Type.INT, Type.STRING);
		addFunction("substring", Type.STRING, Type.STRING, Type.INT, Type.INT);
		addFunction("concat", Type.STRING, Type.STRING, Type.STRING);
		addFunction("not", Type.INT, Type.INT);
		addFunction("exit", null, Type.INT);

		
		this.errors = errors;
	}
	
	private void addFunction(String name, Type retType, Type... types){
		name = ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, name);
		names.put(name, new ScopedName(true, name, retType));
		List<Type> typeList = new LinkedList<>();
		List<String> nameList = new LinkedList<>();
		int i = 0;
		for(Type t : types){
			typeList.add(t);
			nameList.add(ScopedName.addScopeToName(name, "param" + i++));
		}
		putFunctionParams(name, typeList, nameList);
	}
	
	public void build(ParseTreeNode root){
		ParseTreeNode declarationSegment = root.getChildren().get(1);
		if(!declarationSegment.getChildren().isEmpty()){
			ParseTreeNode typeDeclarationList = declarationSegment.getChildren().get(0);
			ParseTreeNode varDeclarationList = declarationSegment.getChildren().get(1);
			ParseTreeNode functDeclarationList = declarationSegment.getChildren().get(2);
			buildTypes(typeDeclarationList);
			buildVariables(varDeclarationList);
			buildFunctions(functDeclarationList);
		}
	}
		
	private void buildFunctions(ParseTreeNode functDeclarationList) {
		if(!functDeclarationList.getChildren().isEmpty()){
			ParseTreeNode functDeclaration = functDeclarationList.getChild(0);
			String functName = ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, 
					functDeclaration.getChild(1).getSymbol().getText());
			ParseTreeNode retType = functDeclaration.getChild(5);
			Type type = null;
			if(!retType.getChildren().isEmpty()){
				type = getTypeId(retType.getChild(1));
			}
			List<Type> paramTypes = new LinkedList<>();
			List<String> paramNames = new LinkedList<>();
			
			if(!functDeclaration.getChild(3).getChildren().isEmpty()){
				ParseTreeNode param = functDeclaration.getChild(3).getChild(0);
				handleParam(functName, paramTypes, paramNames, param);
				buildParams(functName, paramTypes, paramNames, functDeclaration.getChild(3).getChild(1));
			}
			if(names.containsKey(functName)){
//			if(containsFunctionAndParamsHelper(functName, paramTypes)){
				errors.add(new SemanticError("Redeclared name " + functName 
						+ " in global scope at position " 
						+ functDeclaration.getChild(0).getSymbol().getPosition()));
			} else {
				names.put(functName, new ScopedName(true, functName, type));
				putFunctionParams(functName, paramTypes, paramNames);
			}
			boolean hasReturn = false;
			for(ParseTreeNode terminal : functDeclaration.getChild(7).allTerminals()){
				if(terminal.getSymbol().equals(ETERMINAL.RETURN)){
					hasReturn = true;
					break;
				}
			}
			if(type == null && hasReturn){
				errors.add(new SemanticError("Function " + functName 
						+ " returns when it does not have return type at position " 
						+ functDeclaration.getChild(0).getSymbol().getPosition()));
			} else if(type != null && !hasReturn){				
				errors.add(new SemanticError("Function " + functName 
						+ " does not returns when it has return type at position " 
						+ functDeclaration.getChild(0).getSymbol().getPosition()));
			}
			
			buildFunctions(functDeclarationList.getChild(1));
		}
	}

	boolean containsFunctionAndParams(String scope, String name, List<Type> params){
		return containsFunctionAndParamsHelper(ScopedName.addScopeToName(
					Configuration.GLOBAL_SCOPE_NAME, name), params) 
				|| containsFunctionAndParamsHelper(ScopedName.addScopeToName(scope, name), params);
	}
	
	private boolean containsFunctionAndParamsHelper(String name, List<Type> params){
		if(names.containsKey(name)){
			for(Cons<List<Type>, List<String>> cons : functionParams.get(name)){
				List<Type> possibleTypes = cons.a;
				if(possibleTypes.size() != params.size()){
					continue;
				} else {
					boolean same = true; 
					for(int i = 0; i < possibleTypes.size() && same;i++){
						if(!possibleTypes.get(i).baseType().equals(params.get(i).baseType())){
							same = false;
						}
					}
					if(same){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void putFunctionParams(String functName, List<Type> paramTypes, List<String> names) {
		if(!functionParams.containsKey(functName)){
			functionParams.put(functName, new LinkedList<Cons<List<Type>, List<String>>>());
		}
		functionParams.get(functName).add(new Cons<>(paramTypes, names));
	}

	private void buildParams(String functName, List<Type> paramTypes, List<String> paramNames, 
			ParseTreeNode paramListTail) {
		if(!paramListTail.getChildren().isEmpty()){
			ParseTreeNode param = paramListTail.getChild(1);
			handleParam(functName, paramTypes, paramNames, param);
			buildParams(functName, paramTypes, paramNames, paramListTail.getChild(2));
		}
	}

	private void handleParam(String functName, List<Type> paramTypes,
			List<String> paramNames, ParseTreeNode param) {
		String paramName = ScopedName.addScopeToName(functName, param.getChild(0).getSymbol().getText()); 
		Type paramType = getTypeId(param.getChild(2));
		if(names.containsKey(paramName)){
			errors.add(new SemanticError("Redeclared name " + paramName + " at position " 
					+ param.getChild(0).getSymbol().getPosition()));
		} else {
			names.put(paramName, new ScopedName(false, paramName, paramType));
			paramTypes.add(paramType);
			paramNames.add(paramName);
		}
	}

	private void buildVariables(ParseTreeNode varDeclarationList) {
		if(!varDeclarationList.getChildren().isEmpty()){
			ParseTreeNode varDeclaration = varDeclarationList.getChild(0);
			List<String> ids = buildIdList(varDeclaration.getChild(1));
			Type type = getTypeId(varDeclaration.getChild(3));
			for(String id : ids){
				id = ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id);
				if(names.containsKey(id)){
					errors.add(new SemanticError("Redeclared name " + id  + " in global scope at position " 
							+ varDeclaration.getChild(0).getSymbol().getPosition()));
				} else {
					names.put(id, new ScopedName(false, id, type));
				}
			}
			optionalInitTypeCheck(varDeclaration.getChild(4), type);
			buildVariables(varDeclarationList.getChild(1));
		}
	}

	private void optionalInitTypeCheck(ParseTreeNode optionalInit, Type type) {
		if(!optionalInit.getChildren().isEmpty()){
			int position = optionalInit.getChild(1).getChild(0).getSymbol().getPosition();
			if(optionalInit.getChild(1).getChild(0).getSymbol().equals(ETERMINAL.STRLIT)){
				if(!type.arrayBaseType().equals(Type.STRING)){	
					errors.add(new SemanticError("Type mismatch : expected " 
							+ type + " but was " + Type.STRING + " in optional init at position " + position));
				}
			} else {
				if(!type.arrayBaseType().equals(Type.INT)){	
					errors.add(new SemanticError("Type mismatch : expected "
							+ type + " but was " + Type.INT + " in optional init at position " + position));
				}
			}
		}
	}
	
	private List<String> buildIdList(ParseTreeNode idList){
		List<String> ids = new LinkedList<>(); 
		ids.add(idList.getChild(0).getSymbol().getText());
		buildIdList(ids, idList.getChild(1));
		return ids;
	}
	
	private void buildIdList(List<String> ids, ParseTreeNode idListTail) {
		if(!idListTail.getChildren().isEmpty()){
			ids.add(idListTail.getChild(1).getSymbol().getText());
			buildIdList(ids, idListTail.getChild(2));
		}
	}

	private void buildTypes(ParseTreeNode tree){
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
					types.put(typeName, new Type(typeName, getTypeId(typeLit.getChild(0))));
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
			return types.get(typeDef);
		} else {
			errors.add(new SemanticError("Undefined type " + typeDef 
					+ " near position " + typeId.getChild(0).getSymbol().getPosition()));
			return null;
		}
	}
	
	@Override
	public String toString(){
		return "Symbol Table :\nTypes : " + types 
				+ "\nNames : " + names 
				+ "\nKinds : " + functionParams;
	}

	Type getTypeOfId(String scope, String id, int position) {
		if(names.containsKey(ScopedName.addScopeToName(scope, id))){
			ScopedName name = names.get(ScopedName.addScopeToName(scope, id));
			return name.type;
		} else if(names.containsKey(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id))){
			ScopedName name = names.get(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id));
			return name.type;
		} else {
			errors.add(new SemanticError("Missing variable " + id  
					+ " in scope of " + scope + " at position " + position));
			return Configuration.DEFAULT_TYPE_ON_ERROR;
		}
	}

	public boolean isFunction(String qualifiedId){
		return names.containsKey(qualifiedId) && names.get(qualifiedId).function;
	}

	public List<String> functionParamNames(String qualifiedId){
		if(isFunction(qualifiedId)){
			List<String> allNames = new LinkedList<>();
			for(Cons<List<Type>, List<String>> params : functionParams.get(qualifiedId)){
				for(String param : params.b){
					allNames.add(param);
				}
			}
			return allNames;
		}
		return null;
	}

	public Set<String> allFunctionParamNames(){
		Set<String> allNames = new HashSet<>();
		for(String function : functionParams.keySet()){
			allNames.addAll(functionParamNames(function));
		}
		return allNames;
	}
	
	public Type getTypeOfQualifiedId(String id){
		if(names.containsKey(id)){
			return names.get(id).type;
		} else {
			return null;
		}
	}
	
	public Type getTypeOfId(String scope, String id){
		if(names.containsKey(id)){
			return names.get(id).type;
		} else if(names.containsKey(ScopedName.addScopeToName(scope, id))){
			ScopedName name = names.get(ScopedName.addScopeToName(scope, id));
			return name.type;
		} else if(names.containsKey(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id))){
			ScopedName name = names.get(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id));
			return name.type;
		} else {
			return null;
		}
	}
	
	public String getFullNameOfId(String scope, String id) {
		if(names.containsKey(ScopedName.addScopeToName(scope, id))){
			return ScopedName.addScopeToName(scope, id);
		} else if(names.containsKey(ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id))){
			return ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, id);
		} else {
			return Configuration.MISSING_NAME;
		}
	}
	
	public List<Type> typesOfList(String scope, List<String> ids){
		List<Type> types = new LinkedList<>();
		for(String id : ids){
			types.add(getTypeOfId(scope, id));
		}
		return types;
	}
	
	public List<String> matchingParamNames(String scope, String name, List<Type> paramTypes){
		List<String> names = matchingParamNamesHelper(ScopedName.addScopeToName(
				Configuration.GLOBAL_SCOPE_NAME, name), paramTypes);
		if(names != null){
			return names; 
		}
		return matchingParamNamesHelper(ScopedName.addScopeToName(scope, name), paramTypes);
	}
	
	private List<String> matchingParamNamesHelper(String name, List<Type> paramTypes){
		if(names.containsKey(name)){
			for(Cons<List<Type>, List<String>> cons : functionParams.get(name)){
				List<Type> possibleTypes = cons.a;
				if(possibleTypes.size() != paramTypes.size()){
					continue;
				} else {
					boolean same = true;
					for(int i = 0; i < possibleTypes.size() && same;i++){
						if(!possibleTypes.get(i).baseType().equals(paramTypes.get(i).baseType())){
							same = false;
						}
					}
					if(same){
						return cons.b;
					}
				}
			}
		}
		return null;
	}
	
	List<Cons<List<Type>, List<String>>> getParams(String scope, String name, int position) {
		name = ScopedName.addScopeToName(Configuration.GLOBAL_SCOPE_NAME, name);
		if(functionParams.containsKey(name)){
			return functionParams.get(name);
		} else {
			errors.add(new SemanticError("Undefined function " + name + " at position " + position ));
			return new LinkedList<>();
		}
	}
}
