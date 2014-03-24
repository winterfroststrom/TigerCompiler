package General;


public class Configuration {
	public static final boolean PRINT_TOKENS = true;
	public static final boolean PRINT_TREE = true;
	public static final boolean PRINT_TABLE = true;
	public static final String PARSE_TREE_NODE_DELIMITER = "\n";
	public static final String PARSE_TREE_NODE_PREFIX = " ";
	public static final boolean LL1PARSER_DEBUGGING = false;
	public static final String SCOPE_DELIMITER = ".";
	public static final String GLOBAL_SCOPE_NAME = "";
	public static final Type DEFAULT_TYPE_ON_ERROR = Type.INT;
	public static final String TIGER_FILE_TYPE = ".tiger";
	public static final String TIGER_LEXER_OUTPUT_TYPE = ".tokens";
	public static final String TIGER_LEXER_ERROR_TYPE = ".err";
//	public static final String FORCED_LOAD_FILE = "resources/ex10.tiger";
	public static final String FORCED_LOAD_FILE = null;
//	public static final boolean REDIRECTING_CONSOLE = false;
	public static final boolean REDIRECTING_CONSOLE = true;
}
