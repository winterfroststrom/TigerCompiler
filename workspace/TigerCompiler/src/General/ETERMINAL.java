package General;

public enum ETERMINAL {
	UMINUS,
	PLUS, MINUS, MULT, DIV,
	AND, OR, 
	LPAREN, RPAREN,
	EQ, NEQ, LESSEREQ, LESS, GREATEREQ, GREATER, 
	LBRACK, RBRACK, 
	SEMI, ASSIGN, COLON, COMMA, 
	INTLIT, ID, STRLIT, NIL, ARRAY, 
	RETURN, BREAK, DO, ELSE, END, FOR, FUNC, 
	IF, IN, LET, OF, THEN, TO, TYPE, VAR, WHILE, ENDIF, BEGIN, ENDDO;
	public static final int length = EVARIABLE.values().length;
	
	public int precedence(){
		switch(this){
		case LPAREN:
		case RPAREN:
		case LBRACK:
		case RBRACK:
			return 1;
		case UMINUS:
			return 2;
		case MULT:
		case DIV:			
			return 3;
		case PLUS:
		case MINUS:
			return 4;
		case EQ:
		case NEQ:
		case GREATER:
		case LESS:
		case GREATEREQ:
		case LESSEREQ:
			return 5;
		case AND:
			return 6;
		case OR:
			return 7;
		case ASSIGN:
			return 8;
		default:
			throw new IllegalArgumentException("Terminal is not a valid operator.");
		}
	}
	
	public boolean isOperator(){
		switch(this){
		case LPAREN:
		case RPAREN:
		case LBRACK:
		case RBRACK:
		case UMINUS:
		case MULT:
		case DIV:	
		case PLUS:
		case MINUS:
		case EQ:
		case NEQ:
		case GREATER:
		case LESS:
		case GREATEREQ:
		case LESSEREQ:
		case AND:
		case OR:
		case ASSIGN:
			return true;
		default:
			return false;
		}
	}
	
	public int operands(){
		switch(this){
		case UMINUS:
			return 1;
		case MULT:
		case DIV:	
		case PLUS:
		case MINUS:
		case EQ:
		case NEQ:
		case GREATER:
		case LESS:
		case GREATEREQ:
		case LESSEREQ:
		case AND:
		case OR:
		case ASSIGN:
			return 2;
		default:
			throw new IllegalArgumentException("Terminal is not a valid operator.");
		}
	}
}
