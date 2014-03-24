package Parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import General.Configuration;
import General.ETERMINAL;
import General.EVARIABLE;
import General.Symbol;

public class ParseTreeNode {
	private ParseTreeNode parent;
	private Symbol symbol;
	private List<ParseTreeNode> children;
	private int index;
	
	public ParseTreeNode(ParseTreeNode parent){
		this(parent, null);
	}
	
	public ParseTreeNode(ParseTreeNode parent, Symbol symbol){
		this.symbol = symbol;
		this.parent = parent;
		this.children = new ArrayList<>();
	}
	
	ParseTreeNode addRule(Symbol current, Production rule){
		if(symbol == null){
			symbol = current;
			for(Symbol s : rule){
				children.add(new ParseTreeNode(this, s));
			}
			return this;
		} else {
			ParseTreeNode curr = children.get(index);
			curr.symbol = current;
			if(rule.size() > 0){
				for(Symbol s : rule){
					curr.children.add(new ParseTreeNode(curr, s));
				}
				index++;
				return curr;
			} else {
				index++;
				ParseTreeNode next = this;
				while(next.index == next.children.size()){
					next = next.parent;
				}
				return next;
			}
		}
	}
	
	ParseTreeNode addTerminal(Symbol current){
		children.get(index).symbol = current;
		index++;
		ParseTreeNode next = this;
		while(next.index == next.children.size() && next.parent != null){
			next = next.parent;
		}
		return next;
	}
	
	private List<ParseTreeNode> createExpressionTree(List<ParseTreeNode> terminals){
		for(ParseTreeNode terminal : terminals){
			terminal.setChildren(new ArrayList<ParseTreeNode>());
		}
		if(terminals.size() < 2){
			return terminals;
		} else if(terminals.get(0).symbol.equals(ETERMINAL.ID) 
				&& terminals.get(1).symbol.equals(ETERMINAL.LPAREN)){
			return createExpressionTreeFunction(terminals);
		} else {
			return shuntingYardAlgorithm(handleLvalue(terminals));
		}
	}

	private List<ParseTreeNode> handleLvalue(List<ParseTreeNode> terminals) {
		List<ParseTreeNode> newTerminals = new ArrayList<>();
		int index = 0;
		while(index < terminals.size() - 2){
			while(index < terminals.size() - 2 
					&& !(terminals.get(index).symbol.equals(ETERMINAL.ID) 
							&& terminals.get(index + 1).symbol.equals(ETERMINAL.LBRACK))){
				newTerminals.add(terminals.get(index++));
			}
			if(index < terminals.size() - 2){		
				ParseTreeNode lvalue = new ParseTreeNode(this, new Symbol(EVARIABLE.LVALUE));
				List<ParseTreeNode> lvalueChildren = new ArrayList<>();
				lvalueChildren.add(terminals.get(index++));
				
				while(index < terminals.size() && terminals.get(index).symbol.equals(ETERMINAL.LBRACK)){
					lvalueChildren.add(terminals.get(index++));
					ParseTreeNode lvalueExpr = new ParseTreeNode(lvalue, new Symbol(EVARIABLE.EXPR));
					List<ParseTreeNode> lvalueExprChildren = new ArrayList<>();
					int lbracks = 0;
					while(index < terminals.size()){
						if(lbracks == 0 && terminals.get(index).symbol.equals(ETERMINAL.RBRACK)){
							break;
						} else if(terminals.get(index).symbol.equals(ETERMINAL.RBRACK)){
							lbracks--;
							lvalueExprChildren.add(terminals.get(index++));
						} else if(terminals.get(index).symbol.equals(ETERMINAL.LBRACK)){
							lbracks++;
							lvalueExprChildren.add(terminals.get(index++));
						} else {
							lvalueExprChildren.add(terminals.get(index++));
						}
					}
					lvalueExpr.setChildren(lvalueExpr.createExpressionTree(lvalueExprChildren));
					lvalueChildren.add(lvalueExpr);
					lvalueChildren.add(terminals.get(index++));	
				}
				
				lvalue.setChildren(lvalueChildren);
				newTerminals.add(lvalue);
			}
		}
		while(index < terminals.size()){
			newTerminals.add(terminals.get(index++));
		}
		return newTerminals;
	}
	
