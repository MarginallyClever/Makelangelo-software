package com.marginallyclever.makelangelo.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;

import java.awt.*;

public class NodeConnection extends Indexable {
    public static final double DEFAULT_RADIUS = 5;
    public static final Color DEFAULT_COLOR = Color.BLUE;

    private Node inNode;
    private int inVariableIndex=-1;
    private Node outNode;
    private int outVariableIndex=-1;

    public NodeConnection() {
        super("Connection");
    }
    /**
     * send the value of upstream variables to downstream variables
     */
    public void apply() throws Exception {
        if(!isInputValid() || !isOutputValid()) return;

        NodeVariable<?> in = getInputVariable();
        NodeVariable<?> out = getOutputVariable();
        if( out.isValidType(in.getValue()) ) {
            out.setValue(in.getValue());
        } else {
            throw new Exception("types do not match "+out.getTypeClass() + " vs " +in.getTypeClass());
        }
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

    public void setInput(Node n, int i) throws Exception {
        inNode = n;
        inVariableIndex = i;
        apply();
    }

    public void setOutput(Node n, int i) throws Exception {
        outNode = n;
        outVariableIndex = i;
        apply();
    }

    @Override
    public String toString() {
        return "NodeConnection{" +
                "uniqueName=" + getUniqueName() +
                ", inNode=" + inNode.getUniqueName() +
                ", inVariableIndex=" + inVariableIndex +
                ", outNode=" + outNode.getUniqueName() +
                ", outVariableIndex=" + outVariableIndex +
                '}';
    }

    // the in position of this {@link NodeConnection} is the out position of a {@link NodeVariable}
    public Point2D getInPosition() {
        return inNode.getOutPosition(inVariableIndex);
    }

    /**
     * The out position of this {@link NodeConnection} is the in position of a {@link NodeVariable}
     */
    public Point2D getOutPosition() {
        return outNode.getInPosition(outVariableIndex);
    }

    public boolean isConnectedTo(Node n) {
        return inNode==n || outNode==n;
    }
}
