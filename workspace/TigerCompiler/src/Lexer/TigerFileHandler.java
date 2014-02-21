package Lexer;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TigerFileHandler {
	public static final String TIGER_FILE_TYPE = ".tiger";
	public static final String TIGER_LEXER_OUTPUT_TYPE = ".tokens";
	public static final String TIGER_LEXER_ERROR_TYPE = ".err";

	private String[] args;
	
	public TigerFileHandler(String[] args){
		this.args = args;
	}
	
	public boolean isTigerFile(String name) {
		if(name.length() <= TIGER_FILE_TYPE.length()) {
			return false;
		}
		String fileType = name.substring(name.length() - TIGER_FILE_TYPE.length());
		return fileType.equalsIgnoreCase(TIGER_FILE_TYPE);
	}

	public void redirectConsole() throws IOException{
		String filename = checkFilename();
		String outputName = createOutputFilename(filename);
		String errorName = createErrorFilename(filename);
		PrintStream out = new PrintStream(outputName);
		PrintStream err = new PrintStream(errorName);
		System.setOut(out);
		System.setErr(err);
	}
	
	public String getInput() throws IOException{
		String filename = checkFilename();
		return readFile(filename, Charset.defaultCharset());
	}

	private String checkFilename() throws IOException {
		if(args.length < 1){
			throw new IOException("No arguments");
		}
		String filename = args[0];
		File file = new File(filename);
		if(!file.exists()){
			throw new IOException("Supplied file does not exist");
		}
		if(!file.isFile()){
			throw new IOException("Supplied file is not a file");
		}
		if(!file.canRead()){
			throw new IOException("Unable to read file");
		}
		if(!isTigerFile(file.getName())){
			throw new IOException("File was not a Tiger file");
		}
		return filename;
	}
	
	private String createErrorFilename(String filename){
		File inputFile = new File(filename);
		String inputFilename = inputFile.getName();
		String inputName = inputFilename.substring(0, inputFilename.length() - TIGER_FILE_TYPE.length());
		return inputFile.getParent() + File.separator + inputName + TIGER_LEXER_OUTPUT_TYPE + TIGER_LEXER_ERROR_TYPE;
	}
	
	
	private String createOutputFilename(String filename){
		File inputFile = new File(filename);
		String inputFilename = inputFile.getName();
		String inputName = inputFilename.substring(0, inputFilename.length() - TIGER_FILE_TYPE.length());
		return inputFile.getParent() + File.separator + inputName + TIGER_LEXER_OUTPUT_TYPE;
	}
	
	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
