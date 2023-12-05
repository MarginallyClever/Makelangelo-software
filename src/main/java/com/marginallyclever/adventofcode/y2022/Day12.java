package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Day12 {
    static class MyMap {
        int width,height;
        int [] values;

        public MyMap() {
            width=77;
            height=41;
            values = new int[width*height];
        }

        int getIndex(int x,int y) {
            if(x<0 || x>=width || y<0 || y>=height) return -1;
            return y*width+x;
        }

        void set(int x,int y,int value) {
            set(getIndex(x,y),value);
        }

        void set(int index,int value) {
            values[index]=value;
        }

        int get(int x,int y) {
            return get(getIndex(x,y));
        }

        int get(int i) {
            return values[i];
        }

        void display() {
            for(int y=0;y<height;++y) {
                for(int x=0;x<width;++x) {
                    System.out.print(get(x,y)+"\t");
                }
                System.out.println();
            }
        }
    }

    MyMap map = new MyMap();
    MyMap searchField = new MyMap();
    int mapCounter=0;
    int indexStart, indexEnd;
    int smallestSearch = 10000;

    public static void main(String[] args) {
        Day12 me = new Day12();
        me.processFile("input.txt");
        //me.map.display();
        //me.doSearch(indexStart);
        me.doSearch2();
        //searchField.display();
        System.out.println("found end in "+me.smallestSearch+" steps.");
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
        for(int i=0;i<line.length();++i) {
            int v = line.charAt(i);
            if(v=='S') {
                indexStart = mapCounter;
                v='a';
            }
            if(v=='E') {
                indexEnd = mapCounter;
                v='z';
            }
            map.set(mapCounter++,v-'a');
        }
    }

    private void doSearch2() {
        for(int i=0;i<map.values.length;++i) {
            if(map.get(i)==0) {
                doSearch(i);
            }
        }
    }

    private void doSearch(int start) {
        int x = start % map.width;
        int y = start / map.width;
        System.out.println("start at "+x+","+y);
        Arrays.fill(searchField.values,10000);
        searchFrom(x,y,0);
    }

    private void searchFrom(int x, int y,int depth) {
        int i = map.getIndex(x,y);  // check bounds (should never happen
        int h = map.get(i);
        if(searchField.get(i) <= depth) return;  // already been here, better.
        searchField.set(i,depth);
        if(i==indexEnd) {
            if(smallestSearch>depth) smallestSearch=depth;
            return;
        }

        if(canReach(x+1,y,h)) searchFrom(x+1,y,depth+1);
        if(canReach(x-1,y,h)) searchFrom(x-1,y,depth+1);
        if(canReach(x,y+1,h)) searchFrom(x,y+1,depth+1);
        if(canReach(x,y-1,h)) searchFrom(x,y-1,depth+1);
    }

    private boolean canReach(int x,int y,int currentHeight) {
        int i = map.getIndex(x,y);
        if(i==-1) return false;  // off map
        return (map.get(i)-currentHeight) < 2;  // true if not too high or too low.
    }
}
