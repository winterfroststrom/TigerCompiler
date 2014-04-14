package CodeGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Cons;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class BasicBlock {
	int position; 
	List<IRInstruction> instructions;
	List<Integer> successors;
	IRInstruction label;
	IRInstruction jump;
	Set<Operand> variables;
	Set<Operand> usedFirst;
	Set<Operand> defFirst;
	
	
	public BasicBlock(int position){
		this.position = position;
		instructions = new LinkedList<>();
		successors = new LinkedList<>();
		variables = new HashSet<>();
		usedFirst = new HashSet<>();
		defFirst = new HashSet<>();
	}
	
	public static List<BasicBlock> parseIR(List<IRInstruction> ir, int beginIndex, SymbolTable table){
		Map<Integer, BasicBlock> blocks = new HashMap<>(); 
		List<Cons<String, BasicBlock>> enteringBlocks = new LinkedList<>(); // current function 
		List<Cons<String, BasicBlock>> returningBlocks = new LinkedList<>(); // current function 
		List<Cons<String, BasicBlock>> breakingBlocks = new LinkedList<>(); // target label
		List<Cons<String, BasicBlock>> labeledBlocks = new LinkedList<>(); // current label
		List<Cons<String, BasicBlock>> callingBlocks = new LinkedList<>(); // target function
		List<Cons<String, BasicBlock>> afterCallingBlocks = new LinkedList<>(); // from function
		
		BasicBlock current = null;
		String function = "";
		for(int i = beginIndex; i < ir.size();i++){
			IRInstruction instruction = ir.get(i);
			switch(instruction.opcode){
			case LABEL:
				if(table.isFunction(IRRenamer.unrename(instruction.param(0).value))
						|| instruction.param(0).value.equals("main")){
					function = instruction.param(0).value;
				}
				if(current != null) {
					if(current.position != i){
						if(current.jump == null){
							current.successors.add(i);
						}
						blocks.put(current.position, current);
						current = new BasicBlock(i);
					}
				} else {
					current = new BasicBlock(i);
				}
				current.label = instruction;
				enteringBlocks.add(new Cons<>(function, current));
				if(!table.isFunction(IRRenamer.unrename(instruction.param(0).value))){
					labeledBlocks.add(new Cons<>(instruction.param(0).value, current));
				}				
				break;
			case BREQ:
			case BRNEQ:
			case BRLT:
			case BRGT:
			case BRGEQ:
			case BRLEQ:
				if(current != null) {
					current.successors.add(i + 1);
					breakingBlocks.add(new Cons<>(instruction.param(2).value, current));
					blocks.put(current.position, current);
					current.jump = instruction;
				}
				current = new BasicBlock(i + 1);
				break;
			case RETURN:
				if(current != null) {
					returningBlocks.add(new Cons<>(function, current));
					blocks.put(current.position, current);
					current.jump = instruction;
				}
				current = new BasicBlock(i + 1);
				break;
			case CALL:
				current = handleCall(blocks, callingBlocks, afterCallingBlocks,
						current, i, instruction, instruction.param(0).value);
				break;
			case CALLR:
				current = handleCall(blocks, callingBlocks, afterCallingBlocks,
						current, i, instruction, instruction.param(1).value);
				break;
			case GOTO:
				if(current != null) {
					breakingBlocks.add(new Cons<>(instruction.param(0).value, current));
					blocks.put(current.position, current);
					current.jump = instruction;
				}
				current = new BasicBlock(i + 1);
				break;
			default:
				current.instructions.add(instruction);
				break;
			}
		}
		if(current != null){
			blocks.put(current.position, current);
		}

		jumpSuccessors(callingBlocks, enteringBlocks);
		jumpSuccessors(returningBlocks, afterCallingBlocks);
		jumpSuccessors(breakingBlocks, labeledBlocks);
		
		List<BasicBlock> ret = new LinkedList<>();
		for(int i = beginIndex; i < ir.size();i++){
			if(blocks.containsKey(i)){
				ret.add(blocks.get(i));
			}
		}
		return addVariables(ret);
	}

	private static List<BasicBlock> addVariables(List<BasicBlock> ret){
		for(BasicBlock bb : ret){
			
			for(IRInstruction ir : bb.instructions){
				classifyIRInstructionOperands(ir, bb);
			}
			if(bb.jump != null){
				classifyIRInstructionOperands(bb.jump, bb);
			}
		}
		return ret;
	}
	
	private static void classifyIRInstructionOperands(IRInstruction instruction, BasicBlock bb){
//		switch(instruction.opcode){
//		case ASSIGN:
//			Operand possiblyDefined = instruction.param(0);
//			if(!bb.variables.contains(possiblyDefined)){
//				bb.variables.add(possiblyDefined);
//				bb.defFirst.add(possiblyDefined);
//			}
//			if(instruction.param(1).type.equals(EOPERAND.VARIABLE) 
//					|| instruction.param(1).type.equals(EOPERAND.REGISTER)){
//				
//			}
//			break;
//		ASSIGN, ADD, SUB, MULT, DIV, AND,
//		OR, GOTO, BREQ, BRNEQ, BRLT, BRGT, BRGEQ, BRLEQ,
//		RETURN, CALL, CALLR, ARRAY_STORE, ARRAY_LOAD,
//		LABEL, META_EXACT;
//		
//		case LABEL:
//			break;
//		}
		for(Operand op : instruction.params){
			if(op.type.equals(EOPERAND.VARIABLE) 
					|| op.type.equals(EOPERAND.REGISTER)){
				bb.variables.add(op);
			}
		}
	}
	
	private static BasicBlock handleCall(Map<Integer, BasicBlock> blocks,
			List<Cons<String, BasicBlock>> callingBlocks,
			List<Cons<String, BasicBlock>> afterCallingBlocks,
			BasicBlock current, int i, IRInstruction instruction,
			String calledFunction) {
		if(current != null) {
			callingBlocks.add(new Cons<>(calledFunction, current));
			blocks.put(current.position, current);
			current.jump = instruction;
		}
		current = new BasicBlock(i + 1);
		afterCallingBlocks.add(new Cons<>(calledFunction, current));
		return current;
	}

	private static void jumpSuccessors(
			List<Cons<String, BasicBlock>> sourceBlocks,
			List<Cons<String, BasicBlock>> targetBlocks) {
		for(Cons<String, BasicBlock> source : sourceBlocks){
			for(Cons<String, BasicBlock> target : targetBlocks){
				if(source.a.equals(target.a)){
					source.b.successors.add(target.b.position);
					break;
				}
			}
		}
	}
	
	@Override
	public String toString(){
		String out = position + " to " + successors + "\n";
		out += "VARIABLES:\t" + variables + "\n";
		out += "LABEL:\t" + label + "\n";
		for(IRInstruction instruction : instructions){
			out += instruction + "\n";
		}
		out += "JUMP: " + jump + "\n";
		return out;
	}
}
