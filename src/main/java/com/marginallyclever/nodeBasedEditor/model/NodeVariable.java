package com.marginallyclever.nodeBasedEditor.model;

import com.marginallyclever.convenience.Point2D;

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
    private final T defaultValue;
    private final Class<T> type;

    private final String name;
    private final boolean hasInput;
    private final boolean hasOutput;

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

    public boolean getHasOutput() {
        return hasOutput;
    }

    public boolean getHasInput() {
        return hasInput;
    }

    public Point2D getInPosition() {
        return new Point2D(rectangle.getMinX(), rectangle.y+rectangle.height/2);
    }

    public Point2D getOutPosition() {
        return new Point2D(rectangle.getMaxX(), rectangle.y+rectangle.height/2);
    }
}
