package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.Node;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeConnection;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeGraphModel;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.NodeVariable;
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
    private final JButton deleteNode = new JButton("Delete");
    private final JButton editNode = new JButton("Edit");

    private Node nodeBeingDragged=null;

    public NodeGraphEditorPanel(NodeGraphModel model) {
        super(new BorderLayout());
        this.model = model;

        paintArea = new NodeGraphViewPanel(model);

        setupBar();

        this.add(bar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(600,200));

        attachMouseAdapter();
        paintArea.updatePaintAreaBounds();
        paintArea.repaint();

        setLastSelectedNode(null);
    }

    private void setupBar() {
        JButton clearAll = new JButton("New");
        bar.add(clearAll);
        clearAll.addActionListener((e)->onClear());

        JButton addConnection = new JButton("Add");
        bar.add(addConnection);
        addConnection.addActionListener((e)->onAdd());

        bar.add(deleteNode);
        deleteNode.addActionListener((e)->onDelete());

        bar.add(editNode);
        editNode.addActionListener((e)->onEdit());

        JButton toString = new JButton("toString");
        bar.add(toString);
        toString.addActionListener((e)-> System.out.println(model) );

        JButton update = new JButton("Update");
        bar.add(update);
        update.addActionListener((e)-> {
            try {
                System.out.println("Update");
                model.update();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void onEdit() {
        System.out.println("Edit node "+paintArea.getLastSelectedNode().getUniqueName());
    }

    private void onDelete() {
        model.removeNode(paintArea.getLastSelectedNode());
        setLastSelectedNode(null);
    }

    private void onAdd() {
        System.out.println("adding node");
        try {
            Node n = model.addNode(new Add());
            model.addNode(n);
            paintArea.updatePaintAreaBounds();
            paintArea.repaint();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void onClear() {
        System.out.println("clear");
        model.clear();
        paintArea.repaint();
    }

    private void attachMouseAdapter() {
        System.out.println("Attaching mouse adapter");
        final Point2D mouseDragPreviousPosition = new Point2D();

        paintArea.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (nodeBeingDragged != null) {
                    Rectangle r = nodeBeingDragged.getRectangle();
                    r.x += e.getX() - mouseDragPreviousPosition.x;
                    r.y += e.getY() - mouseDragPreviousPosition.y;
                    mouseDragPreviousPosition.set(e.getX(), e.getY());
                    paintArea.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                highlightNearbyConnectionPoint(new Point2D(p.x,p.y));
            }
        });

        paintArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setLastSelectedNode(getNodeAt(e.getPoint()));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                nodeBeingDragged=getNodeAt(e.getPoint());
                if(nodeBeingDragged!=null) {
                    beginDragNode(e.getPoint());
                }
                onClickConnectionPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(nodeBeingDragged!=null) {
                    nodeBeingDragged = null;
                } else if(paintArea.getLastSelectedVariable()!=null) {
                    paintArea.setLastSelectedVariable(null,null,0);
                }
            }

            private void beginDragNode(Point e) {
                setLastSelectedNode(nodeBeingDragged);
                mouseDragPreviousPosition.set(e.getX(), e.getY());
                Rectangle r = nodeBeingDragged.getRectangle();
            }
        });
    }

    private boolean createConnectionStarted=false;
    private Node createConnectionNode;
    private NodeVariable<?> createConnectionVariable;
    private int createConnectionFlags;

    private void onClickConnectionPoint() {
        if(!createConnectionStarted) {
            // store the start point
            createConnectionStarted = true;
            createConnectionVariable = paintArea.getLastSelectedVariable();
            createConnectionFlags = paintArea.getLastSelectedFlags();
        } else {
            // check that the end point is not the same flag as the start point.
            // check that the end node is not the same as the start node.
            // check if the user created from end to start instead of start to end.

        }
    }

    private void highlightNearbyConnectionPoint(Point2D p) {
        NodeVariable<?> v = model.getNearestConnection(p,15,NodeGraphModel.IN | NodeGraphModel.OUT);
        if(v!=null) {
            if( v.getInPosition().distanceSquared(p) <
                    v.getOutPosition().distanceSquared(p) ) {
                // near the in point
                System.out.println("in");
                setLastSelectedVariable(v,v.getInPosition(),NodeGraphModel.IN);
            } else {
                // near the out point
                System.out.println("out");
                setLastSelectedVariable(v,v.getOutPosition(),NodeGraphModel.OUT);
            }
        } else {
            setLastSelectedVariable(null,null,0);
        }
    }

    /**
     *
     * @param v the {@link NodeVariable}
     * @param p the point
     * @param flag either {@code NodeGraphModel.IN} or {@code NodeGraphModel.OUT}
     */
    private void setLastSelectedVariable(NodeVariable<?> v, Point2D p,int flag) {
        paintArea.setLastSelectedVariable(v,p,flag);
        repaint();
    }

    private void setLastSelectedNode(Node nodeAt) {
        paintArea.setLastSelectedNode(nodeAt);
        deleteNode.setEnabled(nodeAt!=null);
        editNode.setEnabled(nodeAt!=null);
        repaint();
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

    public static void main(String[] args) throws Exception {
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

        constant1.getRectangle().y=50;
        add.getRectangle().x=200;
        report.getRectangle().x=400;

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);
        JFrame frame = new JFrame("NodeBasedEditorPanel");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
