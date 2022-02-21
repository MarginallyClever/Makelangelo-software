package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.nodeBasedEditor.PrintWithGraphics;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraph;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphViewPanel} visualizes the contents of a {@link NodeGraph} with Java Swing.
 * It can call on {@link NodeGraphViewListener}s to add additional flavor.
 */
public class NodeGraphViewPanel extends JPanel {
    public static final Color NODE_COLOR_BACKGROUND = Color.WHITE;
    public static final Color NODE_COLOR_BORDER = Color.BLACK;
    public static final Color NODE_COLOR_INTERNAL_BORDER = Color.DARK_GRAY;
    public static final Color PANEL_COLOR_BACKGROUND = Color.LIGHT_GRAY;
    public static final Color NODE_COLOR_FONT_CLEAN = Color.BLACK;
    public static final Color NODE_COLOR_FONT_DIRTY = Color.RED;

    public static final Color NODE_COLOR_TITLE_FONT = Color.WHITE;
    public static final Color NODE_COLOR_TITLE_BACKGROUND = Color.BLACK;

    public static final Color CONNECTION_POINT_COLOR = Color.LIGHT_GRAY;
    public static final Color CONNECTION_COLOR = Color.BLUE;

    public static final int CORNER_RADIUS = 5;
    public static final int ALIGN_LEFT=0;
    public static final int ALIGN_RIGHT=1;
    public static final int ALIGN_CENTER=2;
    public static final int ALIGN_TOP=0;
    public static final int ALIGN_BOTTOM=1;

    private final NodeGraph model;

