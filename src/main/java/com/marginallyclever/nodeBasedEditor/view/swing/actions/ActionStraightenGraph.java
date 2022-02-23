package com.marginallyclever.nodeBasedEditor.view.swing.actions;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.view.swing.NodeGraphEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ActionStraightenGraph extends AbstractAction {
    private final int SNAP_SIZE = 10;
    private final NodeGraphEditorPanel editor;

    public ActionStraightenGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeGraph g = editor.getGraph();

        for(Node n : g.getNodes()) {
            Rectangle r = n.getRectangle();
            r.x -= r.x % SNAP_SIZE;
            r.y -= r.y % SNAP_SIZE;
        }
        editor.repaint();
    }
}
