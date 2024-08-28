package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.InputStream;

/**
 * Loads a Factorio map exported from <a href='https://github.com/FUE5BASE/FUE5-Exporter'>FUE5 Exporter</a>
 */
public class LoadFactorioMap implements TurtleLoader {
    private static final Logger logger = LoggerFactory.getLogger(LoadFactorioMap.class);

    static final Color BELT = Color.ORANGE;
    static final Color PIPE = Color.BLUE;
    static final Color RAIL = Color.BLACK;
    static final Color TREE = Color.GREEN;

    @Override
    public FileNameExtensionFilter getFileNameFilter() {
        return new FileNameExtensionFilter("FUE5 export","json");
    }

    @Override
    public boolean canLoad(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.'));
        return (ext.equalsIgnoreCase(".json"));
    }

    /**
     * {
     * "name": "transport-belt",
     * "x": 1586.5,
     * "y": 364,
     * "direction": 0,
     * "width": 1,
     * "height": 1,
     * "variant": "I"
     * }
     * @param inputStream source of image
     * @return a new Turtle
     * @throws Exception
     */
    @Override
    public Turtle load(InputStream inputStream) throws Exception {
        if (inputStream == null) throw new NullPointerException("Input stream is null");

        Turtle turtle = new Turtle();

        logger.debug("Loading...");
        // parse json
        JSONObject tree = new JSONObject(new JSONTokener(inputStream));

        // create turtle
        var list = tree.getJSONArray("entities");
        list.forEach(entity -> {
            JSONObject obj = (JSONObject) entity;
            String name = obj.getString("name");
            int w = (int)Math.ceil(obj.getDouble("width"));
            int h = (int)Math.ceil(obj.getDouble("height"));
            if(w==0||h==0) return;
            double x = obj.getDouble("x");
            double y = obj.getDouble("y");
            int dir = obj.getInt("direction");

            if(name.endsWith("pipe")) {
                turtle.setColor(PIPE);
                drawPipe(turtle, x, y, w, h, dir,obj.getString("variant"));
            } else if(name.endsWith("pipe-to-ground")) {
                turtle.setColor(PIPE);
                drawPipeToGround(turtle, x, y, w, h, dir);
            } else if(name.endsWith("pump")) {
                turtle.setColor(PIPE);
                drawPump(turtle, x, y, w, h, dir);
            } else if(name.endsWith("transport-belt")) {
                turtle.setColor(BELT);
                drawBelt(turtle, x, y, w, h, dir, obj.getString("variant"));
            } else if(name.endsWith("underground-belt")) {
                turtle.setColor(BELT);
                drawUndergroundBelt(turtle, x, y, w, h, dir);
            } else if(name.endsWith("straight-rail")) {
                turtle.setColor(RAIL);
                drawStraightRail(turtle, x, y, w, h, dir, obj.getString("variant"));
            } else if(name.endsWith("curved-rail")) {
                turtle.setColor(RAIL);
                drawCurvedRail(turtle, x, y, w, h, dir, obj.getString("variant"));
            } else if(name.endsWith("tree")) {
                turtle.setColor(Color.GREEN);
                drawRectangleWithRotation(turtle, x, y, w, h, dir);
            } else if(name.endsWith("splitter")) {
                drawSplitter(turtle, x, y, w, h, dir);
            } else {
                turtle.setColor(Color.RED);
                drawRectangleWithRotation(turtle, x, y, w, h, dir);
            }
        });

        turtle.scale(1,-1);
        return turtle;
    }