    public NodeGraphViewPanel(NodeGraph model) {
        super();
        this.model=model;
        this.setBackground(PANEL_COLOR_BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g) {
        updatePaintAreaBounds();
        super.paintComponent(g);

        paintNodesInBackground(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        for(Node n : model.getNodes()) paintNode(g,n);

        g.setColor(CONNECTION_COLOR);
        for(NodeConnection c : model.getConnections()) paintConnection(g,c);

        paintExtras(g);
    }

    private void paintNodesInBackground(Graphics g) {
        for(Node n : model.getNodes()) {
            if(n instanceof PrintWithGraphics) {
                ((PrintWithGraphics) n).print(g);
            }
        }
    }

    public void updatePaintAreaBounds() {
        Rectangle r = this.getBounds();
        for(Node n : model.getNodes()) {
            n.updateRectangle();
            Rectangle other = new Rectangle(n.getRectangle());
            //other.grow(100,100);
            r.add(other.getMinX(),other.getMinY());
            r.add(other.getMaxX(),other.getMaxY());
        }
        Dimension d = new Dimension(r.width,r.height);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
        this.setPreferredSize(d);
        //System.out.println("Bounds="+r.toString());
    }

    public void paintNode(Graphics g, Node n) {
        g.setColor(NODE_COLOR_BACKGROUND);
        paintNodeBackground(g,n);

        paintNodeTitleBar(g, n);

        paintAllNodeVariables(g, n);

        g.setColor(NODE_COLOR_BORDER);
        paintNodeBorder(g, n);
    }

    public void paintNodeBackground(Graphics g, Node n) {
        Rectangle r = n.getRectangle();
        g.fillRoundRect(r.x, r.y, r.width, r.height, CORNER_RADIUS, CORNER_RADIUS);
    }

    public void paintNodeTitleBar(Graphics g, Node n) {
        Rectangle r = n.getRectangle();
        g.setColor(NODE_COLOR_TITLE_BACKGROUND);
        g.fillRoundRect(r.x, r.y, r.width, CORNER_RADIUS*2, CORNER_RADIUS, CORNER_RADIUS);
        g.fillRect(r.x, r.y+CORNER_RADIUS, r.width, Node.TITLE_HEIGHT -CORNER_RADIUS);

        Rectangle box = getNodeInternalBounds(n.getRectangle());
        g.setColor(NODE_COLOR_TITLE_FONT);
        box.height=Node.TITLE_HEIGHT;
        paintText(g,n.getLabel(),box,ALIGN_LEFT,ALIGN_CENTER);
        paintText(g,n.getName(),box,ALIGN_RIGHT,ALIGN_CENTER);
    }

    private void paintAllNodeVariables(Graphics g, Node n) {
        for(int i=0;i<n.getNumVariables();++i) {
            NodeVariable<?> v = n.getVariable(i);
            paintVariable(g,v);
        }
    }

    public void paintVariable(Graphics g, NodeVariable<?> v) {
        Rectangle box = v.getRectangle();
        Rectangle insideBox = getNodeInternalBounds(box);

        // label
        g.setColor(v.getIsDirty()?NODE_COLOR_FONT_DIRTY : NODE_COLOR_FONT_CLEAN);
        paintText(g,v.getName(),insideBox,ALIGN_LEFT,ALIGN_CENTER);

        // value
        Object vObj = v.getValue();
        if(vObj != null) {
            String val = vObj.toString();
            int MAX_CHARS = 10;
            if (val.length() > MAX_CHARS) val = val.substring(0, MAX_CHARS) + "...";
            paintText(g, val, insideBox, ALIGN_RIGHT, ALIGN_CENTER);
        }

        // internal border
        g.setColor(NODE_COLOR_INTERNAL_BORDER);
        g.drawLine((int)box.getMinX(),(int)box.getMinY(),(int)box.getMaxX(),(int)box.getMinY());

        // connection points
        g.setColor(CONNECTION_POINT_COLOR);
        paintVariableConnectionPoints(g,v);
    }

    public Rectangle getNodeInternalBounds(Rectangle r) {
        Rectangle r2 = new Rectangle(r);
        int padding = (int)NodeConnection.DEFAULT_RADIUS+4;
        r2.x += padding;
        r2.width -= padding*2;
        return r2;
    }

    public void paintNodeBorder(Graphics g,Node n) {
        Rectangle r = n.getRectangle();
        g.drawRoundRect(r.x, r.y, r.width, r.height,CORNER_RADIUS,CORNER_RADIUS);
    }

    public void paintVariableConnectionPoints(Graphics g, NodeVariable<?> v) {
        if(v.getHasInput()) {
            Point p = v.getInPosition();
            int radius = (int)NodeConnection.DEFAULT_RADIUS+2;
            g.drawOval((int)p.x-radius,(int)p.y-radius,radius*2,radius*2);
        }
        if(v.getHasOutput()) {
            Point p = v.getOutPosition();
            int radius = (int)NodeConnection.DEFAULT_RADIUS+2;
            g.drawOval((int)p.x-radius,(int)p.y-radius,radius*2,radius*2);
        }
    }

    /**
     * Use the graphics context to paint text within a box with the provided alignment.
     * @param g the graphics context
     * @param str the text to paint
     * @param box the bounding limits
     * @param alignH the desired horizontal alignment.  Can be any one of {@link NodeGraphViewPanel#ALIGN_LEFT}, {@link NodeGraphViewPanel#ALIGN_RIGHT}, or {@link NodeGraphViewPanel#ALIGN_CENTER}
     * @param alignV the desired vertical alignment.  Can be any one of {@link NodeGraphViewPanel#ALIGN_TOP}, {@link NodeGraphViewPanel#ALIGN_BOTTOM}, or {@link NodeGraphViewPanel#ALIGN_CENTER}
     */
    public void paintText(Graphics g,String str,Rectangle box,int alignH,int alignV) {
        if(str==null || str.isEmpty()) return;

        FontRenderContext frc = new FontRenderContext(null, false, false);
        TextLayout layout = new TextLayout(str,g.getFont(),frc);
        Rectangle2D nameR = layout.getBounds();

        int x,y;
        switch(alignH) {
            default: x = (int)box.getMinX(); break;
            case ALIGN_RIGHT: x = (int)( box.getMaxX() - nameR.getWidth() ); break;
            case ALIGN_CENTER: x = (int)( box.getMinX() + (box.getWidth() - nameR.getWidth() )/2); break;
        }
        switch(alignV) {
            default: y = (int)( box.getMinY() + nameR.getHeight() ); break;
            case ALIGN_BOTTOM: y = (int)( box.getMaxY() ); break;
            case ALIGN_CENTER: y = (int)( box.getMinY() + (box.getHeight() + nameR.getHeight() )/2); break;
        }
        layout.draw((Graphics2D)g,x,y);
    }

    public void paintConnection(Graphics g, NodeConnection c) {
        Point p0 = c.getInPosition();
        Point p3 = c.getOutPosition();
        paintBezierBetweenTwoPoints(g,p0,p3);

        if(c.isOutputValid()) paintConnectionAtPoint(g,c.getOutPosition());
        if(c.isInputValid()) paintConnectionAtPoint(g,c.getInPosition());
    }

    public void paintConnectionAtPoint(Graphics g,Point p) {
        int radius = (int) NodeConnection.DEFAULT_RADIUS;
        g.fillOval((int) p.x - radius, (int) p.y - radius, radius * 2, radius * 2);
    }

    /**
     * Given points p0 and p3,
     * @param g
     * @param p0
     * @param p3
     */
    public void paintBezierBetweenTwoPoints(Graphics g,Point p0, Point p3) {
        Point p1 = new Point(p0);
        Point p2 = new Point(p3);

        double d=Math.abs(p3.x-p1.x)/2;
        p1.x+=d;
        p2.x-=d;

        Bezier b = new Bezier(
                p0.x,p0.y,
                p1.x,p1.y,
                p2.x,p2.y,
                p3.x,p3.y);
        drawBezier(g,b);
    }

    private void drawBezier(Graphics g, Bezier b) {
        List<Point2D> points = b.generateCurvePoints(0.2);
        int len=points.size();
        int [] x = new int[len];
        int [] y = new int[len];
        for(int i=0;i<len;++i) {
            Point2D p = points.get(i);
            x[i]=(int)p.x;
            y[i]=(int)p.y;
        }
        g.drawPolyline(x,y,len);
    }

    // listener pattern for painting
    private final List<NodeGraphViewListener> listeners = new ArrayList<NodeGraphViewListener>();

    public void addViewListener(NodeGraphViewListener p) {
        listeners.add(p);
    }

    public void removeViewListener(NodeGraphViewListener p) {
        listeners.remove(p);
    }

    private void paintExtras(Graphics g) {
        for( NodeGraphViewListener p : listeners ) {
            p.paint(g, this);
        }
    }
}
