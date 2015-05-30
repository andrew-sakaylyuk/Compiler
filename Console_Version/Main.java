import java.io.*; import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		String inFile = "", outFile = ""; 
		Scanner scan = new Scanner(System.in);
		if ( args.length < 1 ) { 
			System.out.println("Write the name of input file:");
			if ( scan.hasNextLine() ) inFile = scan.nextLine();
			//inFile = "C:\\test.txt";
		} else inFile = args[0];
		scan.close();
		if (args.length > 1 ) outFile = args[1];
		else {
			outFile = inFile;
			if (outFile.lastIndexOf('.') > 0)
				outFile = outFile.substring( 0, outFile.lastIndexOf('.') ) + ".ASM";
			else outFile += ".ASM";
		}
		Parser parse = new Parser(inFile, outFile); 
		parse.program();
		System.out.println("Translation complete!");
		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
					"start notepad++ " + inFile);
			builder.start();
			builder = new ProcessBuilder("cmd.exe", "/c", 
					"start notepad++ " + outFile);
			builder.start();
		} catch (IOException e) { }
		parse.lex.in.close();
		parse.out.close();
	}
}
