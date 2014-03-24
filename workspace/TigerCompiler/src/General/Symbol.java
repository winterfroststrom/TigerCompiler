package General;


public class Symbol {
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
	
	public String getText(){
		if(isTerminal()){
			return token.text;
		} else {
			throw new IllegalArgumentException("Symbol is not a terminal");
		}
	}
	
	public int getPosition(){
		if(isTerminal()){
			return token.position;
		} else {
			throw new IllegalArgumentException("Symbol is not a terminal");
		}
	}
	
	public void changeMinusToUMinus(){
		if(isTerminal() && token.token.equals(ETERMINAL.MINUS)){
			token = new Token(ETERMINAL.UMINUS, token.text, token.position);
		} else {
			throw new IllegalArgumentException("Cannot change non-minus to uminus");
		}
	}
	
	public boolean isValue(){
		return equals(ETERMINAL.INTLIT) || equals(ETERMINAL.STRLIT) 
				|| equals(ETERMINAL.ID) || equals(EVARIABLE.LVALUE);
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
			return this.token.token.equals(terminal);
		} else {
			return false;
		}
	}
}
