package CodeGeneration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Cons;
import General.IRInstruction;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class BBMipsGenerator {
	private static final int MIPS_TEMPORARY_COUNT = 17;
	private List<String> output;

	public BBMipsGenerator(List<String> output) {
		this.output = output;
	}

	public List<String> generate(List<IRInstruction> instructions,
			SymbolTable table, int instructionIndex) {
		Map<Integer, BasicBlock> blocks = BasicBlock.parseIR(instructions,
				instructionIndex, table);

		for (BasicBlock bb : BasicBlock.order(blocks)) {
			output.add("#\tBlock " + bb.position + "\t" + bb.getVariables());
			registerAllocate(bb, output, table);
		}
		return output;
	}

	private void registerAllocate(BasicBlock bb, List<String> output,
			SymbolTable table) {
		Map<Operand, String> registerMap = new HashMap<>();
		if (bb.getVariables().size() < MIPS_TEMPORARY_COUNT) {
			int registerNum = 8;
			for (Operand op : bb.getVariables()) {
				registerMap.put(op, "$" + registerNum++);
			}
		} else {
			graphColor(bb, output, table, registerMap);
		}
		RegisterCodeGenerator.generateBasicBlock(bb, registerMap, output, table, 
				bb.getUsed().keySet(), bb.getDefined().keySet());
	}

	private void graphColor(BasicBlock bb, List<String> output2,
			SymbolTable table, Map<Operand, String> registerMap) {
		// TODO implement graph coloring
	}

}
