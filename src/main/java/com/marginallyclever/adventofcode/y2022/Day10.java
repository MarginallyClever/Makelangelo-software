package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Day10 {
    static class Operation {
        String name;
        int param;
        int cycles;

        Operation(String name,int cycles) {
            this.name=name;
            this.cycles=cycles;
        }

        void setParam(int param) {
            this.param=param;
        }
        int getParam() {
            return param;
        }
    }

    static class CRTScreen {
        static int WIDTH=40;
        static int HEIGHT=6;
        int [] screen = new int[WIDTH*HEIGHT];
        int cursor=0;

        public CRTScreen() {
            Arrays.fill(screen, '.');
        }

        void advance() {
            cursor = (cursor+1)%screen.length;
        }
        void turnPixelOn() {
            screen[cursor]='*';
        }

        void draw() {
            for(int y=0;y<HEIGHT;++y) {
                for(int x=0;x<WIDTH;++x) {
                    System.out.print((char)screen[y*WIDTH+x]);
                }
                System.out.println();
            }
        }

        public int getCursorPosX() {
            return cursor % WIDTH;
        }
    }

    private Operation [] ops = {
            new Operation("noop", 1),
            new Operation("addx", 2),
    };
    private Operation currentOp;

    private int clock=0;
    private int registerX = 1;
    private int nextOp;
    private static int sum=0;
    private CRTScreen screen = new CRTScreen();

    public static void main(String[] args) {
        Day10 day = new Day10();
        day.processFile("input.txt");
        System.out.println("sum: "+sum);
        day.screen.draw();
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day9.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(br,line);
                do {
                    clock++;
                    checkRegister();
                } while(clock<nextOp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkRegister() {
        System.out.println("clock: "+clock+" registerX: "+registerX);
        switch(clock) {
            case 20: case 60: case 100: case 140: case 180: case 220:
                int toAdd = registerX * clock;
                sum += toAdd;
                System.out.println("toAdd: "+toAdd+" sum: "+sum);
                break;
        }
        if(Math.abs(screen.getCursorPosX()-registerX)<2) {
            screen.turnPixelOn();
        }
        screen.advance();

        if(clock==nextOp) {
            registerX += currentOp.param;
        }
    }
    private void processLine(BufferedReader br, String line) {
        System.out.println(line);
        String [] parts = line.split(" ");
        switch (parts[0]) {
            case "noop" -> currentOp = ops[0];
            case "addx" -> currentOp = ops[1];
        }
        currentOp.setParam( parts.length==1 ? 0 : Integer.parseInt(parts[1]) );
        nextOp = clock + currentOp.cycles;
    }
}
