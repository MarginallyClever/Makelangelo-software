package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.makelangelo.donatelloimpl.nodes.points.PointsDAO4JSON;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.nodegraphcore.AbstractDAO4JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Point2d;
import java.util.stream.IntStream;

/**
 * Data Access Object for {@link ListOfLines}
 */
public class LinesDAO4JSON extends AbstractDAO4JSON<ListOfLines> {
    public LinesDAO4JSON() {
        super(ListOfLines.class);
    }

    @Override
    public Object toJSON(Object object) throws JSONException {
        JSONArray array = new JSONArray();
        ListOfLines lines = (ListOfLines)object;
        for(var l : lines.getAllLines()) {
            PointsDAO4JSON pointsDAO = new PointsDAO4JSON();
            pointsDAO.toJSON(l);
            JSONArray points = new JSONArray();
            for(var p : l.getAllPoints()) {
                JSONObject point = new JSONObject();
                point.put("x", p.getX());
                point.put("y", p.getY());
                points.put(point);
            }
            array.put(points);
        }

        return array;
    }

    @Override
    public ListOfLines fromJSON(Object object) throws JSONException {
        ListOfLines list = new ListOfLines();
        JSONArray array = (JSONArray)object;
        IntStream.range(0, array.length()).forEach(j ->{
            Line2d line = new Line2d();
            JSONArray points = array.getJSONArray(j);
            IntStream.range(0, points.length()).forEach(i -> {
                JSONObject point = points.getJSONObject(i);
                line.add(new Point2d(
                        point.getDouble("x"),
                        point.getDouble("y")));
            });
            list.add(line);
        });
        return list;
    }
}
