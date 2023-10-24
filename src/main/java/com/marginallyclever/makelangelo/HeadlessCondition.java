package com.marginallyclever.makelangelo;

public class HeadlessCondition {
    public static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("java.awt.headless", "false"));
    }
}