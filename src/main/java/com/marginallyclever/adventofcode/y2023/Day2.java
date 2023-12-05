package com.marginallyclever.adventofcode.y2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day2 {

    public static void main(String[] args) {
        Day2 me = new Day2();
        me.processFile("input.txt");
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day2.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int sum = 0;
    final int maxR = 12;
    final int maxG = 13;
    final int maxB = 14;

    /**
     * line is in format
     * <pre>Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
     * Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue</pre>
     */
    private void processLine(String line) {
        // read the header
        if(!line.startsWith("Game ")) {
            throw new RuntimeException("Expected line to start with 'Game ' but got '"+line+"'");
        }
        line = line.substring(5);
        int colon = line.indexOf(":");
        String s1 = line.substring(0,colon);
        int gameNumber = Integer.parseInt(s1);
        System.out.println("Game "+gameNumber);
        line = line.substring(colon+1);

        int r=0;
        int g=0;
        int b=0;

        // split by `;`
        String [] parts = line.split(";");
        for(String part : parts) {
            // split part by `,`
            String [] colorSection = part.split(",");
            for(String section : colorSection) {
                section = section.trim();
                // split section by ' '
                String [] colorCount = section.split(" ");
                int count = Integer.parseInt(colorCount[0]);
                String colorName = colorCount[1];

                switch(colorName) {
                    case "red": r = count;  break;
                    case "green": g = count;  break;
                    case "blue": b = count;   break;
                    default: throw new RuntimeException("Unknown color '"+colorName+"'");
                }
            }
            if(r>maxR) return;
            if(g>maxG) return;
            if(b>maxB) return;
        }
        System.out.println("  red="+r+" green="+g+" blue="+b);
        System.out.println("Game "+gameNumber+" is valid.");
        sum += gameNumber;
        System.out.println("sum is "+sum);
    }
}
