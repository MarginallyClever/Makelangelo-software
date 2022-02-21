package com.marginallyclever.nodeBasedEditor.view;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.nodeBasedEditor.model.*;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadNumber;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.LoadImage;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.PrintImage;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.PrintToStdOut;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.math.Add;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.Action.ACCELERATOR_KEY;

/**
 * {@link NodeGraphEditorPanel} is a Graphic User Interface to edit a {@link NodeGraph}.
 */
public class NodeGraphEditorPanel extends JPanel {
    public static final Color CONNECTION_POINT_COLOR_SELECTED = Color.RED;

    private final NodeGraph model;
    private final NodeGraphViewPanel paintArea;
    private final JToolBar toolBar = new JToolBar();

    private final JPopupMenu popupBar = new JPopupMenu();
    private final JMenuItem deleteNodes = new JMenuItem("Delete");
    private final JMenuItem copyNodes = new JMenuItem("Copy");
    private final JMenuItem pasteNodes = new JMenuItem("Paste");
    private final JMenuItem editNode = new JMenuItem("Edit");
    private final JMenuItem collapseNodes = new JMenuItem("Collapse");
    private final JMenuItem expandNodes = new JMenuItem("Expand");

    private final NodeConnection connectionBeingCreated = new NodeConnection();

    private final List<Node> selectedNodes = new ArrayList<>();
    private NodeConnectionPointInfo lastConnectionPoint = null;

    private final NodeGraph copiedNodes = new NodeGraph();

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

        paintArea = new NodeGraphViewPanel(model);

        this.add(toolBar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(600,200));

        setupToolBar();
        setupPopopBar();
        attachMouseAdapter();
        setupPaintArea();

