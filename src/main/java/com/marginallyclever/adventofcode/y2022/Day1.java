package com.marginallyclever.adventofcode.y2022;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Day1 {
    static List<Integer> sums = new ArrayList<>();
    public static void main(String[] args) throws FileNotFoundException {
        sums.add(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sums.sort(Integer::compareTo);

        int size= sums.size();
        int total=0;
        for(int i=size-3;i<size;++i) {
            total+=sums.get(i);
        }
        System.out.println(total);
    }

    private static void processLine(String line) {
        if(line.isEmpty()) {
            sums.add(0);
            return;
        }
        int index = sums.size()-1;
        Integer value = Integer.parseInt(line) + sums.get(index);
        sums.set(index,value);
    }
}
