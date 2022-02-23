package com.marginallyclever.nodeBasedEditor.view.swing;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.nodeBasedEditor.NodeFactory;
import com.marginallyclever.nodeBasedEditor.model.*;
import com.marginallyclever.nodeBasedEditor.builtInNodes.LoadNumber;
import com.marginallyclever.nodeBasedEditor.view.swing.actions.*;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.LoadImage;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.images.PrintImage;
import com.marginallyclever.nodeBasedEditor.builtInNodes.PrintToStdOut;
import com.marginallyclever.nodeBasedEditor.builtInNodes.math.Add;
import com.marginallyclever.nodeBasedEditor.view.swing.nodes.turtle.LoadTurtle;
import com.marginallyclever.nodeBasedEditor.view.actions.*;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphEditorPanel} is a Graphic User Interface to edit a {@link NodeGraph}.
 * @author Dan Royer
 * @since 2022-02-01
 */
public class NodeGraphEditorPanel extends JPanel {
    // used by save and load actions
    public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("Node Graph","graph");

    private static final Color CONNECTION_POINT_COLOR_SELECTED = Color.RED;
    private static final double NEARBY_CONNECTION_DISTANCE_MAX = 20;

    private final NodeGraph model;
    private final NodeGraphViewPanel paintArea;

    /**
     * The currently selected nodes for group operations
     */
    private final List<Node> selectedNodes = new ArrayList<>();

    /**
     * Store copied nodes in this buffer.  Could be a user-space file instead.
     */
    private final NodeGraph copiedGraph = new NodeGraph();

    private final JToolBar toolBar = new JToolBar();
    private final JPopupMenu popupBar = new JPopupMenu();

    private final ActionNewGraph actionNewGraph = new ActionNewGraph("New",this);
    private final ActionSaveGraph actionSaveGraph = new ActionSaveGraph("Save",this);
    private final ActionLoadGraph actionLoadGraph = new ActionLoadGraph("Load",this);
    private final ActionUpdateGraph actionUpdateGraph = new ActionUpdateGraph("Update",this);

    private final ActionPrintGraph actionPrintGraph = new ActionPrintGraph("Print",this);
    private final ActionStraightenGraph actionStraightenGraph = new ActionStraightenGraph("Straighten",this);

    private final ActionCopyGraph actionCopyGraph = new ActionCopyGraph("Copy",this);
    private final ActionPasteGraph actionPasteGraph = new ActionPasteGraph("Paste",this);
    private final ActionDeleteGraph actionDeleteGraph = new ActionDeleteGraph("Delete",this);
    private final ActionCutGraph actionCutGraph = new ActionCutGraph("Cut", actionDeleteGraph, actionCopyGraph);

    private final ActionAddNode actionAddNode = new ActionAddNode("Add",this);
    private final ActionEditNodes actionEditNodes = new ActionEditNodes("Edit",this);
    private final ActionForciblyUpdateNodes actionForciblyUpdateNodes = new ActionForciblyUpdateNodes("Force update",this);
    private final ActionFoldGraph actionFoldGraph = new ActionFoldGraph("Fold",this, actionCutGraph);
    private final ActionUnfoldGraph actionUnfoldGraph = new ActionUnfoldGraph("Unfold",this);

    private final NodeConnection connectionBeingCreated = new NodeConnection();

    private NodeConnectionPointInfo lastConnectionPoint = null;

    // true while dragging one or more nodes around.
    private boolean dragOn=false;
    // for tracking relative motion, useful for relative moves like dragging.
    private final Point mousePreviousPosition = new Point();

    // true while drawing a box to select nodes.
    private boolean selectionOn=false;
    // first corner of the bounding area when a user clicks and drags to form a box.
    private final Point selectionAreaStart = new Point();

    // cursor position when the popup menu happened.
    private final Point popupPoint = new Point();

    public NodeGraphEditorPanel(NodeGraph model) {
        super(new BorderLayout());
        this.model = model;

        NodeFactory.registerBuiltInNodes();

        paintArea = new NodeGraphViewPanel(model);

        this.add(toolBar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);

        setupToolBar();
        setupPopopBar();
        setupAccelerators();

        attachMouseAdapter();
        setupPaintArea();

        setSelectedNodes(null);
        updateActionEnableStatus();
    }

    private void setupPaintArea() {
        paintArea.addViewListener((g,e)->{
            highlightSelectedNodes(g);
            paintConnectionBeingMade(g);
            highlightNearbyConnectionPoint(g);

            if(selectionOn) paintSelectionArea(g);
            paintCursor(g);
        });
        paintArea.updatePaintAreaBounds();
        paintArea.repaint();
    }

    private void paintCursor(Graphics g) {
        int r=5;
        g.setColor(Color.YELLOW);
        g.drawArc(mousePreviousPosition.x-r,mousePreviousPosition.y-r,r*2,r*2,0,360);
    }

