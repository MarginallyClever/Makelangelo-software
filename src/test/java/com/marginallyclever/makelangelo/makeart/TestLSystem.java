package com.marginallyclever.makelangelo.makeart;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLSystem {
    @Test
    public void testCantorSet() {
        var test = new LSystem("A");
        test.setRule("A","ABA");
        test.setRule("B","BBB");
        Assertions.assertEquals("A", test.getResult(0));
        Assertions.assertEquals("ABA", test.getResult(1));
        Assertions.assertEquals("ABABBBABA", test.getResult(2));
        Assertions.assertEquals("ABABBBABABBBBBBBBBABABBBABA", test.getResult(3));
    }

    @Test
    public void testTwoRulesAndConstants() {
        // an LSystem tree
        var test = new LSystem("0");
        test.setRule("0","1[0]0");
        test.setRule("1","11");
        Assertions.assertEquals("0", test.getResult(0));
        Assertions.assertEquals("1[0]0", test.getResult(1));
        Assertions.assertEquals("11[1[0]0]1[0]0", test.getResult(2));
        Assertions.assertEquals("1111[11[1[0]0]1[0]0]11[1[0]0]1[0]0", test.getResult(3));
    }

    @Test
    public void testKochCurve() {
        var test = new LSystem("F");
        test.setRule("F","F+F-F-F+F");
        Assertions.assertEquals("F", test.getResult(0));
        Assertions.assertEquals("F+F-F-F+F", test.getResult(1));
        Assertions.assertEquals("F+F-F-F+F+F+F-F-F+F-F+F-F-F+F-F+F-F-F+F+F+F-F-F+F", test.getResult(2));
    }

    @Test
    public void testSierpinksiTriangle() {
        var test = new LSystem("F-G-G");
        test.setRule("F","F-G+F+G-F");
        test.setRule("G","GG");
        Assertions.assertEquals("F-G-G", test.getResult(0));
        Assertions.assertEquals("F-G+F+G-F-GG-GG", test.getResult(1));
        Assertions.assertEquals("F-G+F+G-F-GG+F-G+F+G-F+GG-F-G+F+G-F-GGGG-GGGG", test.getResult(2));
    }

    @Test
    public void testSierpinksiTriangle2() {
        var test = new LSystem("A");
        test.setRule("A","B-A-B");
        test.setRule("B","A+B+A");
        Assertions.assertEquals("A", test.getResult(0));
        Assertions.assertEquals("B-A-B", test.getResult(1));
        Assertions.assertEquals("A+B+A-B-A-B-A+B+A", test.getResult(2));
    }

    @Test
    public void testDragonCurve() {
        var test = new LSystem("F");
        test.setRule("F","F+G");
        test.setRule("G","F-G");
        Assertions.assertEquals("F", test.getResult(0));
        Assertions.assertEquals("F+G", test.getResult(1));
        Assertions.assertEquals("F+G+F-G", test.getResult(2));
    }
}
