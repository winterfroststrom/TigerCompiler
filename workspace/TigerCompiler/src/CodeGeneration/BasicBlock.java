package CodeGeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Cons;
import General.EIROPCODE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class BasicBlock{
	int position; 
	List<IRInstruction> instructions;
	List<Integer> predecessors;
	List<Integer> successors;
	IRInstruction label;
	IRInstruction jump;
	Set<Operand> in;
	Set<Operand> out;
	private Set<Operand> variables;
	private Set<Operand> used;
	private Set<Operand> defined;

	
	public BasicBlock(int position){
		this.position = position;
		instructions = new ArrayList<>();
		successors = new LinkedList<>();
		predecessors = new LinkedList<>();
		variables = new HashSet<>();
		in = new HashSet<>();
		out = new HashSet<>();
		used = new HashSet<>();
		defined = new HashSet<>();
	}
	
	/**
	 * 
	 * @param ir 
	 * @param beginIndex
	 * @param table
	 * @return this second return parameter depends on irinstructions being unchanged 
	 */
	public static Cons<Map<Integer, BasicBlock>, Map<IRInstruction, String>> parseIR(List<IRInstruction> ir, int beginIndex, SymbolTable table){
		Map<Integer, BasicBlock> blocks = new HashMap<>(); 
		Map<IRInstruction, String> functionMap = new HashMap<>();
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
					functionMap.put(instruction, function);
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
		
		addPredecessors(blocks);
		
		addVariables(blocks);
		LivelinessAnalysis.computeInOut(blocks);
		return new Cons<>(blocks, functionMap);
	}

	private static void addVariables(Map<Integer, BasicBlock> ret){
		for(BasicBlock bb : ret.values()){
			int i = 0;
			for(i = 0; i < bb.instructions.size();i++){
				classifyIRInstructionOperands(bb.instructions.get(i), bb, i);
			}
			if(bb.jump != null){
				classifyIRInstructionOperands(bb.jump, bb, i);
			}
		}
	}
	
	private static void addPredecessors(Map<Integer, BasicBlock> blocks){
		for(int bbIndex : blocks.keySet()){
			for(int successor : blocks.get(bbIndex).successors){
				blocks.get(successor).predecessors.add(bbIndex);
			}
		}
	}
	
	
	private static void classifyIRInstructionOperands(IRInstruction instruction, BasicBlock bb, int position){
		for(Operand op : instruction.getUsed()){
			if(!bb.defined.contains(op)){
				bb.used.add(op);
			}
		}
		for(Operand op : instruction.getDefined()){
			bb.defined.add(op);
		}
		for(int i = 0; i < instruction.params.size();i++){
			tryAddTo(bb.variables, instruction.param(i));
		}
	}
	
	private static void tryAddTo(Set<Operand> variables, Operand op) {
		if(op.type.equals(EOPERAND.VARIABLE) 
				|| op.type.equals(EOPERAND.REGISTER)){
			variables.add(op);
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
	
	public Set<Operand> getUsed(){
		return used;
	}
	
	public Set<Operand> getDefined(){
		return defined;
	}
	
	public Set<Operand> getVariables(){
		return variables;
	}
	
	public List<IRInstruction> allInstructions(){
		List<IRInstruction> ret = new ArrayList<>();
		if(label != null){
			ret.add(label);
		}
		for(IRInstruction ins : instructions){
			ret.add(ins);
		}
		if(jump != null){
			ret.add(jump);
		}
		return ret;
	}
		
	public static List<BasicBlock> order(Map<Integer, BasicBlock> blocks){
		List<BasicBlock> blockList = new LinkedList<>();
		List<Integer> keys = new ArrayList<>();
		for(int i : blocks.keySet()){
			keys.add(i);
		}
		Collections.sort(keys);
		for(int i : keys){
				blockList.add(blocks.get(i));
		}
		return blockList;
	}
	
	public static List<Integer> positions(Collection<BasicBlock> blocks){
		List<Integer> positions = new LinkedList<>();
		for(BasicBlock bb : blocks){
			positions.add(bb.position);
		}
		return positions;
	}
	
	@Override
	public String toString(){
		String out = predecessors + " to " + position + " to " + successors + "\n";
		out += "IN:\t" + in + "\n";
		out += "OUT:\t" + this.out + "\n";
		out += "DEFINED:\t" + defined + "\n";
		out += "USED:\t" + used + "\n";
		out += "LABEL:\t" + label + "\n";
		for(IRInstruction instruction : instructions){
			out += instruction + "\n";
		}
		out += "JUMP: " + jump + "\n";
		return out;
	}

	public IRInstruction lastInstruction() {
		if(jump != null){
			return jump;
		} else if(instructions.size() > 0){
			return instructions.get(instructions.size() - 1);
		} else {
			return label;
		}
	}
}
