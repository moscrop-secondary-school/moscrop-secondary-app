package com.ivon.jsontagtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Auto
{
    private static File csvFile = new File("tag_data_spreadsheet.csv");
    private static File jsonFile = new File("test.json");

    public static void main(String[] args)
            throws FileNotFoundException
    {
        // Read config files
        //File configFile = new File("json-tag-tool_config.txt");

    /*Scanner configReader = new Scanner(configFile);
    List<TagObject> tags = new ArrayList();
    int lineCount = 0;
    while (reader.hasNextLine()) {
        String line = reader.nextLine();
        lineCount++;
        String[] fields = line.split(",");
        this.id_input = csvFile;
        this.id_output = jsonFile;
    }
    reader.close();*/
        //test
    /*
    BufferedReader textReader = new BufferedReader(FileReader(configFile));

    int numberofLines = readLines();
    String[] textData - new String[numberOfLines];

    int i;

    for (i=0; i < numberOfLines; i++) {
        textData[i] - textReader.readLine();
    }

    textReader.close();
    return textData;*/

    /*Scanner reader = new Scanner(configFile);
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
    reader.close(); */

        Scanner reader = new Scanner(csvFile);
        List<TagObject> tags = new ArrayList();
        int lineCount = 0;
        while (reader.hasNextLine())
        {
            String line = reader.nextLine();
            lineCount++;
            if (lineCount > 2)
            {
                String[] fields = line.split(",");
                if (fields.length >= 4) {
                    try
                    {
                        tags.add(new TagObject(fields[0], fields[1], fields[2], fields[3]));
                    }
                    catch (TagObject.InvalidNameException e) {}catch (TagObject.InvalidCriteriaException e) {}
                }
            }
        }
        reader.close();

        PrintWriter writer = new PrintWriter(jsonFile);

        writer.println("{");
        writer.println("\t\"updated\":\"" + System.currentTimeMillis() + "\",");
        writer.println("\t\"tags\":[");
        for (int i = 0; i < tags.size(); i++)
        {
            TagObject tag = (TagObject)tags.get(i);
            writer.println("        \t{");
            writer.println("                \t\"name\":\"" + tag.name + "\",");
            writer.println("                \t\"id_author\":\"" + tag.id_author + "\",");
            writer.println("                \t\"id_category\":\"" + tag.id_category + "\",");
            writer.println("                \t\"icon_img\":\"" + tag.icon_img + "\"");
            if (i < tags.size() - 1) {
                writer.println("        \t},");
            } else {
                writer.println("        \t}");
            }
        }
        writer.println("\t]");
        writer.println("}");
        writer.close();
    }
}
