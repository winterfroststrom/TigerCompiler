package IRGeneration;

import java.util.LinkedList;
import java.util.List;

import static General.EIROPCODE.*;
import General.Configuration;
import General.Cons;
import General.Type;
import General.EIROPCODE;
import General.ETERMINAL;
import General.EVARIABLE;
import General.IRInstruction;
import Parser.ParseTreeNode;
import SemanticChecking.ScopedName;
import SemanticChecking.SymbolTable;

public class IRGenerator {
	private List<IRInstruction> code;
	private int tempCounter;
	private int loopCounter;
	private int ifCounter;
	
	
	public IRGenerator(){
		code = new LinkedList<>();
	}
	
	public List<IRInstruction> generate(ParseTreeNode tree, SymbolTable table){
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree.getChild(1), table);
		code.add(new IRInstruction(LABEL, Configuration.PROGRAM_ENTRY_LABEL));
		handleStatSeq(Configuration.GLOBAL_SCOPE_NAME, Configuration.PROGRAM_ENTRY_LABEL, 
				tree.getChild(3), table);
		return code;
	}

	private void traverse(String scope, ParseTreeNode tree, SymbolTable table) {
		if(tree.getSymbol().equals(EVARIABLE.VAR_DECLARATION)){
			handleVarDeclaration(scope, tree, table);
		} else if(tree.getSymbol().equals(EVARIABLE.FUNCT_DECLARATION)){
			handleFunctionDeclaration(scope, tree, table);
		} else {
			for(ParseTreeNode child : tree.getChildren()){
				traverse(scope, child, table);
			}
		}
	}

	private void handleVarDeclaration(String scope, ParseTreeNode tree,
			SymbolTable table) {
		List<String> ids = buildIdList(tree.getChild(1));
		ParseTreeNode optionalInit = tree.getChild(4);
		String value = "0";
		if(!optionalInit.getChildren().isEmpty()){
			value = optionalInit.getChild(1).getChild(0).getSymbol().getText();
		}
		for(String id : ids){
			Type type = table.getTypeOfId(scope, id);
			if(type.isArray()){
				code.add(new IRInstruction(ASSIGN, table.getFullNameOfId(scope, id), 
						"" + type.length(), value));
			} else {
				code.add(new IRInstruction(ASSIGN, table.getFullNameOfId(scope, id), value));
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
	
	private void handleFunctionDeclaration(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String functName = tree.getChild(1).getSymbol().getText();
		String qualifiedName = table.getFullNameOfId(scope, functName);
		code.add(new IRInstruction(LABEL, qualifiedName));
		scope = ScopedName.addScopeToName(scope, functName);
		handleStatSeq(scope, qualifiedName, tree.getChild(7), table);
		code.add(new IRInstruction(RETURN));		
	}

	private void handleStatSeq(String scope, String breakLabel, ParseTreeNode tree,
			SymbolTable table) {
		if(!tree.getChildren().isEmpty()){
			handleStat(scope, breakLabel, tree.getChild(0), table);
			handleStatSeq(scope, breakLabel, tree.getChild(1), table);
		}
	}

	private void handleStat(String scope, String breakLabel, ParseTreeNode tree, SymbolTable table) {
		if(tree.getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			handleStatFunction(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.IF)){
			handleIf(scope, breakLabel, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.WHILE)){
			handleWhile(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.FOR)){
			handleForLoop(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.RETURN)){
			String expr = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
			code.add(new IRInstruction(RETURN, expr));
		} else if(tree.getChild(0).getSymbol().equals(EVARIABLE.LVALUE)){
			handleStatLvalue(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.BREAK)){
			code.add(new IRInstruction(GOTO, breakLabel));
		} else {
			System.err.println("This should never occur. "
					+ "All cases for stat expressions should be coverd.");
		}
	}

	private void handleIf(String scope, String breakLabel, ParseTreeNode tree,
			SymbolTable table) {
		String ifSuffix = iffi();
		String flag = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
		code.add(new IRInstruction(BREQ, flag, "0", Configuration.IF_FALSE + ifSuffix));
		handleStatSeq(scope, breakLabel, tree.getChild(3), table);
		code.add(new IRInstruction(GOTO, Configuration.IF_END + ifSuffix));
		code.add(new IRInstruction(LABEL, Configuration.IF_FALSE + ifSuffix));		
		if(tree.getChild(4).getChildren().size() > 1){
			handleStatSeq(scope, breakLabel, tree.getChild(4).getChild(1), table);
		}
		code.add(new IRInstruction(LABEL, Configuration.IF_END + ifSuffix));
	}

	private void handleStatLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String assigned = tree.getChild(0).getChild(0).getSymbol().getText();
		String value;
		if(tree.getChild(2).getChild(0).getChildren().size() > 1 
				&& tree.getChild(2).getChild(0).getChild(0).getSymbol().equals(ETERMINAL.ID)
				&& tree.getChild(2).getChild(0).getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			value = handleAssignFunction(scope, tree.getChild(2).getChild(0), table);
		} else {
			value = handleExpr(scope, tree.getChild(2).getChild(0).getChild(0), table).a;	
		}
		Type type = table.getTypeOfId(scope, assigned);
		if(type.isArray()){
			Cons<String, Type> index = buildLvalueIndex(scope, tree.getChild(0), table);
			code.add(new IRInstruction(ARRAY_STORE, table.getFullNameOfId(scope, assigned), index.a, value));
		} else {
			code.add(new IRInstruction(ASSIGN, table.getFullNameOfId(scope, assigned), value));
		}		
	}
	
	private void handleWhile(String scope, ParseTreeNode tree, SymbolTable table) {
		String loopSuffix = loop();
		code.add(new IRInstruction(LABEL, Configuration.LOOP_BEGIN + loopSuffix));
		String flag = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
		code.add(new IRInstruction(BREQ, flag, "0", Configuration.LOOP_END + loopSuffix));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(3), table);
		code.add(new IRInstruction(GOTO, Configuration.LOOP_BEGIN + loopSuffix));
		code.add(new IRInstruction(LABEL, Configuration.LOOP_END + loopSuffix));
	}
	
	private void handleForLoop(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String loopSuffix = loop();
		String initial = handleExpr(scope, tree.getChild(3).getChild(0), table).a;
		String counter = tree.getChild(1).getSymbol().getText();
		code.add(new IRInstruction(ASSIGN, counter, initial));
		code.add(new IRInstruction(LABEL, Configuration.LOOP_BEGIN + loopSuffix));
		String end = handleExpr(scope, tree.getChild(5).getChild(0), table).a;
		code.add(new IRInstruction(BRGEQ, counter, end, Configuration.LOOP_END + loopSuffix));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(7), table);
		code.add(new IRInstruction(ADD, counter, "" + 1));
		code.add(new IRInstruction(GOTO, Configuration.LOOP_BEGIN + loopSuffix));
		code.add(new IRInstruction(LABEL, Configuration.LOOP_END + loopSuffix));
	}

	private void handleStatFunction(String scope, ParseTreeNode tree,
			SymbolTable table) {
		code.add(new IRInstruction(EIROPCODE.CALL, handleParams(scope, tree, table)));
	}

	private String handleAssignFunction(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String ret = temp();
		List<String> params = handleParams(scope, tree, table);
		params.add(0, ret);
		code.add(new IRInstruction(EIROPCODE.CALLR, params));
		return ret;
	}

	private List<String> handleParams(String scope, ParseTreeNode tree,
			SymbolTable table) {
		List<String> params = new LinkedList<>();
		String functName = tree.getChild(0).getSymbol().getText();
		params.add(table.getFullNameOfId(scope, functName));
		handleParamsHelper(scope, tree, table, functName, params);
		return params;
	}
	
	private void handleParamsHelper(String scope, ParseTreeNode tree,
			SymbolTable table, String functionName, List<String> params) {
		Cons<List<String>, List<Type>> functionParams = new Cons<List<String>, List<Type>>(
				new LinkedList<String>(), new LinkedList<Type>());
		handleExprList(scope, tree.getChild(2), table, functionParams);
		List<String> functionParamNames = handleRenamingFunctionParams(
				scope, table, functionName, functionParams);
		for(String functionParam : functionParamNames){
			params.add(functionParam);
		}
	}

	private List<String> handleRenamingFunctionParams(String scope, SymbolTable table, 
			String functionName, Cons<List<String>, List<Type>> params) {
		List<String> names = table.matchingParamNames(scope, functionName, params.b);
		for(int i = 0; i < names.size();i++){
			code.add(new IRInstruction(ASSIGN, names.get(i), params.a.get(i)));
		}
		return names;
	}

	
	private void handleExprList(String scope, ParseTreeNode tree,
			SymbolTable table, Cons<List<String>, List<Type>> params) {
		if(!tree.getChildren().isEmpty()){
			if(!tree.getChild(0).getChildren().isEmpty()){
				Cons<String, Type> cons = handleExpr(scope, tree.getChild(0).getChild(0), table);
				params.a.add(cons.a);
				params.b.add(cons.b);
			}
			handleExprListTail(scope, tree.getChild(1), table, params);
		}
	}
	
	private void handleExprListTail(String scope, ParseTreeNode tree,
			SymbolTable table, Cons<List<String>, List<Type>> params) {
		if(!tree.getChildren().isEmpty()){
			Cons<String, Type> cons = handleExpr(scope, tree.getChild(1).getChild(0), table);
			params.a.add(cons.a);
			params.b.add(cons.b);
			handleExprListTail(scope, tree.getChild(2), table, params);
		}
	}


	private Cons<String, Type> handleExpr(String scope, ParseTreeNode tree,
			SymbolTable table) {
		if(tree.getSymbol().isValue()){
			if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
				return handleLvalue(scope, tree, table);
			} else if(tree.getSymbol().equals(ETERMINAL.STRLIT)){
				return new Cons<>(tree.getSymbol().getText(), Type.STRING);
			} else if(tree.getSymbol().equals(ETERMINAL.INTLIT)){
				return new Cons<>(tree.getSymbol().getText(), Type.INT);
			} else {
				return new Cons<>(table.getFullNameOfId(scope, tree.getSymbol().getText()),
						table.getTypeOfId(scope, tree.getSymbol().getText()));
			}
		}
		String temp = temp();
		if(tree.getSymbol().equals(ETERMINAL.UMINUS)){
			Cons<String, Type> value = handleExpr(scope, tree.getChild(0), table);
			code.add(new IRInstruction(MULT, temp, value.a, "-1"));
			return new Cons<>(temp, Type.INT);
		}
		EIROPCODE opcode = convertOperatorEnum(tree.getSymbol().getTerminal());
		Cons<String, Type> left = handleExpr(scope, tree.getChild(0), table);
		if(opcode.equals(AND)){
			code.add(new IRInstruction(ASSIGN, temp, "0"));
			code.add(new IRInstruction(BREQ, left.a, "0",Configuration.SHORT_CIRCUIT + temp));
		} else if(opcode.equals(OR)){
			code.add(new IRInstruction(ASSIGN, temp, "1"));
			code.add(new IRInstruction(BRNEQ, left.a, "0", Configuration.SHORT_CIRCUIT + temp));
		}
		Cons<String, Type> right = handleExpr(scope, tree.getChild(1), table);
		if(tree.getSymbol().getTerminal().comparisionOperators()){
			code.add(new IRInstruction(opcode, left.a, right.a, Configuration.COMPARE_FALSE + temp));
			code.add(new IRInstruction(ADD, temp, "0", "1"));
			code.add(new IRInstruction(GOTO, Configuration.COMPARE_END + temp));
			code.add(new IRInstruction(LABEL, Configuration.COMPARE_FALSE + temp));
			code.add(new IRInstruction(ADD, temp, "0", "0"));
			code.add(new IRInstruction(LABEL, Configuration.COMPARE_END + temp));
		} else {
			code.add(new IRInstruction(opcode, temp, left.a, right.a));
		}
		if(opcode.equals(OR) || opcode.equals(AND)){
			code.add(new IRInstruction(LABEL, Configuration.SHORT_CIRCUIT + temp));
			return new Cons<>(temp, Type.INT);
		}
		return new Cons<>(temp, left.b);
	}

	private EIROPCODE convertOperatorEnum(ETERMINAL terminal) {
		EIROPCODE opcode;
		switch(terminal){
		case EQ:
			opcode = BRNEQ;
			break;
		case NEQ:
			opcode = BREQ;
			break;
		case LESS:
			opcode = BRGEQ;
			break;
		case LESSEREQ:
			opcode = BRGT;
			break;
		case GREATER:
			opcode = BRLT;
			break;
		case GREATEREQ:
			opcode = BRLEQ;
			break;
		case PLUS:
			opcode = ADD;
			break;
		case MULT:
			opcode = MULT;
			break;
		case DIV:
			opcode = DIV;
			break;
		case MINUS:
			opcode = SUB;
			break;
		case AND:
			opcode = AND;
			break;
		case OR:
			opcode = OR;
			break;
		default:
			opcode = null;
		}
		return opcode;
	}
	
	private Cons<String, Type> handleLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String id = tree.getChild(0).getSymbol().getText();
		String ret = temp();
		Cons<String, Type> index = buildLvalueIndex(scope, tree, table);
		code.add(new IRInstruction(ARRAY_LOAD, ret, id, index.a));
		return new Cons<>(ret, index.b);
	}

	private Cons<String, Type> buildLvalueIndex(String scope, ParseTreeNode tree,
			SymbolTable table) {
		int numExpr = (tree.getChildren().size() - 1) / 3;
		String id = tree.getChild(0).getSymbol().getText();
		List<Integer> dimensions = table.getTypeOfId(scope, id).totalDimensions();
		String index = temp();
		String mult = temp();
		code.add(new IRInstruction(ASSIGN, index, "" + 0));
		for(int i = 0 ; i < numExpr;i++){
			int length = 1;
			for(int j = i + 1; j < dimensions.size();j++){
				length *= dimensions.get(j);
			}
			String exprIndex = handleExpr(scope, tree.getChild(2 + i * 3).getChild(0), table).a;
			code.add(new IRInstruction(MULT, mult, exprIndex, "" + length));
			code.add(new IRInstruction(ADD, index, index, mult));
		}
		return new Cons<>(index, table.getTypeOfId(scope, id).dereference(numExpr));
	}

	private String loop(){
		return "" + loopCounter++;
	}
	
	private String iffi(){
		return "" + ifCounter++;
	}
	
	private String temp(){
		return Configuration.TEMP_PREFIX + tempCounter++;
	}
}
