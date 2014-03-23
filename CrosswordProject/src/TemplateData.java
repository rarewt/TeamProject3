import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class TemplateData {
	
	// stores all crossword grid templates during runtime
	// format: {<size>: { {<template name>: <string representation>} . . .}
	private HashMap<Integer, HashMap<String, String>> templates;
	
	public TemplateData() {
		templates = new HashMap<Integer, HashMap<String, String>>();
	}
	
	public void scanTemplates() {
		File projectDir = new File(System.getProperty("user.dir"));
		File dataDir = new File(projectDir, "data");
		File templatesDir = new File(dataDir, "templates"); // default folder
		for (File file : templatesDir.listFiles()) { // each file stores a single template
			if (!file.isDirectory() && file.getName().toLowerCase().endsWith(".txt")) {
				try {
					FileReader reader = new FileReader(file);
					Scanner lineScanner = new Scanner(reader);  
			        if (lineScanner.hasNext() == false) continue; // skip empty files
			        // create a string representation of the template from the contents of the file
			        String template = "";
			        int templateSize = 0;
					while (true) {
						templateSize++;
						template += lineScanner.nextLine();
						if (lineScanner.hasNext()) template += ",";
						else break;
					}
					lineScanner.close();
					reader.close();
					// proceed only if the size of the template is in the supported range (between 5 and 13)
					if ((template.split(",").length)%2 == 0 ||
						(template.split(",").length) < 5 ||
						(template.split(",").length) > 13)
						continue;
					// avoid creating hash maps for unused sizes
			        if(!templates.containsKey(templateSize))        	
			        	templates.put(templateSize, new HashMap<String, String>());
			        // place the template in the master hash map
			        templates.get(templateSize).put(file.getName().split("\\.")[0], template);
				} catch (IOException e) {}; // keeps the compiler happy
			}
		}
	}
	
	public HashMap<Integer, HashMap<String, String>> getTemplates() {
		return templates;
	}
	
}

