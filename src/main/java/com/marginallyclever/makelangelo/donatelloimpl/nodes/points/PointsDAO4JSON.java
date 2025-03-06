package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.makelangelo.turtle.ConcreteListOfPoints;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.nodegraphcore.AbstractDAO4JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Point2d;
import java.util.stream.IntStream;

/**
 * DAO for ListOfPoints
 */
public class PointsDAO4JSON extends AbstractDAO4JSON<ListOfPoints> {
    public PointsDAO4JSON() {
        super(ListOfPoints.class);
    }

    @Override
    public Object toJSON(Object object) throws JSONException {
        JSONArray array = new JSONArray();
        ListOfPoints points = (ListOfPoints)object;
        // for a complete snapshot, capture all the instance details, too.
        for(var p : points.getAllPoints()) {
            JSONObject point = new JSONObject();
            point.put("x", p.getX());
            point.put("y", p.getY());
            array.put(point);
        }

        return array;
    }

    @Override
    public ListOfPoints fromJSON(Object object) throws JSONException {
        JSONArray array = (JSONArray)object;
        // for a complete snapshot, restore all the instance details, too.
        var list = new ConcreteListOfPoints();
        IntStream.range(0, array.length()).forEach(i ->{
            JSONObject point = array.getJSONObject(i);
            list.add(new Point2d(
                    point.getDouble("x"),
                    point.getDouble("y")
            ));
        });
        return list;
    }
}
