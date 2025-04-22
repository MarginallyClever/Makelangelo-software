package com.marginallyclever.makelangelo.makeart.turtletool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThickenLinesByIntensityTest {
    @Test
    public void testAdjustedOffset() {
        var byIntensity = new ThickenLinesByIntensity();
        Assertions.assertEquals(-5,byIntensity.adjustedOffset(10, 0.0));
        Assertions.assertEquals( 0,byIntensity.adjustedOffset(10, 0.5));
        Assertions.assertEquals( 5,byIntensity.adjustedOffset(10, 1.0));
    }
}