    private void highlightSelectedNodes(Graphics g) {
        if(selectedNodes.isEmpty()) return;

        g.setColor(Color.GREEN);
        for( Node n : selectedNodes) {
            paintArea.paintNodeBorder(g, n);
        }
    }

    private void paintConnectionBeingMade(Graphics g) {
        // draw a connection as it is being made
        if(connectionBeingCreated.isInputValid() || connectionBeingCreated.isOutputValid()) {
            g.setColor(Color.RED);
            setLineWidth(g,3);

            Point a,b;
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
    }

    // draw the connection point under the cursor
    private void highlightNearbyConnectionPoint(Graphics g) {
        if(lastConnectionPoint !=null) {
            g.setColor(CONNECTION_POINT_COLOR_SELECTED);
            setLineWidth(g,2);
            paintArea.paintVariableConnectionPoints(g,lastConnectionPoint.getVariable());
            setLineWidth(g,1);
        }
    }

    private void paintSelectionArea(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                2, new float[]{3}, 0);
        g2d.setStroke(dashed);
        g2d.setColor(Color.MAGENTA);
        Rectangle2D r = getSelectionArea(mousePreviousPosition);
        g2d.drawRect((int)r.getMinX(),(int)r.getMinY(),(int)r.getWidth(),(int)r.getHeight());
    }

