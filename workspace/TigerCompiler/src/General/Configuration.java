package General;


public class Configuration {
	public static final boolean RELEASE = false;
	public static final boolean REDIRECTING_CONSOLE = RELEASE || false;
	
	public static final boolean PRINT_TOKENS = RELEASE || false;
	public static final boolean PRINT_TREE = RELEASE || false;
	public static final boolean PRINT_TABLE = RELEASE || false;
	public static final boolean PRINT_IR = RELEASE || true;
	
	public static final boolean LL1PARSER_DEBUGGING = !RELEASE && false;
	public static final boolean LL1PARSER_DEBUGGING_VERBOSE = !RELEASE && false;

	public static final String FORCED_LOAD_FILE = RELEASE ? null : "resources/tictactoe.tiger";
	
	public static final String TIGER_FILE_TYPE = ".tiger";
	public static final String TIGER_LEXER_OUTPUT_TYPE = ".tokens";
	public static final String TIGER_LEXER_ERROR_TYPE = ".err";
	
	public static final String PARSE_TREE_NODE_DELIMITER = "\n";
	public static final String PARSE_TREE_NODE_PREFIX = " ";
	
	public static final String SCOPE_DELIMITER = ".";
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
	
	
}
