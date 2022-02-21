package com.marginallyclever.nodeBasedEditor.view;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionCutGraph extends AbstractAction {
    private final ActionDeleteGraph actionDeleteGraph;
    private final ActionCopyGraph actionCopyGraph;

    public ActionCutGraph(String name, ActionDeleteGraph actionDeleteGraph, ActionCopyGraph actionCopyGraph) {
        super(name);
        this.actionDeleteGraph = actionDeleteGraph;
        this.actionCopyGraph = actionCopyGraph;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionCopyGraph.actionPerformed(e);
        actionDeleteGraph.actionPerformed(e);
    }
}
