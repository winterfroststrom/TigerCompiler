package CodeGeneration;

import java.util.List;
import java.util.Map;

import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class RegisterCodeGeneration {
	private static final String RETURN_VALUE_REGISTER = "$v0";
	private static final String SPILL_1_REGISTER = "$a2";
	private static final String SPILL_2_REGISTER = "$a3";

	public static void generate(IRInstruction instruction, SymbolTable table,
			List<String> output, Map<Operand, String> registerMap) {

		output.add("#\t" + instruction);
		switch (instruction.opcode) {
		case LABEL:
			output.add(instruction.toString());
			break;
		case ASSIGN:
			handleAssign(instruction, output, registerMap);
			break;
		case ADD:
			handleOperator("add", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case SUB:
			handleOperator("sub", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case MULT:
			handleOperator("mul", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case DIV:
			handleOperator("div", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case AND:
			handleOperator("and", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case OR:
			handleOperator("or", instruction.param(0), instruction.param(1),
					instruction.param(2), output, registerMap);
			break;
		case GOTO:
			output.add("\tb " + instruction.param(0).value);
			break;
		case BREQ:
			handleBranch("beq", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case BRNEQ:
			handleBranch("bne", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case BRLT:
			handleBranch("blt", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case BRGT:
			handleBranch("bgt", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case BRGEQ:
			handleBranch("bge", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case BRLEQ:
			handleBranch("ble", instruction.param(0), instruction.param(1),
					instruction.param(2).value, output, registerMap);
			break;
		case RETURN:
			if (instruction.params.size() > 0) {
				handleLoadIntoRegister(instruction.param(0),
						RETURN_VALUE_REGISTER, output, registerMap);
			}
			output.add("\tjr $ra");
			break;
		case CALL:
			handleCall(instruction.param(0).value, output, registerMap);
			break;
		case CALLR:
			handleCall(instruction.param(1).value, output, registerMap);
			if (table.getTypeOfId("", instruction.param(1).value).isArray()) {
				// TODO: implement array returns
				throw new UnsupportedOperationException();
			} else {
				handleStoreLabel(instruction.param(0), RETURN_VALUE_REGISTER,
						output, registerMap);
			}
			break;
		case ARRAY_STORE:
			handleArrayStore(instruction, output, registerMap);
			break;
		case ARRAY_LOAD:
			handleArrayLoad(instruction, output, registerMap);
			break;
		case META_EXACT:
			String out = instruction.param(0).value;
			for (int j = 1; j < instruction.params.size(); j++) {
				out += ", " + instruction.param(j).value;
			}
			output.add(out);
			break;
		}
	}

	private static void handleArrayStore(IRInstruction instruction,
			List<String> output, Map<Operand, String> registerMap) {
		String address = "$a0";
		String offset = "$a1";
		output.add("\tla " + address + ", " + instruction.param(0).value); // address
		offset = handleLoadLiteralOrRegister(instruction.param(1), offset,
				output, registerMap); // load offset
		output.add("\tmul " + offset + ", " + offset + ", 4");
		output.add("\tadd " + address + ", " + address + ", " + offset); // add
																			// offset
																			// to
																			// address
		String value = "$a1";
		value = handleLoadLiteralOrRegister(instruction.param(2), value,
				output, registerMap); // load new value
		// store value into array
		output.add("\tsw " + value + ", 0(" + address + ")");
	}

	private static void handleArrayLoad(IRInstruction instruction,
			List<String> output, Map<Operand, String> registerMap) {
		String labelAddress = "$a0";
		String offset = "$a1";
		output.add("\tla " + labelAddress + ", " + instruction.param(1).value); // address
		offset = handleLoadLiteralOrRegister(instruction.param(2), offset,
				output, registerMap);
		output.add("\tmul " + offset + ", " + offset + ", 4");
		output.add("\tadd " + labelAddress + ", " + labelAddress + ", "
				+ offset); // add offset
		// load value from address + offset into register
		if(registerMap.containsKey(instruction.param(0))){
			output.add("\tlw "
					+ registerMap.get(instruction.param(0))
					+ ", 0(" + labelAddress + ")"); 
			
		} else {
			output.add("\tlw "
					+ "$a0"
					+ ", 0(" + labelAddress + ")"); 
			storeIntoRegisterMapRegister(instruction.param(0), "$a0", output, registerMap);	
		}
	}

	private static void handleCall(String function, List<String> output,
			Map<Operand, String> registerMap) {
		saveRegister("$ra", output);
		output.add("\tjal " + function);
		restoreRegister("$ra", output);
	}

	private static void handleBranch(String branch, Operand operand1,
			Operand operand2, String label, List<String> output,
			Map<Operand, String> registerMap) {

		output.add("\t"
				+ branch
				+ " "
				+ handleLoadLiteralOrRegister(operand1, "$a0", output,
						registerMap)
				+ ", "
				+ handleLoadLiteralOrRegister(operand2, "$a1", output,
						registerMap) + ", " + label);
	}

	private static void handleOperator(String operator, Operand destination,
			Operand operand1, Operand operand2, List<String> output,
			Map<Operand, String> registerMap) {
		if (registerMap.containsKey(destination)) {
			output.add("\t"
					+ operator
					+ " "
					+ registerMap.get(destination)
					+ ", "
					+ handleLoadLiteralOrRegister(operand1, "$a0", output,
							registerMap)
					+ ", "
					+ handleLoadLiteralOrRegister(operand2, "$a1", output,
							registerMap));
		} else {
			output.add("\t"
					+ operator
					+ " "
					+ "$a0"
					+ ", "
					+ handleLoadLiteralOrRegister(operand1, "$a0", output,
							registerMap)
					+ ", "
					+ handleLoadLiteralOrRegister(operand2, "$a1", output,
							registerMap));
			storeIntoRegisterMapRegister(destination, "$a0", output, registerMap);
		}
	}

	private static void handleLoadIntoRegister(Operand operand,
			String destination, List<String> output,
			Map<Operand, String> registerMap) {
		if (operand.type.equals(EOPERAND.LITERAL)) {
			output.add("\taddi " + destination + ", $zero, " + operand.value);
		} else {
			output.add("\tmove "
					+ destination
					+ ", "
					+ loadFromRegisterMap(operand, destination, output,
							registerMap));
		}

	}

	private static String handleLoadLiteralOrRegister(Operand operand,
			String destination, List<String> output,
			Map<Operand, String> registerMap) {
		if (operand.type.equals(EOPERAND.LITERAL)) {
			output.add("\taddi " + destination + ", $zero, " + operand.value);
			return destination;
		} else {
			return loadFromRegisterMap(operand, destination, output,
					registerMap);
		}
	}

	private static void handleStoreLabel(Operand label, String source,
			List<String> output, Map<Operand, String> registerMap) {
		storeIntoRegisterMapRegister(label, source, output, registerMap);
	}

	private static void handleAssign(IRInstruction instruction,
			List<String> output, Map<Operand, String> registerMap) {
		storeIntoRegisterMapGeneral(instruction.param(0), instruction.param(1),
				output, registerMap);
	}

	public static String loadFromRegisterMap(Operand op, String spill,
			List<String> output, Map<Operand, String> registerMap) {
		if (registerMap.containsKey(op)) {
			return registerMap.get(op);
		} else {
			output.add("\tla " + spill + ", " + op.value);
			output.add("\tlw " + spill + ", 0(" + spill + ")");
			return spill;
		}
	}

	public static void storeIntoRegisterMapGeneral(Operand destination,
			Operand value, List<String> output, Map<Operand, String> registerMap) {
		if (registerMap.containsKey(destination)) {
			String desintationRegister = registerMap.get(destination);
			switch (value.type) {
			case LITERAL:
				output.add("\taddi " + desintationRegister + ", $zero, "
						+ value.value);
				break;
			case VARIABLE:
			case REGISTER:
				output.add("\tmove "
						+ desintationRegister
						+ ", "
						+ loadFromRegisterMap(value, "$a2", output, registerMap));
				break;
			case LABEL:
				System.err.println("NMG HSL: This should never happen.");
				break;
			}
		} else {
			String spill1 = "$a2";
			String spill2 = "$a3";
			output.add("sw " + spill1 + ", 0($sp)");
			output.add("sw " + spill2 + ", -4($sp)");
			output.add("addi $sp, $sp, -8");

			switch (value.type) {
			case LITERAL:
				output.add("\taddi " + spill1 + ", $zero, " + value.value);
				break;
			case VARIABLE:
			case REGISTER:
				output.add("\tmove "
						+ spill1
						+ ", "
						+ loadFromRegisterMap(value, spill1, output,
								registerMap));
				break;
			case LABEL:
				System.err.println("NMG HSL: This should never happen.");
				break;
			}
			output.add("\tla " + spill2 + ", " + destination.value);
			output.add("\tsw " + spill1 + ", 0(" + spill2 + ")");
			output.add("lw " + spill1 + ", 4($sp)");
			output.add("lw " + spill2 + ", 8($sp)");
			output.add("addi $sp, $sp, 8");

		}
	}

	public static void storeIntoRegisterMapLiteral(Operand destination,
			String value, List<String> output, Map<Operand, String> registerMap) {
		if (registerMap.containsKey(destination)) {
			String desintationRegister = registerMap.get(destination);
			output.add("\taddi " + desintationRegister + ", $zero, " + value);
		} else {
			storeSpillPreamble(output);
			output.add("\taddi " + SPILL_1_REGISTER + ", $zero, " + value);
			storeSpillEpilogue(destination, output);
		}
	}

	public static void storeIntoRegisterMapOperand(Operand destination,
			Operand value, List<String> output, Map<Operand, String> registerMap) {
		if (registerMap.containsKey(destination)) {
			String desintationRegister = registerMap.get(destination);
			if (registerMap.containsKey(value)) {
				output.add("\tmove " + desintationRegister + ", "
						+ registerMap.get(value));
			} else {
				saveRegister(SPILL_1_REGISTER, output);
				output.add("\tla " + SPILL_1_REGISTER + ", " + value.value);
				output.add("\tlw " + SPILL_1_REGISTER + ", 0("
						+ SPILL_1_REGISTER + ")");
				output.add("\tmove " + desintationRegister + ", "
						+ SPILL_1_REGISTER);
				restoreRegister(SPILL_1_REGISTER, output);
			}
		} else {
			storeSpillPreamble(output);
			output.add("\tmove "
					+ SPILL_1_REGISTER
					+ ", "
					+ loadFromRegisterMap(value, SPILL_1_REGISTER, output,
							registerMap));
			storeSpillEpilogue(destination, output);
		}
	}

	public static void storeIntoRegisterMapRegister(Operand destination,
			String register, List<String> output,
			Map<Operand, String> registerMap) {
		if (registerMap.containsKey(destination)) {
			String desintationRegister = registerMap.get(destination);
			output.add("\tmove " + desintationRegister + ", " + register);
		} else {
			storeSpillPreamble(output);
			output.add("\tmove " + SPILL_1_REGISTER + ", " + register);
			storeSpillEpilogue(destination, output);
		}
	}

	private static void storeSpillEpilogue(Operand destination,
			List<String> output) {
		output.add("\tla " + SPILL_2_REGISTER + ", " + destination.value);
		restoreRegister(SPILL_2_REGISTER, output);
		restoreRegister(SPILL_1_REGISTER, output);
	}

	private static void storeSpillPreamble(List<String> output) {
		saveRegister(SPILL_1_REGISTER, output);
		saveRegister(SPILL_2_REGISTER, output);
	}

	private static void saveRegister(String register, List<String> output) {
		output.add("sw " + register + ", 0($sp)");
		output.add("addi $sp, $sp, -4");
	}

	private static void restoreRegister(String register, List<String> output) {
		output.add("lw " + register + ", 4($sp)");
		output.add("addi $sp, $sp, 4");
	}

}
