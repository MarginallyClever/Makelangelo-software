package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Curved Curtain Right
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetCurvedCurtainR extends TruchetCurved {
    public TruchetCurvedCurtainR(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_CURTAIN_RIGHT);
    }
}
