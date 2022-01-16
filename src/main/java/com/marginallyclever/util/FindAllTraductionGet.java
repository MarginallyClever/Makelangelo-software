/*
 * Copyright (C) 2022 Marginally Clever Robots, Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.marginallyclever.util;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.LanguagePreferences;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.slf4j.LoggerFactory;

/**
 * A Try to find in the source code the Tratuction keys used ...
 *
 *
 * @author PPAC37
 */
public class FindAllTraductionGet {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FindAllTraductionGet.class);

    private static Map<FindAllTraductionResult, Path> mapMatchResultToFilePath = new HashMap<>();

    public static Map<FindAllTraductionResult, Path> getMapMatchResultToFilePath() {
	return mapMatchResultToFilePath;
    }

    /**
     * Try to search a src java project for a specifik pattern ( like
     * Traduction.get(...) )
     *
     * @param args
     */
    public static void main(String[] args) {

	Log.start();
	// lazy init to be able to purge old files
	//logger = LoggerFactory.getLogger(Makelangelo.class);

	PreferencesHelper.start();
	CommandLineOptions.setFromMain(args);
	Translator.start();

	if (Translator.isThisTheFirstTimeLoadingLanguageFiles()) {
	    LanguagePreferences.chooseLanguage();
	}
	try {
	    // TODO arg 0 as dirToSearch 
	    if (args != null && args.length > 0) {
		//

	    }
	    String baseDirToSearch = "src" + File.separator + "main" + File.separator + "java";
	    System.out.printf("PDW=%s\n", new File(".").getAbsolutePath());
	    File srcDir = new File(".", baseDirToSearch);
	    try {
		System.out.printf("srcDir=%s\n", srcDir.getCanonicalPath());
	    } catch (IOException ex) {
		Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    // list all .java files in srcDir.

	    List<Path> paths = listFiles(srcDir.toPath(), ".java");
	    // search in the file ...
	    paths.forEach(x -> searchAFile(x, srcDir.toPath()));
	} catch (IOException ex) {
	    Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	}

	System.out.printf("totalMatchCountInAllFiles      =%d\n", totalMatchCountInAllFiles);
	System.out.printf("totalMatchCountInAllFilesV0    =%d\n", totalMatchCountInAllFilesV0);
	System.out.printf("mapMatchResultToFilePath.size()=%d\n", mapMatchResultToFilePath.size()
	);

	JFrame jf = new JFrame();
	jf.setLayout(new BorderLayout());
	JTable jtab = new JTable(new FindAllTraductionGetTableModel());
	jtab.setAutoCreateRowSorter(true);
	JScrollPane jsp = new JScrollPane();
	jsp.setViewportView(jtab);
	jf.getContentPane().add(jsp, BorderLayout.CENTER);
	jf.setMinimumSize(new Dimension(800, 600));
	jf.pack();
	jf.setVisible(true);
	jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    private static int totalMatchCountInAllFiles = 0;
    private static int totalMatchCountInAllFilesV0 = 0;
    private static boolean debugPaser = false;

    /**
     * line by line scanner then token matcher ...TODO if i have a multiline ?
     *
     * @param x
     * @param baseDir only to relativize output path
     */
    public static int searchAFile(Path x, Path baseDir) {
	int totalMatchCount = 0;
	try {
	    if (debugPaser) {
		System.out.println(x);
	    }
	    // TODO parse the file
	    int posVarCombinationsFromController = 0;

	    // See pattern regexp (fr) https://cyberzoide.developpez.com/tutoriels/java/regex/
	    // normaly '(' and ')' are used to define group in patern matcher so to match a real '(' you have to déspécialise it by adding a trailling '\' ( and as we are in a string you have to add a '\' befor the '\' ... )
	    // '.' in regexp is for any caractére if you whant to match a '.' you have to despecialise it by adding a '\' ...
	    // [^\\).]
	    // \s Un caractère blanc : [ \t\n\x0B\f\r]
	    String patternString1 = "Translator\\s*\\.\\s*get\\s*\\(([^\\)]*)\\)";
	    Pattern patternS1 = Pattern.compile(patternString1);

	    // not line by line ... 
//	    try ( Scanner sc = new Scanner(x)) {
//		Pattern pat = Pattern.compile(patternString1);
//		List<String> n = sc.findAll(pat)
//			.map(MatchResult::group)
//			.collect(Collectors.toList());
//		if ( n.size()>0){
//		    System.out.printf("::%d\n", n.size());
//		    for ( String s : n){
//			System.out.printf(" %s\n", s);
//		    }
//		}
//		
//	    }
	    try ( Scanner sc = new Scanner(x)) {
		Pattern pat = Pattern.compile(patternString1);
		List<MatchResult> n = sc.findAll(pat)
			//.map(MatchResult)
			.collect(Collectors.toList());
		if (n.size() > 0) {
//		    System.out.printf("::%d in %s\n", n.size(), x.toAbsolutePath());
		    for (MatchResult mr : n) {
			totalMatchCountInAllFilesV0++;
			// Can we get the line num ? currently in this implementation we have the car pos in the file/strem ...
//			System.out.printf(" %-50s in %s at sart:%d end:%d\n", mr.group(1), mr.group(), mr.start(), mr.end());
		    }
		}

	    }

	    // line by line ( this can miss some like "Traduction.\nget(\n...\n);" )
	    Scanner scanner = new Scanner(x);
	    int lineNum = 0;
	    while (scanner.hasNextLine()) {
		lineNum++;
		String nextToken = scanner.nextLine();

		Matcher m = patternS1.matcher(nextToken);

		int matchCount = 0;
		while (m.find()) {
		    totalMatchCountInAllFiles++;
		    totalMatchCount++;
		    matchCount++;
		    if (debugPaser) {
			System.out.println("#found: " + m.group(0));
			System.out.flush();
			System.out.println("#found gp count : " + m.groupCount());
			System.out.flush();
		    }
		    if (false) {
			System.out.printf("%s\n\tat %s(l:%d c:%d)\n", m.group(1), baseDir.relativize(x), lineNum, m.start(1));
		    }
		    FindAllTraductionResult res = new FindAllTraductionResult(m.group(1), lineNum, m.start(1), x);
		    mapMatchResultToFilePath.put(res, x);
		    //
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	}
	return totalMatchCount;
    }

    /**
     * List all file in this path. Using <code>Files.walk(path)</code> (so this
     * take care of recursive path exploration ) And applying filter (
     * RegularFile and ReadableFile ) and filterring FileName ... TODO : a
     * better/more efficient way ? ( im not familiare with the usage of walk
     * (filter organisation) and the cumultation/collect ...
     *
     * @param path
     * @param fileNameEndsWithSuffix use ".java" to get only ... ( this is not a
     * regexp so no '.' despecialisation needed )
     * @return
     * @throws IOException
     */
    public static List<Path> listFiles(Path path, String fileNameEndsWithSuffix) throws IOException {

	List<Path> result = null;
	try ( Stream<Path> walk = Files.walk(path)) {
	    result = walk
		    .filter(Files::isRegularFile)
		    .filter(Files::isReadable)
		    .map(Path::toFile)
		    .filter(f -> f.getName().endsWith(fileNameEndsWithSuffix))
		    .map(File::toPath)
		    .collect(Collectors.toList());
	}
	if (debugPaser || true) {
	    if (result == null) {
		System.out.printf("listFiles (%s, \"%s\").size()=null\n", path, fileNameEndsWithSuffix);
	    } else {
		System.out.printf("listFiles (%s, \"%s\").size()=%d\n", path, fileNameEndsWithSuffix, result.size());
	    }
	}
	return result;

    }

}
