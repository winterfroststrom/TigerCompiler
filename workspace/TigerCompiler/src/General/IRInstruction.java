package General;

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
	
	
	public IRInstruction(EIROPCODE opcode, Operand...params){
		this.opcode = opcode;
		this.params = new LinkedList<>();
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
}
