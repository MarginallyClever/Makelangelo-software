package com.marginallyclever.makelangelo.nodeBasedEditor;

import java.awt.*;

public class NodeConnection {
    private static int uniqueIDSource=0;
    private int uniqueID;

    private Node inNode;
    private int inVariableIndex=-1;
    private Node outNode;
    private int outVariableIndex=-1;

    public NodeConnection() {
        super();
        uniqueID = ++uniqueIDSource;
    }
    /**
     * send the value of upstream variables to downstream variables
     */
    public void apply() throws Exception {
        if(!isInputValid() || !isOutputValid()) return;

        NodeVariable in = getInputVariable();
        NodeVariable out = getOutputVariable();
        //if( out.getTypeClass().isInstance(in.getValue()) ) {
            out.setValue(in.getValue());
        //} else {
            //throw new Exception("types do not match "+out.getTypeClass() + " vs " +in.getTypeClass());
        //}
    }

    private NodeVariable getOutputVariable() {
        return outNode.getVariable(outVariableIndex);
    }

    private NodeVariable getInputVariable() {
        return inNode.getVariable(inVariableIndex);
    }

    public boolean isInputValid() {
        if(inNode==null) return false;
        if(inVariableIndex==-1) return false;
        if(inNode.getNumVariables() <= inVariableIndex) return false;
        if(inNode.getVariable(inVariableIndex).getHasOutput()==false) return false;
        return true;
    }

    public boolean isOutputValid() {
        if(outNode==null) return false;
        if(outVariableIndex==-1) return false;
        if(outNode.getNumVariables() <= outVariableIndex) return false;
        if(outNode.getVariable(outVariableIndex).getHasInput()==false) return false;
        return true;
    }

    public void render(Graphics g) {

    }

    public void setInput(Node n, int i) {
        inNode = n;
        inVariableIndex = i;
    }

    public void setOutput(Node n, int i) {
        outNode = n;
        outVariableIndex = i;
    }

    @Override
    public String toString() {
        return "NodeConnection{" +
                "uniqueID=" + uniqueID +
                ", inNode="+inNode.getUniqueName() +
                ", inVariableIndex=" + inVariableIndex +
                ", outNode=" + outNode +
                ", outVariableIndex=" + outVariableIndex +
                '}';
    }
}
