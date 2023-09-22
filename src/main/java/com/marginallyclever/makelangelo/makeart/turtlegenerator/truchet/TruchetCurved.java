package com.marginallyclever.makelangelo.makeart.turtlegenerator.truchet;

import com.marginallyclever.makelangelo.turtle.Turtle;

public class TruchetCurved implements TruchetTileGenerator {
    private final Turtle turtle;
    private final double spaceBetweenLines;
    private final double tileSize;

    public TruchetCurved(Turtle turtle,double spaceBetweenLines,double linesPerTileCount) {
        super();
        this.turtle=turtle;
        this.spaceBetweenLines = spaceBetweenLines;
        tileSize = spaceBetweenLines * linesPerTileCount;
    }

    @Override
    public void drawTile(double x,double y) {
        if(Math.random() >= 0.5) tileA(x,y);
        else                     tileB(x,y);
    }

    // style=/
    public void tileA(double x0,double y0) {
        double x1=x0+tileSize;
        double y1=y0+tileSize;

        for(double x=spaceBetweenLines;x<tileSize;x += spaceBetweenLines) {
            interTile(x0,y0,x,Math.PI*0.0,Math.PI*0.5);
            interTile(x1,y1,x,Math.PI*1.5,Math.PI*1.0);
        }
    }

    // style=\
    public void tileB(double x0,double y0) {
        double x1=x0+tileSize;
        double y1=y0+tileSize;

        for(double x=spaceBetweenLines;x<tileSize;x += spaceBetweenLines) {
            interTile(x0,y1,x,Math.PI*2.0,Math.PI*1.5);
            interTile(x1,y0,x,Math.PI*1.0,Math.PI*0.5);
        }
    }

    // Interpolate from (x0,y0) to (x1,y1) in steps of length iterSize.
    private void interTile(double cx,double cy,double radius,double a0, double a1) {
        turtle.drawArc(cx,cy,radius,a0,a1,10);
    }
}
