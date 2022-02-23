package com.marginallyclever.nodeBasedEditor.view.swing;

import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.LoadImage;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.PrintImage;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.turtle.*;

public class SwingNodeFactory {
    public static void registerSwingNodes() {
        NodeFactory.registerNode(new LoadImage());
        NodeFactory.registerNode(new PrintImage());

        NodeFactory.registerNode(new LoadTurtle());
        NodeFactory.registerNode(new PrintTurtle());
        NodeFactory.registerNode(new TurtleLine());
        NodeFactory.registerNode(new TurtleRectangle());
        NodeFactory.registerNode(new TurtleCircle());
        NodeFactory.registerNode(new TurtlePatternOnPath());
    }
}
