package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterTresholdFancyTest {
    @Test
    public void test() throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/chicken.png")) );
        for(int thresholdValue=1;thresholdValue<255;++thresholdValue) {
            FilterThresholdFancy threshold = new FilterThresholdFancy(src, thresholdValue, 50.0);
            for (int i = 0; i < 255; ++i) {
                if (i >= thresholdValue) Assertions.assertEquals(255, threshold.modify(i));
                else Assertions.assertTrue(threshold.modify(i) < 255);
            }
        }
    }
}
