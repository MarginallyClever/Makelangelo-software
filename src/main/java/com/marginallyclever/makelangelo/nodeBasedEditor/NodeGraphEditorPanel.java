package com.marginallyclever.makelangelo.nodeBasedEditor;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodeBasedEditor.model.*;
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

    private Node nodeBeingDragged = null;
    private final NodeConnection connectionBeingCreated = new NodeConnection();

    private Node lastSelectedNode=null;
    private NodeConnectionPointInfo lastConnectionPoint = null;

    final Point2D mousePreviousPosition = new Point2D();

    public NodeGraphEditorPanel(NodeGraphModel model) {
        super(new BorderLayout());
        this.model = model;

        paintArea = new NodeGraphViewPanel(model);
        setupBar();

        this.add(bar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(600,200));

        attachMouseAdapter();
        setupPaintArea();

        setLastSelectedNode(null);
    }

    private void setupPaintArea() {
        paintArea.addViewListener((g,e)->{
            // highlight the last selected node
            if(lastSelectedNode!=null) {
                g.setColor(Color.GREEN);
                paintArea.paintNodeBorder(g,lastSelectedNode);
            }

            // draw a connection as it is being made
            if(connectionBeingCreated.isInputValid() || connectionBeingCreated.isOutputValid()) {
                g.setColor(Color.RED);
                setLineWidth(g,3);

                Point2D a,b;
                if(connectionBeingCreated.isInputValid()) {
                    a = connectionBeingCreated.getInPosition();
                    b = mousePreviousPosition;
                    paintArea.paintConnectionAtPoint(g,a);
                } else {
                    a = mousePreviousPosition;
                    b = connectionBeingCreated.getOutPosition();
                    paintArea.paintConnectionAtPoint(g,b);
                }
                paintArea.paintBezierBetweenTwoPoints(g,a,b);

                setLineWidth(g,1);
            }

            // draw the connection point under the cursor
            if(lastConnectionPoint !=null) {
                g.setColor(Color.RED);
                setLineWidth(g,2);
                paintArea.paintVariableConnectionPoints(g,lastConnectionPoint.getVariable());
                setLineWidth(g,1);
            }
        });
        paintArea.updatePaintAreaBounds();
        paintArea.repaint();
    }

    private void setLineWidth(Graphics g,float r) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(r));
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
        System.out.println("Edit node "+lastSelectedNode.getUniqueName());
    }

    private void onDelete() {
        model.removeNode(lastSelectedNode);
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
        paintArea.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (nodeBeingDragged != null) {
                    Rectangle r = nodeBeingDragged.getRectangle();
                    r.x += e.getX() - mousePreviousPosition.x;
                    r.y += e.getY() - mousePreviousPosition.y;
                    mousePreviousPosition.set(e.getX(), e.getY());
                    paintArea.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                highlightNearbyConnectionPoint(new Point2D(p.x,p.y));
                mousePreviousPosition.set(e.getX(), e.getY());
            }
        });

        paintArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClickConnectionPoint();
                if(lastConnectionPoint == null) {
                    setLastSelectedNode(getNodeAt(e.getPoint()));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // clicking a connection point takes precedence
                if(lastConnectionPoint == null) {
                    // then dragging a node
                    nodeBeingDragged = getNodeAt(e.getPoint());
                    if (nodeBeingDragged != null) {
                        beginDragNode(e.getPoint());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(nodeBeingDragged != null) {
                    // end drag node
                    nodeBeingDragged = null;
                }
            }

            private void beginDragNode(Point e) {
                setLastSelectedNode(nodeBeingDragged);
                mousePreviousPosition.set(e.getX(), e.getY());
            }
        });
    }

    private void onClickConnectionPoint() {
        System.out.println("onClickConnectionPoint");
        if(lastConnectionPoint == null) {
            connectionBeingCreated.disconnectAll();
            return;
        }

        // check that the end node is not the same as the start node.
        if(!connectionBeingCreated.isConnectedTo(lastConnectionPoint.node)) {
            if (lastConnectionPoint.flags == NodeGraphModel.IN) {
                // the output of a connection goes to the input of a node.
                connectionBeingCreated.setOutput(lastConnectionPoint.node, lastConnectionPoint.nodeVariableIndex);
            } else {
                //the output of a node goes to the input of a connection.
                connectionBeingCreated.setInput(lastConnectionPoint.node, lastConnectionPoint.nodeVariableIndex);
            }
        }

        if(connectionBeingCreated.isInputValid() && connectionBeingCreated.isOutputValid() ) {
            if(connectionBeingCreated.isValidDataType()) {
                NodeConnection match = model.getMatchingConnection(connectionBeingCreated);
                if(match!=null) model.removeConnection(match);
                else model.addConnection(new NodeConnection(connectionBeingCreated));
            }
            // if any of the tests failed, restart.
            connectionBeingCreated.disconnectAll();
            repaint();
        }
    }

    private void highlightNearbyConnectionPoint(Point2D p) {
        NodeConnectionPointInfo info = model.getNearestConnection(p,15,NodeGraphModel.IN | NodeGraphModel.OUT);
        setLastConnectionPoint(info);
    }

    /**
     *
     * @param info the {@link NodeConnectionPointInfo}
     */
    private void setLastConnectionPoint(NodeConnectionPointInfo info) {
        lastConnectionPoint = info;
        repaint();
    }

    private void setLastSelectedNode(Node node) {
        lastSelectedNode = node;
        deleteNode.setEnabled(node!=null);
        editNode.setEnabled(node!=null);
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
        NodeConnection c0 = model.addConnection(new NodeConnection(constant0,0,add,0));
        NodeConnection c1 = model.addConnection(new NodeConnection(constant1,0,add,1));
        NodeConnection c2 = model.addConnection(new NodeConnection(add,2,report,0));

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
