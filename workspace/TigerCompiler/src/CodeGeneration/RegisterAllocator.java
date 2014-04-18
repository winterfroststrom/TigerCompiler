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
	public static Map<IRInstruction, Map<Operand, String>> allocate(BasicBlock bb) {
		return LivelinessAnalysis.analyze(bb);
	}
	
	public static Map<IRInstruction, Map<Operand, String>> allocate(ExtendedBasicBlock ebb) {
		return LivelinessAnalysis.analyze(ebb);
	}
	
	private static boolean canAssign(Set<Operand> variables){
		return variables.size() < Configuration.TEMP_REGISTERS.size();
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

	public static Map<BasicBlock, Map<IRInstruction, Map<Operand, String>>> allocateEBB(
			Collection<ExtendedBasicBlock> ebbs) {
		Map<BasicBlock, Map<IRInstruction, Map<Operand, String>>> registerMapMap = new HashMap<>();
		for(ExtendedBasicBlock ebb : ebbs){
			Map<IRInstruction, Map<Operand, String>> registerMap = allocate(ebb);
			for(BasicBlock bb : ebb.allBlocks()){
				registerMapMap.put(bb, registerMap);
			}
		}
		
		return registerMapMap;
	}
	
	public static Map<BasicBlock, Map<IRInstruction, Map<Operand, String>>> allocateBB(
			Collection<BasicBlock> blocks) {
		Map<BasicBlock, Map<IRInstruction, Map<Operand, String>>> registerMapMap = new HashMap<>();
		for(BasicBlock bb : blocks){
			registerMapMap.put(bb, allocate(bb));
		}
		return registerMapMap;
	}
}
