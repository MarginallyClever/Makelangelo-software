package com.marginallyclever.makelangelo.pen;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;

public class DroppablePanel extends JPanel {
    private static final DataFlavor PANEL_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "JPanel");

    public DroppablePanel(LayoutManager layout) {
        super(layout);
        setTransferHandler(new PanelTransferHandler());
        new DropTarget(this, DnDConstants.ACTION_MOVE, new PanelDropTargetListener(), true);
    }

    private static class PanelTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{PANEL_FLAVOR};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(PANEL_FLAVOR);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!isDataFlavorSupported(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return c;
                }
            };
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDrop() && support.isDataFlavorSupported(PANEL_FLAVOR);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                JPanel droppedPanel = (JPanel) support.getTransferable().getTransferData(PANEL_FLAVOR);
                Container container = (Container) support.getComponent();
                while (!(container instanceof DADPanel parentPanel)) {
                    container = container.getParent();
                }
                Point dropPoint = support.getDropLocation().getDropPoint();
                int dropIndex = parentPanel.getDropIndex(dropPoint);
                System.out.println("import "+dropPoint+" "+dropIndex);
                parentPanel.remove(droppedPanel);
                parentPanel.add(droppedPanel, dropIndex);
                parentPanel.revalidate();
                parentPanel.repaint();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class PanelDropTargetListener extends DropTargetAdapter {
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            Container first = (Container) dtde.getDropTargetContext().getComponent();
            Container parent = getDragParentOf(first);
            DADPanel parentPanel = (DADPanel) parent;
            parentPanel.updateLineIndicator(getYRelativeToParent(dtde.getLocation(),first,parent));
        }

        private Container getDragParentOf(Container first) {
            Container parent = first.getParent();
            while (!(parent instanceof DADPanel)) {
                parent = parent.getParent();
            }
            return parent;
        }

        private int getYRelativeToParent(Point dropPoint,Container first,Container parent) {
            // first is contained inside parent.  get the relative y offset of first from parent
            Point firstLocation = SwingUtilities.convertPoint(first, 0, 0, parent);
            var snapY = dropPoint.y;
            if (dropPoint.y < first.getHeight() / 2) {
                // if dropPoint is in the top half of first, snapY is the top of first
                snapY = 0;
            } else {
                // if dropPoint is in the bottom half of first, snapY is the bottom of first
                snapY = first.getHeight();
            }
            return firstLocation.y + snapY;
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            Container first = (Container) dtde.getDropTargetContext().getComponent();
            Container parent = getDragParentOf(first);
            DADPanel parentPanel = (DADPanel) parent;
            parentPanel.removeLineIndicator();
            parentPanel.moveDroppableHere(getYRelativeToParent(dtde.getLocation(),first,parent));
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            Container container = (Container) dte.getDropTargetContext().getComponent();
            while (!(container instanceof DADPanel)) {
                container = container.getParent();
            }
            DADPanel parentPanel = (DADPanel) container;
            parentPanel.removeLineIndicator();
        }
    }
}
