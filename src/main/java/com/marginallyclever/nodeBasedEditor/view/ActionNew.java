package com.marginallyclever.nodeBasedEditor.view;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionNew extends AbstractAction {
    NodeGraphEditorPanel editor;

    public ActionNew(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.clear();
    }
}
