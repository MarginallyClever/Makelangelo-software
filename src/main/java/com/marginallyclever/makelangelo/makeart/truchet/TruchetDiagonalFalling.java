package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Diagonal down to the right
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetDiagonalFalling extends TruchetDiagonal {
    public TruchetDiagonalFalling(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_FALLING);
    }
}
