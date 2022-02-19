package com.marginallyclever.nodeBasedEditor;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.nodeBasedEditor.model.*;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Add;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.Constant;
import com.marginallyclever.nodeBasedEditor.model.builtInNodes.ReportToStdOut;
import com.marginallyclever.nodeBasedEditor.view.NodeGraphViewPanel;
import com.marginallyclever.util.PreferencesHelper;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeGraphEditorPanel} is a Graphic User Interface to edit a {@link NodeGraphModel}.
 */
public class NodeGraphEditorPanel extends JPanel {
    private final NodeGraphModel model;
    private final NodeGraphViewPanel paintArea;
    private final JToolBar bar = new JToolBar();
    private final JButton deleteNodes = new JButton("Delete");
    private final JButton copyNodes = new JButton("Copy");
    private final JButton pasteNodes = new JButton("Paste");
    private final JButton editNode = new JButton("Edit");

    private final NodeConnection connectionBeingCreated = new NodeConnection();

    private final List<Node> selectedNodes = new ArrayList<Node>();
    private NodeConnectionPointInfo lastConnectionPoint = null;

    private final Point mousePreviousPosition = new Point();
    private final Point selectionAreaStart = new Point();
    private boolean selectionOn=false;
    private boolean dragOn=false;

    public NodeGraphEditorPanel(NodeGraphModel model) {
        super(new BorderLayout());
        this.model = model;

        paintArea = new NodeGraphViewPanel(model);

        this.add(bar,BorderLayout.NORTH);
        this.add(new JScrollPane(paintArea),BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(600,200));

        setupBar();
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
        });
        paintArea.updatePaintAreaBounds();
        paintArea.repaint();
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
            g.setColor(Color.RED);
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

    private void setupBar() {
        JButton clearAll = new JButton("New");
        bar.add(clearAll);
        clearAll.addActionListener((e)->onClear());

        JButton addConnection = new JButton("Add");
        bar.add(addConnection);
        addConnection.addActionListener((e)->onAdd());

        JButton saveAll = new JButton("Save");
        bar.add(saveAll);
        saveAll.addActionListener((e)->onSave());

        JButton loadAll = new JButton("Load");
        bar.add(loadAll);
        loadAll.addActionListener((e)->onLoad());

        bar.add(deleteNodes);
        deleteNodes.addActionListener((e)->onDelete());
        deleteNodes.setEnabled(false);

        bar.add(copyNodes);
        copyNodes.addActionListener((e)->onCopy());
        copyNodes.setEnabled(false);

        bar.add(pasteNodes);
        pasteNodes.addActionListener((e)->onPaste());
        pasteNodes.setEnabled(false);

        bar.add(editNode);
        editNode.addActionListener((e)->onEdit());
        editNode.setEnabled(false);

        JButton update = new JButton("Update");
        bar.add(update);
        update.addActionListener((e)-> onUpdate());
    }

    private void onSave() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            saveModelToFile(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveModelToFile(String absolutePath) {
        try(BufferedWriter w = new BufferedWriter(new FileWriter(absolutePath))) {
            w.write(model.toJSON().toString());
        } catch(Exception e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void onLoad() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            model.add(loadModelFromFile(fc.getSelectedFile().getAbsolutePath()));
        }
    }

    private NodeGraphModel loadModelFromFile(String absolutePath) {
        NodeGraphModel newModel = new NodeGraphModel();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            JSONObject modelAsJSON = new JSONObject(responseStrBuilder.toString());
            newModel.parseJSON(modelAsJSON);
        } catch(IOException e) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),e.getLocalizedMessage());
            e.printStackTrace();
        }
        return newModel;
    }

    private void onEdit() {
        System.out.println("Edit node(s)");
        throw new RuntimeException("Not implemented");
    }

    private void onUpdate() {
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

    private final NodeGraphModel copiedNodes = new NodeGraphModel();

    private void onCopy() {
        copiedNodes.clear();
        NodeGraphModel modelB = new NodeGraphModel();
        for(Node n : selectedNodes) modelB.add(n);
        List<NodeConnection> selectedConnections = model.getConnectionsBetweenTheseNodes(selectedNodes);
        for(NodeConnection c : selectedConnections) modelB.add(c);
        copiedNodes.add(modelB.deepCopy());
        pasteNodes.setEnabled(!copiedNodes.isEmpty());
    }

    private void onPaste() {
        NodeGraphModel modelC = copiedNodes.deepCopy();
        model.add(modelC);
        setSelectedNodes(modelC.getNodes());
    }

    private void onAdd() {
        System.out.println("adding node");
        Node n = NodeFactoryPanel.runAsDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        if(n!=null) {
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
                // clicking a connection point takes precedence
                if(lastConnectionPoint == null) {
                    // if user presses down on an already selected item then user is dragging selected nodes
                    Node n = getNodeAt(e.getPoint());
                    if(selectedNodes.contains(n)) {
                        beginDragNode(e.getPoint());
                    } else {
                        beginSelectionArea(e.getPoint());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(dragOn) endDragNodes();
                else if(selectionOn) endSelectionArea(e.getPoint());
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
        Rectangle2D r = new Rectangle2D.Double(x1,y1,x2-x1,y2-y1);
        return r;
    }

    private void onClickConnectionPoint() {
        System.out.println("onClickConnectionPoint");
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
            }
            // if any of the tests failed, restart.
            connectionBeingCreated.disconnectAll();
            repaint();
        }
    }

    private void selectOneNearbyConnectionPoint(Point p) {
        NodeConnectionPointInfo info = model.getFirstNearbyConnection(p,15,NodeVariable.IN | NodeVariable.OUT);
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

    private void setSelectedNodes(Object o) {
        selectedNodes.clear();
        if(o instanceof Node) {
            Node node = (Node)o;
            if(node != null) selectedNodes.add(node);
        } else if(o instanceof List<?>) {
            selectedNodes.addAll((List<Node>)o);
        }
        boolean notEmpty = !selectedNodes.isEmpty();
        deleteNodes.setEnabled(notEmpty);
        editNode.setEnabled(notEmpty);
        copyNodes.setEnabled(notEmpty);
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

    public static void main(String[] args) {
        PreferencesHelper.start();
        CommandLineOptions.setFromMain(args);
        Translator.start();

        NodeGraphModel model = new NodeGraphModel();
        Node constant0 = model.add(new Constant(1));
        Node constant1 = model.add(new Constant(2));
        Node add = model.add(new Add());
        Node report = model.add(new ReportToStdOut());
        model.add(new NodeConnection(constant0,0,add,0));
        model.add(new NodeConnection(constant1,0,add,1));
        model.add(new NodeConnection(add,2,report,0));

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
