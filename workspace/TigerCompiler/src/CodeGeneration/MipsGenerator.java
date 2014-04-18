package CodeGeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import General.Configuration;
import General.Cons;
import General.EIROPCODE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

public class MipsGenerator {
	public enum EGENERATOR{
		NAIVE, BB, EBB
	}
	public List<String> generate(EGENERATOR generator, List<IRInstruction> instructions, SymbolTable table){
		instructions = copy(instructions); 
		MipsFunctionImplementations.implementLibrary(instructions);
		Cons<List<IRInstruction>, Set<String>> renamed = IRRenamer.renameIR(instructions);
		instructions = renamed.a;
		
		instructions.add(new IRInstruction(EIROPCODE.META_EXACT, new Operand(EOPERAND.LITERAL, "\tjr $ra")));
		
		List<String> output = new LinkedList<>();
		if(Configuration.MIPS_COMMENTS){
			output.add("#\tBEGIN CODE GEN");
		}
		
		int instructionIndex = handleAssignment(instructions, output, renamed.b, table);
		switch(generator){
		case NAIVE:
			return (new NaiveMipsGenerator(output)).generate(instructions, table, instructionIndex);
		case BB:
			return (new BBMipsGenerator(output)).generate(instructions, table, instructionIndex);
		case EBB:
			return (new EBBMipsGenerator(output)).generate(instructions, table, instructionIndex);
		default:
			throw new UnsupportedOperationException("Uknown generator type");
		}
	}
	
	private List<IRInstruction> copy(List<IRInstruction> instructions) {
		List<IRInstruction> copy = new ArrayList<>();
		for(IRInstruction instruction : instructions){
			copy.add(new IRInstruction(instruction.opcode, instruction.params));
		}
		return copy;
	}

	private int handleAssignment(List<IRInstruction> instructions, List<String> output, 
			Set<String> variables, SymbolTable table) {
		int instructionIndex = 0;
		output.add("\t.data");
		Set<String> params = IRRenamer.rename(table.allFunctionParamNames());
		while(instructions.get(instructionIndex).opcode.equals(EIROPCODE.ASSIGN)){
			IRInstruction instruction = instructions.get(instructionIndex);
			String assignment = instruction.param(0).value + ":\t .word ";
			params.remove(instruction.param(0).value);
			if(instruction.params.size() == 3){
				int times = Integer.parseInt(instruction.param(1).value);
				for(int i = 0; i < times - 1;i++){
					assignment += instruction.param(2) + ", ";
				}
				assignment += instruction.param(2);
			} else {
				assignment += instruction.param(1);
			}
			output.add(assignment);
			instructionIndex++;
			variables.remove(instruction.param(0).value);
		}
		for(String variable : variables){
			params.remove(variable);
			output.add(variable + ":\t .word 0");
		}
		for(String unassignedParams : params){
			output.add(unassignedParams + ":\t .word 0");
		}
		output.add("\t.text");
		//output.add("\t.global main");
		return instructionIndex;
	}

}
