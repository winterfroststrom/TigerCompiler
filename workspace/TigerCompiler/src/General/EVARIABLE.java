package General;

public enum EVARIABLE {
	LVALUE_TAIL, LVALUE, EXPR_LIST_TAIL, EXPR_LIST, MULOP, ADDOP, COMPOP, 
	ANDOP, OROP, CONST, UNARYMINUS, FACTOR, TERM_TAIL, TERM, COMPARE_TAIL, 
	COMPARE, ANDEXPR_TAIL, ANDEXPR, OREXPR, OREXPR_TAIL, EXPR, EXPR_TAIL, 
	STAT_ASSIGN, STAT_ASSIGN_ID, STAT_ASSIGN_TAIL, STAT_IF_TAIL, 
	STAT_FUNC_OR_ASSIGN, STAT, STAT_SEQ, STAT_SEQ_TAIL, PARAM, RET_TYPE, 
	PARAM_LIST_TAIL, PARAM_LIST, OPTIONAL_INIT, ID_LIST_TAIL, ID_LIST, 
	TYPE_ID, TYPE_DIM, TYPE, FUNCT_DECLARATION, VAR_DECLARATION, 
	TYPE_DECLARATION, FUNCT_DECLARATION_LIST, VAR_DECLARATION_LIST, 
	TYPE_DECLARATION_LIST, DECLARATION_SEGMENT, TIGER_PROGRAM;
	public static final int length = EVARIABLE.values().length;

}
