package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphViewPanel} visualizes the contents of a {@link NodeGraphModel} with Java Swing.
 * It can call on {@link NodeGraphViewListener}s to add additional flavor.
 */
public class NodeGraphViewPanel extends JPanel {
    public static final Color DEFAULT_BORDER = Color.BLACK;
    public static final Color DEFAULT_BACKGROUND = Color.WHITE;
    public static final Color DEFAULT_FONT = Color.BLACK;

    private final NodeGraphModel model;

    public NodeGraphViewPanel(NodeGraphModel model) {
        super();
        this.model=model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        updatePaintAreaBounds();
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        for(Node n : model.getNodes()) paintNode(g,n);

        g.setColor(NodeConnection.DEFAULT_COLOR);
        for(NodeConnection c : model.getConnections()) paintConnection(g,c);

        paintExtras(g);
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
        this.setBounds(r);
        Dimension d = new Dimension(r.width,r.height);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
        //System.out.println("Bounds="+r.toString());
    }

    public void paintNode(Graphics g, Node n) {
        g.setColor(DEFAULT_BACKGROUND);
        paintNodeBackground(g,n);

        g.setColor(DEFAULT_FONT);
        paintNodeTitle(g, n);

        paintAllNodeVariables(g, n);

        g.setColor(DEFAULT_BORDER);
        paintNodeBorder(g, n);
    }

    public void paintNodeBackground(Graphics g, Node n) {
        Rectangle r = n.getRectangle();
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    public void paintNodeTitle(Graphics g,Node n) {
        String name = n.getUniqueName();
        Rectangle box = new Rectangle(n.getRectangle());
        box.height=Node.NODE_TITLE_HEIGHT;
        paintTextCentered(g,name,box);
    }

    private void paintAllNodeVariables(Graphics g, Node n) {
        for(int i=0;i<n.getNumVariables();++i) {
            NodeVariable<?> v = n.getVariable(i);
            paintVariable(g,v);
        }
    }

    public void paintVariable(Graphics g, NodeVariable<?> v) {
        Rectangle box = v.getRectangle();
        // label
        g.setColor(DEFAULT_FONT);
        paintTextCentered(g,v.getName(),box);

        // internal border
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(box.x,box.y,box.width,box.height);

        // connection points
        g.setColor(NodeVariable.DEFAULT_CONNECTION_POINT_COLOR);
        paintVariableConnectionPoints(g,v);
    }

    public void paintNodeBorder(Graphics g,Node n) {
        Rectangle r = n.getRectangle();
        g.drawRect(r.x, r.y, r.width, r.height);
    }

    public void paintVariableConnectionPoints(Graphics g, NodeVariable<?> v) {
        if(v.getHasInput()) {
            Point2D p = v.getInPosition();
            int radius = (int)NodeConnection.DEFAULT_RADIUS+2;
            g.drawOval((int)p.x-radius,(int)p.y-radius,radius*2,radius*2);
        }
        if(v.getHasOutput()) {
            Point2D p = v.getOutPosition();
            int radius = (int)NodeConnection.DEFAULT_RADIUS+2;
            g.drawOval((int)p.x-radius,(int)p.y-radius,radius*2,radius*2);
        }
    }

    public void paintTextCentered(Graphics g,String str,Rectangle box) {
        FontRenderContext frc = new FontRenderContext(null, false, false);
        Rectangle2D nameR = g.getFont().getStringBounds(str,frc);
        g.setColor(Color.DARK_GRAY);
        g.drawString(str,
                (int)( box.getX() + (box.getWidth() -nameR.getWidth() )/2),
                (int)( box.getY() + box.getHeight()/2 + nameR.getHeight()/2) );
    }

    public void paintConnection(Graphics g, NodeConnection c) {
        Point2D p0 = c.getInPosition();
        Point2D p3 = c.getOutPosition();
        paintBezierBetweenTwoPoints(g,p0,p3);

        if(c.isOutputValid()) paintConnectionAtPoint(g,c.getOutPosition());
        if(c.isInputValid()) paintConnectionAtPoint(g,c.getInPosition());
    }

    public void paintConnectionAtPoint(Graphics g,Point2D p) {
        int radius = (int) NodeConnection.DEFAULT_RADIUS;
        g.fillOval((int) p.x - radius, (int) p.y - radius, radius * 2, radius * 2);
    }

    /**
     * Given points p0 and p3,
     * @param g
     * @param p0
     * @param p3
     */
    public void paintBezierBetweenTwoPoints(Graphics g,Point2D p0, Point2D p3) {
        Point2D p1 = new Point2D(p0);
        Point2D p2 = new Point2D(p3);

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
