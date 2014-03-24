package Parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static General.ETERMINAL.*;
import static General.EVARIABLE.*;
import General.Configuration;
import General.EVARIABLE;
import General.ETERMINAL;
import General.Symbol;
import General.Token;

class LL1Parser {
	private static Production[][] GRAMMAR_TABLE = new Production[EVARIABLE.length][ETERMINAL.length];
	private static boolean initialized;
	private Stack<Symbol> symbols;
	private List<Token> input;
	private List<ParserError> errors;
	private int position;
	
	private void initializeTable(){
		if(!initialized){
			multiLiteral(ADDOP, nexts(PLUS, MINUS));
			multiRule(ANDEXPR, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS), 
					p(s(COMPARE), s(ANDEXPR_TAIL)));
			multiRule(ANDEXPR_TAIL, nexts(EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ),
					p(s(COMPOP), s(COMPARE), s(COMPARE_TAIL)));
			multiEpsilon(ANDEXPR_TAIL, nexts(AND, OR, RPAREN, COMMA, RBRACK, THEN, DO, TO, SEMI));			
			literal(ANDOP, AND);
			multiRule(COMPARE, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS),
					p(s(TERM), s(COMPARE_TAIL)));
			multiRule(COMPARE_TAIL, nexts(PLUS, MINUS), 
					p(s(ADDOP), s(TERM), s(COMPARE_TAIL)));
			multiEpsilon(COMPARE_TAIL, nexts(AND, OR, RPAREN, COMMA, RBRACK, THEN, DO, TO, SEMI, 
					EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ));
			multiLiteral(COMPOP, nexts(EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ));
			multiLiteral(CONST, nexts(NIL, STRLIT, INTLIT));
			multiRule(DECLARATION_SEGMENT, nexts(FUNC, VAR, TYPE), 
					p(s(TYPE_DECLARATION_LIST), s(VAR_DECLARATION_LIST), s(FUNCT_DECLARATION_LIST)));
			epsilon(DECLARATION_SEGMENT, IN);
			epsilon(EXPR_LIST, RPAREN);
			multiRule(EXPR_LIST, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS),
					p(s(EXPR), s(EXPR_LIST_TAIL)));
			epsilon(EXPR_LIST_TAIL, RPAREN);
			rule(EXPR_LIST_TAIL, COMMA, p(s(COMMA), s(EXPR), s(EXPR_LIST_TAIL)));
			multiRule(EXPR, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS),
					p(s(OREXPR), s(EXPR_TAIL)));
			rule(EXPR_TAIL, OR, p(s(OROP), s(OREXPR), s(EXPR_TAIL)));
			multiEpsilon(EXPR_TAIL, nexts(RPAREN, COMMA, RBRACK, THEN, DO, TO, SEMI));
			multiRule(FACTOR, nexts(LPAREN, NIL, STRLIT, INTLIT, ID),
					p(s(UNARYMINUS)));
			rule(FACTOR, MINUS, p(s(MINUS), s(UNARYMINUS)));
			rule(FUNCT_DECLARATION, FUNC, 
					p(s(FUNC), s(ID), s(LPAREN), s(PARAM_LIST), s(RPAREN), s(RET_TYPE), 
							s(BEGIN), s(STAT_SEQ), s(END), s(SEMI)));
			rule(FUNCT_DECLARATION_LIST, FUNC, 
					p(s(FUNCT_DECLARATION), s(FUNCT_DECLARATION_LIST)));
			epsilon(FUNCT_DECLARATION_LIST, IN);
			rule(ID_LIST, ID, p(s(ID), s(ID_LIST_TAIL)));
			epsilon(ID_LIST_TAIL, COLON);
			rule(ID_LIST_TAIL, COMMA, 
					p(s(COMMA), s(ID), s(ID_LIST_TAIL)));
			rule(LVALUE, ID, p(s(ID), s(LVALUE_TAIL)));
			rule(LVALUE_TAIL, LBRACK, 
					p(s(LBRACK), s(EXPR), s(RBRACK), s(LVALUE_TAIL)));
			multiEpsilon(LVALUE_TAIL, nexts(ASSIGN, MULT, DIV, PLUS, MINUS,
					EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ, AND, OR, RPAREN, COMMA,
					RBRACK, THEN, DO, TO, SEMI));
			multiLiteral(MULOP, nexts(MULT, DIV));
			rule(OPTIONAL_INIT, ASSIGN, p(s(ASSIGN), s(CONST)));
			epsilon(OPTIONAL_INIT, SEMI);
			multiRule(OREXPR, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS),
					p(s(ANDEXPR), s(OREXPR_TAIL)));
			rule(OREXPR_TAIL, AND, 
					p(s(ANDOP), s(ANDEXPR), s(OREXPR_TAIL)));
			multiEpsilon(OREXPR_TAIL, nexts(OR, RPAREN, COMMA, RBRACK, THEN, DO, TO, SEMI));
			literal(OROP, OR);
			rule(PARAM, ID, p(s(ID), s(COLON), s(TYPE_ID)));
			rule(PARAM_LIST, ID, p(s(PARAM), s(PARAM_LIST_TAIL)));
			epsilon(PARAM_LIST, RPAREN);
			epsilon(PARAM_LIST_TAIL, RPAREN);
			rule(PARAM_LIST_TAIL, COMMA, 
					p(s(COMMA), s(PARAM), s(PARAM_LIST_TAIL)));
			epsilon(RET_TYPE, BEGIN);
			rule(RET_TYPE, COLON, p(s(COLON), s(TYPE_ID)));
			multiRule(STAT_ASSIGN, nexts(NIL, STRLIT, INTLIT),
					p(s(CONST), s(STAT_ASSIGN_TAIL)));
			rule(STAT_ASSIGN, ID, p(s(ID), s(STAT_ASSIGN_ID)));
			rule(STAT_ASSIGN, MINUS, p(s(MINUS), s(UNARYMINUS), s(STAT_ASSIGN_TAIL)));
			epsilon(STAT_ASSIGN_ID, SEMI);
			multiRule(STAT_ASSIGN_ID, nexts(OR, AND, NEQ, LESSEREQ, GREATEREQ, LESS, GREATER, EQ,
					PLUS, MINUS, DIV, MULT, LBRACK),
					p(s(LVALUE_TAIL), s(STAT_ASSIGN_TAIL)));
			rule(STAT_ASSIGN_ID, LPAREN, 
					p(s(LPAREN), s(EXPR_LIST), s(RPAREN)));
			epsilon(STAT_ASSIGN_TAIL, SEMI);
			rule(STAT_ASSIGN_TAIL, OR, p(s(EXPR_TAIL)));
			rule(STAT_ASSIGN_TAIL, AND, p(s(OREXPR_TAIL)));
			multiRule(STAT_ASSIGN_TAIL, nexts(EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ), 
					p(s(ANDEXPR_TAIL)));
			multiRule(STAT_ASSIGN_TAIL, nexts(PLUS, MINUS), p(s(COMPARE_TAIL)));
			multiRule(STAT_ASSIGN_TAIL, nexts(MULT, DIV), p(s(TERM_TAIL)));
			rule(STAT_FUNC_OR_ASSIGN, LPAREN, 
					p(s(LPAREN), s(EXPR_LIST), s(RPAREN)));
			multiRule(STAT_FUNC_OR_ASSIGN, nexts(ASSIGN, LBRACK), 
					p(s(LVALUE_TAIL), s(ASSIGN), s(STAT_ASSIGN)));
			rule(STAT_IF_TAIL, ELSE, 
					p(s(ELSE), s(STAT_SEQ), s(ENDIF)));
			rule(STAT_IF_TAIL, ENDIF, p(s(ENDIF)));
			rule(STAT, IF, p(s(IF), s(EXPR), s(THEN), s(STAT_SEQ), s(STAT_IF_TAIL), s(SEMI)));
			rule(STAT, WHILE, p(s(WHILE), s(EXPR), s(DO), s(STAT_SEQ), s(ENDDO), s(SEMI)));
			rule(STAT, FOR, p(s(FOR), s(ID), s(ASSIGN), s(EXPR), s(TO), s(EXPR), s(DO), s(STAT_SEQ), s(ENDDO), s(SEMI)));
			rule(STAT, BREAK, p(s(BREAK), s(SEMI)));
			rule(STAT, RETURN, p(s(RETURN), s(EXPR), s(SEMI)));
			rule(STAT, ID, p(s(ID), s(STAT_FUNC_OR_ASSIGN), s(SEMI)));
			multiRule(STAT_SEQ, nexts(IF, WHILE, FOR, BREAK, RETURN, ID), 
					p(s(STAT), s(STAT_SEQ_TAIL)));
			multiEpsilon(STAT_SEQ_TAIL, nexts(ELSE, ENDIF, END, ENDDO));
			multiRule(STAT_SEQ_TAIL, nexts(IF, WHILE, FOR, BREAK, RETURN, ID), 
					p(s(STAT), s(STAT_SEQ_TAIL)));
			multiRule(TERM, nexts(MINUS, ID, INTLIT, STRLIT, NIL, LPAREN),
					p(s(FACTOR), s(TERM_TAIL)));
			multiRule(TERM_TAIL, nexts(MULT, DIV),
					p(s(MULOP), s(FACTOR), s(TERM_TAIL)));
			multiEpsilon(TERM_TAIL, nexts(RPAREN, OR, AND, MINUS, PLUS, NEQ, LESSEREQ, 
					GREATEREQ, GREATER, LESS, EQ, SEMI, TO, DO, THEN, RBRACK, COMMA));
			rule(TIGER_PROGRAM, LET, 
					p(s(LET), s(DECLARATION_SEGMENT), s(IN), s(STAT_SEQ), s(END)));
			rule(TYPE_LIT, ARRAY, 
					p(s(ARRAY), s(LBRACK), s(INTLIT), s(RBRACK), s(TYPE_DIM), s(OF), s(TYPE_ID)));
			rule(TYPE_LIT, ID, p(s(TYPE_ID)));
			rule(TYPE_DECLARATION_LIST, TYPE, 
					p(s(TYPE_DECLARATION), s(TYPE_DECLARATION_LIST)));
			multiEpsilon(TYPE_DECLARATION_LIST, nexts(VAR, FUNC, IN));
			rule(TYPE_DECLARATION, TYPE, 
					p(s(TYPE), s(ID), s(EQ), s(TYPE_LIT), s(SEMI)));
			rule(TYPE_DIM, LBRACK, p(s(LBRACK), s(INTLIT), s(RBRACK), s(TYPE_DIM)));
			epsilon(TYPE_DIM, OF);
			literal(TYPE_ID, ID);
			rule(UNARYMINUS, LPAREN, p(s(LPAREN), s(EXPR), s(RPAREN)));
			multiRule(UNARYMINUS, nexts(NIL, STRLIT, INTLIT), p(s(CONST)));
			rule(UNARYMINUS, ID, p(s(LVALUE)));
			multiEpsilon(VAR_DECLARATION_LIST, nexts(FUNC, IN));
			rule(VAR_DECLARATION_LIST, VAR, 
					p(s(VAR_DECLARATION), s(VAR_DECLARATION_LIST)));
			rule(VAR_DECLARATION, VAR, p(s(VAR), s(ID_LIST), s(COLON), 
					s(TYPE_ID), s(OPTIONAL_INIT), s(SEMI)));
			initialized = true;	
		}
	}

	private Symbol s(EVARIABLE v){
		return new Symbol(v);
	}
	
	private Symbol s(Token t){
		return new Symbol(t);
	}
	
	
	private Symbol s(ETERMINAL t){
		return new Symbol(t);
	}
	
	private Production p(Symbol...symbols){
		return new Production(symbols);
	}
	
	private ETERMINAL[] nexts(ETERMINAL... nexts){
		return nexts;
	}
	
	private void multiRule(EVARIABLE current, ETERMINAL[] nexts, Production production){
		for(ETERMINAL next : nexts){
			rule(current, next, production);
		}
	}
	
	private void multiEpsilon(EVARIABLE current, ETERMINAL[] nexts){
		for(ETERMINAL next : nexts){
			epsilon(current, next);
		}
	}

	private void multiLiteral(EVARIABLE current, ETERMINAL[] nexts){
		for(ETERMINAL next : nexts){
			literal(current, next);
		}
	}
	
	private void epsilon(EVARIABLE current, ETERMINAL next){
		GRAMMAR_TABLE[current.ordinal()][next.ordinal()] = new Production();
	}
	
	private void literal(EVARIABLE current, ETERMINAL next){
		GRAMMAR_TABLE[current.ordinal()][next.ordinal()] = 
				new Production(new Symbol(next));
	}

	
	private void rule(EVARIABLE current, ETERMINAL next, Production production){
		GRAMMAR_TABLE[current.ordinal()][next.ordinal()] = production;
	}
	
	private void init(List<Token> tokens){
		symbols = new Stack<>();
		symbols.push(new Symbol(EVARIABLE.TIGER_PROGRAM));
		input = new ArrayList<>();
		for(Token token : tokens){
			input.add(token);
		}
		position = 0;
		errors = new LinkedList<>();
	}
	
	public ParseTreeNode parse(List<Token> tokens){
		ParseTreeNode tree = new ParseTreeNode(null);
		ParseTreeNode curr = tree;
		init(tokens);
		initializeTable();
		while(!symbols.isEmpty()){
			Symbol current = symbols.pop();
			if(Configuration.LL1PARSER_DEBUGGING){
				System.err.println(current);
			}
			if(current.isTerminal()){
				if(current.getTerminal().equals(input.get(position).token)){
					curr = curr.addTerminal(s(input.get(position)));
					position++;
				} else {
					errors.add(new ParserError(input.get(position).token, position, current.getTerminal()));
					if(Configuration.LL1PARSER_DEBUGGING){
						for(int i = 0; i < position;i++){
							System.err.print(input.get(i) + " ");
						}
						System.err.println();
						System.err.println(symbols);
						System.err.println(errors.get(errors.size() - 1));
					}
				}
			} else {
				Production rule = GRAMMAR_TABLE[current.getVariable().ordinal()]
						[input.get(position).token.ordinal()];

				if(rule == null){
					errors.add(new ParserError(input.get(position).token, position, 
							validTerminals(current.getVariable())));
					if(Configuration.LL1PARSER_DEBUGGING){
						for(int i = 0; i < position;i++){
							System.err.print(input.get(i) + " ");
						}
						System.err.println();
						System.err.println(symbols);
						System.err.println(errors.get(errors.size() - 1));
					}
				} else {
					curr = curr.addRule(current, rule);
					rule.addToStack(symbols);
				}
			}
		}
		tree.flattenExpressions();
		return tree;
	}
	
	public List<ParserError> errors(){
		if(errors != null){
			return errors;
		} else {
			return new LinkedList<>();
		}
	}
	
	private List<ETERMINAL> validTerminals(EVARIABLE current){
		List<ETERMINAL> terminals = new LinkedList<>();
		Production[] productions = GRAMMAR_TABLE[current.ordinal()];
		ETERMINAL[] values = ETERMINAL.values();
		for(int i = 0; i < productions.length;i++){
			if(productions[i] != null){
				terminals.add(values[i]);
			}
		}
		return terminals;
	}
}
