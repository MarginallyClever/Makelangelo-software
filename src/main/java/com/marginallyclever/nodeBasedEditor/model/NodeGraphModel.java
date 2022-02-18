package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.*;

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
        NodeFactory.registerNode(new Add());
        NodeFactory.registerNode(new Subtract());
        NodeFactory.registerNode(new Multiply());
        NodeFactory.registerNode(new Divide());
        NodeFactory.registerNode(new ReportToStdOut());
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

    /**
     * Adds a {@link NodeConnection} without checking if it already exists.
     * @param connection the item to add.
     */
    public NodeConnection addConnection(NodeConnection connection) {
        connections.add(connection);
        return connection;
    }

    public void removeConnection(NodeConnection c) {
        connections.remove(c);
    }

    /**
     *
     * @param connection the item to compare against.
     * @return returns the existing match or null.
     */
    public NodeConnection getMatchingConnection(NodeConnection connection) {
        for(NodeConnection c : connections) {
            if(c.equals(connection)) return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return "NodeBasedEditorModel{" +
                "nodes=" + nodes +
                ", connections=" + connections +
                '}';
    }

    /**
     * Empty the model.
     */
    public void clear() {
        nodes.clear();
        connections.clear();
    }

    /**
     *
     * @param point center of search area
     * @param r radius limit
     * @param flags {@code NodeVariable.IN} or {@code NodeVariable.OUT} for the types you want.
     * @return the a {@link NodeConnectionPointInfo} or null.
     */
    public NodeConnectionPointInfo getFirstNearbyConnection(Point2D point, double r, int flags) {
        double rr=r*r;
        boolean doIn = (flags & NodeVariable.IN) == NodeVariable.IN;
        boolean doOut = (flags & NodeVariable.OUT) == NodeVariable.OUT;
        if(doIn || doOut) {
            for(Node n : nodes) {
                for(int i = 0; i < n.getNumVariables(); ++i) {
                    NodeVariable<?> v = n.getVariable(i);
                    if(doIn && v.getInPosition().distanceSquared(point) < rr) {
                        return new NodeConnectionPointInfo(n,i,NodeVariable.IN);
                    }
                    if(doOut && v.getOutPosition().distanceSquared(point) < rr) {
                        return new NodeConnectionPointInfo(n,i,NodeVariable.OUT);
                    }
                }
            }
        }
        return null;
    }
}
