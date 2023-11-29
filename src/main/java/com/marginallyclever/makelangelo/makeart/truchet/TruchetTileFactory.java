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

    public static TruchetTile getTile(int index, Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        switch(index) {
            case 0: return new TruchetDiagonalRising(turtle,spaceBetweenLines,linesPerTileCount);
            case 1: return new TruchetDiagonalFalling(turtle,spaceBetweenLines,linesPerTileCount);
            case 2: return new TruchetOrthogonalH(turtle,spaceBetweenLines,linesPerTileCount);
            case 3: return new TruchetOrthogonalV(turtle,spaceBetweenLines,linesPerTileCount);
            case 4: return new TruchetCurvedCurtainL(turtle,spaceBetweenLines,linesPerTileCount);
            case 5: return new TruchetCurvedCurtainR(turtle,spaceBetweenLines,linesPerTileCount);
            case 6: return new TruchetCurvedFanL(turtle,spaceBetweenLines,linesPerTileCount);
            case 7: return new TruchetCurvedFanR(turtle,spaceBetweenLines,linesPerTileCount);
            default: throw new IllegalArgumentException("Unknown Truchet tile index "+index);
        }
    }
}
