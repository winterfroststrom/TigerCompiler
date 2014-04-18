package CodeGeneration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BBtoEBB {
	public static Map<BasicBlock, ExtendedBasicBlock> convert(Map<Integer, BasicBlock> blocks){
		List<ExtendedBasicBlock> ebbs = new LinkedList<>();
		Set<BasicBlock> roots = new HashSet<>();
		for(BasicBlock block : blocks.values()){
			if(block.predecessors.size() > 1 || block.predecessors.size() < 1){
				ebbs.add(new ExtendedBasicBlock(block));
				roots.add(block);
			}
		}
		bfs(blocks, ebbs, roots);
		addExits(blocks, ebbs);
		for(ExtendedBasicBlock ebb : ebbs){
			ebb.realize();
		}return toMap(ebbs);
	}

	private static Map<BasicBlock, ExtendedBasicBlock> toMap(
			List<ExtendedBasicBlock> ebbs) {
		Map<BasicBlock, ExtendedBasicBlock> map = new HashMap<>();
		for(ExtendedBasicBlock ebb : ebbs){
			for(BasicBlock block : ebb.allBlocks()){
				map.put(block, ebb);
			}
		}
		return map;
	}

	private static void addExits(Map<Integer, BasicBlock> blocks,
			List<ExtendedBasicBlock> ebbs) {
		for(ExtendedBasicBlock ebb : ebbs){
			Set<BasicBlock> allBlocks = new HashSet<>();
			allBlocks.add(ebb.root);
			for(BasicBlock bb : ebb.blocks){
				allBlocks.add(bb);
			}
			for(BasicBlock block : allBlocks){
				for(int successorIndex : block.successors){
					BasicBlock successor = blocks.get(successorIndex);
					if(!ebb.blocks.contains(successor)){
						ebb.exits.add(block);
					}
				}	
			}
		}
	}

	private static void bfs(Map<Integer, BasicBlock> blocks,
			List<ExtendedBasicBlock> ebbs, Set<BasicBlock> roots) {
		for(ExtendedBasicBlock ebb : ebbs){
			Set<BasicBlock> visited = new HashSet<>();
			List<BasicBlock> ds = new LinkedList<>();
			ds.add(ebb.root);
			while(!ds.isEmpty()){
				BasicBlock current = ds.remove(0);
				if(visited.contains(current)){
					continue;
				} else if(roots.contains(current) && !current.equals(ebb.root)){
					continue;
				} else if(!current.equals(ebb.root)){
					ebb.blocks.add(current);
				}
				visited.add(current);
				for(int successorIndex : current.successors){
					ds.add(blocks.get(successorIndex));
				}
			}
		}
	}
}