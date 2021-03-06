package CodeGeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import General.Configuration;
import General.Cons;
import General.IRInstruction;
import General.IRInstruction.Operand;
import General.Utilities;

public class LivelinessAnalysis {
	public static void computeInOut(Map<Integer, BasicBlock> bbs){
		boolean changed;
		do{
			changed = false;
			for(Integer i : bbs.keySet()){
				BasicBlock bb = bbs.get(i);
				// in = (out - defined) + used
				Set<Operand> newIn = new HashSet<>();
				for(Operand out : bb.out){
					newIn.add(out);
				}
				for(Operand op : bb.getDefined()){
					newIn.remove(op);
				}
				for(Operand op : bb.getUsed()){
					newIn.add(op);
				}
				for(Operand op : newIn){
					if(!bb.in.contains(op)){
						bb.in.add(op);
						changed = true;
					}
				}
				
				// out = Union successor.in
				for(Integer successorIndex : bb.successors){
					BasicBlock successor = bbs.get(successorIndex);
					for(Operand in : successor.in){
						if(!bb.out.contains(in)){
							bb.out.add(in);
							changed = true;
						}
					}
				}
				
			}
		} while(changed);
	}
	public static Map<IRInstruction, Map<Operand, String>> analyze(ExtendedBasicBlock ebb){
		List<IRInstruction> instructions = ebb.allInstructions();
		Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap = 
				initializeVirtualRegisterMap(instructions);
		
		int registerNum = reverseDuChainAnalysis(ebb, instructions, virtualRegisterMap);
		
		Map<IRInstruction, Map<Operand, String>> registerMap = colorRegisters(
				instructions, virtualRegisterMap, registerNum);
		
		return registerMap;
	}	
	
	public static Map<IRInstruction, Map<Operand, String>> analyze(BasicBlock bb){
		List<IRInstruction> instructions = bb.allInstructions();
		
//		Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap = 
//				initializeVirtualRegisterMap(instructions);
//		int registerNum = duChainAnalysis(bb, instructions, virtualRegisterMap);
//				
//		Map<IRInstruction, Map<Operand, String>> registerMap = colorRegisters(
//				instructions, virtualRegisterMap, registerNum);

		Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap = 
				initializeVirtualRegisterMap(instructions);
		int registerNum = reverseDuChainAnalysis(bb, instructions, virtualRegisterMap);		

		Map<IRInstruction, Map<Operand, String>> registerMap = colorRegisters(
				instructions, virtualRegisterMap, registerNum);
		
		return registerMap;
	}
	private static Map<IRInstruction, Map<Operand, String>> colorRegisters(
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			int registerNum) {
		Map<Integer, Set<Integer>> graph = createInterferenceGraph(
				instructions, virtualRegisterMap, registerNum);
		
		Map<Integer, String> virtualRegisterColoring = 
				GraphColor.graphColor(Configuration.TEMP_REGISTERS, graph);
		
		Map<IRInstruction, Map<Operand, String>> registerMap = createRegisterMap(
				instructions, virtualRegisterMap, virtualRegisterColoring);
		return registerMap;
	}


	private static Map<IRInstruction, Map<Operand, Integer>> initializeVirtualRegisterMap(
			List<IRInstruction> instructions) {
		Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap = new HashMap<>();
		virtualRegisterMap.put(null, new HashMap<Operand, Integer>());
		for(IRInstruction instruction : instructions){
			virtualRegisterMap.put(instruction, new HashMap<Operand, Integer>());
			
		}
		return virtualRegisterMap;
	}

