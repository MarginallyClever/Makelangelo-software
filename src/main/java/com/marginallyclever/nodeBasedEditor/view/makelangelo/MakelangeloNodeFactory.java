package com.marginallyclever.nodeBasedEditor.view.makelangelo;

import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.turtle.*;

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
