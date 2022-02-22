package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LoadHelper {

    /**
     * Load a whole file in a String
     * @param filename path of the file in the classpath
     * @return the content of the file
     */
    public static String readFile(String filename) {
        return new Scanner(LoadSVGTest.class.getResourceAsStream(filename), StandardCharsets.UTF_8).useDelimiter("\\A").next();
    }
}
