package com.marginallyclever.makelangelo.makeArt.io.vector;

import org.junit.jupiter.api.DynamicTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class LoadHelper {

    /**
     * Load a whole file in a String
     *
     * @param file path of the file in the classpath
     * @return the content of the file
     */
    public static String readFile(File file) throws FileNotFoundException {
        return new Scanner(new FileInputStream(file), StandardCharsets.UTF_8).useDelimiter("\\A").next();
    }

    /**
     * Generate a list of test based on the files ending with the specified extension in the given folder
     *
     * @param folder folder where the file are stored
     * @param extension extension of file to filter. Must start with a .
     * @param verify Method that check the expected behavior
     * @return a stream a test to launch for Junit
     */
    public static Stream<DynamicTest> loadAndTestFiles(String folder, String extension, BiConsumer<File, File> verify) {
        return Arrays.stream(new File(folder).listFiles())
                .map(File::getName)
                .filter(filename -> filename.endsWith(extension))
                .map(filename -> filename.replaceAll("([^.]*)\\..*", "$1"))
                .map(basename -> dynamicTest(basename + extension, () -> {
                    String base = folder + File.separator + basename;
                    File fileToTest = new File(base + extension);
                    File fileExpected = new File(base + "_expected.txt");
                    System.out.println("fileToTest = " + fileToTest + ", " + "fileExpected = " + fileExpected);
                    assertTrue(fileToTest.exists(), "file to test does not exists");
                    assertTrue(fileExpected.exists(), "expected file does not exists");
                    verify.accept(fileToTest, fileExpected);
                }));
    }
}
