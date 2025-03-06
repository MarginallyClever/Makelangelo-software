package com.marginallyclever.makelangelo.donatelloimpl.nodes.lines;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.turtle.ListOfLines;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;

import javax.vecmath.Point2d;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Prints a list of lines using the {@link Graphics} context.</p>
 */
public class PrintLines extends Node implements PrintWithGraphics {
    private final InputLines input = new InputLines("lines");
    private final InputInt radius = new InputInt("radius",5);
    private final InputColor color = new InputColor("color",Color.WHITE);
    private final InputInt layer = new InputInt("layer",4);
    private final ReentrantLock lock = new ReentrantLock();
    private ListOfLines list;

    public PrintLines() {
        super("PrintLines");
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
        if(list==null || list.hasNoLines()) return;

        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(radius.getValue(),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.setColor(color.getValue());

        lock.lock();
        try {
            list.getAllLines().stream().parallel().forEach(p -> {
                var points = p.getAllPoints();
                if(points.isEmpty()) return;
                Point2d prev = points.getFirst();
                for (int i = 1; i < points.size(); i++) {
                    Point2d next = points.get(i);
                    g2.drawLine((int) prev.x, (int) prev.y, (int) next.x, (int) next.y);
                    prev = next;
                }
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
