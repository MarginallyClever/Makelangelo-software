package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectFile;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.makeart.turtletool.ThickenLinesByIntensity;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;

/**
 * Generate a drawing where the thickness of the line is determined by the intensity of the image at that point.
 * @deprecated since 7.62.0 to be replaced with Donatello nodes.
 */
@Deprecated(since="7.62.0")
public class LineWeightByImageIntensity extends TurtleGenerator {
    private static final Logger logger = LoggerFactory.getLogger(LineWeightByImageIntensity.class);

    private final SelectDouble selectMaxLineWidth = new SelectDouble("thickness", Translator.get("LineWeightByImageIntensity.thickness"), maxLineWidth);
    private final SelectFile selectFile = new SelectFile("image", Translator.get("LineWeightByImageIntensity.image"),imageName,null);
    private final SelectDouble selectPenDiameter = new SelectDouble("pen diameter", Translator.get("penDiameter"), penDiameter);
    private final SelectDouble selectStepSize = new SelectDouble("step size", Translator.get("Converter_EdgeDetection.stepSize"), stepSize);

    // refinement of lines for sampling.  must be greater than zero.
    private static double stepSize = 1;
    // maximum thickness of the new line. must be greater than zero.
    private static double maxLineWidth = 3.0;
    // the pen diameter, controls spacing between passes.
    private static double penDiameter = 0.8;
    // source of weight image
    private static String imageName = null;

    private TransformedImage sourceImage;

    public LineWeightByImageIntensity() {
        super();

        add(selectMaxLineWidth);
        selectMaxLineWidth.addSelectListener(e->{
            maxLineWidth = Math.max(0.1,selectMaxLineWidth.getValue());
            generate();
        });

        add(selectPenDiameter);
        selectPenDiameter.addSelectListener(e->{
            penDiameter = Math.max(0.05,selectPenDiameter.getValue());
            generate();
        });

        add(selectStepSize);
        selectStepSize.addSelectListener(e->{
            stepSize = Math.max(0.1,selectStepSize.getValue());
            generate();
        });

        add(selectFile);
        selectFile.addSelectListener(e->{
            imageName = selectFile.getText();
            generate();
        });

    }

    @Override
    public String getName() {
        return Translator.get("LineWeightByImageIntensity.name");
    }

    @Override
    public void generate() {
        if(imageName==null || imageName.trim().isEmpty()) {
            return;
        }

        // load intensity image
        try (FileInputStream stream = new FileInputStream(imageName)) {
            sourceImage = new TransformedImage(ImageIO.read(stream));
        } catch(Exception e) {
            logger.error("failed to load intensity image. ",e);
            return;
        }
        scaleImage(1);  // fill paper

        var tool = new ThickenLinesByIntensity();
        Turtle turtle = tool.execute(myTurtle,sourceImage,stepSize,maxLineWidth,penDiameter);
        turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

        notifyListeners(turtle);
    }

    /**
     * mode 0 = fill paper
     * mode 1 = fit paper
     * @param mode the mode to scale the image
     */
    private void scaleImage(int mode) {
        Rectangle2D.Double rect = myPaper.getMarginRectangle();
        double width  = rect.getWidth();
        double height = rect.getHeight();

        boolean test;
        if (mode == 0) {
            test = width < height;  // fill paper
        } else {
            test = width > height;  // fit paper
        }

        float f;
        if( test ) {
            f = (float)( width / (double)sourceImage.getSourceImage().getWidth() );
        } else {
            f = (float)( height / (double)sourceImage.getSourceImage().getHeight() );
        }
        sourceImage.setScale(f,-f);
    }

    public void setMaxLineWidth(double maxLineWidth) {
        //selectMaxLineWidth.setValue(maxLineWidth);
        LineWeightByImageIntensity.maxLineWidth = maxLineWidth;
    }

    public void setImageName(String imageName) {
        //selectFile.setText(imageName);
        LineWeightByImageIntensity.imageName = imageName;
    }
}