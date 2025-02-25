package com.marginallyclever.makelangelo.donatelloimpl;

import com.marginallyclever.donatello.EditNodePanel;
import com.marginallyclever.nodegraphcore.Graph;
import com.marginallyclever.nodegraphcore.Node;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DockableEditNodePanel extends JPanel {
    private final JPanel container = new JPanel(new BorderLayout());

    public DockableEditNodePanel() {
        super(new BorderLayout());
        add(new JScrollPane(container), BorderLayout.CENTER);
    }

    public void setNodeAndGraph(List<Node> selectedNodes, Graph graph) {
        container.removeAll();
        if(selectedNodes.isEmpty()) {
            return;
        }
        container.add(new EditNodePanel(selectedNodes.getFirst(), graph), BorderLayout.NORTH);
    }
}
