package CodeGeneration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		List<BasicBlock> blocks = BasicBlock.parseIR(instructions,
				instructionIndex, table);
		for (BasicBlock bb : blocks) {
			output.add("#\tBlock " + bb.position + "\t" + bb.variables);
			registerAllocate(bb, output, table);
		}
		return output;
	}

	private void registerAllocate(BasicBlock bb, List<String> output,
			SymbolTable table) {
		Map<Operand, String> registerMap = new HashMap<>();
		if (bb.variables.size() < MIPS_TEMPORARY_COUNT) {
			int registerNum = 8;
			for (Operand op : bb.variables) {
				registerMap.put(op, "$" + registerNum++);
			}
		} else {
			graphColor(bb, output, table, registerMap);
		}
		if (bb.label != null) {
			RegisterCodeGeneration.generate(bb.label, table, output, registerMap);
		}
		output.add("#\t Load Registers");
		for (Operand op : registerMap.keySet()) {
			output.add("\tla $a0, " + op.value);
			output.add("\tlw " + registerMap.get(op) + ", 0($a0)");
		}
		for (IRInstruction instruction : bb.instructions) {
			RegisterCodeGeneration.generate(instruction, table, output, registerMap);
		}
		output.add("#\t Store Registers");
		for (Operand op : registerMap.keySet()) {
			output.add("\tla $a0, " + op.value);
			output.add("\tsw " + registerMap.get(op) + ", 0($a0)");
		}
		if (bb.jump != null) {
			RegisterCodeGeneration.generate(bb.jump, table, output, registerMap);
		}
		
	}

	private void graphColor(BasicBlock bb, List<String> output2,
			SymbolTable table, Map<Operand, String> registerMap) {
		// TODO implement graph coloring
	}

}
