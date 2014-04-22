package IRGeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static General.EIROPCODE.*;
import General.Configuration;
import General.Cons;
import General.IRInstruction.Operand;
import General.Type;
import General.EIROPCODE;
import General.ETERMINAL;
import General.EVARIABLE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
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
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.PROGRAM_ENTRY_LABEL)));
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
				code.add(new IRInstruction(ASSIGN, 
						new Operand(EOPERAND.VARIABLE, table.getFullNameOfId(scope, id)), 
						new Operand(EOPERAND.LITERAL, "" + type.length()), 
						new Operand(EOPERAND.LITERAL, value)));
			} else {
				code.add(new IRInstruction(ASSIGN, 
						new Operand(EOPERAND.VARIABLE, table.getFullNameOfId(scope, id)), 
						new Operand(EOPERAND.LITERAL, value)));
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
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, qualifiedName)));
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
			Operand expr = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
			code.add(new IRInstruction(RETURN, expr));
		} else if(tree.getChild(0).getSymbol().equals(EVARIABLE.LVALUE)){
			handleStatLvalue(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.BREAK)){
			code.add(new IRInstruction(GOTO, new Operand(EOPERAND.LABEL, breakLabel)));
		} else {
			System.err.println("This should never occur. "
					+ "All cases for stat expressions should be coverd.");
		}
	}

	private void handleIf(String scope, String breakLabel, ParseTreeNode tree,
			SymbolTable table) {
		String ifSuffix = iffi();
		Operand flag = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
		code.add(new IRInstruction(BREQ, 
				flag, 
				new Operand(EOPERAND.LITERAL, "0"), 
				new Operand(EOPERAND.LABEL, Configuration.IF_FALSE + ifSuffix)));
		handleStatSeq(scope, breakLabel, tree.getChild(3), table);
		code.add(new IRInstruction(GOTO, new Operand(EOPERAND.LABEL, Configuration.IF_END + ifSuffix)));
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.IF_FALSE + ifSuffix)));		
		if(tree.getChild(4).getChildren().size() > 1){
			handleStatSeq(scope, breakLabel, tree.getChild(4).getChild(1), table);
		}
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.IF_END + ifSuffix)));
	}

	private void handleStatLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		Operand assigned = new Operand(EOPERAND.VARIABLE, 
				table.getFullNameOfId(scope, tree.getChild(0).getChild(0).getSymbol().getText()));
		Operand value;
		if(tree.getChild(2).getChild(0).getChildren().size() > 1 
				&& tree.getChild(2).getChild(0).getChild(0).getSymbol().equals(ETERMINAL.ID)
				&& tree.getChild(2).getChild(0).getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			value = handleAssignFunction(scope, tree.getChild(2).getChild(0), table);
		} else {
			value = handleExpr(scope, tree.getChild(2).getChild(0).getChild(0), table).a;	
		}
		Type type = table.getTypeOfId(scope, assigned.value);
		if(type.isArray()){
			Cons<Operand, Type> index = buildLvalueIndex(scope, tree.getChild(0), table);
			code.add(new IRInstruction(ARRAY_STORE, assigned, index.a, value));
		} else {
			code.add(new IRInstruction(ASSIGN, assigned, value));
		}		
	}
	
	private void handleWhile(String scope, ParseTreeNode tree, SymbolTable table) {
		String loopSuffix = loop();
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.LOOP_BEGIN + loopSuffix)));
		Operand flag = handleExpr(scope, tree.getChild(1).getChild(0), table).a;
		code.add(new IRInstruction(BREQ, 
				flag, 
				new Operand(EOPERAND.LITERAL, "0"), 
				new Operand(EOPERAND.LABEL, Configuration.LOOP_END + loopSuffix)));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(3), table);
		code.add(new IRInstruction(GOTO, new Operand(EOPERAND.LABEL, Configuration.LOOP_BEGIN + loopSuffix)));
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.LOOP_END + loopSuffix)));
	}
	
	private void handleForLoop(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String loopSuffix = loop();
		Operand initial = handleExpr(scope, tree.getChild(3).getChild(0), table).a;
		Operand counter = new Operand(EOPERAND.VARIABLE, table.getFullNameOfId(scope, tree.getChild(1).getSymbol().getText()));
		code.add(new IRInstruction(ASSIGN, counter, initial));
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.LOOP_BEGIN + loopSuffix)));
		Operand end = handleExpr(scope, tree.getChild(5).getChild(0), table).a;
		code.add(new IRInstruction(BRGEQ, 
				counter, 
				end, 
				new Operand(EOPERAND.LABEL, Configuration.LOOP_END + loopSuffix)));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(7), table);
		code.add(new IRInstruction(ADD, 
				counter, 
				counter,
				new Operand(EOPERAND.LITERAL, "" + 1)));
		code.add(new IRInstruction(GOTO, new Operand(EOPERAND.LABEL, Configuration.LOOP_BEGIN + loopSuffix)));
		code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.LOOP_END + loopSuffix)));
	}

	private void handleStatFunction(String scope, ParseTreeNode tree,
			SymbolTable table) {
		code.add(new IRInstruction(EIROPCODE.CALL, handleParams(scope, tree, table)));
	}

	private Operand handleAssignFunction(String scope, ParseTreeNode tree,
			SymbolTable table) {
		Operand ret = new Operand(EOPERAND.REGISTER, temp());
		List<Operand> params = handleParams(scope, tree, table);
		params.add(0, ret);
		code.add(new IRInstruction(EIROPCODE.CALLR, params));
		return ret;
	}

	private List<Operand> handleParams(String scope, ParseTreeNode tree,
			SymbolTable table) {
		List<Operand> params = new ArrayList<>();
		String functName = tree.getChild(0).getSymbol().getText();
		params.add(new Operand(EOPERAND.LABEL, table.getFullNameOfId(scope, functName)));
		handleParamsHelper(scope, tree, table, functName, params);
		return params;
	}
	
	private void handleParamsHelper(String scope, ParseTreeNode tree,
			SymbolTable table, String functionName, List<Operand> params) {
		Cons<List<Operand>, List<Type>> functionParams = new Cons<List<Operand>, List<Type>>(
				new LinkedList<Operand>(), new LinkedList<Type>());
		handleExprList(scope, tree.getChild(2), table, functionParams);
		List<Operand> functionParamNames = handleRenamingFunctionParams(
				scope, table, functionName, functionParams);
		for(Operand functionParam : functionParamNames){
			params.add(functionParam);
		}
	}

	private List<Operand> handleRenamingFunctionParams(String scope, SymbolTable table, 
			String functionName, Cons<List<Operand>, List<Type>> params) {
		List<String> names = table.matchingParamNames(scope, functionName, params.b);
		List<Operand> operands = new LinkedList<>();
		for(int i = 0; i < names.size();i++){
			operands.add(new Operand(EOPERAND.VARIABLE, names.get(i)));
			code.add(new IRInstruction(ASSIGN, operands.get(i), params.a.get(i)));
		}
		return operands;
	}

	
	private void handleExprList(String scope, ParseTreeNode tree,
			SymbolTable table, Cons<List<Operand>, List<Type>> params) {
		if(!tree.getChildren().isEmpty()){
			if(!tree.getChild(0).getChildren().isEmpty()){
				Cons<Operand, Type> cons = handleExpr(scope, tree.getChild(0).getChild(0), table);
				params.a.add(cons.a);
				params.b.add(cons.b);
			}
			handleExprListTail(scope, tree.getChild(1), table, params);
		}
	}
	
	private void handleExprListTail(String scope, ParseTreeNode tree,
			SymbolTable table, Cons<List<Operand>, List<Type>> params) {
		if(!tree.getChildren().isEmpty()){
			Cons<Operand, Type> cons = handleExpr(scope, tree.getChild(1).getChild(0), table);
			params.a.add(cons.a);
			params.b.add(cons.b);
			handleExprListTail(scope, tree.getChild(2), table, params);
		}
	}


	private Cons<Operand, Type> handleExpr(String scope, ParseTreeNode tree,
			SymbolTable table) {
		if(tree.getSymbol().isValue()){
			if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
				return handleLvalue(scope, tree, table);
			} else if(tree.getSymbol().equals(ETERMINAL.STRLIT)){
				return new Cons<>(new Operand(EOPERAND.LITERAL, tree.getSymbol().getText()), Type.STRING);
			} else if(tree.getSymbol().equals(ETERMINAL.INTLIT)){
				return new Cons<>(new Operand(EOPERAND.LITERAL, tree.getSymbol().getText()), Type.INT);
			} else {
				return new Cons<>(new Operand(EOPERAND.VARIABLE, table.getFullNameOfId(scope, tree.getSymbol().getText())),
						table.getTypeOfId(scope, tree.getSymbol().getText()));
			}
		}
		Operand temp = new Operand(EOPERAND.REGISTER, temp());
		if(tree.getSymbol().equals(ETERMINAL.UMINUS)){
			Cons<Operand, Type> value = handleExpr(scope, tree.getChild(0), table);
			code.add(new IRInstruction(MULT, temp, value.a, new Operand(EOPERAND.LITERAL, "-1")));
			return new Cons<>(temp, Type.INT);
		}
		EIROPCODE opcode = convertOperatorEnum(tree.getSymbol().getTerminal());
		Cons<Operand, Type> left = handleExpr(scope, tree.getChild(0), table);
		if(opcode.equals(AND)){
			code.add(new IRInstruction(ASSIGN, temp, new Operand(EOPERAND.LITERAL, "0")));
			code.add(new IRInstruction(BREQ, 
					left.a, 
					new Operand(EOPERAND.LITERAL, "0"), 
					new Operand(EOPERAND.LABEL, Configuration.SHORT_CIRCUIT + temp)));
		} else if(opcode.equals(OR)){
			code.add(new IRInstruction(ASSIGN, temp, new Operand(EOPERAND.LITERAL, "1")));
			code.add(new IRInstruction(BRNEQ, 
					left.a, 
					new Operand(EOPERAND.LITERAL, "0"), 
					new Operand(EOPERAND.LABEL, Configuration.SHORT_CIRCUIT + temp)));
		}
		Cons<Operand, Type> right = handleExpr(scope, tree.getChild(1), table);
		if(tree.getSymbol().getTerminal().comparisionOperators()){
			code.add(new IRInstruction(opcode, 
					left.a, 
					right.a, 
					new Operand(EOPERAND.LABEL, Configuration.COMPARE_FALSE + temp)));
			code.add(new IRInstruction(ADD, 
					temp, 
					new Operand(EOPERAND.LITERAL, "0"), 
					new Operand(EOPERAND.LITERAL, "1")));
			code.add(new IRInstruction(GOTO, new Operand(EOPERAND.LABEL, Configuration.COMPARE_END + temp)));
			code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.COMPARE_FALSE + temp)));
			code.add(new IRInstruction(ADD, 
					temp, 
					new Operand(EOPERAND.LITERAL, "0"), 
					new Operand(EOPERAND.LITERAL, "0")));
			code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.COMPARE_END + temp)));
		} else {
			code.add(new IRInstruction(opcode, temp, left.a, right.a));
		}
		if(opcode.equals(OR) || opcode.equals(AND)){
			code.add(new IRInstruction(LABEL, new Operand(EOPERAND.LABEL, Configuration.SHORT_CIRCUIT + temp)));
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
			opcode = BRLEQ;
			break;
		case GREATEREQ:
			opcode = BRLT;
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
	
	private Cons<Operand, Type> handleLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String id = tree.getChild(0).getSymbol().getText();
		String ret = temp();
		Cons<Operand, Type> index = buildLvalueIndex(scope, tree, table);
		code.add(new IRInstruction(ARRAY_LOAD, 
				new Operand(EOPERAND.REGISTER, ret), 
				new Operand(EOPERAND.VARIABLE, table.getFullNameOfId(scope, id)), 
				index.a));
		return new Cons<>(new Operand(EOPERAND.REGISTER, ret), index.b);
	}

	private Cons<Operand, Type> buildLvalueIndex(String scope, ParseTreeNode tree,
			SymbolTable table) {
		int numExpr = (tree.getChildren().size() - 1) / 3;
		String id = tree.getChild(0).getSymbol().getText();
		List<Integer> dimensions = table.getTypeOfId(scope, id).totalDimensions();
		Operand index = new Operand(EOPERAND.REGISTER, temp());
		Operand mult = new Operand(EOPERAND.REGISTER, temp());
		code.add(new IRInstruction(ASSIGN, 
				index, 
				new Operand(EOPERAND.LITERAL, "" + 0)));
		for(int i = 0 ; i < numExpr;i++){
			int length = 1;
			for(int j = i + 1; j < dimensions.size();j++){
				length *= dimensions.get(j);
			}
			Operand exprIndex = handleExpr(scope, tree.getChild(2 + i * 3).getChild(0), table).a;
			if(length != 1){
				code.add(new IRInstruction(MULT, 
						mult, 
						exprIndex, 
						new Operand(EOPERAND.LITERAL, "" + length)));
				code.add(new IRInstruction(ADD, index, index, mult));
			} else {
				code.add(new IRInstruction(ADD, index, index, exprIndex));				
			}
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
