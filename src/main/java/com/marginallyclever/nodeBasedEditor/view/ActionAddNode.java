package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ActionAddNode extends AbstractAction {
    private NodeGraphEditorPanel editor;

    public ActionAddNode(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Node n = NodeFactoryPanel.runAsDialog((JFrame)SwingUtilities.getWindowAncestor(editor));
        if(n!=null) {
            n.setPosition(editor.getPopupPoint());
            editor.getGraph().add(n);
            editor.setSelectedNode(n);
            editor.repaint();
        }
    }
}
