package Parser;

import General.ETERMINAL;
import General.EVARIABLE;

class Symbol {
	private ETERMINAL token;
	private EVARIABLE variable;
	
	public Symbol(ETERMINAL token){
		this.token = token;
	}
	
	public Symbol(EVARIABLE symbol){
		this.variable = symbol;
	}
	
	public boolean isTerminal(){
		return variable == null;
	}
	
	public ETERMINAL getTerminal(){
		return token;
	}
	
	public EVARIABLE getVariable(){
		return variable;
	}
	
	@Override
	public String toString(){
		if(isTerminal()){
			return token.name();
		} else {
			return variable.name();
		}
	}
}
