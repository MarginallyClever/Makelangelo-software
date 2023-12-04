package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Curved Curtain Left
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetCurvedCurtainL extends TruchetCurved {
    public TruchetCurvedCurtainL(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_CURTAIN_LEFT);
    }
}
