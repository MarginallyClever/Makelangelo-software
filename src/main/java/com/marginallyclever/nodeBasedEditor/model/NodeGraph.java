package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.*;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadImage;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link NodeGraph} contains the {@link Node}s, and {@link NodeConnection}s
 */
public class NodeGraph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<NodeConnection> connections = new ArrayList<>();

    public NodeGraph() {
        super();
        NodeFactory.registerNode(new LoadNumber());
        NodeFactory.registerNode(new Random());
        NodeFactory.registerNode(new Add());
        NodeFactory.registerNode(new Subtract());
        NodeFactory.registerNode(new Multiply());
        NodeFactory.registerNode(new Divide());
        NodeFactory.registerNode(new PrintToStdOut());
        NodeFactory.registerNode(new Cos());
        NodeFactory.registerNode(new Sin());
        NodeFactory.registerNode(new Tan());
        NodeFactory.registerNode(new ATan2());

        NodeFactory.registerNode(new LoadImage());
        NodeFactory.registerNode(new PrintImage());

        NodeFactory.registerNode(new LoadString());
    }

    public void update() {
        for(Node n : nodes) n.update();
        for(NodeConnection c : connections) c.apply();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<NodeConnection> getConnections() {
        return connections;
    }

    public Node add(Node n) {
        nodes.add(n);
        return n;
    }

    /**
     * Remove a {@link Node} and all associated {@link NodeConnection}s from the model.
     * @param n the subject to be removed.
     */
    public void remove(Node n) {
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
    public NodeConnection add(NodeConnection connection) {
        connections.add(connection);
        return connection;
    }

    public void remove(NodeConnection c) {
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
     * Return the first connection point found within radius of a point
     * @param point center of search area
     * @param r radius limit
     * @return a {@link NodeConnectionPointInfo} describing the point found or null.
     */
    public NodeConnectionPointInfo getFirstNearbyConnection(Point point, double r) {
        double rr=r*r;
        for(Node n : nodes) {
            for(int i = 0; i < n.getNumVariables(); ++i) {
                NodeVariable<?> v = n.getVariable(i);
                if(v.getHasInput() && v.getInPosition().distanceSq(point) < rr) {
                    return new NodeConnectionPointInfo(n,i,NodeVariable.IN);
                }
                if(v.getHasOutput() && v.getOutPosition().distanceSq(point) < rr) {
                    return new NodeConnectionPointInfo(n,i,NodeVariable.OUT);
                }
            }
        }
        return null;
    }

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("nodes",getAllNodesAsJSON());
        jo.put("nodeConnections",getAllNodeConnectionsAsJSON());
        return jo;
    }

    public void parseJSON(JSONObject jo) throws JSONException {
        clear();
        parseAllNodesFromJSON(jo.getJSONArray("nodes"));
        parseAllNodeConnectionsFromJSON(jo.getJSONArray("nodeConnections"));
        bumpUpIndexableID();
    }

    private void parseAllNodesFromJSON(JSONArray arr) throws JSONException {
        for (Object element : arr) {
            JSONObject o = (JSONObject)element;
            Node n = NodeFactory.createNode(o.getString("name"));
            n.parseJSON(o);
            add(n);
        }
    }

    private void parseAllNodeConnectionsFromJSON(JSONArray arr) throws JSONException {
        for (Object o : arr) {
            NodeConnection c = new NodeConnection();
            parseOneConnectionFromJSON(c, (JSONObject)o);
            add(c);
        }
    }

    /**
     * {@link NodeConnection} must be parsed in the {@link NodeGraph} because only here can we access the list of
     * nodes to find the one with a matching {@code getUniqueName()}.
     * @param c the connection to parse into.
     * @param jo the JSON to parse.
     */
    private void parseOneConnectionFromJSON(NodeConnection c, JSONObject jo) {
        if(jo.has("inNode")) {
            Node n = findNodeWithUniqueName(jo.getString("inNode"));
            int i = jo.getInt("inVariableIndex");
            c.setInput(n,i);
        }
        if(jo.has("outNode")) {
            Node n = findNodeWithUniqueName(jo.getString("outNode"));
            int i = jo.getInt("outVariableIndex");
            c.setOutput(n,i);
        }
    }

    private Node findNodeWithUniqueName(String name) {
        for(Node n : nodes) {
            if(n.getUniqueName().equals(name)) return n;
        }
        return null;
    }

    /**
     * Every {@link Node} and {@link NodeConnection} has a unique ID.  If the model has just been restored from a file
     * then the static unique ID will be wrong.  This method bumps the first available unique ID up to the largest value
     * found.  Then the next attempt to create a unique item will be safe.
     */
    private void bumpUpIndexableID() {
        int id=0;
        for(Node n : nodes) {
            id = id > n.getUniqueID() ? id : n.getUniqueID();
        }
        Node.setUniqueIDSource(id);
    }

    private JSONArray getAllNodesAsJSON() {
        JSONArray a = new JSONArray();
        for (Node n : nodes) {
            a.put(n.toJSON());
        }
        return a;
    }

    private JSONArray getAllNodeConnectionsAsJSON() {
        JSONArray a = new JSONArray();
        for (NodeConnection c : connections) {
            a.put(c.toJSON());
        }
        return a;
    }

    /**
     * Add all {@link Node}s and {@link NodeConnection}s from one model to this model.
     * @param b the model to add.
     */
    public void add(NodeGraph b) {
        assignNewUniqueIDs(0);
        b.assignNewUniqueIDs(Node.getUniqueIDSource());

        nodes.addAll(b.nodes);
        connections.addAll(b.connections);

        bumpUpIndexableID();
    }

    private void assignNewUniqueIDs(int startingIndex) {
        for(Node n : nodes) n.setUniqueID(++startingIndex);
    }

    /**
     * Returns a deep copy of this {@link NodeGraph} by using the JSON serialization methods.
     * <blockquote><pre>newInstance.parseJSON(this.toJSON());</pre></blockquote>
     * @return a deep copy of this {@link NodeGraph}
     */
    public NodeGraph deepCopy() {
        NodeGraph copy = new NodeGraph();
        copy.parseJSON(toJSON());
        return copy;
    }

    /**
     * Return the index within the list of nodes of the first occurrence of class c.
     * Comparisons are done using {@code isInstance()} and may return subclasses.
     * @param c the class to match.
     * @return the index within the list of nodes of the first occurrence of class c, or -1.
     */
    public int indexOfNode(Class<Add> c) {
        return indexOfNode(c,0);
    }

    /**
     * Return the index within the list of nodes of the first occurrence of class c, starting the search at the
     * specified index.
     * Comparisons are done using {@code isInstance()} and may return subclasses.
     * @param c the class to match.
     * @param fromIndex the index to start the search from.
     * @return the index within the list of nodes of the first occurrence of class c, or -1.
     */
    public int indexOfNode(Class<Add> c, int fromIndex) {
        for(int i=fromIndex;i<nodes.size();++i) {
            if(c.isInstance(nodes.get(i))) return i;
        }
        return -1;
    }

    /**
     * Return the number of instances of class c or its subclasses.
     * Comparisons are done using {@code isInstance()} and may count subclasses.
     * @param c the class to match
     * @return the number of instances of class c or its subclasses.
     */
    public int countNodesOfClass(Class<?> c) {
        int i=0;
        for(Node n : nodes) {
            if(c.isInstance(n)) i++;
        }
        return i;
    }

    public Collection<? extends Node> getNodesInRectangle(Rectangle2D selectionArea) {
        if(selectionArea==null) throw new InvalidParameterException("selectionArea cannot be null.");
        ArrayList<Node> found = new ArrayList<>();
        for(Node n : nodes) {
            Rectangle r = n.getRectangle();
            if(selectionArea.intersects(r)) found.add(n);
        }
        return found;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Returns all {@link NodeConnection}s that are only connected between the given set of nodes.
     * @param selectedNodes the set of nodes to check
     * @return all {@link NodeConnection}s that are only connected between the given set of nodes.
     */
    public List<NodeConnection> getConnectionsBetweenTheseNodes(List<Node> selectedNodes) {
        List<NodeConnection> list = new ArrayList<>();
        if(selectedNodes.size()<2) return list;

        for(NodeConnection c : connections) {
            if(selectedNodes.contains(c.getOutNode()) &&
                selectedNodes.contains(c.getInNode())) {
                list.add(c);
            }
        }
        return list;
    }
}
