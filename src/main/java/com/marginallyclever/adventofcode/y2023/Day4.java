package com.marginallyclever.adventofcode.y2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Day4 {
    public static void main(String[] args) {
        Day4 me = new Day4();
        me.processFile("input.txt");
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(Day4.class.getResourceAsStream(filename)))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int totalSum=0;

    private void processLine(String line) {
        // split on ':'
        String[] parts = line.split(":");
        // split the second half on '|'
        String[] secondHalf = parts[1].split("\\|");
        List<Integer> winners = splitNumbers(secondHalf[0]);
        List<Integer> card = splitNumbers(secondHalf[1]);
        // find each card number in the winners list
        int winningNumbers = 0;
        for (Integer cardNumber : card) {
            if (winners.contains(cardNumber)) {
                if(winningNumbers==0) winningNumbers=1;
                else winningNumbers<<=1;
            }
        }
        totalSum += winningNumbers;
        System.out.println("Card is worth "+winningNumbers+" points for a total of "+totalSum+" points.");
    }

    private List<Integer> splitNumbers(String s) {
        // split the string on ' ' and convert to a List of integers
        ArrayList<Integer> list = new ArrayList<>();
        String[] parts = s.trim().split(" ");
        for (String part : parts) {
            if(part.isEmpty()) continue;
            list.add(Integer.parseInt(part));
        }
        return list;
    }
}