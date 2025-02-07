package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.AbstractDAO4JSON;
import org.json.JSONException;
import org.json.JSONObject;

public class TurtleDAO4JSON extends AbstractDAO4JSON<Turtle> {
    public TurtleDAO4JSON() {
        super(Turtle.class);
    }

    @Override
    public Object toJSON(Object object) throws JSONException {
        JSONObject json = new JSONObject();
        Turtle turtle = (Turtle)object;
        // for a complete snapshot, capture all the instance details, too.
        // TODO save turtle to JSON.

        return json;
    }

    @Override
    public Turtle fromJSON(Object object) throws JSONException {
        JSONObject json = (JSONObject)object;
        // for a complete snapshot, restore all the instance details, too.
        Turtle turtle = new Turtle();
        // TODO load turtle from JSON
        return turtle;
    }
}
