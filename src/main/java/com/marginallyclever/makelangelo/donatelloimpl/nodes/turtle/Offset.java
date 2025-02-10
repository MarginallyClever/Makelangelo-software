package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.Node;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.*;
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
        Color lastColor = Color.BLACK;
        var list = new ArrayList<Coordinate>();
        double offsetValue = offset.getValue();
        Turtle result = new Turtle();
        TurtleMove previousMove = null;

        setComplete(0);
        int size = input.history.size();
        int count = 0;
        try {
            for (TurtleMove move : input.history) {
                if (move == null) throw new NullPointerException();
                if (move.type == MovementType.TOOL_CHANGE) {
                    lastColor = move.getColor();
                    count++;
                    continue;
                }

                if (previousMove != null) {
                    if (previousMove.type != move.type) {
                        // pen has lifted, polyline ends
                        result.add(offsetList(list, offsetValue, lastColor));

                        if (previousMove.type == MovementType.TRAVEL)
                            list.add(new Coordinate(previousMove.x, previousMove.y));
                    }
                    if (move.type == MovementType.DRAW_LINE) {
                        list.add(new Coordinate(move.x, move.y));
                    }
                }
                previousMove = move;
                setComplete((int) (100.0 * count++ / size));
            }

            // in case there's a line on the go.
            result.add(offsetList(list, offsetValue, lastColor));
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
            turtle.setColor(lastColor);
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
