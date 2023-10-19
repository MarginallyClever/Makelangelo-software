package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Horizontal lines
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetOrthogonalH extends TruchetOrthogonal {
    public TruchetOrthogonalH(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_HORIZONTAL);
    }
}
