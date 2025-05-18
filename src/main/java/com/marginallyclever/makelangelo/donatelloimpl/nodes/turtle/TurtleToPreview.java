package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.Donatello;
import com.marginallyclever.donatello.ports.InputBoolean;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.makelangelo.MainFrame;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.Generator_AnalogClock;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorFactory;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.GenerateClockHands;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
import com.marginallyclever.makelangelo.turtle.PolylineBuilder;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleToBufferedImageHelper;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderer;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Print the {@link Turtle}'s path behind the {@link Node}s.</p>
 * <p>On {@link #update()} pass over the {@link Turtle} once and build a list of polylines for faster rendering.
 * This is done using a {@link PolylineBuilder} which also optimizes to remove points on nearly straight lines.</p>
 */
public class TurtleToPreview extends Node implements PrintWithGraphics {
    private static final Logger logger = LoggerFactory.getLogger(TurtleToPreview.class);

    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputInt layer = new InputInt("layer",5);
    private final InputOneOfMany style = new InputOneOfMany("style");

    private PreviewPanel previewPanel;

    public TurtleToPreview() {
        super("TurtleToPreview");
        addPort(turtle);
        addPort(style);
        addPort(layer);

        style.setOptions(TurtleRenderFactory.getNames());
    }

    public static JFrame getActiveJFrame() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isVisible()) {
                return (JFrame) window;
            }
        }
        return null; // No visible JFrame found
    }

    @Override
    public void update() {
        setComplete(0);

        MainFrame frame = (MainFrame)SwingUtilities.getRoot(getActiveJFrame());
        previewPanel = frame.getPreviewPanel();

        setComplete(100);
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }

    @Override
    public void print(Graphics g) {

        Turtle turtle1 = this.turtle.getValue();

        if(previewPanel != null && turtle1 != null) {
            previewPanel.setTurtle(turtle1);

        } else {
            logger.debug("Invalid turtle or preview panel");
        }
    }

}
