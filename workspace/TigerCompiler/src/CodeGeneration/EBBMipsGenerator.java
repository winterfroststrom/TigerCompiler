package CodeGeneration;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Cons;
import General.IRInstruction;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class EBBMipsGenerator {
	private List<String> output;

	public EBBMipsGenerator(List<String> output) {
		this.output = output;
	}

	public List<String> generate(List<IRInstruction> instructions,
			SymbolTable table, int instructionIndex) {
		Cons<Map<Integer, BasicBlock>, Map<IRInstruction, String>> cons = 
				BasicBlock.parseIR(instructions, instructionIndex, table);
		Map<Integer, BasicBlock> blocks = cons.a;
		Map<IRInstruction, String> functionMap = cons.b;
		
		Map<BasicBlock, ExtendedBasicBlock> ebbMap = BBtoEBB.convert(blocks);
		Set<Integer> ebbRootPositions = new HashSet<>();
		for(ExtendedBasicBlock ebb : ebbMap.values()){
			ebbRootPositions.add(ebb.root.position);
		}
		for (BasicBlock bb : BasicBlock.order(blocks)) {
			ExtendedBasicBlock ebb = ebbMap.get(bb);
			Map<IRInstruction, Map<Operand, String>> registerMap = 
					RegisterAllocator.allocate(bb, output, table, functionMap, ebb, ebbRootPositions);
			Collection<Operand> load = new LinkedList<>();
			if(ebb.root.equals(bb)){
				load = ebb.getIn();
			}
			Collection<Operand> save = determineSave(bb, ebbMap.get(bb), ebbRootPositions);
			RegisterCodeGenerator.generateBasicBlock(bb, registerMap, output, table, functionMap,
					load, save);
		}
		return output;
	}
	
	private static Collection<Operand> determineSave(BasicBlock bb,
			ExtendedBasicBlock ebb, Set<Integer> ebbPositions) {
		boolean needSave = false;
		if(ebb.exits.contains(bb)){
			needSave = true;
		} else {
			for(int successorIndex : bb.successors){
				if(ebbPositions.contains(successorIndex)){
					needSave = true;
					break;
				}
			}
		}
		if(needSave){
			return ebb.getOut();
		} else {
			return new LinkedList<>();
		}
	}

}
