package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class TruchetOrthogonal implements TruchetTile {
    public static final int TYPE_RANDOM = 0;
    public static final int TYPE_HORIZONTAL = 1;
    public static final int TYPE_VERTICAL = 2;
    public static final int MAX_TYPES = 3;

    private int type = TYPE_RANDOM;
    private final Turtle turtle;
    private final double spaceBetweenLines;
    private final double tileSize;

    public TruchetOrthogonal(Turtle turtle, double spaceBetweenLines, double linesPerTileCount) {
        super();
        this.turtle=turtle;
        this.spaceBetweenLines = spaceBetweenLines;
        tileSize = spaceBetweenLines * linesPerTileCount;
    }

    @Override
    public void drawTile(double x,double y) {
        switch(type) {
            case TYPE_RANDOM -> {
                if (Math.random() >= 0.5) tileA(x, y);
                else tileB(x, y);
            }
            case TYPE_HORIZONTAL -> tileA(x, y);
            case TYPE_VERTICAL -> tileB(x, y);
        }
    }

    // horizontal lines
    public void tileA(double x0,double y0) {
        double y1=y0+tileSize;

        for(double x=0;x<=tileSize;x += spaceBetweenLines) {
            interTile(
                    x0+x,y0,
                    x0+x,y1);
        }
    }

    // vertical lines
    public void tileB(double x0,double y0) {
        double x1=x0+tileSize;
        double y1=y0+tileSize;

        for(double y=0;y<=tileSize;y += spaceBetweenLines) {
            interTile(
                    x0,y0+y,
                    x1,y0+y);
        }
    }

    private void interTile(double x0,double y0,double x1,double y1) {
        turtle.penUp();
        turtle.moveTo(x0,y0);
        turtle.penDown();
        turtle.moveTo(x1,y1);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if(type<0 || type >=MAX_TYPES) throw new IllegalArgumentException("Invalid type");
        this.type = type;
    }
}