        setSelectedNodes(null);
    }

    private void setupPaintArea() {
        paintArea.addViewListener((g,e)->{
            highlightSelectedNodes(g);
            paintConnectionBeingMade(g);
            HighlightNearbyConnectionPoint(g);

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
    private void HighlightNearbyConnectionPoint(Graphics g) {
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
        ActionNew clear = new ActionNew("New",this);
        clear.putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        toolBar.add(clear);

        ActionSave saveAll = new ActionSave("Save",this);
        saveAll.putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        toolBar.add(saveAll);

        ActionSave loadAll = new ActionSave("Load",this);
        loadAll.putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        toolBar.add(loadAll);

        ActionPrint print = new ActionPrint("Print",this);
        print.putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        toolBar.add(print);

        ActionUpdate update = new ActionUpdate("Update",this);
        update.putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
        toolBar.add(update);
    }

    private void setupPopopBar() {
        JMenuItem addConnection = new JMenuItem("Add");
        popupBar.add(addConnection);
        addConnection.addActionListener((e)->onAdd());

        popupBar.add(deleteNodes);
        deleteNodes.addActionListener((e)->onDelete());
        deleteNodes.setEnabled(false);

        popupBar.add(copyNodes);
        copyNodes.addActionListener((e)->onCopy());
        copyNodes.setEnabled(false);
        copyNodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));

        popupBar.add(pasteNodes);
        pasteNodes.addActionListener((e)->onPaste());
        pasteNodes.setEnabled(false);
        pasteNodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));

        popupBar.add(editNode);
        editNode.addActionListener((e)->onEdit());
        editNode.setEnabled(false);
        editNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));

        popupBar.add(collapseNodes);
        collapseNodes.addActionListener((e)->onCollapse());
        collapseNodes.setEnabled(false);
        collapseNodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK));

        popupBar.add(expandNodes);
        expandNodes.addActionListener((e)->onExpand());
        expandNodes.setEnabled(false);
        expandNodes.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK));

    }

    private void onCollapse() {
    }

    private void onExpand() {
    }

    private void onEdit() {
        System.out.println("Edit node(s)");
        throw new RuntimeException("Not implemented");
    }

    public void update() {
        try {
            model.update();
            paintArea.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onDelete() {
        for(Node n : selectedNodes) model.remove(n);
        setSelectedNodes(null);
    }

    private void onCopy() {
        copiedNodes.clear();
        NodeGraph modelB = new NodeGraph();
        for(Node n : selectedNodes) modelB.add(n);
        List<NodeConnection> selectedConnections = model.getConnectionsBetweenTheseNodes(selectedNodes);
        for(NodeConnection c : selectedConnections) modelB.add(c);
        copiedNodes.add(modelB.deepCopy());
        pasteNodes.setEnabled(!copiedNodes.isEmpty());
    }

    private void onPaste() {
        NodeGraph modelC = copiedNodes.deepCopy();
        model.add(modelC);
        setSelectedNodes(modelC.getNodes());
    }

    private void onAdd() {
        System.out.println("adding node");
        Node n = NodeFactoryPanel.runAsDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        if(n!=null) {
            n.setPosition(popupPoint);
            model.add(n);
            paintArea.updatePaintAreaBounds();
            paintArea.repaint();
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
                dragSelectedNodes(e);
                mousePreviousPosition.setLocation(e.getX(), e.getY());
                if(selectionOn)
                    paintArea.repaint();
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
                    setSelectedNodes(getNodeAt(e.getPoint()));
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
                        if(selectedNodes.contains(n)) {
                            beginDragNode(e.getPoint());
                        } else {
                            setSelectedNodes(n);
                            beginDragNode(e.getPoint());
                        }
                    } else {
                        beginSelectionArea(e.getPoint());
                    }
                }
            }



            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
                if(dragOn) endDragNodes();
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

    private void beginDragNode(Point e) {
        dragOn=true;
        mousePreviousPosition.setLocation(e.getX(), e.getY());
    }

    private void endDragNodes() {
        dragOn=false;
    }

    private void dragSelectedNodes(MouseEvent e) {
        if(!dragOn) return;
        int dx=e.getX() - mousePreviousPosition.x;
        int dy=e.getY() - mousePreviousPosition.y;
        for(Node n : selectedNodes) {
            Rectangle r = n.getRectangle();
            r.x += dx;
            r.y += dy;
        }
        paintArea.repaint();
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

    Rectangle2D getSelectionArea(Point point) {
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
        NodeConnectionPointInfo info = model.getFirstNearbyConnection(p,15);
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

    @SuppressWarnings("unchecked")
    private void setSelectedNodes(Object o) {
        selectedNodes.clear();
        if(o!=null) {
            if(o instanceof Node) {
                selectedNodes.add((Node) o);
            } else if(o instanceof List<?>) {
                selectedNodes.addAll((List<Node>) o);
            }
        }
        boolean notEmpty = !selectedNodes.isEmpty();
        deleteNodes.setEnabled(notEmpty);
        editNode.setEnabled(notEmpty);
        copyNodes.setEnabled(notEmpty);
        collapseNodes.setEnabled(selectedNodes.size()>1);
        expandNodes.setEnabled();

        repaint();
    }

    /**
     * Return the last {@link Node} at the given point, which will be the top-most visible.
     * @param point the search location.
     * @return the last {@link Node} at the given point
     */
    private Node getNodeAt(Point point) {
        //System.out.println("getNodeAt("+point.x+","+point.y+")");

        List<Node> list = model.getNodes();
        for (int i = list.size(); i-- > 0; ) {
            Node n = list.get(i);
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

    public NodeGraph getGraph() {
        return model;
    }

    /**
     * Clears the internal graph and resets everything.
     */
    public void clear() {
        model.clear();
        Node.setUniqueIDSource(0);
        paintArea.repaint();
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
        Node loadImage = model.add(new LoadImage("test.png"));
        Node printImage = model.add(new PrintImage());
        model.add(new NodeConnection(loadImage,1,printImage,0));

        constant1.getRectangle().y=50;
        add.getRectangle().x=200;
        report.getRectangle().x=400;
        loadImage.getRectangle().setLocation(20,150);
        printImage.getRectangle().setLocation(200,150);

        NodeGraphEditorPanel panel = new NodeGraphEditorPanel(model);
        JFrame frame = new JFrame("NodeBasedEditorPanel");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
