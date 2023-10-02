package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterTresholdTest {
    @Test
    public void test() throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/chicken.png")) );
        FilterThreshold threshold = new FilterThreshold(src, 8);
        for(int i=0;i<255;++i) {
            Assertions.assertEquals(i<8?0:255,threshold.modify(i));
        }
    }
}
