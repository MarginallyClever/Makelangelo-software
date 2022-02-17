package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.Bezier;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.List;

/**
 * {@link NodeGraphEditorPanel} is the View to a {@link NodeGraphModel}.
 */
public class NodeGraphEditorPanel extends JPanel {
    private final NodeGraphModel model;
    private final JPanel paintArea = new JPanel();
    private final JToolBar bar = new JToolBar();

    public NodeGraphEditorPanel() {
        this(new NodeGraphModel());
    }

    public NodeGraphEditorPanel(NodeGraphModel model) {
        super(new BorderLayout());
        this.model = model;

        JScrollPane scrollPane = new JScrollPane(paintArea);

        JButton addConnection = new JButton("Add Node");
        bar.add(addConnection);
        addConnection.addActionListener((e)->onAdd());

        JButton toString = new JButton("toString");
        bar.add(toString);
        toString.addActionListener((e)-> System.out.println(model) );

        this.add(bar,BorderLayout.NORTH);
        this.add(scrollPane,BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(200,200));

        System.out.println("Attaching mouse adapter");
        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {}
            @Override
            public void mouseMoved(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("mouse at node "+getNodeAt(e.getPoint()));
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        };
        paintArea.addMouseMotionListener(m);
        paintArea.addMouseListener(m);
        updatePaintAreaBounds();
        paintArea.repaint();

    }

    private Node getNodeAt(Point point) {
        System.out.println("getNodeAt("+point.x+","+point.y+")");
        for(Node n : model.getNodes()) {
            Rectangle r = n.getRectangle();
            if(r.x>point.x) continue;
            if(r.y>point.y) continue;
            if(r.x+r.width<point.x) continue;
            if(r.y+r.height<point.y) continue;
            return n;
        }
        return null;
    }

    private void onAdd() {
        System.out.println("adding node");
        try {
            Node n = model.addNode(new Add());
            model.addNode(n);
            updatePaintAreaBounds();
            paintArea.repaint();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        updatePaintAreaBounds();
        super.paintComponent(g);

        for(Node n : model.getNodes()) paintNode(g,n);
        for(NodeConnection c : model.getConnections()) paintConnection(g,c);
    }

    private void updatePaintAreaBounds() {
        Rectangle r = paintArea.getBounds();
        for(Node n : model.getNodes()) {
            n.updateRectangle();
            Rectangle other = new Rectangle(n.getRectangle());
            other.grow(100,100);
            r.add(other.getMinX(),other.getMinY());
            r.add(other.getMaxX(),other.getMaxY());
        }
        paintArea.setBounds(r);
        Dimension d = new Dimension(r.width,r.height);
        paintArea.setMinimumSize(d);
        paintArea.setMaximumSize(d);
        System.out.println("Bounds="+r.toString());
    }

    private void paintNode(Graphics g, Node n) {
        Rectangle r = n.getRectangle();
        g.setColor(Color.WHITE);
        g.fillRect(r.x,r.y,r.width,r.height);
        g.setColor(Color.BLACK);
        g.drawRect(r.x,r.y,r.width,r.height);
    }

    private void paintConnection(Graphics g, NodeConnection c) {
        Vector2d p0 = c.getInPosition();
        Vector2d p1 = new Vector2d(p0);
        p1.x+=10;
        Vector2d p3 = c.getOutPosition();
        Vector2d p2 = new Vector2d(p3);
        p1.x-=10;
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

    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        NodeGraphModel model = new NodeGraphModel();
        Node constant0 = model.addNode(new Constant(1));
        Node constant1 = model.addNode(new Constant(2));
        Node add = model.addNode(new Add());
        Node report = model.addNode(new ReportToStdOut());
        NodeConnection c0 = model.createNodeConnection();   c0.setInput(constant0,0);   c0.setOutput(add,0);
        NodeConnection c1 = model.createNodeConnection();   c1.setInput(constant1,0);   c1.setOutput(add,1);
        NodeConnection c2 = model.createNodeConnection();   c2.setInput(add,2);         c2.setOutput(report,0);

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);
        JFrame frame = new JFrame("NodeBasedEditorPanel");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
