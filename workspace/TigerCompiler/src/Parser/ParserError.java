package Parser;

import java.util.List;

import General.ETERMINAL;

public class ParserError {
	private ETERMINAL unexpected;
	private ETERMINAL expected;
	private List<ETERMINAL> expecteds;
	private int position;
	private boolean terminalError;
	private boolean endOfInputError;
	
	public ParserError(ETERMINAL unexpected, int position){
		this.unexpected = unexpected;
		this.position = position;
		this.endOfInputError = true;
	}
	
	public ParserError(ETERMINAL unexpected, int position, List<ETERMINAL> expecteds){
		this.unexpected = unexpected;
		this.position = position;
		this.expecteds = expecteds;
	}
	
	public ParserError(ETERMINAL unexpected, int position, ETERMINAL expected){
		this.unexpected = unexpected;
		this.position = position;
		this.expected = expected;
		terminalError = true;
	}
	
	@Override
	public String toString(){
		if(endOfInputError){
			return "End of input reached on terminal " 
					+ unexpected + " at position " + position;
		} else if(terminalError){
			return "Unexpected terminal " + unexpected 
					+ " at position " + position + " expecting " + expected;
		} else {
			return "Unexpected terminal " + unexpected 
					+ " at position " + position + 
					" expecting one of " + expecteds;
		}
	}
}
