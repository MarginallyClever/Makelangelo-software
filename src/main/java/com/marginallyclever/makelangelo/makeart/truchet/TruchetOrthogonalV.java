package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Vertical lines
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetOrthogonalV extends TruchetOrthogonal {
    public TruchetOrthogonalV(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_VERTICAL);
    }
}
