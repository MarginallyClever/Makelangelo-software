package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.donatello.ports.InputRange;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterExtendedDifferenceOfGaussians;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterGaussianBlur;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.image.BufferedImage;

/**
 * <p>Detect edges in a {@link BufferedImage} and produce a {@link Turtle}.  Uses
 * <a href="http://en.wikipedia.org/wiki/Marching_squares">marching squares</a> to detect edges.</p>
 */
public class DetectEdges extends Node {
    private final InputImage src = new InputImage("image");
    private final InputRange cutoff = new InputRange("cutoff",128,255,0);
    private final OutputTurtle output = new OutputTurtle("turtle");

    public DetectEdges() {
        super("DetectEdges");
        addPort(src);
        addPort(cutoff);
        addPort(output);
    }

    @Override
    public void update() {
        BufferedImage img = src.getValue();
        var edge = cutoff.getValue();

        setComplete(0);
        FilterDesaturate desaturates = new FilterDesaturate(new TransformedImage(img));
        var img2 = desaturates.filter();

        FilterGaussianBlur blur1 = new FilterGaussianBlur(img2, 1);
        FilterGaussianBlur blur2 = new FilterGaussianBlur(img2, 4);
        TransformedImage img3 = blur1.filter();
        TransformedImage img4 = blur2.filter();
        FilterExtendedDifferenceOfGaussians dog = new FilterExtendedDifferenceOfGaussians(img3,img4,20);
        var img5 = dog.filter();

        output.setValue(marchingSquares(img5.getSourceImage(),edge));
        setComplete(100);
    }

    Turtle marchingSquares(BufferedImage img,int edge) {
        var turtle = new Turtle();
        int height  = img.getHeight();
        int width   = img.getWidth();

        int size=width*height;
        int i=0;
        for(int y=0;y<height-1;++y) {
            for(int x=0;x<width-1;++x) {
                marchSquare(img,turtle,edge,x, y);
                setComplete((int)(i * 100.0/size));
            }
        }

        var x0 = -width/2;
        var y0 = -height/2;
        turtle.translate(x0,y0);

        return turtle;
    }

    private int brightness(int color) {
        return color & 0xFF;
    }

    void marchSquare(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        int in0 = brightness(img.getRGB(x0,y0)) >= edge ? 1:0;
        int in1 = brightness(img.getRGB(x1,y0)) >= edge ? 2:0;
        int in2 = brightness(img.getRGB(x1,y1)) >= edge ? 4:0;
        int in3 = brightness(img.getRGB(x0,y1)) >= edge ? 8:0;
        int code = in0 | in1 | in2 | in3;

        // 15 is a mirror of 1 and so on.
        if(code>7) code = 15-code;

        switch(code) {
            case 0:  break;
            case 1:  case1(img,turtle,edge,x0,y0);  break;
            case 2:  case2(img,turtle,edge,x0,y0);  break;
            case 3:  case3(img,turtle,edge,x0,y0);  break;
            case 4:  case4(img,turtle,edge,x0,y0);  break;
            case 5:  case5(img,turtle,edge,x0,y0);  break;
            case 6:  case6(img,turtle,edge,x0,y0);  break;
            case 7:  case7(img,turtle,edge,x0,y0);  break;
        }
    }

    float lerp(float a,float b,float v) {
        return a + (b - a) * v;
    }

    Point2D lerpEdge(BufferedImage img,int edge,int x0, int y0, int x1, int y1) {
        float in0 = brightness(img.getRGB(x0,y0));
        float in1 = brightness(img.getRGB(x1,y1));

        float v = (edge-in0) / (in1-in0);
        v=Math.max(0,Math.min(1,v));
        float x3 = lerp((float)x0,(float)x1,v);
        float y3 = lerp((float)y0,(float)y1,v);
        return new Point2D(x3,y3);
    }

    void line(Turtle turtle,Point2D a,Point2D b) {
        turtle.jumpTo(a.x,a.y);
        turtle.moveTo(b.x,b.y);
    }

    void case1(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x0,y0,x0,y1);
        Point2D b = lerpEdge(img,edge,x0,y0,x1,y0);
        line(turtle,a,b);
    }

    void case2(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x1,y0,x0,y0);
        Point2D b = lerpEdge(img,edge,x1,y0,x1,y1);
        line(turtle,a,b);
    }

    // 1 + 2
    void case3(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x0,y0,x0,y1);
        Point2D b = lerpEdge(img,edge,x1,y0,x1,y1);
        line(turtle,a,b);
    }

    void case4(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x1,y1,x0,y1);
        Point2D b = lerpEdge(img,edge,x1,y1,x1,y0);
        line(turtle,a,b);
    }

    // 1 + 4
    void case5(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        case1(img,turtle,edge,x0,y0);
        case4(img,turtle,edge,x0,y0);
    }

    // 2 + 4
    void case6(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x0,y0,x1,y0);
        Point2D b = lerpEdge(img,edge,x0,y1,x1,y1);
        line(turtle,a,b);
    }

    // 1+2+4
    void case7(BufferedImage img,Turtle turtle,int edge,int x0,int y0) {
        int x1 = x0+1;
        int y1 = y0+1;
        Point2D a = lerpEdge(img,edge,x0,y1,x0,y0);
        Point2D b = lerpEdge(img,edge,x0,y1,x1,y1);
        line(turtle,a,b);
    }
}
