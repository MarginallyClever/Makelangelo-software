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
    public void test() {
        assertColorsEqual(new ColorRGB(0xff123456),0x12,0x34,0x56);
    }
}
