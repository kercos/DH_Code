package corpora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CleanCsvFiles {

	public static void cleanCsv(File inputFile, File outputFile) throws FileNotFoundException {
		System.out.println("Cleaning CSV");
		System.out.println("Input File: " + inputFile.getAbsolutePath());
		System.out.println("Output File: " + outputFile.getAbsolutePath());
		Scanner scan = new Scanner(inputFile);
		PrintWriter pw = new PrintWriter(outputFile);
		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			pw.println(correct(line));
		}
		scan.close();
		pw.close();
	}
	
	public static String correct(String s) {
		return s.replace("\"\\\"\"", "\"\"\"\"");
		// "\"" -> """"
	}
	
	
	public static void test() {
		String a = "fdsasfa\\\"fdsafdas";
		System.out.println(a);
		a = a.replace("\\\"", "\"\"");
		System.out.println(a);

	}
	
	public static void main(String[] args) throws FileNotFoundException {
		File inputPath = new File("/Volumes/HardDisk/Scratch/Projects/MADRE/Corpora_Original");
		String outputPath = "/Volumes/HardDisk/Scratch/Projects/MADRE/Corpora_Fixed/";
		for(File inputFile : inputPath.listFiles()) {
			String fileName = inputFile.getName();
			if (!fileName.endsWith(".csv"))
				continue;
			File outputFile = new File(outputPath + fileName);
			cleanCsv(inputFile, outputFile);
		}
	}

}
