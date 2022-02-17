package com.marginallyclever.makelangelo.nodeBasedEditor.model;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Node} is a collection of zero or more inputs and zero or more outputs connected by some operator.
 * The operator is defined by extending the {@link Node} class and defining the {@code update()} method.
 */
public abstract class Node extends Indexable {
    private List<NodeVariable<?>> variables = new ArrayList<>();
    private final Rectangle rectangle = new Rectangle();

    public Node(String str) {
        super(str);
        rectangle.setBounds(0,0,150,50);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * This method should be overriden to provide the custom behavior of this node.
     */
    public abstract void update();

    public void updateRectangle() {
        int w=0;
        int h=0;
        for(NodeVariable v : variables) {
            Rectangle r = v.getRectangle();
            if(w<r.width) w = r.width;
            h+=r.height;
        }
        rectangle.width=w;
        rectangle.height=h;
    }
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

    public void addVariable(NodeVariable v) {
        variables.add(v);
    }

    public int getNumVariables() {
        return variables.size();
    }

    public NodeVariable<?> getVariable(int index) {
        return variables.get(index);
    }

    @Override
    public String toString() {
        return "Node{" +
                "uniqueName=" + getUniqueName() +", "+
                "variables=" + variables +
                '}';
    }
}
