package com.marginallyclever.nodeBasedEditor.view;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionUpdate extends AbstractAction {
    NodeGraphEditorPanel editor;

    public ActionUpdate(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.update();
    }
}
