package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Node} is a collection of zero or more inputs and zero or more outputs connected by some operator.
 * The operator is defined by extending the {@link Node} class and defining the {@code update()} method.
 */
public abstract class Node extends Indexable {
    public static final int NODE_TITLE_HEIGHT = 25;
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
        int h=Node.NODE_TITLE_HEIGHT;
        int y=getRectangle().y;
        int x=getRectangle().x;
        for(NodeVariable v : variables) {
            Rectangle r = v.getRectangle();
            r.y=h+y;
            r.x=x;
            if(w < r.width) w = r.width;
            h += r.height;
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

    public Point2D getInPosition(int index) {
        Rectangle r = getRectangle();
        Point2D p = new Point2D(r.x,r.y+getPointHeight(index));
        return p;
    }

    public Point2D getOutPosition(int index) {
        Rectangle r = getRectangle();
        Point2D p = new Point2D(r.x+r.width,r.y+getPointHeight(index));
        return p;
    }

    private double getPointHeight(int index) {
        float y = NODE_TITLE_HEIGHT;
        Rectangle inr = getRectangle();
        for(int i=0;i<index;++i) {
            y += getVariable(i).getRectangle().height;
        }
        y += getVariable(index).getRectangle().height/2;
        return y;
    }
}
