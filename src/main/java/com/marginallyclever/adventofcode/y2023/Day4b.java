package com.marginallyclever.adventofcode.y2023;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Day4b {
    private static int totalWinningScore =0;
    private final List<ScratchCard> allCards = new ArrayList<>();

    static class ScratchCard {
        int index;
        final List<Integer> winners;
        final List<Integer> card;
        // how many winning numbers?
        int winningNumbers = 0;

        public ScratchCard(int index,String line) {
            this.index = index;
            // split on ':'
            String[] parts = line.split(":");
            // split the second half on '|'
            String[] secondHalf = parts[1].split("\\|");
            winners = splitNumbers(secondHalf[0]);
            card = splitNumbers(secondHalf[1]);
            countWinners();
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

        private void countWinners() {
            int winningScore = 0;
            // find each card number in the winners list
            for (Integer cardNumber : card) {
                if (winners.contains(cardNumber)) {
                    winningNumbers++;
                    if(winningScore==0) winningScore=1;
                    else winningScore<<=1;
                }
            }

            totalWinningScore += winningScore;
            System.out.println("Card is worth "+winningScore+" points for a total of "+ totalWinningScore +" points.");
        }
    }

    public static void main(String[] args) {
        Day4b me = new Day4b();
        // get time in nanoseconds
        long t0 = System.nanoTime();

        me.processFile("input.txt");
        long t1 = System.nanoTime();
        me.countCardsObtained();
        long t2 = System.nanoTime();

        System.out.println("Time to read file: "+(t1-t0)/1000000+"ms");
        System.out.println("Time to count cards: "+(t2-t1)/1000000+"ms");
    }

    private void countCardsObtained() {
        Map<Integer,Integer> cardCount = new HashMap<>();
        List<ScratchCard> toProcess = new ArrayList<>(allCards);
        // find all toProcess with no winners and move them to cardCount.
        for(ScratchCard card : allCards) {
            if(card.winningNumbers==0) {
                cardCount.put(card.index,0);
                toProcess.remove(card);
                System.out.println("Card "+(card.index+1)+" has 0 winners.");
            }
        }
        while(!toProcess.isEmpty()) {
            ScratchCard card = toProcess.remove(0);
            int start = card.index+1;
            int end = start + card.winningNumbers;
            int sum=0;
            int i;
            for(i=start;i<end;++i) {
                if(!cardCount.containsKey(i)) break;
                sum += cardCount.get(i)+1;
            }
            if(i==end) {
                // all cards are in cardCount, so we can add this card to cardCount
                cardCount.put(card.index,sum);
                System.out.println("Card "+(card.index+1)+" has "+sum+" winners.");
            } else {
                // not all cards are in cardCount yet, so we can't add this card to cardCount
                toProcess.add(card);
            }
        }

        int totalWinningCards=allCards.size();
        for(ScratchCard card : allCards) {
            if(cardCount.containsKey(card.index)) {
                totalWinningCards += cardCount.get(card.index);
            }
        }

        System.out.println("Total winning cards: "+totalWinningCards);
    }

    private void processFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(Objects.requireNonNull(Day4b.class.getResourceAsStream(filename)))))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        ScratchCard scratchCard = new ScratchCard(allCards.size(),line);
        allCards.add(scratchCard);
    }
}