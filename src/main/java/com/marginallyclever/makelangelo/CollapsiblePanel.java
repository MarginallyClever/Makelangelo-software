package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.select.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * The user-triggered collapsable panel containing the component (trigger) in the titled border
 */
public class CollapsiblePanel extends JPanel {
	private String title;
    private final TitledBorder border;
    private final JPanel innerPanel;
    private final Window parentWindow;
    private Dimension previousDimension;
    private final int heightCollapsibleComponent;
    private Dimension initialDimension;
    private final boolean collapsedByDefault;

    public CollapsiblePanel(Window parentWindow, String title, int heightCollapsibleComponent, boolean collapsedByDefault) {
        this.parentWindow = parentWindow;
        this.title = title;
        border = BorderFactory.createTitledBorder(title);
        setBorder(border);
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        addMouseListener(mouseListener);
        innerPanel = new JPanel(new GridLayout(1, 1), false);
        parentWindow.addComponentListener(contentComponentListener);
        super.add(innerPanel);
        this.heightCollapsibleComponent = heightCollapsibleComponent;
        this.collapsedByDefault = collapsedByDefault;
        toggleVisibility(false);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            toggleVisibility();
        }
    };

    ComponentListener contentComponentListener = new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
            updateBorderTitle();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            updateBorderTitle();
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        firePropertyChange("title", this.title, this.title = title);
    }

    @Override
    public Component add(Component comp) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        Component r = innerPanel.add(comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(String name, Component comp) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        Component r = innerPanel.add(name, comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(Component comp, int index) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        Component r = innerPanel.add(comp, index);
        updateBorderTitle();
        return r;
    }

    @Override
    public void add(Component comp, Object constraints) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        innerPanel.add(comp, constraints);
        updateBorderTitle();
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        innerPanel.add(comp, constraints, index);
        updateBorderTitle();
    }

    @Override
    public void remove(int index) {
        Component comp = innerPanel.getComponent(index);
        comp.removeComponentListener(contentComponentListener);
        innerPanel.remove(index);
    }

    @Override
    public void remove(Component comp) {
        comp.removeComponentListener(contentComponentListener);
        innerPanel.remove(comp);
    }

    @Override
    public void removeAll() {
        for (Component c : getComponents()) {
            c.removeComponentListener(contentComponentListener);
        }
        innerPanel.removeAll();
    }

    protected void toggleVisibility() {
        toggleVisibility(!hasVisibleComponent());
    }

    protected void toggleVisibility(boolean visible) {
        for (Component c : innerPanel.getComponents()) {
            c.setVisible(visible);
        }
        updateBorderTitle();
        if (initialDimension == null) {
            initialDimension = new Dimension(parentWindow.getSize());
        }

        if (visible) {
            // expands all elements
            int height = previousDimension == null ? heightCollapsibleComponent: previousDimension.height;
            Dimension toggle = new Dimension(parentWindow.getWidth(), height);
            parentWindow.setPreferredSize(toggle);
            parentWindow.setMinimumSize(new Dimension(initialDimension.width, heightCollapsibleComponent));
            parentWindow.setMaximumSize(null);
        } else {
            // collapse all elements
            previousDimension = parentWindow.getSize();
            int height = previousDimension.height - innerPanel.getHeight();
            Dimension toggle = new Dimension(previousDimension.width, height);
            parentWindow.setPreferredSize(toggle);
            parentWindow.setMinimumSize(initialDimension);
            parentWindow.setMaximumSize(new Dimension(previousDimension.width, initialDimension.height));
        }
        parentWindow.validate();
        parentWindow.repaint();
        parentWindow.pack();
        repaint();
    }

    private void updateBorderTitle() {
        String arrow = "";
        if (innerPanel.getComponentCount() > 0) {
            arrow = (hasVisibleComponent() ? "-" : "+");
        }
        border.setTitle(title + " " + arrow + " ");
    }

    private boolean hasVisibleComponent() {
        for (Component c : innerPanel.getComponents()) {
            if (c.isVisible()) {
                return true;
            }
        }
        return false;
    }

    // TEST

    public static void main(String[] args) {
        JFrame frame = new JFrame("Collapsible Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        SelectBoolean a = new SelectBoolean("A", "AAAAAAAAAAA", false);
        jPanel.add(a, BorderLayout.NORTH);

        CollapsiblePanel cpanel = new CollapsiblePanel(frame, "lot of buttons", 400, true);
        jPanel.add(cpanel, BorderLayout.CENTER);

        SelectButton b = new SelectButton("B", "B");
        SelectColor c = new SelectColor("C", "CCCCCC", Color.BLACK, frame);
        SelectFile d = new SelectFile("D", "D", null);
        SelectDouble e = new SelectDouble("E", "E", 0.0f);
        SelectInteger f = new SelectInteger("F", "FFF", 0);
        String[] list = {"cars", "trains", "planes", "boats", "rockets"};
        SelectOneOfMany g = new SelectOneOfMany("G", "G", list, 0);
        String ipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        SelectReadOnlyText h = new SelectReadOnlyText("H", "H " + ipsum);
        SelectSlider i = new SelectSlider("I", "I", 200, 0, 100);
        SelectTextArea j = new SelectTextArea("J", "J", ipsum);

        cpanel.add(b);
        cpanel.add(c);
        cpanel.add(d);
        cpanel.add(e);
        cpanel.add(f);
        cpanel.add(g);
        cpanel.add(h);
        cpanel.add(i);
        cpanel.add(j);

        frame.setPreferredSize(new Dimension(600, 90));
        frame.add(jPanel);
        frame.pack();
        frame.setVisible(true);
    }
}