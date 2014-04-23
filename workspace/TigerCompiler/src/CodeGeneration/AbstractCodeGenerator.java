package CodeGeneration;

import java.util.List;
import java.util.Map;

import General.IRInstruction;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

abstract class AbstractCodeGenerator {
	private void storeFromLabelToLabelToStack(String variable, String label, List<String> output){
		//store label -> stack
		String labelRegister = getRegisterMap(label);
		if(labelRegister != null){			
			saveRegister(labelRegister, output);			
		} else {
			output.add("\tla $a1, " + label);
			output.add("\tlw $a1, 0($a1)");
			saveRegister("$a1", output);
		}
		//new value -> label
		output.add("\tla $a0, " + variable);
		output.add("\tlw $a0, 0($a0)");
		output.add("\tla $a1, " + label);
		output.add("\tsw $a0, 0($a1)");
		
		if(labelRegister != null){
			output.add("\tmove " + labelRegister + ", $a0");			
		}
	}
	
	private void storeFromRegisterToLabelToStack(String register, String label, List<String> output){
		//store label -> stack
		String labelRegister = getRegisterMap(label);
		if(labelRegister != null){			
			saveRegister(labelRegister, output);			
		} else {
			output.add("\tla $a1, " + label);
			output.add("\tlw $a1, 0($a1)");
			saveRegister("$a1", output);
		}
		//new value -> label
		output.add("\tla $a0, " + label);
		output.add("\tsw " + register + ", 0($a0)");
		
		if(labelRegister != null){
			output.add("\tmove " + labelRegister + ", " + register);			
		}
	}
	
	private void storeFromImmediateToLabelToStack(String immediate, String label, List<String> output){
		//store label -> stack
		String labelRegister = getRegisterMap(label);
		if(labelRegister != null){			
			saveRegister(labelRegister, output);			
		} else {
			output.add("\tla $a1, " + label);
			output.add("\tlw $a1, 0($a1)");
			saveRegister("$a1", output);
		}
		//new value -> label
		output.add("\taddi $a0, $zero, " + immediate);
		output.add("\tla $a1, " + label);
		output.add("\tsw $a0, 0($a1)");
		
		if(labelRegister != null){
			output.add("\tmove " + labelRegister + ", $a0");		
		}
	}
	
	private void loadToLabelFromStack(String variable, List<String> output){
		restoreRegister("$a1", output);
		output.add("\tla $a0, " + variable);
		output.add("\tsw $a1, 0($a0)");		
	}
	
	private void loadToRegisterFromLabelFromStack(String register, String variable, List<String> output){
		restoreRegister("$a1", output);
		output.add("\tla $a0, " + variable);
		output.add("\tsw $a1, 0($a0)");
		output.add("\tmove " + register + ", $a1");
		
	}
	
	protected void handleCall(String function, List<Operand> params, SymbolTable table, List<String> output) {
		saveRegister("$ra", output);
		
		String label = IRRenamer.unrename(function);
		List<String> paramNames = IRRenamer.rename(table.functionParamNames(label));
		for(int i = 0; i < paramNames.size();i++){
			if(params.get(i).isVariable()){
				String register = getRegisterMap(params.get(i).value);
				if(register == null){
					storeFromLabelToLabelToStack(params.get(i).value, paramNames.get(i), output);
				} else {
					storeFromRegisterToLabelToStack(register, paramNames.get(i), output);
				}	
			} else {
				storeFromImmediateToLabelToStack(params.get(i).value, paramNames.get(i), output);
			}
			
		}
		output.add("\tjal " + function);
	}
	
	protected void handleRestoreReturn(IRInstruction instruction, Map<IRInstruction, String> functionMap, 
			SymbolTable table, List<String> output) {
		handleRestoreFromFunction(functionMap.get(instruction), table, output);
	}
	
	protected void handleRestoreFromFunction(String function, SymbolTable table, List<String> output){
		List<String> paramNames = IRRenamer.rename(table.functionParamNames(IRRenamer.unrename(function)));
		for(int i = paramNames.size() - 1; i >= 0;i--){
			String register = getRegisterMap(paramNames.get(i));
			if(register == null){
				loadToLabelFromStack(paramNames.get(i), output);
			} else {
				loadToRegisterFromLabelFromStack(register, paramNames.get(i), output);
			}
		}
	}
	
	protected void saveRegister(String register, List<String> output) {		
		output.add("\tsw " + register + ", 0($sp)");
		output.add("\taddi $sp, $sp, -4");
	}

	protected void restoreRegister(String register, List<String> output) {
		output.add("\taddi $sp, $sp, 4");
		output.add("\tlw " + register + ", 0($sp)");
	}
	
	
	protected abstract String getRegisterMap(String register);
}
