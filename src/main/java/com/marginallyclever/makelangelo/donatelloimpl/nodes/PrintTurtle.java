package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import com.marginallyclever.nodegraphcore.port.Input;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Print the {@link Turtle}'s path behind the {@link Node}s.</p>
 * <p>On {@link #update()} pass over the {@link Turtle} once and build a list of polylines for faster rendering.
 * This is done using a {@link PolylineBuilder} which also optimizes to remove points on nearly straight lines.</p>
 */
public class PrintTurtle extends Node implements PrintWithGraphics {
    //private static final Logger logger = LoggerFactory.getLogger(PrintTurtle.class);
    private final Input<Turtle> turtle = new Input<>("turtle", Turtle.class,new Turtle());
    private final Input<Number> px = new Input<>("X",Number.class,0);
    private final Input<Number> py = new Input<>("Y",Number.class,0);
    private final Input<Boolean> showTravel = new Input<>("show travel",Boolean.class,false);
    private final Input<Color> travelColor = new Input<>("travel color",Color.class,Color.GREEN);
    private final Input<Number> lineThickness = new Input<>("line thickness",Number.class,1);
    private final List<Polyline> polylines = new ArrayList<>();

    /**
     * A poly line to draw.
     */
    static class Polyline {
        private final int[] x;
        private final int[] y;
        private final int n;
        private final Color color;

        public Polyline(int[] x, int[] y, int n, Color color) {
            if(x.length!=y.length) throw new IllegalArgumentException("x and y must be the same length");
            if(n<x.length) {
                // trim the buffers to the correct size.
                int[] newX = new int[n];
                int[] newY = new int[n];
                System.arraycopy(x,0,newX,0,n);
                System.arraycopy(y,0,newY,0,n);
                x = newX;
                y = newY;
            }
            this.x = x;
            this.y = y;
            this.n = n;
            this.color = color;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.drawPolyline(x,y,n);
        }
    }

    /**
     * A builder for creating a {@link Polyline}.
     * Optimizes to remove points on nearly straight lines.
     */
    static class PolylineBuilder {
        private int[] x;
        private int[] y;
        private int n;

        public PolylineBuilder() {
            x = new int[100];
            y = new int[100];
            n = 0;
        }

        public void add(int x,int y) {
            if(n>=this.x.length) {
                // grow the buffer if needed.
                int[] newX = new int[this.x.length*2];
                int[] newY = new int[this.y.length*2];
                System.arraycopy(this.x,0,newX,0,n);
                System.arraycopy(this.y,0,newY,0,n);
                this.x = newX;
                this.y = newY;
            }
            this.x[n] = x;
            this.y[n] = y;
            n++;
        }

        /**
         * Build a {@link Polyline} with the given color.  This is the same as calling {@link #compile(Color, int)}
         * with a deviation of 10 degrees.
         * @param color the color of the line.
         * @return a {@link Polyline} with the given color.
         */
        public Polyline compile(Color color) {
            return compile(color,10);
        }

        /**
         * Build a {@link Polyline} with the given color.  Remove any points that form a nearly straight line.
         * @param color the color of the line.
         * @param deviationDegrees the maximum deviation in degrees between two lines to be considered a straight line.
         * @return a {@link Polyline} with the given color.
         */
        public Polyline compile(Color color, int deviationDegrees) {
            if(n<3) {
                return new Polyline(x.clone(), y.clone(), n, color);
            }

            // examine the buffers and remove any points that form a nearly straight line.
            // use a dot product to determine if the angle between the two lines is less than `maxDeviation` degrees.
            var nx = new int[n];
            var ny = new int[n];
            int j=0;
            nx[j] = x[0];
            ny[j] = y[0];
            j++;

            var maxDeviation = Math.toRadians(deviationDegrees);
            var x0 = x[0];
            var y0 = y[0];
            var x1 = x[1];
            var y1 = y[1];
            for(int i=2;i<n;i++) {
                // compare line 0-1 with line 1-2
                var x2 = x[i];
                var y2 = y[i];
                var dx1 = x1-x0;
                var dy1 = y1-y0;
                var dx2 = x2-x1;
                var dy2 = y2-y1;
                var dot = dx1*dx2 + dy1*dy2;
                var len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
                var len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
                var len = len1*len2;
                var angle = len==0? 0 : Math.acos(dot/len);  // no divide by zero
                // if the angle is less than the deviation, skip point 1.
                if(angle>maxDeviation) {
                    // otherwise save point 1
                    nx[j] = x1;
                    ny[j] = y1;
                    j++;
                    x0 = x1;
                    y0 = y1;
                }
                // move on to the next point.
                x1 = x2;
                y1 = y2;
            }
            nx[j] = x1;
            ny[j] = y1;
            j++;
            //if(j<n) System.out.println("saved "+(n-j)+" points");
            return new Polyline(nx,ny,j,color);
        }

        public void clear() {
            n = 0;
        }
    }

    public PrintTurtle() {
        super("PrintTurtle");
        addVariable(turtle);
        addVariable(px);
        addVariable(py);
        addVariable(showTravel);
        addVariable(travelColor);
        addVariable(lineThickness);
    }

    @Override
    public void update() {
        polylines.clear();
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        generatePolylines(myTurtle);
    }

    @Override
    public void print(Graphics g) {
        if(getComplete()<100) return;
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dx=px.getValue().intValue();
        int dy=py.getValue().intValue();
        g2.translate(dx,dy);
        var lineThickness = this.lineThickness.getValue().floatValue();
        g2.setStroke(new BasicStroke(lineThickness));
        polylines.forEach(p -> p.draw(g2));
    }

    private void generatePolylines(Turtle myTurtle) {
        int size = myTurtle.history.size();
        int count = 0;

        setComplete(0);

        // where we're at in the drawing (to check if we're between first & last)
        boolean showPenUp = showTravel.getValue();
        TurtleMove previousMove = null;

        Color upColor = travelColor.getValue();
        Color downColor = new Color(0,0,0);
        PolylineBuilder builder = new PolylineBuilder();
        builder.add(0,0);
        try {
            for (TurtleMove m : myTurtle.history) {
                if(m==null) throw new NullPointerException();
                if(m.type == MovementType.TOOL_CHANGE) {
                    downColor = m.getColor();
                    continue;
                }
                if ( previousMove != null) {
                    if( previousMove.type != m.type ) {
                        polylines.add(builder.compile(previousMove.type == MovementType.TRAVEL ? upColor : downColor));
                        builder.clear();
                        builder.add((int) previousMove.x, (int) previousMove.y);
                    }
                    if ((m.type == MovementType.TRAVEL && showPenUp) || m.type == MovementType.DRAW_LINE) {
                        builder.add((int) m.x, (int) m.y);
                    }
                }
                previousMove = m;
                setComplete((int) (100.0 * count++ / size));
            }
            if(builder.n>0 && previousMove!=null) {
                polylines.add(builder.compile(previousMove.type == MovementType.TRAVEL ? upColor : downColor));
            }
        }
        catch(Exception ignored) {}
        setComplete(100);
    }
}
