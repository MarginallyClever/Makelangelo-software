package com.marginallyclever.adventofcode.y2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day1 {

    public static void main(String[] args) {
        Day1 me = new Day1();
        me.processFile("input.txt");
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int sum = 0;

    private void processLine(String line) {
        int value=0;
        // scan forward
        for(int i=0;i<line.length();++i) {
            Character c = line.charAt(i);
            if(Character.isDigit(c)) {
                value = Character.getNumericValue(c)*10;
                System.out.println(c+"="+value);
                break;
            }
            int j = matchDigitName(line.substring(i));
            if(j!=-1) {
                value = j*10;
                System.out.println(line.substring(i)+"="+value);
                break;
            }
        }
        // scan backward
        for(int i=line.length()-1;i>=0;--i) {
            Character c = line.charAt(i);
            if(Character.isDigit(c)) {
                value += Character.getNumericValue(c);
                System.out.println(c+"="+value);
                break;
            }
            int j = matchDigitName(line.substring(i));
            if(j!=-1) {
                value += j;
                System.out.println(line.substring(i)+"="+value);
                break;
            }
        }
        sum += value;
        System.out.println("sum="+sum);
    }

    private int matchDigitName(String substring) {
        if(substring.startsWith("one")) return 1;
        if(substring.startsWith("two")) return 2;
        if(substring.startsWith("three")) return 3;
        if(substring.startsWith("four")) return 4;
        if(substring.startsWith("five")) return 5;
        if(substring.startsWith("six")) return 6;
        if(substring.startsWith("seven")) return 7;
        if(substring.startsWith("eight")) return 8;
        if(substring.startsWith("nine")) return 9;
        return -1;
    }
}
