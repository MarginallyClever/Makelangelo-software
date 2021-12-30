package com.marginallyclever.makelangelo;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.select.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * The user-triggered collapsable panel containing the component (trigger) in the titled border
 */
public class CollapsiblePanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 154827706727963515L;
	private String title = "";
    private final TitledBorder border;
    private final JPanel innerPannel;
    private final Window parentWindow;
    private Dimension previousDimension;
    private int heightCollapsibleComponent;
    private boolean collapsedByDefault = false;

    public CollapsiblePanel(Window parentWindow, String title, int heightCollapsibleComponent) {
        this(parentWindow, title);
        this.heightCollapsibleComponent = heightCollapsibleComponent;
        collapsedByDefault = true;
    }

    public CollapsiblePanel(Window parentWindow, String title) {
        this.parentWindow = parentWindow;
        this.title = title;
        border = BorderFactory.createTitledBorder(title);
        setBorder(border);
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        addMouseListener(mouseListener);
        innerPannel = new JPanel();
        innerPannel.addComponentListener(contentComponentListener);
        super.add(innerPannel);
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
        Component r = innerPannel.add(comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(String name, Component comp) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        Component r = innerPannel.add(name, comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(Component comp, int index) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        Component r = innerPannel.add(comp, index);
        updateBorderTitle();
        return r;
    }

    @Override
    public void add(Component comp, Object constraints) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        innerPannel.add(comp, constraints);
        updateBorderTitle();
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        comp.addComponentListener(contentComponentListener);
        comp.setVisible(!collapsedByDefault);
        innerPannel.add(comp, constraints, index);
        updateBorderTitle();
    }

    @Override
    public void remove(int index) {
        Component comp = innerPannel.getComponent(index);
        comp.removeComponentListener(contentComponentListener);
        innerPannel.remove(index);
    }

    @Override
    public void remove(Component comp) {
        comp.removeComponentListener(contentComponentListener);
        innerPannel.remove(comp);
    }

    @Override
    public void removeAll() {
        for (Component c : getComponents()) {
            c.removeComponentListener(contentComponentListener);
        }
        innerPannel.removeAll();
    }

    protected void toggleVisibility() {
        toggleVisibility(hasInvisibleComponent());
    }

    protected void toggleVisibility(boolean visible) {
        for (Component c : innerPannel.getComponents()) {
            c.setVisible(visible);
        }
        updateBorderTitle();
        if (visible) {
            int height = previousDimension == null? heightCollapsibleComponent: previousDimension.height;
            Dimension toggle = new Dimension(parentWindow.getWidth(), height);
            parentWindow.setPreferredSize(toggle);
        } else {
            previousDimension = parentWindow.getSize();
            int height = previousDimension.height - innerPannel.getHeight();
            Dimension toggle = new Dimension(previousDimension.width, height);
            parentWindow.setPreferredSize(toggle);
        }
        parentWindow.validate();
        parentWindow.repaint();
        parentWindow.pack();
        repaint();
    }

    private void updateBorderTitle() {
        String arrow = "";
        if (innerPannel.getComponentCount() > 0) {
            arrow = (hasInvisibleComponent() ? "▾" : "▸");
        }
        border.setTitle(title + " " + arrow + " ");
    }

    private boolean hasInvisibleComponent() {
        for (Component c : innerPannel.getComponents()) {
            if (!c.isVisible()) {
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

        CollapsiblePanel cpanel = new CollapsiblePanel(frame, "lot of buttons", 400);
        jPanel.add(cpanel, BorderLayout.CENTER);

        SelectButton b = new SelectButton("B", "B");
        SelectColor c = new SelectColor("C", "CCCCCC", new ColorRGB(0, 0, 0), frame);
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