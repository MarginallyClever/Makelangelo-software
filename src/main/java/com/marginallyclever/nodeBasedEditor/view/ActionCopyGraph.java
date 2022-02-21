package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ActionCopyGraph extends AbstractAction {
    private final NodeGraphEditorPanel editor;

    public ActionCopyGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeGraph g = editor.getGraph();

        NodeGraph modelB = new NodeGraph();
        List<Node> selectedNodes = editor.getSelectedNodes();
        for(Node n : selectedNodes) modelB.add(n);
        List<NodeConnection> selectedConnections = g.getConnectionsBetweenTheseNodes(selectedNodes);
        for(NodeConnection c : selectedConnections) modelB.add(c);
        editor.setCopiedGraph(modelB.deepCopy());
    }
}
