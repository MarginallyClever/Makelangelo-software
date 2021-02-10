package com.marginallyclever.util;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Scan all java files in the project, find instances of Translator.get("AAAAA") and return the AAAAA part,
 * XML formatted, to the System.out.  You can copy/paste the results to an XML file and start filling in the values.
 * Don't forget to change the meta-language and meta-author values!
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
					try {
						List<String> lines = Files.readAllLines(Paths.get(fullPath));
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
		                    		System.out.println("\t<string><key>"+result+"</key><value></value></string>");
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
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		
		System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		System.out.println("<!DOCTYPE language>");
		System.out.println("<language>");
		System.out.println("\t<meta>");
		System.out.println("\t\t<name>English</name>");
		System.out.println("\t\t<author>Dan Royer (dan@marginallyclever.com)</author>");
		System.out.println("\t\t<when>"+dtf.format(now)+"</when>");
		System.out.println("\t</meta>");
		ListAllTranslations lat = new ListAllTranslations(cwd);
		File f = new File(cwd);
		lat.listFilesForFolder(f);

		System.out.println("</language>");
	}
	
}
