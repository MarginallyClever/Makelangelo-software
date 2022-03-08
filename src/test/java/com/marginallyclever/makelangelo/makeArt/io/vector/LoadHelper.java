package com.marginallyclever.makelangelo.makeArt.io.vector;

import org.junit.jupiter.api.DynamicTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LoadHelper {

    /**
     * Load a whole file in a String
     *
     * @param filename path of the file in the classpath
     * @return the content of the file
     */
    public static String readFile(String filename) {
        Scanner scanner = new Scanner(LoadHelper.class.getResourceAsStream(filename), StandardCharsets.UTF_8).useDelimiter("\\A");
        assertTrue(scanner.hasNext(), "The file '"+ filename + "' is empty");
        return scanner.next();
    }

    /**
     * Load a whole file in a String
     *
     * @param file path of the file
     * @return the content of the file
     */
    public static String readFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8).useDelimiter("\\A");
        assertTrue(scanner.hasNext(), "The file '"+ file + "' is empty");
        return scanner.next();
    }

    /**
     * Generate a list of test based on the files ending with the specified extension in the given folder
     *
     * @param filenames list of files to verify
     * @param folder    folder where the file are stored
     * @param verify    Method that check the expected behavior
     * @return a stream a test to launch for Junit
     */
    public static Stream<DynamicTest> loadAndTestFiles(List<String> filenames, String folder, BiConsumer<String, String> verify) {
        return filenames.stream()
                .map(filename -> dynamicTest(filename, () -> {
                    String fileToTest = folder + "/" + filename;
                    String basename = filename.replaceAll("([^.]*)\\..*", "$1");
                    String fileExpected = folder + "/" + basename + "_expected.txt";
                    System.out.println("fileToTest = " + fileToTest + ", " + "fileExpected = " + fileExpected);
                    assertNotNull(LoadHelper.class.getResourceAsStream(fileToTest), "the file '" + fileToTest + "' to test does not exists");
                    assertNotNull(LoadHelper.class.getResourceAsStream(fileExpected), "the expected file '" + fileExpected + "'does not exists");
                    verify.accept(fileToTest, fileExpected);
                }));
    }
}
