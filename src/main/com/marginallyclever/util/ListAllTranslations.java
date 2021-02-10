package com.marginallyclever.util;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Scan all java files in the project, find instances of Translator.get("AAAAA") and return the AAAAA part
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class ListAllTranslations {
	private final static String TRANSLATOR_HEAD = "Translator.get(\"";
	private final static String TRANSLATOR_TAIL = "\")";

	private String rootPath;
	private ArrayList<String> keys = new ArrayList<String>();
		
	
	ListAllTranslations(String root) {
		super();
		rootPath = root;
	}
	
	public void listFilesForFolder(File folder) {
		String path = folder.getAbsolutePath();
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	String filename = fileEntry.getName();
	        	String fullPath = path + File.separator + filename;
	            if(filename.endsWith(".java")) {
	            	boolean found=false;
	                Scanner kb = new Scanner(System.in);
	                List<String> lines;
					try {
						lines = Files.readAllLines(Paths.get(fullPath));
		                for (String line : lines) {
		                    while(line.contains(TRANSLATOR_HEAD)) {
		                    	if(!found) {
		            	            System.out.println("<!-- "+fullPath.substring(rootPath.length())+" -->");
		            	            found=true;
		                    	}
		                    	int first = line.indexOf(TRANSLATOR_HEAD);
		                    	int last = line.indexOf(TRANSLATOR_TAIL, first);
		                    	String result = line.substring(first+TRANSLATOR_HEAD.length(),last);
		                    	if(!keys.contains(result)) {
		                    		System.out.println("\t"+result);
		                    		keys.add(result);
		                    	}
		                        line = line.substring(last+TRANSLATOR_TAIL.length());
		                    }
		                }
					} catch (IOException e) {
						e.printStackTrace();
					}
	            }
	        }
	    }
	}

	public static void main(String[] args) {
		String cwd = System.getProperty("user.dir") + File.separator + "src"+ File.separator +"main";
		
		ListAllTranslations lat = new ListAllTranslations(cwd);
		File f = new File(cwd);
		lat.listFilesForFolder(f);
	}
	
}
