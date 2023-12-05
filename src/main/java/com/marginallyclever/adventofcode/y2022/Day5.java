package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Day5 {
    private static final List<Stack<Character>> stacks = new ArrayList<>();
    private static final Stack<Character> tempStack = new Stack<>();

    public static void main(String[] args) {
        for(int i=0;i<9;++i) {
            stacks.add(new Stack<>());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printStacks();
        printFinalAnswer();
    }

    private static void reverseStacks() {
        for(Stack<Character> v : stacks) {
            Object[] array = v.toArray();
            for(int i=0;i<array.length/2;++i) {
                Object temp = array[i];
                array[i] = array[array.length-i-1];
                array[array.length-i-1] = temp;
            }
            v.clear();
            for(Object c : array) {
                v.push((Character)c);
            }
        }
    }

    private static void printStacks() {
        for(Stack<Character> v : stacks) {
            System.out.println(Arrays.toString(v.toArray()));
        }
    }

    private static void printFinalAnswer() {
        for(Stack<Character> v : stacks) {
            System.out.print(v.peek());
        }
        System.out.println();
    }

    static int once=0;
    private static void processLine(String line) {
        if(line.trim().startsWith("[")) {
            // still reading stacks
            readStack(line);
        } else if(line.trim().isEmpty() && once++==0) {
            reverseStacks();
        } else if(line.startsWith("move")) {
            moveStack(line);
        }
    }

    private static void readStack(String line) {
        for(int i=1;i<line.length();i+=4) {
            char c = line.charAt(i);
            if(c==' ') continue;
            stacks.get(i/4).push(c);
        }
    }

    // move 1 from 7 to 6
    private static void moveStack(String line) {
        printStacks();
        String[] parts = line.split(" ");
        // 0 move
        // 1 count
        // 2 from
        // 3 src
        // 4 to
        // 5 dst
        int count = Integer.parseInt(parts[1]);
        int src = Integer.parseInt(parts[3]);
        int dst = Integer.parseInt(parts[5]);
        System.out.println("Move "+count+" from "+src+" to "+dst);
        for(int i=0;i<count;++i) {
            tempStack.push(stacks.get(src-1).pop());
        }
        for(int i=0;i<count;++i) {
            stacks.get(dst-1).push(tempStack.pop());
        }
    }

    private static void move(int src,int dst) {
        stacks.get(dst).push(stacks.get(src).pop());
    }
}
