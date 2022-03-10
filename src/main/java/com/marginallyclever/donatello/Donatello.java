package com.marginallyclever.donatello;

import com.marginallyClever.nodeGraphCore.BuiltInRegistry;
import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeConnection;
import com.marginallyClever.nodeGraphCore.NodeGraph;
import com.marginallyClever.nodeGraphCore.builtInNodes.LoadNumber;
import com.marginallyClever.nodeGraphCore.builtInNodes.PrintToStdOut;
import com.marginallyClever.nodeGraphCore.builtInNodes.math.Add;
import com.marginallyClever.nodeGraphSwing.NodeGraphEditorPanel;
import com.marginallyClever.nodeGraphSwing.SwingRegistry;
import com.marginallyClever.nodeGraphSwing.nodes.images.LoadImage;
import com.marginallyClever.nodeGraphSwing.nodes.images.PrintImage;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.donatello.nodes.LoadTurtle;
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

        setupAnInitialModel(model);

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

        Node loadImage = model.add(new LoadImage("src/test/resources/test.png"));
        Node printImage = model.add(new PrintImage());
        model.add(new NodeConnection(loadImage,1,printImage,0));
        loadImage.getRectangle().setLocation(0,150);
        printImage.getRectangle().setLocation(200,150);

        Node loadTurtle = model.add(new LoadTurtle("./src/test/resources/stanfordBunny.svg"));
        loadTurtle.getRectangle().setLocation(0,300);
    }
}
