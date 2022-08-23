package com.marginallyclever.makelangelo.makeart;

import java.util.HashMap;
import java.util.Map;

/**
 * Lindenmayer System (L System) processor.  Supply the axiom, the rules, and then `getResult(depth)`
 * See also <a href='https://en.wikipedia.org/wiki/L-system#Examples_of_L-systems'>Procedural L Systems</a>.
 * Does not understand context sensitive grammars, stochastic grammars, parametric grammars, or bi-directional grammars.
 * @author Dan Royer
 * @since 2022-08-03
 */
public class LSystem {
    private final Map<String,String> rules = new HashMap<>();
    private final String axiom;

    public LSystem(String axiom) {
        super();
        this.axiom = axiom;
    }

    /**
     * @param from a String with one character.
     * @param to any number of characters.
     */
    public void setRule(String from,String to) {
        rules.put(from,to);
    }

    public String getResult(int depth) {
        String result=axiom;

        for(int i=0;i<depth;++i) {
            var sb = new StringBuilder();
            for( int j=0;j<result.length();++j) {
                String b = result.substring(j,j+1);
                String to = rules.get(b);
                sb.append(to==null ? b : to);
            }
            result = sb.toString();
        }

        return result;
    }
}
