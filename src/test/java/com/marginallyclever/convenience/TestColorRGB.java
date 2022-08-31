package com.marginallyclever.convenience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestColorRGB {
    private void assertColorsEqual(ColorRGB c,int r,int g,int b) {
        Assertions.assertEquals(r,c.red);
        Assertions.assertEquals(g,c.green);
        Assertions.assertEquals(b,c.blue);
    }

    @Test
    public void limitRange() {
        assertColorsEqual(new ColorRGB(),0,0,0);
        assertColorsEqual(new ColorRGB(255,0,0),255,0,0);
        assertColorsEqual(new ColorRGB(0,255,0),0,255,0);
        assertColorsEqual(new ColorRGB(0,0,255),0,0,255);
        assertColorsEqual(new ColorRGB(-1,0,0),0,0,0);
        assertColorsEqual(new ColorRGB(0,-1,0),0,0,0);
        assertColorsEqual(new ColorRGB(0,0,-1),0,0,0);
        assertColorsEqual(new ColorRGB(256,0,0),255,0,0);
        assertColorsEqual(new ColorRGB(0,256,0),0,255,0);
        assertColorsEqual(new ColorRGB(0,0,256),0,0,255);
    }
}
