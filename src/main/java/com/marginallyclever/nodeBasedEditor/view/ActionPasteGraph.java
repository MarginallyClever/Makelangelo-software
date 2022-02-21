package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.nodeBasedEditor.model.NodeGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionPasteGraph extends AbstractAction {
    private final NodeGraphEditorPanel editor;

    public ActionPasteGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NodeGraph modelC = editor.getCopiedGraph().deepCopy();
        editor.getGraph().add(modelC);
        editor.setSelectedNodes(modelC.getNodes());
    }
}
