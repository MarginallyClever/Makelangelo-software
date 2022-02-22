package com.marginallyclever.nodeBasedEditor.view.actions;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.model.Subgraph;
import com.marginallyclever.nodeBasedEditor.view.NodeGraphEditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionFoldGraph extends AbstractAction {
    private NodeGraphEditorPanel editor;
    private ActionCutGraph actionCutGraph;

    public ActionFoldGraph(String name, NodeGraphEditorPanel editor, ActionCutGraph actionCutGraph) {
        super(name);
        this.editor = editor;
        this.actionCutGraph = actionCutGraph;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeGraph preserveCopyBehaviour = editor.getCopiedGraph().deepCopy();

        actionCutGraph.actionPerformed(e);
        NodeGraph justCut = editor.getCopiedGraph().deepCopy();
        Node n = editor.getGraph().add(new Subgraph(justCut));
        n.setPosition(editor.getPopupPoint());

        editor.setCopiedGraph(preserveCopyBehaviour);
    }
}
