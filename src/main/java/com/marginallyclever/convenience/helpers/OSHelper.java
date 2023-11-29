package com.marginallyclever.convenience.helpers;

public class OSHelper {
    public static boolean isWindows() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("win");
    }

    public static boolean isOSX() {
        String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("mac") || OS.contains("osx");
    }
}
