package com.marginallyclever.makelangelo.pen;

import com.marginallyclever.convenience.swing.DADPanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PalettePanel extends JPanel {
    private final DADPanel dadPanel = new DADPanel();
    private final List<PenPanel> penPanels;
    private final Palette palette;

    public PalettePanel(Palette palette) {
        setLayout(new BorderLayout());
        this.palette = palette;
        penPanels = new ArrayList<>();

        JScrollPane scrollPane = new JScrollPane(dadPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Create the toolbar with add and delete buttons
        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);

        JButton addButton = new JButton("+");
        toolBar.add(addButton);
        addButton.addActionListener(e -> addPen());

        JButton deleteButton = new JButton("-");
        deleteButton.addActionListener(e -> deleteSelectedPens());
        toolBar.add(deleteButton);
    }

    private void addPen() {
        Pen newPen = new Pen("Pen " + (penPanels.size() + 1));
        PenPanel penPanel = new PenPanel(newPen);
        penPanel.setMinimumSize(penPanel.getPreferredSize());
        penPanel.setMaximumSize(penPanel.getPreferredSize());
        penPanels.add(penPanel);
        palette.addPen(newPen);
        addPenPanelToList(penPanel);

        // Enable dragging on the pen panel
        penPanel.setTransferHandler(new ListItemTransferHandler());
        penPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.exportAsDrag(comp, e, TransferHandler.MOVE);
            }
        });
    }

    private void addPenPanelToList(PenPanel penPanel) {
        dadPanel.add(penPanel);
    }

    private void deleteSelectedPens() {
        // look in dadPanel for all DadInnerPanel with check.isSelected()
        var list = dadPanel.getAllSelectedPanels();

        for (Component penPanel : list) {
            if(!(penPanel instanceof PenPanel pp)) continue;
            penPanels.remove(pp);
            palette.removePen(pp.getPen());
            dadPanel.remove(pp);
        }
        repaint();
    }

    private class ListItemTransferHandler extends TransferHandler {
        private int draggedIndex = -1;

        @Override
        protected Transferable createTransferable(JComponent c) {
            draggedIndex = penPanels.indexOf((PenPanel) c);
            return new StringSelection(Integer.toString(draggedIndex));
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDrop();
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                //int dropIndex = Integer.parseInt(support.getTransferable().getTransferData(DataFlavor.stringFlavor).toString());
                Component dropTarget = support.getComponent().getComponentAt(support.getDropLocation().getDropPoint());
                int targetIndex = penPanels.indexOf((PenPanel) dropTarget);
                if (draggedIndex == -1 || draggedIndex == targetIndex) {
                    return false;
                }

                PenPanel draggedPanel = penPanels.remove(draggedIndex);
                penPanels.add(targetIndex, draggedPanel);

                dadPanel.removeAll();
                for (PenPanel penPanel : penPanels) {
                    addPenPanelToList(penPanel);
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class ListDropTargetListener extends DropTargetAdapter {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            dtde.dropComplete(true);
        }
    }

    public static void main(String[] args) {
        PreferencesHelper.start();
        Translator.start();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Palette Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PalettePanel(new Palette()));
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }
}
