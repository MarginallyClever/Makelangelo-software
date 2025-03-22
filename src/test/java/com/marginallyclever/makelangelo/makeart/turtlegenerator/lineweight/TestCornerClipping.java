package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;

public class TestCornerClipping {
    private static final int radius = 50;
    private static final int margin = 20;
    private static int x = 0;
    private static int y = 0;
    private static int k=0;

    public static void main(String[] args) {
        Turtle result = new Turtle();

        PreferencesHelper.start();  // required by Translator
        Translator.start();  // required by LineWeightByImageIntensity

        for(int i=0;i<=60;i+=5) {
            System.out.println("i="+i);
            var hands = GenerateClockHands.generateClockHands(0,i,radius);
            thickenClockHands(hands,result);
        }

        if(k==0) {
            System.out.println("No hands thickened.");
            return;
        }
        try {
            TurtleFactory.save(result, "corners.svg", PlotterSettingsManager.buildMakelangelo5());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done.");
        System.exit(0);
    }

    private static void thickenClockHands(Turtle hands,Turtle result) {
        var paper = new Paper();
        paper.setPaperSize(100,100,0,0);
        paper.setPaperMargin(1.0);

        var calculator = new LineWeightByImageIntensity();
        calculator.setPaper(paper);
        calculator.setTurtle(new Turtle());
        calculator.setMaxLineWidth(5);
        calculator.setImageName("src/test/resources/com/marginallyclever/makelangelo/makeart/turtlegenerator/lineweight/black-dot-100.png");
        calculator.addListener(thick->{
            thick.translate(x,y);
            x += radius*2+margin;
            k++;
            if( 0 == (k % 3) ) {
                y -= radius*2+margin;
                x = 0;
            }
            result.add(thick);
        });
        calculator.setTurtle(hands);
        calculator.generate();
    }
}
