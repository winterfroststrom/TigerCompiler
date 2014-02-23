package Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Production {
	private List<Symbol> rule;
	
	public Production(Symbol... symbols){
		rule = new ArrayList<>();
		for(Symbol symbol : symbols){
			rule.add(symbol);
		}
	}
	
	public void addToStack(Stack<Symbol> stack){
		for(int i = rule.size() - 1; i >= 0; i-- ){
			stack.push(rule.get(i));
		}
	} 
	
	@Override
	public String toString(){
		String ret = "[ ";
		for(Symbol symbol : rule){
			ret += symbol + " ";
		}
		return ret + "]";
	}
}
