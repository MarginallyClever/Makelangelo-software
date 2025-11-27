package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import org.junit.jupiter.api.Test;

public class LSystemTest {
    @Test
    public void test() {
        LSystem lSystem = new LSystem();
        lSystem.addRule("A", "AB");
        lSystem.addRule("B", "A");
        String result = lSystem.generate("A", 5);
        assert result.equals("ABAABABAABAAB");
    }
}
