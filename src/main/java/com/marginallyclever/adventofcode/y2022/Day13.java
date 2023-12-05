package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Day13 {
    static class ListOrInteger {
        List<ListOrInteger> list = new ArrayList<>();
        int value;
        boolean isList = false;

        public ListOrInteger() {}

        void display(int depth) {
            if(isList) {
                System.out.println();
                indent(depth);
                System.out.println("[");
                for(ListOrInteger i : list) {
                    i.display(depth+1);
                    System.out.println(",");
                }
                indent(depth);
                System.out.print("]");
            } else {
                indent(depth);
                System.out.print(value);
            }
        }
        void indent(int depth) {
            for(int i=0;i<depth;++i) System.out.print("  ");
        }
    }

    private final ListOrInteger root = new ListOrInteger();
    private final Stack<ListOrInteger> stack = new Stack<>();

    public static void main(String[] args) {
        Day13 me = new Day13();
        me.root.isList=true;
        me.processFile("input.txt");
        me.root.display(0);

    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day11.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(br,line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(BufferedReader br, String line) {
        System.out.println(line);
        if(line.trim().isEmpty()) return;
        parse(line);
    }

    private void parse(String line) {
        stack.clear();
        ListOrInteger parent = null;
        for(int i=0;i<line.length();++i) {
            int c = line.charAt(i);
            if(c=='[') {
                ListOrInteger child = new ListOrInteger();
                child.isList=true;
                if(stack.isEmpty()) root.list.add(child);
                else parent.list.add(child);
                stack.push(child);
                parent = child;
            } else if(c==']') {
                stack.pop();
                if(stack.isEmpty()) parent = null;
                else parent = stack.peek();
            } else if(Character.isDigit(c)) {
                int firstComma = line.indexOf(',',i);
                if(firstComma==-1) firstComma = line.length();
                int firstClose = line.indexOf(']',i);
                if(firstClose==-1) firstClose = line.length();
                int end = Math.min(firstComma,firstClose);
                int value = Integer.parseInt(line.substring(i,end));
                ListOrInteger child = new ListOrInteger();
                child.value = value;
                parent.list.add(child);
                i = end-1;
            }
        }
    }
}
