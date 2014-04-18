package CodeGeneration;

import java.util.List;
import java.util.Map;

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
		Map<BasicBlock, Map<IRInstruction, Map<Operand, String>>> registerMapMap = 
				RegisterAllocator.allocateBB(blocks.values());
		for (BasicBlock bb : BasicBlock.order(blocks)) {
			Map<IRInstruction, Map<Operand, String>> registerMap = registerMapMap.get(bb);
			RegisterCodeGenerator.generateBasicBlock(bb, registerMap, output, table, functionMap, 
					bb.getUsed(), bb.getDefined());
		}
		return output;
	}

	
}
