package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.pen.Palette;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;

public class LoadFilePanelTest {
    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        JFrame frame = new JFrame(LoadFilePanel.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new LoadFilePanel(new Paper(),new Palette(),""));
        frame.setPreferredSize(new Dimension(800,600));
        frame.pack();
        frame.setVisible(true);
    }
}
