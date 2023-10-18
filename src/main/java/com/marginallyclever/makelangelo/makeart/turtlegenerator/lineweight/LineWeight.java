package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LineWeight {
    public List<LineSegmentWeight> segments = new ArrayList<>();

    public void flip() {
        Collections.reverse(segments);
        for (LineSegmentWeight s : segments) {
            s.flip();
        }
    }
}
