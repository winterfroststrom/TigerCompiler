package CodeGeneration;

import java.util.List;

import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

public class NaiveMipsGenerator {
private List<String> output;
	
	public NaiveMipsGenerator(List<String> output){
		this.output = output;
	}
	
	public List<String> generate(List<IRInstruction> instructions, SymbolTable table, int instructionIndex){
		for(int i = instructionIndex; i < instructions.size();i++){
			IRInstruction instruction = instructions.get(i);
			output.add("#\t" + instruction.toString());
			switch(instruction.opcode){
			case LABEL:
				output.add(instruction.toString());
				break;
			case ASSIGN:
				handleAssign(instruction);
				break;
			case ADD:
				handleOperator("add", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case SUB: 
				handleOperator("sub", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case MULT:
				handleOperator("mul", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case DIV:
				handleOperator("div", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case AND:
				handleOperator("and", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case OR:
				handleOperator("or", instruction.param(0).value, instruction.param(1), instruction.param(2));
				break;
			case GOTO:
				output.add("\tb " + instruction.param(0).value);
				break;
			case BREQ:
				handleBranch("beq", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case BRNEQ:
				handleBranch("bne", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case BRLT:
				handleBranch("blt", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case BRGT:
				handleBranch("bgt", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case BRGEQ:
				handleBranch("bge", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case BRLEQ:
				handleBranch("ble", instruction.param(0), instruction.param(1), instruction.param(2).value);
				break;
			case RETURN:
				if(instruction.params.size() > 0){
					handleLoadOperand(instruction.param(0), "$v0");
				}
				output.add("\tjr $ra");
				break;
			case CALL:
				handleCall(instruction.param(0).value);
				break;
			case CALLR:
				handleCall(instruction.param(0).value);
				String functionName = IRRenamer.unrename(instruction.param(1).value);
				if(table.getTypeOfQualifiedId(functionName).isArray()){
					// TODO: implement array returns
					throw new UnsupportedOperationException();
				} else {
					handleStoreLabel(instruction.param(0).value, "$v0");
				}
				break;
			case ARRAY_STORE:
				output.add("\tla $t1, " + instruction.param(0).value); // address
				handleLoadOperand(instruction.param(2), "$t2");
				output.add("\tmul $t2, $t2, 4");
				output.add("\tadd $t0, $t1, $t2"); // add offset
				output.add("\tsw " + handleLoadOperand(instruction.param(2), "$t3") + ", 0($t0)");
				break;
			case ARRAY_LOAD:
				output.add("\tla $t1, " + instruction.param(1).value); // address
				handleLoadOperand(instruction.param(2), "$t2");
				output.add("\tmul $t2, $t2, 4");
				output.add("\tadd $t0, $t1, $t2"); // add offset
				output.add("\tlw $t3, 0($t0)"); // load value from address + offset
				handleStoreLabel(instruction.param(0).value, "$t3"); // store value into label location
				break;
			case META_EXACT:
				String out = instruction.param(0).value;
				for(int j = 1; j < instruction.params.size();j++){
					out += ", " + instruction.param(j).value;
				}
				output.add(out);
				break;
			}
		}
		return output;
	}

	private void handleCall(String function) {
		output.add("\tsw $ra, 0($sp)");
		output.add("\taddi $sp, $sp, -4");
		output.add("\tjal " + function);
		output.add("\taddi $sp, $sp, 4");
		output.add("\tlw $ra, 0($sp)");
	}

	private void handleBranch(String branch, Operand operand1, Operand operand2,
			String label) {
		output.add("\t" + branch + " "
			+ handleLoadOperand(operand1, "$t0") + ", "
			+ handleLoadOperand(operand2, "$t1") + ", " + label);
	}

	private void handleOperator(String operator, String destination, Operand operand1, Operand operand2){
		output.add("\t" + operator 
				+ " $t0, " 
				+ handleLoadOperand(operand1, "$t1") + ", "
				+ handleLoadOperand(operand2, "$t2"));
		handleStoreLabel(destination, "$t0");		
	}
	
	private String handleLoadImmediate(String immediate, String register){
		output.add("\tli " + register + ", " + immediate);
		return register;
	}
	

	private String handleLoadLabel(String label, String register){
		output.add("\tla $s1, " + label);
		output.add("\tlw " + register + ", 0($s1)");
		return register;
	}

	private String handleLoadOperand(Operand operand, String register){
		if(operand.type.equals(EOPERAND.LITERAL)){
			return handleLoadImmediate(operand.value, register);
		} else {
			return handleLoadLabel(operand.value, register);
		}
	}
	
	private void handleStoreLabel(String label, String register){
		output.add("\tla $s0, " + label);
		output.add("\tsw " + register + ", 0($s0)");
	}
	
	private void handleAssign(IRInstruction instruction) {
		switch(instruction.param(1).type){
		case LITERAL:
			handleStoreLabel(instruction.param(0).value, 
					handleLoadImmediate(instruction.param(1).value, "$t1"));
			break;
		case VARIABLE:
		case REGISTER:
			handleStoreLabel(instruction.param(0).value, 
					handleLoadLabel(instruction.param(1).value, "$t1"));
			break;
		case LABEL:
			System.err.println("NMG HSL: This should never happen.");
			break;
		}
	}
}
