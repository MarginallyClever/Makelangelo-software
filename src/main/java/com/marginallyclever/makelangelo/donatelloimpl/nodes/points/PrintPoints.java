package com.marginallyclever.makelangelo.donatelloimpl.nodes.points;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.turtle.ListOfPoints;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;

import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Draw a list of points.
 */
public class PrintPoints extends Node implements PrintWithGraphics {
    private final InputPoints input = new InputPoints("points");
    private final InputInt radius = new InputInt("radius",5);
    private final InputColor color = new InputColor("color",Color.WHITE);
    private final InputInt layer = new InputInt("layer",4);
    private final ReentrantLock lock = new ReentrantLock();
    private ListOfPoints list;

    public PrintPoints() {
        super("PrintPoints");
        addPort(input);
        addPort(radius);
        addPort(color);
        addPort(layer);
    }

    @Override
    public void update() {
        lock.lock();
        try {
            list = input.getValue();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void print(Graphics g) {
        if(list==null || list.hasNoPoints()) return;

        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(radius.getValue(),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.setColor(color.getValue());

        lock.lock();
        try {
            list.getAllPoints().stream().parallel().forEach(p -> {
                g2.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
            });
        } finally {
            lock.unlock();
        }
        g2.dispose();
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }
}
