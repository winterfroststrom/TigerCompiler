package General;

import static General.ETERMINAL.*;

public class Token {
	public final ETERMINAL token;
	public final String text;
	public final int position;
	
	public Token(ETERMINAL token, String text, int position){
		if(token.equals(ID)){
			this.token = idToken(text);
		} else {
			this.token = token;
		}
		this.text = text;
		this.position = position;
	}
	
	private static final ETERMINAL idToken(String text){
		switch(text){
		case "return":
			return RETURN;
//		case "string":
//			return STRING;
//		case "int":
//			return INT;
		case "array":
			return ARRAY;
		case "break":
			return BREAK;
		case "do":
			return DO;
		case "else":
			return ELSE;
		case "end":
			return END;
		case "for":
			return FOR;
		case "function":
			return FUNC;
		case "if":
			return IF;
		case "in":
			return IN;
		case "let":
			return LET;
		case "nil":
			return NIL;
		case "of":
			return OF;
		case "then":
			return THEN;
		case "to":
			return TO;
		case "type":
			return TYPE;
		case "var":
			return VAR;
		case "while":
			return WHILE;
		case "endif":
			return ENDIF;
		case "begin":
			return BEGIN;
		case "enddo":
			return ENDDO;
		default:
			return ETERMINAL.ID;
		}
	}
	
	@Override
	public String toString(){
		return "[" + token.name() + " : " + text + " : " + position + "]";
	}
}
