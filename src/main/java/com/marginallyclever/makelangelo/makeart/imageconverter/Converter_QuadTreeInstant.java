package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Convert image to quad tree fractal
 * @author Mohammed Thaier
 * @since ?
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
    static class BoxCondition{
            public Boolean drawTop =false;
            public Boolean drawBottom =false;
            public Boolean drawLeft =false;
            public Boolean drawRight =false;
        public BoxCondition(boolean _top, boolean _bottom, boolean _left, boolean _right){
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

        selectMaxDepth.addPropertyChangeListener(evt -> {
            setMaxDepth((int) evt.getNewValue());
            fireRestart();
        });
        selectBaseCutOff.addPropertyChangeListener(evt -> {
            setCutOff((int) evt.getNewValue());
            fireRestart();
        });
        selectCutOffIncrement.addPropertyChangeListener(evt -> {
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

        Filter_Greyscale bw = new Filter_Greyscale(255);
        img = bw.filter(myImage);

        Point2D topLeftP = new Point2D(myPaper.getMarginLeft(),paper.getMarginTop());
        Point2D bottomRightP = new Point2D(myPaper.getMarginRight(), paper.getMarginBottom());

        turtle = new Turtle();

        BoxCondition boxCondition = new BoxCondition(true,true,true,true);
        //BoxCondition boxCondition = new BoxCondition(false,false,false,false);
        Fractal(topLeftP, bottomRightP, boxCondition, 0,baseCutOff);
        fireConversionFinished();
    }

    private void Fractal(Point2D topLeft, Point2D bottomRight, BoxCondition boxCondition, int curDepth, int cutOff){
        if(curDepth > maxDepth){
            return;
        }

        int xDiff = (int)(bottomRight.x - topLeft.x);
        int yDiff = (int)(topLeft.y - bottomRight.y);
        float sum = 0;
        int c = 0;
        for(int i=(int)topLeft.x; i<bottomRight.x; i++){
            for(int k=(int)bottomRight.y; k<topLeft.y; k++){
                float p = img.sample1x1(i,k);
                sum += p;
                c++;
            }
        }
        if(sum/c > cutOff){
            return;
        }
        System.out.println("Sum: "+sum);

        if(boxCondition.drawTop){
            Point2D p1 = new Point2D(topLeft.x, topLeft.y);
            Point2D p2 = new Point2D(bottomRight.x, topLeft.y);
            DrawLine(p1, p2);
        }
        if(boxCondition.drawBottom){
            Point2D p1 = new Point2D(topLeft.x, bottomRight.y);
            Point2D p2 = new Point2D(bottomRight.x, bottomRight.y);
            DrawLine(p1, p2);
        }
        if(boxCondition.drawLeft){
            Point2D p1 = new Point2D(topLeft.x, topLeft.y);
            Point2D p2 = new Point2D(topLeft.x, bottomRight.y);
            DrawLine(p1, p2);
        }
        if(boxCondition.drawRight){
            Point2D p1 = new Point2D(bottomRight.x, topLeft.y);
            Point2D p2 = new Point2D(bottomRight.x, bottomRight.y);
            DrawLine(p1, p2);
        }

        //Drawing sub-fractals
        int newCutOff = cutOff-cutOffIncrement;
        Fractal(
                new Point2D(topLeft.x, topLeft.y),
                new Point2D(topLeft.x + xDiff/2, topLeft.y-yDiff/2),
                new BoxCondition(false,true,false,true),
              curDepth+1,
                newCutOff
        );
        Fractal(
                new Point2D(topLeft.x + xDiff/2, topLeft.y),
                new Point2D(bottomRight.x, topLeft.y-yDiff/2),
                new BoxCondition(false,true,true,false),
                curDepth+1,
                newCutOff
        );
        Fractal(
                new Point2D(topLeft.x, topLeft.y-yDiff/2),
                new Point2D(topLeft.x+xDiff/2, bottomRight.y),
                new BoxCondition(true,false,false,true),
                curDepth+1,
                newCutOff
        );
        Fractal(
                new Point2D(topLeft.x+xDiff/2, topLeft.y-yDiff/2),
                new Point2D(bottomRight.x, bottomRight.y),
                new BoxCondition(true,false,true,false),
                curDepth+1,
                newCutOff
        );

    }
    private void DrawLine(Point2D p1, Point2D p2){
        turtle.jumpTo(p1.x,p1.y);
        turtle.moveTo(p2.x,p2.y);
    }

}