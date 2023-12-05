package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Day2 {
    static int totalScore = 0;
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Day1.class.getResourceAsStream("input.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void processLine(String line) {
        String [] parts = line.split(" ");
        int a = parts[0].getBytes()[0]-'A';
        int b = parts[1].getBytes()[0]-'X';
        int score = scoreRound2(a,b);
        totalScore+=score;
        System.out.println(line+" "+score+" "+totalScore);
    }

    /**
     * Score this round
     * rock A X 1pt
     * paper B Y 2pt
     * scissors C Z 3pt
     * also 0 for lose, 3 for draw, 6 for win.
     * @param a their move (0,1,2)
     * @param b my move (0,1,2)
     * @return b+1+(0|3|6)
     */
    static int scoreRound(int a, int b) {
        int score = 1+b;
        if(a==b) score+=3;
        else if(a==0 && b==1) score+=6;
        else if(a==1 && b==2) score+=6;
        else if(a==2 && b==0) score+=6;
        return score;
    }

    /**
     * Score this round
     * rock A X 1pt
     * paper B Y 2pt
     * scissors C Z 3pt
     * also 0 for lose, 3 for draw, 6 for win.
     * @param a their move (0,1,2)
     * @param b 0 lose, 1 draw, 2 win
     * @return b+1+(0|3|6)
     */
    static int scoreRound2(int a, int b) {
        int score = b*3;
        // add the value of the hand I would have chosen to win,lose,or draw.
        switch(b) {
            case 0: score+=(a+2)%3; break;
            case 1: score+=a; break;  // draw
            case 2: score+=(a+1)%3; break;
        }
        return 1+score;
    }
}
