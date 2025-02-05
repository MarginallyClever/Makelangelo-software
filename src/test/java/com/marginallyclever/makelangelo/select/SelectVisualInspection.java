package com.marginallyclever.makelangelo.select;

import com.marginallyclever.donatello.select.*;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Run this to visually examine every panel element and how they look in next to each other.
 * @author Dan Royer
 */
public class SelectVisualInspection {
    public static void main(String[] args) {
        PreferencesHelper.start();
        Translator.start();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("failed to set native look and feel.");
        }

        JFrame frame = new JFrame("Select Look and feel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(buildPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel buildPanel() {
        SelectPanel panel = new SelectPanel();
        SelectBoolean a = new SelectBoolean("A","AAAAAAAAAAA",false);
        SelectButton b = new SelectButton("B","B");
        SelectColor c = new SelectColor("C","CCCCCC",Color.BLACK,panel);
        SelectFile d = new SelectFile("D","D",null,panel);
        SelectDouble e = new SelectDouble("E","E",0.0f);
        SelectInteger f = new SelectInteger("F","FFF",0);
        String [] list = {"cars","trains","planes","boats","rockets"};
        SelectOneOfMany g = new SelectOneOfMany("G","G",list,0);
        String ipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. <a href=\"http://google.com\">Google</a> Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        SelectReadOnlyText h = new SelectReadOnlyText("H","H "+ipsum);
        SelectSlider i = new SelectSlider("I","I",200,0,100);
        SelectTextArea j = new SelectTextArea("J","J",ipsum);
        SelectSpinner k = new SelectSpinner("K", "K", 1, 10, 3);
        SelectTextField m = new SelectTextField("M", "M", "M");
        SelectRandomSeed n = new SelectRandomSeed("N", "N", 0);

        panel.add(a);
        panel.add(b);
        panel.add(c);
        panel.add(d);
        panel.add(e);
        panel.add(f);
        panel.add(g);
        panel.add(h);
        panel.add(i);
        panel.add(j);
        panel.add(k);
        panel.add(m);
        panel.add(n);

        // test finish
        panel.setPreferredSize(new Dimension(400,600));

        panel.addPropertyChangeListener((evt)-> {
            System.out.println("Event: "+evt.toString());
        });
        return panel;
    }
}
