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

    public static final int IN=1;
    public static final int OUT=2;

    /**
     *
     * @param point center of search area
     * @param r radius limit
     * @param flags NodeGraphModel.IN | NodeGraphModel.OUT for the types you want.
     * @return the a {@link NodeConnectionPointInfo} or null.
     */
    public NodeConnectionPointInfo getNearestConnection(Point2D point, double r,int flags) {
        double rr=r*r;
        boolean doIn = (flags & NodeGraphModel.IN) == NodeGraphModel.IN;
        boolean doOut = (flags & NodeGraphModel.OUT) == NodeGraphModel.OUT;
        if(doIn || doOut) {
            for(Node n : nodes) {
                for(int i = 0; i < n.getNumVariables(); ++i) {
                    NodeVariable<?> v = n.getVariable(i);
                    if(doIn && v.getInPosition().distanceSquared(point) < rr) {
                        return new NodeConnectionPointInfo(n,i,NodeGraphModel.IN);
                    }
                    if(doOut && v.getOutPosition().distanceSquared(point) < rr) {
                        return new NodeConnectionPointInfo(n,i,NodeGraphModel.OUT);
                    }
                }
            }
        }
        return null;
    }
}
