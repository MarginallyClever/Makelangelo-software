package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.ColorRGB;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Node} is a collection of zero or more inputs and zero or more outputs connected by some operator.
 * The operator is defined by extending the {@link Node} class and defining the {@code update()} method.
 */
public abstract class Node {
    private static int uniqueIDSource=0;
    private int uniqueID;
    private String name;
    private List<NodeVariable<?>> variables = new ArrayList<>();

    public Node(String _name) {
        super();
        uniqueID = ++uniqueIDSource;
        this.name = _name;
    }

    /**
     * This method should be overriden to provide the custom behavior of this node.
     */
    public abstract void update();

    /**
     * Check if any variables are dirty.
     * @return true if any variables are dirty.
     */
    public boolean isDirty() {
        for(NodeVariable<?> v : variables) {
            if (v.getIsDirty()) return true;
        }
        return false;
    }

    /**
     * Set all variables to clean (dirty=false).
     */
    protected void alwaysBeCleaning() {
        for(NodeVariable<?> v : variables) {
            v.setIsDirty(false);
        }
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void addVariable(NodeVariable v) {
        variables.add(v);
    }

    public int getNumVariables() {
        return variables.size();
    }

    public NodeVariable<?> getVariable(int index) {
        return variables.get(index);
    }

    public void render(Graphics g) {
    }

    @Override
    public String toString() {
        return "Node{" +
                "uniqueID=" + uniqueID +
                ", name='" + name + '\'' +
                ", variables=" + variables +
                '}';
    }

    public String getUniqueName() {
        return uniqueID+"-"+name;
    }
}
