package com.marginallyclever.makelangelo.pen;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DADPanel extends JPanel {
    private int draggedIndex = -1;
    private int lineY = -1;
    private JScrollPane scrollPane;

    public DADPanel(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(createDraggablePanel("Item 1"));
        this.add(createDraggablePanel("Item 2"));
        this.add(createDraggablePanel("Item 3"));
        this.add(createDraggablePanel("Item 4"));
        this.add(createDraggablePanel("Item 5"));

        new DropTarget(this, DnDConstants.ACTION_MOVE, new PanelDragAndDropHandler(), true);
    }

    private DroppablePanel createDraggablePanel(String text) {
        DroppablePanel panel = new DroppablePanel(new BorderLayout());
        panel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        JLabel handle = new JLabel("\u2630 ");  // U+2630 character
        panel.add(handle, BorderLayout.WEST);

        handle.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = ((JPanel) c.getParent()).getTransferHandler();
                if (handler != null) {
                    handler.exportAsDrag((JComponent) c.getParent(), e, TransferHandler.MOVE);
                    draggedIndex = getComponentZOrder(c.getParent());
                }
            }
        });

        panel.add(createInnerPanel(text), BorderLayout.CENTER);
        return panel;
    }

    private Component createInnerPanel(String text) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        container.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        JButton button = new JButton(text);
        button.addActionListener(e -> System.out.println(text + " button clicked!"));
        container.add(button);
        return container;
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
        }
    }

    private class PanelDragAndDropHandler extends DropTargetAdapter {
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            Point dropPoint = dtde.getLocation();
            updateLineIndicator(dropPoint.y);
            autoScroll(dropPoint);
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

        private void autoScroll(Point dropPoint) {
            JViewport viewport = scrollPane.getViewport();
            Rectangle viewRect = viewport.getViewRect();

            int scrollIncrement = 10; // Adjust this value for faster or slower scrolling
            if (dropPoint.y < viewRect.y + scrollIncrement) {
                int newY = Math.max(0, viewRect.y - scrollIncrement);
                scrollPane.getVerticalScrollBar().setValue(newY);
            } else if (dropPoint.y > viewRect.y + viewRect.height - scrollIncrement) {
                int newY = Math.min(getHeight() - viewRect.height, viewRect.y + scrollIncrement);
                scrollPane.getVerticalScrollBar().setValue(newY);
            }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            JScrollPane scrollPane = new JScrollPane();
            DADPanel panel = new DADPanel(scrollPane);
            scrollPane.setViewportView(panel);

            frame.add(scrollPane);
            frame.setTitle("Drag-and-Drop Panels with Buttons");
            frame.setSize(400, 300);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
