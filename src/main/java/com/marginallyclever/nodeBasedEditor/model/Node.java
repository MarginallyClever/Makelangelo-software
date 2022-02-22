package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.nodeBasedEditor.JSONHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Node} is a collection of zero or more inputs and zero or more outputs connected by some operator.
 * The operator is defined by extending the {@link Node} class and defining the {@code update()} method.
 */
public abstract class Node {
    public static final int TITLE_HEIGHT = 25;

    private static int uniqueIDSource=0;
    private int uniqueID;

    private String name;
    private String label;
    private final List<NodeVariable<?>> variables = new ArrayList<>();
    private final Rectangle rectangle = new Rectangle();

    public Node(String name) {
        super();
        uniqueID = ++uniqueIDSource;
        this.name = name;
        rectangle.setBounds(0,0,150,50);
    }

    /**
     * Return one new instance of this type of {@link Node}.
     * Override this method in derived classes.
     * @return One new instance of this type of {@link Node}.
     */
    public abstract Node create();

    public static void setUniqueIDSource(int index) {
        uniqueIDSource=index;
    }

    public static int getUniqueIDSource() {
        return uniqueIDSource;
    }

    public void setUniqueID(int i) {
        uniqueID=i;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueID+"-"+name;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Override this method to provide the custom behavior of this node.
     */
    public abstract void update();

    public void updateBounds() {
        int w=(int)rectangle.getWidth();
        int h=Node.TITLE_HEIGHT;
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
     * Makes all input variables not dirty.
     */
    protected void cleanAllInputs() {
        for(NodeVariable<?> v : variables) {
            if(v.getHasInput()) v.setIsDirty(false);
        }
    }

    public void cleanAllOutputs() {
        for(NodeVariable<?> v : variables) {
            if(v.getHasOutput()) v.setIsDirty(false);
        }
    }

    public void addVariable(NodeVariable v) {
        variables.add(v);
    }

    public void removeVariable(NodeVariable v) {
        variables.remove(v);
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
                "name=" + getName() +
                ", uniqueID=" + getUniqueID() +
                ", label=" + label +
                ", variables=" + variables +
                ", rectangle=" + rectangle +
                '}';
    }

    public Point getInPosition(int index) {
        Rectangle r = getRectangle();
        Point p = new Point(r.x,r.y+(int)getPointHeight(index));
        return p;
    }

    public Point getOutPosition(int index) {
        Rectangle r = getRectangle();
        Point p = new Point(r.x+r.width,r.y+(int)getPointHeight(index));
        return p;
    }

    private double getPointHeight(int index) {
        float y = TITLE_HEIGHT;
        Rectangle inr = getRectangle();
        for(int i=0;i<index;++i) {
            y += getVariable(i).getRectangle().height;
        }
        y += getVariable(index).getRectangle().height/2;
        return y;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("name",name);
        jo.put("uniqueID",uniqueID);
        jo.put("label", label);
        jo.put("rectangle", JSONHelper.rectangleToJSON(rectangle));
        jo.put("variables", getAllVariablesAsJSON());
        return jo;
    }

    private JSONArray getAllVariablesAsJSON() {
        JSONArray vars = new JSONArray();
        for(NodeVariable<?> v : variables) {
            vars.put(v.toJSON());
        }
        return vars;
    }

    public void parseJSON(JSONObject jo) throws JSONException {
        String joName = jo.getString("name");
        if(!name.equals(joName)) throw new JSONException("Node types do not match: "+name+", "+joName);

        uniqueID = jo.getInt("uniqueID");
        if(jo.has("label")) {
            String s = jo.getString("label");
            if(!s.equals("null")) label = s;
        }
        rectangle.setBounds(JSONHelper.rectangleFromJSON(jo.getJSONObject("rectangle")));
        parseAllVariablesFromJSON(jo.getJSONArray("variables"));
    }

    private void parseAllVariablesFromJSON(JSONArray vars) throws JSONException {
        guaranteeSameNumberOfVariables(vars);
        for(int i=0;i<vars.length();++i) {
            variables.get(i).parseJSON(vars.getJSONObject(i));
        }
    }

    private void guaranteeSameNumberOfVariables(JSONArray vars) throws JSONException {
        if(vars.length() != variables.size()) {
            int a = variables.size();
            int b = vars.length();
            throw new JSONException("JSON bad number of node variables.  Expected "+a+" found "+b);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String str) {
        label=str;
    }

    /**
     * Sets the top left corner of the {@link Node}'s rectangle.
     * @param point the new position of the top left corner.
     */
    public void setPosition(Point point) {
        rectangle.x=point.x;
        rectangle.y=point.y;
    }

    public void moveRelative(int dx, int dy) {
        rectangle.x += dx;
        rectangle.y += dy;
    }
}
