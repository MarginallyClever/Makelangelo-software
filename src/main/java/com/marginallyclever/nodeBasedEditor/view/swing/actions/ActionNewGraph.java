package com.marginallyclever.nodeBasedEditor.view.swing.actions;

import com.marginallyclever.nodeBasedEditor.view.swing.NodeGraphEditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionNewGraph extends AbstractAction {
    NodeGraphEditorPanel editor;

    public ActionNewGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.clear();
    }
}
