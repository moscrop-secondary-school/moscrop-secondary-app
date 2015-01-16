package com.ivon.jsontagtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Application {
	
	public static String parseArgs(String[] args) {
		if (args.length > 0) {
			File testFile = new File(args[0]);
			if (!testFile.exists()) {
				return null;
			} else {			
				return args[0];
			}
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// Get and test arguments
		File csvFile = null;
		File jsonFile = null;
		if (args.length >= 2) {
			
			// Validate the in-file
			File testFile = new File(args[0]);
			if (!testFile.exists()) {
				System.out.println("Error: invalid source file location");
				showUsageHelp();
				return;
			} else {			
				csvFile = testFile;
			}
			
			// Validate the out-file
			jsonFile = new File(args[1]);
			
		} else {
			showUsageHelp();
			return;
		}
	
		Scanner reader = new Scanner(csvFile);
		List<TagObject> tags = new ArrayList<TagObject>();
		int lineCount = 0;
		
		while (reader.hasNextLine()) {
			String line = reader.nextLine();
			lineCount++;
			if (lineCount > 2) {
				String[] fields = line.split(",");
				if (fields.length >= 4) {
					try {
						tags.add(new TagObject(fields[0], fields[1], fields[2], fields[3]));
					} catch (IllegalArgumentException e) {
						System.out.println("The following error occured when processing row " + lineCount + " of the spreadsheet. This error occurs when the 'name' field is empty.");
					}
				}
			}
		}
		
		reader.close();
		
		PrintWriter writer = new PrintWriter(jsonFile);
		
		writer.println("{\"tags\":[");
		for (int i=0; i<tags.size(); i++) {
			TagObject tag = tags.get(i);
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
	}
	
	public static void showUsageHelp() {
		System.out.println("Please provide the absolute path to a valid .csv file");
	}
}
