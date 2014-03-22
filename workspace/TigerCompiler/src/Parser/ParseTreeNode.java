package Parser;

import java.util.ArrayList;
import java.util.List;

import General.ETERMINAL;
import General.EVARIABLE;


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
	
	public ParseTreeNode addRule(Symbol current, Production rule){
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
	
	public ParseTreeNode addTerminal(Symbol current){
		children.get(index).symbol = current;
		index++;
		ParseTreeNode next = this;
		while(next.index == next.children.size() && next.parent != null){
			next = next.parent;
		}
		return next;
	}
	
	private List<ParseTreeNode> createExpressionTree(List<ParseTreeNode> terminals){
		return terminals;
	}
	
	public void flattenExpressions(){
		if(symbol.equals(EVARIABLE.EXPR)){
			setChildren(createExpressionTree(allTerminals()));
		} else if(symbol.equals(EVARIABLE.STAT_ASSIGN)){
			setChildren(createExpressionTree(allTerminals()));
		} else {
			for(int i = 0; i < children.size();i++){
				children.get(i).flattenExpressions();
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
		} else {
			for(ParseTreeNode child : curr.children){
				allTerminals(child, list);
			}
		}
		return list;
	}
	
//	public ParseTreeNode simplify(){
//		if(children.size() == 1){
//			return children.get(0).simplify();
//		}
//		for(int i = 0; i < children.size();i++){
//			children.set(i, children.get(i).simplify());
//		}
//		return this;
//	}
//	
//	public void simplify2(){
//		if(children.size() > 0){
//			ParseTreeNode lastChild = children.get(children.size() - 1);
//			
//			if(!lastChild.symbol.isTerminal() && lastChild.symbol.getVariable().isTail()){
//				children.remove(children.size() - 1);
//				for(ParseTreeNode grandChild : lastChild.children){
//					children.add(grandChild);
//					grandChild.parent = this;
//				}
//			}
//			for(ParseTreeNode child : children){
//				child.simplify2();
//			}
//		}
//	}
	
	@Override
	public String toString(){
		String params = "";
		if(children != null){
			for(ParseTreeNode child : children){
				params += "\n" + child.toString(" ");
			}
		}
		return "(" + symbol + params + ")";
	}
	
	public String toString(String prefix){
		String params = "";
		if(children != null){
			for(ParseTreeNode child : children){
				params += "\n" + child.toString(prefix + " ");
			}
		}
		return prefix + "(" + symbol + params + ")";
	}
}
