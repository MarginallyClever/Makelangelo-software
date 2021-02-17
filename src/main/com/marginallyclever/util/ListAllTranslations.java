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
 * Scan all java files in the project, find instances of "Translator.get"+"(\"AAAAA\")" and return the AAAAA part,
 * XML formatted, to the System.out.  You can copy/paste the results to an XML file and start filling in the values.
 * Don't forget to change the meta-language and meta-author values!
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class ListAllTranslations {
	private final static String TRANSLATOR_HEAD = "Translator.get(\"";
	private final static String TRANSLATOR_TAIL = "\")";

	private class TranslationKey {
		public String myKey;
		public String mySrcFile;
		//public String value;
		
		public TranslationKey(String key,String srcFile) {
			myKey=key;
			mySrcFile=srcFile;
		}
	};
	private ArrayList<TranslationKey> keys = new ArrayList<TranslationKey>();
	
	private String rootPath;
		
	
	ListAllTranslations(String root) {
		super();
		rootPath = root;
	}
	
	private void emitXML() {
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

		for(TranslationKey tk : keys) {
			System.out.println("\t<string>");
			System.out.println("\t\t<key>"+tk.myKey+"</key>");
			System.out.println("\t\t<value></value>");
			System.out.println("\t\t<src>"+tk.mySrcFile+"</src>");
			System.out.println("\t</string>");
		}

		System.out.println("</language>");
		
	}
	
	private void emitCSV() {
		System.out.println("File\tKey\tValue");
		for(TranslationKey tk : keys) {
			System.out.println(tk.mySrcFile+"\t"+tk.myKey+"\t ");
		}
	}
	
	private boolean keyIsUnique(String k) {
		for(TranslationKey tk : keys) {
			if(tk.myKey.contentEquals(k)) return false;
		}
		return true;
	}
	
	private void listFilesForFolder(File folder) {
		String path = folder.getAbsolutePath();
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	String filename = fileEntry.getName();
	        	String fullPath = path + File.separator + filename;
	        	String srcFile="";
	            if(filename.endsWith(".java")) {
	            	boolean found=false;
					try {
						List<String> lines = Files.readAllLines(Paths.get(fullPath));
		                for (String line : lines) {
		                    while(line.contains(TRANSLATOR_HEAD)) {
		                    	if(!found) {
		                    		srcFile = fullPath.substring(rootPath.length());
		            	            found=true;
		                    	}
		                    	int first = line.indexOf(TRANSLATOR_HEAD);
		                    	int last = line.indexOf(TRANSLATOR_TAIL, first);
		                    	String result = line.substring(first+TRANSLATOR_HEAD.length(),last);
		                    	if(keyIsUnique(result)) {
		                    		TranslationKey tk = new TranslationKey(result,srcFile);
		            	            keys.add(tk);
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
		
		int choice=0;  // default csv
		for( String a : args ) {
			if(a.contentEquals("csv")) choice=0;
			if(a.contentEquals("xml")) choice=1;
		}
		switch(choice) {
		case 0:  lat.emitCSV();  break;
		case 1:  lat.emitXML();  break;
		}
	}
	
}
