package CodeGeneration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Configuration;
import General.IRInstruction;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class RegisterAllocator {
	public static Map<IRInstruction, Map<Operand, String>> allocate(BasicBlock bb, List<String> output,
			SymbolTable table, Map<IRInstruction, String> functionMap) {
		return  LivelinessAnalysis.analyze(bb);
	}
	
	public static Map<IRInstruction, Map<Operand, String>> allocate(BasicBlock bb, List<String> output,
			SymbolTable table, Map<IRInstruction, String> functionMap, 
			ExtendedBasicBlock ebb, Set<Integer> ebbPositions) {
		if (ebb.getVariables().size() < Configuration.TEMP_REGISTERS.size()) {
			return registerAssignment(ebb.getVariables(), ebb.allInstructions());
		} else {
			return emptyAssignment(ebb.allInstructions());
		}
	}
	
	private static Map<IRInstruction, Map<Operand, String>> registerAssignment(Set<Operand> variables, 
			List<IRInstruction> instructions) {
		Map<IRInstruction, Map<Operand, String>> registerMap = new HashMap<>();
		
		Map<Operand, String> registerMapAtInstruction = new HashMap<>();
		int registerNum = 0;
		for (Operand op : variables) {
			registerMapAtInstruction.put(op, Configuration.TEMP_REGISTERS.get(registerNum++));
		}
		for(IRInstruction instruction : instructions){
			registerMap.put(instruction, registerMapAtInstruction);
		}
		registerMap.put(null, registerMapAtInstruction);
		return registerMap;
	}
	
	private static Map<IRInstruction, Map<Operand, String>>  emptyAssignment(List<IRInstruction> instructions) {
		Map<IRInstruction, Map<Operand, String>> registerMap = new HashMap<>();
		Map<Operand, String> registerMapAtInstruction = new HashMap<>();
		for(IRInstruction instruction : instructions){
			registerMap.put(instruction, registerMapAtInstruction);
		}
		registerMap.put(null, registerMapAtInstruction);
		return registerMap;
	}
}
