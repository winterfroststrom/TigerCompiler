package SemanticChecking;

public class SemanticError {
	String error = "";
	
	public SemanticError(String string) {
		error = string;
	}

	@Override
	public String toString(){
		return error;
	}
}
