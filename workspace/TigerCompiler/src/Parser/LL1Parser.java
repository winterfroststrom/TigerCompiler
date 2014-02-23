package Parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static General.ETERMINAL.*;
import static General.EVARIABLE.*;
import General.EVARIABLE;
import General.ETERMINAL;
import General.Token;

class LL1Parser {
	private static Production[][] GRAMMAR_TABLE = new Production[EVARIABLE.length][ETERMINAL.length]; 
	private static boolean initialized;
	private Stack<Symbol> symbols;
	private List<ETERMINAL> input;
	private int position;
	
	private void initializeTable(){
		if(!initialized){
			multiLiteral(ADDOP, nexts(PLUS, MINUS));
			multiRule(ANDEXPR, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS), 
					new Production(new Symbol(COMPARE), new Symbol(ANDEXPR_TAIL)));
			multiRule(ANDEXPR_TAIL, nexts(EQ, LESS, GREATER, LESSEREQ, GREATEREQ, NEQ),
					new Production(new Symbol(COMPOP), new Symbol(COMPARE), new Symbol(COMPARE_TAIL)));
			multiEpsilon(ANDEXPR_TAIL, nexts(IF, WHILE, FOR, BREAK, RETURN, ID, ENDIF, END, AND,
					OR, RPAREN, COMMA, RBRACK, THEN, DO, TO, SEMI));			
			literal(ANDOP, AND);
			multiRule(COMPARE, nexts(LPAREN, NIL, STRLIT, INTLIT, ID, MINUS),
					new Production(new Symbol(TERM), new Symbol(COMPARE_TAIL)));

			initialized = true;	
		}
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
			input.add(token.token);
		}
		position = 0;
	}
	
	public boolean parse(List<Token> tokens){
		init(tokens);
		initializeTable();
		while(!symbols.isEmpty()){
			Symbol current = symbols.pop();
			if(current.isTerminal()){
				if(current.getTerminal().equals(input.get(position))){
					position++;
				} else {
					System.err.println("Unexpected terminal " + input.get(position) 
							+ " at position " + position + " expecting " + current);
					return false;
				}
			} else {
				Production rule = GRAMMAR_TABLE[current.getVariable().ordinal()]
						[input.get(position).ordinal()];

				if(rule == null){
					System.err.println("Unexpected terminal " + input.get(position) 
							+ " at position " + position + 
							" expecting one of" + validTerminals(current.getVariable()));
						return false;
				} else {
					rule.addToStack(symbols);
				}
			}
		}
		return true;
	}
	
	public List<ParserError> errors(){
		return new LinkedList<>();
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
