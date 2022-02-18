package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * {@link NodeGraphViewPanel} visualizes the contents of a {@link NodeGraphModel} with Java Swing.
 */
public class NodeGraphViewPanel extends JPanel {
    public static final Color DEFAULT_BORDER = Color.BLACK;
    public static final Color DEFAULT_BORDER_SELECTED = Color.GREEN;

    private final NodeGraphModel model;

    private Node lastSelectedNode=null;
    private NodeConnectionPointInfo lastSelectedVariable = null;

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
        for(NodeConnection c : model.getConnections()) paintConnection(g,c);
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
        paintNodeBackground(g,n);
        paintNodeTitle(g, n);
        paintVariables(g, n);
        paintNodeBorder(g, n);
    }

    public void paintNodeBackground(Graphics g, Node n) {
        Rectangle r = n.getRectangle();
        g.setColor(Color.WHITE);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    public void paintNodeTitle(Graphics g,Node n) {
        String name = n.getUniqueName();
        Rectangle box = new Rectangle(n.getRectangle());
        box.height=Node.NODE_TITLE_HEIGHT;
        paintTextCentered(g,name,box);
    }

    public void paintVariables(Graphics g, Node n) {
        int x = n.getRectangle().x;
        int y = n.getRectangle().y+Node.NODE_TITLE_HEIGHT;
        for(int i=0;i<n.getNumVariables();++i) {
            NodeVariable<?> v = n.getVariable(i);
            String name = v.getName();
            Rectangle box = new Rectangle(v.getRectangle());
            box.y=y;
            box.x=x;
            y+=box.height;
            paintTextCentered(g,name,box);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(box.x,box.y,box.width,box.height);
            g.setColor(NodeVariable.DEFAULT_CONNECTION_POINT_COLOR);
            paintVariableConnectionPoints(g,v);
        }
    }

    public void paintNodeBorder(Graphics g,Node n) {
        g.setColor(lastSelectedNode == n ? DEFAULT_BORDER_SELECTED : DEFAULT_BORDER);
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
        Point2D p1 = new Point2D(p0);
        p1.x+=50;
        Point2D p3 = c.getOutPosition();
        Point2D p2 = new Point2D(p3);
        p2.x-=50;
        Bezier b = new Bezier(
                p0.x,p0.y,
                p1.x,p1.y,
                p2.x,p2.y,
                p3.x,p3.y);
        drawBezier(g,b);

        if(c.isOutputValid()) {
            Point2D p = c.getOutPosition();
            int radius = (int) NodeConnection.DEFAULT_RADIUS;
            g.setColor(NodeConnection.DEFAULT_COLOR);
            g.fillOval((int) p.x - radius, (int) p.y - radius, radius * 2, radius * 2);
        }
        if(c.isInputValid()) {
            Point2D p = c.getInPosition();
            int radius = (int) NodeConnection.DEFAULT_RADIUS;
            g.setColor(NodeConnection.DEFAULT_COLOR);
            g.fillOval((int) p.x - radius, (int) p.y - radius, radius * 2, radius * 2);
        }
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
        g.setColor(NodeConnection.DEFAULT_COLOR);
        g.drawPolyline(x,y,len);
    }

    public Node getLastSelectedNode() {
        return lastSelectedNode;
    }

    public void setLastSelectedNode(Node lastSelectedNode) {
        this.lastSelectedNode = lastSelectedNode;
    }

    public NodeConnectionPointInfo getLastSelectedVariable() {
        return lastSelectedVariable;
    }

    /**
     *
     * @param v the {@link NodeConnectionPointInfo}
     */
    public void setLastSelectedVariable(NodeConnectionPointInfo info) {
        lastSelectedVariable = info;
    }
}
