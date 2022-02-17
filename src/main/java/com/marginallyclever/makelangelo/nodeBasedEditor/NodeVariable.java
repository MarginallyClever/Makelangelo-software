package com.marginallyclever.makelangelo.nodeBasedEditor;

import java.awt.*;

/**
 * {@link NodeVariable}
 */
public class NodeVariable<T> {
    private String name;
    private boolean isDirty;
    private boolean hasInput;
    private boolean hasOutput;
    private T value;

    public NodeVariable(String _name,T startingValue,boolean _hasInput,boolean _hasOutput) {
        super();
        this.name = _name;
        this.value = startingValue;
        this.hasInput = _hasInput;
        this.hasOutput = _hasOutput;
    }

    public void setValue(T arg0) {
        value=arg0;
        isDirty=true;
    }

    public T getValue() {
        return value;
    }

    public Class getTypeClass() {
        return value == null ? Object.class : value.getClass();
    }

    public void setIsDirty(boolean state) {
        isDirty=state;
    }

    public boolean getIsDirty() {
        return isDirty;
    }

    public void render(Graphics g) {
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
}
