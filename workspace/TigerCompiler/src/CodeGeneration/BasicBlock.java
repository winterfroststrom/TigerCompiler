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
	private Set<Operand> variables;
	private Map<Operand, Set<Integer>> used;
	private Map<Operand, Set<Integer>> defined;

	
	public BasicBlock(int position){
		this.position = position;
		instructions = new ArrayList<>();
		successors = new LinkedList<>();
		predecessors = new LinkedList<>();
		variables = new HashSet<>();
		used = new HashMap<>();
		defined = new HashMap<>();
	}
	
	public static Map<Integer, BasicBlock> parseIR(List<IRInstruction> ir, int beginIndex, SymbolTable table){
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
		
		addPredecessors(blocks);
		
		addVariables(blocks);
		
		return blocks;
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
		switch(instruction.opcode){
		case ASSIGN:
		case ADD:
		case SUB:
		case MULT:
		case DIV:
		case AND:
		case OR:
		case CALLR:
		case ARRAY_LOAD:
			tryAddTo(bb.defined, instruction.param(0), position);
			for(int i = 1; i < instruction.params.size();i++){
				tryAddTo(bb.used, instruction.param(i), position);	
			}
			break;
		case BREQ:
		case BRNEQ:
		case BRLT:
		case BRGT:
		case BRGEQ:
		case BRLEQ:
		case CALL:
		case ARRAY_STORE:
			for(int i = 0; i < instruction.params.size();i++){
				tryAddTo(bb.used, instruction.param(i), position);	
			}
			break;
		case RETURN:
			if(instruction.params.size() > 0){
				tryAddTo(bb.used, instruction.param(0), position);					
			}
		case GOTO:
			break;
		case META_EXACT:
		case LABEL:
			break;
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
	
	private static void tryAddTo(Map<Operand, Set<Integer>> map, Operand op, int position) {
		if(op.type.equals(EOPERAND.VARIABLE) 
				|| op.type.equals(EOPERAND.REGISTER)){
			if(!map.containsKey(op)){
				map.put(op, new HashSet<Integer>());
			}
			map.get(op).add(position);
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
	
	public Map<Operand, Set<Integer>> getUsed(){
		return used;
	}
	
	public Map<Operand, Set<Integer>> getDefined(){
		return defined;
	}
	
	public Set<Operand> getVariables(){
		return variables;
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
		out += "DEFINED:\t" + defined + "\n";
		out += "USED:\t" + used + "\n";
		out += "LABEL:\t" + label + "\n";
		for(IRInstruction instruction : instructions){
			out += instruction + "\n";
		}
		out += "JUMP: " + jump + "\n";
		return out;
	}
}