	private static int reverseDuChainAnalysis(ExtendedBasicBlock ebb,
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap) {
		int registerNum = 0;
		
		Map<Operand, Integer> firstVirtualMap = virtualRegisterMap.get(null);
		for(Operand op : ebb.root.in){
			firstVirtualMap.put(op, registerNum++);
		}

		List<Cons<Map<Operand, Integer>, BasicBlock>> queue = new LinkedList<>();
		Set<BasicBlock> visited = new HashSet<>();
		queue.add(new Cons<>(firstVirtualMap, ebb.root));
		while(!queue.isEmpty()){
			Cons<Map<Operand, Integer>, BasicBlock> current = queue.remove(0);
			BasicBlock bb = current.b;
			if(visited.contains(bb)){
				continue;
			}
			visited.add(bb);
			registerNum = reverseDuChainAnalysis(registerNum, bb, bb.allInstructions(), 
					virtualRegisterMap, current.a);
			for(int successorIndex : bb.successors){
				for(BasicBlock possibleSuccessor : ebb.blocks){
					if(possibleSuccessor.position == successorIndex){
						queue.add(new Cons<>(virtualRegisterMap.get(bb.lastInstruction()), possibleSuccessor));
					}
				}
			}
		}
		
		return registerNum;
	}

	private static int reverseDuChainAnalysis(BasicBlock bb,
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap){
		int registerNum = 0;
		Map<Operand, Integer> firstVirtualMap = virtualRegisterMap.get(null);
		for(Operand op : bb.in){
			firstVirtualMap.put(op, registerNum++);
		}
		registerNum = reverseDuChainAnalysis(registerNum, bb, instructions, virtualRegisterMap, firstVirtualMap);
		return registerNum;
	}
	
	private static int reverseDuChainAnalysis(int registerNum, BasicBlock bb,
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			Map<Operand, Integer> firstVirtualMap){
		Map<IRInstruction, Set<Operand>> live = liveAtInstructions(bb, instructions);
		for(int i = 0; i < instructions.size();i++){
			IRInstruction instruction = instructions.get(i);
			Map<Operand, Integer> previousVirtualMap;
			if(i == 0){
				previousVirtualMap = firstVirtualMap;
			} else {
				IRInstruction previous = instructions.get(i - 1);
				previousVirtualMap = virtualRegisterMap.get(previous);
			}
			
			Map<Operand, Integer> virtualMap = virtualRegisterMap.get(instruction);
			Set<Operand> liveAtInstruction = live.get(instruction);
			
			for(Operand op : previousVirtualMap.keySet()){
				if(liveAtInstruction.contains(op)){
					virtualMap.put(op, previousVirtualMap.get(op));	
				}
			}
			
			for(Operand defined : instruction.getDefined()){
				if(!instruction.getUsed().contains(defined)){
					virtualMap.put(defined, registerNum++);
				}
			}
		}
		return registerNum;
	}
	
	private static int duChainAnalysis(BasicBlock bb,
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap) {
		int registerNum = 0;
		for(int i = instructions.size() - 1; i >= 0; i--){
			IRInstruction instruction = instructions.get(i);
			Map<Operand, Integer> virtualMap = virtualRegisterMap.get(instruction);
			
			IRInstruction next = i - 1 < 0 ? null : instructions.get(i - 1);
			Map<Operand, Integer> lastVirtualMap = virtualRegisterMap.get(next);
			
			if(i == instructions.size() - 1){
				for(Operand op : bb.out){
					virtualMap.put(op, registerNum++);	
				}
			} else{
				IRInstruction last = instructions.get(i + 1);
				registerNum = duChain(registerNum, instruction, last,
						virtualMap);
			}
			// propagate to next instruction
			for(Operand op : virtualMap.keySet()){
				lastVirtualMap.put(op, virtualMap.get(op));
			}
		}
		duChainEnd(instructions.get(0), virtualRegisterMap.get(null));
		return registerNum;
	}

	private static int duChain(int registerNum, IRInstruction instruction,
			IRInstruction last, Map<Operand, Integer> virtualMap) {
		duChainEnd(last, virtualMap);

		// if live, then still live, if dead, then now live with new chain
		for(Operand used : instruction.getUsed()){
			if(!virtualMap.containsKey(used)){
				virtualMap.put(used, registerNum++);
			}
		}
		return registerNum;
	}


