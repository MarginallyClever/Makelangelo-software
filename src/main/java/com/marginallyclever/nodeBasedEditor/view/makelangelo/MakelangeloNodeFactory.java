package com.marginallyclever.nodeBasedEditor.view.makelangelo;

import com.marginallyclever.nodeBasedEditor.view.makelangelo.nodes.*;
import com.marginallyClever.nodeGraphCore.NodeFactory;

/**
 * Create custom {@link com.marginallyClever.nodeGraphCore.Node}s for Makleangelo.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class MakelangeloNodeFactory {
    public static void registerNodes() {
        NodeFactory.registerNode(new LoadTurtle());
        NodeFactory.registerNode(new PrintTurtle());
        NodeFactory.registerNode(new TurtleLine());
        NodeFactory.registerNode(new TurtleRectangle());
        NodeFactory.registerNode(new TurtleCircle());
        NodeFactory.registerNode(new TurtlePatternOnPath());
    }
}
