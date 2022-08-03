package com.marginallyclever.makelangelo.makeart;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLSystem {
    @Test
    public void testTwoRules() {
        var test = new LSystem("A");
        test.setRule("A","AB");
        test.setRule("B","A");
        Assertions.assertEquals("A", test.getResult(0));
        Assertions.assertEquals("AB", test.getResult(1));
        Assertions.assertEquals("ABA", test.getResult(2));
        Assertions.assertEquals("ABAAB", test.getResult(3));
        Assertions.assertEquals("ABAABABA", test.getResult(4));
    }
    @Test
    public void testTwoRulesAndConstants() {
        var test = new LSystem("0");
        test.setRule("0","1[0]0");
        test.setRule("1","11");
        Assertions.assertEquals("0", test.getResult(0));
        Assertions.assertEquals("1[0]0", test.getResult(1));
        Assertions.assertEquals("11[1[0]0]1[0]0", test.getResult(2));
        Assertions.assertEquals("1111[11[1[0]0]1[0]0]11[1[0]0]1[0]0", test.getResult(3));
    }
}