	private static void duChainEnd(IRInstruction last,
			Map<Operand, Integer> virtualMap) {
		// if live then i - 1 is dead, if dead then still dead
		for(Operand defined : last.getDefined()){
			if(!last.getUsed().contains(defined)){
				virtualMap.remove(defined);
			}
		}
	}
	private static Map<IRInstruction, Map<Operand, String>> createRegisterMap(
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			Map<Integer, String> virtualRegisterColoring) {
		Map<IRInstruction, Map<Operand, String>> registerMap = new HashMap<>();
		for(IRInstruction instruction : instructions){
			registerMap.put(instruction, new HashMap<Operand, String>());
		}
		registerMap.put(null, new HashMap<Operand, String>());
		
		reifyColoring(instructions, virtualRegisterMap,
				virtualRegisterColoring, registerMap);
		return registerMap;
	}


	private static Map<Integer, Set<Integer>> createInterferenceGraph(
			List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			int registerNum) {
		Map<Integer, Set<Integer>> graph = new HashMap<>();
		for(int i = 0; i < registerNum; i++){
			graph.put(i, new HashSet<Integer>());
		}
		
		for(IRInstruction instruction : instructions){
			addConflictsToGraph(virtualRegisterMap, graph, instruction);
		}
		addConflictsToGraph(virtualRegisterMap, graph, null);
		return graph;
	}


	private static void addConflictsToGraph(
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			Map<Integer, Set<Integer>> graph, IRInstruction instruction) {
		Map<Operand, Integer> virtualMap = virtualRegisterMap.get(instruction);
		List<Integer> conflictingRegisters = new ArrayList<>();
		for(Integer i : virtualMap.values()){
			conflictingRegisters.add(i);
		}
		for(int i1 = 0; i1 < conflictingRegisters.size();i1++){
			for(int j = 0; j < conflictingRegisters.size();j++){
				if(i1 != j){
					graph.get(conflictingRegisters.get(i1)).add(conflictingRegisters.get(j));
				}
			}	
		}
	}


	private static void reifyColoring(List<IRInstruction> instructions,
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			Map<Integer, String> virtualRegisterColoring,
			Map<IRInstruction, Map<Operand, String>> registerMap) {
		for(IRInstruction instruction : instructions){
			reifyColoringAtInstruction(virtualRegisterMap,
					virtualRegisterColoring, registerMap, instruction);
		}
		IRInstruction instruction = null;
		reifyColoringAtInstruction(virtualRegisterMap, virtualRegisterColoring,
				registerMap, instruction);
	}


	private static void reifyColoringAtInstruction(
			Map<IRInstruction, Map<Operand, Integer>> virtualRegisterMap,
			Map<Integer, String> virtualRegisterColoring,
			Map<IRInstruction, Map<Operand, String>> registerMap,
			IRInstruction instruction) {
		Map<Operand, Integer> virtualMap = virtualRegisterMap.get(instruction);
		for(Operand op : virtualMap.keySet()){
			Integer virtualRegister = virtualMap.get(op);
			if(virtualRegisterColoring.containsKey(virtualRegister)){
				registerMap.get(instruction).put(op, virtualRegisterColoring.get(virtualRegister));	
			}	
		}
	}

	private static Map<IRInstruction, Set<Operand>> liveAtInstructions(BasicBlock bb, 
			List<IRInstruction> instructions) {
		Map<IRInstruction, Set<Operand>> live = new HashMap<>();
		for(IRInstruction instruction : instructions){
			live.put(instruction, new HashSet<Operand>());
		}
		for(int i = instructions.size() - 1; i >= 0; i--){
			IRInstruction instruction = instructions.get(i);
			Set<Operand> liveAtInstruction = live.get(instruction);
			if(i == instructions.size() - 1){
				liveAtInstruction.addAll(bb.out);
			} else {
				IRInstruction last = instructions.get(i + 1);
				for(Operand op : live.get(last)){
					liveAtInstruction.add(op);
				}
				for(Operand op : instruction.getDefined()){
					liveAtInstruction.remove(op);
				}
				liveAtInstruction.addAll(instruction.getUsed());
			}
		}
		return live;
	}
}
