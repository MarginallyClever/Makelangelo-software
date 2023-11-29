package com.marginallyclever.makelangelo.makeart.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class TruchetDiagonal implements TruchetTile {
    public static final int TYPE_RANDOM = 0;
    public static final int TYPE_RISING = 1;
    public static final int TYPE_FALLING = 2;
    public static final int MAX_TYPES = 3;

    private int type = TYPE_RANDOM;
    private final Turtle turtle;
    private final double spaceBetweenLines;
    private final double tileSize;

    public TruchetDiagonal(Turtle turtle,double spaceBetweenLines,double linesPerTileCount) {
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
            case TYPE_RISING -> tileA(x, y);
            case TYPE_FALLING -> tileB(x, y);
        }
    }

    // style=/
    public void tileA(double x0,double y0) {
        double x1=x0+tileSize;
        double y1=y0+tileSize;

        for(double x=spaceBetweenLines;x<tileSize;x += spaceBetweenLines) {
            interTile(x0+x,y0,x0,y0+x);
            interTile(x0+x,y1,x1,y0+x);
        }
        interTile(x0,y1,x1,y0);
    }

    // style=\
    public void tileB(double x0,double y0) {
        double x1=x0+tileSize;
        double y1=y0+tileSize;

        for(double x=spaceBetweenLines;x<tileSize;x += spaceBetweenLines) {
            interTile(x0+x,y0,x1,y1-x);
            interTile(x0+x,y1,x0,y1-x);
        }
        interTile(x0,y0,x1,y1);
    }

    // Interpolate from (x0,y0) to (x1,y1) in steps of length iterSize.
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
