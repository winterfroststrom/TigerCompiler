package General;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


public class TigerFileHandler implements AutoCloseable{
	private static boolean redirectedConsole;
	private PrintWriter[] out;
	private String[] args;
	
	public static enum EOUTPUT{
		ERROR, TOKENS, TREE, TABLE, IR, MIPS_NAIVE, MIPS_BB, MIPS_EBB
	}
	
	public TigerFileHandler(String[] args){
		this.args = args;
	}

	public void redirectConsole() throws IOException{
		if(Configuration.REDIRECTING_CONSOLE){
			if(!redirectedConsole){
				String filename = checkFilename();
				out = new PrintWriter[EOUTPUT.values().length];
				for(EOUTPUT output : EOUTPUT.values()){
					out[output.ordinal()] = new PrintWriter(createOutputFilename(output, filename));
				}
				redirectedConsole = true;
			}
		}
	}
	
	public void println(EOUTPUT output, String string){
		if(Configuration.REDIRECTING_CONSOLE){
			out[output.ordinal()].println(string);
			out[output.ordinal()].flush();
		} else {
			if(output.equals(EOUTPUT.ERROR)){
				System.err.println(string);
			} else {
				System.out.println(string);
			}
		}
	}
		
	public void print(EOUTPUT output, String string){
		if(Configuration.REDIRECTING_CONSOLE){
			out[output.ordinal()].print(string);
			out[output.ordinal()].flush();
		} else {
			if(output.equals(EOUTPUT.ERROR)){
				System.err.print(string);
			} else {
				System.out.print(string);
			}
		}
	}
	
	public void println(EOUTPUT output){
		println(output, "");
	}
		
	public void print(EOUTPUT output){
		print(output, "");
	}
	
	@Override
	public void close(){
		if(Configuration.REDIRECTING_CONSOLE){
			for(int i = 0; i < out.length;i++){
				out[i].close();
			}
		}
	}
	
	public String getInput() throws IOException{
		String filename = checkFilename();
		return readFile(filename, Charset.defaultCharset());
	}
	
	private boolean isTigerFile(String name) {
		if(name.length() <= Configuration.TIGER_FILE_TYPE.length()) {
			return false;
		}
		String fileType = name.substring(name.length() - Configuration.TIGER_FILE_TYPE.length());
		return fileType.equalsIgnoreCase(Configuration.TIGER_FILE_TYPE);
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
		
	private String createOutputFilename(EOUTPUT output, String filename){
		File inputFile = new File(filename);
		String inputFilename = inputFile.getName();
		String inputName = inputFilename.substring(0, inputFilename.length() - Configuration.TIGER_FILE_TYPE.length());
		return inputFile.getParent() + File.separator + inputName + "." + output;
	}
	
	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
}
