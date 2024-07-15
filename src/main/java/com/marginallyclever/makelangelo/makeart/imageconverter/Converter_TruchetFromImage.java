package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.truchet.TruchetDiagonal;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.pen.Palette;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;

/**
 * Choose from two truchet tile patterns based on the intensity of a source image.
 * @author Dan Royer
 * @since 7.48.0
 */
public class Converter_TruchetFromImage extends ImageConverter {
    public static int spaceBetweenLines = 5;
    public static int linesPerTile = 5;

    public Converter_TruchetFromImage() {
        SelectSlider selectSpacing = new SelectSlider("size",Translator.get("Converter_TruchetFromImage.spacing"), 20,1,spaceBetweenLines);
        SelectSlider selectLinesPerTile = new SelectSlider("sampleRate",Translator.get("Converter_TruchetFromImage.linesPerTile"),20,1,linesPerTile);

        add(selectSpacing);
        add(selectLinesPerTile);

        selectSpacing.addSelectListener(evt->{
            spaceBetweenLines = (int)evt.getNewValue();
            fireRestart();
        });
        selectLinesPerTile.addSelectListener(evt->{
            linesPerTile = (int) evt.getNewValue();
            fireRestart();
        });
    }

    /**
     * @return the translated name.
     */
    @Override
    public String getName() {
        return Translator.get("Converter_TruchetFromImage.name");
    }


    @Override
    public void start(Paper paper, Palette palette, TransformedImage image) {
        super.start(paper, palette, image);

        FilterDesaturate desaturate = new FilterDesaturate(image);
        TransformedImage result = desaturate.filter();

        turtle = new Turtle();

        TruchetDiagonal truchet = new TruchetDiagonal(turtle,spaceBetweenLines,linesPerTile);
        double tileSize = spaceBetweenLines * linesPerTile;

        Rectangle2D.Double rect = myPaper.getMarginRectangle();
        double adjx = (rect.getWidth() % tileSize)/2;
        double adjy = (rect.getHeight() % tileSize)/2;
        double minx = rect.getMinX()+adjx;
        double miny = rect.getMinY()+adjy;
        double maxx = rect.getMaxX()-adjx;
        double maxy = rect.getMaxY()-adjy;
        double px = myPaper.getCenterX();
        double py = myPaper.getCenterY();

        for(double y=miny;y<maxy;y+=tileSize) {
            for(double x=minx;x<maxx;x+=tileSize) {
                int intensity = result.sample(x,y,x+tileSize,y+tileSize);
                if(intensity>128) truchet.tileA(px+x,py+y);
                else              truchet.tileB(px+x,py+y);
            }
        }

        fireConversionFinished();
    }
}
