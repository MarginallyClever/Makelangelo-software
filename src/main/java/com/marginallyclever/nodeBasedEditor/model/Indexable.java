package com.marginallyclever.nodeBasedEditor.model;

/**
 * A base class that ensures all nodes and connections have unique ids for easier debugging and reflection.
 */
public abstract class Indexable {
    private static int uniqueIDSource=0;
    private int uniqueID;
    private String name;

    public Indexable(String _name) {
        super();
        uniqueID = ++uniqueIDSource;
        this.name = _name;
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

    @Override
    public String toString() {
        return "Indexable{" +
                "uniqueID=" + uniqueID +", "+
                "name='" + name + '\'' +
                '}';
    }
}
