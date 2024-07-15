package com.marginallyclever.makelangelo.pen;

import java.util.ArrayList;
import java.util.List;

/**
 * A Palette is a collection of pens.
 */
public class Palette {
    private final List<Pen> pens = new ArrayList<>();

    public void addPen(Pen pen) {
        pens.add(pen);
        System.out.println("Added pen "+pen);
    }

    public void removePen(Pen pen) {
        pens.remove(pen);
        System.out.println("Removed pen "+pen);
    }

    public int getPenCount() {
        return pens.size();
    }

    public Pen getPen(int index) {
        return pens.get(index);
    }

    public void clear() {
        pens.clear();
    }
}
