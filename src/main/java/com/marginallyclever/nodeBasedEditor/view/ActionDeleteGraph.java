package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionDeleteGraph extends AbstractAction {
    private final NodeGraphEditorPanel editor;

    public ActionDeleteGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeGraph g = editor.getGraph();
        for(Node n : editor.getSelectedNodes()) g.remove(n);
        editor.setSelectedNodes(null);
    }
}
