package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.nodeBasedEditor.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

/**
 * {@link NodeVariable}
 */
public class NodeVariable<T> {
    public static final int IN=1;
    public static final int OUT=2;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    public static final Color DEFAULT_CONNECTION_POINT_COLOR_SELECTED = Color.RED;
    public static final Color DEFAULT_CONNECTION_POINT_COLOR = Color.LIGHT_GRAY;

    private T value;
    private T defaultValue;
    private final Class<T> type;

    private String name;
    private boolean hasInput;
    private boolean hasOutput;

    private boolean isDirty;
    private final Rectangle rectangle = new Rectangle();

    private NodeVariable(String _name,Class<T> type,T defaultValue,boolean _hasInput,boolean _hasOutput) {
        super();
        this.type = type;
        this.name = _name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.hasInput = _hasInput;
        this.hasOutput = _hasOutput;
        rectangle.setBounds(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    public static <T> NodeVariable<T> newInstance(String _name,Class<T> clazz,T defaultValue,boolean _hasInput,boolean _hasOutput) {
        return new NodeVariable<T>(_name,clazz,defaultValue,_hasInput,_hasOutput);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object arg0) {
        if(isValidType(arg0)) {
            value = (T)arg0;
            isDirty = true;
        }
    }

    public String getTypeClass() {
        return type.getSimpleName();
    }

    public boolean isValidType(Object arg0) {
        return type.isInstance(arg0);
    }

    public T getValue() {
        return value;
    }

    public void setIsDirty(boolean state) {
        isDirty=state;
    }

    public boolean getIsDirty() {
        return isDirty;
    }

    @Override
    public String toString() {
        return "NodeVariable{" +
                "name='" + name + '\'' +
                ", isDirty=" + isDirty +
                ", hasInput=" + hasInput +
                ", hasOutput=" + hasOutput +
                ", value=" + value +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("value",value);
        jo.put("defaultValue",defaultValue);
        jo.put("name",name);
        jo.put("hasInput",hasInput);
        jo.put("hasOutput",hasOutput);
        jo.put("rectangle", JSONHelper.rectangleToJSON(rectangle));
        jo.put("isDirty",isDirty);
        return jo;
    }

    @SuppressWarnings("unchecked")
    public void parseJSON(JSONObject jo) throws JSONException, ClassCastException {
        value = (jo.has("value") ? (T)jo.get("value") : null);
        defaultValue = (jo.has("defaultValue") ? (T)jo.get("defaultValue") : null);
        name = jo.getString("name");
        hasInput = jo.getBoolean("hasInput");
        hasOutput = jo.getBoolean("hasOutput");
        rectangle.setBounds(JSONHelper.rectangleFromJSON(jo.getJSONObject("rectangle")));
        isDirty = jo.getBoolean("isDirty");
    }

    public boolean getHasOutput() {
        return hasOutput;
    }

    public boolean getHasInput() {
        return hasInput;
    }

    public Point getInPosition() {
        return new Point((int)rectangle.getMinX(), rectangle.y+rectangle.height/2);
    }

    public Point getOutPosition() {
        return new Point((int)rectangle.getMaxX(), rectangle.y+rectangle.height/2);
    }

}
