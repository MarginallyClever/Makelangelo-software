package com.marginallyclever.makelangelo.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphModel} contains the {@link Node}s, and {@link NodeConnection}s
 */
public class NodeGraphModel {
    private final List<Node> nodes = new ArrayList<>();
    private final List<NodeConnection> connections = new ArrayList<>();

    public NodeGraphModel() {
        super();
    }

    public void update() throws Exception {
        for(Node n : nodes) n.update();
        for(NodeConnection c : connections) c.apply();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<NodeConnection> getConnections() {
        return connections;
    }

    public Node addNode(Node n) {
        nodes.add(n);
        return n;
    }

    /**
     * Remove a {@link Node} and all associated {@link NodeConnection}s from the model.
     * @param n the subject to be removed.
     */
    public void removeNode(Node n) {
        nodes.remove(n);
        removeConnectionsToNode(n);
    }

    /**
     * Remove all {@link NodeConnection}s from the model associated with a given {@link Node}
     * @param n the subject from which all connections should be removed.
     */
    public void removeConnectionsToNode(Node n) {
        ArrayList<NodeConnection> toKeep = new ArrayList<>();
        for(NodeConnection c : connections) {
            if(!c.isConnectedTo(n)) toKeep.add(c);
        }
        connections.clear();
        connections.addAll(toKeep);
    }

    public NodeConnection createNodeConnection() {
        NodeConnection c = new NodeConnection();
        connections.add(c);
        return c;
    }

    @Override
    public String toString() {
        return "NodeBasedEditorModel{" +
                "nodes=" + nodes +
                ", connections=" + connections +
                '}';
    }

    /**
     * Remove all nodes and connections
     */
    public void clear() {
        nodes.clear();
        connections.clear();
    }

    public Point2D getNearestConnection(Point point, double r) {
        double rr=r*r;
        Point2D p = new Point2D(point.x,point.y);

        for(Node n : nodes) {
            for(int i=0;i<n.getNumVariables();++i) {
                NodeVariable<?> v = n.getVariable(i);
                if(v.getInPosition().distanceSquared(p) < rr) return v.getInPosition();
                if(v.getOutPosition().distanceSquared(p) < rr) return v.getOutPosition();
            }
        }
        return null;
    }
}
