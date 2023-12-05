package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day4 {
    public static void main(String[] args) {
        System.out.println(processLine("6-6,4-6"));
        System.out.println(processLine("2-8,3-7"));

        int sum=0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                sum += processLine(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("sum="+sum);
    }

    private static int processLine(String line) {
        String [] parts = line.split("[,-]");
        //System.out.println(Arrays.toString(parts));
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        int d = Integer.parseInt(parts[3]);
        if(overlap(a,b,c,d) || overlap(c,d,a,b)) return 1;
        return 0;
    }

    private static boolean overlap(int a,int b,int c,int d) {
        if(a<=c && c<=b) return true;
        if(a<=d && d<=b) return true;
        return false;
    }
}
