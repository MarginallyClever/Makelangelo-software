package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DAO4JSON;
import org.json.JSONException;
import org.json.JSONObject;

public class TurtleDAO4JSON implements DAO4JSON<Turtle> {
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
