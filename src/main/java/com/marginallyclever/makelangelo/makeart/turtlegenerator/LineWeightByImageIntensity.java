package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class LineWeightByImageIntensity extends TurtleGenerator {
    private static double thickness = 1.0;
    private static String imageName = null;

    public LineWeightByImageIntensity() {
        super();

        SelectDouble selectThickness = new SelectDouble("thickness", Translator.get("LineWeightByImageIntensity.thickness"),thickness);
        add(selectThickness);
        selectThickness.addPropertyChangeListener(e->{
            thickness = selectThickness.getValue();
            generate();
        });
        SelectFile selectFile = new SelectFile("image", Translator.get("LineWeightByImageIntensity.image"),imageName);
        add(selectFile);
        selectFile.addPropertyChangeListener(e->{
            imageName = selectFile.getText();
            generate();
        });
    }

    @Override
    public String getName() {
        return Translator.get("LineWeightByImageIntensity.name");
    }

    /**
     * Generate
     */
    @Override
    public void generate() {
        Turtle turtle = new Turtle();

        //previousTurtle.

        notifyListeners(turtle);
    }
}
