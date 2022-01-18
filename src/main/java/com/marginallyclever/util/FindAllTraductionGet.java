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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.LoggerFactory;

/**
 * An attempt to find the Tratuction.get(...) arguments in the source code. To
 * deduce when possible (args = simple string value) the keys used and therefore
 * missing in the translation file in use.
 *
 * @author PPAC37
 */
public class FindAllTraductionGet {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FindAllTraductionGet.class);

    //
    private static boolean debugPaser = true;
    private static boolean debugListFiles = true;

    /**
     * Try to search a src java project for a specific pattern.
     * like <code>Traduction.get(...)</code> in all .java file.
     *
     * @param args
     */
    public static void main(String[] args) {

	Log.start();
	
	PreferencesHelper.start();
	CommandLineOptions.setFromMain(args);
	Translator.start();

	// TODO test assert the current languge is from english.xml // ??? 
	// TODO get ride of any GUI for the continuous integration test :
	// ??? not need for the test but only in the case the languge set in prefrence do not existe anymore ...
	if (Translator.isThisTheFirstTimeLoadingLanguageFiles()) {
	    LanguagePreferences.chooseLanguage();
	}
	
	// TODO in the env test is this the way ?
	String baseDirToSearch = "src" + File.separator + "main" + File.separator + "java";
	logger.debug(String.format("PDW=%s", new File(".").getAbsolutePath()));
	
	File srcDir = new File(".", baseDirToSearch);
	try {
	    logger.debug(String.format("srcDir=%s", srcDir.getCanonicalPath()));
	} catch (IOException ex) {
	    Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	}

	// TODO to reveiw this regexp do not get the complet content/args if there is a ")" in it ... like Translation.get(myObject()+"someValue") ...
	// TODO a lead to explace this regexp will not work if Translation.get is refactoref ( ex : class renamed or methode renamend )
	Map<FindAllTraductionResult, Path> mapMatchResultToFilePath = matchTraductionGetInAllSrcJavaFiles(srcDir);

	SortedMap<String, ArrayList<FindAllTraductionResult>> groupIdenticalKey = getTraductionGetStringMissingKey(mapMatchResultToFilePath);
	logger.debug(String.format("groupIdenticalKey.size()=%d", groupIdenticalKey.size()));
	//
	// output the missing keys
	//
	for (String k : groupIdenticalKey.keySet()) {
	    logger.debug(String.format("missing traduction key : \"%s\"", k));
	    for (FindAllTraductionResult tr : groupIdenticalKey.get(k)) {
		logger.debug(String.format("  used in : \"%s\" line %d", tr.pSrc, tr.lineInFile));
	    }
	}
	//
	// TODO propose a lead for the resolution if one or more translation keys are missing.
	// TODO (Done in another PR of mine...) give the name of the .xml translation file where its keys are missing (normally it should be english.xml but to be checked.)
	// propose to disable the test because it is not a critical failure for the use of the appliation. (but then remember that it's not great / professional to make a version where translations are missing.)
	// propose to correct the source code to use existing keys or propose a partial .xml model with these new keys to facilitate the creation of its translations in the .xml translation file.

    }

    public static SortedMap<String, ArrayList<FindAllTraductionResult>> getTraductionGetStringMissingKey(Map<FindAllTraductionResult, Path> mapMatchResultToFilePath) {
	//
	// group identical missing keys.
	//
	int totalSrcLineWithMissingKey = 0;
	SortedMap<String, ArrayList<FindAllTraductionResult>> groupIdenticalKey = new TreeMap<>();
	for (FindAllTraductionResult tr : mapMatchResultToFilePath.keySet()) {
	    // find the key that get a Missing: when traducted
	    if (tr.isTraductionStartWithMissing()) {
		totalSrcLineWithMissingKey++;
		String k = tr.getSimpleStringFromArgs();		
		// a way to group same missing keys.
		if (groupIdenticalKey.containsKey(k)) {
		    groupIdenticalKey.get(k).add(tr);
		} else {
		    ArrayList<FindAllTraductionResult> alist = new ArrayList<>();
		    alist.add(tr);
		    groupIdenticalKey.put(k, alist);
		}
	    }
	}
	logger.debug(String.format("totalSrcLineWithMissingKey=%d", totalSrcLineWithMissingKey));
	return groupIdenticalKey;
    }

    public static Map<FindAllTraductionResult, Path> matchTraductionGetInAllSrcJavaFiles(File srcDir) {
	// "Translator\\s*\\.\\s*get\\s*\\(([^\\)]*)\\)" is a patternt to match and it define a group "(...)". "[^\\)]*" could be interpreted as any char that is not a ')'.
	// notice the "\" to despecialize some special char in the regexp like '\(' and also tu specify specific char like "\s"
	// the "\\" is cause we are in a string to finaly have a '\' ... (yes this is confusing ... )
	// 
	return matchPatternInAllFilesThatHaveAFilenameThatEndWithInADir(srcDir, ".java", "Translator\\s*\\.\\s*get\\s*\\(([^\\)]*)\\)");
    }

    /**
     * The patterne is used to match and have to containe a match group in this implementation.
     * 
     * N.B. : the pattern have to have 1 group ...
     * 
     * // See pattern regexp (fr) https://cyberzoide.developpez.com/tutoriels/java/regex/
	// normaly '(' and ')' are used to define group in patern matcher so to match a real '(' you have to déspécialise it by adding a trailling '\' ( and as we are in a string you have to add a '\' befor the '\' ... )
	// '.' in regexp is for any caractére if you whant to match a '.' you have to despecialise it by adding a '\' ...
	// [^\\).]
	// \s Un caractère blanc : [ \t\n\x0B\f\r]
	
     * @param srcDir
     * @param fileNameEndWith not a pattern just a string to select filename that end with it.
     * @param patternToMatchInFiles patterne used to match in files from srcDir, the pattern have to containe a match group e.g. "(...)"in this implementation.
     * @return 
     */
    public static Map<FindAllTraductionResult, Path> matchPatternInAllFilesThatHaveAFilenameThatEndWithInADir(File srcDir, String fileNameEndWith, String patternToMatchInFiles) {
	final Map<FindAllTraductionResult, Path> mapMatchResultToFilePath = new HashMap<>();
	try {

	    // list all .java files in srcDir.
	    List<Path> paths = listFiles(srcDir.toPath(), fileNameEndWith);

	    // search in the files ...
	    paths.forEach(x -> {
		mapMatchResultToFilePath.putAll(searchInAFile(x, srcDir.toPath(), patternToMatchInFiles));
	    });
	} catch (IOException ex) {
	    Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	}
	logger.debug(String.format("mapMatchResultToFilePath.size()=%d", mapMatchResultToFilePath.size()));
	return mapMatchResultToFilePath;
    }

    /**
     *
     * @param x the value of x
     * @param baseDir the value of baseDir
     * @param regexp the value of regexp ( see java.​util.​regex.Pattern )
     * @return the int
     */
    public static Map<FindAllTraductionResult, Path> searchInAFile(Path x, Path baseDir, String regexp) {
	Map<FindAllTraductionResult, Path> mapMatchResultToFilePath = new HashMap<>();
	try {
	    if (debugPaser) {
		logger.debug(String.format("searchInAFile %s",x));
	    }

	    Pattern pattern = Pattern.compile(regexp);

	    // not line by line ... not used ... only to vérifie if we miss some on multilines
	    int countMatchNotLineByLine = 0;
	    try ( Scanner sc = new Scanner(x)) {
		List<MatchResult> n = sc.findAll(pattern)
			.collect(Collectors.toList());
		if (n.size() > 0) {
//		    
logger.debug(String.format("::%d in %s", n.size(), x.toAbsolutePath()));
		    for (MatchResult mr : n) {
			countMatchNotLineByLine++;
			// Can we get the line num ? currently in this implementation we have the car pos in the file/stream ...
//			
logger.debug(String.format(" %-50s in %s at sart:%d end:%d", mr.group(1), mr.group(), mr.start(), mr.end()));
		    }
		}
	    }

	    // line by line ( this can miss some like "Traduction.\nget(\n...\n);" ) but it gave me the line number in the file/stream ...
	    Scanner scanner = new Scanner(x);
	    int lineNum = 0;
	    while (scanner.hasNextLine()) {
		lineNum++;
		String nextToken = scanner.nextLine();

		Matcher m = pattern.matcher(nextToken);

		while (m.find()) {
		    if (debugPaser) {
			logger.debug(String.format("#found (gp count=%d) : %s ", m.groupCount(), m.group(0)));			
		    }
		    if (true) {
			logger.debug(String.format("%s\n\tat %s(l:%d c:%d)", m.group(1), baseDir.relativize(x), lineNum, m.start(1)));
		    }
		    FindAllTraductionResult res = new FindAllTraductionResult(m.group(1), lineNum, m.start(1), x);
		    mapMatchResultToFilePath.put(res, x);
		}
	    }
	} catch (IOException ex) {
	    Logger.getLogger(FindAllTraductionGet.class.getName()).log(Level.SEVERE, null, ex);
	}
	return mapMatchResultToFilePath;
    }

    /**
     * List all files and sub files in this path. Using
     * <code>Files.walk(path)</code> (so this take care of recursive path
     * exploration ) And applying filter ( RegularFile and ReadableFile ) and
     * filtering FileName ...
     *
     * @param path where to look.
     * @param fileNameEndsWithSuffix use ".java" to get only ... ( this is not a
     * regexp so no '.' despecialization required ) can be set to
     * <code>""</code> to get all files.
     * @return a list of files (may be empty if nothing is found) or null if
     * something is wrong.
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
	if (debugListFiles) {
	    if (result == null) {
		logger.debug(String.format("listFiles (%s, \"%s\").size()=null", path, fileNameEndsWithSuffix));
	    } else {
		logger.debug(String.format("listFiles (%s, \"%s\").size()=%d", path, fileNameEndsWithSuffix, result.size()));
	    }
	}
	return result;
    }

}
