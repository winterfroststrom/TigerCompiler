package CodeGeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import General.Configuration;
import General.Cons;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;

class IRRenamer {
	public static Cons<List<IRInstruction>, Set<String>> renameIR(List<IRInstruction> ir){
		List<IRInstruction> ret = new LinkedList<>();
		Set<String> variables = new HashSet<>();
		for(IRInstruction instruction : ir){
			List<Operand> operands = new ArrayList<>();
			for(Operand operand : instruction.params){
				switch(operand.type){
				case LITERAL:
					operands.add(operand);
					break;
				case LABEL:
					if(operand.value.equals("main")){
						operands.add(operand);
						break;
					}
				case REGISTER:
				case VARIABLE:
					if(!variables.contains(operand.value)){
						if(operand.type.equals(EOPERAND.VARIABLE) 
								|| operand.type.equals(EOPERAND.REGISTER)){
							variables.add(rename(operand.value));
						}
					}
					operands.add(new Operand(operand.type, rename(operand.value)));
					break;
				}
			}
			ret.add(new IRInstruction(instruction.opcode, operands));
		}
		return new Cons<>(ret, variables);
	}
	
	public static String rename(String name){
		if(Configuration.MIPS_VALID_LABELS){
			String out = Configuration.RENAME_WORD;
			for(char c : name.toCharArray()){
				out += Configuration.RENAME_WORD_DELIMITER + (int)c;
			}
			return out;
		} else {
			return name;	
		}
	}
	
	public static String unrename(String rename){
		if(Configuration.MIPS_VALID_LABELS){
			String name = rename.substring(Configuration.RENAME_WORD.length());
			String[] nameParts = name.split(Configuration.RENAME_WORD_DELIMITER);
			String out = "";
			for(int i = 1; i < nameParts.length;i++){
				out += (char) Integer.parseInt(nameParts[i]);
			}
			return out;
		} else {
			return rename;
		}
	}
	
	public static Set<String> rename(Set<String> names){
		Set<String> renames = new HashSet<>();
		for(String name : names){
			renames.add(rename(name));
		}
		return renames;
	}
	
	public static List<String> rename(List<String> names){
		List<String> renames = new LinkedList<>();
		for(String name : names){
			renames.add(rename(name));
		}
		return renames;
	}
	
	public static List<String> unrename(List<String> renames){
		List<String> names = new LinkedList<>();
		for(String rename : renames){
			names.add(unrename(rename));
		}
		return names;
	}
}
