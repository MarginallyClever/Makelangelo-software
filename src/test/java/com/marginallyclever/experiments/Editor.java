package com.marginallyclever.experiments;

import ModernDocking.DockingRegion;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.ext.ui.DockingUI;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.makelangelo.DockingPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Uses Modern Docking to create three kinds of DockingPanels: a tree, an output, and many text editor panels.
 * each type of panel automatically group together when created.
 */
public class Editor extends JFrame {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(()->(new Editor()).setVisible(true));
    }

    public Editor() {
        super("Demo");

        // show the main window
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Docking.initialize(this);
        DockingUI.initialize();
        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // create root panel
        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        var bar = new JMenuBar();
        var menu = new JMenu("File");
        menu.add("New");
        bar.add(menu);
        add(bar, BorderLayout.NORTH);

        // create the tree panel
        DockingPanel tree = new DockingPanel("Tree","Tree");
        Docking.dock(tree, this, DockingRegion.WEST);
        tree.add(new JTree());

        DockingPanel [] list = new DockingPanel[5];
        // create the text editor panels
        for (int i = 0; i < list.length; ++i) {
            DockingPanel editor = new DockingPanel("File " + i,"File " + i);
            var textArea = new JTextArea();
            textArea.setText("This is the text editor panel " + i + ".\nIt is editable.\n");
            editor.add(new JScrollPane(textArea));
            list[i] = editor;
            if(i==0) Docking.dock(editor, this, DockingRegion.EAST,0.8);
            else Docking.dock(editor, list[0], DockingRegion.CENTER);
        }

        // create the output panel
        DockingPanel output = new DockingPanel("Output","Output");
        var outText = new JTextArea();
        outText.setEditable(false);
        outText.setLineWrap(false);
        outText.setText("This is the output panel.\nIt is not editable.\n");
        output.add(new JScrollPane(outText));
        Docking.dock(output, this, DockingRegion.SOUTH);
    }
}
