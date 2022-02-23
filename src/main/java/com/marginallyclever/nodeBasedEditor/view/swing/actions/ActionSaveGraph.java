package com.marginallyclever.nodeBasedEditor.view.swing.actions;

import com.marginallyclever.nodeBasedEditor.view.swing.NodeGraphEditorPanel;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ActionSaveGraph extends AbstractAction {
    private final NodeGraphEditorPanel editor;
    private final JFileChooser fc = new JFileChooser();

    public ActionSaveGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fc.setFileFilter(NodeGraphEditorPanel.FILE_FILTER);
        if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(editor)) == JFileChooser.APPROVE_OPTION) {
            String name = addExtensionIfNeeded(fc.getSelectedFile().getAbsolutePath());
            saveModelToFile(name);
        }
    }

    private String addExtensionIfNeeded(String s) {
        if(FilenameUtils.getExtension(s).isEmpty()) {
            String[] extensions = NodeGraphEditorPanel.FILE_FILTER.getExtensions();
            s += "."+extensions[0];
        }
        return s;
    }

    private void saveModelToFile(String absolutePath) {
        try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
            w.write(editor.getGraph().toJSON().toString());
        } catch(Exception e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(editor),e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
