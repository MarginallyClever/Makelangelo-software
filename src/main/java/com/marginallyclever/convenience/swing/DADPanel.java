package com.marginallyclever.convenience.swing;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * <p>{@link DADPanel} (Drag-and-Drop Panel) contains components that can be vertically rearranged by dragging a handle.</p>
 * <p>To use, call <code>add(component)</code> on {@link DADPanel}, which will wrap <code>component</code> in a
 * {@link DADMiddlePanel} and add it to the bottom end of the panel.  DADPanel fires {@link ListDataEvent} to all
 * {@link ListDataListener} subscribers when the order of the list is changed.</p>
 * <p>The {@link DADPanel} also supports auto-scrolling when dragging a elements near the top or bottom of the viewport.</p>
 */
public class DADPanel extends JPanel {
    private int draggedIndex = -1;
    private int lineY = -1;

    public DADPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        new DropTarget(this, DnDConstants.ACTION_MOVE, new PanelDragAndDropHandler(), true);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dispatchEventToParent(e);
            }
        });
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if(!(comp instanceof DADMiddlePanel)) {
            comp = createInnerPanel(comp);
        }
        super.addImpl(comp, constraints,index);
        revalidate();
        repaint();
    }

    @Override
    public void remove(Component comp) {
        synchronized (getTreeLock()) {
            for(int i=0;i<getComponentCount();++i) {
                var p = (DADMiddlePanel)getComponent(i);
                if(p == comp || p.getInnerComponent() == comp) {
                    super.remove(i);
                    revalidate();
                    repaint();
                    return;
                }
            }
        }
    }

    private void dispatchEventToParent(MouseEvent e) {
        Container parent = getParent();
        if (parent != null) {
            Point parentPoint = SwingUtilities.convertPoint(this, e.getPoint(), parent);
            MouseEvent parentEvent = new MouseEvent(parent, e.getID(), e.getWhen(), e.getModifiersEx(), parentPoint.x, parentPoint.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
            parent.dispatchEvent(parentEvent);
        }
    }

    private DADMiddlePanel createInnerPanel(Component component) {
        DADMiddlePanel panel = new DADMiddlePanel(component);
        panel.getHandle().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = ((JPanel) c.getParent()).getTransferHandler();
                if (handler != null) {
                    handler.exportAsDrag((JComponent) c.getParent(), e, TransferHandler.MOVE);
                    draggedIndex = getComponentZOrder(c.getParent());
                }
            }
        });

        return panel;
    }

    public void updateLineIndicator(int mouseY) {
        lineY = mouseY;
        repaint();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (lineY >= 0) {
            g.setColor(Color.BLUE);
            g.fillRect(0, lineY - 2, getWidth(), 4);
        }
    }

    public void moveDroppableHere(int lineY) {
        int dropIndex = getDropIndex(new Point(0, lineY));
        // Adjust dropIndex if necessary
        dropIndex = dropIndex > draggedIndex ? dropIndex - 1 : dropIndex;
        if (dropIndex != draggedIndex) {
            Component draggedComponent = getComponent(draggedIndex);
            remove(draggedComponent);
            add(draggedComponent, dropIndex);
            revalidate();
            repaint();
            draggedIndex = dropIndex; // Update draggedIndex

            fireMoveEvent(draggedIndex,dropIndex);
        }
    }

    public void addListener(ListDataListener listener) {
        listenerList.add(ListDataListener.class, listener);
    }

    public void removeListener(ListDataListener listener) {
        listenerList.remove(ListDataListener.class, listener);
    }

    /**
     * Fire a move event to all listeners.
     * @param draggedIndex the from index
     * @param dropIndex the to index
     */
    private void fireMoveEvent(int draggedIndex, int dropIndex) {
        ListDataListener[] listeners = listenerList.getListeners(ListDataListener.class);
        ListDataEvent e = null;
        for (ListDataListener listener : listeners) {
            if(e==null) {
                // lazy init - if no listeners we don't waste time allocating ram.
                e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, draggedIndex, dropIndex);
            }
            listener.contentsChanged(e);
        }
    }

    /**
     * If index0 is &gt; index1, index0 and index1 will be swapped such that index0 will always be &lt;= index1.
     * @param index0 the first index
     * @param index1 the second index
     */
    private void fireDeleteEvent(int index0,int index1) {
        ListDataListener[] listeners = listenerList.getListeners(ListDataListener.class);
        ListDataEvent e = null;
        for (ListDataListener listener : listeners) {
            if(e==null) {
                // lazy init - if no listeners we don't waste time allocating ram.
                e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index0, index1);
            }
            listener.intervalRemoved(e);
        }
    }

    private class PanelDragAndDropHandler extends DropTargetAdapter {
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            Point dropPoint = dtde.getLocation();
            updateLineIndicator(dropPoint.y);
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            moveDroppableHere(lineY);
            removeLineIndicator();
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            removeLineIndicator();
        }
    }

    public int getDropIndex(Point dropPoint) {
        for (int i = 0; i < getComponentCount(); i++) {
            Rectangle bounds = getComponent(i).getBounds();
            if (dropPoint.y < bounds.y + bounds.height / 2) {
                return i;
            }
        }
        return getComponentCount();
    }

    public void removeLineIndicator() {
        lineY = -1;
        repaint();
    }

    /**
     * Components are contained in {@link DADMiddlePanel}, which has a checkbox.  Find all components associated with
     * a selected checkbox.
     * @return all selected panels.
     */
    public List<Component> getAllSelectedPanels() {
        List<Component> selectedPanels = new ArrayList<>();
        for (Component c : getComponents()) {
            if (c instanceof DADMiddlePanel panel) {
                if (panel.getCheck().isSelected()) {
                    selectedPanels.add(panel.getInnerComponent());
                }
            }
        }
        return selectedPanels;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            JScrollPane scrollPane = new JScrollPane();
            DADPanel panel = new DADPanel();
            scrollPane.setViewportView(panel);

            panel.add(new JButton("Item 1"));
            panel.add(new JButton("Item 2"));
            panel.add(new JButton("Item 3"));
            panel.add(new JButton("Item 4"));
            panel.add(new JButton("Item 5"));

            frame.add(scrollPane);
            frame.setTitle("Drag-and-Drop Panels with Buttons");
            frame.setSize(400, 300);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