    private void drawSplitter(Turtle turtle, double x, double y, int w, int h, int dir) {
        turtle.setColor(BELT);

        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90 + dir * 45);
        turtle.forward(-w*1/4d);
        turtle.strafe(-h*4/4d);
        turtle.penDown();
        turtle.forward(h);
        turtle.strafe(w);
        turtle.forward(-h);
        turtle.strafe(-w);
    }

    private void drawPipeToGround(Turtle turtle, double x, double y, int w, int h, int dir) {
        drawShortLine(turtle, x, y, w, h, dir);

        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.forward(w/4d);
        turtle.penDown();
        turtle.strafe(-h/4d);
        turtle.forward(-w/2d);
        turtle.strafe(h/2d);
        turtle.forward(w/2d);
        turtle.strafe(-h/4d);
    }

    public void drawPump(Turtle turtle, double x, double y, int w, int h, int dir) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90 + dir * 45);
        turtle.forward(-h*1/2d);
        turtle.strafe(-w*1/2d);
        turtle.penDown();
        turtle.forward(h);
        turtle.strafe(w);
        turtle.forward(-h);
        turtle.strafe(-w);
    }


    private void drawUndergroundBelt(Turtle turtle, double x, double y, int w, int h, int dir) {
        drawShortLine(turtle, x, y, w, h, dir);

        double w2 =w/4d;
        double h2 = h/4d;
        var hypotenuse = Math.sqrt(w2*w2+h2*h2);
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.penDown();
        turtle.strafe(-h2);
        turtle.turn(45);
        turtle.strafe(hypotenuse);
        turtle.turn(-90);
        turtle.strafe(hypotenuse);
        turtle.turn(45);
        turtle.strafe(-h2);
    }

    private void drawShortLine(Turtle turtle, double x, double y, int w, int h, int dir) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.penDown();
        turtle.forward(-w/2d);
    }

    private void drawPipe(Turtle turtle, double x, double y, int w, int h, int dir,String variant) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.forward(-w/2d);
        turtle.penDown();
        switch(variant) {
            case "L": drawRightTurn(turtle, w); break;
            case "R": drawLeftTurn(turtle, w); break;
            case "T": drawTJunction(turtle, w); break;
            case "X": drawXJunction(turtle, w); break;
            default: turtle.forward(w); break;  // case I
        }
    }

    private void drawBelt(Turtle turtle, double x, double y, int w, int h, int dir,String variant) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.forward(-w/2d);
        turtle.penDown();
        switch(variant) {
            case "R": drawRightTurn(turtle, w); break;
            case "L": drawLeftTurn(turtle, w); break;
            default: turtle.forward(w); break;  // case I
        }
    }

    private void drawXJunction(Turtle turtle, int w) {
        turtle.forward(w);
        turtle.penUp();
        turtle.forward(-w/2d);
        turtle.strafe(-w/2d);
        turtle.penDown();
        turtle.strafe(w);
    }

    private void drawTJunction(Turtle turtle, int w) {
        turtle.penUp();
        turtle.forward(w);
        turtle.penDown();
        turtle.forward(-w/2d);
        turtle.strafe(w/2d);
        turtle.penUp();
        turtle.strafe(-w/2d);
        turtle.penDown();
        turtle.strafe(-w/2d);
    }

    private void drawLeftTurn(Turtle turtle, int w) {
        turtle.forward(w/2d);
        turtle.turn(90);
        turtle.forward(w/2d);
        turtle.turn(-90);
    }

    private void drawRightTurn(Turtle turtle, int w) {
        turtle.forward(w/2d);
        turtle.turn(-90);
        turtle.forward(w/2d);
        turtle.turn(90);
    }

    private void drawStraightRail(Turtle turtle, double x, double y, int w, int h, int dir, String variant) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        if(variant.endsWith("/")) turtle.turn(-45);
        turtle.forward(-w/2d);
        turtle.strafe(-h/4d);
        turtle.penDown();
        turtle.forward(w);
        turtle.penUp();
        turtle.strafe(h/2d);
        turtle.penDown();
        turtle.forward(-w);
    }

    /**
     * Draw the two rails, separated by 1 unit, through 1/8 of a circle.
     * @param turtle the turtle
     * @param x center of track piece
     * @param y center of track piece
     * @param w width of track piece
     * @param h height of track piece
     * @param dir 0...8
     * @param variant R or L
     */
    private void drawCurvedRail(Turtle turtle, double x, double y, int w, int h, int dir, String variant) {
        double insideTrackRadius = 8.25;
        double outsideTrackRadius = insideTrackRadius+1;
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90+dir * 45);
        turtle.forward(w/2d+2);
        Vector2d start = turtle.getPosition();
        double angle = turtle.getAngle();

        if(variant.equals("R")) {
            turtle.strafe(-h/4d);
            turtle.penDown();
            drawArc(turtle, false, insideTrackRadius);
            turtle.penUp();
            turtle.moveTo(start.x,start.y);
            turtle.setAngle(angle);
            turtle.strafe(-h*3/4d);
            turtle.penDown();
            drawArc(turtle, false, outsideTrackRadius);
        } else {
            // must be L
            turtle.strafe(h/4d);
            turtle.penDown();
            drawArc(turtle,true, insideTrackRadius);
            turtle.penUp();
            turtle.moveTo(start.x,start.y);
            turtle.setAngle(angle);
            turtle.strafe(h*3/4d);
            turtle.penDown();
            drawArc(turtle, true, outsideTrackRadius);
        }
    }

    /**
     * Draw an arc from the current position and angle.
     * @param turtle the turtle
     * @param rightHandTurn true for right hand turn, false for left hand turn
     * @param radius the turn radius
     */
    private void drawArc(Turtle turtle, boolean rightHandTurn, double radius) {
        double circumference = 2 * Math.PI * radius;
        double distance = circumference/8;
        double steps=9;
        double stepSize = distance/steps;

        for(int i=0;i<=steps;++i) {
            turtle.forward(-stepSize);
            turtle.turn(45.0/steps * (rightHandTurn ? -1 : 1));
        }
    }

    /**
     *
     * @param turtle
     * @param x
     * @param y
     * @param w
     * @param h
     * @param dir 0...8
     */
    private void drawRectangleWithRotation(Turtle turtle, double x, double y, int w, int h, int dir) {
        turtle.penUp();
        turtle.moveTo(x, y);
        turtle.setAngle(90 + dir * 45);
        turtle.forward(-w/2d);
        turtle.strafe(-h/2d);
        turtle.penDown();
        turtle.forward(h);
        turtle.strafe(w);
        turtle.forward(-h);
        turtle.strafe(-w);
    }
}
