package General;

import java.util.ArrayList;

public class Configuration {
	public static final boolean RELEASE = true;
	public static final boolean REDIRECTING_CONSOLE = RELEASE || false;
	
	public static final boolean PRINT_TOKENS = RELEASE || false;
	public static final boolean PRINT_TREE = RELEASE || false;
	public static final boolean PRINT_TABLE = RELEASE || false;
	public static final boolean PRINT_IR = RELEASE || false;
	public static final boolean PRINT_MIPS_NAIVE = RELEASE || false;
	public static final boolean PRINT_MIPS_BB = RELEASE || false;
	public static final boolean PRINT_MIPS_EBB = RELEASE || true;

//	public static final String FORCED_LOAD_FILE = RELEASE ? null : "resources/e7.tiger";
	public static final String FORCED_LOAD_FILE = "resources/d1.tiger";
	
	public static final boolean MIPS_COMMENTS = !RELEASE && false; 
	public static final boolean MIPS_VALID_LABELS = RELEASE || false;
	public static final boolean LOAD_EXTENDED_LIBRARY = RELEASE || true;
	public static final boolean GRAPH_COLORING = RELEASE || true;
	public static final ArrayList<String> TEMP_REGISTERS = (RELEASE || true) ? 
			Utilities.toAL("$8", "$9", "$10", "$11", "$12", "$13", 
							"$14", "$15", "$16", "$17", "$18", "$19", 
							"$20", "$21", "$22", "$23", "$24", "$25") : 
			Utilities.toAL("$8", "$9", "$10", "$11", "$12");
	public static final boolean SHORT_BB_PRINT = true;
	
	public static final boolean LL1PARSER_DEBUGGING = !RELEASE && false;
	public static final boolean LL1PARSER_DEBUGGING_VERBOSE = !RELEASE && false;
	
	public static final String TIGER_FILE_TYPE = ".tiger";
	
	public static final String PARSE_TREE_NODE_DELIMITER = "\n";
	public static final String PARSE_TREE_NODE_PREFIX = " ";
	
	public static final String SCOPE_DELIMITER = "@";
	public static final String GLOBAL_SCOPE_NAME = "";
	public static final Type DEFAULT_TYPE_ON_ERROR = Type.INT;
	
	public static final String MISSING_NAME = "ERROR|MISSING_NAME";
	public static final String PROGRAM_ENTRY_LABEL = "main";
	public static final String LOOP_BEGIN = "LoopBegin_";
	public static final String LOOP_REPEAT = "Break_";
	public static final String LOOP_END = "LoopEnd_";
	public static final String IF_FALSE = "Else_";
	public static final String IF_END = "FI_";
	public static final String COMPARE_FALSE = "CompFalse_";
	public static final String COMPARE_END = "CompEnd_";
	public static final String TEMP_PREFIX = "t";
	public static final String SHORT_CIRCUIT = "ShortCircuit_";
	
	public static final String RENAME_WORD = "Z";
	public static final String RENAME_WORD_DELIMITER = "z";
	public static final String RENAME_WORD_MISC = "0";
	public static final String BASIC_BLOCK_LABEL = "BasicBlock";
}
