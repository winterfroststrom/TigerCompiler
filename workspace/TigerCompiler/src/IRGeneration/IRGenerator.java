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
	
	public IRGenerator(){
		code = new LinkedList<>();
	}
	
	public List<IRInstruction> generate(ParseTreeNode tree, SymbolTable table){
		traverse(Configuration.GLOBAL_SCOPE_NAME, tree.getChild(1), table);
		code.add(new IRInstruction(LABEL, "main"));
		handleStatSeq(Configuration.GLOBAL_SCOPE_NAME, tree.getChild(3), table);
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
		code.add(new IRInstruction(LABEL, table.getFullNameOfId(scope, functName)));
		scope = ScopedName.addScopeToName(scope, functName);
		handleStatSeq(scope, tree.getChild(7), table);
		code.add(new IRInstruction(RETURN));		
	}

	private void handleStatSeq(String scope, ParseTreeNode tree,
			SymbolTable table) {
		if(!tree.getChildren().isEmpty()){
			handleStat(scope, tree.getChild(0), table);
			handleStatSeq(scope, tree.getChild(1), table);
		}
	}

	private void handleStat(String scope, ParseTreeNode tree, SymbolTable table) {
		//System.out.println(tree);
		if(tree.getChild(1).getSymbol().equals(ETERMINAL.LPAREN)){
			handleStatFunction(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.IF)){
			//handleIf(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.WHILE)){
			//handleWhile(scope, tree);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.FOR)){
			handleForLoop(scope, tree, table);
		} else if(tree.getChild(0).getSymbol().equals(ETERMINAL.RETURN)){
			String expr = handleExpr(scope, tree.getChild(1).getChild(0), table);
			code.add(new IRInstruction(RETURN, expr));
		} else if(tree.getChild(0).getSymbol().equals(EVARIABLE.LVALUE)){
			handleStatLvalue(scope, tree, table);
		} else {
			System.err.println("This should never occur. "
					+ "All cases for stat expressions should be coverd.");
		}
	}

	private void handleStatLvalue(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String assigned = tree.getChild(0).getChild(0).getSymbol().getText();
		String value = handleExpr(scope, tree.getChild(2).getChild(0).getChild(0), table);
		Type type = table.getTypeOfId(scope, assigned);
		if(type.isArray()){
			// TODO: support arrays	
		} else {
			code.add(new IRInstruction(ASSIGN, table.getFullNameOfId(scope, assigned), value));
		}		
	}

	private void handleForLoop(String scope, ParseTreeNode tree,
			SymbolTable table) {
		String loopName = temp();
		String loopEndName = temp();
		code.add(new IRInstruction(LABEL, loopName));
		String initial = handleExpr(scope, tree.getChild(3).getChild(0), table);
		String end = handleExpr(scope, tree.getChild(5).getChild(0), table);
		String counter = tree.getChild(1).getSymbol().getText();
		code.add(new IRInstruction(ASSIGN, counter, initial));
		code.add(new IRInstruction(BRGEQ, counter, end, loopEndName));
		handleStatSeq(scope, tree.getChild(7), table);
		code.add(new IRInstruction(ADD, counter, "" + 1));
		code.add(new IRInstruction(GOTO, loopName));
		code.add(new IRInstruction(LABEL, loopEndName));
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
			throw new UnsupportedOperationException();
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
		int numExpr = (tree.getChildren().size() - 1) / 3;
		List<Integer> dimensions = table.getTypeOfId(scope, id).totalDimensions();
		// TODO: support multi-dimensional arrays
		String ret = temp();
		String index = temp();
		code.add(new IRInstruction(ASSIGN, index, "" + 0));
		for(int i = 0 ; i < numExpr;i++){
			String exprIndex = handleExpr(scope, tree.getChild(2 + i * 3).getChild(0), table);
			code.add(new IRInstruction(ADD, index, index, exprIndex));
		}
		code.add(new IRInstruction(ARRAY_LOAD, ret, id, index));
		return ret;
	}

	private String temp(){
		return "t" + tempCounter++;
	}
}
