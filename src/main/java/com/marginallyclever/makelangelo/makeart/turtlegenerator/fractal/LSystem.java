package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple <a href='https://en.wikipedia.org/wiki/L-system'>L-system</a> implementation for generating strings based
 * on production rules.
 * @author Dan Royer
 */
public class LSystem {
    private final Map<String, String> rules = new HashMap<>();

    public LSystem() {}

    /**
     * Add a production rule to the L-system.
     * @param predecessor the symbol to be replaced
     * @param successor the string to replace it with
     */
    public void addRule(String predecessor, String successor) {
        rules.put(predecessor, successor);
    }

    /**
     * Generate the L-system string after a given number of iterations.
     * @param axiom the initial string
     * @param iterations the number of iterations to apply the rules
     * @return the resulting string after applying the rules
     */
    public String generate(String axiom, int iterations) {
        String current = axiom;
        for (int i = 0; i < iterations; i++) {
            StringBuilder next = new StringBuilder();
            for (char c : current.toCharArray()) {
                String replacement = rules.getOrDefault(String.valueOf(c), String.valueOf(c));
                next.append(replacement);
            }
            current = next.toString();
        }
        return current;
    }
}
