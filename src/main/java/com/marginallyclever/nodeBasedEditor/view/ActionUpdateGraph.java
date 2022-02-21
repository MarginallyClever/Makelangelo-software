package com.marginallyclever.nodeBasedEditor.view;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionUpdateGraph extends AbstractAction {
    NodeGraphEditorPanel editor;

    public ActionUpdateGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.update();
    }
}
