package com.marginallyclever.nodeBasedEditor.view.actions;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.view.NodeGraphEditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionForciblyUpdateNodes extends AbstractAction {
    private final NodeGraphEditorPanel editor;

    public ActionForciblyUpdateNodes(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for(Node n : editor.getSelectedNodes()) {
            n.update();
        }
    }
}
