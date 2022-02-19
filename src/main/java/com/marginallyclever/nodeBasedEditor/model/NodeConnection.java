package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Objects;

public class NodeConnection {
    public static final double DEFAULT_RADIUS = 5;
    public static final Color DEFAULT_COLOR = Color.BLUE;

    private Node inNode;
    private int inVariableIndex=-1;
    private Node outNode;
    private int outVariableIndex=-1;

    public NodeConnection() {
        super();
    }

    public NodeConnection(Node inNode,int inVariableIndex,Node outNode,int outVariableIndex) {
        this();
        setInput(inNode,inVariableIndex);
        setOutput(outNode,outVariableIndex);
    }

    public NodeConnection(NodeConnection b) {
        this();
        set(b);
    }

    /**
     * Send the value of upstream variables to downstream variables
     */
    public void apply() {
        if(!isInputValid() || !isOutputValid()) return;
        if( isValidDataType() ) {
            NodeVariable<?> in = getInputVariable();
            NodeVariable<?> out = getOutputVariable();
            out.setValue(in.getValue());
        }
    }

    public boolean isValidDataType() {
        if(!isInputValid() || !isOutputValid()) return false;

        NodeVariable<?> in = getInputVariable();
        NodeVariable<?> out = getOutputVariable();
        return out.isValidType(in.getValue());
    }

    public Node getInNode() {
        return inNode;
    }

    public Node getOutNode() {
        return outNode;
    }

    private NodeVariable<?> getOutputVariable() {
        return outNode.getVariable(outVariableIndex);
    }

    private NodeVariable<?> getInputVariable() {
        return inNode.getVariable(inVariableIndex);
    }

    public boolean isInputValid() {
        if(inNode==null) return false;
        if(inVariableIndex==-1) return false;
        if(inNode.getNumVariables() <= inVariableIndex) return false;
        if(!inNode.getVariable(inVariableIndex).getHasOutput()) return false;
        return true;
    }

    public boolean isOutputValid() {
        if(outNode==null) return false;
        if(outVariableIndex==-1) return false;
        if(outNode.getNumVariables() <= outVariableIndex) return false;
        if(!outNode.getVariable(outVariableIndex).getHasInput()) return false;
        return true;
    }

    public void setInput(Node n, int i) {
        inNode = n;
        inVariableIndex = i;
        apply();
    }

    public void setOutput(Node n, int i) {
        outNode = n;
        outVariableIndex = i;
        apply();
    }

    @Override
    public String toString() {
        return "NodeConnection{" +
                "inNode=" + (inNode==null ? "null" : inNode.getUniqueName()) +
                ", inVariableIndex=" + inVariableIndex +
                ", outNode=" + (outNode==null ? "null" : outNode.getUniqueName()) +
                ", outVariableIndex=" + outVariableIndex +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        if(inNode!=null) {
            jo.put("inNode",inNode.getUniqueName());
            jo.put("inVariableIndex",inVariableIndex);
        }
        if(outNode!=null) {
            jo.put("outNode", outNode.getUniqueName());
            jo.put("outVariableIndex", outVariableIndex);
        }
        return jo;
    }

    // the in position of this {@link NodeConnection} is the out position of a {@link NodeVariable}
    public Point getInPosition() {
        return inNode.getOutPosition(inVariableIndex);
    }

    /**
     * The out position of this {@link NodeConnection} is the in position of a {@link NodeVariable}
     */
    public Point getOutPosition() {
        return outNode.getInPosition(outVariableIndex);
    }

    public boolean isConnectedTo(Node n) {
        return inNode==n || outNode==n;
    }

    public void disconnectAll() {
        setInput(null,0);
        setOutput(null,0);
    }

    public void set(NodeConnection b) {
        setInput(b.inNode,b.inVariableIndex);
        setOutput(b.outNode,b.outVariableIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeConnection that = (NodeConnection) o;
        return inVariableIndex == that.inVariableIndex &&
                outVariableIndex == that.outVariableIndex &&
                inNode.equals(that.inNode) &&
                outNode.equals(that.outNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inNode, inVariableIndex, outNode, outVariableIndex);
    }
}
