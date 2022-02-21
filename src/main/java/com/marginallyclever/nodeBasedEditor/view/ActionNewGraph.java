package com.marginallyclever.nodeBasedEditor.view;

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
