package com.marginallyclever.nodeBasedEditor.view.makelangelo;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadNumber;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.PrintToStdOut;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Add;
import com.marginallyclever.nodeBasedEditor.view.swing.NodeGraphEditorPanel;
import com.marginallyclever.nodeBasedEditor.view.swing.SwingNodeFactory;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.LoadImage;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.PrintImage;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.turtle.LoadTurtle;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;

public class MakelangeloNodeEditor {
    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        NodeFactory.registerBuiltInNodes();
        SwingNodeFactory.registerNodes();
        MakelangeloNodeFactory.registerNodes();
        NodeGraph model = new NodeGraph();

        setupAnInitialModel(model);

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);

        JFrame frame = new JFrame("Makelangelo Node Graph");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1200,800));
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Fills a model with some starting nodes and sundry for something to work with in a hurry.
     * @param model the model to fill.
     */
    private static void setupAnInitialModel(NodeGraph model) {
        Node constant0 = model.add(new LoadNumber(1));
        Node constant1 = model.add(new LoadNumber(2));
        Node add = model.add(new Add());
        Node report = model.add(new PrintToStdOut());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.add(new NodeConnection(add,2,report,0));
        constant1.getRectangle().y=50;
        add.getRectangle().x=200;
        report.getRectangle().x=400;

        Node loadImage = model.add(new LoadImage("test.png"));
        Node printImage = model.add(new PrintImage());
        model.add(new NodeConnection(loadImage,1,printImage,0));
        loadImage.getRectangle().setLocation(0,150);
        printImage.getRectangle().setLocation(200,150);

        Node loadTurtle = model.add(new LoadTurtle("./src/test/resources/stanfordBunny.svg"));
        loadTurtle.getRectangle().setLocation(0,300);
    }
}
