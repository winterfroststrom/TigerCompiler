package CodeGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.IRInstruction;
import General.IRInstruction.Operand;

class ExtendedBasicBlock{
	BasicBlock root;
	Set<BasicBlock> blocks;
	Set<BasicBlock> exits;
	private Set<Operand> in;
	private Set<Operand> out;
	private Set<BasicBlock> allBlocks;
	private Set<Operand> variables;
	
	public ExtendedBasicBlock(BasicBlock root){
		this.root = root;
		blocks = new HashSet<>();
		exits = new HashSet<>();
	}
	
	public void realize(){
		//only need root since there is only one entry point
		in = new HashSet<>(); 
		for(Operand op : root.in){
			in.add(op);
		}
		
		out = new HashSet<>();
		//  should only need to care about outs of exits because otherwise registers are still passing
		for(BasicBlock block : exits){
			for(Operand op : block.out){
				out.add(op);
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
	
	public Set<Operand> getIn(){
		return in;
	}
	
	public Set<Operand> getOut(){
		return out;
	}
	
	public Set<BasicBlock> allBlocks(){
		return allBlocks;
	}
	
	public Set<Operand> getVariables(){
		return variables;
	}
	
	public List<IRInstruction> allInstructions(){
		List<IRInstruction> all = new LinkedList<>();
		for(BasicBlock block : allBlocks()){
			for(IRInstruction instruction : block.allInstructions()){
				all.add(instruction);
			}
		}
		return all;
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
		out += "IN:\t" + in + "\n";
		out += "OUT:\t" + out + "\n";
		out += "ROOT:\n" + root + "\n";
		for(BasicBlock block : blocks){
			out += block;
		}
		return out;
	}
}
