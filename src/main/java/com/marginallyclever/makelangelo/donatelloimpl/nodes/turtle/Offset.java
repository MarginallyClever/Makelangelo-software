package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;

import java.awt.*;
import java.util.ArrayList;

/**
 * Offset uses jts location services to draw a line offset from a Turtle by a given amount
 */
public class Offset extends Node {
    private final InputTurtle turtleA = new InputTurtle("A");
    private final InputDouble offset = new InputDouble("offset",5d);
    private final OutputTurtle output = new OutputTurtle("output");

    public Offset() {
        super("Offset");
        addPort(turtleA);
        addPort(offset);
        addPort(output);
    }

    @Override
    public void update() {
        var input = turtleA.getValue();
        var list = new ArrayList<Coordinate>();
        double offsetValue = offset.getValue();
        Turtle result = new Turtle();

        setComplete(0);
        int size = input.countPoints()+1;
        int count = 0;
        try {
            for (var cl : input.getLayers() ) {
                for (var line : cl.getAllLines() ) {
                    for( var p : line.getAllPoints() ) {
                        list.add(new Coordinate(p.x, p.y));
                        setComplete((int) (100.0 * count++ / size));
                    }
                    result.add(offsetList(list, offsetValue, cl.getColor()));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        setComplete(100);
        output.setValue(result);
    }

    private Turtle offsetList(ArrayList<Coordinate> list, double offsetValue,Color lastColor) {
        Turtle turtle = new Turtle();
        if(list.size()<2) return turtle;

        // get a jts geometry factory
        GeometryFactory geometryFactory = new GeometryFactory();
        // setup line endings
        var params = new BufferParameters();
        params.setEndCapStyle(BufferParameters.CAP_ROUND);

        // convert the data to the jts format
        Coordinate[] offsetLine = null;
        // compare first and last points to see if this is a closed loop.
        if(list.size()>2 && list.getFirst().equals2D(list.getLast())) {
            // closed loop, use a polygon
            var ring = geometryFactory.createLinearRing(list.toArray(new Coordinate[0]));
            var polygon = geometryFactory.createPolygon(ring);
            Geometry offsetGeom = BufferOp.bufferOp(polygon, offsetValue, params);
            // Extract the offset boundary
            if (offsetGeom instanceof Polygon) {
                LineString offsetBoundary = ((Polygon) offsetGeom).getExteriorRing();
                offsetLine = offsetBoundary.getCoordinates();
            }
        } else {
            var line = geometryFactory.createLineString(list.toArray(new Coordinate[0]));
            // Create an offset curve builder
            OffsetCurveBuilder builder = new OffsetCurveBuilder(new PrecisionModel(), params);
            // Generate the offset curve
            offsetLine = builder.getOffsetCurve(line.getCoordinates(), offsetValue);
        }

        if(offsetLine != null) {
            //var offsetLine = line.buffer(offsetValue).getBoundary();
            // turn them into a turtle
            turtle.setStroke(lastColor);
            turtle.penUp();
            for (var p : offsetLine) {
                turtle.moveTo(p.x, p.y);
                turtle.penDown();
            }
            turtle.penUp();
            // clear the list for the next run
            list.clear();
        }
        // finish
        return turtle;
    }
}