	private List<ParseTreeNode> shuntingYardAlgorithm(List<ParseTreeNode> nodes){
		List<ParseTreeNode> stack = new ArrayList<>();
		List<ParseTreeNode> postfix = new ArrayList<>();
		for(int i = 0; i < nodes.size();i++){
			if(nodes.get(i).symbol.isValue()){
				postfix.add(nodes.get(i));
			} else {
				if(nodes.get(i).symbol.equals(ETERMINAL.MINUS) 
						&& (i == 0 || (nodes.get(i - 1).symbol.isTerminal() 
								&& nodes.get(i - 1).symbol.getTerminal().isOperator()))){
					nodes.get(i).symbol.changeMinusToUMinus();			
				}
				if(stack.isEmpty()){
					stack.add(nodes.get(i));
				} else if(nodes.get(i).symbol.equals(ETERMINAL.RPAREN)){
					while(!stack.get(stack.size() - 1).symbol.equals(ETERMINAL.LPAREN)){
						postfix.add(stack.remove(stack.size() - 1));
					}
					stack.remove(stack.size() - 1);
				} else if(nodes.get(i).symbol.equals(ETERMINAL.LPAREN)){
					stack.add(nodes.get(i));					
				} else {
					int precedence = nodes.get(i).symbol.getTerminal().precedence();
					while(!stack.isEmpty() && 
							stack.get(stack.size() - 1).symbol.getTerminal().precedence() <= precedence
							&& !stack.get(stack.size() - 1).symbol.equals(ETERMINAL.LPAREN)){
						postfix.add(stack.remove(stack.size() - 1));
					}
					stack.add(nodes.get(i));
				}
			}
		}
		while(!stack.isEmpty()){
			postfix.add(stack.remove(stack.size() - 1));
		}
		return convertFromPostFixToTree(postfix);
	}
	
	private List<ParseTreeNode> convertFromPostFixToTree(List<ParseTreeNode> postfix){
		List<ParseTreeNode> stack = new ArrayList<>();
		for(int i = 0; i < postfix.size();i++){
			if(!postfix.get(i).symbol.isValue()){
				int operands = postfix.get(i).symbol.getTerminal().operands();
				List<ParseTreeNode> operand = new ArrayList<>();
				for(int j = 0; j < operands;j++){
					operand.add(0, stack.remove(stack.size() - 1));
				}
				postfix.get(i).setChildren(operand);
			}
			stack.add(postfix.get(i));
		}
		return stack;
	}
	
	private List<ParseTreeNode> createExpressionTreeFunction(List<ParseTreeNode> terminals) {
		List<ParseTreeNode> newExpr = new ArrayList<>();
		List<ParseTreeNode> params = new ArrayList<>();
		newExpr.add(terminals.get(0)); // id
		newExpr.add(terminals.get(1)); // (
		
		for(int i = 2; i < terminals.size() - 1; i++){
			params.add(terminals.get(i));
		}
		List<List<ParseTreeNode>> paramExpr = new ArrayList<>();
		List<ParseTreeNode> commas = new LinkedList<>();
		paramExpr.add(new ArrayList<ParseTreeNode>());
		int index = 0;
		for(ParseTreeNode node : params){
			if(node.symbol.equals(ETERMINAL.COMMA)){
				paramExpr.add(new ArrayList<ParseTreeNode>());
				index++;
				commas.add(node);
			} else {
				paramExpr.get(index).add(node);
			}
		}
		ParseTreeNode exprList = new ParseTreeNode(this, new Symbol(EVARIABLE.EXPR_LIST));
		newExpr.add(exprList);
		for(List<ParseTreeNode> param : paramExpr){
			ParseTreeNode para = new ParseTreeNode(this, new Symbol(EVARIABLE.EXPR));
			List<ParseTreeNode> exprListChildren = new ArrayList<>();
			para.setChildren(createExpressionTree(param));
			if(exprList.symbol.equals(EVARIABLE.EXPR_LIST_TAIL)){
				exprListChildren.add(commas.remove(0));
				exprListChildren.add(para);
				exprListChildren.add(new ParseTreeNode(exprList, new Symbol(EVARIABLE.EXPR_LIST_TAIL)));
				exprList.setChildren(exprListChildren);
				exprList = exprList.getChild(2);
			} else {
				exprListChildren.add(para);
				exprListChildren.add(new ParseTreeNode(exprList, new Symbol(EVARIABLE.EXPR_LIST_TAIL)));
				exprList.setChildren(exprListChildren);
				exprList = exprList.getChild(1);	
			}
		}
		newExpr.add(terminals.get(terminals.size() - 1)); // )
		return newExpr;
	}
	
