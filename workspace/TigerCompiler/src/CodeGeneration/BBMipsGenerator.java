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
	private List<String> output;

	public BBMipsGenerator(List<String> output) {
		this.output = output;
	}

	public List<String> generate(List<IRInstruction> instructions,
			SymbolTable table, int instructionIndex) {
		Cons<Map<Integer, BasicBlock>, Map<IRInstruction, String>> cons = BasicBlock.parseIR(instructions,
				instructionIndex, table);
		Map<Integer, BasicBlock> blocks = cons.a;
		Map<IRInstruction, String> functionMap = cons.b;

		for (BasicBlock bb : BasicBlock.order(blocks)) {
			output.add("#\tBlock " + bb.position + "\t" + bb.getVariables());
			registerAllocate(bb, output, table, functionMap);
		}
		return output;
	}

	private void registerAllocate(BasicBlock bb, List<String> output,
			SymbolTable table, Map<IRInstruction, String> functionMap) {
		Map<Operand, String> registerMap = new HashMap<>();
		if (bb.getVariables().size() < MipsGenerator.TEMP_REGISTERS.size()) {
			int registerNum = 0;
			for (Operand op : bb.getVariables()) {
				registerMap.put(op, MipsGenerator.TEMP_REGISTERS.get(registerNum++));
			}
		} else {
			graphColor(bb, output, table, registerMap);
		}
		RegisterCodeGenerator.generateBasicBlock(bb, registerMap, output, table, functionMap, 
				bb.getUsed().keySet(), bb.getDefined().keySet());
	}

	private void graphColor(BasicBlock bb, List<String> output2,
			SymbolTable table, Map<Operand, String> registerMap) {
		// TODO implement graph coloring
	}

}
