package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import java.util.Collections;
import java.util.LinkedList;

public class LineWeight {
    public LinkedList<LineWeightSegment> segments = new LinkedList<>();

    public void flip() {
        Collections.reverse(segments);
        for (LineWeightSegment s : segments) {
            s.flip();
        }
    }
}
