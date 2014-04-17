package CodeGeneration;

import java.util.Collection;
import java.util.HashMap;
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
	private static final int MIPS_TEMPORARY_COUNT = 17;
	private List<String> output;

	public EBBMipsGenerator(List<String> output) {
		this.output = output;
	}

	public List<String> generate(List<IRInstruction> instructions,
			SymbolTable table, int instructionIndex) {
		Map<Integer, BasicBlock> blocks = BasicBlock.parseIR(instructions,
				instructionIndex, table);
		Map<BasicBlock, ExtendedBasicBlock> ebbMap = BBtoEBB.convert(blocks);
		Set<Integer> ebbRootPositions = new HashSet<>();
		for(ExtendedBasicBlock ebb : ebbMap.values()){
			ebbRootPositions.add(ebb.root.position);
		}
		for (BasicBlock bb : BasicBlock.order(blocks)) {
			output.add("#\tBlock " + bb.position + "\t" + bb.getVariables());
			registerAllocate(bb, output, table, ebbMap.get(bb), ebbRootPositions);
		}
		return output;
	}

	private void registerAllocate(BasicBlock bb, List<String> output,
			SymbolTable table, ExtendedBasicBlock ebb, Set<Integer> ebbPositions) {
		Map<Operand, String> registerMap = new HashMap<>();
		if (ebb.getVariables().size() < MIPS_TEMPORARY_COUNT) {
			int registerNum = 8;
			for (Operand op : ebb.getVariables()) {
				registerMap.put(op, "$" + registerNum++);
			}
		} else {
			graphColor(bb, output, table, registerMap);
		}
		Collection<Operand> load = new LinkedList<>();
		if(ebb.root.equals(bb)){
			load = ebb.getUsed().keySet();
		}
		Collection<Operand>save = determineSave(bb, ebb, ebbPositions);
		RegisterCodeGenerator.generateBasicBlock(bb, registerMap, output, table, 
				load, save);
	}

	private Collection<Operand> determineSave(BasicBlock bb,
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
			return ebb.getDefined().keySet();
		} else {

			return new LinkedList<>();
		}
	}

	private void graphColor(BasicBlock bb, List<String> output2,
			SymbolTable table, Map<Operand, String> registerMap) {
		// TODO implement graph coloring
	}

}