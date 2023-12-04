package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.util.PreferencesHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterCMYKTest {
    //@Test
    public void testConversion() throws IOException {
        PreferencesHelper.start();
        final String PATH_NAME = "target/classes/bill-murray";
        final String EXT = "jpg";
        File file = new File(PATH_NAME + "." + EXT);
        assert (file.isFile());
        TransformedImage img = new TransformedImage(ImageIO.read(new FileInputStream(file)));
        FilterCMYK filter = new FilterCMYK(img);
        filter.filter();

        ImageIO.write(filter.getC().getSourceImage(), EXT, new File(PATH_NAME + "C." + EXT));
        ImageIO.write(filter.getM().getSourceImage(), EXT, new File(PATH_NAME + "M." + EXT));
        ImageIO.write(filter.getY().getSourceImage(), EXT, new File(PATH_NAME + "Y." + EXT));
        ImageIO.write(filter.getK().getSourceImage(), EXT, new File(PATH_NAME + "K." + EXT));
    }
}
