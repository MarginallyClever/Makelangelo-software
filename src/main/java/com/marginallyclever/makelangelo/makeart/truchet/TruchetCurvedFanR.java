package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Curved Fan Right
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetCurvedFanR extends TruchetCurved {
    public TruchetCurvedFanR(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_FAN_RIGHT);
    }
}