	public void flattenExpressions(){
		associativeExpressions();
		removeStatFuncOrAssign();
	}
		
	private void removeStatFuncOrAssign(){
		//stat -> id stat_func_or_assign ;
		//stat_func_or_assign -> ( expr_list )
		//stat_func_or_assign -> lvalue_tail := stat_assign
		if(symbol.equals(EVARIABLE.STAT) && children.get(0).symbol.equals(ETERMINAL.ID)){
			ParseTreeNode statFuncOrAssign = children.get(1);
			if(children.get(1).children.get(0).symbol.equals(ETERMINAL.LPAREN)){
				List<ParseTreeNode> newChildren = new ArrayList<>();
				newChildren.add(children.get(0)); // add id
				for(ParseTreeNode child : statFuncOrAssign.children){
					newChildren.add(child); // add ( expr_list )
				}
				newChildren.add(children.get(2)); // add ;
				setChildren(newChildren);
			} else {
				List<ParseTreeNode> newChildren = new ArrayList<>();
				
				ParseTreeNode lvalue = new ParseTreeNode(this, new Symbol(EVARIABLE.LVALUE));
				List<ParseTreeNode> newLvalueChildren = new ArrayList<>();
				newLvalueChildren.add(children.get(0)); // add id
				ParseTreeNode lvalueTail = statFuncOrAssign.getChild(0);
				while(!lvalueTail.children.isEmpty()){
					newLvalueChildren.add(lvalueTail.children.get(0)); // add lvalue_tail.[
					newLvalueChildren.add(lvalueTail.children.get(1)); // add lvalue_tail.expr
					newLvalueChildren.add(lvalueTail.children.get(2)); // add lvalue_tail.]
					lvalueTail = lvalueTail.children.get(3);
				}
				lvalue.setChildren(newLvalueChildren);
				newChildren.add(lvalue); // add lvalue
				newChildren.add(statFuncOrAssign.children.get(1)); // add :=
				newChildren.add(statFuncOrAssign.children.get(2)); // add stat_assign
				newChildren.add(children.get(2)); // add ;
				setChildren(newChildren);
			}
		} else {
			for(int i = 0; i < children.size();i++){
				children.get(i).removeStatFuncOrAssign();
			}
		}
	}
	
	private void associativeExpressions() {
		if(symbol.equals(EVARIABLE.EXPR)){
			setChildren(createExpressionTree(allTerminals()));
		} else if(symbol.equals(EVARIABLE.STAT_ASSIGN)){
			List<ParseTreeNode> expr = new ArrayList<>();
			expr.add(new ParseTreeNode(this, new Symbol(EVARIABLE.EXPR)));
			expr.get(0).setChildren(createExpressionTree(allTerminals()));
			setChildren(expr);
		} else {
			for(int i = 0; i < children.size();i++){
				children.get(i).associativeExpressions();
			}
		}
	}
	
	private void setChildren(List<ParseTreeNode> newChildren){
		children = newChildren;
		index = children.size();
		for(ParseTreeNode child : children){
			child.parent = this;
		}
	}
	
	public List<ParseTreeNode> allTerminals(){
		return allTerminals(this, new ArrayList<ParseTreeNode>());
	}
	
	private List<ParseTreeNode> allTerminals(ParseTreeNode curr, List<ParseTreeNode> list){
		if(curr.symbol.isTerminal()){
			list.add(curr);
		}
		for(ParseTreeNode child : curr.children){
			allTerminals(child, list);
		}
		return list;
	}
	
	public List<ParseTreeNode> getChildren(){
		return children;
	}
	
	public ParseTreeNode getChild(int index){
		return children.get(index);
	}
	
	public Symbol getSymbol(){
		return symbol;
	}
	
	@Override
	public String toString(){
		return toString("");
	}
	
	private String toString(String prefix){
		String params = "";
		if(children != null){
			for(ParseTreeNode child : children){
				params += Configuration.PARSE_TREE_NODE_DELIMITER + child.toString(prefix + Configuration.PARSE_TREE_NODE_PREFIX);
			}
		}
		return prefix + "(" + symbol + params + ")";
	}
}
