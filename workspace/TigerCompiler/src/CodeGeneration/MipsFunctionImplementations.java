package CodeGeneration;

import java.util.List;

import General.Configuration;
import General.EIROPCODE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;

public class MipsFunctionImplementations {

	public static void implementPrinti(List<IRInstruction> instructions){
		int afterAssignIndex = 0;
		while(instructions.get(afterAssignIndex).opcode.equals(EIROPCODE.ASSIGN)){
			afterAssignIndex++;
		}
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.LABEL, 
				new Operand(EOPERAND.LABEL, Configuration.GLOBAL_SCOPE_NAME
						+ Configuration.SCOPE_DELIMITER
						+ "printi")));
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tla $a0"), new Operand(EOPERAND.VARIABLE, 
						Configuration.GLOBAL_SCOPE_NAME
						+ Configuration.SCOPE_DELIMITER
						+ "printi"
						+ Configuration.SCOPE_DELIMITER
						+ "param0")));
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tlw $a0, 0($a0)")));
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tli $v0, 1")));
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.META_EXACT, 
				new Operand(EOPERAND.LITERAL, "\tsyscall")));
		instructions.add(afterAssignIndex++, new IRInstruction(EIROPCODE.RETURN));

	}

}
