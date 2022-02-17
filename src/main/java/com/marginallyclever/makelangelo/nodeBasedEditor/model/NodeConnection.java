package com.marginallyclever.makelangelo.nodeBasedEditor.model;

import javax.vecmath.Vector2d;
import java.awt.*;

public class NodeConnection extends Indexable {
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

    public void paintComponent(Graphics g) {

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
                "uniqueName=" + getUniqueName() +
                ", inNode=" + inNode.getUniqueName() +
                ", inVariableIndex=" + inVariableIndex +
                ", outNode=" + outNode.getUniqueName() +
                ", outVariableIndex=" + outVariableIndex +
                '}';
    }

    public Vector2d getInPosition() {
        Vector2d p = new Vector2d(0,0);
        if(isInputValid()) {
            p.y = getPointHeight(inVariableIndex);
        }
        Rectangle r = inNode.getRectangle();
        p.x = r.x+r.width;
        return p;
    }

    public Vector2d getOutPosition() {
        Vector2d p = new Vector2d(0,0);
        if(isInputValid()) {
            p.y = getPointHeight(outVariableIndex);
        }
        Rectangle r = outNode.getRectangle();
        p.x = r.x;
        return p;
    }

    private double getPointHeight(int index) {
        float y = 0;
        Rectangle inr = inNode.getRectangle();
        for(int i=0;i<index;++i) {
            y += inNode.getVariable(i).getRectangle().height;
        }
        y += inNode.getVariable(inVariableIndex).getRectangle().height/2;
        return y;
    }
}
