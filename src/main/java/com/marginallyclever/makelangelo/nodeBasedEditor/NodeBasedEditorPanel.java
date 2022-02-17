package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * {@link NodeBasedEditorPanel} is the View to a {@link NodeBasedEditorModel}.
 */
public class NodeBasedEditorPanel extends JPanel {
    private final NodeBasedEditorModel model = new NodeBasedEditorModel();
    private final JPanel paintArea = new JPanel();
    private final JToolBar bar = new JToolBar();

    public NodeBasedEditorPanel() {
        super(new BorderLayout());

        this.add(bar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for(Node n : model.getNodes()) n.render(g);
        for(NodeConnection c : model.getConnections()) c.render(g);
    }

    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        NodeBasedEditorPanel panel = new NodeBasedEditorPanel();
        JFrame frame = new JFrame("NodeBasedEditorPanel");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
