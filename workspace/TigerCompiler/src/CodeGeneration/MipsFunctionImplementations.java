package CodeGeneration;

import java.util.List;

import General.Configuration;
import General.EIROPCODE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;

public class MipsFunctionImplementations {

	public static void implementLibrary(List<IRInstruction> instructions){
		int instructionIndex = 0;
		while(instructions.get(instructionIndex).opcode.equals(EIROPCODE.ASSIGN)){
			instructionIndex++;
		}instructionIndex = implementPrinti(instructions, instructionIndex);
		
		if(Configuration.LOAD_EXTENDED_LIBRARY){
//			instructionIndex = implementPrint(instructions, instructionIndex); // prints char not string
			instructionIndex = implementGetchar(instructions, instructionIndex);
			instructionIndex = implementNot(instructions, instructionIndex);
			instructionIndex = implementExit(instructions, instructionIndex);
		}
	}

	private static int implementPrint(List<IRInstruction> instructions,
			int instructionIndex) {
		String function = "print";
		instructionIndex = addLabel(instructions, instructionIndex, function);
		instructionIndex = loadParam(instructions, instructionIndex, function, "param0");
		instructionIndex = syscall(instructions, instructionIndex, 11); // 4 for null-terminated string
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		return instructionIndex;
	}
	
	private static int implementPrinti(List<IRInstruction> instructions,
			int instructionIndex) {
		String function = "printi";
		instructionIndex = addLabel(instructions, instructionIndex, function);
		instructionIndex = loadParam(instructions, instructionIndex, function, "param0");
		instructionIndex = syscall(instructions, instructionIndex, 1);
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		return instructionIndex;
	}
	
	private static int implementGetchar(List<IRInstruction> instructions,
			int instructionIndex) {
		String function = "getchar";
		instructionIndex = addLabel(instructions, instructionIndex, function);
		instructionIndex = syscall(instructions, instructionIndex, 12);
		instructionIndex = loadReturn(instructions, instructionIndex);
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		return instructionIndex;
	}
	
	
	private static int implementNot(List<IRInstruction> instructions,
			int instructionIndex) {
		String function = "not";
		instructionIndex = addLabel(instructions, instructionIndex, function);
		instructionIndex = loadParam(instructions, instructionIndex, function, "param0");
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "beq $a0, $zero, not0")));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "addi $v0, $zero, 1")));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "not0: ")));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "addi $v0, $zero, 0")));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		return instructionIndex;
	}
	
	private static int implementExit(List<IRInstruction> instructions,
			int instructionIndex) {
		String function = "exit";
		instructionIndex = addLabel(instructions, instructionIndex, function);
		instructionIndex = loadParam(instructions, instructionIndex, function, "param0");
		instructionIndex = syscall(instructions, instructionIndex, 17);
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.RETURN));
		return instructionIndex;
	}
	
	private static int addLabel(List<IRInstruction> instructions,
			int instructionIndex, String function) {
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.LABEL, createFunction(function)));
		return instructionIndex;
	}

	private static int syscall(List<IRInstruction> instructions,
			int instructionIndex, int code) {
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tli $v0, " + code)));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tsyscall")));
		return instructionIndex;
	}

	private static int loadParam(List<IRInstruction> instructions,
			int instructionIndex, String function, String param) {
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tla $a0"), createParam(function, param)));
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tlw $a0, 0($a0)")));
		return instructionIndex;
	}
	
	private static int loadReturn(List<IRInstruction> instructions, int instructionIndex) {
		instructions.add(instructionIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tmove $v0, $a0")));
		return instructionIndex;
	}
	
	private static Operand createFunction(String function){
		return new Operand(EOPERAND.LABEL, Configuration.GLOBAL_SCOPE_NAME
						+ Configuration.SCOPE_DELIMITER
						+ function);
	}
	
	private static Operand createParam(String function, String param){
		return new Operand(EOPERAND.VARIABLE, 
				Configuration.GLOBAL_SCOPE_NAME
				+ Configuration.SCOPE_DELIMITER
				+ function
				+ Configuration.SCOPE_DELIMITER
				+ param);
	}

}
