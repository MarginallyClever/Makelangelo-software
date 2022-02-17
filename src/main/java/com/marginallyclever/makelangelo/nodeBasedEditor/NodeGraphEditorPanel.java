package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * {@link NodeGraphEditorPanel} is a Graphic User Interface to edit a {@link NodeGraphModel}.
 */
public class NodeGraphEditorPanel extends JPanel {
    private final NodeGraphModel model;
    private final NodeGraphViewPanel paintArea;
    private final JToolBar bar = new JToolBar();

    public NodeGraphEditorPanel(NodeGraphModel model) {
        super(new BorderLayout());
        this.model = model;

        paintArea = new NodeGraphViewPanel(model);

        JButton addConnection = new JButton("Add Node");
        bar.add(addConnection);
        addConnection.addActionListener((e)->onAdd());

        JButton toString = new JButton("toString");
        bar.add(toString);
        toString.addActionListener((e)-> System.out.println(model) );

        this.add(bar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(200,200));

        attachMouseAdapter();
        paintArea.updatePaintAreaBounds();
        paintArea.repaint();
    }

    private Node nodeBeingDragged=null;

    private void attachMouseAdapter() {
        System.out.println("Attaching mouse adapter");
        final Point2D offset = new Point2D();
        final Point2D prev = new Point2D();

        paintArea.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged (MouseEvent e){
                if (nodeBeingDragged != null) {
                    Rectangle r = nodeBeingDragged.getRectangle();
                    r.x += e.getX() - prev.x;
                    r.y += e.getY() - prev.y;
                    prev.set(e.getX(), e.getY());
                    paintArea.repaint();
                }
            }

            @Override
            public void mouseMoved (MouseEvent e) {}
        });

        paintArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                nodeBeingDragged=getNodeAt(e.getPoint());
                if(nodeBeingDragged!=null) {
                    //System.out.println("hit "+nodeBeingDragged.getUniqueName());
                    prev.set(e.getX(), e.getY());
                    Rectangle r = nodeBeingDragged.getRectangle();
                    offset.set(e.getX() - r.x, e.getY() - r.y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //System.out.println("release");
                nodeBeingDragged=null;
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private Node getNodeAt(Point point) {
        //System.out.println("getNodeAt("+point.x+","+point.y+")");
        for(Node n : model.getNodes()) {
            Rectangle r = n.getRectangle();
            //System.out.println(n.getUniqueName()+":"+r);
            if(r.getMinX()>point.x) continue;
            if(r.getMinY()>point.y) continue;
            if(r.getMaxX()<point.x) continue;
            if(r.getMaxY()<point.y) continue;
            return n;
        }
        return null;
    }

    private void onAdd() {
        System.out.println("adding node");
        try {
            Node n = model.addNode(new Add());
            model.addNode(n);
            paintArea.updatePaintAreaBounds();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
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
