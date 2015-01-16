package com.ivon.jsontagtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Application {
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	
	private static boolean showLogs = true;
	private static File csvFile = null;
	private static File jsonFile = null;
	
	private static boolean parseArgs(String[] args) throws FileNotFoundException {
		
		boolean containsSpecifiedFiles = false;
		
		boolean requestShowUsage = false;
		boolean requestStopProgram = false;
		
		for (int i=0; i<args.length; i++) {
	
			if (args[i].equals("-x") || args[i].equals("--execute")) {

				containsSpecifiedFiles = true;
				
				// Operate on specified input/output files
				if (i < args.length-2) {
					File testFile = new File(args[i+1]);
					if (!testFile.exists()) {
						error("invalid source file location");
						requestShowUsage = true;
						requestStopProgram = true;						
					} else {			
						csvFile = testFile;
						jsonFile = new File(args[i+2]);
					}
					i = i+2;
				} else {
					error("invalid arguments for '-x'");
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
				warn("invalid argument " + args[i] + " ignored");
				requestShowUsage = true;
				
			}
		}
		
		if (!containsSpecifiedFiles) {
			
			// Read config files
			File configFile = new File("json-tag-tool_config.txt");
			if (!configFile.exists()) {
				error("Please either specify files to execute on using '-x' or create a config file");
				requestShowUsage = true;
				requestStopProgram = true;
			} else {
			
				boolean hasInputLocation = false;
				boolean hasOutputLocation = false;
				
				Scanner reader = new Scanner(configFile);
				while (reader.hasNextLine()) {
					String line = reader.nextLine();
					String[] cmds = line.split(":");
					if (cmds.length > 0) {
						switch (cmds[0]) {
							case "input": {
								File testFile = new File(cmds[1]);
								if (!testFile.exists()) {
									error("invalid source file location specified in json-tag-tool_config.txt (" + cmds[1] + ")");
									requestShowUsage = true;
									requestStopProgram = true;;					
								} else {
									csvFile = testFile;
									hasInputLocation = true;
								}
								break;
							}
							case "output": {
								jsonFile = new File(cmds[1]);
								hasOutputLocation = true;
								break;
							}
						}
					}
				}
				reader.close();
				
				if (!hasInputLocation || !hasOutputLocation) {
					error("json-tag-tool_config.txt is incomplete!");
					requestShowUsage = true;
					requestStopProgram = true;
				}
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
						warn("'name' field of row " + lineCount + " is empty");
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
		System.out.printf("usage: json-tag-tool [options] -x <csv file path> <json file path>%n");
		System.out.printf("%n");
		System.out.printf("options:%n");
		System.out.printf("    %-24s%-1s%n", "-s, --silent", "Silence all progress messages");
		System.out.printf("%n");
		System.out.printf("Alternatively, you may create a config file to save you from having to enter\nthe paths every time. You can do this by creating 'json-tag-tool_config.txt'\nin the same directory as the jar and add the following lines:%n%n");
		System.out.printf("    %-24s%-1s%n", "input:<path>", "Specify the path of the source .csv file");
		System.out.printf("    %-24s%-1s%n", "output:<path>", "Specify the path of the destination .json file");
		System.out.printf("%n");
		System.out.printf("Use 'json-tag-tool -h' or 'json-tag-tool --help' to show this message again.%n");
		System.out.printf("%n");
	}
	
	private static void log(String msg) {
		if (showLogs) {
			System.out.println(msg);
		}
	}
	
	private static void warn(String msg) {
		System.out.println(ANSI_YELLOW + "Warning: " + msg + ANSI_RESET);
	}
	
	private static void error(String msg) {
		System.out.println(ANSI_RED + "Error: " + msg + ANSI_RESET);
	}
}
