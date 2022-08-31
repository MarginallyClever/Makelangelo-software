package com.marginallyclever.convenience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestColorHSB {
    private void assertColorsEqual(ColorHSB c,int hue,int saturation,int brightness) {
        Assertions.assertEquals(hue,c.hue);
        Assertions.assertEquals(saturation,c.saturation);
        Assertions.assertEquals(brightness,c.brightness);
    }

    @Test
    public void limitRange() {
        assertColorsEqual(new ColorHSB(),0,0,0);
        assertColorsEqual(new ColorHSB(1,0,0),1,0,0);
        assertColorsEqual(new ColorHSB(0,1,0),0,1,0);
        assertColorsEqual(new ColorHSB(0,0,1),0,0,1);
        assertColorsEqual(new ColorHSB(-1,0,0),0,0,0);
        assertColorsEqual(new ColorHSB(0,-1,0),0,0,0);
        assertColorsEqual(new ColorHSB(0,0,-1),0,0,0);
        assertColorsEqual(new ColorHSB(2,0,0),1,0,0);
        assertColorsEqual(new ColorHSB(0,2,0),0,1,0);
        assertColorsEqual(new ColorHSB(0,0,2),0,0,1);
    }
}
