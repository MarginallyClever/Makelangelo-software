package com.marginallyclever.makelangelo.config;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;

public class ConfigPanelTest {
    public static void main(String[] args) {
        PreferencesHelper.start();
        Translator.start();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("failed to set native look and feel.");
        }

        ConfigListTest test = new ConfigListTest();
        ConfigPanel panel = new ConfigPanel(test.buildTestList());
        JFrame frame = new JFrame(ConfigPanel.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