    private void setLineWidth(Graphics g,float r) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(r));
    }

    private void setupToolBar() {
        toolBar.add(actionNewGraph);
        toolBar.add(actionLoadGraph);
        toolBar.add(actionSaveGraph);
        toolBar.add(actionUpdateGraph);
        toolBar.addSeparator();
        toolBar.add(actionPrintGraph);
        toolBar.add(actionStraightenGraph);
    }

    private void setupPopopBar() {
        popupBar.add(actionAddNode);
        popupBar.add(actionEditNodes);
        popupBar.add(actionForciblyUpdateNodes);
        popupBar.add(actionFoldGraph);
        popupBar.add(actionUnfoldGraph);
        popupBar.addSeparator();
        popupBar.add(actionCopyGraph);
        popupBar.add(actionCutGraph);
        popupBar.add(actionPasteGraph);
        popupBar.addSeparator();
        popupBar.add(actionDeleteGraph);
    }

    private void setupAccelerators() {
        actionNewGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        actionSaveGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        actionLoadGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        actionPrintGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        actionUpdateGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));

        actionAddNode.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        actionEditNodes.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        actionFoldGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));
        actionUnfoldGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));

        actionCopyGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        actionCutGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        actionPasteGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));

        actionDeleteGraph.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK));
    }

    public void update() {
        try {
            model.update();
            paintArea.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void attachMouseAdapter() {
        paintArea.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(dragOn) {
                    int dx = e.getX() - mousePreviousPosition.x;
                    int dy = e.getY() - mousePreviousPosition.y;
                    moveSelectedNodes(dx, dy);
                    paintArea.repaint();
                }
                if(selectionOn)
                    paintArea.repaint();

                mousePreviousPosition.setLocation(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                selectOneNearbyConnectionPoint(new Point(p.x,p.y));
                mousePreviousPosition.setLocation(e.getX(), e.getY());
                if(selectionOn)
                    paintArea.repaint();
            }
        });

        paintArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClickConnectionPoint();
                if(lastConnectionPoint == null) {
                    setSelectedNode(getNodeAt(e.getPoint()));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);

                // clicking a connection point takes precedence
                if(lastConnectionPoint == null) {
                    // if user presses down on an already selected item then user is dragging selected nodes
                    Node n = getNodeAt(e.getPoint());
                    if(n!=null) {
                        if(!selectedNodes.contains(n)) {
                            setSelectedNode(n);
                        }
                        dragOn=true;
                    } else {
                        // nothing under point, start new selection.
                        beginSelectionArea(e.getPoint());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
                if(dragOn) dragOn=false;
                else if(selectionOn) endSelectionArea(e.getPoint());
            }

            private void maybeShowPopup(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    popupPoint.setLocation(e.getPoint());
                    popupBar.show(e.getComponent(),e.getX(),e.getY());
                }
            }
        });
    }

    private void moveSelectedNodes(int dx, int dy) {
        for(Node n : selectedNodes) {
            n.moveRelative(dx,dy);
        }
    }

    private void beginSelectionArea(Point point) {
        selectionOn=true;
        selectionAreaStart.x=point.x;
        selectionAreaStart.y=point.y;
    }

    private void endSelectionArea(Point point) {
        selectionOn=false;
        setSelectedNodes(model.getNodesInRectangle(getSelectionArea(point)));
    }

    private Rectangle2D getSelectionArea(Point point) {
        double x1 = Math.min(point.x, selectionAreaStart.x);
        double x2 = Math.max(point.x, selectionAreaStart.x);
        double y1 = Math.min(point.y, selectionAreaStart.y);
        double y2 = Math.max(point.y, selectionAreaStart.y);
        return new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
    }

    private void onClickConnectionPoint() {
        if(lastConnectionPoint == null) {
            connectionBeingCreated.disconnectAll();
            return;
        }

        // check that the end node is not the same as the start node.
        if(!connectionBeingCreated.isConnectedTo(lastConnectionPoint.node)) {
            if (lastConnectionPoint.flags == NodeVariable.IN) {
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
                if(match!=null) model.remove(match);
                else model.add(new NodeConnection(connectionBeingCreated));
            } else {
                NodeVariable<?> vIn = connectionBeingCreated.getInVariable();
                NodeVariable<?> vOut = connectionBeingCreated.getOutVariable();
                String nameIn = (vIn==null) ? "null" : vIn.getTypeClass();
                String nameOut = (vOut==null) ? "null" : vOut.getTypeClass();
                System.out.println("Invalid types "+nameOut+", "+nameIn+".");
            }
            // if any of the tests failed, restart.
            connectionBeingCreated.disconnectAll();
            repaint();
        }
    }

    private void selectOneNearbyConnectionPoint(Point p) {
        NodeConnectionPointInfo info = model.getFirstNearbyConnection(p,NEARBY_CONNECTION_DISTANCE_MAX);
        setLastConnectionPoint(info);
    }

    /**
     * Remembers a connection point as described by a {@link NodeConnectionPointInfo}.
     * @param info the {@link NodeConnectionPointInfo}
     */
    private void setLastConnectionPoint(NodeConnectionPointInfo info) {
        lastConnectionPoint = info;
        repaint();
    }

    public void setSelectedNode(Node n) {
        ArrayList<Node> nodes = new ArrayList<>();
        if(n!=null) nodes.add(n);
        setSelectedNodes(nodes);
    }

    public void setSelectedNodes(List<Node> list) {
        selectedNodes.clear();
        if (list != null) selectedNodes.addAll(list);
        updateActionEnableStatus();
        paintArea.repaint();
    }

    public List<Node> getSelectedNodes() {
        return selectedNodes;
    }

    private void updateActionEnableStatus() {
        boolean notEmpty = !selectedNodes.isEmpty();

        // TODO All Actions have the tools to check for themselves if they are active.
        actionDeleteGraph.setEnabled(notEmpty);
        actionEditNodes.setEnabled(notEmpty);
        actionCopyGraph.setEnabled(notEmpty);
        actionPasteGraph.setEnabled(!copiedGraph.isEmpty());
        actionFoldGraph.setEnabled(selectedNodes.size()>1);
        actionUnfoldGraph.setEnabled(notEmpty); // TODO more checks here?
    }

    /**
     * Return the last {@link Node} at the given point, which will be the top-most visible.
     * @param point the search location.
     * @return the last {@link Node} at the given point
     */
    private Node getNodeAt(Point point) {
        List<Node> list = model.getNodes();
        // reverse iterator because last node is top-most.
        for (int i = list.size(); i-- > 0; ) {
            Node n = list.get(i);
            if(n.getRectangle().contains(point)) {
                return n;
            }
        }
        return null;
    }

    public NodeGraph getGraph() {
        return model;
    }

    public Point getPopupPoint() {
        return popupPoint;
    }

    public NodeGraph getCopiedGraph() {
        return copiedGraph;
    }

    public void setCopiedGraph(NodeGraph graph) {
        copiedGraph.clear();
        copiedGraph.add(graph);
    }

    /**
     * Clears the internal graph and resets everything.
     */
    public void clear() {
        model.clear();
        Node.setUniqueIDSource(0);
        connectionBeingCreated.disconnectAll();
        setSelectedNode(null);
        repaint();
    }

    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        NodeGraph model = new NodeGraph();
        Node constant0 = model.add(new LoadNumber(1));
        Node constant1 = model.add(new LoadNumber(2));
        Node add = model.add(new Add());
        Node report = model.add(new PrintToStdOut());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.add(new NodeConnection(add,2,report,0));
        constant1.getRectangle().y=50;
        add.getRectangle().x=200;
        report.getRectangle().x=400;

        Node loadImage = model.add(new LoadImage("test.png"));
        Node printImage = model.add(new PrintImage());
        model.add(new NodeConnection(loadImage,1,printImage,0));
        loadImage.getRectangle().setLocation(0,150);
        printImage.getRectangle().setLocation(200,150);

        Node loadTurtle = model.add(new LoadTurtle("./src/test/resources/stanfordBunny.svg"));
        loadTurtle.getRectangle().setLocation(0,300);

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);

        JFrame frame = new JFrame("NodeBasedEditorPanel");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1200,800));
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }
}
