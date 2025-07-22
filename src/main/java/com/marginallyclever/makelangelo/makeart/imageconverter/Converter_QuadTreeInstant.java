package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Convert image to quad tree fractal
 * @author Mohammed Thaier
 * @since 7.41
 */
public class Converter_QuadTreeInstant extends ImageConverter{
    private TransformedImage img;
    private static int maxDepth = 5;
    private static int baseCutOff = 120;
    private static int cutOffIncrement = 15;

    public int getMaxDepth(){
        return maxDepth;
    }
    public void setMaxDepth(int value){
        maxDepth = value;
    }
    public int getCutOff(){
        return baseCutOff;
    }
    public void setCutOff(int value){
        baseCutOff = value;
    }
    public int getCutOffIncrement(){
        return cutOffIncrement;
    }
    public void setCutOffIncrement(int value){
        cutOffIncrement = value;
    }

    /**
     * Determines what sides of the box should be drawn,
     * useful for canceling duplicate lines
     */
    static class BoxCondition {
        public Boolean drawTop;
        public Boolean drawBottom;
        public Boolean drawLeft;
        public Boolean drawRight;

        public BoxCondition(boolean _top, boolean _bottom, boolean _left, boolean _right) {
            drawTop = _top;
            drawBottom = _bottom;
            drawLeft = _left;
            drawRight = _right;
        }
    }

    public Converter_QuadTreeInstant() {
        super();

        SelectSlider selectMaxDepth = new SelectSlider("maxDepth", Translator.get("Converter_QuadTreeInstant.maxDepth"), 8, 1, getMaxDepth());
        SelectSlider selectBaseCutOff = new SelectSlider("cutOff", Translator.get("Converter_QuadTreeInstant.baseCutOff"), 255, 1, getCutOff());
        SelectSlider selectCutOffIncrement = new SelectSlider("cutOff", Translator.get("Converter_QuadTreeInstant.cutOffIncrement"), 255, 1, getCutOffIncrement());

        add(selectMaxDepth);
        add(selectBaseCutOff);
        add(selectCutOffIncrement);

        selectMaxDepth.addSelectListener(evt -> {
            setMaxDepth((int) evt.getNewValue());
            fireRestart();
        });
        selectBaseCutOff.addSelectListener(evt -> {
            setCutOff((int) evt.getNewValue());
            fireRestart();
        });
        selectCutOffIncrement.addSelectListener(evt -> {
            setCutOffIncrement((int) evt.getNewValue());
            fireRestart();
        });
    }

    @Override
    public String getName(){
        return Translator.get("Converter_QuadTreeInstant");
    }

    @Override
    public void start(Paper paper, TransformedImage image){
        super.start(paper, image);

        FilterDesaturate bw = new FilterDesaturate(myImage);
        img = bw.filter();

        turtle = new Turtle();
        turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

        Rectangle2D.Double rect = myPaper.getMarginRectangle();
        Point2d topLeftP = new Point2d(rect.getMaxX(),rect.getMaxY());
        Point2d bottomRightP = new Point2d(rect.getMinX(),rect.getMinY());
        BoxCondition boxCondition = new BoxCondition(true,true,true,true);
        recurse(topLeftP, bottomRightP, boxCondition, 0,baseCutOff);
        fireConversionFinished();
    }

    private float getAverageOfRegion(Point2d topLeft, Point2d bottomRight) {
        float sum = 0;
        int c = 0;
        for(int i=(int)topLeft.x; i<bottomRight.x; i++){
            for(int k=(int)bottomRight.y; k<topLeft.y; k++){
                sum += img.sample1x1(i,k);
                c++;
            }
        }
        if(c==0) return 0;
        return sum/c;
    }

    private void recurse(Point2d topLeft, Point2d bottomRight, BoxCondition boxCondition, int curDepth, int cutOff){
        if(curDepth > maxDepth) return;

        float average = getAverageOfRegion(topLeft, bottomRight);
        // if this region is brighter than the cutoff, stop.
        if(average > cutOff) return;

        // only draw the sides of the box that are needed
        if(boxCondition.drawTop) {
            drawLine(topLeft,
                     new Point2d(bottomRight.x, topLeft.y));
        }
        if(boxCondition.drawBottom) {
            drawLine(new Point2d(topLeft.x, bottomRight.y),
                     bottomRight);
        }
        if(boxCondition.drawLeft) {
            drawLine(topLeft,
                     new Point2d(topLeft.x, bottomRight.y));
        }
        if(boxCondition.drawRight) {
            drawLine(new Point2d(bottomRight.x, topLeft.y),
                     bottomRight);
        }

        // go deeper, but each time lower the cutoff.  darker regions will start to fail the test.
        int newCutOff = cutOff - cutOffIncrement;
        //Drawing sub-fractals
        int w2 = (int)(bottomRight.x - topLeft.x)/2;
        int h2 = (int)(topLeft.y - bottomRight.y)/2;
        // top left corner
        recurse(topLeft,
                new Point2d(topLeft.x + w2, topLeft.y-h2),
                new BoxCondition(false,true,false,true),
              curDepth+1,
                newCutOff);
        // top right corner
        recurse(new Point2d(topLeft.x + w2, topLeft.y),
                new Point2d(bottomRight.x, topLeft.y-h2),
                new BoxCondition(false,true,true,false),
                curDepth+1,
                newCutOff);
        // bottom left corner
        recurse(new Point2d(topLeft.x, topLeft.y-h2),
                new Point2d(topLeft.x+w2, bottomRight.y),
                new BoxCondition(true,false,false,true),
                curDepth+1,
                newCutOff);
        // bottom right corner
        recurse(new Point2d(topLeft.x+w2, topLeft.y-h2),
                bottomRight,
                new BoxCondition(true,false,true,false),
                curDepth+1,
                newCutOff);
    }

    private void drawLine(Point2d p1, Point2d p2) {
        double px = myPaper.getCenterX();
        double py = myPaper.getCenterY();

        turtle.jumpTo(px+p1.x,py+p1.y);
        turtle.moveTo(px+p2.x,py+p2.y);
    }
}