package CodeGeneration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import General.Configuration;
import General.EIROPCODE;
import General.IRInstruction;
import General.IRInstruction.EOPERAND;
import General.IRInstruction.Operand;
import SemanticChecking.SymbolTable;

class RegisterCodeGenerator extends AbstractCodeGenerator{
	private static final String RETURN_VALUE_REGISTER = "$v0";
	private static final String STORE_SPILL_VALUE_REGISTER = "$a2";
	private static final String STORE_SPILL_ADDRESS_REGISTER = "$a3";
	
	private Map<IRInstruction, Map<Operand, String>> instructionRegisterMap;
	private List<String> output;
	private SymbolTable table;
	private Map<IRInstruction, String> functionMap;
	private Map<Operand, String> registerMap;
	
	public RegisterCodeGenerator(Map<IRInstruction, Map<Operand, String>> instructionRegisterMap, 
			List<String> output, SymbolTable table, Map<IRInstruction, String> functionMap){
		this.instructionRegisterMap = instructionRegisterMap;
		this.output = output;
		this.table = table;
		this.functionMap = functionMap;
	}
	
	public void generate(IRInstruction instruction) {
		registerMap = instructionRegisterMap.get(instruction);
		if(Configuration.MIPS_COMMENTS){
			output.add("#\t IRInstruction : " + instruction + "\t" + registerMap);
		}
		switch (instruction.opcode) {
		case LABEL:
			output.add(instruction.toString());
			break;
		case ASSIGN:
			handleAssign(instruction);
			break;
		case ADD:
			handleOperator("add", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case SUB:
			handleOperator("sub", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case MULT:
			handleOperator("mul", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case DIV:
			handleOperator("div", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case AND:
			handleOperator("and", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case OR:
			handleOperator("or", instruction.param(0), instruction.param(1),
					instruction.param(2));
			break;
		case GOTO:
			output.add("\tb " + instruction.param(0).value);
			break;
		case BREQ:
			handleBranch("beq", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case BRNEQ:
			handleBranch("bne", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case BRLT:
			handleBranch("blt", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case BRGT:
			handleBranch("bgt", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case BRGEQ:
			handleBranch("bge", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case BRLEQ:
			handleBranch("ble", instruction.param(0), instruction.param(1),
					instruction.param(2).value);
			break;
		case RETURN:
			if (instruction.params.size() > 0) {
				handleLoadIntoRegister(instruction.param(0), RETURN_VALUE_REGISTER);
			}
			output.add("\tjr $ra");
			break;
		case CALL:
			handleCall(instruction.functionName(), instruction.functionParameters(), table, output);
			break;
		case CALLR:
			handleCall(instruction.functionName(), instruction.functionParameters(), table, output);
			String functionName = IRRenamer.unrename(instruction.functionName());
			if(table.getTypeOfQualifiedId(functionName).isArray()){
				// TODO: implement array returns
				throw new UnsupportedOperationException();
			} else {
				handleStoreFunctionReturnToLabelAndRegister(instruction.param(0));
			}
			break;
		case ARRAY_STORE:
			handleArrayStore(instruction);
			break;
		case ARRAY_LOAD:
			handleArrayLoad(instruction);
			break;
		case META_EXACT:
			String out = instruction.param(0).value;
			for (int j = 1; j < instruction.params.size(); j++) {
				out += ", " + instruction.param(j).value;
			}
			output.add(out);
			break;
		}
		registerMap = null;
	}

	private void handleArrayStore(IRInstruction instruction) {
		String address = "$a0";
		String offset = "$a1";
		output.add("\tla " + address + ", " + instruction.param(0).value); // address
		offset = handleLoadLiteralOrRegister(instruction.param(1), offset); // load offset
		output.add("\tmul " + offset + ", " + offset + ", 4");
		output.add("\tadd " + address + ", " + address + ", " + offset); // add
																			// offset
																			// to
																			// address
		String value = "$a1";
		value = handleLoadLiteralOrRegister(instruction.param(2), value); // load new value
		// store value into array
		output.add("\tsw " + value + ", 0(" + address + ")");
	}

	private void handleArrayLoad(IRInstruction instruction) {
		String labelAddress = "$a0";
		String offset = "$a1";
		output.add("\tla " + labelAddress + ", " + instruction.param(1).value); // address
		offset = handleLoadLiteralOrRegister(instruction.param(2), offset);
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
			storeIntoRegisterMapRegister(instruction.param(0), "$a0");	
		}
	}
	
	protected String getRegisterMap(String variable){
		Operand isVariable = new Operand(EOPERAND.REGISTER, variable);
		Operand isRegister = new Operand(EOPERAND.VARIABLE, variable);
		if(registerMap.containsKey(isVariable)){
			return registerMap.get(isVariable);
		} else if(registerMap.containsKey(isRegister)){
			return registerMap.get(isRegister);
		} else {
			return null;
		}
	}
	
	private void handleBranch(String branch, Operand operand1,
			Operand operand2, String label) {

		output.add("\t"
				+ branch
				+ " "
				+ handleLoadLiteralOrRegister(operand1, "$a0")
				+ ", "
				+ handleLoadLiteralOrRegister(operand2, "$a1") + ", " + label);
	}

	private void handleOperator(String operator, Operand destination, Operand operand1, Operand operand2) {
		if (registerMap.containsKey(destination)) {
			output.add("\t"
					+ operator
					+ " "
					+ registerMap.get(destination)
					+ ", "
					+ handleLoadLiteralOrRegister(operand1, "$a0")
					+ ", "
					+ handleLoadLiteralOrRegister(operand2, "$a1"));
		} else {
			output.add("\t"
					+ operator
					+ " "
					+ "$a0"
					+ ", "
					+ handleLoadLiteralOrRegister(operand1, "$a0")
					+ ", "
					+ handleLoadLiteralOrRegister(operand2, "$a1"));
			storeIntoRegisterMapRegister(destination, "$a0");
		}
	}

	private void handleLoadIntoRegister(Operand operand, String destination) {
		if (operand.type.equals(EOPERAND.LITERAL)) {
			output.add("\taddi " + destination + ", $zero, " + operand.value);
		} else {
			output.add("\tmove "
					+ destination
					+ ", "
					+ loadFromRegisterMap(operand, destination));
		}
	}

	private String handleLoadLiteralOrRegister(Operand operand,
			String destination) {
		if (operand.type.equals(EOPERAND.LITERAL)) {
			output.add("\taddi " + destination + ", $zero, " + operand.value);
			return destination;
		} else {
			return loadFromRegisterMap(operand, destination);
		}
	}

	private void handleStoreFunctionReturnToLabelAndRegister(Operand label) {
		output.add("\tla $a0, " + label);
		output.add("\tsw " + RETURN_VALUE_REGISTER + ", 0($a0)");
		if (registerMap.containsKey(label)) {
			String desintationRegister = registerMap.get(label);
			output.add("\tmove " + desintationRegister + ", " + RETURN_VALUE_REGISTER);
		}
	}

	private void handleAssign(IRInstruction instruction) {
		storeIntoRegisterMapGeneral(instruction.param(0), instruction.param(1));
	}

	public String loadFromRegisterMap(Operand op, String spill) {
		if (registerMap.containsKey(op)) {
			return registerMap.get(op);
		} else {
			output.add("\tla " + spill + ", " + op.value);
			output.add("\tlw " + spill + ", 0(" + spill + ")");
			return spill;
		}
	}

	private void storeIntoRegisterMapGeneral(Operand destination, Operand value) {
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
						+ loadFromRegisterMap(value, "$a2"));
				break;
			case LABEL:
				System.err.println("NMG HSL: This should never happen.");
				break;
			}
		} else {
			storeSpillPreamble();
			switch (value.type) {
			case LITERAL:
				output.add("\taddi " + STORE_SPILL_VALUE_REGISTER + ", $zero, " + value.value);
				break;
			case VARIABLE:
			case REGISTER:
				output.add("\tmove "
						+ STORE_SPILL_VALUE_REGISTER
						+ ", "
						+ loadFromRegisterMap(value, STORE_SPILL_VALUE_REGISTER));
				break;
			case LABEL:
				System.err.println("NMG HSL: This should never happen.");
				break;
			}
			storeSpillEpilogue(destination);

		}
	}

	private void storeIntoRegisterMapRegister(Operand destination, String register) {
		if (registerMap.containsKey(destination)) {
			String desintationRegister = registerMap.get(destination);
			output.add("\tmove " + desintationRegister + ", " + register);
		} else {
			storeSpillPreamble();
			output.add("\tmove " + STORE_SPILL_VALUE_REGISTER + ", " + register);
			storeSpillEpilogue(destination);
		}
	}

	private void handleSpill(List<String> instructions, List<String> registers){
		for(int i = 0; i < registers.size();i++){
			saveRegister(registers.get(i), output);
		}
		for(String instruction : instructions){
			output.add(instruction);
		}
		for(int i = registers.size() - 1; i >= 0 ;i--){
			restoreRegister(registers.get(i), output);
		}
	}
	
	private void storeSpillEpilogue(Operand destination) {
		output.add("\tla " + STORE_SPILL_ADDRESS_REGISTER + ", " + destination.value);
		output.add("\tsw " + STORE_SPILL_VALUE_REGISTER + ", 0(" + STORE_SPILL_ADDRESS_REGISTER + ")");
//		restoreRegister(STORE_SPILL_ADDRESS_REGISTER);
//		restoreRegister(STORE_SPILL_VALUE_REGISTER);
	}

	private void storeSpillPreamble() {
//		saveRegister(STORE_SPILL_VALUE_REGISTER);
//		saveRegister(STORE_SPILL_ADDRESS_REGISTER);
	}

	public void loadFromLabel(Operand op){
		if(registerMap.containsKey(op)){
			output.add("\tla $a0, " + op.value);
			output.add("\tlw " + registerMap.get(op) + ", 0($a0)");
		}
	}
	
	public void storeToLabel(Operand op){
		if(registerMap.containsKey(op)){
			output.add("\tla $a0, " + op.value);
			output.add("\tsw " + registerMap.get(op) + ", 0($a0)");
		}
	}
	
	private void functionLoadFromLabel(String label) {
		String register = getRegisterMap(label);
		if(register != null){
			output.add("\tla $a0, " + label);
			output.add("\tlw " + register + ", 0($a0)");
		}
	}
	
		
	
	public static void generateBasicBlock(BasicBlock bb, 
			Map<IRInstruction,Map<Operand, String>> instructionRegisterMap, 
			List<String> output, SymbolTable table, Map<IRInstruction, String> functionMap, 
			Collection<Operand> load, Collection<Operand> save){
		//removes dead code
		if(bb.successors.isEmpty() && bb.predecessors.isEmpty() && !(bb.label != null && bb.label.equals("main")))
			return;
		RegisterCodeGenerator rcg = new RegisterCodeGenerator(instructionRegisterMap, output, table, functionMap);
		if(Configuration.MIPS_COMMENTS){
			output.add("#\tBlock " + bb.predecessors + " -> "+ bb.position + " -> " + bb.successors);
			output.add("#\t\tVARIABLES :" + bb.getVariables());
			output.add("#\t\tIN :" + bb.in);
			output.add("#\t\tOUT :" + bb.out);
			output.add("#\t\tUSED :" + bb.getUsed());
			output.add("#\t\tDEFINED :" + bb.getDefined());
		}
		if (bb.label != null) {
			rcg.generate(bb.label);
		}
		if(Configuration.MIPS_COMMENTS){
			output.add("#\t Load Registers ");
		}
		for (Operand op : load) {
			Map<Operand, String> registerMap = rcg.instructionRegisterMap.get(null);
			if(registerMap.containsKey(op)){
				rcg.output.add("\tla $a0, " + op.value);
				rcg.output.add("\tlw " + registerMap.get(op) + ", 0($a0)");
			}
		}
		for (IRInstruction instruction : bb.instructions) {
			rcg.generate(instruction);
		}
		if(Configuration.MIPS_COMMENTS){
			output.add("#\t Store Registers ");
		}
		List<String> params;
		if(bb.jump != null && bb.jump.isFunctionCall()){
			String functionName = IRRenamer.unrename(bb.jump.functionName());
			params = IRRenamer.rename(table.functionParamNames(functionName));
		} else {
			params = new LinkedList<>();
		}
		for (Operand op : save) {
			Map<Operand, String> registerMap = rcg.instructionRegisterMap.get(bb.lastInstruction());
			if(registerMap.containsKey(op) && !params.contains(op.value)){
				//System.out.println("-------------------------------");
				rcg.output.add("\tla $a0, " + op.value);
				rcg.output.add("\tsw " + registerMap.get(op) + ", 0($a0)");
			}
		}
		if (bb.jump != null) {
			rcg.generate(bb.jump);
		}
	}
}
