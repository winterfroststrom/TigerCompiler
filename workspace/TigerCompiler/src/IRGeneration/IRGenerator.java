package IRGeneration;

import java.util.LinkedList;
import java.util.List;

import static General.EIROPCODE.*;
import General.Configuration;
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
			String expr = handleExpr(scope, tree.getChild(1).getChild(0), table);
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
		String ifSuffix = temp();
		String flag = handleExpr(scope, tree.getChild(1).getChild(0), table);
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
		String value = handleExpr(scope, tree.getChild(2).getChild(0).getChild(0), table);
		Type type = table.getTypeOfId(scope, assigned);
		if(type.isArray()){
			System.out.println(tree);
			String index = buildLvalueIndex(scope, tree.getChild(0), table);
			code.add(new IRInstruction(ARRAY_STORE, table.getFullNameOfId(scope, assigned), index, value));
		} else {
			code.add(new IRInstruction(ASSIGN, table.getFullNameOfId(scope, assigned), value));
		}		
	}
	
	private void handleWhile(String scope, ParseTreeNode tree, SymbolTable table) {
		String loopSuffix = loop();
		code.add(new IRInstruction(LABEL, Configuration.LOOP_BEGIN + loopSuffix));
		String flag = handleExpr(scope, tree.getChild(1).getChild(0), table);
		code.add(new IRInstruction(BREQ, flag, "0", Configuration.LOOP_END + loopSuffix));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(3), table);
		code.add(new IRInstruction(LABEL, Configuration.LOOP_END + loopSuffix));
	}
	private void handleForLoop(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String loopSuffix = loop();
		String initial = handleExpr(scope, tree.getChild(3).getChild(0), table);
		String end = handleExpr(scope, tree.getChild(5).getChild(0), table);
		String counter = tree.getChild(1).getSymbol().getText();
		code.add(new IRInstruction(ASSIGN, counter, initial));
		code.add(new IRInstruction(LABEL, Configuration.LOOP_BEGIN + loopSuffix));
		code.add(new IRInstruction(BRGEQ, counter, end, Configuration.LOOP_END + loopSuffix));
		handleStatSeq(scope, Configuration.LOOP_END + loopSuffix, tree.getChild(7), table);
		code.add(new IRInstruction(ADD, counter, "" + 1));
		code.add(new IRInstruction(GOTO, Configuration.LOOP_END + loopSuffix));
		code.add(new IRInstruction(LABEL, Configuration.LOOP_END + loopSuffix));
	}

	private void handleStatFunction(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String functName = tree.getChild(0).getSymbol().getText();
		List<String> params = new LinkedList<>();
		params.add(table.getFullNameOfId(scope, functName));
		handleExprList(scope, tree.getChild(2), table, params);
		code.add(new IRInstruction(EIROPCODE.CALL, params));
	}

	
	private void handleExprList(String scope, ParseTreeNode tree,
			SymbolTable table, List<String> params) {
		if(!tree.getChildren().isEmpty()){
			params.add(handleExpr(scope, tree.getChild(0).getChild(0), table));
			handleExprList(scope, tree.getChild(1), table, params);
		}
	}


	private String handleExpr(String scope, ParseTreeNode tree,
			SymbolTable table) {
		if(tree.getSymbol().isValue()){
			if(tree.getSymbol().equals(EVARIABLE.LVALUE)){
				return handleLvalue(scope, tree, table);
			} else if(tree.getSymbol().equals(ETERMINAL.STRLIT) 
					|| tree.getSymbol().equals(ETERMINAL.INTLIT)){
				return tree.getSymbol().getText();
			} else {
				return table.getFullNameOfId(scope, tree.getSymbol().getText());
			}
		}
		String temp = temp();
		if(tree.getSymbol().equals(ETERMINAL.UMINUS)){
			String value = handleExpr(scope, tree.getChild(0), table);
			code.add(new IRInstruction(MULT, temp, value, "-1"));
			return temp;
		}
		
		String left = handleExpr(scope, tree.getChild(0), table);
		String right = handleExpr(scope, tree.getChild(1), table);
		if(tree.getSymbol().getTerminal().comparisionOperators()){
			EIROPCODE opcode;
			switch(tree.getSymbol().getTerminal()){
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
			default:
				opcode = null;
				break;
			}
			code.add(new IRInstruction(opcode, left, right, Configuration.IF_FALSE + temp));
			code.add(new IRInstruction(ADD, temp, "0", "1"));
			code.add(new IRInstruction(GOTO, Configuration.IF_END + temp));
			code.add(new IRInstruction(LABEL, Configuration.IF_FALSE + temp));
			code.add(new IRInstruction(ADD, temp, "0", "0"));
			code.add(new IRInstruction(LABEL, Configuration.IF_END + temp));
			return temp;
		} else {
			EIROPCODE opcode;
			switch(tree.getSymbol().getTerminal()){
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
			code.add(new IRInstruction(opcode, temp, left, right));
			return temp;
		}
	}
	
	private String handleLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String id = tree.getChild(0).getSymbol().getText();
		String ret = temp();
		String index = buildLvalueIndex(scope, tree, table);
		code.add(new IRInstruction(ARRAY_LOAD, ret, id, index));
		return ret;
	}

	private String buildLvalueIndex(String scope, ParseTreeNode tree,
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
			String exprIndex = handleExpr(scope, tree.getChild(2 + i * 3).getChild(0), table);
			code.add(new IRInstruction(MULT, mult, exprIndex, "" + length));
			code.add(new IRInstruction(ADD, index, index, mult));
		}
		return index;
	}

	private String loop(){
		return "" + loopCounter++;
	}
	
	private String temp(){
		return Configuration.TEMP_PREFIX + tempCounter++;
	}
}
