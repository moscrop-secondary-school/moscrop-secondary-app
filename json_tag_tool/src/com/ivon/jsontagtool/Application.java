package com.ivon.jsontagtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Application {
	
	private static boolean showLogs = true;
	private static File csvFile = null;
	private static File jsonFile = null;
	
	private static boolean parseArgs(String[] args) {
		
		boolean requestShowUsage = false;
		boolean requestStopProgram = false;
		
		for (int i=0; i<args.length; i++) {
	
			if (args[i].equals("-i") || args[i].equals("--input")) {

				// In-file
				if (i < args.length-1) {
					File testFile = new File(args[i+1]);
					if (!testFile.exists()) {
						System.out.println("Error: invalid source file location");
						requestShowUsage = true;
						requestStopProgram = true;						
					} else {			
						csvFile = testFile;
					}
					i++;	// Skip over the "value" of the -i flag
				} else {
					System.out.println("Error: invalid source file location");
					requestShowUsage = true;
					requestStopProgram = true;	
				}
				
			} else if (args[i].equals("-o") || args[i].equals("--output")) {
				
				// Out-file
				if (i < args.length-1) {
					jsonFile = new File(args[i+1]);
					i++;	// Skip over the "value" of the -o flag
				} else {
					System.out.println("Error: invalid destination file location");
					requestShowUsage = true;
					requestStopProgram = true;	
				}
				
			} else if (args[i].equals("-s") || args[i].equals("--silent")) {
				
				// Silence logs
				showLogs = false;
				
			} else if (args[i].equals("-h") || args[i].equals("--help")) {
				
				// Show help
				requestShowUsage = true;
				
			} else {
				
				// Other arguments
				System.out.println("Invalid argument " + args[i] + " ignored");
				requestShowUsage = true;
				
			}
		}
		
		if (requestShowUsage) {
			showUsageHelp();
		}
		
		return requestStopProgram;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// Get and test arguments
		if (parseArgs(args) || csvFile == null || jsonFile == null) {
			return;
		}
	
		Scanner reader = new Scanner(csvFile);
		List<TagObject> tags = new ArrayList<TagObject>();
		int lineCount = 0;
		
		log("\n---Begin read---");
		
		while (reader.hasNextLine()) {
			String line = reader.nextLine();
			lineCount++;
			if (lineCount > 2) {
				String[] fields = line.split(",");
				if (fields.length >= 4) {
					try {
						log("Reading " + fields[0] + " (row " + lineCount + ")");
						tags.add(new TagObject(fields[0], fields[1], fields[2], fields[3]));
					} catch (IllegalArgumentException e) {
						System.out.println("Warning: 'name' field of row " + lineCount + " is empty");
					}
				}
			}
		}
		
		reader.close();
		
		PrintWriter writer = new PrintWriter(jsonFile);
		log("\n---Begin write---");
		
		writer.println("{\"tags\":[");
		for (int i=0; i<tags.size(); i++) {
			TagObject tag = tags.get(i);
			log("Writing " + tag.name);
			writer.println("        {");
			writer.println("                \"name\":\"" + tag.name + "\"");
			writer.println("                \"id_author\":\"" + tag.id_author + "\"");
			writer.println("                \"id_category\":\"" + tag.id_category + "\"");
			writer.println("                \"icon_img\":\"" + tag.icon_img + "\"");
			if (i < tags.size()-1) {
				writer.println("        },");
			} else {
				writer.println("        }");
			}
		}
		writer.println("]}");
		writer.close();
		
		log("\nFinish!\n");
	}
	
	private static void showUsageHelp() {
		System.out.printf("%n");
		System.out.printf("usage: json-tag-tool [options] <commands>%n");
		System.out.printf("%n");
		System.out.printf("mandatory commamds (must include all):%n");
		System.out.printf("    %-24s%-1s%n", "-i, --input", "The location of the source .csv file");
		System.out.printf("    %-24s%-1s%n", "-o, --output", "The location of the destination .json file");
		System.out.printf("%n");
		System.out.printf("options:%n");
		System.out.printf("    %-24s%-1s%n", "-s, --silent", "Silence all progress messages");
		System.out.printf("%n");
		System.out.printf("%'json-tag-tool -h' or 'json-tag-tool --help' to show this message again.%n");
		System.out.printf("%n");
	}
	
	private static void log(String msg) {
		if (showLogs) {
			System.out.println(msg);
		}
	}
}
