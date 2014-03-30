package General;

import java.util.LinkedList;
import java.util.List;

public class IRInstruction {
	public final EIROPCODE opcode;
	public final List<String> params;
	
	public IRInstruction(EIROPCODE opcode, String...params){
		this.opcode = opcode;
		this.params = new LinkedList<>();
		for(String param : params){
			this.params.add(param);
		}
	}
	
	public IRInstruction(EIROPCODE opcode, List<String> params){
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
