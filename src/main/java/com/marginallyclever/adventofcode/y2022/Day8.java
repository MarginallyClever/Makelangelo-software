package com.marginallyclever.adventofcode.y2022;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day8 {
    public static class Matrix {
        private int width, height;
        private int[] data;
        private int counter=0;

        Matrix(int w,int h) {
            width=w;
            height=h;
            data = new int[w*h];
        }

        int getWidth() {
            return width;
        }

        int getHeight() {
            return height;
        }

        boolean inside(int x,int y) {
            return x>=0 && x<width && y>=0 && y<height;
        }

        boolean inside(Point2d p) {
            return inside((int)p.x,(int)p.y);
        }

        int getIndex(int x,int y) {
            return x+y*width;
        }

        int get(int x,int y) {
            return data[getIndex(x,y)];
        }
        int get(Point2d p) {
            return data[getIndex((int)p.x,(int)p.y)];
        }

        void set(int x,int y, int v) {
            data[getIndex(x,y)]=v;
        }
        void set(Point2d p,int v) {
            set((int)p.x,(int)p.y,v);
        }

        void append(int v) {
            data[counter++] = v;
        }

        public boolean isFull() {
            return counter == width * height;
        }
    }


    public static Matrix grid, visible;

    public static void main(String[] args) {
        processFile("input.txt");
        printGrid(grid);
        countVisibleFromOutside();
        printGrid(visible);
        countGrid(visible);
        scoreTrees();
    }

    public static void scoreTrees() {
        int bestScore = 0;
        for(int y=0;y<grid.getHeight();++y) {
            for(int x=0;x<grid.getWidth();++x) {
                int score = countVisibleFromInside(x,y);
                if(score>bestScore) {
                    bestScore = score;
                    System.out.println("new best score: "+bestScore+" at "+x+","+y);
                }
            }
        }
    }

    private static void countGrid(Matrix visible) {
        int count=0;
        for(int i=0;i<visible.data.length;++i) {
            if(visible.data[i]==1) ++count;
        }
        System.out.println("visible: "+count);
    }

    private static int countVisibleFromInside(int x,int y) {
        return  scanInside(new Point2d(x, y), new Vector2d( 1,0)) *
                scanInside(new Point2d(x, y), new Vector2d(-1,0)) *
                scanInside(new Point2d(x, y), new Vector2d(0, 1)) *
                scanInside(new Point2d(x, y), new Vector2d(0,-1));
    }

    private static int scanInside(Point2d p,Vector2d dir) {
        int score = 0;
        int a = grid.get(p);
        p.add(dir);
        while(grid.inside(p)) {
            int b = grid.get(p);
            ++score;
            if(b>=a) break;
            p.add(dir);
        }
        return score;
    }

    private static void countVisibleFromOutside() {
        int w=grid.getWidth()-1;
        for(int y=0;y<grid.getHeight();++y) {
            scanOutside(new Point2d(0, y), new Vector2d(1,0));
            scanOutside(new Point2d(w, y), new Vector2d(-1,0));
        }
        int h=grid.getHeight()-1;
        for(int x=0;x<grid.getWidth();++x) {
            scanOutside(new Point2d(x, 0), new Vector2d(0,1));
            scanOutside(new Point2d(x, h), new Vector2d(0,-1));
        }
    }

    private static void scanOutside(Point2d pos, Vector2d dir) {
        int a = grid.get(pos);
        visible.set(pos,1);
        pos.add(dir);
        while(grid.inside(pos)) {
            int b = grid.get(pos);
            if(b>a) {
                // b is taller than a.
                a = b;
                visible.set(pos, 1);
            }
            pos.add(dir);
        }
    }

    public static void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day8.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(br,line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void processLine(BufferedReader br,String line) {
        //System.out.println(line);
        if(grid==null) {
            int len = line.length();
            grid = new Matrix(len,len);
            visible = new Matrix(len,len);
        }
        for(char c : line.toCharArray()) {
            grid.append(c-'0');
        }
    }

    public static void printGrid(Matrix m) {
        for(int y=0;y<m.getHeight();++y) {
            for(int x=0;x<m.getWidth();++x) {
                System.out.print(m.get(x,y));
            }
            System.out.println();
        }
        if(m.isFull()) System.out.println("grid is full");
    }
}
