package CodeGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.IRInstruction.Operand;

class ExtendedBasicBlock{
	BasicBlock root;
	Set<BasicBlock> blocks;
	Set<BasicBlock> exits;	
	private Map<Operand, Set<Integer>> used;
	private Map<Operand, Set<Integer>> defined;
	private Set<BasicBlock> allBlocks;
	private Set<Operand> variables;
	
	public ExtendedBasicBlock(BasicBlock root){
		this.root = root;
		blocks = new HashSet<>();
		exits = new HashSet<>();
	}
	
	public void realize(){
		used = new HashMap<>();
		for(Operand key : root.getUsed().keySet()){
			used.put(key, root.getUsed().get(key));
		}
		for(BasicBlock block : blocks){
			for(Operand key : block.getUsed().keySet()){
				used.put(key, block.getUsed().get(key));
			}
		}
		defined = new HashMap<>();
		for(Operand key : root.getDefined().keySet()){
			defined.put(key, root.getDefined().get(key));
		}
		for(BasicBlock block : blocks){
			for(Operand key : block.getDefined().keySet()){
				defined.put(key, block.getDefined().get(key));
			}
		}
		allBlocks = new HashSet<>();
		allBlocks.add(root);
		for(BasicBlock bb : blocks){
			allBlocks.add(bb);
		}
		variables = new HashSet<>();
		variables.addAll(root.getVariables());
		for(BasicBlock bb : blocks){
			variables.addAll(bb.getVariables());
		}
	}
	
	public Map<Operand, Set<Integer>> getUsed(){
		return used;
	}
	
	public Map<Operand, Set<Integer>> getDefined(){
		return defined;
	}
	
	public Set<BasicBlock> allBlocks(){
		return allBlocks;
	}
	
	public Set<Operand> getVariables(){
		return variables;
	}
	
	@Override
	public String toString(){
		List<Integer> positions = new LinkedList<>();
		for(BasicBlock block : blocks){
			positions.add(block.position);
		}
		List<Integer> exitPositions = new LinkedList<>();
		for(BasicBlock block : exits){
			exitPositions.add(block.position);
		}
		String out = "ExtendedBasicBlock: " + root.position + " and " + positions 
				+ " exiting on " + exitPositions + "\n";
		out += "DEFINED:\t" + defined + "\n";
		out += "USED:\t" + used + "\n";
		out += "ROOT:\n" + root + "\n";
		for(BasicBlock block : blocks){
			out += block;
		}
		return out;
	}
}
