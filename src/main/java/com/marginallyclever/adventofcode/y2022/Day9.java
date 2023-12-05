package com.marginallyclever.adventofcode.y2022;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class Day9 {
    public class Matrix {
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
            if(!inside(p)) throw new RuntimeException("out of bounds "+p.toString());
            set((int)p.x,(int)p.y,v);
        }

        void append(int v) {
            data[counter++] = v;
        }

        public boolean isFull() {
            return counter == width * height;
        }
    }


    private Matrix grid, visible;
    private final Point2d head = new Point2d(0,0),
                                tail = new Point2d(0,0);
    private int maxWidth =0, maxHeight =0,minWidth =0, minHeight =0;

    private final Vector2d diff = new Vector2d();
    private Day9 next;

    public static void main(String[] args) {
        Day9 [] knots = new Day9[10];
        for(int i=0;i<9;++i) {
            knots[i] = new Day9();
            if(i>0) {
                knots[i-1].next = knots[i];
            }
        }

        knots[0].processFile("input.txt", knots[0]::moveDrag);
        knots[8].printGrid(knots[8].visible);
        knots[8].countGrid(knots[8].visible);
    }

    public Day9() {
        head.set(0,0);
        processFile("day9.txt", this::moveCount);
        System.out.println("maxWidth: "+ maxWidth +" maxHeight: "+ maxHeight);
        System.out.println("minWidth: "+ minWidth +" minHeight: "+ minHeight);

        grid    = new Matrix(maxWidth-minWidth, maxHeight-minHeight);
        visible = new Matrix(maxWidth-minWidth, maxHeight-minHeight);
        head.set(-minWidth, -minHeight);
        tail.set(head);
        visible.set(tail,1);
    }

    private void moveCount(Vector2d dir) {
        head.add(dir);
        if(maxWidth  <= head.x) maxWidth  = 1+(int)head.x;
        if(maxHeight <=head.y) maxHeight = 1+(int)head.y;
        if(minWidth  > head.x) minWidth  = (int)head.x;
        if(minHeight > head.y) minHeight = (int)head.y;
    }

    private void moveDrag(Vector2d dir) {
        head.add(dir);
        diff.sub(head,tail);
        dir.set(0,0);
        if(Math.abs(diff.x)>1 || Math.abs(diff.y)>1) {
            dir.x += Math.signum(diff.x);
            dir.y += Math.signum(diff.y);
        }
        tail.add(dir);
        visible.set(tail,1);

        if(next!=null) next.moveDrag(dir);
    }

    private void countGrid(Matrix visible) {
        int count=0;
        for(int i=0;i<visible.data.length;++i) {
            if(visible.data[i]==1) ++count;
        }
        System.out.println("count: "+count);
    }

    private void processFile(String filename, Consumer<Vector2d> mover) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day9.class.getResourceAsStream(filename))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(br,line,mover);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(BufferedReader br, String line, Consumer<Vector2d> mover) {
        System.out.println(line);
        if(grid==null) {
            int len = line.length();
            grid = new Matrix(len,len);
            visible = new Matrix(len,len);
        }
        String [] parts = line.split(" ");
        int count = Integer.parseInt(parts[1]);
        while(--count>=0) {
            switch (parts[0]) {
                case "U" -> mover.accept(new Vector2d(0, -1));
                case "D" -> mover.accept(new Vector2d(0,  1));
                case "L" -> mover.accept(new Vector2d(-1, 0));
                case "R" -> mover.accept(new Vector2d( 1, 0));
            }
        }
    }

    private void printGrid(Matrix m) {
        for(int y=0;y<m.getHeight();++y) {
            for(int x=0;x<m.getWidth();++x) {
                System.out.print(m.get(x,y));
            }
            System.out.println();
        }
        if(m.isFull()) System.out.println("grid is full");
    }
}
