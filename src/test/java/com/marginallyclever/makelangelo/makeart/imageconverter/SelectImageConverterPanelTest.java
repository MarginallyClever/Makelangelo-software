package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.util.PreferencesHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.FileInputStream;

public class SelectImageConverterPanelTest {
    public static void main(String[] args) throws Exception {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        TransformedImage image = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/test.png")));
        JFrame frame = new JFrame(SelectImageConverterPanel.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SelectImageConverterPanel(new Paper(), new PlotterSettings(), image));
        frame.pack();
        frame.setVisible(true);
    }
}
