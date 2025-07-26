package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.io.SaveSVG;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Converter_Mickymoe1992Test {
    @BeforeAll
    public static void beforeAll() {
        PreferencesHelper.start();
        Translator.start();
    }

    @Test
    public void testContourMapConversion() throws IOException {
        Paper paper = new Paper();
        PlotterSettings plotterSettings = new PlotterSettings();

        // load src/test/resources/mandrill.png as a TransformedImage
        TransformedImage src = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")));

        Converter_Mickymoe1992 converter = new Converter_Mickymoe1992();
        converter.setPlotterSettings(plotterSettings);
        converter.addImageConverterListener(new ImageConverterListener() {
            @Override
            public void onRestart(ImageConverter panel) {
                System.out.println("Restart requested: " + panel.getName());
            }

            @Override
            public void onConvertFinished(Turtle turtle) {
                System.out.println("Conversion finished: " + converter.getName());
                // save turtle as an SVG file
                try(FileOutputStream fileOutputStream = new FileOutputStream("contour_map.svg")) {
                    SaveSVG save = new SaveSVG();
                    save.save(fileOutputStream, turtle, PlotterSettingsManager.buildMakelangelo5());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        converter.start(paper,src);

    }
}
