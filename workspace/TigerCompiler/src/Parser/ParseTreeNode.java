package Parser;

import java.util.ArrayList;


public class ParseTreeNode {
	private ParseTreeNode parent;
	private Symbol symbol;
	private ArrayList<ParseTreeNode> children;
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
