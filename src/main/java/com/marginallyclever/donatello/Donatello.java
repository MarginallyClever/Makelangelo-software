package com.marginallyclever.donatello;

import com.marginallyClever.nodeGraphCore.BuiltInRegistry;
import com.marginallyClever.nodeGraphCore.NodeGraph;
import com.marginallyClever.nodeGraphSwing.NodeGraphEditorPanel;
import com.marginallyClever.nodeGraphSwing.SwingRegistry;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Launch {@link NodeGraphEditorPanel} with {@link com.marginallyclever.makelangelo.turtle.Turtle} tools.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class Donatello {
    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        BuiltInRegistry.register();
        SwingRegistry.register();
        DonatelloRegistry.register();
        NodeGraph model = new NodeGraph();

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);

        JFrame frame = new JFrame("Donatello");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1200,800));
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        panel.setupMenuBar();
        frame.setVisible(true);
    }
}
