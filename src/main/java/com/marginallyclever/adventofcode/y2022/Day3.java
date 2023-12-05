package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day3 {
    public static void main(String[] args) {
        System.out.println("test="+(examineSack("vJrwpWtwJgWrhcsFMMfFFhFp") +
        examineSack("jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL") +
        examineSack("PmmdzqPrVvPwwTWBwg") +
        examineSack("wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn") +
        examineSack("ttgJtRGJQctTZtZT") +
        examineSack("CrZsJsPPZsGzwwsLwLmpwMDw") ));
        System.out.println(examineThreeSacks("vJrwpWtwJgWrhcsFMMfFFhFp","jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL","PmmdzqPrVvPwwTWBwg"));
        System.out.println(examineThreeSacks("wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn","ttgJtRGJQctTZtZT","CrZsJsPPZsGzwwsLwLmpwMDw"));

        int sum=0;
        int sum2=0;
        int counter=0;
        String [] lines = new String[3];
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                sum += examineSack(line.trim());
                lines[0] = lines[1];
                lines[1] = lines[2];
                lines[2] = line.trim();
                counter++;
                if(counter==3) {
                    counter=0;
                    sum2+=examineThreeSacks(lines[0],lines[1],lines[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("sum="+sum);
        System.out.println("sum2="+sum2);
    }

    /**
     * split input in two halves, find the char shared by both halves, convert to int.
     * @param input
     * @return int value of char shared by both halves of String input.
     */
    private static int examineSack(String input) {
        int half = input.length()/2;
        String a = input.substring(0,half);
        String b = input.substring(half);

        return findCharSharedByTwoStrings(a,b);
    }

    private static int findCharSharedByTwoStrings(String a,String b) {
        // find the character shared by both a and b.
        for(int i=0;i<a.length();++i) {
            char c = a.charAt(i);
            int index = b.indexOf(c);
            if(index>=0) {
                if(Character.isLowerCase(c)) {
                    return c-'a'+1;
                } else {
                    return c-'A'+27;
                }
            }
        }
        return -1;
    }

    private static int examineThreeSacks(String a,String b,String c) {
        // find the character shared by both a and b and c.
        for(int i=0;i<a.length();++i) {
            char d = a.charAt(i);
            int index = b.indexOf(d);
            if(index>=0) {
                index = c.indexOf(d);
                if(index>=0) {
                    if(Character.isLowerCase(d)) {
                        return d-'a'+1;
                    } else {
                        return d-'A'+27;
                    }
                }
            }
        }
        return -1;
    }
}
