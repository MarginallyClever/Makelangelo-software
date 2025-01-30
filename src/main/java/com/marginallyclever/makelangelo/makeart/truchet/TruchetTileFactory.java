package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for Truchet tiles.
 * @author Dan Royer
 * @since 7.48.0
 */
public class TruchetTileFactory {
    public static List<String> getNames() {
        return new ArrayList<>(List.of(new String[]{
                "Rising",
                "Falling",
                "Horizontal",
                "Vertical",
                "Curtain left",
                "Curtain right",
                "Fan left",
                "Fan right",
        }));
    }

    /**
     * spaceBetweenLines * linesPerTileCount = tileSize
     * @param index the index of the tile to create
     * @param turtle the turtle to draw with
     * @param spaceBetweenLines the distance between lines
     * @param linesPerTileCount the number of lines per tile
     * @return a new Truchet tile
     */
    public static TruchetTile getTile(int index, Turtle turtle, double spaceBetweenLines, int linesPerTileCount) {
        return switch (index) {
            case 0 -> new TruchetDiagonalRising(turtle, spaceBetweenLines, linesPerTileCount);
            case 1 -> new TruchetDiagonalFalling(turtle, spaceBetweenLines, linesPerTileCount);
            case 2 -> new TruchetOrthogonalH(turtle, spaceBetweenLines, linesPerTileCount);
            case 3 -> new TruchetOrthogonalV(turtle, spaceBetweenLines, linesPerTileCount);
            case 4 -> new TruchetCurvedCurtainL(turtle, spaceBetweenLines, linesPerTileCount);
            case 5 -> new TruchetCurvedCurtainR(turtle, spaceBetweenLines, linesPerTileCount);
            case 6 -> new TruchetCurvedFanL(turtle, spaceBetweenLines, linesPerTileCount);
            case 7 -> new TruchetCurvedFanR(turtle, spaceBetweenLines, linesPerTileCount);
            default -> throw new IllegalArgumentException("Unknown Truchet tile index " + index);
        };
    }
}
