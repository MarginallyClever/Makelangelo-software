package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class Filter_BlurTest {
    @Test
    public void test() throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
        Filter_GaussianBlur a = new Filter_GaussianBlur(1);
        Filter_GaussianBlur b = new Filter_GaussianBlur(2);
        TransformedImage a2 = a.filter(src);
        TransformedImage b2 = b.filter(src);
        //Filter_Difference()
    }

}
