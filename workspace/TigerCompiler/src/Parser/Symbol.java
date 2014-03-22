package Parser;

import General.ETERMINAL;
import General.EVARIABLE;
import General.Token;

class Symbol {
	private Token token;
	private EVARIABLE variable;
	
	public Symbol(Token token){
		this.token = token;
	}
	
	public Symbol(ETERMINAL terminal){
		this.token = new Token(terminal, "", -1);
	}
	
	public Symbol(EVARIABLE symbol){
		this.variable = symbol;
	}
	
	public boolean isTerminal(){
		return variable == null;
	}
	
	public ETERMINAL getTerminal(){
		return token.token;
	}
	
	public EVARIABLE getVariable(){
		return variable;
	}
	
	@Override
	public String toString(){
		if(isTerminal()){
			return token.toString();
		} else {
			return variable.name();
		}
	}
	
	public boolean equals(EVARIABLE variable){
		if(isTerminal()){
			return false;
		} else {
			return this.variable.equals(variable);
		}
	}
	
	public boolean equals(ETERMINAL terminal){
		if(isTerminal()){
			return this.variable.equals(terminal);
		} else {
			return false;
		}
	}
}
