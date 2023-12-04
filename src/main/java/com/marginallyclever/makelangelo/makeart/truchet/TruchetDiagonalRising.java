package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Truchet Tile - Diagonal up to the right
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetDiagonalRising extends TruchetDiagonal {
    public TruchetDiagonalRising(Turtle turtle,double spaceBetweenLines,double linesPerTileCount) {
        super(turtle,spaceBetweenLines,linesPerTileCount);
        setType(TYPE_RISING);
    }
}
