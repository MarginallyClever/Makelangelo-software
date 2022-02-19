package com.marginallyclever.nodeBasedEditor.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

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

    public static void setUniqueIDSource(int index) {
        uniqueIDSource=index;
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

    public JSONObject toJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("uniqueID",uniqueID);
        jo.put("name",name);
        return jo;
    }

    public void parseJSON(JSONObject jo) throws JSONException {
        uniqueID = jo.getInt("uniqueID");
        name = jo.getString("name");
    }
}
