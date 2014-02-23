package General;

import static General.ETOKEN.*;

public class Token {
	public final ETOKEN token;
	public final String text;
	
	public Token(ETOKEN token, String text){
		if(token.equals(ID)){
			this.token = idToken(text);
		} else {
			this.token = token;
		}
		this.text = text;
	}
	
	private static final ETOKEN idToken(String text){
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
			return ETOKEN.ID;
		}
	}
	
	@Override
	public String toString(){
		return "[" + token.name() + " : " + text + "]";
	}
}
