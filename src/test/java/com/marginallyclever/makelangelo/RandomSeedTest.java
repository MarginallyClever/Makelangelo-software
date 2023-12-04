package com.marginallyclever.makelangelo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test that the random number generator is working as expected.
 * @author Dan Royer
 * @since 7.50.5
 */
public class RandomSeedTest {
    /**
     * Resetting the seed should generate the same sequence of numbers.
     */
    @Test
    public void testSame() {
        Random r = new Random();
        r.setSeed(0);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            list.add(r.nextInt());
        }
        r.setSeed(0);
        for(int i=0;i<10;++i) {
            Assertions.assertEquals(list.get(i),r.nextInt());
        }
    }

    /**
     * Two different seeds should produce different sequence of numbers.
     */
    @Test
    public void testDifferent() {
        Random r = new Random();
        r.setSeed(0);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            list.add(r.nextInt());
        }
        r.setSeed(1);
        boolean different = false;
        for(int i=0;i<10;++i) {
            if(list.get(i)!=r.nextInt()) {
                different = true;
                break;
            }
        }
        Assertions.assertTrue(different);
    }
}
