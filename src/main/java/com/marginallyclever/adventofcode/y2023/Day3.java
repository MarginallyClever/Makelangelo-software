package com.marginallyclever.adventofcode.y2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Day3 {
    private final List<String> lines = new ArrayList<>();

    static class GearPoint {
        public int x,y;
        public int count=0;
        public int sum=1;

        public GearPoint(int x,int y) {
            this.x=x;
            this.y=y;
        }
    }
    List<GearPoint> gearPoints = new ArrayList<>();

    public static void main(String[] args) {
        Day3 me = new Day3();
        me.processFile("input.txt");
        //System.out.println(me.isAdjacentToSymbol(137,96));
        me.findAllGearPoints();
        me.sumNumbersAdjacentToSymbol();
        //me.display();
    }

    private void findAllGearPoints() {
        // find all '*'
        int sum=0;
        for(int i=0;i<lines.size();++i) {
            String line = lines.get(i);
            for(int j=0;j<line.length();++j) {
                char c = line.charAt(j);
                if(c=='*') gearPoints.add(new GearPoint(j, i));
            }
        }
    }

    private void display() {
        for(String line : lines) {
            System.out.println(line);
        }
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(Day3.class.getResourceAsStream(filename)))))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sumNumbersAdjacentToSymbol() {
        int sum = 0;

        for(int y=0;y<lines.size();++y) {
            String line = lines.get(y);
            for(int x=0;x<line.length();++x) {
                char c = line.charAt(x);
                if(Character.isDigit(c)) {
                    int length=getLengthOfNumber(line,x);
                    char symbol = isAdjacentToSymbol(y,x);
                    if(symbol=='.') {
                        String fill = ".".repeat(length);
                        line = line.substring(0,x) + fill + line.substring(x+length);
                    } else {
                        int value = Integer.parseInt(line.substring(x,x+length));
                        System.out.println(value);
                        sum+=value;
                        if(symbol=='*') adjacentSymbolIsGear(y,x,length,value);
                    }
                    x+=length-1;
                }
            }
            lines.set(y,line);
        }
        System.out.println("sum="+sum);

        // sum all GearPoint with count==2
        int gearSum = 0;
        for(GearPoint gp : gearPoints) {
            if(gp.count==2) {
                gearSum+=gp.sum;
            }
        }
        System.out.println("gearSum="+gearSum);
    }

    private void adjacentSymbolIsGear(int y, int x,int length,int value) {
        // find which GearPoint is within y-1...y+1, x-1...x+length
        for(GearPoint gp : gearPoints) {
            if(gp.x>=x-1 && gp.x<x+length+1 && gp.y>=y-1 && gp.y<y+2) {
                gp.count++;
                gp.sum*=value;
            }
        }
    }

    /**
     * Starting at i,j is a number of one or more digits.  Scan around each digit for a symbol.  A symbol is any
     * character that is not a digit and not a period and not a line ending.
     * @param y the line number
     * @param x the column number
     * @return the symbol found, or '.' if none found.
     */
    private Character isAdjacentToSymbol(int y, int x) {
        String line = lines.get(y);
        int length=getLengthOfNumber(line,x);

        for(int k=-1;k<=1;++k) {
            for(int l=-1;l<=length;++l) {
                if(y+k<0 || y+k>=lines.size()) continue;
                if(x+l<0 || x+l>=line.length()) continue;
                char c = lines.get(y+k).charAt(x+l);
                if(!Character.isDigit(c) && c!='.' && c!='\n') {
                    return c;
                }
            }
        }
        return '.';
    }

    int getLengthOfNumber(String line,int j) {
        int length=0;
        for(int k=j;k<line.length();++k) {
            char c = line.charAt(k);
            if(Character.isDigit(c)) {
                length++;
            } else {
                break;
            }
        }
        return length;
    }
}