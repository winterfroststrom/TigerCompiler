package Parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import General.Symbol;

class Production implements Iterable<Symbol>{
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
	
	public int size(){
		return rule.size();
	}
	
	@Override
	public String toString(){
		String ret = "[ ";
		for(Symbol symbol : rule){
			ret += symbol + " ";
		}
		return ret + "]";
	}

	@Override
	public Iterator<Symbol> iterator() {
		return rule.iterator();
	}
}
