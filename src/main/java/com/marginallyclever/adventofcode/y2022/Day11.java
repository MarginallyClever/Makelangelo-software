package com.marginallyclever.adventofcode.y2022;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Day11 {
    static class Monkey {
        List<Long> items = new ArrayList<>();
        int operation = '*';
        String opAmount = "1";
        int testDivideBy = 1;
        int tossTrue = 0;
        int tossFalse = 0;
        int inspections = 0;

        public void print() {
            //System.out.println("Monkey");
            //System.out.println("  items: "+items);
            //System.out.println("  Operation: old "+(char)operation+" "+opAmount);
            //System.out.println("  Test: divisible by "+testDivideBy);
            //System.out.println("    If true: throw to monkey "+tossTrue);
            //System.out.println("    If false: throw to monkey "+tossFalse);
            System.out.println("  Inspections: "+inspections);
        }
    }

    private final List<Monkey> monkeys = new ArrayList<>();
    private int lcm;

    public static void main(String[] args) {
        Day11 me = new Day11();
        me.processFile("input.txt");
        me.calculateLCM();

        for(int i=0;i<10000;++i) {
            me.doRound();
        }

        me.printAllMonkeys();
    }

    private void calculateLCM() {
        lcm=1;
        for(Monkey m : monkeys) {
            lcm *= m.testDivideBy;
        }
        System.out.println("LCM: "+lcm);
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
        //System.out.println(line);
        if(line.startsWith("Monkey ")) {
            monkeys.add(new Monkey());
            return;
        }
        Monkey m = monkeys.get(monkeys.size()-1);
        if(line.startsWith("  Starting items: ")) {
            String [] items = line.substring(17).split(", ");
            for(int i=0;i<items.length;++i) {
                m.items.add(Long.parseLong(items[i].trim()));
            }
            return;
        }
        if(line.startsWith("  Operation: new = old ")) {
            m.operation = line.charAt(23);
            m.opAmount = line.substring(25);
            return;
        }
        if(line.startsWith("  Test: divisible by ")) {
            m.testDivideBy = Integer.parseInt(line.substring(21));
            return;
        }
        if(line.startsWith("    If true: throw to monkey ")) {
            m.tossTrue = Integer.parseInt(line.substring(29));
            return;
        }
        if(line.startsWith("    If false: throw to monkey ")) {
            m.tossFalse = Integer.parseInt(line.substring(30));
            return;
        }
    }

    private void printAllMonkeys() {
        for(Monkey m : monkeys) {
            m.print();
        }
    }

    private void doRound() {
        for(Monkey m : monkeys) {
            doMonkeyInspectAllItems(m);
        }
    }

    private void doMonkeyInspectAllItems(Monkey m) {
        while(!m.items.isEmpty()) {
            doMonkeyInspectItem(m);
            adjustRelief(m);
            doMonkeyTest(m);
        }
    }

    private void doMonkeyInspectItem(Monkey m) {
        m.inspections++;

        long worry = m.items.get(0);
        String opAmount = m.opAmount;
        long opAmountInt = opAmount.equals("old") ? worry : Integer.parseInt(opAmount);

        switch(m.operation) {
            case '+': worry += opAmountInt; break;
            case '*': worry *= opAmountInt; break;
        }
        m.items.set(0,worry);
    }

    private void adjustRelief(Monkey m) {
        long worry = m.items.get(0);
        worry = worry % lcm;
        //worry=worry/3;
        m.items.set(0,worry);
    }

    private void doMonkeyTest(Monkey m) {
        long worry = m.items.remove(0);
        long remain = worry % m.testDivideBy;
        if(remain==0) {
            monkeys.get(m.tossTrue).items.add(worry);
        } else {
            monkeys.get(m.tossFalse).items.add(worry);
        }
    }
}
