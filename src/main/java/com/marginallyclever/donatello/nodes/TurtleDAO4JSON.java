package com.marginallyClever.donatello.nodes;

import com.marginallyClever.nodeGraphCore.JSON_DAO;
import com.marginallyClever.makelangelo.turtle.Turtle;
import org.json.JSONException;
import org.json.JSONObject;

public class TurtleDAO4JSON implements JSON_DAO<Turtle> {
    @Override
    public Object toJSON(Object object) throws JSONException {
        JSONObject json = new JSONObject();
        Turtle turtle = (Turtle)object;
        // for a complete snapshot, capture all the instance details, too.
        return json;
    }

    @Override
    public Turtle fromJSON(Object object) throws JSONException {
        JSONObject json = (JSONObject)object;
        Turtle turtle = new Turtle();
        // for a complete snapshot, restore all the instance details, too.
        return turtle;
    }
}
