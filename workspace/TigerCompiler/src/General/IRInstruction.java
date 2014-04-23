package General;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IRInstruction {
	public final EIROPCODE opcode;
	public final List<Operand> params;

	public static enum EOPERAND{
		LITERAL, LABEL, REGISTER, VARIABLE
	}
	
	public static class Operand{
		public final EOPERAND type;
		public final String value;
		
		public Operand(EOPERAND type, String value){
			this.type = type;
			this.value = value;
		}

		public boolean isVariable(){
			return type.equals(EOPERAND.VARIABLE) || type.equals(EOPERAND.REGISTER);
		}
		
		@Override
		public String toString(){
			return value;
		}
		
		@Override
		public int hashCode(){
			return type.hashCode() ^ value.hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof Operand){
				return ((Operand)o).type.equals(type) && ((Operand) o).value.equals(value);
			}
			return false;
		}
	}
	
	public Operand param(int index){
		return params.get(index);
	}
	
	public List<Operand> params(int index){
		List<Operand> ret = new ArrayList<>(params.size() - index);
		for(int i = index; i < params.size();i++){
			ret.add(params.get(i));
		}
		return ret;
	}
	
	public IRInstruction(EIROPCODE opcode, Operand...params){
		this.opcode = opcode;
		this.params = new ArrayList<>();
		for(Operand param : params){
			this.params.add(param);
		}
	}
	
	public IRInstruction(EIROPCODE opcode, List<Operand> params){
		this.opcode = opcode;
		this.params = params;
	}
	
	@Override
	public String toString(){
		if(opcode.equals(EIROPCODE.LABEL)){
			return params.get(0) + ":";
		} else {
			String ret = "\t"+opcode.name().toLowerCase();
			for(int i = 0; i < 3;i++){
				ret += ", ";
				if(i < params.size() ){
					ret += params.get(i);	
				}
			}
			if(params.size() > 3){
				for(int i = 3; i < params.size();i++){
					ret += ", " + params.get(i);
				}
			}
			return ret; 
		}
	}
	
	public List<Operand> getUsed(){
		List<Operand> used = new LinkedList<>();
		switch(opcode){
		case ASSIGN:
		case ADD:
		case SUB:
		case MULT:
		case DIV:
		case AND:
		case OR:
		case CALLR:
		case ARRAY_LOAD:
			for(int i = 1; i < params.size();i++){
				Operand op = param(i);
				if(op.type.equals(EOPERAND.VARIABLE) 
						|| op.type.equals(EOPERAND.REGISTER)){
					used.add(op);
				}
			}
			break;
		case BREQ:
		case BRNEQ:
		case BRLT:
		case BRGT:
		case BRGEQ:
		case BRLEQ:
		case CALL:
		case ARRAY_STORE:
		case META_EXACT:
			for(int i = 0; i < params.size();i++){
				Operand op = param(i);
				if(op.type.equals(EOPERAND.VARIABLE) 
						|| op.type.equals(EOPERAND.REGISTER)){
					used.add(op);
				}
			}
			break;
		case RETURN:
			if(params.size() > 0){
				Operand op = param(0);
				if(op.type.equals(EOPERAND.VARIABLE) 
						|| op.type.equals(EOPERAND.REGISTER)){
					used.add(op);
				}
			}
		case GOTO:
		case LABEL:
			break;
		}
		return used;
	}
	
	public boolean isFunctionCall(){
		return opcode.equals(EIROPCODE.CALL) || opcode.equals(EIROPCODE.CALLR);
	}
	
	public List<Operand> functionParameters(){
		if(opcode.equals(EIROPCODE.CALL)){
			return params(1);
		} else if(opcode.equals(EIROPCODE.CALLR)){
			return params(2);
		} else {
			throw new IllegalStateException("Should not be able to ask for paramters of non-function call");
		}
	}
	
	public String functionName(){
		if(opcode.equals(EIROPCODE.CALL)){
			return param(0).value;
		} else if(opcode.equals(EIROPCODE.CALLR)){
			return param(1).value;
		} else {
			throw new IllegalStateException("Should not be able to ask for function name of non-function call");
		}
	}
	
	public List<Operand> getDefined(){
		List<Operand> defined = new LinkedList<>();
		switch(opcode){
		case ASSIGN:
		case ADD:
		case SUB:
		case MULT:
		case DIV:
		case AND:
		case OR:
		case CALLR:
		case ARRAY_LOAD:
			Operand op = param(0);
			if(op.type.equals(EOPERAND.VARIABLE) 
					|| op.type.equals(EOPERAND.REGISTER)){
				defined.add(op);
			}
			return defined;
		case BREQ:
		case BRNEQ:
		case BRLT:
		case BRGT:
		case BRGEQ:
		case BRLEQ:
		case CALL:
		case ARRAY_STORE:
		case RETURN:
		case GOTO:
		case META_EXACT:
		case LABEL:
			break;
		}
		return defined;
	}
}
