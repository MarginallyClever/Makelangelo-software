package com.marginallyclever.nodeBasedEditor.view.actions;

import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.view.NodeGraphEditorPanel;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.*;

public class ActionLoadGraph extends AbstractAction {
    private final NodeGraphEditorPanel editor;
    private final JFileChooser fc = new JFileChooser();

    public ActionLoadGraph(String name, NodeGraphEditorPanel editor) {
        super(name);
        this.editor = editor;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        fc.setFileFilter(NodeGraphEditorPanel.FILE_FILTER);
        if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(editor)) == JFileChooser.APPROVE_OPTION) {
            editor.getGraph().add(loadModelFromFile(fc.getSelectedFile().getAbsolutePath()));
        }
    }

    private NodeGraph loadModelFromFile(String absolutePath) {
        NodeGraph newModel = new NodeGraph();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            JSONObject modelAsJSON = new JSONObject(responseStrBuilder.toString());
            newModel.parseJSON(modelAsJSON);
        } catch(IOException e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(editor),e.getLocalizedMessage());
            e.printStackTrace();
        }
        return newModel;
    }
}
